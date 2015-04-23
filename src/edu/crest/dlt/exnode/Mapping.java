/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exnode;

import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.exception.IBPException;
import edu.crest.dlt.exception.ReadException;
import edu.crest.dlt.exception.WriteException;
import edu.crest.dlt.exnode.function.Argument;
import edu.crest.dlt.exnode.function.Function;
import edu.crest.dlt.exnode.function.FunctionIdentity;
import edu.crest.dlt.exnode.metadata.Metadata;
import edu.crest.dlt.exnode.metadata.MetadataContainer;
import edu.crest.dlt.exnode.metadata.MetadataInteger;
import edu.crest.dlt.ibp.Allocation;
import edu.crest.dlt.transfer.TransferThread;
import edu.crest.dlt.utils.Configuration;

public class Mapping extends MetadataContainer
{
	private static final Logger log = Logger.getLogger(Mapping.class.getName());

	public Allocation allocation;
	public Function function;

	public Mapping()
	{
		add(new MetadataInteger("alloc_offset", -1));
		add(new MetadataInteger("exnode_offset", -1));
		add(new MetadataInteger("logical_length", -1));
	}
	
	public boolean accessible() {
		return allocation.accessible();
	}

	public long allocation_offset()
	{
		return (get("alloc_offset").getInteger().longValue());
	}

	public long allocation_length()
	{
		return (get("alloc_length").getInteger().longValue());
	}

	public long exnode_offset()
	{
		return (get("exnode_offset").getInteger().longValue());
	}

	public long logical_length()
	{
		return (get("logical_length").getInteger().longValue());
	}

	public long e2e_block_size()
	{
		return (get("e2e_blocksize").getInteger().longValue());
	}

	public boolean has_e2e_blocks()
	{
		return e2e_block_size() > 0;
	}

	public String start_date()
	{
		return (get("start").getString());
	}

	public String end_date()
	{
		return (get("end").getString());
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();
		xml.append("<exnode:mapping>\n");
		Iterator<Metadata> i = iterator();
		Metadata md;
		while (i.hasNext()) {
			md = i.next();
			xml.append(md.xml());
		}

		if (function != null) {
			xml.append(function.xml());
		}

		if (allocation != null) {
			xml.append(allocation.xml());
		}
		xml.append("</exnode:mapping>\n");

		return (xml.toString());
	}

	public static Mapping xml(Element xml) throws DeserializeException
	{
		String namespace = Configuration.dlt_exnode_namespace;
		Mapping mapping = new Mapping();

		try {
			NodeList xml_metadata = xml.getElementsByTagNameNS(namespace, "metadata");
			Metadata metadata;
			for (int i = 0; i < xml_metadata.getLength(); i++) {
				Element element = (Element) xml_metadata.item(i);
				if (element.getParentNode().equals(xml)) {
					metadata = Metadata.xml(element);
					mapping.add(metadata);
				}
			}

			NodeList xml_functions = xml.getElementsByTagNameNS(namespace, "function");
			for (int i = 0; i < xml_functions.getLength(); i++) {
				mapping.function = Function.xml((Element) xml_functions.item(i));
			}
			if (mapping.function == null) {
				mapping.function = new FunctionIdentity();
			}

			mapping.allocation = Allocation.xml(xml);

		} catch (DeserializeException ex) {
			log.severe("error deserializing mapping : " + ex);
			throw ex;
		}

		return (mapping);
	}

	private JsonObject lifetime_json()
	{
		JsonObjectBuilder json_lifetime = Json.createObjectBuilder();
		json_lifetime.add("start", start_date());
		json_lifetime.add("end", end_date());
		return json_lifetime.build();
	}

	public JsonObject json()
	{
		JsonObjectBuilder json_mapping = Json.createObjectBuilder();
		json_mapping.add("lifetimes", lifetime_json());
		json_mapping.add("mapping", allocation.json());
		json_mapping.add("location", "ibp://");
		json_mapping.add("offset", exnode_offset());
		json_mapping.add("size", logical_length());
		json_mapping.add("$schema", "http://unis.incntre.iu.edu/schema/exnode/ext/ibp#");
		return json_mapping.build();
	}

	public static Mapping json(JsonObject json_extent)
	{
		Mapping mapping = new Mapping();

		mapping.add(Metadata.json(json_extent, "offset"));
		mapping.add(Metadata.json(json_extent, "size"));

		// Need alloc_offset, alloc_length and e2e_blocksize metaData to
		// maintain end to end
		// integrity in Lors application. Creating dummy mapping. Could be
		// problem in future ?
		mapping.add(new MetadataInteger("alloc_offset", 0));
		mapping.add(new MetadataInteger("e2e_blocksize", 0));
		mapping.add(Metadata.json(json_extent, "alloc_length"));

		// This required for some unknown reason ??
		mapping.function = new FunctionIdentity();

		try {
			JsonObject json_mapping = json_extent.getJsonObject("mapping");
			mapping.allocation = Allocation.json(json_mapping);
		} catch (DeserializeException ex) {
			log.severe("error deserializing mapping : " + ex);
		}

		return mapping;
	}

	public byte[] read(Socket socket_to_read, long size, long readOffset,
			int offset_write) throws ReadException
	{
		long offset_load = -1;
		long length_load = size;

		try {
			/* logical_length if no function applied */
			boolean isFunctionIdentity = this.function.name.equals("identity");
			offset_load = allocation_offset() + readOffset;

			if (!isFunctionIdentity) { // If it is NOT Identity function
				log.info(this + ": not an identity function.");

				/* Some functions (e.g checksum, compress, etc.) do not require a key */
				Argument keyarg = this.function.argument("KEY");
				String key = keyarg != null ? keyarg.getString() : null;
				function.key(key);

				/* corresponding readOffset if there is a function */
				length_load = allocation_length();
			}

			/* NOTE: cannot create array > 2GB items; so make sure loadLength <= 2GB */
			if (length_load > Integer.MAX_VALUE) {
				throw new ReadException(this + ".read(): encountered load-length " + length_load + " > "
						+ Integer.MAX_VALUE);
			}

			log.info(this + ": requesting load [" + readOffset + " - " + (readOffset + length_load - 1)
					+ "] (" + length_load + " bytes)");
			byte[] bytes_raw = new byte[(int) length_load];
			int count_bytes_read = allocation.load(socket_to_read, bytes_raw, length_load,
					offset_load, offset_write);

			log.info(this + ": received " + bytes_raw.length + " bytes of raw-data.");
			TransferThread transferThread = (TransferThread) Thread.currentThread();
			transferThread.transfer_status = this + ": received " + count_bytes_read
					+ " bytes of raw-data.";

			byte[] bytes_out = function.execute(bytes_raw);
			log.info(this + ": received " + bytes_out.length + " bytes of data.");
			transferThread.transfer_status = this + ": received " + bytes_out.length + " bytes of data.";

			return bytes_out;
		} catch (Exception e) {
			e.printStackTrace();
			throw (new ReadException(e.getMessage()));

		}
	}

	public int write(Socket socket_to_read, byte[] buf, long size) throws WriteException
	{
		try {
			return (allocation.store(socket_to_read, buf, size));
		} catch (IBPException e) {
			throw (new WriteException(e.getMessage()));
		}
	}

	public String toString()
	{
		return "Mapping [" + exnode_offset() + " - " + (exnode_offset() + logical_length() - 1) + "]("
				+ logical_length() + "B)";
	}
}
