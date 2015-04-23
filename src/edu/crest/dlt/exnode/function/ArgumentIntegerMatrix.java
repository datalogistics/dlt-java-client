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
public class ArgumentIntegerMatrix extends ArgumentMatrix
{
	public ArgumentIntegerMatrix(String name)
	{
		super(name);
	}

	public void insertInteger(int i, int j, ArgumentInteger arg)
	{
		insert(i, j, arg);
	}

	public Integer getInteger(int i, int j)
	{
		return (getElement(i, j).getInteger());
	}

	public String xml()
	{
		return (new String());
	}
}
