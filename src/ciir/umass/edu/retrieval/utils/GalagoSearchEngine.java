package ciir.umass.edu.retrieval.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lemurproject.galago.core.index.stats.FieldStatistics;
import org.lemurproject.galago.core.index.stats.NodeStatistics;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.Document.DocumentComponents;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.FakeParameters;
import org.lemurproject.galago.tupleflow.Parameters;

import ciir.umass.edu.qproc.KStemmer;


public class GalagoSearchEngine {

    private Retrieval retrieval ;
    private Parameters param ;
    private long collectionTermCount;
    private long collectionDocCount;
    private KrovetzStemmer stemmer ;
    
	public GalagoSearchEngine(String col) throws Exception {
		// TODO Auto-generated constructor stub
		retrieval = RetrievalFactory.instance(col);
		
		param = new Parameters();
//	    stemmer = new KStemmer();
		stemmer = new KrovetzStemmer();
	    
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

		 //////////////////
		 Node n = new Node();

		 n.setOperator("lengths");
		 n.getNodeParameters().set("part", "lengths");

		 FieldStatistics stat = retrieval.getCollectionStatistics(n);
		 collectionTermCount = stat.collectionLength;
		 collectionDocCount = stat.documentCount;

		 //////////////////////
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

//	public Document[] getDocumentVectors(String[] docExternalIDs) throws Exception {
//		
//		TagTokenizer tokenizer = new TagTokenizer(new FakeParameters(param));
//		    
//		DocumentComponents dc = new DocumentComponents(param);
//		
//		List<Document> documents = new ArrayList<Document>();
//		
//		for(String id : docExternalIDs)
//		{
//
//		    Document document = retrieval.getDocument( id , dc);
//		    tokenizer.tokenize(document);
//		    documents.add(document);
//		}
//
//		return (Document[]) documents.toArray();
//	}
	

	public Document getDocumentVector(String docExternalID, String field) throws Exception {
		
		//////////// to be finished 
		TagTokenizer tokenizer = new TagTokenizer(new FakeParameters(param));

		DocumentComponents dc = new DocumentComponents(param);
		
		Document document = retrieval.getDocument( docExternalID , dc);
		
		
//		tokenizer.tokenize(document);
		String textInField = getTextInField(document.text, field);
		System.out.println(textInField);
		String stemmed = stemmer.stem(textInField);
		System.out.println(stemmed);
		Document doc = tokenizer.tokenize(stemmed);
		


		return doc;
	}
	
	public String getTextInField(String text, String fieldName)
	{
		
	      if(text.contains("<"+ fieldName +">"))
	    	   return text.substring(text.indexOf("<" + fieldName +">")+fieldName.length()+2 , text.indexOf("</" + fieldName +">"));
	      else 
	    	  return "";
	}

	
	public String getDocumentText(String docName, String fieldName) throws IOException
	{
		
		DocumentComponents dc = new DocumentComponents(true, false, false);
	    
		assert retrieval.getAvailableParts().containsKey("corpus") : "Index does not contain a corpus part.";
		
	    Document document = retrieval.getDocument(docName, dc);
	    if (document != null) {
	      return getTextInField(document.text, fieldName);	    	   
	    }
	    
		return "";		
	}
	
	public String getDocumentText(long docID, String fieldName) throws IOException
	{
		String docName = retrieval.getDocumentName((int) docID);
		return getDocumentText(docName, fieldName);
	}
	public Document[] getDocumentVectors(Long[] docInternalIDs) throws Exception {
		
		TagTokenizer tokenizer = new TagTokenizer(new FakeParameters(param));
		    
		DocumentComponents dc = new DocumentComponents(true, false, false);
		
//		List<Document> documents = new ArrayList<Document>();
		Document[] documents = new Document[docInternalIDs.length];
		int counter=0;
		for(long id : docInternalIDs)
		{

		    Document document = retrieval.getDocument(retrieval.getDocumentName((int) id), dc);
		    tokenizer.tokenize(document);
		    System.out.println(document.terms.toString());
		    documents[counter++] = document;
//		    documents.add(document);
			
		}
		    
//		    return (Document[]) documents.toArray();
		return documents;
	}

	public double getTermCollectionProb(String term, boolean isStem, boolean smoothed, String  field) throws Exception
	{
		if(smoothed)
			return ((double)getTermCount(term, isStem, field)+0.5)/(collectionTermCount+1);
		return ((double)getTermCount(term, isStem , field))/(collectionTermCount);
	}
	
	
	public long getCollectionTermCount() throws Exception {
		 	
		return collectionTermCount; 
		
	}

	public long getGramCount(String query, boolean isStemmed, String field) throws Exception {
		
		String[] strs = query.split(" ");
		
		String unigram = "";
		for(int i=0;i<strs.length;i++)
		{
			unigram += strs[i] + "." + field + " ";
		}
		
		// TODO Auto-generated method stub
		query = "#od:1("+ unigram + ")";
		
        Node node = StructuredQuery.parse(query);
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
//        System.out.println(stat.maximumCount);
//        System.out.println(stat.nodeDocumentCount);
//        System.out.println(stat.nodeFrequency);
        
        return stat.nodeFrequency;
	}



	public double getTermCount(String query, boolean isStemmed, String field) throws Exception {

		query = query + "." +field;
        Node node = StructuredQuery.parse(query);
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
        
        return stat.nodeFrequency;
	}
	
	public static void main(String[] args) throws Exception {
		args = new String[1];
		String param = "/Users/mostafakeikha/studies_bigFiles/postdoc/twitter2/param.json";
		args[0]=param;
		
		Parameters p = new Parameters();
		p = p.parseArgs(args);
		
		GalagoSearchEngine se = new GalagoSearchEngine(p);
		
		System.out.println(se.getDocumentVector("20091114000000_2", "tweet").terms.toString());
		
		
	}

}
