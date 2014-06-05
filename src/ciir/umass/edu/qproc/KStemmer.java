package ciir.umass.edu.qproc;

import org.lemurproject.kstem.KrovetzStemmer;

public class KStemmer {
	private static KrovetzStemmer stemmer = new KrovetzStemmer();
	
	public String stem(String text)
	{
		String[] s = text.split("\\s+");
		String output = "";
		for(int i=0;i<s.length;i++)
			output += stemmer.stem(s[i]) + ((i<s.length-1)?" ":"");
		return output;
	} 
}
