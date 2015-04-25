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

import java.util.Vector;

public class ArgumentStringVector extends ArgumentVector
{
	Vector<ArgumentString> value = null;

	public ArgumentStringVector(String name)
	{
		super(name);
	}

	public void insertString(int i, ArgumentString arg)
	{
		insert(i, arg);
	}

	public String getString(int i)
	{
		return (getElement(i).getString());
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:argument name=\"" + name + "\" type=\"meta\">\n");

		Argument argument;
		for (int i = 0; i < value.size(); i++) {
			try {
				argument = getElement(i);
				xml.append(argument.xml());
			} catch (ArrayIndexOutOfBoundsException e) {
				xml.append(new ArgumentString("", "").xml());
			}
		}
		xml.append("</exnode:argument\n");

		return (xml.toString());
	}
}
