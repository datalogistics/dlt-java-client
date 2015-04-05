package edu.crest.dlt.exnode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.crest.dlt.bd.diskio.FileReadThread;
import edu.crest.dlt.bd.diskio.FileReadThread.reader_state;
import edu.crest.dlt.bd.diskio.FileWriteThread;
import edu.crest.dlt.bd.diskio.FileWriteThread.writer_state;
import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.exception.IBPException;
import edu.crest.dlt.exception.SerializeException;
import edu.crest.dlt.exnode.metadata.Metadata;
import edu.crest.dlt.exnode.metadata.MetadataContainer;
import edu.crest.dlt.exnode.metadata.MetadataInteger;
import edu.crest.dlt.exnode.metadata.MetadataString;
import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.transfer.ConcurrentJob;
import edu.crest.dlt.transfer.Job;
import edu.crest.dlt.transfer.JobQueue;
import edu.crest.dlt.transfer.ProgressListener;
import edu.crest.dlt.transfer.ReadJob;
import edu.crest.dlt.transfer.ReadThread;
import edu.crest.dlt.transfer.Scoreboard;
import edu.crest.dlt.transfer.TransferThread;
import edu.crest.dlt.transfer.WriteJob;
import edu.crest.dlt.transfer.WriteThread;
import edu.crest.dlt.utils.Configuration;
import edu.crest.dlt.utils.MappedOffsets;

public class Exnode extends MetadataContainer
{
	public static final Logger log = Logger.getLogger(Exnode.class.getName());

	public enum service_exnode {
		read, write
	}

	public enum state_exnode {
		nascent, ready, transit, done, failed,
	}

	private state_exnode state;

	private SortedMap<MappedOffsets, List<Mapping>> mappings_sorted;
	private long length = 0;

	private JobQueue transfer_jobs;
	private Set<Depot> depots;
	private int copies = -1;
	private TransferThread[] transfer_threads;
	private Scoreboard transfer_monitor;
	private Thread transfer_thread_monitor;

	private FileReadThread input_file_reader;
	private FileWriteThread output_file_writer;

	public Exnode()
	{
		mappings_sorted = new TreeMap<MappedOffsets, List<Mapping>>();
		transfer_monitor = new Scoreboard(); // NOTE: initially length = 0

		add(new MetadataString("Version", Configuration.exnode_version));
		depots = new HashSet<Depot>();
		state = state_exnode.nascent;
	}

	public Exnode(String filename) throws FileNotFoundException
	{
		this();
		add(new MetadataString("filename", filename));
		input_file(filename);
	}

	/**
	 * @return whether or not the exnode is accessible enough to begin
	 *         write(upload)/read(download)
	 */
	public boolean accessible(service_exnode service_requested)
	{
		if (state != state_exnode.nascent) {
			log.info(status());
			return true;
		}

		if (service_requested == service_exnode.read) {
			boolean mappings_accessible = true;
			for (Map.Entry<MappedOffsets, List<Mapping>> mappings_identical : mappings_sorted.entrySet()) {
				boolean mapping_accessible = false;
				for (Mapping mapping_identical : mappings_identical.getValue()) {
					mapping_accessible = mapping_identical.accessible();

					/*
					 * if even one allocation (i.e. replica) of the mapping is accessible,
					 * the mapping can be read successfully
					 */
					if (mapping_accessible) {
						break;
					}
				}
				/*
				 * if even one of the mappings (on all of its allocations) isn't
				 * accessible, then the exnode cannot be read successfully
				 */
				if (!mapping_accessible) {
					mappings_accessible = false;
					break;
				}
			}

			state = mappings_accessible ? state_exnode.ready : state;
		} else if (service_requested == service_exnode.write) {
			int depots_servicable = 0;
			for (Depot depot : depots) {
				depots_servicable += depot.connected() ? 1 : 0;
			}

			state = depots_servicable >= copies ? state_exnode.ready : state;
		}

		if (state == state_exnode.ready) {
			log.info(status());
		} else {
			log.warning(status());
		}
		return state == state_exnode.ready;
	}

	public state_exnode state()
	{
		return state;
	}

