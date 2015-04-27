/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.utils;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import edu.crest.dlt.exnode.ExnodeRegistryUNIS;
import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.ibp.DepotLocatorLbone;

public class Configuration
{
	private static final Logger log = Logger.getLogger(Configuration.class.getClass().getName());
	private static final InputStream dlt_configuration_file = Configuration.class
			.getResourceAsStream("config.properties");
	private static Level dlt_log_console_level;

	/* Define all your application-required properties here */
	public static String dlt_ui_title;
	public static String dlt_ui_progress_map_send_url;
	public static String dlt_ui_progress_map_view_url;
	public static String dlt_ui_regex_directory;

	public static int dlt_exnode_transfer_connections_default;
	public static long dlt_exnode_transfer_size_default;
	public static long dlt_transfer_exhaust_timeout;

	public static String dlt_exnode_namespace;
	public static String dlt_exnode_version;

	public static int dlt_exnode_read_retry_interval;
	public static long dlt_exnode_transfer_log_interval;
	public static int dlt_exnode_read_retries_max;
	public static int dlt_exnode_write_retries_max;
	public static String dlt_exnode_write_directory_default;
	public static ExnodeRegistryUNIS dlt_exnode_registry_unis;
	public static String dlt_exnode_registry_unis_content_type;
	public static int dlt_exnode_registry_unis_request_timeout;
	public static SimpleDateFormat dlt_exnode_date_formatter;

	public static int dlt_depot_transfer_sockets_max;
	public static int dlt_depot_transfer_tries_for_comparison;
	public static boolean dlt_depot_transfer_shuffle;
	public static int dlt_depot_connect_timeout;
	public static int dlt_depot_request_timeout;
	public static int dlt_depot_inactivity_timeout;
	public static boolean dlt_depot_transfer_sockets_reuse;
	public static List<DepotLocatorLbone> dlt_depot_locators_lbone;
	public static List<String> dlt_depot_locations;

	public static int dlt_file_write_retries_max;
	public static int dlt_file_read_buffer_size;

	public static String dlt_username;
	public static String dlt_password;
	public static List<String> dlt_file_paths;

	/*
	 * static block for NetBeans to access Configuration objects for building its
	 * components
	 */
	static {
		dlt_depot_locations = new ArrayList<String>();
		dlt_depot_locations.add("");
	}

