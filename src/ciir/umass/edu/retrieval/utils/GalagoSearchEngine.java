package ciir.umass.edu.retrieval.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private KrovetzStemmer stemmerTerm ;
    private KStemmer stemmerSentence;
	private TagTokenizer tokenizer; 

	private DocumentComponents dc;

	
	public static Parameters createParameters(String col)
	{
		Parameters p = new Parameters();
		p.set("index", col);
		return p;
	}
	
	public GalagoSearchEngine(String col) throws Exception {
		
		this(createParameters(col));
	    
	}

	public GalagoSearchEngine(Parameters p) throws Exception {
		 retrieval = RetrievalFactory.instance(p);
		 param = p;

		stemmerTerm = new KrovetzStemmer();
		stemmerSentence = new KStemmer();
		tokenizer = new TagTokenizer(new FakeParameters(param));
		dc = new DocumentComponents(param);
			
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

	public Document[] getDocumentVectors(String[] docExternalIDs , String field , HashMap<String, String> stem2original ) throws Exception {
		
		
		List<Document> documents = new ArrayList<Document>();
		
		for(String id : docExternalIDs)
		{
		    documents.add(getDocumentVector(id, field , stem2original));
		}

		return (Document[]) documents.toArray();
	}
	

	public Document getDocumentVector(String docExternalID, String field , HashMap<String, String> stem2original) throws Exception {
		
		
		Document document = retrieval.getDocument( docExternalID , dc);
		
		String textInField = getTextInField(document.text, field);
		Document doc = tokenizer.tokenize(textInField);



		for (int i = 0; i < doc.terms.size() ; i++) {
			String term = doc.terms.get(i);
			String stemmed = stemmerTerm.stem(term);
			doc.terms.set(i, stemmed);
			if(!term.equals(stemmed) && !stem2original.containsKey(stemmed))
			{
				stem2original.put(stemmed, term);
			}
		}
		
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

	public Document[] getDocumentVectors(Long[] docInternalIDs, String field ,  HashMap<String, String> stem2original ) throws Exception {

		Document[] documents = new Document[docInternalIDs.length];
		int counter=0;
		for(long id : docInternalIDs)
		{

		    String documentName = retrieval.getDocumentName((int) id);
			
		    documents[counter++] = getDocumentVector(documentName, field , stem2original);
		}
		
		return documents;
	}

	
	public double getTermCollectionProb(String term, boolean isStem, boolean smoothed, String  field, HashMap<String, String> stem2original) throws Exception
	{
		if(smoothed)
			return ( (double)getTermCount(term, isStem, field , stem2original )+0.5)/(collectionTermCount+1);
		return ( (double)getTermCount(term, isStem , field , stem2original ))/(collectionTermCount);
	}
	
	
	public long getCollectionTermCount() throws Exception {
		 	
		return collectionTermCount; 
		
	}

	public long getGramCount(String query, boolean isStemmed, String field , HashMap<String, String> stem2original) throws Exception {
		
		String[] strs = query.split(" ");
		
		String unigram = "";
		String term = "";
		for(int i=0;i<strs.length;i++)
		{
			
			term = strs[i];
			if(stem2original.containsKey(term))
				term = stem2original.get(term);
			
			unigram += term + "." + field + " ";
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



	public double getTermCount(String query, boolean isStemmed, String field, HashMap<String, String> stem2original) throws Exception {

		if(stem2original.containsKey(query))
			query = stem2original.get(query);
		
		query = query + "." + field;
        Node node = StructuredQuery.parse(query);
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
        
        return stat.nodeFrequency;
	}

	public double testFunction1() throws Exception
	{
		Node node = StructuredQuery.parse("#extents:computing:part=field.krovetz.tweet()");
        node.getNodeParameters().set("queryType", "count");
        node = retrieval.transformQuery(node,  param);

        NodeStatistics stat = retrieval.getNodeStatistics(node);
        
        
        return stat.nodeFrequency;
        
	}
	
	public double testFunction2() throws Exception
	{
		Node node = StructuredQuery.parse("#extents:computing:part=field.tweet()");
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
		
//		se.getDocumentVector("20091114000000_2", "tweet").terms.toString()
//		
//		System.out.println(se.getTermCount("computing", true, "tweet"));
		
		
		
//		System.out.println(se.testFunction1());
//		System.out.println(se.testFunction2());
		
		
	}

}
