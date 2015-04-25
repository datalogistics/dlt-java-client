/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Jan 28, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

public class FunctionIdentity extends Function
{
	public FunctionIdentity()
	{
		super("identity");
	}

	public void key(String key)
	{
	}

	public byte[] execute(byte[] rawBuf)
	{
		return (rawBuf);
	}
}