	/**
	 * @param mapping
	 *          : mapping to be added to the exnode
	 */
	public void add(Mapping mapping)
	{
		if (mapping == null)
			return;

		MappedOffsets mapped_offsets_new = new MappedOffsets(mapping.exnode_offset(),
				mapping.logical_length());

		List<Mapping> mappings_existing = mappings_sorted.containsKey(mapped_offsets_new) ? mappings_sorted
				.get(mapped_offsets_new) : new ArrayList<Mapping>();

		if (!mappings_existing.contains(mapping)) {
			mappings_existing.add(mapping);
		}

		if (state != state_exnode.transit && mappings_existing.size() == 1) {
			length += mapped_offsets_new.length();
		}

		mappings_sorted.put(mapped_offsets_new, mappings_existing);
		depots.add(mapping.allocation.depot);
		// transfer_monitor.add(mapping.allocation.depot);
	}

	public Mapping add(Depot depot, long exnode_offset, long logical_length, long allocation_offset,
			long allocation_length, long e2e_blocksize, long allocation_duration)
	{
		Mapping mapping_new = new Mapping();
		try {
			mapping_new.allocation = depot.allocateHardByteArray((int) allocation_duration,
					allocation_length);

			if (mapping_new.allocation == null) {
				log.warning(this + " failed to obtain allocation on " + depot);
				return null;
			}
		} catch (IBPException e) {
			log.warning(this + " failed to obtain allocation on " + depot + " " + e);
			return null;
		}

		mapping_new.add(new MetadataInteger("exnode_offset", exnode_offset));
		mapping_new.add(new MetadataInteger("logical_length", logical_length));
		mapping_new.add(new MetadataInteger("alloc_offset", allocation_offset)); // 0
		mapping_new.add(new MetadataInteger("alloc_length", allocation_length));
		mapping_new.add(new MetadataInteger("e2e_blocksize", e2e_blocksize)); // 0

		add(mapping_new);

		return mapping_new;
	}

	public int copies()
	{
		if (copies > 0) {
			return copies;
		} else if (!mappings_sorted.isEmpty()) {
			for (Map.Entry<MappedOffsets, List<Mapping>> mappings_similar : mappings_sorted.entrySet()) {
				return mappings_similar.getValue().size();
			}
		}
		return 0;
	}

	public Set<Depot> depots()
	{
		return depots;
	}

	public void add(ProgressListener progress_listener)
	{
		if (transfer_monitor != null) {
			transfer_monitor.progress_listener(progress_listener);
		}
	}

	public Mapping mapping_best(List<Mapping> target_mappings)
	{
		return transfer_monitor.mapping_best(target_mappings);
	}

	/**
	 * @return all mappings contained in the exnode
	 */
	public List<Mapping> mappings()
	{
		List<Mapping> mappings_all = new ArrayList<Mapping>();

		Iterator<List<Mapping>> iterator_mappings = mappings_sorted.values().iterator();
		while (iterator_mappings.hasNext()) {
			List<Mapping> mappings_next = (List<Mapping>) iterator_mappings.next();
			mappings_all.addAll(mappings_next);
		}

		return mappings_all;
	}

	/**
	 * @param offset
	 * @param length
	 * @return list of mappings applicable to requested <offset, length>
	 */
	public List<Mapping> mappings(long offset, long length)
	{
		List<Mapping> mappings_applicable = new ArrayList<Mapping>();

		for (SortedMap.Entry<MappedOffsets, List<Mapping>> mappings_reachable : mappings_sorted
				.entrySet()) {
			MappedOffsets mapped_offsets_next = mappings_reachable.getKey();

			if (mapped_offsets_next.offset_start > offset) {
				break;
			} else if (mapped_offsets_next.offset_end >= offset + length - 1) {
				mappings_applicable.addAll(mappings_reachable.getValue());
			}
		}

		return mappings_applicable;
	}

	public long length()
	{
		if (length == 0 && input_filename() != null) {
			length = input_file_reader.length();
		}
		return length;
	}

	public String filename()
	{
		return get("filename").getString();
	}

	private void input_file(String filename_input) throws FileNotFoundException
	{
		input_file_reader = new FileReadThread(filename_input);
	}

	public String input_filename()
	{
		return input_file_reader != null ? input_file_reader.toString() : null;
	}

