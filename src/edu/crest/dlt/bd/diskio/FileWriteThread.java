package edu.crest.dlt.bd.diskio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import edu.crest.dlt.transfer.Job;
import edu.crest.dlt.transfer.JobQueue;
import edu.crest.dlt.transfer.ReadJob;
import edu.crest.dlt.utils.Configuration;

public class FileWriteThread extends Thread
{
	static Logger log = Logger.getLogger(FileWriteThread.class.getName());

	public enum writer_state {
		idle, writing
	}

	public writer_state state;

	private File file;
	private RandomAccessFile file_access;
	private JobQueue write_jobs;

	private int count_fail_max = Configuration.dlt_file_write_retries_max;

	public FileWriteThread(String file_name) throws FileNotFoundException
	{
		this.file = new File(file_name);
		this.file_access = new RandomAccessFile(file, "rw");
		this.write_jobs = new JobQueue();
		state = writer_state.idle;
	}

	public void write(ReadJob job_to_write) throws IOException
	{
		if (count_fail_max < 0) {
			throw new IOException(this + ": file write retries exhausted.");
		}

		synchronized (write_jobs) {
			write_jobs.add(job_to_write);
		}
	}

	public void run()
	{
		log.info("Writer [" + this + "]: started.");
		while (!isInterrupted() && count_fail_max >= 0) {
			ReadJob write_job = null;
			synchronized (write_jobs) {
				try {
					write_job = (ReadJob) write_jobs.peek();
				} catch (Exception e) {
				}
			}

			if (write_job != null) {
				state = writer_state.writing;
				/*
				 * TODO: coalesce write buffers for consecutive portions of the file
				 * (remember it's already a SortedMap sorted by offset_start)
				 */
				long write_offset_start = write_job.offset_to_read();
				long write_offset_end = write_job.offset_to_read() + write_job.bytes_to_read() - 1;
				int write_length = (int) write_job.bytes_to_read();
				byte[] write_bytes = write_job.bytes_to_write;

				try {
					file_access.seek(write_offset_start);
					file_access.write(write_bytes, 0, write_length);

					log.info(this + ": wrote [" + write_offset_start + "-" + write_offset_end + "]("
							+ write_length + "B)");

					/*
					 * CAUTION: Remove the job from the queue only after the write
					 * succeeds
					 */
					synchronized (write_jobs) {
						write_jobs.remove(write_job);
						log.warning(write_job + " written. " + write_jobs.size() + " jobs remaining.");
					}
				} catch (IOException e) {
					log.severe(this + ": failed to write " + write_length + "B at [" + write_offset_start
							+ "-" + write_offset_end + "]. Can tolerate " + count_fail_max + " more failure(s).");
					count_fail_max--;
				}
			} else {
				state = writer_state.idle;
			}
		}
		close();
		log.warning("Writer [" + this + "]: stopped.");
	}

	public boolean contains(Job job_to_write)
	{
		synchronized (write_jobs) {
			return write_jobs.contains(job_to_write);
		}
	}

	public long length()
	{
		return (file.length());
	}

	private void close()
	{
		try {
			/* clear pending write_jobs, if any */
			synchronized (write_jobs) {
				if (!write_jobs.isEmpty()) {
					log.severe(this + " discarding " + write_jobs.size() + " file-write requests.");
					write_jobs.clear();
				}
			}

			/* close write access to file */
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
