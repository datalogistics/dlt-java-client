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
public class ArgumentDoubleMatrix extends ArgumentMatrix
{
	public ArgumentDoubleMatrix(String name)
	{
		super(name);
	}

	public void insertDouble(int i, int j, ArgumentDouble arg)
	{
		insert(i, j, arg);
	}

	public Double getDouble(int i, int j)
	{
		return (getElement(i, j).getDouble());
	}

	public String xml()
	{
		return (new String());
	}
}