	public void input_file_read(WriteJob job_to_read)
	{
		try {
			input_file_reader.read(job_to_read);
		} catch (IOException e) {
			log.severe(" unable to read " + input_file_reader + ". " + e);
			transfer_cancel();
		}
	}

	/**
	 * @param job_to_read
	 * @return whether the input_file_reader's job queue "no longer contains" the
	 *         entered <offset, length> CAUTION: this is not a very accurate way
	 *         to check this condition so the caller must be careful to run this
	 *         test only after it has already submitted the <offset, length> job
	 *         previously
	 */
	public boolean input_file_buffer_contains(Job job_to_read)
	{
		return input_file_reader != null && !input_file_reader.contains(job_to_read);
	}

	public void output_file(String filename_output) throws FileNotFoundException
	{
		output_file_writer = new FileWriteThread(filename_output);
	}

	public String output_filename()
	{
		return output_file_writer != null ? output_file_writer.toString() : null;
	}

	public void output_file_write(ReadJob job_to_write)
	{
		try {
			output_file_writer.write(job_to_write);
		} catch (IOException e) {
			log.severe(" unable to write to " + output_file_writer + ". " + e);
			transfer_cancel();
		}
	}

	/**
	 * @param job_to_write
	 * @return whether the output_file_writer's job queue "no longer contains" the
	 *         entered <offset, length> CAUTION: this is not a very accurate way
	 *         to check this condition so the caller must be careful to run this
	 *         test only after it has already submitted the <offset, length> job
	 *         previously
	 */
	public boolean output_file_contains(Job job_to_write)
	{
		return output_file_writer != null && !output_file_writer.contains(job_to_write);
	}

	public synchronized Socket try_start(Mapping mapping_to_read)
	{
		return transfer_monitor.try_start(mapping_to_read.allocation.depot);
	}

	public synchronized void try_end(Mapping mapping_tried, Socket socket_tried,
			long offset_transferred, long bytes_transferred, String transfer_status)
	{
		transfer_monitor.try_end(mapping_tried.allocation.depot, socket_tried, offset_transferred,
				bytes_transferred, transfer_status);
	}

	/**
	 * @param mappings
	 * @param transfer_size_max
	 * @return a sorted set of transfer boundaries
	 * @algorithm 1> populate initial "sorted set" of transfer_boundaries from
	 *            start offsets of provided mappings; 2> split existing
	 *            transfer_boundaries to allow lengths "less than or equal to"
	 *            transfer_size_max
	 */
	public SortedSet<Long> transfer_boundaries(List<Mapping> mappings, long transfer_size_max)
	{
		SortedSet<Long> transfer_boundaries = new TreeSet<Long>();

		for (Iterator<Mapping> i = mappings.iterator(); i.hasNext();) {
			Mapping mapping = (Mapping) i.next();

			/* add 2 transfer boundaries (offset_start and offset_end+1) per mapping */
			if (transfer_boundaries.size() > 0
					&& !transfer_boundaries.contains(new Long(mapping.exnode_offset()))) {
				log.severe(this + " detected gap between offsets " + (transfer_boundaries.last() - 1)
						+ " and " + mapping.exnode_offset());
			}
			transfer_boundaries.add(new Long(mapping.exnode_offset()));
			transfer_boundaries.add(new Long(mapping.exnode_offset() + mapping.logical_length()));

			log.info(this + " added transfer boundary " + mapping.exnode_offset());
		}

		if (transfer_size_max > 0) {
			/* limit transfer boundaries to 2GB */
			transfer_size_max = transfer_size_max > Integer.MAX_VALUE ? Integer.MAX_VALUE
					: transfer_size_max;

			/* Insert new boundaries to guarantee transfer_size_max */
			/*
			 * NOTE: at this point, the last point is the end of the file. We remove
			 * it AFTER inserting the necessary other points.
			 */
			if (!transfer_boundaries.isEmpty()) {
				Object[] boundaries = transfer_boundaries.toArray();

				long offset_previous = ((Long) boundaries[0]).longValue();
				for (int i = 1; i < boundaries.length; i++) {
					long offset_current = ((Long) boundaries[i]).longValue();

					while (offset_current - offset_previous > transfer_size_max) {
						offset_previous += transfer_size_max;

						transfer_boundaries.add(offset_previous);
						log.info(this + " added transfer boundary " + offset_previous);
					}
					offset_previous = offset_current;
				}
			}
		}

		/*
		 * Because we include end points, the last point will be the end of the file
		 * (from which no transfer can be requested). Remove it.
		 */
		if (!transfer_boundaries.isEmpty()) {
			transfer_boundaries.remove(transfer_boundaries.last());
		}

		return transfer_boundaries;
	}

