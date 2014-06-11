package ciir.umass.edu.sum;

import java.util.List;

public class testTermExtractor {

	
	public static void main(String[] args) throws Exception {
		
		
		TermExtractor te = new TermExtractor(args[0]);
		
		List<String> terms = te.getResults("computer", false);
//		for(String term : terms)
//		{
//			System.out.println(term);
//			
//		}

		String newQuery = terms.get(0);
		List<String> documents = te.getDocuments(newQuery, 20, "tweet");
		for(String doc : documents)
			System.out.println(doc);
		
	}
}