	public static boolean load()
	{
		Logger root_logger = Logger.getLogger("");

		List<Handler> handlers = Arrays.asList(root_logger.getHandlers());
		handlers.forEach((handler) -> {
			Formatter formatter = new SimpleFormatter()
			{
				@Override
				public synchronized String format(LogRecord record)
				{
					return "[" + record.getLevel() + "] " + record.getSourceClassName() + "."
							+ record.getSourceMethodName() + "(): " + record.getMessage() + "\n";
				}
			};
			handler.setFormatter(formatter);
		});

		Properties configuration = new Properties();
		try {
			configuration.load(dlt_configuration_file);

			dlt_log_console_level = console_log_level(configuration, "dlt.log.console");

			handlers.forEach((handler) -> {
				if (handler instanceof ConsoleHandler) {
					handler.setLevel(dlt_log_console_level);
				}
			});

			dlt_ui_title = property(configuration, "dlt.title");
			dlt_ui_progress_map_send_url = property(configuration, "dlt.progress.map.send.url");
			dlt_ui_progress_map_view_url = property(configuration, "dlt.progress.map.view.url");
			dlt_ui_regex_directory = property(configuration, "dlt.regex.directory");
			dlt_exnode_transfer_connections_default = Integer.parseInt(property(configuration,
					"dlt.transfer.connections.default"));
			dlt_exnode_transfer_size_default = Long.parseLong(property(configuration,
					"dlt.transfer.size.default"));
			dlt_transfer_exhaust_timeout = Long.parseLong(property(configuration,
					"dlt.transfer.exhaust.timeout"));

			dlt_exnode_namespace = property(configuration, "dlt.exnode.namespace");
			dlt_exnode_version = property(configuration, "dlt.exnode.version");

			dlt_exnode_read_retry_interval = Integer.parseInt(property(configuration,
					"dlt.exnode.read.retry.cycletime"));
			dlt_exnode_read_retries_max = Integer.parseInt(property(configuration,
					"dlt.exnode.read.retries"));
			dlt_exnode_transfer_log_interval = Long.parseLong(property(configuration,
					"dlt.exnode.transfer.log.interval"));
			dlt_exnode_write_retries_max = Integer.parseInt(property(configuration,
					"dlt.exnode.write.retries"));
			dlt_exnode_write_directory_default = property(configuration,
					"dlt.exnode.write.directory_default");
			dlt_exnode_registry_unis = new ExnodeRegistryUNIS(new URL(property(configuration,
					"dlt.exnode.registry.unis")));
			dlt_exnode_registry_unis_content_type = property(configuration,
					"dlt.exnode.registry.unis.content_type");
			dlt_exnode_registry_unis_request_timeout = Integer.parseInt(property(configuration,
					"dlt.exnode.registry.unis.request.timeout"));
			dlt_exnode_date_formatter = new SimpleDateFormat(property(configuration,
					"dlt.exnode.date.format"));

			dlt_depot_transfer_sockets_max = Integer.parseInt(property(configuration,
					"dlt.depot.transfer.sockets.max"));
			dlt_depot_transfer_tries_for_comparison = Integer.parseInt(property(configuration,
					"dlt.depot.comparison.transfer_tries.min"));
			dlt_depot_transfer_shuffle = Boolean.parseBoolean(property(configuration,
					"dlt.depot.transfer.shuffle"));
			dlt_depot_connect_timeout = Integer.parseInt(property(configuration,
					"dlt.depot.connect.timeout"));
			dlt_depot_request_timeout = Integer.parseInt(property(configuration,
					"dlt.depot.request.timeout"));
			dlt_depot_inactivity_timeout = Integer.parseInt(property(configuration,
					"dlt.depot.inactivity.timeout"));
			dlt_depot_transfer_sockets_reuse = Boolean.parseBoolean(property(configuration,
					"dlt.depot.transfer.sockets.reuse"));
			List<String> lbone_servers = properties(configuration, "dlt.depot.locator.lbone");
			dlt_depot_locators_lbone = new ArrayList<DepotLocatorLbone>();
			lbone_servers.forEach((lbone_server) -> {
				List<String> host_port = host_port_verified(lbone_server);
				if (host_port != null) {
					dlt_depot_locators_lbone.add(new DepotLocatorLbone(host_port.get(0), host_port.get(1)));
				}
			});
			dlt_depot_locations = properties(configuration, "dlt.depot.locations");
			List<String> dlt_depots = properties(configuration, "dlt.depot.list");
			dlt_depots.forEach((dlt_depot) -> {
				List<String> host_port = host_port_verified(dlt_depot);
				if (host_port != null) {
					Depot.depot(host_port.get(0), host_port.get(1));
				}
			});

			dlt_file_write_retries_max = Integer.parseInt(property(configuration,
					"dlt.file.write.retries"));
			dlt_file_read_buffer_size = Integer.parseInt(property(configuration,
					"dlt.file.read.buffer.size.default"));
		} catch (Exception e) {
			log.severe("failed to load initial configuration. " + e);
			return false;
		}

		return true;
	}

	private static String property(Properties configuration, String property_key)
	{
		String property_value = configuration.getProperty(property_key);
		log.info("read \"" + property_key + "\"=\"" + property_value + "\"");
		return property_value;
	}

	private static Level console_log_level(Properties configuration, String property_key)
	{
		String level = property(configuration, property_key);
		if (level == null) {
			return Level.OFF;
		}
		level = level.trim().toLowerCase();
		if (level.equals("severe")) {
			return Level.SEVERE;
		} else if (level.equals("warning") || level.equals("default") || level.equals("debug")) {
			return Level.WARNING;
		} else if (level.equals("info")) {
			return Level.INFO;
		} else if (level.equals("config")) {
			return Level.CONFIG;
		} else if (level.equals("fine")) {
			return Level.FINE;
		} else if (level.equals("finer")) {
			return Level.FINER;
		} else if (level.equals("finest")) {
			return Level.FINEST;
		}
		return Level.ALL;
	}

	private static List<String> properties(Properties configuration, String property_key)
	{
		String property_value = configuration.getProperty(property_key);
		log.info("read \"" + property_key + "\"=\"" + property_value + "\"");

		List<String> property_values = new ArrayList<String>();
		if (property_value.length() > 0) {
			StringTokenizer tokenizer = new StringTokenizer(property_value);
			while (tokenizer.hasMoreTokens()) {
				String property_entry = tokenizer.nextToken().trim();
				property_values.add(property_entry);
			}
		}

		return property_values;
	}

	public static List<String> host_port_verified(String host_port)
	{
		if (host_port.length() > 0 && host_port.contains(":")) {
			StringTokenizer tokenizer = new StringTokenizer(host_port, ":");

			String host = "";
			String port = "";
			try {
				host = tokenizer.nextToken();
				port = tokenizer.nextToken();
				Integer.parseInt(port);
				return new ArrayList<String>(Arrays.asList(host, port));
			} catch (Exception e) {
			}
		}
		return null;
	}
}