	/**
	 * @param transfer_size_max
	 * @algorithm get transfer boundaries for the exnode's mappings and create
	 *            1-to-1 ReadJobs for each of them; shuffle the newly created
	 *            transfer_jobs for a better spread of read requests to different
	 *            depots
	 */
	public synchronized void setup_read(long transfer_size_max)
	{
		/* if exnode is already transferring data, discourage setup */
		if (state == state_exnode.transit) {
			return;
			/* else, if same transfer-size is requested, the exnode is already setup */
		} else if (transfer_jobs != null && transfer_jobs.size() >= 0) {
			if (transfer_jobs.peek() instanceof ReadJob) {
				ReadJob firstJob = (ReadJob) transfer_jobs.peek();
				if (firstJob.bytes_to_read() == transfer_size_max) {
					return;
				}
			}
		}

		/* if the exnode is not ready, discourage setup */
		if (!accessible(service_exnode.write)) {
			log.warning(status() + "; cannot setup read jobs.");
			return;
		}

		List<Mapping> read_mappings = mappings();
		Object[] read_boundaries = transfer_boundaries(read_mappings, transfer_size_max).toArray();
		JobQueue read_jobs_serial = new JobQueue();
		List<Depot> target_depots = new ArrayList<Depot>();

		int target_mappings_start_idx = 0;
		List<Mapping> target_mappings = null;
		for (int b = 0; b < read_boundaries.length; b++) {
			/* new transfer boundary start offset */
			long read_offset = ((Long) read_boundaries[b]).longValue();

			/* next transfer boundary end offset */
			long read_offset_next;
			if (b + 1 < read_boundaries.length) {
				read_offset_next = ((Long) read_boundaries[b + 1]).longValue();
			} else {
				read_offset_next = length; // offset of byte past end of file
			}

			/* number of bytes to read */
			if (read_offset_next - read_offset > Integer.MAX_VALUE) {
				throw new RuntimeException(this + ".setup_read(): transfer boundaries must be <= 2GB.");
			}

			/*
			 * find the "next" (in-sorted-list) mapping that holds data corresponding
			 * to this read-job; i.e. if the last read-mapping's last offset is less
			 * than the begin-offset of this rad-job, move to the next mapping
			 */
			boolean target_mappings_same = true;
			while (read_mappings.get(target_mappings_start_idx).exnode_offset()
					+ read_mappings.get(target_mappings_start_idx).logical_length() - 1 < read_offset) {
				target_mappings_start_idx++;
				target_mappings_same = false;
			}

			if (target_mappings == null || !target_mappings_same) {
				target_mappings = new ArrayList<Mapping>();
				for (int i = target_mappings_start_idx; i < read_mappings.size(); i++) {
					Mapping read_mapping = read_mappings.get(i);
					if (read_mapping.exnode_offset() <= read_offset
							&& read_mapping.logical_length() >= (read_offset_next - read_offset)) {
						target_mappings.add(read_mapping);
					}
				}
			}

			ConcurrentJob job = new ReadJob(b, this, target_mappings, read_offset, read_offset_next
					- read_offset);
			log.info(this + " : new " + job.toString());
			read_jobs_serial.add(job);

			if (!target_depots.contains(read_mappings.get(target_mappings_start_idx).allocation.depot)) {
				// setup_depot(read_mappings.get(target_mapping_idx).allocation.depot);
				target_depots.add(read_mappings.get(target_mappings_start_idx).allocation.depot);
			}
		}

		transfer_jobs = new JobQueue();
		log.info(this + ": created " + read_jobs_serial.size() + " new read job(s).");

		if (Configuration.bd_depot_transfer_shuffle) {
			/*
			 * Shuffle mainQueue for better spread of requests (based on their target
			 * depots) NOTE: remove the following for loop if shuffling is not
			 * intended
			 */
			int passes = read_jobs_serial.size() / 10;
			for (int i = 0; i < (passes * read_jobs_serial.size()) && read_jobs_serial.size() > 0; i++) {
				ReadJob read_job = (ReadJob) read_jobs_serial.remove();

				if (read_job.mappings().get(0).allocation.depot.toString().equals(
						target_depots.get(i % target_depots.size()).toString())) {
					transfer_jobs.add(read_job);
				} else {
					read_jobs_serial.add(read_job);
				}
			}
		}

		while (0 < read_jobs_serial.size()) {
			ReadJob read_job = (ReadJob) read_jobs_serial.remove();
			transfer_jobs.add(read_job);
		}
	}

