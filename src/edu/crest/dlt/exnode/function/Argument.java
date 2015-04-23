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

import edu.crest.dlt.exception.DeserializeException;

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class Argument
{
	public static Logger log = Logger.getLogger(Argument.class.getName());

	public String name;

	public Function getFunction() throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Integer getInteger() throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Integer getInteger(int i) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Integer getInteger(int i, int j) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Double getDouble() throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Double getDouble(int i) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public Double getDouble(int i, int j) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public String getString() throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public String getString(int i) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public String getString(int i, int j) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public void insertInteger(int i, ArgumentInteger arg) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public void insertInteger(int i, int j, ArgumentInteger arg) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public void insertDouble(int i, ArgumentDouble arg) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public void insertDouble(int i, int j, ArgumentDouble arg) throws UnsupportedOperationException	{
		throw (new UnsupportedOperationException());
	}

	public void insertString(int i, ArgumentString arg) throws UnsupportedOperationException {
		throw (new UnsupportedOperationException());
	}

	public void insertString(int i, int j, ArgumentString arg) throws UnsupportedOperationException	{
		throw (new UnsupportedOperationException());
	}

	public abstract String xml();

	public static Argument xml(Element xml) throws DeserializeException
	{
		Argument argument = null;
		String type = xml.getAttribute("type");

		if (type.compareToIgnoreCase("integer") == 0) {
			argument = ArgumentInteger.xml(xml);
		} else if (type.compareToIgnoreCase("double") == 0) {
			argument = ArgumentDouble.xml(xml);
		} else if (type.compareToIgnoreCase("string") == 0) {
			argument = ArgumentString.xml(xml);
		} else {
			throw (new DeserializeException("unknown metadata type"));
		}

		return (argument);
	}
}
