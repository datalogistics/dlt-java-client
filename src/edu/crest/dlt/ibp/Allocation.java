/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ibp;

import java.net.Socket;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.w3c.dom.Element;

import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.exception.IBPException;

public class Allocation
{
	private static final Logger log = Logger.getLogger(Allocation.class.getName());

	public Depot depot;
	public Capability capability_read = null;
	public Capability capability_write = null;
	public Capability capability_manage = null;

	public Allocation(Capability read, Capability write, Capability manage) throws IBPException
	{
		if (capabilities_verify(read, write, manage)) {
			capability_read = read;
			capability_write = write;
			capability_manage = manage;

			String host = read != null ? read.host : (write != null ? write.host
					: (manage != null ? manage.host : null));
			String port = read != null ? read.port : (write != null ? write.port
					: (manage != null ? manage.port : null));
			depot = Depot.depot(host, port);

			log.info("new allocation : " + this);
		} else {
			throw new IBPException(-4);
		}
	}

	private boolean capabilities_verify(Capability read, Capability write, Capability manage)
	{
		String host = read != null ? read.host : null;
		host = host == null && write != null ? write.host : host;
		host = host == null && manage != null ? manage.host : host;

		String port = read != null ? read.port : null;
		port = port == null && write != null ? write.port : port;
		port = port == null && manage != null ? manage.port : port;

		return host != null && port != null
				&& (read == null || (read.host.equals(host) && read.port.equals(port)))
				&& (write == null || (write.host.equals(host) && write.port.equals(port)))
				&& (manage == null || (manage.host.equals(host) && manage.port.equals(port)));
	}

	public boolean accessible()
	{
		return depot.connected();
	}

	public String xml()
	{
		return capability_read.xml() + capability_write.xml() + capability_manage.xml();
	}

	public static Allocation xml(Element xml) throws DeserializeException
	{
		try {
			Capability[] capabilities = Capability.xml(xml);

			return new Allocation(capabilities[Capability.CAPABILITY_READ],
					capabilities[Capability.CAPABILITY_WRITE], capabilities[Capability.CAPABILITY_MANAGE]);
		} catch (IBPException ex) {
			log.severe("error deserializing allocation : " + ex);
			throw (new DeserializeException(ex.getMessage()));
		}
	}

	public JsonObject json()
	{
		JsonObjectBuilder json_allocation = Json.createObjectBuilder();
		json_allocation.add("read", capability_read.json());
		json_allocation.add("write", capability_write.json());
		json_allocation.add("manage", capability_manage.json());
		return json_allocation.build();
	}

	public static Allocation json(JsonObject json_mapping) throws DeserializeException
	{
		try {
			Capability[] capabilities = Capability.json(json_mapping);

			return new Allocation(capabilities[Capability.CAPABILITY_READ],
					capabilities[Capability.CAPABILITY_WRITE], capabilities[Capability.CAPABILITY_MANAGE]);
		} catch (IBPException ex) {
			log.severe("error deserializing allocation : " + ex);
			throw (new DeserializeException(ex.getMessage()));
		}
	}

	public int store(Socket socket_to_read, byte[] buf, long size) throws IBPException
	{
		return (IBPCommand.store(socket_to_read, this, buf, size));
	}

	public int load(Socket socket_to_read, byte[] buf, long size, long readOffset, int writeOffset)
			throws IBPException
	{
		return (IBPCommand.load(socket_to_read, this, buf, size, readOffset, writeOffset));
	}

	public int copy(Allocation destination, long size, long offset) throws IBPException
	{
		return (IBPCommand.copy(this, destination, size, offset));
	}

	public String toString()
	{
		return "[r:" + capability_read + ";w:" + capability_write + ";m:" + capability_manage + "]";
	}
}
