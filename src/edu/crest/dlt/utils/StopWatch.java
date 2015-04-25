/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.utils;

public class StopWatch
{
	public boolean running = false;
	private long runtime = 0;
	private long starttime = 0;
	private long stoptime = 0;

	public synchronized void start()
	{
		starttime = running ? starttime : (stoptime != 0 ? stoptime : System.currentTimeMillis());
		running = true;
	}

	public synchronized void stop()
	{
		stoptime = running ? System.currentTimeMillis() : stoptime;
		runtime += stoptime - starttime;

		starttime = 0;
		stoptime = 0;

		running = false;
	}

	public synchronized void pause()
	{
		stoptime = running ? System.currentTimeMillis() : stoptime;
		runtime += stoptime - starttime;

		starttime = stoptime;

		running = false;
	}

	public synchronized long time_run()
	{
		return !running ? runtime : runtime + System.currentTimeMillis() - starttime;
	}

	public synchronized long time_elapsed()
	{
		return System.currentTimeMillis() - starttime;
	}
}
