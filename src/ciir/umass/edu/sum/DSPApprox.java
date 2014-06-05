package ciir.umass.edu.sum;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ciir.umass.edu.retrieval.utils.QueryProcessor;

/**
 * @author vdang
 * This class implement the DSPApprox algorithm for building a term hierarchy from a set of retrieved documents for any given query
 *   D. Lawrie and W.B. Croft. Generating hierarchical summaries for web searches. In Proceedings of SIGIR, pages 457458 , 2003.
 */
public class DSPApprox {

	//parameters
	public static int maxLevel = 1;	
	
	public DSPApprox()
	{
	}
	
	//EXPECT: stemmed query
	public List<TopicTerm> generateTopicTerms(String query, Hierarchy hStats, int nTopicTerms) throws Exception
	{
		List<String> queryTerms = new ArrayList<String>();
		String[] s = query.split(" ");
		for(int i=0;i<s.length;i++)
			queryTerms.add(s[i]);
		
		TreeNode h = new TreeNode();
		h.setText(query);
		buildTermHierarchy(h, 0, hStats, queryTerms, nTopicTerms);
		List<TopicTerm> output = new ArrayList<TopicTerm>();
		h.getBreathFirst(output);
		
		return output;
	}
	
	private void buildTermHierarchy(TreeNode t, int level, Hierarchy h, List<String> queryTerms, int nTopicTerms) throws Exception
	{
		List<Integer> terms = new ArrayList<Integer>();
		terms.addAll(h.getTopicTerms());
		if(terms.size() > 0)
		{
			Hierarchy.Neighbor[][] neighbors = h.getCooccurrence();
			double[] termPredictiveness = computeTermPredictiveness(terms, neighbors);			
			
			List<TreeNode> nodes = candidates(terms, termPredictiveness, neighbors, h, nTopicTerms, queryTerms);

			//free up memory
			termPredictiveness = null;
			neighbors = null;
			terms.clear();
			
			List<Integer> exclude = new ArrayList<Integer>();
			for(int i=0;i<nodes.size();i++)
				exclude.add(nodes.get(i).value());
			
			for(int i=0;i<nodes.size();i++)
			{
				TreeNode n = nodes.get(i);
				t.addChild(n);
				if(level < maxLevel-1)
				{
					List<Integer> parent = new ArrayList<Integer>();
					parent.add(n.value());
					buildTermHierarchy(n, level+1, h.getSubHierarchy(parent, exclude), queryTerms, nTopicTerms/2);
				}
			}
		}
	}
	private double[] computeTermPredictiveness(List<Integer> topicTerms, Hierarchy.Neighbor[][] cooccurrence)
	{
		Hashtable<Integer, Integer> dict = new Hashtable<Integer, Integer>();
		double[] p = new double[topicTerms.size()];
		for(int i=0;i<topicTerms.size();i++)
		{
			p[i] = 0;
			dict.put(topicTerms.get(i), i);
		}
		
		for(int i=0;i<cooccurrence.length;i++)//loop through *ALL (restricted) VOCABULARY*, not just topic terms
		{
			if(cooccurrence[i] != null)
			{
				for(int j=0;j<cooccurrence[i].length;j++)
				{
					Integer idx = dict.get(cooccurrence[i][j].idx); 
					if(idx != null)
						p[idx.intValue()] += cooccurrence[i][j].weight;
				}
			}
		}
		return p;
	}	
	private List<TreeNode> candidates(List<Integer> terms, double[] predictiveness, Hierarchy.Neighbor[][] neighbors, Hierarchy h, int nTopicTerms, List<String> queryTerms)
	{
		if(nTopicTerms > terms.size())
			nTopicTerms = terms.size();
		
		double[] termPredictiveness = new double[predictiveness.length];
		for(int i=0;i<predictiveness.length;i++)
			termPredictiveness[i] = predictiveness[i];
		
		List<Integer> candidates = new ArrayList<Integer>();
		Hashtable<Integer, Integer> map = new Hashtable<Integer, Integer>();
		for(int i=0;i<terms.size();i++)
		{
			map.put(terms.get(i), i);
			candidates.add(i);
		}
		
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		List<Integer> domSet = new ArrayList<Integer>();
		Hashtable<Integer, Float> coveredVertices = new Hashtable<Integer, Float>();
		do {
			//find the topic term with highest utility
			int which = -1;
			double max = -1000000;
			for(int c=0;c<candidates.size();c++)
			{
				int t = candidates.get(c);
				int i = terms.get(t);
				
				//exclude query terms
				if(queryTerms.contains(h.getVocabulary(i)))
					continue;
				
				double util = termPredictiveness[t] * h.getTopicality(i);//Math.abs(h.getTopicality(i));
				if(util > max)
				{
					max = util;
					which = c;
				}
			}
			if(which == -1 || max <= 0)
				break;
			
			int current = candidates.get(which);
			domSet.add(terms.get(current));//select this topic term
			candidates.remove(which);//and remove it from the candidate set
			
			//create a tree node for this selected term
			TreeNode nn = new TreeNode(terms.get(current), QueryProcessor.makeIndriFriendly(h.getVocabulary(terms.get(current))), max);
			nodes.add(nn);
			
			//update the set of covered vertices
			int v = terms.get(current);
			if(neighbors[v] != null)
			{
				for(int j=0;j<neighbors[v].length;j++)
				{
					Hierarchy.Neighbor d = neighbors[v][j];//a dominated vertex
					int t = d.idx;
					//fixme: thresholding???
					if(!coveredVertices.containsKey(t))//hasn't been covered before
					{
						if(neighbors[t] != null)
						{
							for(int k=0;k<neighbors[t].length;k++)
							{
								Integer idx = map.get(neighbors[t][k].idx);
								if(idx != null)//it is a candidate topic term -> down-weight it
									termPredictiveness[idx.intValue()] -= neighbors[t][k].weight;
							}
						}
						coveredVertices.put(t, d.weight);
					}
				}
			}
		}while(domSet.size() < nTopicTerms);
		return nodes;
	}
}
