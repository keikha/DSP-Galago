package ciir.umass.edu.retrieval.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ciir.umass.edu.utilities.Sorter;

import lemurproject.indri.DocumentVector;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;

public class IndriSearchEngine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String collection = args[0];
		int topD = Integer.parseInt(args[1]);
		boolean useFreq = false;
		if(args[2]!=null)
		{
			useFreq = (args[2].compareTo("1")==0);
			System.out.println("[Use frequency]");
		}
		try {
			IndriSearchEngine se = new IndriSearchEngine(collection);
			Scanner scanner = new Scanner(System.in);
			String input = "";
			do {
				input = scanner.nextLine().trim();
				topD = Integer.parseInt(input.split(" ")[input.split(" ").length-1]);
				input = input.substring(0, input.lastIndexOf(" ")).trim();
				System.out.println("query = " + input);
				
				ScoredExtentResult[] r = null;
				
				if(useFreq)
					r = se.runQuery(input, 1000);
				else
					r = se.runQuery(input, topD);
					
				int[] docIDs = new int[r.length];
				for(int i=0;i<r.length;i++)
					docIDs[i] = r[i].document;

				ParsedDocument[] pd = se.getParsedDocuments(docIDs);
				
				if(useFreq)
				{
					int[] freq = new int[pd.length];
					for(int i=0;i<pd.length;i++)
					{
						String t = pd[i].content;
						int i1 = t.indexOf("<DOCNO>");
						int i2 = t.indexOf("</DOCNO>");
						String docno = t.substring(i1+7, i2).trim();
						String[] s = docno.split("-");
						freq[i] = Integer.parseInt(s[1]);					
					}
					int[] idx = Sorter.sort(freq, false);
					int s = (idx.length>topD)?topD:idx.length;
					for(int i=0;i<s;i++)
					{
						int k = idx[i];
						System.out.println("-----------------------------------------------");
						System.out.println("[" + (i+1) + "][" + r[k].score + "]" + pd[k].content);
					}
				}
				else
				{
					for(int i=0;i<pd.length;i++)
					{
						System.out.println("-----------------------------------------------");
						System.out.println("[" + (i+1) + "][" + r[i].score + "]" + pd[i].content);
					}
				}
			}while(input.compareToIgnoreCase("EXIT")!=0);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}

	//sydney
	/*String[] stem_index = new String[]{"/mnt/lustre/vdang/trec/indexes/ap",
			"/mnt/lustre/vdang/trec/indexes/wsj",
			"/mnt/lustre/vdang/trec/indexes/wt10g",
			"/mnt/lustre/vdang/trec/indexes/robust04",
			"/mnt/lustre/xuexb/index/gov2"};
	String[] stem_index = new String[]{"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/ap",
			"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/wsj",
			"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/wt10g",
			"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/robust04",
			"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/gov2",
			"/mnt/work1/croft/vdang/ret-data/indexes/kstem-woStop/ft",
			"/nfs/sydney/indexes/ClueWeb09/clueweb09-English-1-10",
			"/nfs/sydney/indexes/ClueWeb09/ClueWeb09_English_1"};*/
	
	private static List<String> vocabs = null;
	private static int PASSAGE_LENGTH = 50;
	
	//private Hashtable<String, Integer> stem_collections_mapping = new Hashtable<String, Integer>();
	private String index = "";
	private QueryEnvironment env = new QueryEnvironment();
	
	public long collectionDocCount = -1;
	public long collectionTermCount = -1;
	
	public IndriSearchEngine(String col) throws Exception
	{
		index = DataSource.get().getIndex(col);
		env.addIndex(index);
		collectionTermCount = env.termCount();
		collectionDocCount = env.documentCount();
	}
	//run a query
	public ScoredExtentResult[] runQuery(String q, int topD) throws Exception
	{
		return env.runQuery(q, topD);
	}
	public ScoredExtentResult[] runQuery(String q, int[] docIDs, int topD) throws Exception
	{
		return env.runQuery(q, docIDs, topD);
	}
	//provide access to collection statistics
	public int[] docInternalIDs(String[] docExtIDs) throws Exception
	{
		int[] ids = env.documentIDsFromMetadata("docno", docExtIDs);
		return ids;
	}
	public long getCollectionDocumentCount() throws Exception
	{
		return collectionDocCount;
	}
	public long getCollectionTermCount() throws Exception
	{
		return collectionTermCount;
	}
	public long getDocumentCount(String term) throws Exception
	{
		return env.documentCount(term);
	}
	public long getTermCount(String term, boolean isStem) throws Exception
	{
		if(isStem == false)
			return env.termCount(term);		
		long c = (long)env.expressionCount("#1(\"" + term + "\")");
		return c;
	}
	public long getGramCount(String gram, boolean isStem) throws Exception
	{
		String[] strs = gram.split(" ");
		gram = "";
		for(int i=0;i<strs.length;i++)
			if(isStem)
				gram += "\"" + strs[i] + "\" ";
			else
				gram += strs[i] + " ";
		gram = gram.trim();
		long c = (long)env.expressionCount("#1(" + gram + ")");
		return c;
	}
	public double getTermCollectionProb(String term, boolean isStem, boolean smoothed) throws Exception
	{
		if(smoothed)
			return ((double)getTermCount(term, isStem)+0.5)/(collectionTermCount+1);
		return ((double)getTermCount(term, isStem))/(collectionTermCount);
	}
	//FIXME
	public List<String> getCollectionVocabulary() throws Exception
	{
/*		if(vocabs != null)
			return vocabs;
		
		vocabs = new ArrayList<String>();
		Index ind = IndexManager.openIndex(index);
		int termID;
		// iterate over all possible termID's, the termCountUnique() function
		// gives the total count of unique terms, i.e., the vocabulary size.
		// Note that the term index 0 is reserved for out-of-vocabulary
		// terms, so we start from 1.
		for (termID = 1; termID <= ind.termCountUnique(); termID++)
		{
			String term = ind.term(termID);
			if(!StopList.inStopList(term))
				vocabs.add(term);
		}
		return vocabs;*/
		return null;
	}
	public double expressionCount(String exp) throws Exception
	{
		return env.expressionCount(exp);
	}
	public int getDocumentLength(int docID) throws Exception
	{
		return env.documentLength(docID);
	}
	public long getPassageAllCount(String[] qTerms)throws Exception
	{
		String q = "#uw" + PASSAGE_LENGTH + "(";
		for(int i=0;i<qTerms.length;i++)
			q += qTerms[i] + ((i==qTerms.length-1)?"":" ");
		q += ")";
		System.out.println(q);
		return (long)env.expressionCount(q);
	}
	public long getDocAllCount(String[] qTerms)throws Exception
	{
		String q = "";
		for(int i=0;i<qTerms.length;i++)
			if(qTerms[i].compareTo("")!=0)
				q += qTerms[i] + " ";
		q = q.trim();
		ScoredExtentResult[] results = env.runQuery("#band(" + q + ")", 10000000);
		return results.length;
	}
	public long getDocAllCount(String query)throws Exception
	{
		return getDocAllCount(query.split(" "));
	}
	
	public DocumentVector[] getDocumentVectors(int[] docIDs) throws Exception
	{
		return env.documentVectors(docIDs);
	}
	public ParsedDocument[] getParsedDocuments(int[] docIDs) throws Exception 
	{
		return env.documents(docIDs);
	}
	
	/**
	 * Find documents containing any of the query terms
	 * @param qws
	 * @return
	 * @throws Exception
	 */
	public int getDocCount(List<String> qws) throws Exception
	{
		List<ScoredExtentResult[]> docList = new ArrayList<ScoredExtentResult[]>();
		for(int i=0;i<qws.size();i++)
		{
			ScoredExtentResult[] rels = env.expressionList("#ow1(" + qws.get(i) + ")");
			if(rels.length > 0)
				docList.add(rels);
		}
		return unionCount(docList);
	}
	public String getDocs(String term) throws Exception
	{
		ScoredExtentResult[] rels = env.expressionList("#ow1(" + term + ")");
		if(rels.length==0)
			return "";
		String str = rels[0].document + "";
		for(int i=1;i<rels.length;i++)
			if(rels[i].document != rels[i-1].document)
				str += rels[i].document + " ";
		return str.trim();
	}
	private int unionCount(List<ScoredExtentResult[]> docList)
	{
		int totalSize = 0;
		int[] currentPos = new int[docList.size()];
		List<Integer> values = new ArrayList<Integer>();
		List<Integer> positions = new ArrayList<Integer>();
		//init values[]
		for(int i=0;i<docList.size();i++)
		{
			totalSize += docList.get(i).length;
			values.add(docList.get(i)[0].document);
			positions.add(i);
			currentPos[i] = 0;
		}
		//sort values[]
		for(int i=0;i<values.size()-1;i++)
		{
			int min = i;
			for(int j=i+1;j<values.size();j++)
				if(values.get(min) > values.get(j))
					min = j;
			if(min != i)//swap
			{
				int tmp = values.get(i);
				values.set(i, values.get(min));
				values.set(min, tmp);
				
				tmp = positions.get(i);
				positions.set(i, positions.get(min));
				positions.set(min, tmp);
			}
		}
		//values[] is now sorted
		int countRedundant = 0;
		for(int i=1;i<values.size();i++)
		{
			if(values.get(i)==values.get(i-1))
				countRedundant++;
		}
		//start
		do{
			int minList = positions.get(0);
			values.remove(0);
			positions.remove(0);
			do
			{
				currentPos[minList]++;
				if(currentPos[minList] == docList.get(minList).length)
					break;
				if(docList.get(minList)[currentPos[minList]].document != docList.get(minList)[currentPos[minList]-1].document)
					break;
				countRedundant++;
			}while(true);
			if(currentPos[minList] == docList.get(minList).length)
				continue;
			int newValue = docList.get(minList)[currentPos[minList]].document;
			countRedundant += countRedundant(values, newValue);
			//find the position to insert newValue into values[] so that values[] remains sorted
			int i=0;
			for(;i<values.size();i++)
				if(newValue < values.get(i))
					break;
			values.add(i, newValue);
			positions.add(i, minList);
		}while(values.size()>0);
		return (totalSize-countRedundant);
	}
	private List<Integer> computeUnion(List<ScoredExtentResult[]> docList)
	{
		List<Integer> unionList = new ArrayList<Integer>();
		int totalSize = 0;
		int[] currentPos = new int[docList.size()];
		List<Integer> values = new ArrayList<Integer>();
		List<Integer> positions = new ArrayList<Integer>();
		//init values[]
		for(int i=0;i<docList.size();i++)
		{
			totalSize += docList.get(i).length;
			values.add(docList.get(i)[0].document);
			positions.add(i);
			currentPos[i] = 0;
		}
		//sort values[]
		for(int i=0;i<values.size()-1;i++)
		{
			int min = i;
			for(int j=i+1;j<values.size();j++)
				if(values.get(min) > values.get(j))
					min = j;
			if(min != i)//swap
			{
				int tmp = values.get(i);
				values.set(i, values.get(min));
				values.set(min, tmp);
				
				tmp = positions.get(i);
				positions.set(i, positions.get(min));
				positions.set(min, tmp);
			}
		}
		//values[] is now sorted
		unionList.add(values.get(0));
		for(int i=1;i<values.size();i++)
		{
			if(values.get(i)!=values.get(i-1))
				unionList.add(values.get(i));
		}
		//start
		do{
			int minList = positions.get(0);
			values.remove(0);
			positions.remove(0);
			do
			{
				currentPos[minList]++;
				if(currentPos[minList] == docList.get(minList).length)
					break;
				if(docList.get(minList)[currentPos[minList]].document != docList.get(minList)[currentPos[minList]-1].document)
					break;
			}while(true);
			if(currentPos[minList] == docList.get(minList).length)
				continue;
			int newValue = docList.get(minList)[currentPos[minList]].document;
			if(countRedundant(values, newValue)==0)
				unionList.add(newValue);
			//find the position to insert newValue into values[] so that values[] remains sorted
			int i=0;
			for(;i<values.size();i++)
				if(newValue < values.get(i))
					break;
			values.add(i, newValue);
			positions.add(i, minList);
		}while(values.size()>0);
		return unionList;
	}
	private int countRedundant(List<Integer> values, int k)
	{
		for(int i=0;i<values.size();i++)
			if(values.get(i) == k)
				return 1;
		return 0;
	}

	public int getDocCountFromFile(List<String> qws, String invertedDir) throws Exception
	{
		int count = 0;
		BufferedReader[] files = new BufferedReader[qws.size()];
		try {
			for(int i=0;i<qws.size();i++)
				files[i] = new BufferedReader(new InputStreamReader(new FileInputStream(invertedDir + "/" + qws.get(i)+ ".txt"), "ASCII"));
			count = unionCountFromFile(files);
			for(int i=0;i<qws.size();i++)
				files[i].close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		return count;
	}
	public int unionCountFromFile(BufferedReader[] files) throws Exception
	{
		int totalSize = 0;
		List<Integer> values = new ArrayList<Integer>();
		List<Integer> positions = new ArrayList<Integer>();
		for(int i=0;i<files.length;i++)
		{
			files[i].readLine();//skip the first line
		}
		//init values[]
		for(int i=0;i<files.length;i++)
		{
			totalSize++;
			String str = files[i].readLine();
			if(str != null)
			{
				values.add(Integer.parseInt(str.split(" ")[0]));
				positions.add(i);
			}
		}
		//sort values[]
		for(int i=0;i<values.size()-1;i++)
		{
			int min = i;
			for(int j=i+1;j<values.size();j++)
				if(values.get(min) > values.get(j))
					min = j;
			if(min != i)//swap
			{
				int tmp = values.get(i);
				values.set(i, values.get(min));
				values.set(min, tmp);
				
				tmp = positions.get(i);
				positions.set(i, positions.get(min));
				positions.set(min, tmp);
			}
		}
		//values[] is now sorted
		int countRedundant = 0;
		for(int i=1;i<values.size();i++)
		{
			if(values.get(i)==values.get(i-1))
				countRedundant++;
		}
		//start
		do{
			int minList = positions.get(0);
			int previousDoc = values.get(0);
			int currentDoc = 0;
			values.remove(0);
			positions.remove(0);
			String str = null;
			do
			{
				str = files[minList].readLine();
				if(str == null)
					break;
				totalSize++;
				currentDoc = Integer.parseInt(str.split(" ")[0]);
				if(currentDoc != previousDoc)
					break;
				countRedundant++;
			}while(true);
			if(str == null)
				continue;
			
			countRedundant += countRedundant(values, currentDoc);
			//find the position to insert newValue into values[] so that values[] remains sorted
			int i=0;
			for(;i<values.size();i++)
				if(currentDoc < values.get(i))
					break;
			values.add(i, currentDoc);
			positions.add(i, minList);
		}while(values.size()>0);
		return (totalSize-countRedundant);
	}
}
