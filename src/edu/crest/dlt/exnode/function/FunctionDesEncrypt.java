/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exnode.function;

import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class FunctionDesEncrypt extends Function
{
	private static final Logger log = Logger.getLogger(FunctionDesEncrypt.class.getName());

	private Cipher cipher;
	private String key;

	private int mode = Cipher.DECRYPT_MODE; // by default

	public FunctionDesEncrypt()
	{
		super("des_encrypt");
	}

	public String key_gen() throws Exception
	{
		key = super.key_gen(); // TODO: Currently use the default key (provided
		// by class Function)
		mode = Cipher.ENCRYPT_MODE;
		key(key);

		return key;
	}

	public void key(String key)
	{
		PBEKeySpec pbeKeySpec;
		PBEParameterSpec pbeParamSpec;
		SecretKeyFactory keyFac;

		// Salt
		byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8,
				(byte) 0xee, (byte) 0x99 };

		// Iteration count
		int count = 20;

		this.key = key; // useless, the most important is to init cipher

		try {
			char[] charkey = key.toCharArray();

			// Create PBE parameter set
			pbeParamSpec = new PBEParameterSpec(salt, count);
			pbeKeySpec = new PBEKeySpec(charkey);
			keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

			// Create PBE Cipher
			cipher = Cipher.getInstance("PBEWithMD5AndDES");

			// Initialize PBE Cipher with key and parameters
			cipher.init(mode, pbeKey, pbeParamSpec);

		} catch (java.security.InvalidKeyException e) {
			log.severe("DES30:" + e);
		} catch (java.security.NoSuchAlgorithmException e) {
			log.severe("DES31:" + e);
		} catch (java.security.spec.InvalidKeySpecException e) {
			log.severe("DES32:" + e);
		} catch (java.security.InvalidAlgorithmParameterException e) {
			log.severe("DES33:" + e);
		} catch (javax.crypto.NoSuchPaddingException e) {
			log.severe("DES34:" + e);
		}

	}

	public byte[] execute(byte[] rawBuf) throws Exception
	{

		try {
			rawBuf = cipher.doFinal(rawBuf);
		} catch (javax.crypto.BadPaddingException e) {
			log.severe("DES40:" + e);
		} catch (IllegalBlockSizeException e) {
			log.severe("DES41:" + e);
		}

		return (rawBuf);
	}
}
