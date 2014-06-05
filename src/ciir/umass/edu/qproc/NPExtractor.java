package ciir.umass.edu.qproc;

import java.util.ArrayList;
import java.util.List;

import ciir.umass.edu.retrieval.utils.IndriSearchEngine;
import ciir.umass.edu.retrieval.utils.QueryProcessor;
import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;

public class NPExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static double threshold = 200;//1000;
	protected GalagoSearchEngine se = null;
	
	public NPExtractor(GalagoSearchEngine se)
	{
		this.se = se;
	}
	public List<NounPhrase> extract(String sentence)
	{
		List<NounPhrase> nps = new ArrayList<NounPhrase>();
		try {
			String[] s = sentence.split("\\s+");
			if(s.length > 0)
			{
				int i=0;
				String phrase = "";
				int start = -1;
				while(i<s.length && Stopper.getInstance().isStop(s[i])) i++;
				if(i < s.length)
				{
					phrase = s[i];
					start = i;
				}
				i++;
				while(i < s.length)
				{
					double pmi = 0;
					if(!Stopper.getInstance().isStop(s[i]))
					{
						String gram = s[i-1] + " " + s[i];
						gram = QueryProcessor.makeIndriFriendly(gram);
						double c1 = se.getTermCount(s[i-1], false);
						double c2 = se.getTermCount(s[i], false);
						double c = 0;
						if(gram.compareTo("") != 0)
							c = se.getGramCount(gram, true);
						if(c1 > 0 && c2 > 0)
						{
							pmi = c / c1;
							pmi *= se.getCollectionTermCount();
							pmi /= c2;
						}
					}
					if(pmi > threshold)
						phrase += " " + s[i];
					else
					{
						nps.add(new NounPhrase(phrase, start, i-1));
						
						if(!Stopper.getInstance().isStop(s[i]))
						{
							phrase = s[i];
							start = i;
						}
						else
						{
							i++;
							phrase = "";
							while(i < s.length && Stopper.getInstance().isStop(s[i])) i++;
							if(i < s.length)
							{
								phrase = s[i];
								start = i;
							}
						}
					}
					i++;
				}
				if(phrase.compareTo("") != 0)
					nps.add(new NounPhrase(phrase, start, s.length-1));
			}			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return nps;
	}
}
