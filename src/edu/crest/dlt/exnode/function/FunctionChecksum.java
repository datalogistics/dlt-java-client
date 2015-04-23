/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/* $Id: ChecksumFunction.java,v 1.4 2008/05/24 22:25:51 linuxguy79 Exp $ */

package edu.crest.dlt.exnode.function;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import edu.crest.dlt.exception.ChecksumException;

public class FunctionChecksum extends Function
{
	private static final Logger log = Logger.getLogger(FunctionChecksum.class.getName());

	private static final int CHECK_CS = 0; // CS stands for CheckSum
	private static final int SET_CS = 1;

	static final int CHECKSUM_SIZE = 16; // 128 bits
	static final int CHECKSUM_HEADER_SIZE = 2 * CHECKSUM_SIZE + 1; // = 33 Bytes
	static final int DEFAULT_BLOCKSIZE = 512 * 1024;
	private String key = null;

	private int mode = CHECK_CS; // by default

	public FunctionChecksum()
	{
		super("checksum");
	}

	public String key_gen()
	{
		mode = SET_CS;
		key = "(useless)";
		return key;
	}

	public void key(String key)
	{
	}

	public static byte[] keyed_digest(byte[] buffer)
	{
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(buffer);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			log.severe("ChecksumFunction (getKeyedDigest): " + e);
		}
		return null;
	}

	public byte[] execute(byte[] rawBuf) throws Exception
	{

		byte[] checksum;

		switch (mode) {

			case SET_CS: // set checksum [<-- checksum (32)-->:<-- rawBuf
							// (rawBuf.length) -->]

				checksum = keyed_digest(rawBuf);
				String checksumHexStr = hex(checksum) + ":";
				byte[] checksumHexbytes = checksumHexStr.getBytes();
				byte[] buf_w_cs = new byte[checksumHexbytes.length + rawBuf.length];
				System.arraycopy(checksumHexbytes, 0, buf_w_cs, 0, checksumHexbytes.length);
				System.arraycopy(rawBuf, 0, buf_w_cs, checksumHexbytes.length, rawBuf.length);

				return buf_w_cs; // rawBuf w/ checksum

			case CHECK_CS: // check checksum

				long blocksize = argument("blocksize").getInteger().longValue();

				log.info("### blocksize = " + blocksize);

				int numBlocks;
				if (rawBuf.length % blocksize == 0) {
					numBlocks = (int) (rawBuf.length / blocksize);
				} else {
					numBlocks = (int) (rawBuf.length / blocksize) + 1;
				}

				log.info("### numBlocks = " + numBlocks);

				byte[] buf_wo_cs = new byte[rawBuf.length - (numBlocks * CHECKSUM_HEADER_SIZE)];

				for (int nb = 0; nb < numBlocks; nb++) {

					log.info("### Processing block # nb = " + nb);

					checksum = new byte[CHECKSUM_SIZE];

					for (int i = 0; i < CHECKSUM_SIZE; i++) {
						String temp = new String(rawBuf, (int) ((2 * i) + (blocksize * nb)), 2, "US-ASCII");

						// System.err.print( " " + temp );
						Integer intValue = Integer.valueOf(temp, 16);
						checksum[i] = intValue.byteValue();
					}

					log.info(" DONE."); // CR+LF

					// extract rawBuf
					byte[] tmpbuf;
					long lastblocksize;
					if (nb < (numBlocks - 1)) {
						lastblocksize = blocksize - CHECKSUM_HEADER_SIZE;
					} else { // last block
						lastblocksize = rawBuf.length - (blocksize * nb) - CHECKSUM_HEADER_SIZE;
					}

					log.info("lastblocksize = " + lastblocksize);

					tmpbuf = new byte[(int) lastblocksize];
					System.arraycopy(rawBuf, (int) (blocksize * nb) + CHECKSUM_HEADER_SIZE, tmpbuf, 0, (int) lastblocksize);

					byte[] hash = keyed_digest(tmpbuf);

					if (MessageDigest.isEqual(checksum, hash)) {
						// if(true) {
						System.arraycopy(tmpbuf, 0, buf_wo_cs, (int) ((blocksize - CHECKSUM_HEADER_SIZE) * nb), (int) lastblocksize);

					} else {
						throw (new ChecksumException("Checksums differ!"));
					}
				}
				return (buf_wo_cs); // rawBuf w/o checksum
		}
		return null;
	}
}
