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

/**
 * @author millar
 */
public class ArgumentStringMatrix extends ArgumentMatrix
{
	public ArgumentStringMatrix(String name)
	{
		super(name);
	}

	public void insertString(int i, int j, ArgumentString arg)
	{
		insert(i, j, arg);
	}

	public String getString(int i, int j)
	{
		return (getElement(i, j).getString());
	}

	public String xml()
	{
		return (new String());
	}
}
