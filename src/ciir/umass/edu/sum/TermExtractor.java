package ciir.umass.edu.sum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;

import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.tupleflow.Parameters;

import ciir.umass.edu.qproc.KStemmer;
import ciir.umass.edu.qproc.NPExtractor;
import ciir.umass.edu.retrieval.utils.QueryProcessor;
import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;
import ciir.umass.edu.retrieval.utils.StringUtils;
import ciir.umass.edu.utilities.Sorter;

public class TermExtractor {

    int topD = 1000;
    int topT = 20;
    private Parameters param= null;
//    public ScoredDocument[] initialResults = null;
    public ArrayList<ScoredDocument> initialResults = null;
    
//    public TermExtractor(String indexPath) throws Exception {
//        //String index = "/mnt/nfs/work2/ashishjain/adobe/IbrahimData/indexes/NovIndex";
//        this.extractor = new DSPApprox();
//        this.se = new GalagoSearchEngine(indexPath);
//        // te = new TermExtractor(e, );
//
//    }
    
    public TermExtractor(String args) throws Exception {
        //String index = "/mnt/nfs/work2/ashishjain/adobe/IbrahimData/indexes/NovIndex";
        Parameters p = Parameters.parseFile(args);
    	this.param = p;
    	this.extractor = new DSPApprox();
        this.se = new GalagoSearchEngine(param);
        // te = new TermExtractor(e, );

    }
    

    public TermExtractor(DSPApprox e, GalagoSearchEngine se) {
        this.extractor = e;
        this.se = se;

    }


    public List<String> getResults(String query, boolean hashTag) throws Exception{
        initialResults = new ArrayList<ScoredDocument>();
        tm = new TreeMap<String, Integer>();
        Hierarchy.usePhrases = true;
        Hierarchy.usePhrasesOnly = true;
        List<String> S = new ArrayList<String>();
        List<TopicTerm> terms = extract(query, topD, topT, hashTag);
        for(int i=0;i<terms.size();i++) {
            S.add(terms.get(i).term);
        }
        return S;
    }
    public List<String> getDocuments(String query, int topDocs, String field) throws Exception{
        ScoredDocument[] r = null;
        String[] qProcess = query.split("\\s+");
        String reformQuery=qProcess[0];
        for (int j=1;j<qProcess.length;j++){
            if (qProcess[j].contentEquals(qProcess[j-1])){
                continue;
            }
            else
                reformQuery = reformQuery + " " + qProcess[j];
        }
        List<String> S = new ArrayList<String>();
        
        /////////////////
//        System.out.println("number of initial results: " + initialResults.length);
//        System.out.println("First initial results info");
//        System.out.println("documentNAme : "+initialResults[0].documentName);
//        System.out.println( "rank: " + initialResults[0].rank);
//        System.out.println( "score: " + initialResults[0].score);
//        System.out.println( "document: " + initialResults[0].document);
        
        /////////////
        
        r = se.runQuery(QueryProcessor.generateSDMFieldQuery(reformQuery, field), topDocs , initialResults);
//        r = se.runQuery(QueryProcessor.generateSDMFieldQuery(reformQuery.trim(), field), topDocs);
        int size = Math.min(topD, r.length);
        Long[] docIDs = new Long[size];
        for(int j=0;j<r.length;j++)
        {
            docIDs[j] = r[j].document;
            String text = se.getDocumentText(docIDs[j], field);
            S.add(text);
        }
        S.add(reformQuery);
        return S;
    }
    public String getPhraseCount(String query) throws Exception{
        Integer count = 0;
        if (tm.containsKey(query))
            count = tm.get(query);
        return Integer.toString(count);
    }

