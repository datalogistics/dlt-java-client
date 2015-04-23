/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class ConcurrentJob extends Job
{
	private static Logger log = Logger.getLogger(ConcurrentJob.class.getName());
	public static final int CONCURRENT_TRY_ERROR = -1;

	/**
	 * @class to record start and runtime of each try of this job
	 */
	private List<TransferStatistics> try_statistics = new ArrayList<TransferStatistics>();

	public ConcurrentJob(int id)
	{
		super(id);
	}

	public synchronized int try_start()
	{
		if (try_statistics.size() == count_tried()) {
			TransferStatistics new_try_statistics = new TransferStatistics();
			new_try_statistics.try_start();

			try_statistics.add(new_try_statistics);
			return super.try_start(); // count_tried incremented here
		} else {
			log.warning("invalid retry of old try !!!");
			return CONCURRENT_TRY_ERROR;
		}
	}

	/**
	 * @param try_id
	 * @caution An instance of ConcurrentJob must never use the un-overloaded
	 *          try_stop() method inherited from Job.
	 */
	public void try_end(int try_id, long bytes_transferred)
	{
		if (!valid(try_id)) {
			return;
		}

		TransferStatistics old_try_statistics = try_statistics.get(try_id -1);

		synchronized (old_try_statistics) {
			if (!old_try_statistics.try_started()) {
				log.warning("cannot stop try id#" + try_id + "; not started.");
				return;
			}

			/* stop the previously started try */
			old_try_statistics.try_end(bytes_transferred);
			super.try_end(bytes_transferred);
		}
	}

	/**
	 * @param try_id
	 * @return time at which try #try_id of this job was started
	 */
	public long time_try_started(int try_id)
	{
		if (!valid(try_id)) {
			return 0;
		}

		TransferStatistics old_try_statistics = try_statistics.get(try_id - 1);
		return old_try_statistics.time_last_tried;
	}

	/**
	 * @param try_id
	 * @return runtime of this job's try #try_id
	 */
	public long time_try_elapsed(int try_id)
	{
		if (!valid(try_id)) {
			return 0;
		}

		TransferStatistics old_try_stats = try_statistics.get(try_id - 1);
		return old_try_stats.time_elapsed();
	}

	/**
	 * @return time when this concurrent job was first started
	 */
	public long time_first_started()
	{
		if (try_statistics.size() == 0) {
			return 0;
		}
		return try_statistics.get(0).time_last_tried;
	}

	/**
	 * @return total runtime of this job across all its tries
	 */
	public long time_elapsed()
	{
		long time_elapsed = 0;
		for (TransferStatistics next_try_statistics : try_statistics) {
			time_elapsed += next_try_statistics.time_elapsed();
		}
		return time_elapsed;
	}

	private boolean valid(int try_id)
	{
		if (try_statistics.size() < try_id) {
			log.warning("invalid old try id#" + try_id);
			return false;
		}
		return true;
	}

	public synchronized String toString()
	{
		return "Concurrent" + super.toString() + " try " + count_tried();
	}
}
