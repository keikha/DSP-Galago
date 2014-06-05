package ciir.umass.edu.retrieval.dts;

import java.util.ArrayList;
import java.util.List;

public class Ranking {
	
	public String qid = "";
	private List<Document> docs = new ArrayList<Document>();
	
	public Ranking()
	{
		
	}
	
	public Ranking(Ranking r, int idx[])
	{
		qid = r.qid;
		for(int i=0;i<idx.length;i++)
		{
			int k = idx[i];
			docs.add(r.get(k));
		}
	}
	
	public void add(Document d)
	{
		docs.add(d);
	}
	public void remove(int k)
	{
		if(docs.size() > k)
			docs.remove(k);
	}
	public Document get(int k)
	{
		return docs.get(k);
	}
	public int size()
	{
		return docs.size();
	}
	public String getQueryID()
	{
		return qid;
	}
	public void setQueryID(String qid)
	{
		this.qid = qid;
	}
}
