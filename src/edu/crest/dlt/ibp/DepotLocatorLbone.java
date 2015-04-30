/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ibp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DepotLocatorLbone
{
	private static final String VERSION = "0.0.1";
	private static final String REQUEST_TYPE = "1";
	private static final int REQUEST_TIMEOUT = 120000;

	private static final int LENGTH_MSG = 512;
	private static final int LENGTH_HOST_MAX = 256;
	private static final int LENGTH_FIELD = 10;

	private static final int SUCCESS = 1;
	// private static final int FAILURE = 2;

	private String host;
	private int port;

	public DepotLocatorLbone(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public DepotLocatorLbone(String host, String port)
	{
		this(host, Integer.parseInt(port));
	}

	public List<Depot> depots(int count_depots, int hard, int soft, int duration, String location)
			throws Exception
	{
		if (hard < 0 || soft < 0 || (hard == 0 && soft == 0)) {
			throw (new IllegalArgumentException());
		}

		StringBuffer request_buf = new StringBuffer();
		request_buf.append(field_padded(VERSION));
		request_buf.append(field_padded(REQUEST_TYPE));
		request_buf.append(field_padded(String.valueOf(count_depots)));
		request_buf.append(field_padded(String.valueOf(hard)));
		request_buf.append(field_padded(String.valueOf(soft)));
		request_buf.append(field_padded(String.valueOf(duration)));
		request_buf.append(location != null ? location : "NULL");
		String request = request_padded(request_buf.toString());

		Socket socket = null;
		try {
			socket = new Socket(host, port);
			socket.setSoTimeout(REQUEST_TIMEOUT);

			OutputStream out_stream = socket.getOutputStream();
			BufferedReader in_stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out_stream.write(request.getBytes());

			StringBuffer response = new StringBuffer();
			response.append(in_stream.readLine());

			int return_code = Integer.parseInt(response.substring(0, 10).trim());

			if (return_code == SUCCESS) {
				return (parse_depots(response.substring(10), location));
			} else {
				// System.out.println("LBone return code " + return_code);
				String error = response.substring(10, 20);
				throw (new Exception("LBone Error: " + error));
			}
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	private String field_padded(String field)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < LENGTH_FIELD - field.length(); i++) {
			buf.append(' ');
		}
		buf.append(field);
		return (buf.toString());
	}

	private String request_padded(String request)
	{
		StringBuffer buf = new StringBuffer(request);
		for (int i = 0; i < LENGTH_MSG - request.length(); i++) {
			buf.append('\0');
		}
		return (buf.toString());
	}

	private List<Depot> parse_depots(String list, String location)
	{
		int count_depots = Integer.parseInt(list.substring(0, 10).trim());

		List<Depot> depots = new ArrayList<Depot>();
		for (int i = 0; i < count_depots; i++) {
			int host_start_idx = i * (LENGTH_HOST_MAX + LENGTH_FIELD) + 10;
			int host_end_idx = host_start_idx + LENGTH_HOST_MAX;
			int port_start_idx = host_end_idx;
			int port_end_idx = port_start_idx + 10;

			String host = list.substring(host_start_idx, host_end_idx).trim();
			String port = list.substring(port_start_idx, port_end_idx).trim();

			depots.add(Depot.depot(host, port, location));
		}

		return (depots);
	}
}
