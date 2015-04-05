/* $Id: JobQueue.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.transfer;

import java.util.ArrayDeque;
import java.util.Deque;

public class JobQueue
{
	public static final Job job_placeholder = new Job(-1)
	{
		@Override
		public boolean isInterrupted()
		{
			return false;
		}

		@Override
		public void execute() throws Throwable
		{
		}

		@Override
		public boolean ready()
		{
			return false;
		}
	};

	private Deque<Job> queue;
	private Deque<Job> queue_pushback;

	public JobQueue()
	{
		queue = new ArrayDeque<Job>();
		queue_pushback = new ArrayDeque<Job>();
	}

	public synchronized void clear()
	{
		queue.clear();
	}

	public synchronized void add(Job job_place_tail)
	{
		queue.add(job_place_tail);
	}

	public synchronized void add(Job job_place_at_index, int index)
	{
		while (queue_pushback.size() < (index - 1)) {
			queue_pushback.add(job_placeholder);
		}
		queue_pushback.add(job_place_at_index);
	}

	public synchronized void remove(Job o)
	{
		queue.remove(o);
	}

	public synchronized Job remove()
	{
		if (queue_pushback.size() > 0) {
			Job placed_job = queue_pushback.removeFirst();
			if (!placed_job.equals(job_placeholder)) {
				return placed_job;
			}
		}
		return (!queue.isEmpty() ? queue.removeFirst() : null);
	}

	public synchronized int size()
	{
		int count_placed_jobs = 0;
		for (Job placed_job : queue_pushback) {
			if (!placed_job.equals(job_placeholder)) {
				count_placed_jobs++;
			}
		}
		return (queue.size() + count_placed_jobs);
	}

	public synchronized Job peek()
	{
		if (queue_pushback.size() > 0) {
			Job placed_job = queue_pushback.peek();
			if (!placed_job.equals(job_placeholder)) {
				return placed_job;
			}
		}
		return queue.peek();
	}

	public boolean contains(Job job)
	{
		return queue.contains(job);
	}

	public boolean isEmpty()
	{
		return size() == 0;
	}
}
