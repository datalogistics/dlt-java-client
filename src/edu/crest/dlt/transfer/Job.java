/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.transfer;

public abstract class Job
{

	protected int id;
	private TransferStatistics job_statistics;

	public Job(int id)
	{
		this.id = id;
		job_statistics = new TransferStatistics();
	}

	public abstract void execute() throws Throwable;

	public abstract boolean isInterrupted();

	public abstract boolean ready();

	/**
	 * @return try id # (starts with 1)
	 */
	public int try_start()
	{
		job_statistics.try_start();
		return job_statistics.count_tried;
	}

	public void try_end(long bytes_transferred)
	{
		job_statistics.try_end(bytes_transferred);
	}

	public int count_tried()
	{
		return job_statistics.count_tried;
	}

	public int count_failed()
	{
		return job_statistics.count_failed;
	}

	public int count_succeeded()
	{
		return job_statistics.count_succeeded;
	}

	public long time_last_tried()
	{
		return job_statistics.time_last_tried;
	}

	public long time_elapsed()
	{
		return job_statistics.time_elapsed();
	}

	public String toString()
	{
		return "Job#" + id;
	}
}
