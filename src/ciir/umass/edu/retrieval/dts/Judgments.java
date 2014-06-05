package ciir.umass.edu.retrieval.dts;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Judgments {
	private HashMap<String, List<JudgedDocument>> ht = new HashMap<String, List<JudgedDocument>>() ;
	
	public Judgments(String judgmentFile)
	{
		load(judgmentFile);
		
		double avgTopic = 0;
		double avgTopicPerDoc = 0;
		int nDoc = 0;
		int[] nTopicCount = new int[21];
		for(int i=0;i<nTopicCount.length;i++)
			nTopicCount[i] = 0;
		for(String key : ht.keySet())
		{
			Judgment j = get(key);
			avgTopic += j.aspectCount();
			for(int i=0;i<j.aspectCount(); i++)
			{
				if(j.relDocCount(i) <= nTopicCount.length-1)
					nTopicCount[j.relDocCount(i)] ++;
				else
					nTopicCount[nTopicCount.length-1] ++;
			}
			for(String doc : j.keySet())
				if(j.get(doc).coveredAspects().length > 0)
				{
					avgTopicPerDoc += j.get(doc).coveredAspects().length;
					nDoc ++;
				}
		}
		System.out.println("Avg. Topic Count: " + (avgTopic/ht.keySet().size()));
		System.out.println("Avg. Topic Count Per Document: " + (avgTopicPerDoc/nDoc));
		for(int i=0;i<nTopicCount.length;i++)
			System.out.print(nTopicCount[i] + "  ");
		System.out.println("");
	}
	
	public Judgment get(String qid)
	{
		Judgment j = null;
		List<JudgedDocument> documents = ht.get(qid);
		if(documents != null)
			j = new Judgment(documents);
		return j;
	}
	
	private void load(String fn)
	{
		BufferedReader in = null;
		//Loading n-grams
		try{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "ASCII"));
			String content = "";
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.compareTo("")==0 || content.startsWith("#"))
					continue;
				
				String[] s = content.split(" ");
				String qid = s[0];
				String docno = s[1];
				String relString = s[2];
				add(qid, docno, relString);
			}
			in.close();
		}
		catch(Exception e)
		{
			System.out.println("Error in Judgment::load(): " + e.toString());
		}
	}
	private void add(String qid, String docid, String judgment)
	{
		List<JudgedDocument> l = ht.get(qid);
		if(l == null)
		{
			l = new ArrayList<JudgedDocument>();
			ht.put(qid, l);
		}
		l.add(new JudgedDocument(docid, judgment));
	}
}
