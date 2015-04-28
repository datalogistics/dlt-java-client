/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.transfer;

import java.util.logging.Logger;

import edu.crest.dlt.utils.StopWatch;

public class TransferStatistics
{
	private static final Logger log = Logger.getLogger(TransferStatistics.class.getName());

	protected long bytes_transferred;
	protected int threads_active;
	protected StopWatch timer;

	protected int count_tried;
	protected int count_succeeded;
	protected int count_failed;

	protected long time_last_tried;
	protected long time_last_succeeded;
	protected long time_last_failed;

	public TransferStatistics()
	{
		reset();
	}

	public synchronized void reset()
	{
		bytes_transferred = 0;
		threads_active = 0;
		timer = new StopWatch();

		count_tried = 0;
		count_succeeded = 0;
		count_failed = 0;

		time_last_tried = 0;
		time_last_succeeded = 0;
		time_last_failed = 0;
	}

	public synchronized long bytes_transferred()
	{
		return bytes_transferred;
	}

	public synchronized int count_tried()
	{
		return count_tried;
	}

	public synchronized int count_succeeded()
	{
		return count_succeeded;
	}

	public synchronized int count_failed()
	{
		return count_failed;
	}

	public synchronized int threads_active()
	{
		return threads_active;
	}

	public long time_last_tried()
	{
		return time_last_tried;
	}

	public long time_last_succeeded()
	{
		return time_last_succeeded;
	}

	public long time_last_failed()
	{
		return time_last_failed;
	}

	public synchronized boolean is_last_failed()
	{
		return time_last_failed > time_last_succeeded;
	}

	public synchronized double rate_success()
	{
		return (double) count_succeeded / count_tried;
	}

	public synchronized double rate_failure()
	{
		return (double) count_failed / count_tried;
	}

	public synchronized void try_start()
	{
		threads_active++;
		time_last_tried = System.currentTimeMillis();
		count_tried++;

		timer.start();
	}

	public synchronized boolean try_started()
	{
		return threads_active > 0;
	}

	public synchronized void try_end(long transferred_bytes)
	{
		threads_active--;
		if (transferred_bytes == 0) {
			time_last_failed = System.currentTimeMillis();
			count_failed++;
		} else {
			bytes_transferred += transferred_bytes;
			time_last_succeeded = System.currentTimeMillis();
			count_succeeded++;
		}

		/* if no ongoing try, pause the timer */
		if (threads_active == 0) {
			timer.pause();
		}
	}

//	public synchronized double transfer_speed()
//	{
//		return (double) bytes_transferred / timer.time_run();
//	}

	public long time_elapsed()
	{
		return timer.time_run();
	}

	public double elapsed_seconds()
	{
		return ((double) timer.time_run()) / 1000.0;
	}

	/* throughput in B/s */
	public double bytes_per_second()
	{
		return (bytes_transferred) / elapsed_seconds();
	}

	/* throughput in KB/s */
	public double kilobytes_per_second()
	{
		return (bytes_transferred) / 1024.0 / elapsed_seconds();
	}

	/* throughput in MB/s */
	public double megabytes_per_second()
	{
		if (bytes_transferred == 0 || elapsed_seconds() == 0) {
			return 0;
		}
		return (bytes_transferred) / 1024.0 / 1024.0 / elapsed_seconds();
	}
}
