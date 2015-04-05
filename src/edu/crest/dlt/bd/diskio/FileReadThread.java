package edu.crest.dlt.bd.diskio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import edu.crest.dlt.transfer.Job;
import edu.crest.dlt.transfer.JobQueue;
import edu.crest.dlt.transfer.WriteJob;
import edu.crest.dlt.transfer.WriteJob.state_writejob;
import edu.crest.dlt.utils.Configuration;

public class FileReadThread extends Thread
{
	static Logger log = Logger.getLogger(FileReadThread.class.getName());

	public enum reader_state {
		idle, reading
	}

	public reader_state state;

	private File file;
	private RandomAccessFile file_access;
	private JobQueue read_jobs;

	private int count_fail_max = Configuration.bd_file_write_retries_max;
	private int buffered_read_size = Configuration.bd_file_read_buffer_size;

	public FileReadThread(String file_name) throws FileNotFoundException
	{
		this.file = new File(file_name);
		this.file_access = new RandomAccessFile(file, "r");
		this.read_jobs = new JobQueue();
		state = reader_state.idle;
	}
	
	public void finalize()
	{
		close();
	}

	public void read(WriteJob job_to_read) throws IOException
	{
		if (count_fail_max < 0) {
			throw new IOException(this + ": file read retries exhausted.");
		}

		synchronized (read_jobs) {
			read_jobs.add(job_to_read);
		}
	}

	public void run()
	{
		log.info("Reader [" + this + "]: started.");
		while (!isInterrupted() && count_fail_max >= 0) {
			WriteJob read_job = null;
			synchronized (read_jobs) {
				try {
					read_job = (WriteJob) read_jobs.peek();
				} catch (Exception e) {
				}
			}

			if (read_job != null) {
				state = reader_state.reading;
				/*
				 * TODO: coalesce read buffers for consecutive portions of the file
				 * (remember it's already a SortedMap sorted by offset_start)
				 */
				long read_offset_start = read_job.offset_to_write();
				long read_offset_end = read_job.offset_to_write() + read_job.bytes_to_write() - 1;
				int read_length = (int) read_job.bytes_to_write();
				byte[] read_bytes = new byte[read_length];// = read_job.bytes_to_write;

				try {
					file_access.seek(read_offset_start);
					if (read_length != file_access.read(read_bytes, 0, read_length)) {
						throw new IOException(this + "failed to read [" + read_offset_start + "-"
								+ read_offset_end + "](" + read_length + "B)");
					}

					log.info(this + ": read [" + read_offset_start + "-" + read_offset_end + "]("
							+ read_length + "B)");

					/* CAUTION: Remove the job from the queue only after the read succeeds */
					synchronized (read_jobs) {
						read_jobs.remove(read_job);
						log.warning(read_job + " read. " + read_jobs.size() + " jobs remaining.");
					}
				} catch (IOException e) {
					log.severe(this + ": failed to read " + read_length + "B at [" + read_offset_start + "-"
							+ read_offset_end + "]. Can tolerate " + count_fail_max + " more failure(s).");
					count_fail_max--;
				}
			} else {
				state = reader_state.idle;
			}
		}
		close();
		log.warning("Reader [" + this + "]: stopped.");
	}

	public boolean contains(Job job_to_write)
	{
		synchronized (read_jobs) {
			return read_jobs.contains(job_to_write);
		}
	}

	public long length()
	{
		return file != null ? file.length() : 0;
	}

	private void close()
	{
		try {
			/* clear pending read_jobs, if any */
			synchronized (read_jobs) {
				if (!read_jobs.isEmpty()) {
					log.severe(this + " discarding " + read_jobs.size() + " file-read requests.");
					read_jobs.clear();
				}
			}

			/* close read access to file */
			file_access.close();
		} catch (IOException e) {
			log.severe("failed to close " + this + ". " + e);
		}
	}

	public String toString()
	{
		return file.getName();
	}
}
