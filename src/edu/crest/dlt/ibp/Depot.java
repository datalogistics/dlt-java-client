/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ibp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.crest.dlt.exception.IBPException;
import edu.crest.dlt.transfer.TransferStatistics;
import edu.crest.dlt.utils.Configuration;
import edu.crest.dlt.utils.DoubleHelper;

public class Depot implements Comparable<Depot>
{
	private static final Logger log = Logger.getLogger(Depot.class.getName());

	public static final int PREFERENCE_WEIGHT_MULTIPLIER = 1;

	public enum depot_state {
		nascent, setup, active
	}

	public static Map<String, Depot> depots = new HashMap<String, Depot>();

	public String host;
	public int port;
	public Set<String> locations;
	public depot_state state;

	/* runtime properties */
	public TransferStatistics transfer_statistics;
	private ArrayList<Socket> transfer_sockets_ready;
	private ArrayList<Socket> transfer_sockets_active;

	private Depot(String host, int port)
	{
		this.host = host;
		this.port = port;

		this.locations = new HashSet<String>();
		this.transfer_statistics = new TransferStatistics();
		this.transfer_sockets_ready = new ArrayList<Socket>();
		this.transfer_sockets_active = new ArrayList<Socket>();

		/* start setting up initial connection(s) */
		setup();
	}

	private Depot(String host, int port, String location)
	{
		this(host, port);
		this.locations.add(location);
	}

	/**
	 * @param host
	 * @param port
	 * @return a newly/previously prepared Depot object
	 */
	public static Depot depot(String host, String port)
	{
		return depot(host, Integer.parseInt(port));
	}

	public static Depot depot(String host, int port)
	{
		return depot(host, port, null);
	}

	public static Depot depot(String host, String port, String location)
	{
		return depot(host, Integer.parseInt(port), location);
	}

	public static Depot depot(String host, int port, String location)
	{
		String hostPort = host + ":" + port;
		Depot depot = null;

		synchronized (depots) {
			boolean added = false;
			if (!depots.containsKey(hostPort)) {
				depots.put(hostPort, new Depot(host, port, location));
				added = true;
			}

			depot = depots.get(hostPort);

			if (location != null) {
				depot.locations.add(location);
//				System.out.println(depot + ": " + depot.locations);
			}

			if (added) {
				depots.notifyAll();
			}
		}

		return depot;
	}

	public static List<Depot> depots()
	{
		if (depots.isEmpty()) {
			return new ArrayList<Depot>();
		}
		return new ArrayList<Depot>(depots.values());
	}

