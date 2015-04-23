/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/* $Id: AesEncryptFunction.java,v 1.4 2008/05/24 22:25:51 linuxguy79 Exp $ */

package edu.crest.dlt.exnode.function;

import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.crest.dlt.exception.AesEncryptException;

public class FunctionAesEncrypt extends Function
{
	private static final Logger log = Logger.getLogger(FunctionAesEncrypt.class.getName());

	private Cipher cipher;
	private String key;

	private int mode = Cipher.DECRYPT_MODE; // by default

	public FunctionAesEncrypt()
	{
		super("aes_encrypt");
	}

	public String key_gen() throws Exception
	{
		mode = Cipher.ENCRYPT_MODE;

		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128);
			SecretKey skey = kgen.generateKey();
			byte[] rawkey = skey.getEncoded();
			key(rawkey);
			key = encode(rawkey); // transform byte array in string

			return key;
		} catch (java.security.NoSuchAlgorithmException e) {
			log.severe("AES10:" + e);
			// throw(new AesEncryptException(e.getMessage()));
		} catch (Exception e) {
			throw (new AesEncryptException(e.getMessage()));
		}
		return null;
	}

	public void key(byte[] rawkey) throws Exception
	{
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(rawkey, "AES");
			cipher = Cipher.getInstance("AES");
			cipher.init(mode, skeySpec);
		} catch (java.security.InvalidKeyException e) {
			log.severe("AES30:" + e);
			throw (new AesEncryptException(e.getMessage()));
		} catch (java.security.NoSuchAlgorithmException e) {
			log.severe("AES31:" + e);
			throw (new AesEncryptException("AES encryption : " + e.getMessage()));
		} catch (javax.crypto.NoSuchPaddingException e) {
			log.severe("AES32:" + e);
			throw (new AesEncryptException(e.getMessage()));
		} catch (java.lang.SecurityException e) {
			log.severe("AES33:" + e);
			throw (new AesEncryptException("AES encryption : " + e.getMessage()));
		}
	}

	public void key(String strkey) throws Exception
	{
		byte[] rawkey = decode(strkey);
		key(rawkey);
	}

	public byte[] execute(byte[] rawBuf) throws Exception
	{
		try {
			rawBuf = cipher.doFinal(rawBuf);
		} catch (javax.crypto.BadPaddingException e) {
			log.severe("AES40:" + e);
			throw (new AesEncryptException(e.getMessage()));
		} catch (IllegalBlockSizeException e) {
			log.severe("AES41:" + e);
			throw (new AesEncryptException(e.getMessage()));
		}

		return (rawBuf);
	}
}
