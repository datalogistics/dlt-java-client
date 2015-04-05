/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

import java.util.Vector;

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ArgumentDoubleVector extends ArgumentVector
{
	public ArgumentDoubleVector(String name)
	{
		super(name);
	}

	public void insertDouble(int i, ArgumentDouble arg)
	{
		insert(i, arg);
	}

	public Double getDouble(int i)
	{
		return (getElement(i).getDouble());
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:argument name=\"" + name + "\" type=\"meta\">\n");

		Vector<Argument> v = (Vector<Argument>) value;
		Argument arg;
		for (int i = 0; i < v.size(); i++) {
			try {
				arg = getElement(i);
				xml.append(arg.xml());
			} catch (ArrayIndexOutOfBoundsException e) {
				xml.append(new ArgumentDouble("", 0.0).xml());
			}
		}
		xml.append("</exnode:argument>\n");

		return (xml.toString());
	}
}
