/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

/**
 * @author millar
 *
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ArgumentIntegerVector extends ArgumentVector
{
	public ArgumentIntegerVector(String name)
	{
		super(name);
	}

	public void insertInteger(int i, ArgumentInteger arg)
	{
		insert(i, arg);
	}

	public Integer getInteger(int i)
	{
		return (getElement(i).getInteger());
	}

	public String xml()
	{
		StringBuffer xml = new StringBuffer();

		xml.append("<exnode:argument name=\"" + name + "\" type=\"meta\">\n");

		Argument arg;
		for (int i = 0; i < value.size(); i++) {
			try {
				arg = getElement(i);
				xml.append(arg.xml());
			} catch (ArrayIndexOutOfBoundsException e) {
				xml.append(new ArgumentInteger("", 0).xml());
			}
		}
		xml.append("</exnode:argument\n");

		return (xml.toString());
	}
}
