package ciir.umass.edu.retrieval.utils;

import java.text.DecimalFormat;

public class StringUtils {
	public static String pad(String str, String padChar, int targetLen, boolean beginning)
	{
		String output = str;
		while(output.length() < targetLen)
		{
			if(beginning)
				output = padChar + output;
			else //end
				output += padChar;
		}
		return output;
	}
	public static String format(double d, int precision)
	{
		String zero = "";
		for(int i=0;i<precision;i++)
			zero += "0";
		
		String myformat = "0";
		if(precision > 0)
			myformat += "." + zero;
		
		DecimalFormat df = new DecimalFormat(myformat);	
		return df.format(d);
	}
	public static String quote(String str)
	{
		String[] s = str.trim().split(" ");
		String q = "";
		for(int i=0;i<s.length;i++)
		{
			q += "\"" + s[i] + "\" ";
		}
		return q.trim();
	}
}
