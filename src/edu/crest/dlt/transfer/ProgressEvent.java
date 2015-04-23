/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Jan 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.transfer;

import edu.crest.dlt.ibp.Depot;

/**
 * @author millar
 * @coauthor rkhapare 03/19/2015
 */
public class ProgressEvent
{
	/* Instance variables */
	public final Depot depot;
	public final long transfer_offset;
	public final long transfer_length;
	public final String status;
	public final long completed_total;
	public final double completed_percent;
	public final double throughput;
	public final double time_expected_arrival;
	public final double time_elapsed;

	public ProgressEvent(Depot depot, String statusMsg, long transfer_offset, long transfer_length,
			long total, double percentComplete, double throughput, double time_expected_arrival, double time_elapsed)
	{
		this.depot = depot;
		this.transfer_offset = transfer_offset;
		this.transfer_length = transfer_length;
		this.status = statusMsg;
		this.completed_total = total;
		this.completed_percent = percentComplete;
		this.throughput = throughput;
		this.time_expected_arrival = time_expected_arrival;
		this.time_elapsed = time_elapsed;
	}
}
