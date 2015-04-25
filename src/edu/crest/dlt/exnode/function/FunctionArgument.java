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

public class FunctionArgument extends Argument
{
	public Function value = null;

	public FunctionArgument(String name, Function f)
	{
		this.name = name;
		this.value = f;
	}

	public String xml()
	{
		return value.xml();
	}
}