	/**
	 * @param filename_output
	 *          : output filename
	 * @param transfer_bytes_max
	 *          : maximum chunk size of read requests
	 * @param count_transfer_threads
	 *          : the number of transfer threads to use for performing the read
	 *          operation
	 * @algorithm open the file and initiate the file-writer thread; create new
	 *            (if needed) read-jobs for chosen transfer_bytes_max; save the
	 *            current thread as the monitor for current read operation; spawn
	 *            and start read-threads; start monitoring the read-operation
	 *            (exit on interruption or completion)
	 */
	public boolean read(String filename_output, int transfer_bytes_max, int count_transfer_threads)
	{
		if (!accessible(service_exnode.read)) {
			log.severe(status());
			return false;
		}

		/* open the output file for writing */
		try {
			output_file(filename_output);
			output_file_writer.start();
		} catch (FileNotFoundException e) {
			log.severe(this + " failed to open output file for writing." + e);
			state = state_exnode.failed;
			return false;
		}

		/*
		 * prepare the queue for performing the read operation; the queue may have
		 * been "prepopulated" through a previous call to read_jobs_new; however, if
		 * a different transfer_bytes_max is requested for the current read, then
		 * "force-repopulate" the ReadJobs
		 */
		setup_read(transfer_bytes_max);

		/* prepare to transfer the exnode data of precomputed # of bytes */
		transfer_monitor.progress_start(length);
		state = state_exnode.transit;

		/* save current thread as the monitor of current read operation */
		transfer_thread_monitor = Thread.currentThread();

		/* spawn (and start) the required number of ReadThreads */
		log.info(this + ": spawning " + count_transfer_threads + " read-thread(s).");
		transfer_threads = new ReadThread[count_transfer_threads];
		while (0 < count_transfer_threads) {
			transfer_threads[transfer_threads.length - count_transfer_threads] = new ReadThread(
					transfer_threads.length - count_transfer_threads, transfer_jobs);
			transfer_threads[transfer_threads.length - count_transfer_threads].start();

			log.info(this + ": started read-thread #"
					+ (transfer_threads.length - count_transfer_threads + 1));
			count_transfer_threads--;
		}

		double last_progress = 0.0;
		long time_no_progress = 0;
		while (!transfer_thread_monitor.isInterrupted()
				&& transfer_monitor.percent_completed() <= 100.0f && transfer_jobs.size() > 0) {
			log.info(status());

			try {
				Thread.sleep(Configuration.bd_exnode_transfer_log_interval);

				if (last_progress == transfer_monitor.percent_completed()) {
					time_no_progress += Configuration.bd_exnode_transfer_log_interval;
				} else {
					last_progress = transfer_monitor.percent_completed();
					time_no_progress = 0;
				}

				// if (time_no_progress >= Configuration.bd_transfer_exhaust_timeout) {
				// log.severe(this +
				// " exhausted wait-time for further progress. Cancelling.");
				// transfer_cancel();
				// }
			} catch (InterruptedException e) {
				log.severe(this + ": interrupted.");
				break;
			}
		}

		if (transfer_monitor.percent_completed() >= 100.0f) {
			state = state_exnode.done;
			log.info(status());
		} else {
			transfer_monitor.progress_error();
			log.severe(status());
		}

		transfer_teardown();
		log.warning(status());
		return state == state_exnode.done;
	}

