package ciir.umass.edu.sum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Please enter the query:");
		String query1 = br.readLine();
		
	       query1 = query1.replaceAll("[#,\\!,\\$,\\^,\\*,&,\\`,[0-9],@,%,(,),\\[,\\],\\?,\\.,\\,\\|,>,<]", "");

		List<String> terms = te.getResults(query1, false);
		for(String term : terms)
		{
			System.out.println(term);
			
		}

		System.out.println("Please enter the second query:");
		String query2 = br.readLine();
		

		List<String> documents = te.getDocuments(query2+" "+query1, 25, "tweet");
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
