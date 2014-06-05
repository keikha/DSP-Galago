package ciir.umass.edu.sum.simmeasure;

import java.util.Enumeration;
import java.util.Hashtable;

public class SimJaccard {
	
	private ItemArrayList l1 = null;
	private ItemArrayList l2 = null;
	
	public SimJaccard(ItemArrayList l1, ItemArrayList l2)
	{
		this.l1 = l1;
		this.l2 = l2;
	}
	
	public double value()
	{
		int i = 0;
		int j = 0;
		double intersect = 0.0;
		double union = 0.0;
		while(i < l1.size() && j < l2.size())
		{
			union++;
			int v = l1.get(i).compareTo(l2.get(j));
			if(v == 0)
			{
				intersect++;
				i++;
				j++;
			}
			else if(v < 0)
				i++;
			else //v > 0
				j++;
		}
		union += (l1.size()-i) + (l2.size()-j);
		return ((double)(intersect))/union;
	}
	
	
	
	
	
	
	
	
	
	
	
	//OLD OLD OLD OLD !!!
	private long nSharedContext = 0;
	public long getSharedContext()
	{
		return nSharedContext;
	}
	/**
	 * Jaccard over only shared contexts
	 * @param l1
	 * @param l2
	 * @param pL1
	 * @param pL2
	 * @return
	 */
	public double getSim(ItemArrayList l1, ItemHashList l2, double pL1, double pL2)
	{
		double numerator = 0.0;
		double denominator = 0.0;
		double w1, w2, max, min;
		nSharedContext = 0;
		for(int i=0;i<l1.size();i++)
		{
			Item item = l1.get(i);
			Item match = l2.get(item.name());
			if(match != null)
			{
				nSharedContext++;
				w1 = ((item.prob() - item.bgprob()) * pL1) / Math.sqrt(item.bgprob() * pL1);
				w2 = ((match.prob() - match.bgprob()) * pL2) / Math.sqrt(match.bgprob() * pL2);
				max = w1;
				min = w2;
				if(max < min)
				{
					max = w2;
					min = w1;
				}
				numerator += min;
				denominator += max;
			}
		}
		return numerator/denominator;
	}
	/**
	 * Jaccard over union of contexts of both words
	 * @param l1
	 * @param l2
	 * @param pL1
	 * @param pL2
	 * @return
	 */
	public double getSim2(ItemArrayList l1, ItemHashList l2, double pL1, double pL2)
	{
		double numerator = 0.0;
		double denominator = 0.0;
		double w1, w2, max, min;
		nSharedContext = 0;
		Hashtable<String, Integer> tmp = new Hashtable<String, Integer>();
		for(int i=0;i<l1.size();i++)
		{
			Item item = l1.get(i);
			w1 = ((item.prob() - item.bgprob()) * pL1) / Math.sqrt(item.bgprob() * pL1);
			Item match = l2.get(item.name());
			if(match != null)
			{
				nSharedContext++;
				tmp.put(item.name(), 1);
				w2 = ((match.prob() - match.bgprob()) * pL2) / Math.sqrt(match.bgprob() * pL2);
				max = w1;
				min = w2;
				if(max < min)
				{
					max = w2;
					min = w1;
				}
				numerator += min;
				denominator += max;
			}
			else
				denominator += w1;
		}
		for(Enumeration<String> e = l2.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();
			if(tmp.get(key)==null)
			{
				Item match = l2.get(key);
				w2 = ((match.prob() - match.bgprob()) * pL2) / Math.sqrt(match.bgprob() * pL2);
				denominator += w2;
			}
		}
		return numerator/denominator;
	}
}
