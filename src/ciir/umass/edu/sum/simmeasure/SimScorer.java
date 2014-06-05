package ciir.umass.edu.sum.simmeasure;

import java.util.List;

import ciir.umass.edu.retrieval.dts.LanguageModel;

public class SimScorer {
	
	protected ItemArrayList convert(LanguageModel lm)
	{
		ItemArrayList l = new ItemArrayList();
		List<String> s = lm.getVocabs();
		for(int i=0;i<s.size();i++)
		{
			double p = lm.probability(s.get(i));
			l.add(new Item(s.get(i), p));
		}
		l.sort(true);
		return l;
	}
	
	public double score(LanguageModel lm1, LanguageModel lm2)
	{
		return 0.0;
	}
}
