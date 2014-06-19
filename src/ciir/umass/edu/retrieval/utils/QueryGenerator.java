package ciir.umass.edu.retrieval.utils;

import org.lemurproject.galago.tupleflow.Parameters;

public class QueryGenerator {

	
	public static String generateQuery(String query, Parameters param) {
		if(param.getString("field").equals("all"))
			return generateQuery(query, param.get("queryType", "ql"));
		else
		{
			return generateQuery(query, param.get("queryType", "ql") , param.getString("field") ) ;
		}
		
		
	}
	

	private static String generateQuery(String query, String queryType)
	{
		
		query = removeStopWords(query, true);
		query = removeNonAlphaNumericCharacters(query);
		
		if(queryType.equals("SDM"))
			return "#sdm("+ query+")";
		
		if(queryType.equals("UnigramOWConjuctive"))
			return generateUnigramOWConjuctiveQuery(query);
		
		if(queryType.equals("UnigramOW"))
			return generateUnigramOWQuery(query);
		
		
		return query;
	}

	private static String generateQuery(String query, String queryType, String field)
	{
		
		query = removeStopWords(query, true);
		query = removeNonAlphaNumericCharacters(query);
		
		if(queryType.equals("SDM"))
			return generateSDMQuery(query, field);
		
		if(queryType.equals("UnigramOWConjuctive"))
			return generateUnigramOWConjuctiveQuery(query, field);
			
		
		if(queryType.equals("UnigramOW"))
			return generateUnigramOWQuery(query, field);
					
		
		return query;
	}
	

	public static String generateUnigramQuery(String q, String field)
    {

		String[] strs = q.split(" ");

		if(strs.length == 1)
			return "#combine(" + q + "." + field+ ")";
		
		
		String unigram = "";
		
		for(int i=0;i<strs.length;i++)
		{
			unigram += strs[i] + "." + field +" ";
		}

		


        return " #combine(" + unigram + ") ";
    }
	
	
	
	public static String generateUnigramOWQuery(String q)
    {

		String[] strs = q.split(" ");
        if(strs.length == 1)
            return "#combine(" + q + ")";
        String ow1 = "";
        String unigram = "";
        for(int i=0;i<strs.length-1;i++)
        {
            ow1 += "#od:1(" + strs[i] + " " +  strs[i+1] +" ) ";

            unigram += strs[i] +" ";
        }

        unigram += strs[strs.length-1] ;

        return "#combine:0=0.2:1=0.8:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  ) ";
    }

