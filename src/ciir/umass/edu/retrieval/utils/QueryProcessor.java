package ciir.umass.edu.retrieval.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ciir.umass.edu.retrieval.dts.Reformulation;
import ciir.umass.edu.retrieval.dts.Document;
import ciir.umass.edu.retrieval.dts.Ranking;

public class QueryProcessor {

	public static void main(String[] args)
	{
		/*List<String> qids = new ArrayList<String>();
		List<String> qtexts = new ArrayList<String>(); 
		readTRECQueryFile("data/q2012.txt", qids, qtexts);
		for(int i=0;i<qids.size();i++)
		{
			String q = qtexts.get(i);
			q = removeStopWordLite(q);
			q = "#combine(" + q + ")";
			//q = "#combine( #prior(spam60) #prior(fracstops1) " + q + ")";
			qtexts.set(i, q);
		}
		IndriQueryFileWriter w = new IndriQueryFileWriter("data/output.query", "clueB");
		w.write(qids, qtexts);*/
		System.out.println(makeIndriFriendly("#combine(I go )"));
	}
	/**
	 * Read queries in TREC query file format
	 * @param inputQueryFile
	 * @param qids
	 * @param qs
	 */
	public static void readTRECQueryFile(String inputQueryFile, List<String> qids, List<String> qs)
	{
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputQueryFile), "ASCII"));
			String content = "";
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.compareTo("")==0 || content.startsWith("#"))
					continue;
				
				content = content.replace("</DOCNO>", ":");
				content = content.replace("<DOCNO>", "");
				content = content.replace("<DOC>", "");
				content = content.replace("</DOC>", "");
				
				String[] s = content.split(":");
				String qid = s[0].trim();
				String qtext = s[1].trim();
				qtext = qtext.replace("\"", "");
				qtext = qtext.replace("\'", "");
				
				while(qtext.indexOf("  ")!=-1)
					qtext = qtext.replace("  ", " ");
				
				qids.add(qid);
				qs.add(qtext);
			}
			in.close();
		}
		catch(Exception e)
		{
			System.out.println("Error in TRECQueryReader::read(): " + e.toString());
		}
	}
	
	public static void readIndriQueryFile(String fn, List<String> qids, List<String> qs)
	{
		try {
			String content = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "ASCII"));
			String idTag = "<number>";
			String idTagEnd = "</number>";
			String qTag = "<text>";
			String qTagEnd = "</text>";
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				
				int idx = content.indexOf(idTag);
				if(idx != -1)
				{
					int endIdx = content.lastIndexOf(idTagEnd);
					qids.add(content.substring(idx+idTag.length(), endIdx));
				}
				idx = content.indexOf(qTag);
				if(idx != -1)
				{
					int endIdx = content.lastIndexOf(qTagEnd);
					String q = content.substring(idx+qTag.length(), endIdx);
					q = q.trim();
					while(q.indexOf("  ")!=-1)
						q = q.replace("  ", " ");
					qs.add(q);
				}
			}
			in.close();
			System.out.println("Queries loaded. [#queries=" + qs.size() + "]");
		}
		catch(Exception ex)
		{
			System.out.println("Error in QueryProcessor::readIndriQueryFile(): " + ex.toString());
			System.exit(1);
		}
	}
	
	/**
	 * Assuming the input q is already Indri-friendly
	 * @param q text-only query (no #combine, etc)
	 * @return
	 */
	public static String generateMRFQuery(String q)
	{
		String[] strs = q.split(" ");
		if(strs.length == 1)
			return "#combine(" + q + ")";
		String ow = "";
		String uw = "";
		for(int i=0;i<strs.length-1;i++)
		{
			ow += "#od:1(" + strs[i] + " " + strs[i+1] + ") ";
			uw += "#uw:8(" + strs[i] + " " + strs[i+1] + ") ";
		}
		return "#weight(0.85 #combine(" + q + ") 0.1 #combine(" + ow.trim() + ") 0.05 #combine(" + uw.trim() + "))";
	}
	
	/**
	 * Read the ranking file generated by Indri
	 * @param fn
	 * @return
	 */
	public static List<Ranking> readIndriRankingFile(String fn)
	{
		return readIndriRankingFile(fn, -1, true);
	}
    public static String generateSDMFieldQuery(String q, String field)
    {



    	String[] strs = q.split(" ");


    	if(strs.length == 1)
    		return "#combine(" + q + ".(" + field+ "))";

    	String ow = "";
    	String uw = "";
    	String unigram = "";
    	for(int i=0;i<strs.length-1;i++)
    	{
    		ow += "#od:1(" + strs[i] + " " + strs[i+1] + ").("+ field+") ";
    		uw += "#uw:8(" + strs[i] + " " + strs[i+1] + ").("+ field+ ") ";
    		unigram += strs[i] + ".(" + field +") ";
    	}
    
        unigram += strs[strs.length-1] + ".(" + field+")" ;

        return "#combine:0=0.1:1=0.55:2=0.35:w=1.0( ( #combine(" + unigram + ")  #combine(" + ow.trim() + ")  #combine(" + uw.trim() + "))";
    }
    