	public synchronized void setup_write(Set<Depot> depots, int copies, long time_allocation,
			long transfer_size_max, String function_str)
	{
		/* open the input file for reading, if not already open */
		// if (input_filename() == null) {// ||
		// !input_filename().equals(filename_input)) {
		// try {
		// input_file(filename_input);
		// } catch (FileNotFoundException e) {
		// log.severe(this + " failed to open input file for reading.");
		// state = state_exnode.failed;
		// return;
		// }
		// }

		if (input_filename() == null || length() == 0) {
			log.severe(status() + ". Input file not set or empty. Unable to setup write.");
			return;
		}

		/* if exnode is already transferring data, discourage setup */
		if (state == state_exnode.transit) {
			return;
		}

		/* else, update the target depots, allocation-duration ... */
		this.depots.clear();
		if (depots != null && !depots.isEmpty()) {
			this.depots.addAll(depots);
		}
		this.copies = copies;

		/* if the exnode is not ready, discourage setup */
		if (!accessible(service_exnode.write)) {
			log.warning(status() + "; no depots for write jobs.");
//			if (transfer_jobs != null) {
//				transfer_jobs.clear();
//			}
//			return;
		}

		/*
		 * and, if same transfer-size and allocation-time is requested, the exnode
		 * is already setup
		 */
		if (transfer_jobs != null && transfer_jobs.size() >= 0) {
			if (transfer_jobs.peek() instanceof WriteJob) {
				WriteJob firstJob = (WriteJob) transfer_jobs.peek();
				if (firstJob.bytes_to_write() == transfer_size_max) {
					if (firstJob.time_allocation == time_allocation) {
						return;
					}

					/* else, just update the allocation-times */
					synchronized (transfer_jobs) {
						int count_jobs = transfer_jobs.size();
						while (count_jobs > 0) {
							WriteJob write_job = (WriteJob) transfer_jobs.remove();
							write_job.time_allocation = time_allocation;
							transfer_jobs.add(write_job);
						}
					}
				}
			}
		}

		if (transfer_size_max == 0 || transfer_size_max > Integer.MAX_VALUE) {
			throw new RuntimeException(this
					+ ".setup_write(): transfer boundaries must be > 0 and <= 2GB.");
		}
		long blocks_to_create = (length() / transfer_size_max)
				+ ((length() % transfer_size_max == 0) ? 0 : 1);

		transfer_jobs = new JobQueue();
		for (int b = 0; b < blocks_to_create; b++) {
			long write_offset = b * (long) transfer_size_max;
			int write_length = (int) ((write_offset + transfer_size_max <= length()) ? transfer_size_max
					: length() - write_offset);
			WriteJob write_job = new WriteJob(b, this, write_offset, write_length, time_allocation);
			transfer_jobs.add(write_job);
		}
	}

