package ciir.umass.edu.retrieval.dts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ciir.umass.edu.utilities.Sorter;

public class LanguageModel {

	protected HashMap<String, Double> lm = null; 
	protected List<String> vocabs = null;
	
	public LanguageModel()
	{
		lm = new HashMap<String, Double>();
		vocabs = new ArrayList<String>();
	}
	public void set(HashMap<String, Long> ngramFreq)
	{
		lm = new HashMap<String, Double>();
		for(String key: ngramFreq.keySet())
			lm.put(key, (double)ngramFreq.get(key));
		normalize();
	}
	public void normalize()
	{
		double total = 0.0;
		vocabs = new ArrayList<String>();
		for(String key : lm.keySet())
		{
			vocabs.add(key);
			total += lm.get(key);
		}
		for(String key : lm.keySet())
			lm.put(key, lm.get(key)/total);
	}
	
	public LanguageModel(HashMap<String, Double> lm)
	{
		this.lm = lm;
		vocabs = new ArrayList<String>();
		for(String key : lm.keySet())
			vocabs.add(key);
	}
	
	public LanguageModel(String strRep)
	{
		fromString(strRep);
	}
	public LanguageModel(String strRep, int topN)
	{
		fromString(strRep, topN);
	}
	
	public List<String> getVocabs()
	{
		return vocabs;
	}
	public double probability(String term)
	{
		Double p = lm.get(term);
		if(p != null)
			return p.doubleValue();
		return 0.0;
	}
	
	public String toString()
	{
		String output = "";
		List<String> terms = new ArrayList<String>();
		List<Double> probs = new ArrayList<Double>();
		for(String key : lm.keySet())
		{
			terms.add(key);
			probs.add(lm.get(key).doubleValue());
		}
		int[] idx = Sorter.sortDesc(probs);
		for(int i=0;i<terms.size();i++)
			output += terms.get(idx[i]) + " " + probs.get(idx[i]) + " ";
		
		return output.trim();
	}
	public String toString(int topN)
	{
		String output = "";
		List<String> terms = new ArrayList<String>();
		List<Double> probs = new ArrayList<Double>();
		for(String key : lm.keySet())
		{
			terms.add(key);
			probs.add(lm.get(key).doubleValue());
		}
		int[] idx = Sorter.sortDesc(probs);
		int size = (idx.length>topN)?topN:idx.length;
		for(int i=0;i<size;i++)
			output += terms.get(idx[i]) + " " + probs.get(idx[i]) + " ";
		
		return output.trim();
	}
	public String toIndriWeightedQuery()
	{
		String output = "";
		List<String> terms = new ArrayList<String>();
		List<Double> probs = new ArrayList<Double>();
		for(String key : lm.keySet())
		{
			terms.add(key);
			probs.add(lm.get(key).doubleValue());
		}
		int[] idx = Sorter.sortDesc(probs);
		for(int i=0;i<terms.size();i++)
			output += probs.get(idx[i]) + " " + terms.get(idx[i]) + " ";
		
		return output.trim();
	}
	public String toIndriWeightedQuery(int topN)
	{
		String output = "";
		List<String> terms = new ArrayList<String>();
		List<Double> probs = new ArrayList<Double>();
		for(String key : lm.keySet())
		{
			terms.add(key);
			probs.add(lm.get(key).doubleValue());
		}
		int[] idx = Sorter.sortDesc(probs);
		int size = (idx.length>topN)?topN:idx.length;
		for(int i=0;i<size;i++)
			output += probs.get(idx[i]) + " " + terms.get(idx[i]) + " ";
		return output.trim();
	}
	
	public void fromString(String str)
	{
		try {
			lm = new HashMap<String, Double>();
			vocabs = new ArrayList<String>();
			String[] s = str.split(" ");
			for(int i=0;i<s.length;i+=2)
			{
				lm.put(s[i], Double.parseDouble(s[i+1]));
				vocabs.add(s[i]);
			}
		}
		catch(Exception ex)
		{
			System.out.println("Bad language model string representation.");
			System.out.println("Error in LanguageModel::fromString(): " + ex.toString());
			System.exit(1);
		}
	}
	public void fromString(String str, int topN)
	{
		try {
			lm = new HashMap<String, Double>();
			vocabs = new ArrayList<String>();
			String[] s = str.split(" ");
			int size = (s.length>(topN*2))?(topN*2):s.length;
			for(int i=0;i<size;i+=2)
			{
				lm.put(s[i], Double.parseDouble(s[i+1]));
				vocabs.add(s[i]);
			}
		}
		catch(Exception ex)
		{
			System.out.println("Bad language model string representation.");
			System.out.println("Error in LanguageModel::fromString(): " + ex.toString());
			System.exit(1);
		}
	}
}
