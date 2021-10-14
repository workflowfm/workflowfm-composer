package com.workflowfm.composer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Simple debug message logging class. */
public class Log
{
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	// Debug messages
	public static void d(String m)
	{
		String prefix;
		try {
			String cls = Thread.currentThread().getStackTrace()[2].getClassName();
			if (cls.lastIndexOf('.') > -1)
				prefix = cls.substring(cls.lastIndexOf('.')+1);
			else
				prefix = cls;
		} catch (Exception e) {
			prefix = "";
		}
		System.err.println(getDate() + "[" + prefix + "] " + m);
	}

	public static <T> void d(String m, T[] array)
	{
		d(m);

		System.err.print("[");
		String prefix = "";
		for (T x : array)
		{
			System.err.print(prefix + x);
			prefix = ", ";
		}
		System.err.print("]");

		System.err.println();
	}

	// Error messages
	public static void e(String m)
	{
		System.out.println(getDate() + m);
	}

	// Warning messages
	public static void w(String m)
	{
		System.out.println(getDate() + m);
	}
	
	private static String getDate() {
		return "[" + dateFormat.format(Calendar.getInstance().getTime()) + "] ";
	}

}
