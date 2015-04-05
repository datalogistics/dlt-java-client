/*
 * Created on Jan 28, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class FunctionIdentity extends Function
{
    public FunctionIdentity()
    {
        super( "identity" );
    }

    public void key( String key )
    {
    }

    public byte[] execute( byte[] rawBuf )
    {
        return (rawBuf);
    }
}