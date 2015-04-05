/* $Id: CompressFunction.java,v 1.4 2008/05/24 22:25:51 linuxguy79 Exp $ */

package edu.crest.dlt.exnode.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FunctionCompress extends Function
{
	private static final Logger log = Logger.getLogger(FunctionCompress.class.getName());

	private static final int COMPRESS = 0;
	private static final int UNCOMPRESS = 1;

	private String key = null;

	private int mode = UNCOMPRESS; // by default

	public FunctionCompress()
	{
		super("zlib_compress");
	}

	public void key(String key)
	{
		this.key = key;
		log.info("GZIPFunction : key = " + key);
	}

	public String genkey()
	{
		mode = COMPRESS;
		key = "(useless)";
		return key;
	}

	// GZIPdata : return a byte array of compressed data in the GZIP
	// format.
	byte[] GZIPdata(byte[] bufin) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = new GZIPOutputStream(baos);

		gzos.write(bufin, 0, bufin.length);
		gzos.close();
		return (baos.toByteArray());
	}

	// GUNZIPdata :
	byte[] GUNZIPdata(byte[] bufin) throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bufin);
		GZIPInputStream gunzipis = new GZIPInputStream(bais);

		// Use blocksize field to store the original size
		// lfgs 20050304.1408: changed this to int and changed call to
		// .longValue to .intValue
		// Anyway, the old code was calling .getInteger() anyway so longValues
		// would've always failed anyway even in the old code (I think that's
		// probably why there was a !!! in the comments here.)
		int original_size = argument("blocksize").getInteger().intValue();

		byte[] bufout = new byte[original_size];

		log.info("CompressFunction10: bufout.length = " + bufout.length);
		int v = gunzipis.read(bufout, 0, bufout.length);
		gunzipis.close();
		log.info("CompressFunction12: v = " + v);
		return bufout;
	}

	public byte[] execute(byte[] rawBuf)
	{
		try {
			if (mode == COMPRESS) {
				log.info("CompressFunction: COMPRESS!");
				return GZIPdata(rawBuf);
			} else {
				log.info("CompressFunction: UNCOMPRESS!");
				return GUNZIPdata(rawBuf);
			}
		} catch (IOException e) {
			log.severe("CompressFunction: " + e);
		}
		return null;
	}
}
