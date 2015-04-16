/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.metadata;

import java.util.Iterator;

import javax.json.JsonObject;

import org.w3c.dom.Element;

import edu.crest.dlt.exception.DeserializeException;

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Metadata
{
	Object value;
	public String name;

	public Long getInteger() throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public Double getDouble() throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public String getString() throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public Iterator<Metadata> getChildren() throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public Metadata getChild(String name) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public void add(Metadata child) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public void remove(String name) throws UnsupportedOperationException
	{
		throw (new UnsupportedOperationException());
	}

	public String toString()
	{
		return (new String("Metadata(" + name + ")"));
	}

	public abstract String xml();

	public static Metadata xml(Element e) throws DeserializeException
	{
		Metadata md = null;

		String type = e.getAttribute("type");

		if (type.compareToIgnoreCase("integer") == 0) {
			md = MetadataInteger.xml(e);
		} else if (type.compareToIgnoreCase("double") == 0) {
			md = MetadataDouble.xml(e);
		} else if (type.compareToIgnoreCase("string") == 0) {
			md = MetadataString.xml(e);
		} else if (type.compareToIgnoreCase("meta") == 0) {
			md = MetadataList.xml(e);
		} else {
			throw (new DeserializeException("Unknown metadata type"));
		}

		return (md);
	}

	public static Metadata json(JsonObject obj, String hint)
	{
		Metadata md = null;

		if (hint.compareTo("name") == 0) {
			String name = obj.getString("name");
			md = new MetadataString("filename", name);

		} else if (hint.compareTo("offset") == 0) {
			long offset = obj.getJsonNumber("offset").longValue();
			md = new MetadataInteger("exnode_offset", offset);

		} else if (hint.compareTo("size") == 0) {
			long size = obj.getJsonNumber("size").longValue();
			md = new MetadataInteger("logical_length", size);

		} else if (hint.compareTo("alloc_length") == 0) {
			long size = obj.getJsonNumber("size").longValue();
			md = new MetadataInteger("alloc_length", size);
		}
		return md;
	}
	
	public static Metadata json(JsonObject json, String key, Class type) {
		if (json != null && key != null && type != null) {
			if (type.equals(MetadataString.class)) {
				return new MetadataString(key, json.getString(key));
			} else if (type.equals(MetadataInteger.class)) {
				return new MetadataInteger(key, json.getJsonNumber(key).longValue());		
			}
		}
		return null;
	}
}
