package edu.crest.dlt.exception;

public class AesEncryptException extends Exception
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AesEncryptException()
    {
        super();
    }

    public AesEncryptException( String msg )
    {
        super( msg );
    }
}