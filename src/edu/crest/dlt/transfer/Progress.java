/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.transfer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import edu.crest.dlt.ibp.Depot;

public class Progress extends TransferStatistics
{
	private static Logger log = Logger.getLogger(Progress.class.getName());

	private List<ProgressListener> progress_listeners;

	public long bytes_expected;
	private double time_expected_end;

	public String transfer_status;

	public Progress(long bytes_expected)
	{
		super();
		this.bytes_expected = bytes_expected;
		this.time_expected_end = 0.0;
		this.progress_listeners = new ArrayList<ProgressListener>();
	}

	public String status()
	{
		return "[" + bytes_transferred + "B " + elapsed_seconds() + "s "
				+ format_double(megabytes_per_second()) + "MB/s]";
	}

	public void progress_reset(long bytes_expected)
	{
		super.reset();
		this.bytes_expected = bytes_expected;
	}

	public void progress_listener(ProgressListener progress_listener)
	{
		progress_listeners.add(progress_listener);
	}

	// public synchronized void try_update(long progress)
	// {
	// try_update(progress, transfer_status);
	// }

	public synchronized void try_update(Depot depot_tried, long offset_transferred,
			long bytes_transferred, String transfer_status)
	{
		long bytes_transferred_update = this.bytes_transferred + bytes_transferred;
		long remaining = bytes_expected - bytes_transferred;
		this.transfer_status = transfer_status;

		if (this.bytes_transferred != 0) {
			time_expected_end = (remaining * elapsed_seconds()) / ((double) bytes_transferred_update);
		}

		log.info(this + " : " + bytes_transferred_update + " bytes transferred in " + elapsed_seconds()
				+ "s at " + format_double(megabytes_per_second() / 8.0) + "MB/s. ~"
				+ format_double(time_expected_end) + "s remaining.");

		notify_update(depot_tried, offset_transferred, bytes_transferred);
		if (remaining == 0) {
			notify_done();
		}
	}

	public double percent_transferred()
	{
		return ((double) bytes_transferred / (double) bytes_expected) * 100.0f;
	}

	/**
	 * Notify all progress_listeners
	 */
	public synchronized void notify_update(Depot depot_tried, long offset_transferred,
			long bytes_transferred)
	{
		final ProgressEvent event = new ProgressEvent(depot_tried, transfer_status, offset_transferred,
				bytes_transferred, this.bytes_transferred, percent_transferred(), megabytes_per_second(),
				time_expected_end, elapsed_seconds());

		for (final ProgressListener progress_listener : progress_listeners) {
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progress_listener.progressUpdated(event);
				}
			});
		}
	}

	private synchronized void notify_done()
	{
		final ProgressEvent event = new ProgressEvent(null, transfer_status, 0, 0, bytes_transferred,
				percent_transferred(), megabytes_per_second(), time_expected_end, elapsed_seconds());

		for (final ProgressListener progress_listener : progress_listeners) {
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progress_listener.progressDone(event);
				}
			});
		}
	}

	public synchronized void notify_error()
	{
		final ProgressEvent event = new ProgressEvent(null, transfer_status, 0, 0, bytes_transferred,
				percent_transferred(), megabytes_per_second(), time_expected_end, elapsed_seconds());

		for (final ProgressListener progress_listener : progress_listeners) {
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					progress_listener.progressError(event);
				}
			});
		}
	}

	public static NumberFormat format_double;
	static {
		format_double = NumberFormat.getNumberInstance();
		format_double.setMaximumFractionDigits(3);
	}

	public static String format_double(double value)
	{
		return format_double.format(value);
	}

	public static String format_date(int seconds)
	{
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		seconds = (seconds % 3600) % 60;
		return hours + " : " + minutes + " : " + seconds;
	}

	public String toString()
	{
		return "Progress [" + bytes_expected + "]";
	}
}
