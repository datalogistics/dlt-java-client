/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ArgumentString extends Argument
{
	private static final Logger log = Logger.getLogger(ArgumentString.class.getName());

	private String value = null;

	public ArgumentString(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getString()
	{
		return value;
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:argument name=\"" + name + "\" type=\"string\">");
		xml.append(value);
		xml.append("</exnode:argument>\n");

		return (xml.toString());
	}

	public static Argument xml(Element xml)
	{
		String name = xml.getAttribute("name");
		Node xml_child = xml.getFirstChild();
		String value = xml_child.getNodeValue();

		return (new ArgumentString(name, value));
	}
}
