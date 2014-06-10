package ciir.umass.edu.sum.feature;

/**
 * Created by ashishjain on 4/5/14.
 */
import ciir.umass.edu.qproc.Stopper;
import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;
import ciir.umass.edu.retrieval.utils.QueryProcessor;
import lemurproject.indri.DocumentVector;

import java.util.*;

public class PMI {
    //local variables
    private GalagoSearchEngine se = null;
    private String field = "";
    private HashMap<String, String> stem2original = new HashMap<String, String>();
    
    public PMI(GalagoSearchEngine se2, String f)
    {
        this.se = se2;
        this.field = f;
    }
    public double getValue(String Phrase)
    {
        String[] s = Phrase.split("\\s+");
        double final_pmi = 0.0;
        double pmi = 0.0;
        int i=1; //start from index i=1. For unigram, pmi=0.0
        try {
            while(i < s.length) {
                if(!Stopper.getInstance().isStop(s[i]))
                {
                    String gram = s[i-1] + " " + s[i];
                    gram = QueryProcessor.makeIndriFriendly(gram);
                    double c1 = se.getTermCount(s[i-1], false , this.field , stem2original);
                    double c2 = se.getTermCount(s[i], false, this.field , stem2original);
                    double c = 0;
                    if(gram.compareTo("") != 0)
                        c = se.getGramCount(gram, true, this.field , stem2original);
                    if(c1 > 0 && c2 > 0)
                    {
                        pmi = c / c1;
                        pmi *= se.getCollectionTermCount();
                        pmi /= c2;
                    }
                }
                final_pmi += pmi;
                i++;
            }
	    if (final_pmi > 0.0)
                final_pmi = final_pmi/(s.length-1);
        }
        catch(Exception e) {
            System.out.println(e.toString());
        }
        return final_pmi;
    }
}
