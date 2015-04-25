/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * Created on Nov 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exception;

public class IBPException extends Exception
{
	private static final long serialVersionUID = -3971262281686262779L;
	private static String[] errorMessages = { "", "Error reading socket", "Error writing socket",
			"Capability not found", "Not read capability", "Not write capability",
			"Not manage capability", "Invalid write capability", "Invalid read capability",
			"Invalid manage capability", "Bad capability format", "Access denied", "Unable to connect",
			"Error opening file", "Error reading file", "Error writing file", "Error accessing file",
			"File seek error", "Limit would be exceeded", "Data would be damaged", "Bad format",
			"Type not supported", "Resource unavailable", "Internal error", "Invalid command",
			"Operation would block", "Protocol version mismatch", "Duration too long",
			"Incorrect password", "Invalid parameter", "", "", "", "", "", "", "", "Allocation failed",
			"", "", "", "Client timed out", "Unknown function", "", "", "Server timed out", "", "", "" };

	public IBPException(int error)
	{
		super(errorMessages[(-error) - 1]);
	}
}
