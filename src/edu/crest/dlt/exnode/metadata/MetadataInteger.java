/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.metadata;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MetadataInteger extends Metadata
{
	public MetadataInteger(String name, Long value)
	{
		this.name = name;
		this.value = value;
	}

	public MetadataInteger(String name, long value)
	{
		this.name = name;
		this.value = new Long(value);
	}

	public Long getInteger()
	{
		return ((Long) value);
	}

	public String toString()
	{
		return (new String("Metadata(" + name + ",INTEGER," + (Integer) value + ")"));
	}

	public String xml()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("<exnode:metadata name=\"" + name + "\" type=\"integer\">");
		sb.append((Long) value);
		sb.append("</exnode:metadata>\n");

		return (sb.toString());
	}

	public static Metadata xml(Element e)
	{
		String name = e.getAttribute("name");
		Node child = e.getFirstChild();
		Long value = new Long(child.getNodeValue());

		return (new MetadataInteger(name, value));
	}
}
