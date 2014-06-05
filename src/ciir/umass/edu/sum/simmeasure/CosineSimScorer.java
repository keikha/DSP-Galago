package ciir.umass.edu.sum.simmeasure;

import ciir.umass.edu.retrieval.dts.LanguageModel;

public class CosineSimScorer extends SimScorer {
	public double score(LanguageModel lm1, LanguageModel lm2)
	{
		ItemArrayList l1 = convert(lm1);
		ItemArrayList l2 = convert(lm2);
		SimCosine s = new SimCosine(l1, l2);
		return s.value();
	}
}
