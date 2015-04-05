package edu.crest.dlt.transfer;

import java.util.logging.Logger;

import edu.crest.dlt.transfer.TransferThread;
import edu.crest.dlt.transfer.ReadJob.state_readjob;
import edu.crest.dlt.utils.Configuration;

/**
 * @author Rohit
 * @description ReadThread is a special TransferThread that understands a
 *              ReadJob's states and acts as a manager of ReadJobs
 */
public class ReadThread extends TransferThread
{
	private static Logger log = Logger.getLogger(ReadThread.class.getName());

	private JobQueue queued_read_jobs;

	public ReadThread(int id, JobQueue queued_read_jobs)
	{
		super(id);
		this.queued_read_jobs = queued_read_jobs;
	}

	public boolean are_jobs_pending()
	{
		return queued_read_jobs.size() > 0;
	}

	/**
	 * @param try_count
	 * @return bounded-increase pushback index
	 */
	private int pushback_index(int try_count)
	{
		int max_tries_allowed = Configuration.dlt_exnode_read_retries_max;
		int retry_interval = Configuration.dlt_exnode_read_retry_interval;

		synchronized (queued_read_jobs) {
//			return Math.min(queued_read_jobs.size(), (max_tries_allowed * try_count) * retry_interval);
			 return max_tries_allowed > try_count ?
			 Math.min(queued_read_jobs.size(),
			 (max_tries_allowed * try_count) * retry_interval) : retry_interval;
		}
	}
	
	public Job job_next() {
		ReadJob read_job = null;
		synchronized (queued_read_jobs) {
			while (queued_read_jobs.size() > 0) {
				read_job = (ReadJob) queued_read_jobs.remove();
				
				if (read_job != null && !read_job.ready()) {
					log.warning(this + " [SCANNING] " + read_job.status() + " Requeueing.");
					queued_read_jobs.add(read_job);
				} else {
					break;
				}
			}
//			log.info(this + ": " + queued_read_jobs.size() + " read-jobs pending.");
		}
		return read_job;
	}

	public void run() throws ClassCastException
	{
		log.info(this + " [NASCENT]");
		while (!isInterrupted() && queued_read_jobs != null && are_jobs_pending()) {
			ReadJob read_job = (ReadJob) job_next();

			if (read_job == null) {
				log.warning(this + " [IDLE] no job found for reading.");
				synchronized (queued_read_jobs) {
					try {
						queued_read_jobs.wait();
					} catch (InterruptedException e) {
						if (queued_read_jobs.size() > 0) {
							continue;
						}
					}
					break;
				}
			}

			log.info(this + " [EXECUTING] " + read_job.status());
			ReadJob.state_readjob result_execution = read_job.state;

			try {
				read_job.execute();
			} catch (Throwable e) {
				log.severe(this + " [FAILED-EXCEPTION] " + read_job + e);
				result_execution = state_readjob.failed;
			}
			result_execution = read_job.state;

			if (isInterrupted()) {
				break;
			}

			switch (result_execution) {
				/* add job to writer's queue, and consider done */
				case read_data_buffered:
					read_job.exnode_to_read.output_file_write(read_job);
				/* do nothing; (i.e. discard the job) */
				case done:
					log.info(this + " [COMPLETED] " + read_job.status());
					break;

				/* if no more retrials possible, cancel the read operation */
				case failed:
					if (read_job.count_tried() >= Configuration.dlt_exnode_read_retries_max) {
						log.severe(this + " [FAILED-TERMINATING] " + read_job.status());

						/* notify the exnode to cancel the read operation */
						read_job.exnode_to_read.transfer_cancel();
						break;
					} /* else, requeue the read_job */
					/* requeue the read_job */
				default:
					int pushback_idx = pushback_index(read_job.count_tried());

					log.info(this + " [EXECUTED] " + read_job.status() + "; pushed behind " + pushback_idx + " jobs.");
					synchronized (queued_read_jobs) {
						queued_read_jobs.add(read_job, pushback_idx);
						queued_read_jobs.notify();
					}
					break;
			}
		}
		log.warning(this + " [EXITED]");
	}
	
	public String toString()
	{
		return "Read" + super.toString();
	}
}
