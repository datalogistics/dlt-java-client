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

/**
 * @author millar
 */
public abstract class ArgumentVector extends Argument
{
	Vector<Argument> value = null;

	public ArgumentVector(String name)
	{
		this.name = name;
		this.value = new Vector<Argument>();
	}

	public void insert(int i, Argument arg)
	{
		Vector<Argument> v = value;

		if (i > v.size()) {
			v.setSize(i);
		}

		v.add(i, arg);
	}

	public Argument getElement(int i) throws ArrayIndexOutOfBoundsException
	{
		return value.elementAt(i);
	}
}
