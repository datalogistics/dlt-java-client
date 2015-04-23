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

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MetadataString extends Metadata
{
	public MetadataString(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getString()
	{
		return ((String) value);
	}

	public String toString()
	{
		return (new String("Metadata(" + name + ",STRING," + (String) value + ")"));
	}

	public String xml()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("<exnode:metadata name=\"" + name + "\" type=\"string\">");
		sb.append((String) value);
		sb.append("</exnode:metadata>\n");

		return (sb.toString());
	}

	public static Metadata xml(Element e)
	{
		String name = e.getAttribute("name");
		Node child = e.getFirstChild();
		String value = child.getNodeValue();

		return (new MetadataString(name, value));
	}
}
