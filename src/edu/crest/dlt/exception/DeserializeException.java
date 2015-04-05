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
public class DeserializeException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2596497526898093686L;

	public DeserializeException()
    {
        super();
    }

    public DeserializeException( String msg )
    {
        super( msg );
    }
}