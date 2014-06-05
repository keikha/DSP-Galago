package ciir.umass.edu.retrieval.dts;

public class JudgedDocument {
	public String docID = "";
	public int[] relevance = null;
	public int nCoveredAspect = 0;
	
	public JudgedDocument(String docID, String rel)
	{
		this.docID = docID;
		relevance = new int[rel.length()];
		for(int i=0;i<rel.length();i++)
		{
			relevance[i] = Character.getNumericValue(rel.charAt(i));
			if(relevance[i] > 0)
				nCoveredAspect++;
		}
	}
	public int[] coveredAspects()
	{
		int[] aspects = new int[nCoveredAspect];
		int j=0;
		for(int i=0;i<relevance.length;i++)
			if(relevance[i] > 0)
				aspects[j++] = i;
		return aspects;
	}
	
	public int aspectCount()
	{
		return relevance.length;
	}
	
	public int relevance()
	{
		int max = relevance[0];
		for(int i=1;i<relevance.length;i++)
			if(max < relevance[i])
				max = relevance[i];
		return max;
	}
	
	public int relevance(int subtopic)//0-based
	{
		if(subtopic < 0 || subtopic >= relevance.length)
		{
			System.out.println("Sub-topic = " + subtopic + ": Invalid subtopic!");
			System.exit(1);
		}
		return relevance[subtopic];
	}
}
