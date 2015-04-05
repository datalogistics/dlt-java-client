package edu.crest.dlt.utils;

public class DoubleHelper
{
	public static String decimals_2( double d )
  {
      // get integer part
      long l = (long) d;
      long l2 = (long) (Math.round( (d - l) * 100 ));
      return l + "." + l2;
  }
}
