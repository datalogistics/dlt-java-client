/*
 * Created on Feb 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.crest.dlt.transfer;

import java.net.Socket;
import java.util.logging.Logger;

import edu.crest.dlt.ibp.Depot;

/**
 * @author lfgs
 */
public class TransferThread extends Thread
{
	private static Logger log = Logger.getLogger(TransferThread.class.getName());

	/*
	 * set priority to be slightly LESS than normal processes so it doesn't bog
	 * down other normal processes.
	 */
	public static int TRANSFER_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
	public static long CANCELLATION_POLL_TIME = 1000;

	protected int id;

	protected Depot target_depot;
	protected Socket transfer_socket;
//	public Job transfer_job;
	public String transfer_status;

	public TransferThread(int id)
	{
		super();
		this.id = id;

		this.setPriority(TransferThread.TRANSFER_THREAD_PRIORITY);
	}

	public long id()
	{
		return id;
	}

//	public Socket socket()
//	{
//		return transfer_socket;
//	}
//
//	public Depot depot()
//	{
//		return target_depot;
//	}
//
//	public String host()
//	{
//		return target_depot.host;
//	}
//
//	public int port()
//	{
//		return target_depot.port;
//	}
//
//	public void transfer_ready(Depot target_depot, Socket target_socket)
//	{
//		this.target_depot = target_depot;
//		this.transfer_socket = target_socket;
//
//		transfer_status = "Ready to initiate transfer with " + target_depot + " using socket " + target_socket;
//	}
//
//	private void transfer_close()
//	{
////		transfer_status = "Interrupted while performing " + transfer_job;
//		this.transfer_status = "Transfer closing.";
//		if (target_depot != null && transfer_socket != null) {
//			target_depot.release(transfer_socket, 0);
//			this.transfer_status += " Released socket to " + target_depot;
//		}
//		log.severe(this + " closed.");
//	}

	public String toString() {
		return "TransferThread#" + id;
	}
}
