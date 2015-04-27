/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.transfer;

import java.util.logging.Logger;

import edu.crest.dlt.transfer.WriteJob.state_writejob;
import edu.crest.dlt.utils.Configuration;

/**
 * @description WriteThread is a special TransferThread that understands a
 *              WriteJob's states and acts as a manager of WriteJobs
 */
public class WriteThread extends TransferThread
{
	private static Logger log = Logger.getLogger(WriteThread.class.getName());

	private JobQueue queued_write_jobs;
	private JobQueue transferring_write_jobs;

	public WriteThread(int id, JobQueue queued_read_jobs, JobQueue transit_write_jobs)
	{
		super(id);
		this.queued_write_jobs = queued_read_jobs;
		this.transferring_write_jobs = transit_write_jobs;
	}

	public boolean are_jobs_pending()
	{
		/* NOTE: check the size of smaller of the queues first */
		return transferring_write_jobs.size() > 0 || queued_write_jobs.size() > 0;
	}

	private Job staged_job_next()
	{
		WriteJob write_job = null;
		synchronized (transferring_write_jobs) {
			for (int t = transferring_write_jobs.size(); t > 0; t--) {
				write_job = (WriteJob) transferring_write_jobs.remove();

				if (write_job != null && !write_job.ready()) {
					log.warning(this + " [SCANNING] " + write_job.status() + " Requeueing.");
					transferring_write_jobs.add(write_job);
					write_job = null;
				} else {
					break;
				}
			}
		}
		return write_job;
	}

	private Job unstaged_job_next()
	{
		WriteJob write_job = null;
		synchronized (queued_write_jobs) {
			for (int t = queued_write_jobs.size(); t > 0; t--) {
				write_job = (WriteJob) queued_write_jobs.remove();

				if (write_job != null) {
					break;
				}
			}
		}
		return write_job;
	}

	public void run() throws ClassCastException
	{
		log.info(this + " [NASCENT]");

		/* INTENTIONAL: write_job outside the outermost while loop */
		WriteJob write_job = null;
		while (!isInterrupted() && queued_write_jobs != null && transferring_write_jobs != null
				&& are_jobs_pending()) {

			/*
			 * if no job is ready to be staged, check jobs that have been staged for
			 * transfer; if their data has been buffered, they can begin transferring
			 */
			write_job = write_job == null ? (WriteJob) staged_job_next() : write_job;

			/* if no staged jobs are ready for transfer stage the next job */
			write_job = write_job == null ? (WriteJob) unstaged_job_next() : write_job;

			if (write_job == null) {
				log.warning(this + " [IDLE] no job found for writing.");
				continue;
			}

			log.info(this + " [EXECUTING] " + write_job.status());
			WriteJob.state_writejob result_execution = write_job.state;

			try {
				write_job.execute();
			} catch (Throwable e) {
				log.severe(this + " [FAILED-EXCEPTION] " + write_job + e);
				e.printStackTrace();
				result_execution = state_writejob.failed;
			}
			result_execution = write_job.state;

			if (isInterrupted()) {
				break;
			}

			switch (result_execution) {
			/* if this staged job is done, stage the next unstaged job */
				case done:
					log.info(this + " [COMPLETED] " + write_job.status());
					write_job = (WriteJob) unstaged_job_next();
					break;

				/* if no more retrials possible, cancel the write operation */
				case failed:
					if (write_job.count_tried() >= Configuration.dlt_exnode_read_retries_max) {
						log.severe(this + " [FAILED-TERMINATING] " + write_job.status());

						/* notify the exnode to cancel the write operation */
						write_job.exnode_to_write.transfer_cancel();
						break;
					} /* else, requeue the write_job in transferring-queue */
					/* requeue the write_job */
				default:
					synchronized (transferring_write_jobs) {
						log.info(this + " [EXECUTED] " + write_job.status() + "; pushed behind "
								+ transferring_write_jobs.size() + " jobs.");
						transferring_write_jobs.add(write_job);
						transferring_write_jobs.notify();
					}
					/* INTENTIONAL: write_job = null */
					write_job = null;
					break;
			}
		}
		log.warning(this + " [EXITED]");
	}

	public String toString()
	{
		return "Write" + super.toString();
	}
}
