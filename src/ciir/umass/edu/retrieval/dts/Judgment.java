package ciir.umass.edu.retrieval.dts;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Judgment {
	private HashMap<String, JudgedDocument> docs = null;
	private int aspectCount = -1;
	private int relDocCount = 0;
	private int[] aspectRelDocCount = null;
	
	public Judgment(List<JudgedDocument> documents)
	{
		if(documents != null)
		{
			docs = new HashMap<String, JudgedDocument>();
			for(JudgedDocument d: documents)
			{
				if(d.relevance() > 0)
					relDocCount++;
				
				docs.put(d.docID, d);
				if(aspectCount < 0)//first run
				{
					aspectCount = d.aspectCount();
					aspectRelDocCount = new int[aspectCount];
					for(int i=0;i<aspectCount;i++)
						aspectRelDocCount[i] = 0;
				}
				
				int[] ca = d.coveredAspects();
				for(int i=0;i<ca.length;i++)
					aspectRelDocCount[ca[i]]++;
			}
		}
	}
	
	public JudgedDocument get(String docID)
	{
		return docs.get(docID);
	}
	
	public int aspectCount()
	{
		return aspectCount;
	}
	public int size()
	{
		return docs.size();
	}
	public int relDocCount()
	{
		return relDocCount;
	}
	public int actualAspectCount()
	{
		int c = 0;
		for(int i=0;i<aspectCount();i++)
			if(aspectRelDocCount[i] > 0)
				c++;
		return c;
	}
	public int relDocCount(int aspect)
	{
		return aspectRelDocCount[aspect];
	}
	public boolean hasRelevantDocuments(int aspect)//0-based
	{
		boolean rel = false;
		try {
			rel = (aspectRelDocCount[aspect] > 0);
		}
		catch(Exception ex)
		{
			System.out.println("Error in Judgment::hasRelevantDocuments(): " + ex.toString());
		}
		return rel;
	}
	public Set<String> keySet()
	{
		return docs.keySet();
	}
}
