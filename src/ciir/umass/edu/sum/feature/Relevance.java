package ciir.umass.edu.sum.feature;

import ciir.umass.edu.qproc.NounPhrase;
import ciir.umass.edu.qproc.Stopper;
import ciir.umass.edu.retrieval.dts.LanguageModel;
import ciir.umass.edu.utilities.Sorter;
import lemurproject.indri.DocumentVector;
import ciir.umass.edu.qproc.POSTagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lemurproject.galago.core.parse.Document;

/**
 * Created by ashishjain on 4/6/14.
 */
public class Relevance {
    protected int topT = -1;//use all terms to form the word vector
    public double getValue(Document[] documents, double[] scores, String phrase)
    {
        LanguageModel[] lms = new LanguageModel[documents.length];
        for(int i=0;i<documents.length;i++)
        {
            lms[i] = buildDocumentLM(documents[i], topT);
        }
        double relScore = 0.0;
        String[] terms = phrase.split("\\s+");
        for(int i=0;i<documents.length;i++) {
            double lmScore = 1.0;
            for (int j=0; j< terms.length;j++) {
                lmScore=lmScore*lms[i].probability(terms[j]);
            }
            relScore+=scores[i]*lmScore;
        }
        return relScore;
    }
    protected LanguageModel buildDocumentLM(Document documents, int topTerms)
    {
        HashMap<String, Long> ngramFreq = new HashMap<String, Long>();
        for(int i=0;i<documents.terms.size();i++)
        {
            String stem = documents.terms.get(i);
            if(ngramFreq.get(stem) == null)
                ngramFreq.put(stem, 1L);
            else
                ngramFreq.put(stem, ngramFreq.get(stem).longValue() + 1);
        }

        if(topTerms != -1)
        {
            List<String> terms = new ArrayList<String>();
            List<Long> freqs = new ArrayList<Long>();
            for(String key : ngramFreq.keySet())
            {
                terms.add(key);
                freqs.add(ngramFreq.get(key).longValue());
            }
            int[] idx = Sorter.sortLong(freqs, false);
            int size = (idx.length>topTerms)?topTerms:idx.length;
            //re-init
            ngramFreq = new HashMap<String, Long>();
            for(int i=0;i<size;i++)
                ngramFreq.put(terms.get(idx[i]), freqs.get(idx[i]));
        }

        LanguageModel lm = new LanguageModel();
        lm.set(ngramFreq);
        return lm;
    }
}
