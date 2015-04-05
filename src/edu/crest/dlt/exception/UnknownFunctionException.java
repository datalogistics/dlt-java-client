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
public class UnknownFunctionException extends Exception
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4865547364510657680L;

	public UnknownFunctionException()
    {
        super();
    }

    public UnknownFunctionException( String msg )
    {
        super( msg );
    }
}