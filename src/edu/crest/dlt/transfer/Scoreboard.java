package edu.crest.dlt.transfer;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.crest.dlt.exnode.Mapping;
import edu.crest.dlt.ibp.Depot;

/**
 * @author rkhapare 03/16/2015
 */
public class Scoreboard
{
	private static Logger log = Logger.getLogger(Scoreboard.class.getName());

	private Progress transfer_progress = null;
	private Map<String, TransferStatistics> host_statistics = null;

	public Scoreboard()
	{
		transfer_progress = new Progress(0);
		host_statistics = new HashMap<String, TransferStatistics>();
	}

	public synchronized double percent_completed()
	{
		return transfer_progress.percent_transferred();
	}

	public synchronized void progress_listener(ProgressListener progress_listener)
	{
		transfer_progress.progress_listener(progress_listener);
	}

	public synchronized void progress_start(long bytes_expected)
	{
		transfer_progress.progress_reset(bytes_expected);
	}

	private synchronized void progress_update(Depot depot_tried, long offset_transferred,
			long bytes_transferred, String transfer_status)
	{
		transfer_progress.try_update(depot_tried, offset_transferred, bytes_transferred,
				transfer_status);
	}

	public synchronized void progress_error()
	{
		transfer_progress.notify_error();
		transfer_progress.try_end(0);
	}

	public synchronized Socket try_start(Depot depot_to_try)
	{
		transfer_progress.try_start();
		if (depot_to_try != null) {
			/* request the depot for a new free socket to read on */
			Socket socket = depot_to_try.connect();

			/* if free socket is found, start the try */
			if (socket != null) {
				if (!host_statistics.containsKey(depot_to_try.host)) {
					host_statistics.put(depot_to_try.host, new TransferStatistics());
				}
				host_statistics.get(depot_to_try.host).try_start();
			}
			return socket;
		}
		return null;
	}

	public synchronized void try_end(Depot depot_tried, Socket socket_tried, long offset_transferred,
			long bytes_transferred, String transfer_status)
	{
		transfer_progress.try_end(bytes_transferred);
		try {
			/* release the socket for others */
			depot_tried.release(socket_tried, bytes_transferred);

			progress_update(depot_tried, offset_transferred, bytes_transferred, transfer_status);

			if (socket_tried != null && host_statistics.containsKey(depot_tried.host)) {
				/* end try on target host */
				host_statistics.get(depot_tried.host).try_end(bytes_transferred);
			}
		} catch (Exception e) {
			log.warning("Error ending try for depot=" + depot_tried + " socket=" + socket_tried
					+ " bytes-transferred=" + bytes_transferred + ". " + e);
		}
	}

	/*
	 * Max Heap Operations
	 */
	private int parent(int pos)
	{
		return pos / 2;
	}

	public synchronized void add(List<Mapping> mappings_max_heap, Mapping mapping)
	{
		if (!mappings_max_heap.contains(mapping)) {
			/* add new mapping to end of the maxHeap */
			mappings_max_heap.add(mapping);
			int current = mappings_max_heap.size() - 1;

			/* heapify starting from the newly inserted mapping */
			while (parent(current) < current
					&& mappings_max_heap.get(current).allocation.depot.is(Depot.BETTER,
							mappings_max_heap.get(parent(current)).allocation.depot)) {
				Collections.swap(mappings_max_heap, current, parent(current));
				current = parent(current);
			}
		}
		// mappings_max_heap_display(mappings_max_heap);
	}

	public synchronized Mapping mapping_best(List<Mapping> mappings_max_heap)
	{
		if (mappings_max_heap.size() > 0) {
			/* remove head of MaxHeap */
			Mapping mapping_head = mappings_max_heap.remove(0);
			Mapping mapping_best = mapping_head;

			/* reinsert (so that the new depot gets re-heapified) */
			add(mappings_max_heap, mapping_head);

			return mapping_best;
		}
		return null;
	}

	public synchronized void mappings_max_heap_display(List<Mapping> mappings_max_heap)
	{
		System.out
				.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Depot Max Heap ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (int i = 0; i < mappings_max_heap.size(); i++) {
			System.out.printf("[%s]", mappings_max_heap.get(i));

			/* printing [Parent] [Left-Child] [Right-Child] triplets */
			if ((i + 1) % 3 == 0) {
				System.out.printf("\n");
			}
		}
		System.out
				.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	public String toString()
	{
		StringBuffer status = new StringBuffer(transfer_progress.status()).append(" ");
		for (Map.Entry<String, TransferStatistics> host_stats : host_statistics.entrySet()) {
			status.append("(").append(host_stats.getKey()).append(" ")
					.append(host_stats.getValue().bytes_transferred).append("B ")
					.append(host_stats.getValue().elapsed_seconds()).append("s ")
					.append(host_stats.getValue().megabytes_per_second()).append("MB/s").append(")");
		}
		return status.toString();
	}
}
