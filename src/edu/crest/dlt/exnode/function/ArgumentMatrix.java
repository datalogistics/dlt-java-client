/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.crest.dlt.exnode.function;

import java.util.Vector;

/**
 * @author millar
 */
public abstract class ArgumentMatrix extends Argument
{
	Vector<Vector<Argument>> value = null;

	public ArgumentMatrix(String name)
	{
		this.name = name;
		this.value = new Vector<Vector<Argument>>();
	}

	public void insert(int i, int j, Argument arg)
	{
		Vector<Vector<Argument>> rows = value;

		if (i > rows.size()) {
			rows.setSize(i);
		}

		if (rows.elementAt(i) == null) {
			rows.add(i, new Vector<Argument>());
		}

		Vector<Argument> v = (Vector<Argument>) rows.elementAt(i);

		if (j > v.size()) {
			v.setSize(j);
		}

		v.add(j, arg);
	}

	public Argument getElement(int i, int j) throws ArrayIndexOutOfBoundsException
	{
		return value.elementAt(i).elementAt(j);
	}
}
