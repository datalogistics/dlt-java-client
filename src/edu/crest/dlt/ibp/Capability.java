package edu.crest.dlt.ibp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.utils.Configuration;

public class Capability
{
	private static final Logger log = Logger.getLogger(Capability.class.getName());

	public static final int CAPABILITY_READ = 0;
	public static final int CAPABILITY_WRITE = 1;
	public static final int CAPABILITY_MANAGE = 2;

	private int capability_type;
	private URI uri;
	public String protocol;
	public String host;
	public String port;
	public String key;
	public String key_wrm;
	public String type;

	public Capability(int capability_type, String uri) throws URISyntaxException
	{
		if (capability_type < CAPABILITY_READ || capability_type > CAPABILITY_MANAGE) {
			throw new IllegalArgumentException("Invalid capability type " + capability_type);
		}
		this.capability_type = capability_type;
		this.uri = new URI(uri);
		protocol = this.uri.getScheme();
		host = this.uri.getHost();
		port = new Integer(this.uri.getPort()).toString();

		String[] fields = uri.split("/");
		key = fields[3];
		key_wrm = fields[4];
		type = fields[5];

		log.info("new capability : " + this);
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();
		String capability_type_string = "";
		switch (capability_type) {
			case CAPABILITY_READ:
				capability_type_string = "read";
				break;
			case CAPABILITY_WRITE:
				capability_type_string = "write";
				break;
			case CAPABILITY_MANAGE:
				capability_type_string = "manage";
				break;
		}
		xml.append("<exnode:" + capability_type_string + ">");
		xml.append(this);
		xml.append("</exnode:" + capability_type_string + ">\n");

		return xml.toString();
	}

	public static Capability[] xml(Element xml) throws DeserializeException
	{
		String namespace = Configuration.exnode_namespace;
		Capability[] capabilities = new Capability[3];
		try {
			NodeList xml_capability = xml.getElementsByTagNameNS(namespace, "read");
			Element xml_element = (Element) xml_capability.item(0);
			capabilities[CAPABILITY_READ] = xml_element.hasChildNodes() ? new Capability(
					Capability.CAPABILITY_READ, xml_element.getFirstChild().getNodeValue()) : null;

			xml_capability = xml.getElementsByTagNameNS(namespace, "write");
			xml_element = (Element) xml_capability.item(0);
			capabilities[CAPABILITY_WRITE] = xml_element.hasChildNodes() ? new Capability(
					Capability.CAPABILITY_WRITE, xml_element.getFirstChild().getNodeValue()) : null;

			xml_capability = xml.getElementsByTagNameNS(namespace, "manage");
			xml_element = (Element) xml_capability.item(0);
			capabilities[CAPABILITY_MANAGE] = xml_element.hasChildNodes() ? new Capability(
					Capability.CAPABILITY_MANAGE, xml_element.getFirstChild().getNodeValue()) : null;

			return capabilities;
		} catch (Exception ex) {
			log.severe("error deserializing capabilities : " + ex);
			throw (new DeserializeException(ex.getMessage()));
		}
	}
	
	/**
	 * @return "String" value of this capability
	 */
	public String json() {
		return uri.toString();
	}

	public static Capability[] json(JsonObject json_mapping) throws DeserializeException
	{
		Capability[] capabilities = new Capability[3];
		try {
			String read = json_mapping.getString("read", "");
			capabilities[CAPABILITY_READ] = read.compareTo("") != 0 ? new Capability(
					Capability.CAPABILITY_READ, read) : null;

			String write = json_mapping.getString("write", "");
			capabilities[CAPABILITY_WRITE] = write.compareTo("") != 0 ? new Capability(
					Capability.CAPABILITY_WRITE, write) : null;

			String manage = json_mapping.getString("manage", "");
			capabilities[CAPABILITY_MANAGE] = manage.compareTo("") != 0 ? new Capability(
					Capability.CAPABILITY_MANAGE, manage) : null;

			return capabilities;
		} catch (Exception ex) {
			log.severe("error deserializing capabilities : " + ex);
			throw (new DeserializeException(ex.getMessage()));
		}
	}
	
	public String toString()
	{
		return (uri.toString());
	}
}
