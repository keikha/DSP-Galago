package ciir.umass.edu.sum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.ScoredPassage;

import ciir.umass.edu.qproc.KStemmer;
import ciir.umass.edu.qproc.NPExtractor;
import ciir.umass.edu.qproc.NounPhrase;
import ciir.umass.edu.qproc.POSTagger;
import ciir.umass.edu.qproc.Stopper;
import ciir.umass.edu.retrieval.dts.LanguageModel;
import ciir.umass.edu.retrieval.dts.Ranking;
import ciir.umass.edu.retrieval.utils.DataSource;
import ciir.umass.edu.retrieval.utils.IndriSearchEngine;
import ciir.umass.edu.retrieval.utils.QueryProcessor;
import ciir.umass.edu.retrieval.utils.GalagoSearchEngine;
import ciir.umass.edu.utilities.SimpleMath;
import ciir.umass.edu.sum.feature.FeatureExtractor;

public class Hierarchy {

	class Neighbor {
		public int idx = -1;
		public float weight = 0;
		public Neighbor(int idx, float weight)
		{
			this.idx = idx;
			this.weight = weight;
		}
	}
	class Markup {
		int start = -1;
		int end = -1;
		Markup(int start, int end)
		{
			this.start = start;
			this.end = end;
		}
	}
	class DocumentPhraseVector
	{
		List<Markup> positions = new ArrayList<Markup>();
		List<String> phrases = new ArrayList<String>();
		public void add(String phrase, Markup pos)
		{
			phrases.add(phrase);
			positions.add(pos);
		}
	}
	
	//parameters
	public static int minDocCount = 2;
	public static int minCharCount = 2;
	public static int windowSize = 5;
	public static int distToQueryTerm = 8;
	public static boolean usePhrases = false;
	public static boolean usePhrasesOnly = false;
	
	///////////////////////////////
	public static boolean printRetDoc = false;
	public static boolean printPhrase = false;
	
	
	//local variables
	private GalagoSearchEngine se = null;
	private Hashtable<String, Double> queryLanguageModel = null;
	private String[] vocabs = null;//all vocabulary in the query model
	private List<Integer> topicTerms = null;//all potential topic terms --> a subset of vocabulary
	private Hashtable<String, Integer> topicTermMap = null;
	private Neighbor[][] coocurStats = null;//co-occurrence between two terms in the vocab within a window of text
	private double[] topicality = null;//topicality score for the potential topic terms
	//for phrases
	private HashMap<String, Integer> vocabMinDistanceToQT = null;
	
