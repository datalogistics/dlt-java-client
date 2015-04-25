/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exnode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonReader;
import javax.naming.InvalidNameException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.exception.SerializeException;

public class ExnodeBuilder
{
	/* exnode_file.xnd : file containing XML encoded exnode */
	public static Exnode xnd(String filename_xnd) throws DeserializeException, InvalidNameException,
			ParserConfigurationException, SAXException, IOException
	{
		File file_xnd = new File(filename_xnd, "r");
		FileNameExtensionFilter file_filter = new FileNameExtensionFilter(
				"file containing XML encoded exnode", "xnd");
		if (file_filter.accept(file_xnd)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document xnd = db.parse(file_xnd);
			return Exnode.xml(xnd.getDocumentElement());
		} else {
			throw new InvalidNameException("Valid exnode file extension \".xnd\" not found.");
		}
	}

	public static Exnode xnd(URL url_xnd) throws DeserializeException, ParserConfigurationException,
			SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document xnd = db.parse(url_xnd.openStream());
		return Exnode.xml(xnd.getDocumentElement());
	}

	public static void xnd(Exnode exnode, String filename_xnd) throws IOException, SerializeException
	{
		FileOutputStream file_out_stream = new FileOutputStream(filename_xnd);
		try {
			file_out_stream.write(exnode.xml().getBytes());
		} finally {
			file_out_stream.close();
		}
	}

	/* exnode_file.uef : file containing JSON encoded exnode */
	public static Exnode uef(String filename_uef) throws DeserializeException, InvalidNameException,
			FileNotFoundException
	{
		File file_uef = new File(filename_uef, "r");
		FileNameExtensionFilter file_filter = new FileNameExtensionFilter(
				"file containing JSON encoded exnode", "uef");
		if (file_filter.accept(file_uef)) {
			JsonReader json_reader = Json.createReader(new FileReader(filename_uef));
			return Exnode.json(json_reader.readObject());
		} else {
			throw new InvalidNameException("Valid exnode file extension \".uef\" not found.");
		}
	}

	public static Exnode uef(URL url_uef) throws DeserializeException, IOException
	{
		JsonReader json_reader = Json.createReader(url_uef.openStream());
		if (url_uef.getQuery() == null || url_uef.getQuery().trim().length() == 0
				|| !url_uef.getQuery().startsWith("id")) {
			return Exnode.json(json_reader.readObject());
		} else {
			return Exnode.json(json_reader.readArray().getJsonObject(0));
		}
	}

	public static void uef(Exnode exnode, String filename_uef) throws IOException, SerializeException
	{
		FileOutputStream file_out_stream = new FileOutputStream(filename_uef);
		try {
			file_out_stream.write(exnode.json().toString().getBytes());
		} finally {
			file_out_stream.close();
		}
	}
}
