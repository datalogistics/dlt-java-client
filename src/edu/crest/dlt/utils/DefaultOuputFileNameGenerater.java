/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.utils;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import edu.crest.dlt.exnode.Exnode;
import edu.crest.dlt.exnode.metadata.MetadataString;

public class DefaultOuputFileNameGenerater
{

	static Logger LOG = Logger.getLogger(DefaultOuputFileNameGenerater.class.getName());

	static public String getDefaultOutputFile(Exnode exnode)
	{
		return getDefaultOutputFile(exnode, "");
	}

	static public String getDefaultOutputFile(Exnode exnode, String defaultName)
	{
		String out = null;
		MetadataString filenameMetadata = (MetadataString) exnode.get("filename");
		String filename;
		try {
			filename = filenameMetadata.getString();
		} catch (NullPointerException e) {
			filename = defaultName;
		}
		String homeDirectory = getHomeDirectory();
		String separator = File.separator;
		out = generateUniqeName(new String(homeDirectory + separator + filename));

		LOG.info("Default file Name : " + out);
		return out;
	}

	static public String getOuputFileName(Exnode exnode, String path)
	{
		String filename = exnode.filename();

		if (path.charAt(path.length() - 1) == File.separatorChar) {
			return generateUniqeName(path + filename);
		} else {
			return generateUniqeName(path + File.separator + filename);
		}
	}

	static public String generateUniqeName(String fileName)
	{
		File f = new File(fileName);
		int counter = 1;
		String out = fileName;

		while (f.exists()) {
			String fullpath = FilenameUtils.getFullPath(fileName);
			String fileExtension = getExtension(fileName);
			String conflictFileName = fileName.substring(fullpath.length(), fileName.length()
					- fileExtension.length());
			out = fullpath + conflictFileName + "(" + counter + ")" + fileExtension;
			f = new File(out);
			counter++;
		}

		LOG.info("Unique file Name : " + out);
		return out;
	}

	static public String getExtension(String fileName)
	{
		if (fileName.contains(".tar.gz")) {
			return ".tar.gz";
		}

		String ext = FilenameUtils.getExtension(fileName);
		if (ext.isEmpty()) {
			return ext;
		} else {
			return "." + ext;
		}

	}

	public static String getHomeDirectory()
	{
		String out = null;
		String osName = System.getProperty("os.name");
		String windowsRegex = "^[Ww]indows..*$";
		String macRegex = "^[Mm]ac..*$";
		if (osName.matches(windowsRegex))
			out = new String(System.getProperty("user.home") + File.separator + "Desktop");
		else if (osName.matches(macRegex))
			out = new String(System.getProperty("user.home") + File.separator + "Downloads");
		else
			// We assume anything else is some sort of Linux/Unix
			out = System.getProperty("user.home");
		return out;
	}

	public static String cleanName(String name)
	{
		final char[] chars = name.toCharArray();
		for (int x = 0; x < chars.length; x++) {
			final char c = chars[x];
			if (((c >= 'a') && (c <= 'z')))
				continue; // a - z
			if (((c >= 'A') && (c <= 'Z')))
				continue; // A - Z
			if (((c >= '0') && (c <= '9')))
				continue; // 0 - 9
			if (c == '-')
				continue; // hyphen
			if (c == '.')
				continue; // dot
			chars[x] = '_'; // if not replaced by underscore
		}
		return String.valueOf(chars);
	}

}
