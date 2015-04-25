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

import edu.crest.dlt.exception.UnknownFunctionException;

public class FunctionFactory
{
	public FunctionFactory()
	{
	}

	public static Function function(String name) throws UnknownFunctionException
	{
		if (name.compareToIgnoreCase("checksum") == 0) {
			return (new FunctionChecksum());
		} else if (name.compareToIgnoreCase("xor_encrypt") == 0) {
			return (new FunctionXorEncrypt());
		} else if (name.compareToIgnoreCase("des_encrypt") == 0) {
			return (new FunctionDesEncrypt());
		} else if (name.compareToIgnoreCase("aes_encrypt") == 0) {
			return (new FunctionAesEncrypt());
		} else if (name.compareToIgnoreCase("checksum") == 0) {
			return (new FunctionChecksum());
		} else if (name.compareToIgnoreCase("zlib_compress") == 0) {
			return (new FunctionCompress());
		} else if (name.compareToIgnoreCase("identity") == 0) {
			return (new FunctionIdentity());
		} else {
			throw (new UnknownFunctionException("Unknown function " + name));
		}
	}
}
