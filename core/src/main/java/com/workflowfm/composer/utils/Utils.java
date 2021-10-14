package com.workflowfm.composer.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringEscapeUtils;

import com.workflowfm.composer.utils.CustomGson;

/** Miscellaneous utility functions. */
public class Utils
{
	public static String[] objectArrayToStringArray(Object[] a)
	{
		String[] s = new String[a.length];

		for (int i = 0; i < a.length; ++i)
			s[i] = (String) a[i];

		return s;
	}

	public static <T> T[] concatArrays(T[] x, T[] y)
	{
		T[] a = Arrays.copyOf(x, x.length + y.length);
		System.arraycopy(y, 0, a, x.length, y.length);
		return a;
	}

	// Compares strings that could be null
	public static boolean stringsEqual(String x, String y)
	{
		return x == null ? y == null : x.equals(y);
	}
	
	public static String repeatString(String s, int n)
	{		
		StringBuilder str = new StringBuilder(n * s.length());
		for (int i = 0; i < n; i += 1) {
		    str.append(s);
		}
		return str.toString();
	}

	public static String trimQuotes(String s)
	{
		return s.substring(1, s.length() - 1);
	}

	public static boolean isEmpty(String s)
	{
		return s.trim().equals("");
	}

	public static void writeStringToFile(File file, String s) throws IOException
	{
		Writer writer = null;
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(s);
		} finally
		{
			if (writer != null)
				writer.close();
		}
	}

	public static <T> T[] removeDuplicates(T[] couldContainDuplicates)
	{
		Set<T> set = new HashSet<T>();
		set.addAll(Arrays.asList(couldContainDuplicates));

		@SuppressWarnings("unchecked")
		T[] noDuplicates = (T[]) Array.newInstance(couldContainDuplicates.getClass().getComponentType(), set.size());
		int count = 0;
		for (T e : set)
			noDuplicates[count++] = e;

		return noDuplicates;
	}

	public static void scrollToBottomOfTextArea(JTextComponent textComponent)
	{
		int length = textComponent.getDocument().getLength();
		textComponent.select(length, length);
	}
	
	public static String getJsonString ( Object obj ) {
		return getJsonString(obj,true);
	}
	
	public static String getJsonString ( Object obj, boolean escape ) {
		if (escape)
			return StringEscapeUtils.escapeJava(CustomGson.getGson().toJson(obj));
		else
			return CustomGson.getGson().toJson(obj);
	}
	
	public static String stringOf (Collection<?> objs, String delimiter) {
		if (objs.size() == 0) return "";
		
		StringBuffer buf = new StringBuffer();
		for (Iterator<?> it = objs.iterator(); it.hasNext();) {
			buf.append(it.next().toString());
			if (it.hasNext()) buf.append(delimiter);
		}
		return buf.toString();
	}
}
