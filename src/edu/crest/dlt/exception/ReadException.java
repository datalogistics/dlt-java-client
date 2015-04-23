/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exception;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ReadException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2736350509022053900L;

	public ReadException()
    {
        super();
    }

    public ReadException( String msg )
    {
        super( msg );
    }
}