	/**
	 * close all open connection when this Depot is no longer needed
	 */
	protected void finalize()
	{
		close();
	}

	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof Depot)) {
			return (false);
		}

		Depot depot = (Depot) object;
		if (this.host.compareTo(depot.host) == 0 && this.port == depot.port) {
			return (true);
		} else {
			return (false);
		}
	}

	public synchronized void setup()
	{
		new Thread(() -> setup_synchronous()).start();
	}
	
	private synchronized void setup_synchronous()
	{
		try {
			close();
		} catch (Exception e) {
			// ignore failures
		}
		more_connections();
	}

	private synchronized void more_connections()
	{
		int count_connections = count_connections();
		int count_increment = (Configuration.dlt_depot_transfer_sockets_max / 3) > 2 ? Configuration.dlt_depot_transfer_sockets_max / 3
				: 2;
		int valid_increment = Configuration.dlt_depot_transfer_sockets_max - count_connections < count_increment ? Configuration.dlt_depot_transfer_sockets_max
				- count_connections
				: count_increment;

		/* start with 1; cap at count_increment */
		int more_connections = count_connections == 0 ? 1 : valid_increment;

		log.warning(this + ": adding " + more_connections + " connection(s).");

		final int connect_timeout = Configuration.dlt_depot_connect_timeout;
		List<Thread> threads = new ArrayList<Thread>(more_connections);
		for (; more_connections > 0; more_connections--) {
			threads.add(new Thread(() -> {
				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(host, port), connect_timeout);

					synchronized (transfer_sockets_ready) {
						transfer_sockets_ready.add(socket);
					}

					log.info(status());

					/* Depot is setup even if one connection is setup successfully */
					state = state == depot_state.nascent ? depot_state.setup : state;
					if (state == depot_state.setup) {
						synchronized (depots) {
							depots.notifyAll();
						}
					}
				} catch (IOException e) {
					log.warning(status());
				}
			}));
			threads.get(threads.size() -1).start();
		}

		for (Thread thread : threads) {
			try {
				thread.join(Configuration.dlt_depot_connect_timeout);
			} catch (InterruptedException e) {
				continue;
			}			
		}
		
		/* start inactive connections monitor-releaser */
		monitor_connections();
	}

	private boolean monitor_active = false;

	private synchronized void monitor_connections()
	{
		if (monitor_active) {
			return;
		}

		monitor_active = true;
		new Thread(
				() -> {
					long timeout_inactivity = Configuration.dlt_depot_inactivity_timeout;
					do {
						try {
							Thread.sleep(timeout_inactivity);
						} catch (Exception e) {
						}

						if (count_connections() > 0
								&& transfer_statistics.threads_active() == 0
								&& System.currentTimeMillis() - transfer_statistics.time_last_tried() >= timeout_inactivity) {
							less_connections();
						}
					} while (count_connections() > 0);
					monitor_active = false;
				}).start();
	}

	private synchronized void less_connections()
	{
		int count_connections = count_connections();
		int count_decrement = (Configuration.dlt_depot_transfer_sockets_max / 3) > 2 ? Configuration.dlt_depot_transfer_sockets_max / 3
				: 2;
		int valid_decrement = Configuration.dlt_depot_transfer_sockets_max - count_connections < count_decrement ? Configuration.dlt_depot_transfer_sockets_max
				- count_connections
				: count_decrement;

		/* start with 0; cap at count_increment */
		int less_connections = count_connections <= valid_decrement ? count_connections
				: valid_decrement;
		// int less_connections = count_connections <= 1 ? 0 : valid_decrement;

		log.warning(this + ": removing " + less_connections + " connection(s).");

		for (; less_connections > 0 && count_connections_ready() > 0; less_connections--) {
			Socket socket_free = transfer_sockets_ready.remove(0);
			try {
				if (!socket_free.isClosed()) {
					socket_free.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public synchronized int count_connections_ready()
	{
		if (transfer_sockets_ready == null) {
			return 0;
		}
		return transfer_sockets_ready.size();
	}

	public synchronized int count_connections_active()
	{
		if (transfer_sockets_active == null) {
			return 0;
		}
		return transfer_sockets_active.size();
	}

	public synchronized int count_connections()
	{
		return count_connections_ready() + count_connections_active();
	}

	public boolean connected()
	{
//		if(state == depot_state.nascent) {
//			setup_synchronous();
//		}
		return state != depot_state.nascent;
	}

	/**
	 * @return a free socket / null connect(): obtains a ready (connected) socket
	 *         and moves it to the active pool
	 */
	public synchronized Socket connect()
	{
		/* if a ready connection is available, return it */
		if (count_connections_ready() > 0) {
			Socket socket_free = transfer_sockets_ready.remove(0);

			transfer_sockets_active.add(socket_free);
			state = depot_state.active;
			log.info(status());

			transfer_statistics.try_start();
			return socket_free;
		}

		/* else try to add more connections */
		if (state != depot_state.nascent && count_connections() < Configuration.dlt_depot_transfer_sockets_max) {
			more_connections();
			return count_connections_ready() > 0 ? connect() : null;
		}

		/* else decline the connection request */
		log.warning(status() + " connection declined.");
		return null;
	}

	/**
	 * @param socket_busy
	 *          release(): releases an active socket and moves it back to the
	 *          ready (still connected) pool
	 */
	public synchronized void release(Socket socket_busy, long bytes_transferred)
	{
		if (socket_busy != null && transfer_sockets_active.contains(socket_busy)) {
			transfer_statistics.try_end(bytes_transferred); // success/failure

			transfer_sockets_active.remove(socket_busy);

			if (count_connections_active() == 0) {
				state = depot_state.setup;
			}

			if (Configuration.dlt_depot_transfer_sockets_reuse) {
				transfer_sockets_ready.add(socket_busy);
			} else {
				try {
					socket_busy.close();
				} catch (IOException e) {
				}
			}

			log.info(status());
		} else {
			transfer_statistics.try_end(0); // failure
		}

//		if (transfer_statistics.is_last_failed()) {
//			less_connections();
//		}
	}

	public synchronized void transfer_statistics_show()
	{
		log.info(">>> Stats for \t" + this + "\t<<<");
		log.info(" Tries: (" + transfer_statistics.count_succeeded() + "/"
				+ transfer_statistics.count_failed() + "/" + transfer_statistics.threads_active() + ")= "
				+ transfer_statistics.count_tried() + " total");
		log.info(" Wallclock: " + transfer_statistics.bytes_transferred() + " bytes / "
				+ transfer_statistics.time_elapsed() + " ms= "
				+ DoubleHelper.decimals_2(transfer_statistics.megabytes_per_second()) + " MB/s");
	}

	/**
	 * close(): closes all open (ready and/or active) sockets and clears the
	 * socket pools
	 */
	public synchronized void close()
	{
		for (Socket socket_free : transfer_sockets_ready) {
			try {
				if (!socket_free.isClosed()) {
					socket_free.close();
				}
			} catch (IOException e) {
			}
		}
		transfer_sockets_ready.clear();

		for (Socket socket_busy : transfer_sockets_active) {
			try {
				if (!socket_busy.isClosed()) {
					socket_busy.close();
				}
			} catch (IOException e) {
			}
		}
		transfer_sockets_active.clear();

		state = depot_state.nascent;
		log.info(status());
	}

	public Allocation allocateSoftByteArray(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}
		
		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateSoftByteArray(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateHardByteArray(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}
		
		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateHardByteArray(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateSoftBuffer(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}
		
		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateSoftBuffer(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
		
		return allocation;
	}

	public Allocation allocateHardBuffer(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}

		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateHardBuffer(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateSoftFifo(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}

		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateSoftFifo(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateHardFifo(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}

		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateHardFifo(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateSoftCirq(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}

		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateSoftCirq(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public Allocation allocateHardCirq(int duration, long size) throws IBPException
	{
		Socket socket = connect();
		if (socket == null) {
			return null;
		}

		Allocation allocation = null;
		try {
			allocation = IBPCommand.allocateHardCirq(socket, this, duration, size);
		} catch (IBPException e) {
			throw e;
		} finally {
			release(socket, allocation != null ? 1 : 0);	
		}
			
		return allocation;
	}

	public static final int FASTER = -1;
	public static final int BETTER = -1;
	public static final int EQUAL = 0;
	public static final int SLOWER = 1;
	public static final int WORSE = 1;

	/**
	 * @param operator
	 *          : one of FASTER, BETTER, EQUAL, SLOWER or WORSE
	 * @param other
	 *          depot
	 * @return true or false
	 */
	public boolean is(int operator, Depot other)
	{
		if (operator == FASTER || operator == BETTER) {
			int result = compareTo(other);
			return result == FASTER || result == BETTER;
		} else if (operator == EQUAL) {
			int result = compareTo(other);
			return result == EQUAL;
		} else if (operator == SLOWER || operator == WORSE) {
			int result = compareTo(other);
			return result == SLOWER || result == WORSE;
		} else {
			log.warning("unsupported depot comparison operator " + operator);
			return false;
		}
	}

	public static final int FIRST = -1;
	public static final int NEITHER = 0;
	public static final int SECOND = 1;

	@Override
	public int compareTo(Depot other)
	{
		/* ordered list of preference components (lowest first to highest last) */
		List<Integer> preferenceList = new ArrayList<Integer>();
		preferenceList.add(compareLastFailednFailureRates(this, other));
		preferenceList.add(compareTreadCountnTryTimes(this, other));
		preferenceList.add(compareTriesnSuccessRates(this, other));
		preferenceList.add(compareConnectedness(this, other));

		int preference = 0;
		int preferenceWeight = 1;// preferenceList.size();
		for (Integer preferenceComponent : preferenceList) {
			/*
			 * NOTE: each preference component multiplied with the preference weight
			 * contributes towards the overall depot-preference; preference weight is
			 * INCREASED for every subsequent preference component
			 */
			preference += preferenceComponent * (preferenceWeight);
			preferenceWeight = (1 + preferenceWeight) * PREFERENCE_WEIGHT_MULTIPLIER;
		}

		if (preference - FIRST < preference - SECOND) { // (preference < 0) {
			return FIRST;
		} else if (preference - SECOND < preference - FIRST) { // (preference > 0) {
			return SECOND;
		} else {
			return NEITHER;
		}
	}

	private boolean ignoreFailure(TransferStatistics ts)
	{
		if (ts.count_succeeded() == 0) {
			/*
			 * a depot's failure cannot be ignored if it has never ever succeeded yet
			 */
			return false;
		}

		/*
		 * probability that we will "ignore" this node's failure (i.e., that we give
		 * it another chance) grows with (1 - failure rate), but can be higher than
		 * 50% So if failure rate is high, then the threshold is lowered.
		 */
		double threshold = 0.5 * (1 - ts.rate_failure());

		// return true with the probability computed above
		return (Math.random() <= threshold);
	}

	private boolean isLastTimeFailed(TransferStatistics ts)
	{
		if (ignoreFailure(ts)) {
			return false;
		} else {
			return ts.is_last_failed();
		}
	}

	private int compareLastFailednFailureRates(Depot d1, Depot d2)
	{
		TransferStatistics d1TS = d1.transfer_statistics;
		TransferStatistics d2TS = d2.transfer_statistics;

		boolean d1FailedLastTime = isLastTimeFailed(d1TS);
		boolean d2FailedLastTime = isLastTimeFailed(d2TS);

		if (!d1FailedLastTime && d2FailedLastTime) {
			/*
			 * if depot#1 did not fail; while depot#2 failed
			 */
			log.info(d2 + " failed last time, while " + d1 + " did not (or is ignored).");
			return FIRST;
		} else if (d1FailedLastTime && !d2FailedLastTime) {
			/*
			 * if depot#1 failed; while depot#2 did not fail
			 */
			log.info("Compare TransferStatisticss: " + d1 + " failed last time, while " + d2
					+ " did not (or is ignored).");
			return SECOND;
		} else { // if (d1TS.isLastTimeFailed() && d2TS.isLastTimeFailed()) {
			/*
			 * NOTE: else means either none of them failed or both were chosen for
			 * ignoring failure
			 */
			double d1FailureRate = d1TS.rate_failure();
			double d2FailureRate = d2TS.rate_failure();
			log.info("Compare TransferStatisticss: " + d1 + " failure rate=" + d1FailureRate + ", " + d2
					+ " failure rate=" + d2FailureRate);

			/*
			 * LOWER FAILURE rate should return positive; hence the inversion of
			 * comparison
			 */
			int failureRateResult = Double.compare(d2FailureRate, d1FailureRate);
			if (failureRateResult == NEITHER) {
				/*
				 * If depot#1 failed before depot#2 then it's better. (Pick the least
				 * recently failed one as better.)
				 */
				failureRateResult = (d1TS.time_last_failed() < d2TS.time_last_failed()) ? FIRST : SECOND;
			}
			return failureRateResult;
		}
	}

	private int compareTreadCountnTryTimes(Depot d1, Depot d2)
	{
		TransferStatistics d1TS = d1.transfer_statistics;
		TransferStatistics d2TS = d2.transfer_statistics;

		int d1Threads = d1TS.count_tried();
		int d2Threads = d2TS.count_tried();
		log.info("Compare ThreadCounts: " + d1 + " thread count=" + d1Threads + ", " + d2
				+ " thread count=" + d2Threads);

		/* prefer the depot that is less loaded over the other */
		if (d1Threads < d2Threads) {
			return FIRST;
		} else if (d2Threads < d1Threads) {
			return SECOND;
		} else {
			/*
			 * if both are equally loaded prefer the one that was last tried (i.e.
			 * subjected to a load) before the other
			 */
			long d1LastTryTime = d1TS.time_last_tried();
			long d2LastTryTime = d2TS.time_last_tried();

			if (d1LastTryTime < d2LastTryTime) {
				return FIRST;
			} else if (d2LastTryTime < d1LastTryTime) {
				return SECOND;
			} else {
				return NEITHER;
			}
		}
	}

	private int compareTriesnSuccessRates(Depot d1, Depot d2)
	{
		TransferStatistics d1TS = d1.transfer_statistics;
		TransferStatistics d2TS = d2.transfer_statistics;

		int d1Tries = d1TS.count_tried();
		int d2Tries = d2TS.count_tried();

		log.info("Compare Tries: " + d1 + " tried " + d1Tries + " times, " + d2 + " tried " + d2Tries
				+ " times");
		/* if pending minimum tries, prefer it over the other */
		if (d1Tries < Configuration.dlt_depot_transfer_tries_for_comparison) {
			log.info("Compare Tries: " + d1 + " tried only " + d1Tries + " times");
			return FIRST;
		} else if (d2Tries < Configuration.dlt_depot_transfer_tries_for_comparison) {
			log.info("Compare Tries: " + d2 + " tried only " + d2Tries + " times");
			return SECOND;
		} else {
			/* else prefer the one with the higher success rate */
			return Double.compare(d2TS.rate_success(), d1TS.rate_success());
		}
	}

	private int compareConnectedness(Depot d1, Depot d2)
	{
		if (d1.connected() && !d2.connected()) {
			log.info("Compare Connectedness: " + d1 + " is connected but " + d2 + " is not");
			return FIRST;
		} else if (!d1.connected() && d2.connected()) {
			log.info("Compare Connectedness: " + d2 + " is connected but " + d1 + " is not");
			return SECOND;
		} else {
			if (d1.connected()) {
				log.info("Compare Connectedness: Both " + d1 + " and " + d2 + " are connected");
			} else {
				log.info("Compare Connectedness: Neither " + d1 + " nor " + d2 + " is connected");
			}

			return NEITHER;
		}
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	public String status()
	{
		switch (state) {
			case nascent:
				return this + " [NASCENT (no-connections-setup)]";
			case setup:
				return this + " [SETUP " + count_connections_ready() + "/"
						+ Configuration.dlt_depot_transfer_sockets_max + "]";
			case active:
				return this + " [ACTIVE " + count_connections_active() + "/" + count_connections() + "]";
			default:
				return this + " [UNKNOWN STATE]";
		}
	}

	public String toString()
	{
		return (host + ":" + port);
	}
}
