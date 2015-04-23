/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exception;

public class CreateResourceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4549009488365661893L;
	
	String url;
	
	public CreateResourceException(String url) {
		// TODO Auto-generated constructor stub
		this.url = url;
	}
	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return url;
	}
	
	
}
