package ciir.umass.edu.sum.simmeasure;

import ciir.umass.edu.retrieval.utils.IndriSearchEngine;
import ciir.umass.edu.retrieval.utils.QueryProcessor;

public class SimKLDivergence {
	
	private double lambda = 0.4;
	private ItemArrayList l1 = null;
	private ItemArrayList l2 = null;
	protected IndriSearchEngine se = null;
	
	public SimKLDivergence(ItemArrayList l1, ItemArrayList l2, IndriSearchEngine se)
	{
		this.l1 = l1;
		this.l2 = l2;
		this.se = se;
	}
	
	public double value()
	{
		double score = 0.0;
		int i=0;
		int j=0;
		while(i < l1.size() && j < l2.size())
		{
			int v = l1.get(i).compareTo(l2.get(j));
			if(v == 0)
			{
				score += l1.get(i).prob() * Math.log(l1.get(i).prob() / l2.get(j).prob());
				i++;
				j++;
			}
			else if(v < 0)
			{
				double smooth = smoothProb(l1.get(i).name());
				score += l1.get(i).prob() * Math.log(l1.get(i).prob() / smooth);
				i++;
			}
			else //v > 0
			{
				j++;
			}
		}
		while(i < l1.size())
		{
			double smooth = smoothProb(l1.get(i).name());
			score += l1.get(i).prob() * Math.log(l1.get(i).prob() / smooth);
			i++;
		}
		return Math.exp(-score);
	}

	protected double smoothProb(String stem)
	{
		double smooth = Double.MIN_VALUE;
		try {
			stem = QueryProcessor.makeIndriFriendly(stem);
			if(stem.compareTo("")!=0)
				smooth = lambda * se.getTermCollectionProb(stem, true, true);
		}
		catch(Exception ex)
		{
			System.out.println("Error in SimKLDivergence::smoothProb(): " + ex.toString());
			System.out.println("Error in SimKLDivergence::smoothProb(): Term = " + stem);
		}
		return smooth;
	}
}