    /**
         * @param args
         * @throws IOException
         */
	public static void main(String[] args) throws IOException {

		/*String queryFile = DataSource.stemmedQueryFile;
		int nTopicTerms = 500;
		String topicTermFile = "";
		String hDirectory = "";
		int topD = 50;
		int method = 0;//Lawrie & Croft, 2003.
		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].compareTo("-query") == 0)
				queryFile = args[++i];
			else if(args[i].compareTo("-d") == 0)
				topD = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-t") == 0)
				nTopicTerms = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-m") == 0)
				method = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-dir") == 0)
				hDirectory = FileUtils.makePathStandard(args[++i]);
			else if(args[i].compareTo("-save") == 0)
				topicTermFile = args[++i];
			else if(args[i].compareTo("-qt") == 0)
				includeQueryTerm = true;
			else if(args[i].compareTo("-col") == 0)
				index = args[++i];
		}		*/
		
		
		Parameters p = Parameters.parseArgs(args);
		
		String index = p.getAsString("index"); //"/Users/mostafakeikha/studies_bigFiles/postdoc/twitter/index";
		try {
			DSPApprox e = new DSPApprox();	
			TermExtractor te = new TermExtractor(e, new GalagoSearchEngine(index));
			//te.run(queryFile, nTopicTerms, hDirectory, topicTermFile);			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int topD = 1000;
			int topT = 50;
			Hierarchy.usePhrases = true;
			Hierarchy.usePhrasesOnly = true;
			do {
				System.out.print("query: ");
				String text = br.readLine();
				if(text.compareToIgnoreCase("exit") == 0)
					break;
				
				if(text.indexOf("=") != -1)
				{
					String[] s = text.split("=");
					if(s[0].compareToIgnoreCase("d") == 0)
						topD = Integer.parseInt(s[1]);
					else if(s[0].compareToIgnoreCase("t") == 0)
						topD = Integer.parseInt(s[1]);
					else if(s[0].compareToIgnoreCase("p") == 0)
					{
						Hierarchy.usePhrases = true;
						Hierarchy.usePhrasesOnly = false;
					}
					else if(s[0].compareToIgnoreCase("po") == 0)
					{
						Hierarchy.usePhrases = true;
						Hierarchy.usePhrasesOnly = true;
					}
					else if(s[0].compareToIgnoreCase("u") == 0)
						Hierarchy.usePhrases = false;
					else if(s[0].compareToIgnoreCase("th") == 0)
						NPExtractor.threshold = Double.parseDouble(s[1]);
					else if(s[0].compareToIgnoreCase("pr") == 0)
						Hierarchy.printRetDoc = true;
					else if(s[0].compareToIgnoreCase("pp") == 0)
						Hierarchy.printPhrase = true;
				}
				else
				{

					ScoredDocument[] r = null;
					System.out.println(text);
//					System.out.println("Came here");
					String newQ = text.replaceAll("#", "");
        				newQ.trim();
					System.out.println("Query: " + newQ);
//					r = se.runQuery(QueryProcessor.generateMRFQuery(text), 20);
                        		//se.runQuery(text, 1000);
                        		/*
                        		int[] docIDs = new int[r.length];
                        		for(int z=0;z<r.length;z++) {
						System.out.println(r[z].document);
                            			docIDs[z] = r[z].document;
					}

                        		ParsedDocument[] pd = se.getParsedDocuments(docIDs);
					Pattern p = Pattern.compile("<TWEET>(.+?)</TWEET>");
                        		for(int z=0;z<pd.length;z++) {
                            			//System.out.println(pd[z].content);
						Matcher m = p.matcher(pd[z].content);
						m.find();
						System.out.println(m.group(1));
					}
					*/
					List<TopicTerm> terms = te.extract(text, topD, topT, false);
                    			List<String> S = new ArrayList<String>();
                    for(int i=0;i<terms.size();i++) {
                        S.add(terms.get(i).term);
                    }
					for(int i=0;i<terms.size();i++) {
//                      FeatureExtractor fe = new FeatureExtractor(se);
//                      double [] scores = fe.run(terms.get(i).term, 1000, S);
						System.out.println((i+1) + "\t" + terms.get(i).term + "\t" + terms.get(i).weight);
//                      for(int z=0;z< scores.length;z++)
//                      System.out.print(scores[z] + " ");
                        System.out.println();
                    }
				}
			}while(true);
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	public static boolean includeQueryTerm = false;
	
	protected KStemmer stemmer = new KStemmer();
	protected DSPApprox extractor = null;
	public static GalagoSearchEngine se = null;
    public TreeMap<String, Integer> tm = null;

	public List<TopicTerm> extract(String query, int topD, int topTerm, boolean hashTag) throws Exception
	{
		List<TopicTerm> terms = null;
		try{
            Hierarchy h = null;
            if(!hashTag) {
			    h = new Hierarchy(se, false, tm);
			    h.estimate(query, topD , initialResults);
            }
            else {
                h = new Hierarchy(se, true, tm);
                h.estimate(query, topD , initialResults);
            }
            
			String q = stemmer.stem(query);			
			terms = extractor.generateTopicTerms(q, h, topTerm);			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return terms;
	}
	
	//not in use
	public void run(String queryFile, int topTerm, String hDir, String outputFile) throws Exception
	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
			List<String> qids = new ArrayList<String>();
			List<String> qtext = new ArrayList<String>();
			
			//read queries
			QueryProcessor.readIndriQueryFile(queryFile, qids, qtext);
			List<Integer> numTopics = new ArrayList<Integer>();
			int sum = 0;
			int nq = 0;
			for(int i=0;i<qids.size();i++)
			{
				String hFile = hDir + "q-" + qids.get(i);
				File f = new File(hFile);
				if(!f.exists())
					continue;
				
				//load hierarchy statistics
				System.out.println("model: " + hFile);
				Hierarchy h = new Hierarchy();
				h.load(hFile);
				
				String q = qtext.get(i);
				String[] qt = q.split("\\s+");
				HashSet<String> qts = new HashSet<String>();
				for(int k=qt.length-1;k>=0;k--)
					qts.add(qt[k]);
				
				List<TopicTerm> output = extractor.generateTopicTerms(q, h, topTerm);
				for(int j=0;j<output.size();j++)
				{
					String tt = output.get(j).term;
					if(includeQueryTerm)
					{
						String[] ss = tt.split("\\s+");
						boolean flag = true;
						for(int k=0;k<ss.length&&flag;k++)
							if(!qts.contains(ss[k]))
								flag = false;
						
						if(flag)//yes, the query subsums the topic ==> discard this topic
						{
							output.remove(j);
							j--;
						}
						else
						{
							HashSet<String> hs = new HashSet<String>();
							for(int k=0;k<ss.length;k++)
								hs.add(ss[k]);
							for(int k=qt.length-1;k>=0;k--)
							{
								if(!hs.contains(qt[k]))
									tt = qt[k] + " " + tt;
							}
							output.get(j).term = StringUtils.quote(tt);
						}
					}
					else
						output.get(j).term = StringUtils.quote(tt);
				}
				
				//save reformulation file
				out.write(qids.get(i) + "\t" + "0" + "\t" + StringUtils.quote(q));
				out.newLine();
				for(int j=0;j<output.size();j++)
				{
					out.write(qids.get(i) + "\t" + output.get(j).weight + "\t" + output.get(j).term);
					out.newLine();
				}
				
				numTopics.add(output.size());
				sum += output.size();
				if(output.size() > 0)
					nq++;
			}
			//print stats
			int[] idx = Sorter.sort(numTopics, true);
			double min = numTopics.get(idx[0]);
			double max = numTopics.get(idx[idx.length-1]);
			double median = numTopics.get(idx[idx.length/2]);
			double mean = ((double)sum)/numTopics.size();
			out.write("# min: " + min);
			out.newLine();
			out.write("# max: " + max);
			out.newLine();
			out.write("# mean: " + mean);
			out.newLine();
			out.write("# median: " + median);
			out.newLine();
			out.write("# nq=" + nq);
			out.newLine();
			int[] stats = new int[numTopics.get(idx[idx.length-1])+1];
			for(int i=0;i<stats.length;i++)
				stats[i] = 0;
			for(int i=0;i<numTopics.size();i++)
				stats[numTopics.get(i)]++;
			for(int i=0;i<stats.length;i++)
			{
				out.write("# " + i + "\t" + stats[i]);
				out.newLine();
			}

			out.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
}
