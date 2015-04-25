/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/* $Id: ChecksumException.java,v 1.4 2008/05/24 22:25:51 linuxguy79 Exp $ */

package edu.crest.dlt.exception;

public class ChecksumException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6251848394322272113L;

	public ChecksumException()
	{
		super();
	}

	public ChecksumException(String msg)
	{
		super(msg);
	}
}
