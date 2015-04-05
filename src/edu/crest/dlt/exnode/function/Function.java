/* $Id: Function.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.crest.dlt.exnode.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.crest.dlt.exception.DeserializeException;
import edu.crest.dlt.exception.UnknownFunctionException;
import edu.crest.dlt.exnode.metadata.Metadata;
import edu.crest.dlt.exnode.metadata.MetadataContainer;
import edu.crest.dlt.utils.Configuration;

public abstract class Function extends MetadataContainer
{
	private static Logger log = Logger.getLogger(Function.class.getName());

	Map<String, Argument> arguments;
	public String name;

	public abstract void key(String key) throws Exception;

	public abstract byte[] execute(byte[] rawBuf) throws Exception;

	public Function(String name)
	{
		this.name = name;
		this.arguments = new HashMap<String, Argument>();
	}

	public void add(Argument arg)
	{
		arguments.put(arg.name, arg);
	}

	public Iterator<Argument> arguments()
	{
		Set<String> keySet = arguments.keySet();
		List<Argument> list = new ArrayList<Argument>();
		Iterator<String> i = keySet.iterator();
		String name;
		while (i.hasNext()) {
			name = (String) i.next();
			list.add(arguments.get(name));
		}
		return (list.iterator());
	}

	public Argument argument(String name)
	{
		return ((Argument) arguments.get(name));
	}

	// generate a random key in a String
	// New functions can overload this method to generate a more adapted key
	public String key_gen() throws Exception
	{
		return "ThIsIsAdEfauLtKeY31415"; // TODO
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:function name=\"" + name + "\">\n");

		Iterator<?> i = iterator();
		while (i.hasNext()) {
			Metadata metadata = (Metadata) i.next();
			xml.append(metadata.xml());
		}

		i = arguments();
		while (i.hasNext()) {
			Argument argument = (Argument) i.next();
			xml.append(argument.xml());
		}

		xml.append("</exnode:function>\n");

		return (xml.toString());
	}

	public static Function xml(Element xml) throws DeserializeException
	{
		String namespace = Configuration.dlt_exnode_namespace;
		Function function;
		try {
			function = FunctionFactory.function(xml.getAttribute("name"));
		} catch (UnknownFunctionException ufe) {
			throw (new DeserializeException(ufe.getMessage()));
		}

		NodeList xml_metadata = xml.getElementsByTagNameNS(namespace, "metadata");
		Metadata metadata;
		for (int i = 0; i < xml_metadata.getLength(); i++) {
			Element element = (Element) xml_metadata.item(i);
			if (element.getParentNode().equals(xml)) {
				metadata = Metadata.xml(element);
				function.add(metadata);
			}
		}

		NodeList xml_subfunctions = xml.getElementsByTagNameNS(namespace, "function");
		Function subfunction;
		for (int i = 0; i < xml_subfunctions.getLength(); i++) {
			Element element = (Element) xml_subfunctions.item(i);
			if (element.getParentNode().equals(xml)) {
				subfunction = Function.xml(element);
				function.add(new FunctionArgument(subfunction.name, subfunction));
			}
		}

		NodeList xml_arguments = xml.getElementsByTagNameNS(namespace, "argument");
		Argument argument;
		for (int i = 0; i < xml_arguments.getLength(); i++) {
			Element element = (Element) xml_arguments.item(i);
			if (element.getParentNode().equals(xml)) {
				argument = Argument.xml(element);
				function.add(argument);
			}
		}

		return (function);
	}

	/* ================== USEFUL METHODS ================== */

	/*
	 * The methods below are common methods for a lot of function
	 */

	// http://www.nsftools.com/tips/JavaTips.htm#sunbase64
	public static String encode(byte[] raw)
	{
		return Base64.encodeBase64String(raw);
	}

	public static byte[] decode(String encodedStr)
	{
		return Base64.decodeBase64(encodedStr);
	}

	public static String hex(byte b)
	{
		char digit16[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { digit16[(b >> 4) & 0x0f], digit16[b & 0x0f] };
		return new String(array);
	}

	public static String hex(byte[] data)
	{
		StringBuffer buf = new StringBuffer();
		for (byte b : data) {
			buf.append(hex(b));
		}
		return (buf.toString());
	}
}
