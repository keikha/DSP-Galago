package ciir.umass.edu.sum;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.tupleflow.Parameters;

import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;

public class testTermExtractor {

	
	public static void main(String[] args) throws Exception {
		
		
		
		test1(args);
//		test2(args);
		
		
		
	}

	private static void test1(String[] args) throws Exception {
		TermExtractor te = new TermExtractor(args[0]);
		
		List<String> terms = te.getResults("geek squad", false);
//		for(String term : terms)
//		{
//			System.out.println(term);
//			
//		}

//		String newQuery = terms.get(0);
		List<String> documents = te.getDocuments("computer geek squad", 20, "tweet");
		for(String doc : documents)
			System.out.println(doc);
	}
	
	public static void test2(String[] args) throws Exception
	{
		Parameters param = Parameters.parseFile(args[0]);
		GalagoSearchEngine searchEngine = new GalagoSearchEngine(param);
        ArrayList<ScoredDocument> ini = new ArrayList<ScoredDocument>();
        
//        ScoredDocument[] results = searchEngine.runQuery("#require (#all(computer geek squad) #combine(computer geek squad))", 20);
        ScoredDocument[] results = searchEngine.runQuery("#require( #all (computer.tweet geek.tweet squad.tweet) #combine:0=0.1:1=0.55:2=0.35:w=1.0(  #combine(computer.tweet geek.tweet squad.tweet)  #combine(#od:1(computer.tweet geek.tweet ) #od:1(geek.tweet squad.tweet ))  #combine(#uw:8(computer.tweet geek.tweet ) #uw:8(geek.tweet squad.tweet ))) )", 20);
        

        for(ScoredDocument r : results)
        	System.out.println(searchEngine.getDocumentText(r.documentName, "tweet"));
	}

}
