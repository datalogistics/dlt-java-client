/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Jan 28, 2004
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
public class WriteException extends Exception
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8052734889325874888L;

	public WriteException()
    {
        super();
    }

    public WriteException( String msg )
    {
        super( msg );
    }
}