package ciir.umass.edu.retrieval.utils;

import java.util.Hashtable;

public class DataSource {
	public String[] stem_index = new String[]{
			"/mnt/nfs/work3/vdang/indexes/clueweb09-TREC-B-nostop",//on sydney
			"/mnt/nfs/work2/vdang/indexes/clue-log",
			"/mnt/nfs/work2/vdang/indexes/msn-log",
			"/mnt/nfs/work2/vdang/indexes/aol-log",
			"/mnt/nfs/work2/vdang/indexes/freebase-2013-06-30/topic-description",
			"/mnt/nfs/work2/vdang/indexes/freebase-2013-06-30/topic-name",
			"/mnt/nfs/work2/vdang/indexes/wiki-Jan-2012-full"
			};
	
	public Hashtable<String, Integer> stem_collections_mapping = new Hashtable<String, Integer>();
	
	protected DataSource()
	{
		stem_collections_mapping.put("clueB", 0);
		stem_collections_mapping.put("clue-log", 1);
		stem_collections_mapping.put("msn-log", 2);
		stem_collections_mapping.put("aol-log", 3);
		stem_collections_mapping.put("freebase-desc", 4);
		stem_collections_mapping.put("freebase-name", 5);
		stem_collections_mapping.put("wiki", 6);
	}
	
	private static DataSource ds = new DataSource();
	
	public static DataSource get()
	{
		return ds;
	}
	
	public String getIndex(String collection)
	{
		if(stem_collections_mapping.containsKey(collection))
			return stem_index[stem_collections_mapping.get(collection)];
		return collection;
	}
	
	//public static String rankingFile = "/mnt/nfs/work2/vdang/retrieval/adhoc/result/clueB/wt.1-200.catB.ql.spam60.fracstops1.run";
	//public static String stemmedQueryFile = "/mnt/nfs/work2/vdang/retrieval/adhoc/param/clueB/wt.1-200.catB.stem.qry";
	//public static String modifiedJudgmentFile = "/mnt/nfs/work2/vdang/qrels/diversity/proprietary/wt.catB.1-200.qrels";
	//public static String trecJudgmentFile = "/mnt/nfs/work2/vdang/qrels/diversity/wt.catB.1-200.qrels";
	public static String queryFile = "/mnt/nfs/work3/vdang/diversification/query/wt.1-200.catB.qry";
	public static String stemmedQueryFile = "/mnt/nfs/work3/vdang/diversification/query/wt.1-200.catB.stem.qry";
	public static String topicDirectory = "/mnt/nfs/work3/vdang/diversification/topic/";
	public static String rankingFile = "/mnt/nfs/work3/vdang/diversification/adhoc/runs/ql/wt.1-200.clueb.run";
	public static String rankingDirectory = "/mnt/nfs/work3/vdang/diversification/adhoc/runs/";
	public static String trecJudgmentFile = "/mnt/nfs/work3/vdang/diversification/qrel/wt.catB.1-200.qrels";
	public static String modifiedJudgmentFile = "/mnt/nfs/work3/vdang/diversification/qrel/proprietary/wt.catB.1-200.qrels";
	public static String hierarchyStatsDirectory = "/mnt/nfs/work3/vdang/diversification/cache/hierarchy/";
	public static String docAspectRelevanceDirectory = "/mnt/nfs/work3/vdang/diversification/cache/dar/";
	public static String diversityRunDirectory = "/mnt/nfs/work3/vdang/diversification/runs/";
	public static String docDump4LDA = "/mnt/nfs/work3/vdang/diversification/cache/doc4LDA/";
	
}
