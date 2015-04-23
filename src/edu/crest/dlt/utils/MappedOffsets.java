/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.utils;

public class MappedOffsets implements Comparable<MappedOffsets>
{
	public long offset_start = -1;
	public long offset_end = -1;

	public MappedOffsets(long offset_start, long length)
	{
		this.offset_start = offset_start;
		this.offset_end = offset_start + length - 1;
	}

	public int length()
	{
		return (int) (offset_end - offset_start + 1);
	}

	@Override
	public int compareTo(MappedOffsets o)
	{
		return offset_start < o.offset_start ? -1 : (offset_start == o.offset_start ? 0 : 1);
	}

	@Override
	public boolean equals(Object o)
	{
		return (o instanceof MappedOffsets) && (0 == compareTo((MappedOffsets) o));
	}
	
	@Override
	public int hashCode()
	{
		return (int) (offset_start % Integer.MAX_VALUE);
	}
}
