/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/* $Id: ReadJob.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.transfer;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.crest.dlt.exception.WriteException;
import edu.crest.dlt.exnode.Exnode;
import edu.crest.dlt.exnode.Mapping;
import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.utils.Configuration;

public class WriteJob extends ConcurrentJob
{
	private static Logger log = Logger.getLogger(WriteJob.class.getName());

	public enum state_writejob {
		nascent, write_data_requested, ready, preparing_mappings, writing_data, done, failed,
	}

	public state_writejob state;

	public final Exnode exnode_to_write;
	public List<Depot> depots_to_write;
	private Set<Mapping> mappings_to_write;

	private final long offset_to_write;
	private final long bytes_to_write;
	public long time_allocation;

	private Set<TransferThread> transfer_threads;
	public byte[] bytes_read;

	public WriteJob(int id, Exnode exnode_to_write, List<Depot> depots_to_write,
			long offset_to_write, long bytes_to_write, long time_allocation)
	{
		super(id);
		this.exnode_to_write = exnode_to_write;
		this.depots_to_write = depots_to_write;
		this.offset_to_write = offset_to_write;
		this.bytes_to_write = bytes_to_write;
		this.time_allocation = time_allocation;

		mappings_to_write = new HashSet<Mapping>();
		transfer_threads = new HashSet<TransferThread>();
		state = state_writejob.nascent;
		ready();
	}

	/**
	 * @return is this job ready to be performed
	 */
	public boolean ready()
	{
		if (state == state_writejob.write_data_requested) {
			/* check whether the data has been copied into memory */
			return exnode_to_write.input_file_buffer_contains(this);
		}
		/* if not; it is not ready unless it is already writing */
		return state == state_writejob.writing_data;
	}

	public int count_ready()
	{
		int count_ready = 0;
		for (Mapping mapping : mappings_to_write) {
			if (mapping.accessible()) {
				count_ready++;
			}
		}
		return count_ready;
	}

	/**
	 * @return status: a predefined function of current state
	 */
	public String status()
	{
		StringBuffer depots_remaining = new StringBuffer();
		for (Mapping mapping_to_write : mappings_to_write) {
			depots_remaining.append(mapping_to_write.allocation.depot).append(" ");
		}

		switch (state) {
			case nascent:
				return this + " [NASCENT] no mappings prepared.";

			case write_data_requested:
				return this + " [REQUESTED, WAITING] [" + offset_to_write + " - "
						+ (offset_to_write + bytes_to_write - 1) + "](" + bytes_to_write + "B).";

			case ready:
				return this + " [BUFFERED] [" + offset_to_write + " - "
						+ (offset_to_write + bytes_to_write - 1) + "](" + bytes_to_write + "B).";

			case preparing_mappings:
				return this + " [PREPARING] " + mappings_to_write.size() + "/" + exnode_to_write.copies()
						+ " depots : " + depots_remaining;

			case writing_data:
				return this + " [WRITING] [" + offset_to_write + " - "
						+ (offset_to_write + bytes_to_write - 1) + "](" + bytes_to_write + "B) to "
						+ mappings_to_write.size() + "/" + exnode_to_write.copies() + " depots : "
						+ depots_remaining;

			case done:
				return this + " [DONE] [" + offset_to_write + " - "
						+ (offset_to_write + bytes_to_write - 1) + "](" + bytes_to_write + "B) "
						+ exnode_to_write.copies() + " depots.";

			case failed:
				return this + " [FAILED " + count_failed() + "/"
						+ Configuration.dlt_exnode_write_retries_max + "] [" + offset_to_write + " - "
						+ (offset_to_write + bytes_to_write - 1) + "](" + bytes_to_write + "B) of "
						+ exnode_to_write.filename();

			default:
				return this + " [UNKNOWN-STATE]";
		}
	}

	public long offset_to_write()
	{
		return offset_to_write;
	}

	public long bytes_to_write()
	{
		return bytes_to_write;
	}

	public synchronized void transfer_thread_register()
	{
		transfer_threads.add((TransferThread) Thread.currentThread());
	}

	public synchronized void transfer_thread_unregister()
	{
		transfer_threads.remove((TransferThread) Thread.currentThread());
	}

	public List<Mapping> mappings()
	{
		List<Mapping> mappings = new ArrayList<Mapping>();
		for (Mapping mapping : mappings_to_write) {
			mappings.add(mapping);
		}
		return mappings;
	}

	public synchronized Socket try_start(Mapping mapping_to_write)
	{
		if (isInterrupted())
			return null;

		/* request the depot for a new free socket to read on */
		Socket socket_to_read = exnode_to_write.try_start(mapping_to_write);

		/* if free socket is found, start the try */
		// if (socket_to_read != null) {
		super.try_start(); // count this try
		transfer_thread_register();
		// }
		return socket_to_read;
	}

	public synchronized void try_end(int try_id, Mapping mapping_tried, Socket socket_tried,
			long bytes_transferred)
	{
		/* first release the socket for others to use */
		exnode_to_write.try_end(mapping_tried, socket_tried, offset_to_write, bytes_transferred,
				status());

		/* record try statistics and unregister transfer-thread */
		if (try_id != CONCURRENT_TRY_ERROR) {
			super.try_end(try_id, bytes_transferred);
		}
		transfer_thread_unregister();
	}

	@Override
	public void execute() throws Throwable
	{
		/*
		 * If already succeeded, return (CAUTION: do not check count_succeeded since
		 * it only accounts for write_data_buffered state)
		 */
		if (state == state_writejob.done) {
			log.info(status());
			return;
		}

		/*
		 * If tried enough number of times, fail permanently/irreparably
		 */
		if (count_tried() >= Configuration.dlt_exnode_write_retries_max) {
			log.warning(this + ": ran out of tries.");
			state = state_writejob.failed;
			log.severe(status());
			return;
		}

		/*
		 * If this WriteJob has been selected for writing for the very first time,
		 * request the exnode for the contents of its input file
		 */
		if (state == state_writejob.nascent) {
			state = state_writejob.write_data_requested;
			exnode_to_write.input_file_read(this);
		}

		/*
		 * If a previous attempt had requested a block of data from input file, and
		 * if the input-file-buffer-contains the data, proceed
		 */
		if (state == state_writejob.write_data_requested) {
			if (exnode_to_write.input_file_buffer_contains(this)) {
				state = state_writejob.ready;
			}
			log.info(status());

			if (state != state_writejob.ready) {
				return;
			}
		}

		if (isInterrupted())
			return;

		/*
		 * if this job had not previously obtained all the necessary mappings to
		 * write, and had not started writing to them
		 */
		if (state != state_writejob.writing_data) {
			state = state_writejob.preparing_mappings;

			// Collections.shuffle(depots_to_write);
			/* request a set of mappings to write to */
			for (int i = 0; i < (depots_to_write.size() / exnode_to_write.copies() + 1); i++) {
				if (mappings_to_write.size() == exnode_to_write.copies()) {
					break;
				}

				List<Depot> depots_best = null;
				synchronized (depots_to_write) {
					depots_best = exnode_to_write.depots_best(depots_to_write, exnode_to_write.copies()
							- mappings_to_write.size());
				}
				List<TransferThread> allocation_requestors = new ArrayList<TransferThread>(
						depots_best.size());
				for (Depot depot_to_write : depots_best) {
					allocation_requestors.add(new TransferThread(() -> {
						Mapping mapping_to_write = exnode_to_write.add(depot_to_write, offset_to_write,
								bytes_to_write, 0, bytes_to_write, 0, time_allocation);

						System.out.println(depot_to_write.status()
								+ (mapping_to_write != null ? ": allocated" : ": cannot allocate"));

						if (mapping_to_write != null) {
							synchronized (mapping_to_write) {
								mappings_to_write.add(mapping_to_write);
							}
						}
					}));
					allocation_requestors.get(allocation_requestors.size() - 1).start();
				}

				allocation_requestors.forEach((allocation_requestor) -> {
					try {
						allocation_requestor.join();
					} catch (Exception e) {
					}
				});
			}
		}

		if (state == state_writejob.preparing_mappings
				&& mappings_to_write.size() != exnode_to_write.copies()) {
			log.warning(status() + "; waiting for "
					+ (exnode_to_write.copies() - mappings_to_write.size()) + " more depots.");
			return;
		}

		state = state_writejob.writing_data;
		/* write to the selected mappings */
		for (Iterator<Mapping> mappings_itr = mappings_to_write.iterator(); mappings_itr.hasNext();) {
			Mapping mapping_to_write = mappings_itr.next();

			/* connect to the target mapping */
			Socket socket_to_write = try_start(mapping_to_write);
			int try_id = count_tried();

			if (try_id == CONCURRENT_TRY_ERROR) {
				log.severe(this + " failed to obtain concurrent try id#.");
				try_end(try_id, mapping_to_write, socket_to_write, 0);
				continue;
			}

			if (socket_to_write == null) {
				log.severe(this + " failed to obtain socket to " + mapping_to_write.allocation.depot);
				try_end(try_id, mapping_to_write, socket_to_write, 0);
				continue;
			}

			if (isInterrupted()) {
				try_end(try_id, mapping_to_write, socket_to_write, 0);
				return;
			}

			/* send/write the mapping data */
			int bytes_written = 0;
			try {
				bytes_written = mapping_to_write.write(socket_to_write, bytes_read, bytes_to_write);
			} catch (WriteException e) {
				e.printStackTrace();
			}

			if (isInterrupted() || bytes_written == 0 || bytes_written != bytes_to_write) {
				try_end(try_id, mapping_to_write, socket_to_write, 0);
			} else {
				try_end(try_id, mapping_to_write, socket_to_write, bytes_written);
				mappings_itr.remove();
			}
		}

		if (mappings_to_write.size() == 0) {
			state = state_writejob.done;
		} else {
			log.warning(status());
		}
	}

	@Override
	public boolean isInterrupted()
	{
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		return transfer_thread.isInterrupted(); // do not reset the interrupted flag
	}

	public String toString()
	{
		return exnode_to_write.filename() + " Write" + super.toString() + "/"
				+ Configuration.dlt_exnode_write_retries_max;
	}
}
