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

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ArgumentDouble extends Argument
{
	private static final Logger log = Logger.getLogger(ArgumentDouble.class.getName());

	private Double value = null;

	public ArgumentDouble(String name, Double value)
	{
		this.name = name;
		this.value = value;
	}

	public Double getDouble()
	{
		return value;
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:argument name=\"" + name + "\" type=\"double\">");
		xml.append(value);
		xml.append("</exnode:argument>\n");

		return (xml.toString());
	}

	public static Argument xml(Element xml)
	{
		String name = xml.getAttribute("name");
		Node xml_child = xml.getFirstChild();
		Double value = new Double(xml_child.getNodeValue());

		return (new ArgumentDouble(name, value));
	}
}