	/**
	 * @param filename_input
	 *          : input filename
	 * @param transfer_bytes_max
	 *          : maximum chunk size of write requests
	 * @param count_transfer_threads
	 *          : the number of transfer threads to use for performing the write
	 *          operation
	 * @algorithm open the file and initiate the file-reader thread; create new
	 *            (if needed) read-jobs for chosen transfer_bytes_max; save the
	 *            current thread as the monitor for current read operation; spawn
	 *            and start read-threads; start monitoring the read-operation
	 *            (exit on interruption or completion)
	 */
	public boolean write(Set<Depot> depots, int copies, long time_allocation, long transfer_size_max,
			String function_str, int count_transfer_threads)
	{
		if (!accessible(service_exnode.write)) {
			log.severe(status());
			return false;
		}

		/* prepare the queue for performing the write operation; */
		setup_write(depots, copies, time_allocation, transfer_size_max, function_str);

		if (transfer_jobs == null || transfer_jobs.size() == 0) {
			state = state_exnode.failed;
			log.severe(status());
			return false;
		}

		/* start the input file reader */
		input_file_reader.start();

		/* prepare to transfer the exnode data of precomputed # of bytes */
		transfer_monitor.progress_start(length());
		state = state_exnode.transit;

		/* save current thread as the monitor of current read operation */
		transfer_thread_monitor = Thread.currentThread();

		/* spawn (and start) the required number of WriteThreads */
		log.info(this + ": spawning " + count_transfer_threads + " write-thread(s).");
		transfer_threads = new WriteThread[count_transfer_threads];
		JobQueue transit_write_jobs = new JobQueue();
		while (0 < count_transfer_threads) {
			transfer_threads[transfer_threads.length - count_transfer_threads] = new WriteThread(
					transfer_threads.length - count_transfer_threads, transfer_jobs, transit_write_jobs);
			transfer_threads[transfer_threads.length - count_transfer_threads].start();

			log.info(this + ": started write-thread #"
					+ (transfer_threads.length - count_transfer_threads + 1));
			count_transfer_threads--;
		}

		double last_progress = 0.0;
		long time_no_progress = 0;
		while (!transfer_thread_monitor.isInterrupted()
				&& transfer_monitor.percent_completed() <= 100.0f && transfer_jobs.size() > 0) {
			log.info(status());

			try {
				Thread.sleep(Configuration.bd_exnode_transfer_log_interval);

				if (last_progress == transfer_monitor.percent_completed()) {
					time_no_progress += Configuration.bd_exnode_transfer_log_interval;
				} else {
					last_progress = transfer_monitor.percent_completed();
					time_no_progress = 0;
				}

				// if (time_no_progress >= Configuration.bd_transfer_exhaust_timeout) {
				// log.severe(this +
				// " exhausted wait-time for further progress. Cancelling.");
				// transfer_cancel();
				// }
			} catch (InterruptedException e) {
				log.severe(this + ": interrupted.");
				break;
			}
		}

		if (transfer_monitor.percent_completed() >= 100.0f) {
			state = state_exnode.done;
			log.info(status());
		} else {
			transfer_monitor.progress_error();
			log.severe(status());
		}

		transfer_teardown();
		log.warning(status());
		return state == state_exnode.done;
	}

	public synchronized void transfer_threads_interrupt()
	{
		for (TransferThread transfer_thread : transfer_threads) {
			transfer_thread.interrupt();
		}
		log.severe(this + " interrupted " + transfer_threads.length + " transfer threads.");

		for (TransferThread transfer_thread : transfer_threads) {
			int count = 100;
			while (count > 0 && transfer_thread.isAlive()) {
				// log.warning(this + ": waiting for " + transfer_thread);
				count--;
			}
		}
	}

	public synchronized void transfer_jobs_clear()
	{
		synchronized (transfer_jobs) {
			int count_transfer_jobs = transfer_jobs.size();
			transfer_jobs = null;
			log.severe(this + " cleared " + count_transfer_jobs + " transfer jobs.");
		}
	}

	public synchronized void input_file_reader_finish()
	{
		if (input_file_reader == null) {
			return;
		}
		while (input_file_reader.state != reader_state.idle) {
			log.warning(this + ": waiting for Reader [" + input_file_reader + "]");
		}
	}

	public synchronized void input_file_reader_stop()
	{
		if (input_file_reader == null) {
			return;
		}

		input_file_reader.interrupt();
		log.severe(this + " interrupted output file reader.");

		int count = Integer.MAX_VALUE;
		while (count > 0 && input_file_reader.isAlive()) {
			log.warning(this + ": waiting for Reader [" + input_file_reader + "]");
			count--;
		}
	}

	public synchronized void output_file_writer_finish()
	{
		if (output_file_writer == null) {
			return;
		}
		while (output_file_writer.state != writer_state.idle) {
			log.warning(this + ": waiting for Writer [" + output_file_writer + "]");
		}
	}

	public synchronized void output_file_writer_stop()
	{
		if (output_file_writer == null) {
			return;
		}

		output_file_writer.interrupt();
		log.severe(this + " interrupted output file writer.");

		int count = Integer.MAX_VALUE;
		while (count > 0 && output_file_writer.isAlive()) {
			log.warning(this + ": waiting for Writer [" + output_file_writer + "]");
			count--;
		}
	}

	public synchronized void transfer_cancel()
	{
		log.severe(this + " cancelling.");
		state = state_exnode.failed;

		transfer_threads_interrupt();
		transfer_jobs_clear();
		// transfer_connections_close();
		input_file_reader_stop();
		output_file_writer_stop();

		transfer_thread_monitor.interrupt();
		log.severe(status());
	}