//    #require (#all(computer geek squad) #combine(computer geek squad))
    
    public static String generateUnigramOWConjuctiveQuery(String q, String field)
    {



        String[] strs = q.split(" ");
        if(strs.length == 1)
            return "#combine(" + q + "." + field+ ")";
        String ow1 = "";
//        String ow8 = "";
        String unigram = "";
        for(int i=0;i<strs.length-1;i++)
        {
            ow1 += "#od:1(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";
//            ow8 += "#od:8(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";

            unigram += strs[i] + "." + field +" ";
        }

        unigram += strs[strs.length-1] + "." + field ;

//        return "#require( #all ("+ unigram + ") #combine:0=0.1:1=0.55:2=0.35:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  #combine(" + ow8.trim() + ")) )";
        return "#require( #all ("+ unigram + ") #combine:0=0.2:1=0.8:w=1.0(  #combine(" + unigram + ")  #combine(" + ow1.trim() + ")  ) )";
    }
    
    public static String generateModifiedSDMQuery(String q, String field)
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

        return "#combine:0=0.1:1=0.55:2=0.35:w=1.0( #combine(" + unigram + ")  #combine(" + ow.trim() + ")  #combine(" + uw.trim() + "))";
    }

    
    public static String generateBigramFieldQuery(String q, String field)
    {



        String[] strs = q.split(" ");
        if(strs.length == 1)
            return "#combine(" + q + "." + field+ ")";
        
        String ow = "";
        String uw = "";

//        for(int i=0;i<strs.length-1;i++)
//		{
//			ow += "#od:1(" + strs[i] + " " + strs[i+1] + ")."+ field+" ";
//			uw += "#uw:8(" + strs[i] + " " + strs[i+1] + ")."+ field+ " ";
//		}
		
//        String unigram = "";
        
        for(int i=0;i<strs.length-1;i++)
        {
            ow += "#od:1(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";
            uw += "#uw:8(" + strs[i] + "." + field + " " +  strs[i+1] +  "." + field +" ) ";

//            unigram += strs[i] + "." + field +" ";
        }

//        unigram += strs[strs.length-1] + "." + field ;

        return "#combine:0=0.7:1=0.3:w=1.0( #combine(" + ow.trim() + ")  #combine(" + uw.trim() + "))";
    }
    
    
	public static List<Ranking> readIndriRankingFile(String fn, int topD, boolean convertScore)
	{
		List<Ranking> rankings = new ArrayList<Ranking>();
		BufferedReader in = null;
		String lastQID = "";
		String content = "";
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "ASCII"));
			Ranking curR = new Ranking();
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.compareTo("")==0 || content.startsWith("#"))
					continue;
				
				String[] fields = content.split("\\s");
	            // 1 Q0 WSJ880711-0086 39 -3.05948 Exp
				String qid = fields[0];
				//String unused = fields[1];
				String docno = fields[2];
				//String rank = fields[3];
				double score = Double.parseDouble(fields[4]);
				if(convertScore)
				{
					if(score < 0.0)
						score = Math.exp(score);
					else //implying "=" 0.0
						score = Double.MIN_VALUE;
				}
				//String runtag = fields[5];		        
				if(lastQID.compareTo(qid) != 0)
				{
					if(curR.size() > 0)
					{
						rankings.add(curR);
						curR = new Ranking();
					}
					lastQID = qid;
					curR.setQueryID(qid);
				}
				if(topD == -1 || curR.size() < topD)
					curR.add(new Document(docno, score));
			}
			rankings.add(curR);
			in.close();
		}
		catch(Exception e)
		{
			System.out.println("Error in QueryProcessor::readRankings(): " + e.toString());
			System.out.println(lastQID + ". Content: " + content);
			System.exit(1);
		}
		System.out.println("Rankings loaded. [#queries=" + rankings.size() + "]");
		return rankings;
	}
	
	public static void writeIndriRankingFile(List<Ranking> rankings, String outputFile)
	{
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
			for(int i=0;i<rankings.size();i++)
			{
				Ranking r = rankings.get(i);
				for(int j=0;j<r.size();j++)
				{
					out.write(r.getQueryID() + " Q0 " + r.get(j).docID + " " + (j+1) + " " + r.get(j).score + " indri");
					out.newLine();
				}
			}
			out.close();
		}
		catch(Exception ex)
		{
			System.out.println("QueryProcessor::writeIndriRankingFile():: " + ex.toString());
			System.exit(1);
		}
	}

	/**
	 * Read reformulation file of which format is "<qid> <tab> <num> <tab> qtext". <num>=0 indicate original query text. Otherwise, it indicates the
	 * probability of this reformulation.
	 * @param fn The input filename.
	 * @param topR only keep top R reformulations for each query
	 * @param uniform Whether all reformulations have uniform distribution (ignore <num>) 
	 * @param stop Whether to do lite-stopping
	 * @param addQuote add quote to each query term (if your input query has been stemmed) 
	 * @return
	 */
	public static HashMap<String, List<Reformulation>> readReformulations(String fn, int topR, boolean uniform, boolean stop, boolean addQuote)
	{
		HashMap<String, List<Reformulation>> reformulations = new HashMap<String, List<Reformulation>>();
		BufferedReader in = null;
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "ASCII"));
			String content = "";
			String lastID = "";
			String origQ = "";
			List<Reformulation> l = new ArrayList<Reformulation>();
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.compareTo("")==0 || content.startsWith("#"))
					continue;
				
				String[] fields = content.split("\t");
	            String qid = fields[0];
				String num = fields[1];
				String qtext = fields[2];
				
				if(stop)
					qtext = removeStopWords(qtext, false);
				
				if(addQuote)
				{
					String[] s = qtext.split(" ");
					qtext = "";
					for(int i=0;i<s.length;i++)
						if(s[i].compareTo("")!=0)
							qtext += "\"" + s[i] + "\"" + ((i==s.length-1)?"":" ");
					qtext = qtext.trim();
				}
				
				if(num.compareTo("0") == 0)//this is the original query
				{
					origQ = qtext;
					continue;
				}
				
				if(lastID.compareTo("") == 0)
					lastID = qid;
				else if(lastID.compareTo(qid) != 0)
				{
					if(l.size() > 0)
					{
						if(uniform)//aspects (or reformulations) have uniform distribution 
							for(int i=0;i<l.size();i++)
								l.get(i).prob = 1.0/l.size();
						reformulations.put(lastID, l);
					}
					
					l = new ArrayList<Reformulation>();
					lastID = qid;
				}
				if(topR == -1 || l.size() < topR)
					l.add(new Reformulation(qtext, Double.parseDouble(num), origQ));
			}
			in.close();
			
			if(l.size() > 0)
			{
				if(uniform)
					for(int i=0;i<l.size();i++)
						l.get(i).prob = 1.0/l.size();
				reformulations.put(lastID, l);
			}
		}
		catch(Exception e)
		{
			System.out.println("Error in QueryProcessor::readReformulations(): " + e.toString());
		}
		System.out.println("Reformulations loaded. [#queries=" + reformulations.keySet().size() + "]");
		return reformulations;
	}
	public static HashMap<String, List<Reformulation>> readReformulations(String fn)
	{
		return readReformulations(fn, -1);
	}
	public static HashMap<String, List<Reformulation>> readReformulations(String fn, int topR)
	{
		return readReformulations(fn, topR, false, false, false);
	}
	
	private static final String FORBIDDEN_CHAR = "[^\\w\\.\\s\"]";
	public static String makeIndriFriendly(String query)
	{
		String qs = query;
		qs = " " + query + " ";
		qs = qs.replaceAll("\"", "");
		//qs = qs.replace(" AND ", " ");
		//qs = qs.replace(" OR ", " ");
		
		// drop characters that are not properly supported by Indri
		// ('.' is only allowed in between digits)
		qs = qs.replaceAll("&\\w++;", " ");
		qs = qs.replaceAll(FORBIDDEN_CHAR, " ");
		String dotsRemoved = "";
		for (int i = 0; i < qs.length(); i++)
			if (qs.charAt(i) != '.' ||
				(i > 0 && i < qs.length() - 1 &&
				 Character.isDigit(qs.charAt(i - 1)) &&
				 Character.isDigit(qs.charAt(i + 1))))
				 dotsRemoved += qs.charAt(i);
		qs = dotsRemoved;
		
		// replace ... OR ... by #or(... ...)
		Matcher m = Pattern.compile(
			"((\\([^\\(\\)]*+\\)|\\\"[^\\\"]*+\\\"|[^\\s\\(\\)]++) OR )++" +
			"(\\([^\\(\\)]*+\\)|\\\"[^\\\"]*+\\\"|[^\\s\\(\\)]++)").matcher(qs);
		while (m.find())
			qs = qs.replace(m.group(0), "#or(" + m.group(0) + ")");
		qs = qs.replace(" OR", "");
		
		// replace ... AND ... by #combine(... ...)
		m = Pattern.compile(
			"((\\([^\\(\\)]*+\\)|\\\"[^\\\"]*+\\\"|[^\\s\\(\\)]++) AND )++" +
			"(\\([^\\(\\)]*+\\)|\\\"[^\\\"]*+\\\"|[^\\s\\(\\)]++)").matcher(qs);
		while (m.find())
			qs = qs.replace(m.group(0), "#combine(" + m.group(0) + ")");
		qs = qs.replace(" AND", "");
		
		// replace "..." by #1(...)
		m = Pattern.compile("\"([^\"]*+)\"").matcher(qs);
		while (m.find())
			qs = qs.replace(m.group(0), "#1(" + m.group(1) + ")");
		
		qs = qs.replace("\"", "");
		qs = qs.replace("'", " ");
		while(qs.indexOf("  ")!=-1)
			qs = qs.replace("  ", " ");
		return qs.toLowerCase().trim();
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
