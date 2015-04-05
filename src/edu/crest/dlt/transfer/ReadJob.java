/* $Id: ReadJob.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.transfer;

import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.crest.dlt.exception.ReadException;
import edu.crest.dlt.exnode.Exnode;
import edu.crest.dlt.exnode.Mapping;
import edu.crest.dlt.utils.Configuration;

public class ReadJob extends ConcurrentJob
{
	private static Logger log = Logger.getLogger(ReadJob.class.getName());

	public enum state_readjob {
		nascent, ready, reading_data, read_data_buffered, done, failed,
	}

	public state_readjob state;

	public final Exnode exnode_to_read;
	private List<Mapping> mappings_to_read;
	private final long offset_to_read;
	private final long bytes_to_read;

	private Set<TransferThread> transfer_threads;
	public byte[] bytes_to_write;
	public int count_depots_tried = 0;

	public ReadJob(int id, Exnode exnode_to_read, List<Mapping> mappings_to_read,
			long offset_to_read, long bytes_to_read)
	{
		super(id);
		this.exnode_to_read = exnode_to_read;
		this.mappings_to_read = mappings_to_read;
		this.offset_to_read = offset_to_read;
		this.bytes_to_read = bytes_to_read;

		transfer_threads = new HashSet<TransferThread>();
		state = state_readjob.nascent;
		ready();
	}

	/**
	 * @return is this job ready to be performed
	 */
	public boolean ready()
	{
		if (state == state_readjob.nascent) {
			/* check whether at least one mapping is accessible */
			for (Mapping mapping : mappings_to_read) {
				if (mapping.accessible()) {
					state = state_readjob.ready;
					return true;
				}
			}
			return false;
		}
		/* if not nascent; it is ready anyways */
		return true;
	}

	public int count_ready()
	{
		int count_ready = 0;
		for (Mapping mapping : mappings_to_read) {
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
		switch (state) {
			case nascent:
				return this + " [NASCENT] no mapping tested accessible.";

			case ready:
				return this + " [READY] " + count_ready() + "/" + mappings_to_read.size()
						+ " mappings accessible.";

			case reading_data:
				return this + " [READING] [" + offset_to_read + " - "
						+ (offset_to_read + bytes_to_read - 1) + "](" + bytes_to_read + "B).";

			case read_data_buffered:
				return this + " [BUFFERED] [" + offset_to_read + " - "
						+ (offset_to_read + bytes_to_read - 1) + "](" + bytes_to_read + "B).";

			case done:
				return this + " [DONE] [" + offset_to_read + " - " + (offset_to_read + bytes_to_read - 1)
						+ "](" + bytes_to_read + "B) written to " + exnode_to_read.output_filename();

			case failed:
				return this + " [FAILED " + count_failed() + "/" + Configuration.dlt_exnode_read_retries_max
						+ "] [" + offset_to_read + " - " + (offset_to_read + bytes_to_read - 1)
						+ "](" + bytes_to_read + "B) of " + exnode_to_read.filename();

			default:
				return this + " [UNKNOWN-STATE]";
		}
	}

	public long offset_to_read()
	{
		return offset_to_read;
	}

	public long bytes_to_read()
	{
		return bytes_to_read;
	}

	public synchronized void transfer_thread_register()
	{
		transfer_threads.add((TransferThread) Thread.currentThread());
	}

	public synchronized void transfer_thread_unregister()
	{
		transfer_threads.remove((TransferThread) Thread.currentThread());
	}

	// public Mapping mapping(Depot depot)
	// {
	// for (Mapping mapping : mappings_to_read) {
	// if (mapping.allocation.depot.equals(depot)) {
	// return mapping;
	// }
	// }
	// return null;
	// }

	// public List<Mapping> mappings()
	// {
	// return mappings_to_read;
	// }

	public List<Mapping> mappings()
	{
		return mappings_to_read;
	}

	public synchronized Socket try_start(Mapping mapping_to_read)
	{
		if (isInterrupted())
			return null;

		/* request the depot for a new free socket to read on */
		Socket socket_to_read = exnode_to_read.try_start(mapping_to_read);

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
		exnode_to_read
				.try_end(mapping_tried, socket_tried, offset_to_read, bytes_transferred, status());

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
		 * If already succeeded, return (CAUTION: do not check
		 * read_statistics.count_succeeded since it only accounts for
		 * read_data_buffered state)
		 */
		if (state == state_readjob.done) {
			log.info(status());
			return;
		}

		/*
		 * If a previous attempt had requested a write-to-output-file, and if the
		 * output-file-contains the data, succeed
		 */
		if (state == state_readjob.read_data_buffered) {
			// if (exnode_to_read.output_file_contains(offset_to_read, bytes_to_read))
			// {
			// state = state_readjob.done;
			// }
			if (exnode_to_read.output_file_contains(this)) {
				state = state_readjob.done;
			}
			log.info(status());
			return;
		}

		/*
		 * If failed enough number of times, fail permanently/irreparably
		 */
		// if (count_failed() >= Configuration.bd_exnode_read_retries_max) {
		// state = state_readjob.failed;
		// log.severe(this + " " + status());
		// // exnode_to_read.transfer_cancel();
		// return;
		// }

		if (count_tried() >= Configuration.dlt_exnode_read_retries_max) {
			log.warning(this + ": ran out of retry attempts.");
			/*
			 * no state-change; since other threads might actually be trying it (and
			 * in a better position to provide a more accurate status)
			 */
			state = state_readjob.failed;
			log.severe(status());
			return;
		}

		if (isInterrupted())
			return;

		Mapping mapping_to_read = null;
		Socket socket_to_read = null;
		int try_id = CONCURRENT_TRY_ERROR;

		synchronized (mappings_to_read) {
			/* obtain the best mapping out of applicable target mappings */
			mapping_to_read = exnode_to_read.mapping_best(mappings_to_read);

			/* connect to the target_depot */
			socket_to_read = try_start(mapping_to_read);
			try_id = count_tried();
			if (try_id == CONCURRENT_TRY_ERROR) {
				log.severe(this + " failed to obtain concurrent try id#.");
				try_end(try_id, mapping_to_read, socket_to_read, 0);
				return;
			}

			if (socket_to_read == null) {
				log.severe(this + " failed to obtain socket to " + mapping_to_read.allocation.depot);
				try_end(try_id, mapping_to_read, socket_to_read, 0);
				return;
			}
		}

		if (isInterrupted()) {
			try_end(try_id, mapping_to_read, socket_to_read, 0);
			return;
		}

		state = state_readjob.reading_data;
		/* read from selected mapping and write to exnode's output file */
		byte[] buffer_to_write = null;
		try {
			buffer_to_write = mapping_to_read.read(socket_to_read, bytes_to_read, offset_to_read
					- mapping_to_read.exnode_offset(), 0);
		} catch (ReadException e) {
		}

		if (isInterrupted() || buffer_to_write == null || buffer_to_write.length != bytes_to_read) {
			state = state_readjob.failed;
			try_end(try_id, mapping_to_read, socket_to_read, 0);
			return;
		}

		state = state_readjob.read_data_buffered;
		bytes_to_write = buffer_to_write;
		// exnode_to_read.output_file_write(buffer_to_write, offset_to_read,
		// bytes_to_read);
//		System.out.println(this + " read data buffered.");
		try_end(try_id, mapping_to_read, socket_to_read, bytes_to_read);
	}

	@Override
	public boolean isInterrupted()
	{
		TransferThread transfer_thread = (TransferThread) Thread.currentThread();
		return transfer_thread.isInterrupted(); // do not reset the interrupted flag
	}

	public String toString()
	{
		return exnode_to_read.filename() + " Read" + super.toString() + "/"
				+ Configuration.dlt_exnode_read_retries_max;
	}
}
