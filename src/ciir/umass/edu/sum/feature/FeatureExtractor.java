package ciir.umass.edu.sum.feature;

import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;

import java.util.*;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.ScoredDocument;

import lemurproject.indri.DocumentVector;
import lemurproject.indri.ScoredExtentResult;

public class FeatureExtractor {
private GalagoSearchEngine se = null;
	
	protected long maxResultsFetch = 1000;
	
 	public FeatureExtractor(String col) throws Exception
	{
		se = new GalagoSearchEngine(col);
	}
	public FeatureExtractor(GalagoSearchEngine se)
	{
		this.se = se;
	}
	
	/**
	 * 
	 * @param query An input entity (noun phrase in the context of tweets).
	 * @param topD
	 */
	public double[] run(String query, int topD, List<String> L)
	{
		double[] features = new double[4];
		try {
			ScoredDocument[] r = se.runQuery(query, topD);
		
			DiversityFeature df = new DiversityFeature(); //Diversity feature
            PMI pm = new PMI(se); //PMI feature
            Relevance re = new Relevance();


            //TopicalityFeature tf = new TopicalityFeature(se); //Topicality feature

            Document[] documentVectors = getDocumentVectors(r);
			double[] scores = getScores(r);
			
			double diversity = df.getValue(documentVectors, scores);
            double pmi = pm.getValue(query);
            double rel = re.getValue(documentVectors, scores, query);
            //double topicality = tf.getValue(L, getDocumentIDs(r), getScores(r));

            features[0] = diversity;
            features[1] = pmi;
            features[2] = (double)se.getGramCount(query, true)/(double)se.getCollectionTermCount(); //uniqueness
            features[3] = rel;
            //features[3] = topicality;


 			//int[] docIDs = getDocumentIDs(r);
			//double uniqueness = ((double)se.runQuery(query, docIDs, docIDs.length).length) / se.runQuery(query, docIDs, docIDs.length).length);
			//System.out.println(query + "\t" + diversity);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return features;
	}
	
	protected int[] getDocumentIDs(ScoredExtentResult[] r) throws Exception
	{
		int[] docIDs = new int[r.length];
		for(int j=0;j<r.length;j++)
			docIDs[j] = r[j].document;		
		return docIDs;
	}
	protected Document[] getDocumentVectors(ScoredDocument[] r) throws Exception
	{
		Long[] docIDs = new Long[r.length];
		for(int j=0;j<r.length;j++)
			docIDs[j] = r[j].document;		
		return se.getDocumentVectors(docIDs);
	}
	protected double[] getScores(ScoredDocument[] r) throws Exception
	{
		double[] scores = new double[r.length];
		for(int j=0;j<r.length;j++)
			scores[j] = r[j].document;		
		return scores;
	}
}
