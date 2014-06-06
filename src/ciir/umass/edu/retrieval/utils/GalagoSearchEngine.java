package ciir.umass.edu.retrieval.utils;

import java.util.ArrayList;
import java.util.List;

import org.lemurproject.galago.core.index.stats.FieldStatistics;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.Document.DocumentComponents;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;


public class GalagoSearchEngine {

    private Retrieval retrieval ;
    private Parameters param ;
    private long collectionTermCount;
    private long collectionDocCount;
   
	public GalagoSearchEngine(String col) throws Exception {
		// TODO Auto-generated constructor stub
		retrieval = RetrievalFactory.instance(col);
		
		param = new Parameters();
	    
		//////////////////
		Node n = new Node();
	    
	    n.setOperator("lengths");
	    n.getNodeParameters().set("part", "lengths");
	    
	    FieldStatistics stat = retrieval.getCollectionStatistics(n);
	    collectionTermCount = stat.collectionLength;
	    collectionDocCount = stat.documentCount;
	    
	    //////////////////////
	    
	}

	public GalagoSearchEngine(Parameters p) throws Exception {
		 retrieval = RetrievalFactory.instance(p);
		 param = p;
		 
	}
	

	public ScoredDocument[] runQuery(String queryText, int topD) throws Exception {
		
	      param.set("requested", topD);
//	      // option to fold query cases -- note that some parameters may require upper case
	      if (param.get("casefold", false)) {
	        queryText = queryText.toLowerCase();
	      }
//
//	      // parse and transform query into runnable form
	      Node root = StructuredQuery.parse(queryText);
	      Node transformed = retrieval.transformQuery(root, param);
//
//
//	      // run query
	      List<ScoredDocument> results = retrieval.executeQuery(transformed, param).scoredDocuments;
	      return (ScoredDocument[]) results.toArray();
	}

	public Long[] docInternalIDs(String[] docExternalIDs) throws Exception {

		List<Long> internalDocBuffer = new ArrayList<Long>();

	    for (String name : docExternalIDs) {
	      try {
	    	  
	        internalDocBuffer.add(retrieval.getDocumentId(name));
	      } catch (Exception e) {
	        // arrays NEED to be aligned for good error detection
	        internalDocBuffer.add(-1L);
	      }
	    }
		return (Long[]) internalDocBuffer.toArray();
	}

	public Document[] getDocumentVectors(String[] docExternalIDs) throws Exception {
		
		TagTokenizer tokenizer = new TagTokenizer(new FakeParameters(param));
		    
		DocumentComponents dc = new DocumentComponents(param);
		
		List<Document> documents = new ArrayList<Document>();
		
		for(String id : docExternalIDs)
		{

		    Document document = retrieval.getDocument( id , dc);
		    tokenizer.tokenize(document);
		    documents.add(document);
		}

		return (Document[]) documents.toArray();
	}
	
	public Document[] getDocumentVectors(Long[] docInternalIDs) throws Exception {
		
		TagTokenizer tokenizer = new TagTokenizer(new FakeParameters(param));
		    
		DocumentComponents dc = new DocumentComponents(param);
		
//		List<Document> documents = new ArrayList<Document>();
		Document[] documents = new Document[docInternalIDs.length];
		int counter=0;
		for(long id : docInternalIDs)
		{

		    Document document = retrieval.getDocument(retrieval.getDocumentName((int) id), dc);
		    tokenizer.tokenize(document);
		    documents[counter++] = document;
//		    documents.add(document);
			
		}
		    
//		    return (Document[]) documents.toArray();
		return documents;
	}

	public double getTermCollectionProb(String term, boolean isStem, boolean smoothed) throws Exception
	{
		if(smoothed)
			return ((double)getTermCount(term, isStem)+0.5)/(collectionTermCount+1);
		return ((double)getTermCount(term, isStem))/(collectionTermCount);
	}
	
	
	public long getCollectionTermCount() throws Exception {
		 	
		return collectionTermCount; 
		
	}

	public long getGramCount(String query, boolean isStemmed) throws Exception {
		// TODO Auto-generated method stub
		query = "#od:1("+ query + ")";
		
        Node node = StructuredQuery.parse(query);
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
//        System.out.println(stat.maximumCount);
//        System.out.println(stat.nodeDocumentCount);
//        System.out.println(stat.nodeFrequency);
        
        return stat.nodeFrequency;
	}



	public double getTermCount(String query, boolean isStemmed) throws Exception {

		
        Node node = StructuredQuery.parse(query);
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
        
        return stat.nodeFrequency;
	}

}
