/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/* $Id: XorEncryptFunction.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.exnode.function;

import java.util.logging.Logger;

public class FunctionXorEncrypt extends Function
{
	private static final Logger log = Logger.getLogger(FunctionXorEncrypt.class.getName());

	private String key = null;

	public FunctionXorEncrypt()
	{
		super("xor_encrypt");
	}

	public void key(String key)
	{
		this.key = key;
		log.info("key : " + key);
	}

	public String key()
	{ // FIX: key length string must equal 7
		// for compatibility w/ LoRS tools.
		key = "1234567"; // TODO
		return key;
	}

	/*
	 * XOR encrypt function
	 * 
	 * input: rawbuf contains the clear/crypted data output: rawbuf contains the
	 * crypted/clear data (No memory allocation is done)
	 */

	public byte[] execute(byte[] rawBuf)
	{
		if (key == null) { // TODO
			log.severe("a key must be set.");
			return null;
		}

		byte[] bk = key.getBytes();
		int keylen = bk.length;
		int rawlen = rawBuf.length;

		for (int b = 0; b < rawlen; b = b + keylen) {
			for (int k = 0; k < keylen && ((b + k) < rawlen); k++) {
				int p = (b + k) % 2; // alternatively 0 or 1
				rawBuf[b + k] ^= (p == 0) ? bk[6] : 0;
			}
		}
		return (rawBuf);
	}
}