	public synchronized void transfer_teardown()
	{
		/* NOTE: no state change */
		log.severe(this + " closing.");

		transfer_threads_interrupt();
		transfer_jobs_clear();
		// transfer_connections_close();

		if (state == state_exnode.done) {
			/*
			 * let the diskio operation complete before interrupting the
			 * file-reader/writer
			 */
			input_file_reader_finish();
			output_file_writer_finish();
		}
		input_file_reader_stop();
		output_file_writer_stop();

		transfer_thread_monitor.interrupt();
		log.warning(status());
	}

	/******************* Serializers-Deserializers *********************/
	public static Exnode xml(Element xml) throws DeserializeException
	{
		if (!xml.getNamespaceURI().equals(Configuration.exnode_namespace)) {
			log.severe("failed to deserialize exnode from xml.");
			throw new DeserializeException("valid exnode namespace not found");
		}

		Exnode exnode = new Exnode();
		NodeList nodes = xml.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element xml_element = (Element) nodes.item(i);

				if (xml_element.getLocalName().equals("metadata")) {
					Metadata metadata = Metadata.xml(xml_element);
					exnode.add(metadata);
				} else if (xml_element.getLocalName().equals("mapping")) {
					Mapping mapping = Mapping.xml(xml_element);
					exnode.add(mapping);
				}
			}
		}
		return exnode;
	}

	public String xml() throws SerializeException
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<?xml version=\"1.0\"?>\n");
		xml.append("<exnode xmlns:exnode=\"" + Configuration.exnode_namespace + "\">\n");

		Iterator<?> i = iterator();
		Metadata metadata;
		while (i.hasNext()) {
			metadata = (Metadata) i.next();
			xml.append(metadata.xml());
		}

		i = mappings().iterator();
		Mapping mapping;
		while (i.hasNext()) {
			mapping = (Mapping) i.next();
			xml.append(mapping.xml());
		}

		xml.append("</exnode>");
		return (xml.toString());
	}

	public static Exnode json(JsonObject json)
	{
		Exnode exnode = new Exnode();
		// process file metadata
		Metadata metadata = Metadata.json(json, "name");
		exnode.add(metadata);

		// process extents
		JsonArray extents = json.getJsonArray("extents");
		if (extents != null) {
			for (JsonObject extent : extents.getValuesAs(JsonObject.class)) {
				Mapping mapping = Mapping.json(extent);
				exnode.add(mapping);
			}
		}
		return exnode;
	}

	private JsonArray mappings_json()
	{
		List<Mapping> mappings = mappings();
		JsonArrayBuilder mappings_json = Json.createArrayBuilder();
		for (Mapping mapping : mappings) {
			mappings_json.add(mapping.json());
		}
		return mappings_json.build();
	}

	public JsonObject json()
	{
		JsonObjectBuilder json_builder = Json.createObjectBuilder();
		json_builder.add("name", filename());
		json_builder.add("parent", JsonValue.NULL);
		json_builder.add("created", new Date().getTime());
		json_builder.add("modified", new Date().getTime());
		json_builder.add("mode", "file");
		json_builder.add("size", length());
		json_builder.add("extents", mappings_json());
		return json_builder.build();
	}

	public String status()
	{
		switch (state) {
			case nascent:
				return this + " [NASCENT] [0 - " + (length() - 1) + "](" + length() + "B); "
						+ mappings().size() + " mappings.";

			case ready:
				return this + " [READY] [0 - " + (length() - 1) + "](" + length() + "B); "
						+ (transfer_jobs == null ? 0 : transfer_jobs.size()) + " jobs";

			case transit:
				return this + " [TRANSIT] : transferring [0 - " + (length() - 1) + "](" + length() + "B) "
						+ (transfer_jobs == null ? 0 : transfer_jobs.size()) + " jobs; "
						+ (transfer_threads == null ? 0 : transfer_threads.length) + " threads "
						+ transfer_monitor;

			case done:
				return this + " [DONE] [0 - " + (length() - 1) + "](" + length() + "B) " + transfer_monitor;

			case failed:
				return this + " [FAILED] [0 - " + (length() - 1) + "](" + length() + "B)";

			default:
				return this + " [UNKNOWN STATE]";
		}
	}

	public String toString()
	{
		return "Exnode [" + filename() + "]";
	}
}