	public static String generateUnigramOWQuery(String q, String field)
    {

		String[] strs = q.split(" ");

		if(strs.length == 1)
			return "#combine(" + q + "." + field+ ")";
		
		String ow1 = "";
		String unigram = "";
		
		for(int i=0;i<strs.length-1;i++)
		{
			ow1 += "#od:1(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";

			unigram += strs[i] + "." + field +" ";
		}

		unigram += strs[strs.length-1] + "." + field ;


        return "#combine:0=0.2:1=0.8:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  ) ";
    }

	
	public static String generateUnigramOWConjuctiveQuery(String q)
    {

		String[] strs = q.split(" ");
        if(strs.length == 1)
            return "#combine(" + q + ")";
        String ow1 = "";
        String unigram = "";
        for(int i=0;i<strs.length-1;i++)
        {
            ow1 += "#od:1(" + strs[i] + " " +  strs[i+1] +" ) ";

            unigram += strs[i] +" ";
        }

        unigram += strs[strs.length-1] ;

        return "#require( #all ("+ unigram + ") #combine:0=0.2:1=0.8:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  ) )";
    }

	
	public static String generateUnigramOWConjuctiveQuery(String q, String field)
    {



        String[] strs = q.split(" ");
        if(strs.length == 1)
            return "#combine(" + q + "." + field+ ")";
        String ow1 = "";
        String unigram = "";
        for(int i=0;i<strs.length-1;i++)
        {
            ow1 += "#od:1(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";

            unigram += strs[i] + "." + field +" ";
        }

        unigram += strs[strs.length-1] + "." + field ;

        return "#require( #all ("+ unigram + ") #combine:0=0.2:1=0.8:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  ) )";
    }

	 	    
	    public static String generateSDMQuery(String q, String field)
	    {



	        String[] strs = q.split(" ");
	        if(strs.length == 1)
	            return "#combine(" + q + "." + field+ ")";
	        
	        String ow = "";
	        String uw = "";
	        String unigram = "";
	        
	        for(int i=0;i<strs.length-1;i++)
	        {
	            ow += "#od:1(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";
	            uw += "#uw:8(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";

	            unigram += strs[i] + "." + field +" ";
	        }

	        unigram += strs[strs.length-1] + "." + field ;

	        return "#combine:0=0.75:1=0.15:2=0.1:w=1.0( #combine(" + unigram + ")  #combine(" + ow.trim() + ")  #combine(" + uw.trim() + "))";
	    }


	    
	private static String[] sw = new String[]{"a", "am", "an", "and", "are", "as", "at", "be", "been", "being", "by", "did", "do", "does", "doing", "done", "for", "from", "had", "have", "has", "he", "in", "if", "is", "it", "its", "of", "on", "or", "that", "the", "to", "was", "were", "will", "with"};
	private static String[] fsw = new String[]{"a", "about", "above", "according", "across", "after", "afterwards", "again", "against", "albeit", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anywhere", "apart", "are", "around", "as", "at", "av", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "both", "but", "by", "can", "cannot", "canst", "certain", "cf", "choose", "contrariwise", "cos", "could", "cu", "day", "do", "does", "doesn't", "doing", "dost", "doth", "double", "down", "dual", "during", "each", "either", "else", "elsewhere", "enough", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "except", "excepted", "excepting", "exception", "exclude", "excluding", "exclusive", "far", "farther", "farthest", "few", "ff", "first", "for", "formerly", "forth", "forward", "from", "front", "further", "furthermore", "furthest", "get", "go", "had", "halves", "hardly", "has", "hast", "hath", "have", "he", "hence", "henceforth", "her", "here", "hereabouts", "hereafter", "hereby", "herein", "hereto", "hereupon", "hers", "herself", "him", "himself", "hindmost", "his", "hither", "hitherto", "how", "however", "howsoever", "i", "ie", "if", "in", "inasmuch", "inc", "include", "included", "including", "indeed", "indoors", "inside", "insomuch", "instead", "into", "inward", "inwards", "is", "it", "its", "itself", "just", "kind", "kg", "km", "last", "latter", "latterly", "less", "lest", "let", "like", "little", "ltd", "many", "may", "maybe", "me", "meantime", "meanwhile", "might", "moreover", "most", "mostly", "more", "mr", "mrs", "ms", "much", "must", "my", "myself", "namely", "need", "neither", "never", "nevertheless", "next", "no", "nobody", "none", "nonetheless", "noone", "nope", "nor", "not", "nothing", "notwithstanding", "now", "nowadays", "nowhere", "of", "off", "often", "ok", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "own", "per", "perhaps", "plenty", "provide", "quite", "rather", "really", "round", "said", "sake", "same", "sang", "save", "saw", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "seldom", "selves", "sent", "several", "shalt", "she", "should", "shown", "sideways", "since", "slept", "slew", "slung", "slunk", "smote", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "spake", "spat", "spoke", "spoken", "sprang", "sprung", "stave", "staves", "still", "such", "supposing", "than", "that", "the", "thee", "their", "them", "themselves", "then", "thence", "thenceforth", "there", "thereabout", "thereabouts", "thereafter", "thereby", "therefore", "therein", "thereof", "thereon", "thereto", "thereupon", "these", "they", "this", "those", "thou", "though", "thrice", "through", "throughout", "thru", "thus", "thy", "thyself", "till", "to", "together", "too", "toward", "towards", "ugh", "unable", "under", "underneath", "unless", "unlike", "until", "up", "upon", "upward", "upwards", "us", "use", "used", "using", "very", "via", "vs", "want", "was", "we", "week", "well", "were", "what", "whatever", "whatsoever", "when", "whence", "whenever", "whensoever", "where", "whereabouts", "whereafter", "whereas", "whereat", "whereby", "wherefore", "wherefrom", "wherein", "whereinto", "whereof", "whereon", "wheresoever", "whereto", "whereunto", "whereupon", "wherever", "wherewith", "whether", "whew", "which", "whichever", "whichsoever", "while", "whilst", "whither", "who", "whoa", "whoever", "whole", "whom", "whomever", "whomsoever", "whose", "whosoever", "why", "will", "wilt", "with", "within", "without", "worse", "worst", "would", "wow", "ye", "yet", "year", "yippee", "you", "your", "yours", "yourself", "yourselves"};
	public static boolean isStop(String term)
	{
		return isStop(term, true);
	}
	public static boolean isStop(String term, boolean fullStop)
	{
		String[] sws = (fullStop==true)?fsw:sw; 
		boolean flag = false;
		for(int j=0;j<sws.length&&!flag;j++)
			if(sws[j].compareTo(term) == 0)
				flag = true;
		return flag;
	}
	public static String removeStopWords(String query, boolean fullStop)
	{	
		String s = "";
		String[] strs = query.split(" ");
		for(int i=0;i<strs.length;i++)
		{
			if(!isStop(strs[i], fullStop))
				s += strs[i] + " ";
		}
		
		while(s.indexOf("  ") != -1)
	    	s = s.replace("  ", " ");
		
		return s.trim();
	}
	public static String removeNonAlphaNumericCharacters(String query)
	{
	    if (query.compareTo("") == 0)
	    {
	        return "";
	    }

	    query = query.replaceAll("'s", " s ");
	    query = query.replaceAll("s'", "s ");

	    String regex = "[,<.>/?;:'\"`~!@#$%^&*()\\-_+=|\\\\]";
	    //String regex = "[,./?;:\"`~!()\\{\\}\\[\\]]";
	    query = query.replaceAll(regex, "");
	    	    
	    query = query.trim();
	    while(query.indexOf("  ") != -1)
	    	query = query.replace("  ", " ");
	    
	    return query;
	}

	
	
}