	private KStemmer st = new KStemmer();
	private POSTagger tagger = new POSTagger();
	private NPExtractor npe = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String col = "clueB";
		String queryFile = DataSource.stemmedQueryFile;
		String runFile = DataSource.rankingFile;
		int topD = 50;
		int qid = -1;
		String outputHierarchyFile = "";
		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].compareTo("-query") == 0)
				queryFile = args[++i];
			else if(args[i].compareTo("-run") == 0)
				runFile = args[++i];
			else if(args[i].compareTo("-d") == 0)
				topD = Integer.parseInt(args[++i]);

			//optional
			else if(args[i].compareTo("-col") == 0)
				col = args[++i];
			else if(args[i].compareTo("-i") == 0)
				qid = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-save") == 0)
				outputHierarchyFile = args[++i];
			else if(args[i].compareTo("-ws") == 0)
				Hierarchy.windowSize = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-dq") == 0) 
				Hierarchy.distToQueryTerm = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-md") == 0) 
				Hierarchy.minDocCount = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-p") == 0)
				Hierarchy.usePhrases = true;
		}
		
		try {
//			IndriSearchEngine se = new IndriSearchEngine(col);
			GalagoSearchEngine se = new GalagoSearchEngine(col);
			
			//read queries
			List<String> qids = new ArrayList<String>();
			List<String> qtext = new ArrayList<String>();
			QueryProcessor.readIndriQueryFile(queryFile, qids, qtext);

			//99="satellite"
			int qidx = qids.indexOf(qid+"");
			String query = qtext.get(qidx);
			System.out.println("q = " + query);

			//read rankings
			List<Ranking> rl = QueryProcessor.readIndriRankingFile(runFile, topD, true);
			int ridx = -1;
			for(int i=0;i<rl.size() && ridx==-1;i++)
				if(rl.get(i).getQueryID().compareTo(qid+"") == 0)
					ridx = i;
			if(ridx == -1)
			{
				System.out.println("No documents retrieved for this query.");
				return;
			}
			
			Ranking r = rl.get(ridx);
			String[] docno = new String[r.size()];
			double[] scores = new double[r.size()];
			for(int i=0;i<r.size();i++)
			{
				docno[i] = r.get(i).docID;
				scores[i] = r.get(i).score;
			}
			
			List<String> queryTerms = new ArrayList<String>();//already stemmed
			String[] s = query.split(" ");
			for(int i=0;i<s.length;i++)
				queryTerms.add(s[i]);
			
			Hierarchy h = new Hierarchy(se);
			h.estimate(queryTerms, docno, scores);
			h.save(outputHierarchyFile);
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	public Hierarchy()
	{
	}
	public Hierarchy(GalagoSearchEngine se)
	{
		this.se = se;
		npe = new NPExtractor(se);
	}	
	
	public void estimate(String query, int topD) throws Exception
	{
		ScoredDocument[] r = se.runQuery(QueryProcessor.generateMRFQuery(query), topD);
		//ScoredExtentResult[] r = se.runQuery("#1(" + query + ")", topD);
		
		//get docid and score
		int size = Math.min(topD, r.length);
		Long[] docIDs = new Long[size];
		double[] scores = new double[size];
		for(int j=0;j<r.length;j++)
		{
			docIDs[j] = r[j].document;
			double score = r[j].score;
			if(r[j].score < 0)
				score = Math.exp(score);
			else
				score = Double.MIN_VALUE;
			scores[j] = score;
		}
		String[] qterms = query.split("\\s+");
		List<String> qts = new ArrayList<String>();
		for(int i=0;i<qterms.length;i++)
			qts.add(qterms[i]);
		estimate(qts, docIDs, scores);
	}
	public void estimate(List<String> qTerms, String[] docExternalIDs, double[] docScores) throws Exception
	{
		Long[] internalIDs = se.docInternalIDs(docExternalIDs);
		estimate(qTerms, internalIDs, docScores);
	}
	public void estimate(List<String> qTerms, Long[] docInternalIDs, double[] docScores) throws Exception
	{
		Document[] dvs = se.getDocumentVectors(docInternalIDs);
		estimate(qTerms, dvs, docScores);
	}
	private void estimate(List<String> qTerms, Document[] dvs, double[] scores) throws Exception
	{
		vocabMinDistanceToQT = new HashMap<String, Integer>();
		LanguageModel[] dlms = new LanguageModel[dvs.length];//document language models
		DocumentPhraseVector[] dpvs = new DocumentPhraseVector[dvs.length];
		double[] weights = new double[dlms.length];//weights for document language models
		
		double sumDocScore = 0.0;
		for(int i=0;i<dlms.length;i++)
		{
			dpvs[i] = new DocumentPhraseVector();

			dlms[i] = buildDocumentLM(dvs[i], qTerms, vocabMinDistanceToQT, dpvs[i]);
			
			if(dlms[i]==null)
				continue;
			
			weights[i] = scores[i];
			sumDocScore += weights[i];
		}
		//compute the posterior
		double docPrior = 1.0;
		for(int i=0;i<dlms.length;i++)
			weights[i] = (weights[i]/sumDocScore) * docPrior;
		
		//record (stemmed) query language model's vocabulary
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		for(int i=0;i<dlms.length;i++)
		{
			if(dlms[i]==null)
				continue;
			List<String> ws = dlms[i].getVocabs();
			for(int j=0;j<ws.size();j++)
				ht.put(ws.get(j), 1);
		}
		
		//estimate query language model (term distribution)
		System.out.print("Estimating query language model... ");
		queryLanguageModel = estimateQueryLanguageModel(ht, dlms, weights);//no smoothing for now
		System.out.println("[Done]");
		
		//construct the hierarchy's vocabulary from the query model's terms
		System.out.print("Extracting vocabulary... ");
		Hashtable<String, Integer> vocabIndex = extractVocabulary(queryLanguageModel, dlms);
		vocabs = new String[vocabIndex.keySet().size()];
		for(String key : vocabIndex.keySet())
			vocabs[vocabIndex.get(key).intValue()] = key;
		System.out.println("[Done][#" + vocabs.length + "]");
		
		//extract topic terms and compute their document-level similarity
		System.out.print("Identifying topic terms... ");
		topicTerms = new ArrayList<Integer>();
		topicTermMap = new Hashtable<String, Integer>();
		extractTopicTerms(vocabIndex, qTerms, topicTerms, topicTermMap);
		System.out.println("[Done][#" + topicTerms.size() + "]");
		
		//compute vocabulary co-occurence statistics
		System.out.print("Estimating co-occurrence statistics... ");
		coocurStats = extractTermCoocurrenceStats(vocabIndex, dvs, dpvs);
		System.out.println("[Done]");
		
		System.out.print("Pre-computing topic terms' topicality... ");
		computeTermTopicality();
		System.out.println("[Done]");
	}

	/**buildDocumentLM
	 * Build document language model from an input document. Note that all terms in [dv] were stemmed. Stop-words are removed.
	 * Record minimum distance for each vocab term to the query terms
	 * @param dv
	 * @return
	 */
	private LanguageModel buildDocumentLM(Document dv, List<String> queryTerms, HashMap<String, Integer> minDistanceToQT, DocumentPhraseVector dpv)
	{
		
		HashMap<String, Long> ngramFreq = new HashMap<String, Long>();
		LanguageModel lm = new LanguageModel();
		
		List<Integer> qTermPos = new ArrayList<Integer>();
		for(int j=0;j<dv.terms.size();j++)
			if(queryTerms.contains(dv.terms.get(j)))
				qTermPos.add(j);
		
		if(usePhrases)
		{
			String content = "";
			for(int i=0;i<dv.terms.size();i++)
			{
				String stem = dv.terms.get(i);
				content += stem + ((i==dv.terms.size()-1)?"":" ");
			}
			
			if(printRetDoc)
				System.out.println("tweet: " + content);
			
			if(content.isEmpty())
				return lm;
			//FeatureExtractor fe = new FeatureExtractor(se);
			List<NounPhrase> nps = tagger.extract(content);
			for(int i=0;i<nps.size();i++)
			{
				if(Stopper.getInstance().isStop(nps.get(i).text) || filter(nps.get(i).text))
				{
					nps.remove(i);
					i--;
				}
				//else
					//fe.run(nps.get(i).text, 200);
			}
			//List<NounPhrase> nps = npe.extract(content);
			if(printPhrase)
			{
				for(int i=0;i<nps.size();i++)
					System.out.println("phrase: " + nps.get(i).text);
			}
			
			for(int i=0;i<nps.size();i++)
			{
				NounPhrase np = nps.get(i);
				if(!usePhrasesOnly || np.text.indexOf(" ") != -1)
				{
					add(np.text, ngramFreq);
					updateMin(minDistanceToQT, np.text, minDist(qTermPos, np.start, np.end));
					dpv.add(np.text, new Markup(np.start, np.end));
				}
			}
		}
		else
		{
			for(int i=0;i<dv.terms.size();i++)
			{
				String stem = dv.terms.get(i);
				if(!Stopper.getInstance().isStop(stem))//in the full-stop word list
				{
					add(stem, ngramFreq);
					updateMin(minDistanceToQT, stem, minDist(qTermPos, i, i));
				}
			}
		}
		
		lm.set(ngramFreq);
		return lm;
	}
	/**
	 * Estimate the query language model (relevance model)
	 * @param terms
	 * @param dlms
	 * @param weights
	 * @return
	 * @throws Exception
	 */
	private Hashtable<String, Double> estimateQueryLanguageModel(Hashtable<String, Integer> terms, LanguageModel[] dlms, double[] weights) throws Exception
	{
		Hashtable<String, Double> qlm = new Hashtable<String, Double>();
		for(Enumeration<String> e = terms.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();//stem
			if(key.compareTo("")==0 || key.compareToIgnoreCase("[OOV]")==0)
				continue;
			//get term probability given by the relevance model RM1 => query language model (no smoothing done at the moment)
			double p = 0.0;
			for(int j=0;j<dlms.length;j++)
				{
					if(dlms[j]==null)
						continue;
					p += dlms[j].probability(key) * weights[j];
				}
			qlm.put(key, p);
		}
		return qlm;
	}
	/**
	 * Select a subset of the query model terms to construct the vocabulary for the topic hierarchy.
	 * @param dlms
	 * @throws Exception
	 */
	private Hashtable<String, Integer> extractVocabulary(Hashtable<String, Double> qlm, LanguageModel[] dlms) throws Exception
	{
		int count = 0;
		Hashtable<String, Integer> vocabIndex = new Hashtable<String, Integer>();
		for(String key : qlm.keySet())
		{
			// construct the vocabulary
			// only consider as vocabulary those terms that:
			// (1) appear in at least two documents
			// (2) contains at least 2 characters
			// (3) is not a number
			int docCount = getDocCount(key, dlms);
			boolean isNumber = true;
			try {
				Double.parseDouble(key);
			}
			catch(Exception ex)
			{
				isNumber = false;
			}
			if(docCount >= minDocCount && key.length() >= minCharCount && !isNumber)
			{
				vocabIndex.put(key, count);
				count++;
			}
		}
		return vocabIndex;
	}
	/**
	 * Select a subset of the vocabulary to form topic terms.
	 * @param vocabIndex
	 * @param qTerms
	 * @param terms
	 * @param map
	 * @throws Exception
	 */
	private void extractTopicTerms(Hashtable<String, Integer> vocabIndex, List<String> qTerms, List<Integer> terms, Hashtable<String, Integer> map) throws Exception
	{
		for(String key : vocabIndex.keySet())
		{
			int dist = vocabMinDistanceToQT.get(key).intValue();
			if(dist <= distToQueryTerm)
			{
				terms.add(vocabIndex.get(key).intValue());
				map.put(key, terms.size()-1);
			}
		}
	}
	/**
	 * Compute vocabulary co-occurrence (window-based) statistics.
	 * @param vocabIndex
	 * @param dvs
	 * @param dpvs
	 * @return
	 */
	private Neighbor[][] extractTermCoocurrenceStats(Hashtable<String, Integer> vocabIndex, Document[] dvs, DocumentPhraseVector[] dpvs)
	{
		List<Hashtable<Integer, Float>> cooccurence = new ArrayList<Hashtable<Integer, Float>>();
		for(int i=0;i<vocabIndex.keySet().size();i++)
			cooccurence.add(new Hashtable<Integer, Float>());
		
		if(!usePhrases)
		{
			for(int i=0;i<dvs.length;i++)
			{
				Document dv = dvs[i];
				List<String> pos = dv.terms;
				for(int j=0;j<pos.size();j++)
				{
					String stem = dv.terms.get(j)
							;
					if(vocabIndex.containsKey(stem))//vocabIndex is a subset dv's terms
					{
						int idx_j = vocabIndex.get(stem).intValue();
						Hashtable<Integer, Float> h_j = cooccurence.get(idx_j);
						int end = j+windowSize;
						if(end > pos.size()-1)
							end = pos.size()-1;
						for(int k=j+1;k<=end;k++)
						{
							stem = dv.terms.get(k);
							if(vocabIndex.containsKey(stem))
							{
								int idx_k = vocabIndex.get(stem).intValue();
								Hashtable<Integer, Float> h_k = cooccurence.get(idx_k);
								
								if(h_j.containsKey(idx_k))
								{
									h_j.put(idx_k, h_j.get(idx_k).floatValue() + 1);
									h_k.put(idx_j, h_k.get(idx_j).floatValue() + 1);
								}
								else
								{
									h_j.put(idx_k, 1F);
									h_k.put(idx_j, 1F);
								}
							}
						}
					}
				}
			}
		}
		
		for(int i=0;i<dpvs.length;i++)
		{
			DocumentPhraseVector dpv = dpvs[i];//phrases are pre-sorted by their positions
			//DocumentVector dv = dvs[i];
			for(int j=0;j<dpv.positions.size();j++)
			{
				String phrase = dpv.phrases.get(j);
				if(vocabIndex.containsKey(phrase))
				{
					//proximities to other phrases/terms
					int idx_j = vocabIndex.get(phrase).intValue();
					Hashtable<Integer, Float> h_j = cooccurence.get(idx_j);
					for(int k=j+1;k<dpv.positions.size();k++)
					{
						if(dpv.positions.get(k).start - dpv.positions.get(j).end > windowSize)
							break;
						phrase = dpv.phrases.get(k);
						if(vocabIndex.containsKey(phrase))
						{
							int idx_k = vocabIndex.get(phrase).intValue();
							Hashtable<Integer, Float> h_k = cooccurence.get(idx_k);							
							if(h_j.containsKey(idx_k))
							{
								h_j.put(idx_k, h_j.get(idx_k).floatValue() + 1);
								h_k.put(idx_j, h_k.get(idx_j).floatValue() + 1);
							}
							else
							{
								h_j.put(idx_k, 1F);
								h_k.put(idx_j, 1F);
							}
						}
					}
					//proximities to terms
					/*if(!useWikiTitleAsConcept)
					{
						int start = dpv.positions.get(j).start - windowSize;
						if(start < 0) start = 0;
						int end = dpv.positions.get(j).end + windowSize;
						if(end > dv.positions.length-1) end = dv.positions.length-1;
						for(int k=start;k<=end;k++)
						{
							String stem = dv.stems[dv.positions[k]];
							if(vocabIndex.containsKey(stem))
							{
								int idx_k = vocabIndex.get(stem).intValue();
								Hashtable<Integer, Float> h_k = cooccurence.get(idx_k);							
								if(h_j.containsKey(idx_k))
								{
									h_j.put(idx_k, h_j.get(idx_k).floatValue() + 1);
									h_k.put(idx_j, h_k.get(idx_j).floatValue() + 1);
								}
								else
								{
									h_j.put(idx_k, 1F);
									h_k.put(idx_j, 1F);
								}
							}
						}
					}*/
				}
			}
		}
		
		//dump the term-coocurrence stats into a array to save memory
		Neighbor[][] stats = new Neighbor[vocabIndex.keySet().size()][];
		for(int i=0;i<stats.length;i++)
		{
			stats[i] = new Neighbor[cooccurence.get(i).keySet().size()];
			int t = 0;
			for(Integer j : cooccurence.get(i).keySet())
			{
				float w = cooccurence.get(i).get(j);
				stats[i][t] = new Neighbor(j.intValue(), w);
				t++;
			}
		}
		return stats;
	}
	/**
	 * Compute topicality for all potential topic terms
	 * @throws Exception
	 */
	private void computeTermTopicality() throws Exception
	{
		topicality = new double[topicTerms.size()];
		for(int i=0;i<topicTerms.size();i++)//loop through all topic terms
		{
			int t = topicTerms.get(i);
			double p1 = probability(vocabs[t], false);//relevance model probability
			//double p2 = se.getTermCollectionProb(vocabs[t], true, false);
			double p2 = getTermCollectionProbability(vocabs[t]);
			double kl = 0;
			if(p1 > 0.0 && p2 > 0.0)
				kl = p1 * SimpleMath.logBase2(p1/p2);
			topicality[i] = kl;
		}
	}

	public void save(String outputFile)
	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
			
			System.out.print("Saving... ");
			out.write("# Topic/Vocab terms");
			out.newLine();
			out.write(vocabs.length + "\t" + topicTermMap.size());
			out.newLine();
			for(int i=0;i<vocabs.length;i++)
			{
				out.write(vocabs[i]);
				Integer idx = topicTermMap.get(vocabs[i]);
				if(idx != null)//is a topic term
					out.write("\t" + topicality[idx.intValue()]);
				out.newLine();
			}
			
			out.write("# Co-occurrence matrix");
			out.newLine();
			for(int i=0;i<coocurStats.length;i++)
			{
				for(int j=0;j<coocurStats[i].length;j++)
					out.write(coocurStats[i][j].idx + " " + coocurStats[i][j].weight + ((j==coocurStats[i].length-1)?"":" "));
				out.newLine();
			}
			
			out.write("# Query model");
			out.newLine();
			for (String key : queryLanguageModel.keySet())
			{
				out.write(key + "\t" + queryLanguageModel.get(key).doubleValue());
				out.newLine();
			}
			
			out.close();
			System.out.print("[Done]");
		}
		catch(Exception e)
		{
			System.out.println("Error in HierarchyStatistics.save(): " + e.toString());
		}
	}
	public void load(String inputFile)
	{
		try {
			String content = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
			
			System.out.print("Loading... ");
			content = in.readLine();//comment("Topic/Vocab terms")
			content = in.readLine();
			String[] s = content.split("\t");
			int vocabCount = Integer.parseInt(s[0]);
			int topicTermCount = Integer.parseInt(s[1]);
			vocabs = new String[vocabCount];
			topicality = new double[topicTermCount];
			topicTerms = new ArrayList<Integer>();
			topicTermMap = new Hashtable<String, Integer>();
			int c = 0;
			for(int i=0;i<vocabCount;i++)
			{
				content = in.readLine();
				s = content.split("\t");
				vocabs[i] = s[0];
				if(s.length == 2)//topic terms
				{
					topicTerms.add(i);
					topicality[c] = Double.parseDouble(s[1]);
					topicTermMap.put(vocabs[i], c);
					c++;
				}					
			}
			if(c != topicTermCount)
			{
				System.out.println("HierarchyStatistics.load(): Topic term count inconsistent.");
				System.exit(1);
			}

			content = in.readLine();//comment ("Co-occurrence matrix")
			coocurStats = new Neighbor[vocabs.length][];
			for(int i=0;i<vocabs.length;i++)
			{
				content = in.readLine();
				if(content.compareTo("") != 0)
				{
					s = content.split(" ");
					coocurStats[i] = new Neighbor[s.length/2];
					for(int j=0;j<s.length;j+=2)
						coocurStats[i][j/2] = new Neighbor(Integer.parseInt(s[j]), Float.parseFloat(s[j+1]));
				}
				else
					coocurStats[i] = new Neighbor[0];
			}
	
			content = in.readLine();//"# Query model"
			queryLanguageModel = new Hashtable<String, Double>();
			while((content = in.readLine()) != null)
			{
				s = content.split("\t");
				queryLanguageModel.put(s[0], Double.parseDouble(s[1]));
			}
			System.out.println("[Done]");
			in.close();
		}
		catch(Exception ex)
		{
			System.out.println("Error in HierarchyStatistics.load(): " + ex.toString());
			System.exit(1);
		}
	}
	
	public String getVocabulary(int vocabIndex)
	{
		return vocabs[vocabIndex];
	}
	public String[] getVocabulary()
	{
		return vocabs;
	}
	public List<Integer> getTopicTerms()
	{
		return topicTerms;
	}
	public double getTopicality(int vocabIndex)
	{
		Integer i = topicTermMap.get(vocabs[vocabIndex]);
		if(i == null)
		{
			System.out.println("Error in HierarchyStatistics::getTopicality(): Requesting topicality score for non-topical term.");
			System.exit(1);
		}
		return topicality[i.intValue()];				
	}
	//Give the input term's probability given by the query model
	public double probability(String term, boolean toStem)
	{
		String t = term.toLowerCase();
		if(toStem)
			t = st.stem(t);
		Double p = queryLanguageModel.get(t);
		if(p == null)
			return 0;
		return p;
	}
	/**
	 * Get co-occurrece statistics among terms: neighbors[i][j] = P(j|i)
	 * @return
	 */
	public Neighbor[][] getCooccurrence()
	{
		Neighbor[][] neighbors = new Neighbor[vocabs.length][];
		for(int i=0;i<vocabs.length;i++)
		{
			neighbors[i] = new Neighbor[coocurStats[i].length];
			double sum = 0;
			for(int j=0;j<coocurStats[i].length;j++)
			{
				neighbors[i][j] = new Neighbor(coocurStats[i][j].idx, coocurStats[i][j].weight);
				sum += coocurStats[i][j].weight;
			}
			if(sum > 0)
				for(int j=0;j<coocurStats[i].length;j++)
					neighbors[i][j].weight /= sum;			
		}
		return neighbors;
	}
	public Hierarchy getSubHierarchy(List<Integer> parentTopicTerms, List<Integer> excludeTerms) throws Exception
	{
		Hierarchy h = new Hierarchy(se);
		Hashtable<Integer, Integer> svocabs = new Hashtable<Integer, Integer>(); 
		for(int k=0;k<parentTopicTerms.size();k++)
		{
			int i = parentTopicTerms.get(k);
			for(int j=0;j<coocurStats[i].length;j++)
				if(!parentTopicTerms.contains(coocurStats[i][j].idx))//exclude the parent term
					if(excludeTerms == null)
						svocabs.put(coocurStats[i][j].idx, 0);
					else if(!excludeTerms.contains(coocurStats[i][j].idx))
						svocabs.put(coocurStats[i][j].idx, 0);
		}
		
		//vocabulary
		h.vocabs = new String[svocabs.keySet().size()];
		int i=0;
		Hashtable<Integer, Integer> map = new Hashtable<Integer, Integer>();
		for(int idx : svocabs.keySet())
		{
			h.vocabs[i] = vocabs[idx];
			map.put(idx, i);
			i++;
		}		
		
		//topic terms
		h.topicTerms = new ArrayList<Integer>();
		h.topicTermMap = new Hashtable<String, Integer>();
		List<Double> t = new ArrayList<Double>();
		for(i=0;i<topicTerms.size();i++)
		{
			Integer idx = map.get(topicTerms.get(i));
			if(idx != null)//it's in the restricted vocab
			{
				h.topicTerms.add(idx.intValue());
				h.topicTermMap.put(h.vocabs[idx.intValue()], h.topicTerms.size()-1);
				t.add(topicality[i]);
			}
		}
		h.topicality = new double[h.topicTerms.size()];		
		for(i=0;i<h.topicTerms.size();i++)
			h.topicality[i] = t.get(i);
		t.clear();
		
		//co-occurrence
		List<Hashtable<Integer, Float>> cooccurence = new ArrayList<Hashtable<Integer, Float>>();
		for(i=0;i<svocabs.keySet().size();i++)
			cooccurence.add(new Hashtable<Integer, Float>());
		
		//loop through the restricted vocab
		for(int idx : svocabs.keySet())
		{
			//loop through its neighbor
			for(int j=0;j<coocurStats[idx].length;j++)
			{
				Neighbor n = coocurStats[idx][j];
				//not all of the neighbors are in the restricted vocab
				Integer p = map.get(n.idx);
				if(p != null)
					cooccurence.get(map.get(idx).intValue()).put(p.intValue(), n.weight);
			}
		}
		
		h.coocurStats = new Neighbor[h.vocabs.length][];
		for(i=0;i<h.vocabs.length;i++)
		{
			if(cooccurence.get(i).keySet().size() == 0)
			{
				h.coocurStats[i] = new Neighbor[0];//FIXME: h.coocurStats[i]=null causes an exception in the main function 
				continue;
			}
			h.coocurStats[i] = new Neighbor[cooccurence.get(i).keySet().size()];
			int count = 0;
			for(Integer j : cooccurence.get(i).keySet())
			{
				h.coocurStats[i][count] = new Neighbor(j.intValue(), cooccurence.get(i).get(j).floatValue());
				count++;
			}
		}
		h.queryLanguageModel = queryLanguageModel;	
		return h;
	}
	
	public Hashtable<String, Double> getQueryLanguageModel()
	{
		return queryLanguageModel;
	}
	
	private boolean filter(String key) {
        	//check if stop word exist in string
        	String[] tokens = key.split("\\s+");
		if (tokens.length ==1)
            		return false;
        	TreeMap<String, Integer> t = new TreeMap();
       		Integer flag=0;
		for(int i=0;i<tokens.length; i++) {
            		if(t.containsKey(tokens[i])) {
                		flag=1;
                		break;
            		}
            		t.put(tokens[i], 1);
            		if(Stopper.getInstance().isStop(tokens[i])) {
                		flag=1;
                		break;
            		}
        	}
		t.clear();
        	if (flag==1)
            		return true;
       		else
            		return false;
    	}	
	private void add(String key, HashMap<String, Long> ht)
	{
		if(ht.get(key) == null)
			ht.put(key, 1L);
		else
			ht.put(key, ht.get(key).longValue() + 1);
	}
	private void updateMin(HashMap<String, Integer> ht, String key, int newValue)
	{
		Integer d = ht.get(key);
		if(d != null)
		{
			if(d.intValue() > newValue)
				ht.put(key, newValue);						
		}
		else
			ht.put(key, newValue);
	}
	/**
	 * Min distance from a word/phrase to query terms
	 * @param qTermPos
	 * @param start
	 * @param end
	 * @return
	 */
	private int minDist(List<Integer> qTermPos, int start, int end)
	{
		int min = 10000000;
		for(int i=start;i<=end;i++)
		{
			for(int j=0;j<qTermPos.size();j++)
			{
				int tmp = Math.abs(i - qTermPos.get(j));
				if(min > tmp)
					min = tmp;
			}
		}
		return min;
	}
	/**
	 * 
	 * @param STEMMED term
	 * @param dlms
	 * @return
	 * @throws Exception
	 */
	private int getDocCount(String term, LanguageModel[] dlms) throws Exception
	{
		int count = 0;
		for(int j=0;j<dlms.length;j++)
		{
			if(dlms[j].probability(term) > 0.0)
				count++;
		}
		return count;
	}
	/**
	 * Get collection probability of a term (single- or multi-word)
	 */
	private double getTermCollectionProbability(String stem) throws Exception
	{
		
		//System.out.println("term : " + stem);
		
		if(stem.indexOf(" ") == -1)//single-word term
			return se.getTermCollectionProb(stem, true, false);
		
		return ((double)se.getGramCount(stem, true))/se.getCollectionTermCount();//multi-word term
		
		//return ((double)wikiConcepts.get(stem).longValue())/se.getCollectionTermCount();//multi-word term
		//return ((double)se.getGramCount(stem, true))/se.getCollectionTermCount();//multi-word term
	}
}
