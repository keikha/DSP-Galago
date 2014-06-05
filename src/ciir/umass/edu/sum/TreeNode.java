package ciir.umass.edu.sum;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ciir.umass.edu.utilities.SimpleMath;
import ciir.umass.edu.utilities.Sorter;

public class TreeNode {
	private TreeNode parent = null;
	private List<TreeNode> children = null;
	
	//For leaf node ONLY 
	private int value = -1;
	private String text = "";
	
	//extra data
	private double data = -1.0;
	
	//for hierarchical diversification
	public double vote = 0;
	public double seats = 0;//occupied (fraction of) seats
	public double quotient = 0;//quotient (Sainte Lague)
	public double[] relevanceScores = null;//(topic, documents) relevance (query-likelihood)
	public void makeUniform()
	{
		if(!isLeaf())
		{
			for(int i=0;i<children.size();i++)
				children.get(i).data = 1.0/children.size();
			
			for(int i=0;i<children.size();i++)
				children.get(i).makeUniform();
		}
	}
	public void init()
	{
		if(!isLeaf())
		{
			for(int i=0;i<children.size();i++)
			{
				TreeNode c = children.get(i);
				c.vote = c.data;
				c.seats = 0;
			}
			computeQuotients();
			for(int i=0;i<children.size();i++)
				children.get(i).init();
		}
	}
	public void computeQuotients()
	{
		double max = -1000;
		int which = -1;	
		//double sq = 0;
		for(int i=0;i<children.size();i++)
		{
			TreeNode c = children.get(i);
			c.quotient = ((double)c.vote) / (2*c.seats + 1);
			//sq += c.quotient;
			if(c.quotient > max)
			{
				max = c.quotient;
				which = i;
			}			
		}
		this.value = which;//node with largest quotient
		//for(int i=0;i<children.size();i++)
			//children.get(i).quotient /= sq;
	}
	public void updateSeats(int doc)
	{
		double sum = 0;
		for(int i=0;i<children.size();i++)
			sum += children.get(i).relevanceScores[doc];
		if(sum > 0)
			for(int i=0;i<children.size();i++)
				children.get(i).seats += children.get(i).relevanceScores[doc]/sum;
	}
	public void getBreathFirst(List<TopicTerm> list)
	{
		if(!isLeaf())
		{
			for(int i=0;i<children.size();i++)
			{
				TopicTerm t = new TopicTerm(children.get(i).text, children.get(i).data);
				list.add(t);
			}
			for(int i=0;i<children.size();i++)
				children.get(i).getBreathFirst(list);
		}
	}
	public void getDescendants(List<TreeNode> list)
	{
		if(!isLeaf())
		{
			for(int i=0;i<children.size();i++)
				list.add(children.get(i));
			for(int i=0;i<children.size();i++)
				children.get(i).getDescendants(list);
		}
	}
	
	//unrelated to diversification
	private boolean flatten = false;
	
	public TreeNode()
	{
		
	}
	public TreeNode(int value)
	{
		this.value = value;
	}
	public TreeNode(int idx, String text, double data)
	{
		this.value = idx;
		this.text = text;
		this.data = data;
	}
	public TreeNode(TreeNode n1, TreeNode n2, double data)
	{
		children = new ArrayList<TreeNode>();
		children.add(n1);
		children.add(n2);
		n1.parent = this;
		n2.parent = this;
		this.data = data;
	}
	public static TreeNode createTree(String strRep)
	{
		strRep = strRep.replace("()", "");
		StringTokenizer st = new StringTokenizer(strRep, "(;)", true);
		TreeNode curNode = null;
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if(s.compareTo("(")==0)
			{
				
			}
			else if(s.compareTo(")")==0)
			{
				curNode = curNode.parent;
			}
			else if(s.compareTo(";")==0)
			{
				curNode = curNode.parent;
			}
			else
			{
				TreeNode n = new TreeNode();
				n.parent = curNode;
				if(curNode != null)
					curNode.addChild(n);
				curNode = n;
				String[] t = s.split(":");
				if(t[0].compareTo("0")==0)//intermediate node
				{
					n.setText(t[1]);
					n.setData(Double.parseDouble(t[2]));
				}
				else if(t[0].compareTo("1")==0)//leaf node
				{
					n.setText(t[1]);
					n.setData(Double.parseDouble(t[2]));
				}
				else
				{
					System.out.println("Fatal Error.");
					System.exit(1);
				}
			}
		}
		while(curNode.parent != null)
			curNode = curNode.parent;
		return curNode;
	}
	public List<TreeNode> getChildren()
	{
		return children;
	}
	/**
	 * Give value of the current [leaf] node. The current node MUST BE A LEAF NODE!!
	 * @return
	 */
	public int value()
	{
		return value;
	}
	/**
	 * Give values of all nodes rooted at the current node
	 * @return
	 */
	public int[] values()
	{
		if(children == null)
		{
			int[] values = new int[1];
			values[0] = value;
			return values;
		}
		List<TreeNode> leaves = leaves();
		int[] values = new int[leaves.size()];
		for(int i=0;i<leaves.size();i++)
			values[i] = leaves.get(i).value;
		return values;
	}
	public boolean isLeaf()
	{
		return (children==null);
	}
	public boolean isLeaf(boolean countFlattenAsLeave)
	{
		if(countFlattenAsLeave)
			return (children==null)|(flatten==true);
		return (children==null);
	}
	
	public List<TreeNode> leaves()
	{
		List<TreeNode> l = new ArrayList<TreeNode>();
		leaves(l, false);
		return l;
	}
	public List<TreeNode> leaves(boolean countFlattenAsLeave)
	{
		List<TreeNode> l = new ArrayList<TreeNode>();
		leaves(l, countFlattenAsLeave);
		return l;
	}
	protected void leaves(List<TreeNode> l, boolean countFlattenAsLeave)
	{
		if(children == null || (countFlattenAsLeave && flatten==true))
			l.add(this);
		else
			for(int i=0;i<children.size();i++)
				children.get(i).leaves(l, countFlattenAsLeave);
	}
	public List<TreeNode> nextToLeaves()
	{
		List<TreeNode> nl = new ArrayList<TreeNode>();
		
		List<TreeNode> l = leaves();
		for(int i=0;i<l.size();i++)
		{
			TreeNode p = l.get(i).parent;
			boolean flag = false;
			for(int j=0;j<nl.size()&&!flag;j++)
				if(nl.get(j) == p)
					flag = true;
			if(!flag)
				nl.add(p);
		}
		return nl;
	}
	public List<TreeNode> top(int k)
	{
		List<TreeNode> leaves = leaves(false);
		double[] v = new double[leaves.size()];
		for(int i=0;i<leaves.size();i++)
			v[i] = leaves.get(i).data();
		int[] idx = Sorter.sort(v, false);
		int size = (idx.length>k)?k:idx.length;
		List<TreeNode> top = new ArrayList<TreeNode>();
		for(int i=0;i<size;i++)
			top.add(leaves.get(idx[i]));
		return top;
	}
	public void topS(int k)
	{
		List<TreeNode> top = top(k);
		topS(top);
		cleanUpInternalTree();
	}
	private void topS(List<TreeNode> top)
	{
		if(children != null)
		{
			for(int i=0;i<children.size();i++)
			{
				TreeNode r = children.get(i);
				if(r.children != null) //r is NOT leaf
				{
					r.topS(top);
					if(r.children.size() == 0)
					{
						r.parent = null;
						children.remove(i);
						i--;
					}
				}
				else //r is leaf
				{
					if(!top.contains(r))
					{
						r.parent = null;
						children.remove(i);
						i--;
					}
				}
				
			}
		}
	}
	//clean up cases where a node has only one non-leaf child
	private void cleanUpInternalTree()
	{
		if(children != null)
		{
			while(children.size() == 1)
			{
				TreeNode r = children.get(0);
				if(r.children != null)
				{
					children.clear();
					for(int i=0;i<r.children.size();i++)
						addChild(r.children.get(i));
					r.children.clear();
				}
				else
					break;
			}
			
			for(int i=0;i<children.size();i++)
			{
				TreeNode r = children.get(i);
				r.cleanUpInternalTree();
			}
		}
	}
	
	public String text()
	{
		return text;
	}
	public TreeNode parent()
	{
		return parent;
	}
	public List<TreeNode> children()
	{
		return children;
	}
	public double data()
	{
		return data;
	}
	
	public void setParent(TreeNode parent)
	{
		this.parent = parent;
	}
	public void setData(double diff)
	{
		this.data = diff;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	public void setValue(int value)
	{
		this.value = value;
	}
	
	public void addChild(TreeNode c)
	{
		if(children == null)
			children = new ArrayList<TreeNode>();
		children.add(c);
		c.parent = this;
	}
	public void flatten()
	{
		List<TreeNode> leaves = leaves();
		if(leaves != null)
		{
			children = new ArrayList<TreeNode>();
			for(int i=0;i<leaves.size();i++)
				addChild(leaves.get(i));
		}
		flatten = true;
	}
	public List<TreeNode> subTrees(double threshold)
	{
		List<TreeNode> l = new ArrayList<TreeNode>();
		subTrees(l, threshold);
		for(int i=0;i<l.size();i++)
			l.get(i).print();
		return l;
	}
	protected void subTrees(List<TreeNode> n, double threshold)
	{
		if(isLeaf() || flatten == true)
			n.add(this);
		else
		{
			if(data >= threshold)
				n.add(this);
			else if(children != null)
				for(int i=0;i<children.size();i++)
					children.get(i).subTrees(n, threshold);
		}
	}
	
	public void print(String[] names)
	{
		print("", names);
	}
	public void print(String indent, String[] names)
	{
		if(children == null)
			System.out.println(indent + "[-] " + ((names==null)?value:names[value]));
		else
		{
			System.out.print(indent + "[+][" + data + "]");
			//for(int i=0;i<cValue.length;i++)
				//System.out.print(cValue[i] + " ");
			System.out.println("");
			for(int i=0;i<children.size();i++)
				children.get(i).print(indent+"----", names);
		}
	}
	public void print()
	{
		print("");
	}
	public void print(String indent)
	{
		if(children == null)
			System.out.println(indent + "[-][" + data + "] " + text);
		else
		{
			System.out.print(indent + "[+][" + data + "]");
			if(text.compareTo("")!=0)
				System.out.print("[" + text + "]");
			System.out.println("");
			for(int i=0;i<children.size();i++)
				children.get(i).print(indent+"----");
		}
	}
	public String toString(String[] names)
	{
		String output = "";
		if(children == null)
			output += "1:" + ((names==null)?value:names[value]);
		else
		{
			output += "0:" + data + "(";
			for(int i=0;i<children.size();i++)
			{
				output += children.get(i).toString(names);
				if(i < (children.size()-1))
					output += ";";
			}
			output += ")";
		}
		return output;
	}
	public String toString(String[] names, double[] probs)
	{
		String output = "";
		if(children == null)
			output += "1:" + names[value] + ":" + SimpleMath.round(probs[value], 4);
		else
		{
			output += "0:" + SimpleMath.round(data, 4) + "(";
			for(int i=0;i<children.size();i++)
			{
				output += children.get(i).toString(names, probs);
				if(i < (children.size()-1))
					output += ";";
			}
			output += ")";
		}
		return output;
	}
	public String toString()
	{
		String output = "";
		if(children == null)
		{
			output += "1:" + text;
			output += ":" + data;//SimpleMath.round(data, 4);
		}
		else
		{
			output += "0:" + text;
			output += ":" + data /*SimpleMath.round(data, 4)*/ + "(";
			for(int i=0;i<children.size();i++)
			{
				output += children.get(i).toString();
				if(i < (children.size()-1))
					output += ";";
			}
			output += ")";
		}
		return output;
	}

	public void obtainRep()
	{
		if(children != null)
		{
			TreeNode rep = null;
			for(int i=0;i<children.size();i++)
			{
				TreeNode r = children.get(i);
				r.obtainRep();
				if(rep == null)
					rep = r;
				else if(rep.data < r.data)
					rep = r;
			}
			this.data = rep.data;
			this.text = rep.text;
		}
	}
	
	public void filterLeaves(int nl)
	{
		if(children != null)
		{
			boolean allLeaf = true;
			for(int i=0;i<children.size() && allLeaf;i++)
				if(children.get(i).children != null)
					allLeaf = false;
			
			if(allLeaf == false)
			{
				for(int i=0;i<children.size();i++)
					if(children.get(i).children != null)
						children.get(i).filterLeaves(nl);
			}
			else
			{
				while(children.size() > nl)
				{
					if(children.get(0).data < children.get(1).data)
						children.remove(0);
					else
						children.remove(1);
				}
			}
		}
	}
	
	public void filterByLevel(int l)
	{
		filterByLevel(1, l);
	}
	private void filterByLevel(int curLevel, int nLevel)
	{
		if(children == null)
			return;
		if(curLevel < nLevel)
		{
			if(children != null)
				for(int i=0;i<children.size();i++)
					children.get(i).filterByLevel(curLevel+1, nLevel);
		}
		else
		{
			flatten();
		}
	}
	//merge very similar leaf nodes together, keep the best one among them
	public void filterLeaves(double sim)
	{
		if(children != null)
		{
			if(this.data > sim)
			{
				List<TreeNode> leaves = leaves();
				double p = 0.0;
				double max = -1.0;
				int which = -1;
				for(int i=0;i<leaves.size();i++)
				{
					p += leaves.get(i).data;
					if(max < leaves.get(i).data)
					{
						max = leaves.get(i).data;
						which = i;
					}
				}
				if(which != -1)
				{
					children.clear();
					leaves.get(which).data = p;
					children.add(leaves.get(which));
				}
			}
			else
				for(int i=0;i<children.size();i++)
					if(children.get(i).children != null)
						children.get(i).filterLeaves(sim);
		}
	}
	
	public TreeNode getFlattenClusters()
	{
		List<TreeNode> l = nextToLeaves();
		if(l.size() <= 1)
			return this;
		
		TreeNode t = new TreeNode();
		for(int i=0;i<l.size();i++)
			t.addChild(l.get(i));
		return t;
	}
	
	public void extract(double sim, TreeNode nn)
	{
		if(children != null)
		{
			if(this.data > sim)
				nn.addChild(this);
			else
				for(int i=0;i<children.size();i++)
					if(children.get(i).children != null)
						children.get(i).extract(sim, nn);
		}
	}
	public void extract(double sim, TreeNode nn, List<Integer> idx)
	{
		if(children != null)
		{
			for(int i=0;i<children.size();i++)
				children.get(i).extract(sim, nn, idx);
		}
		else
		{
			if(idx.contains(this.value))
			{
				TreeNode n = this.parent;//parent of the leaf
				TreeNode c = this;
				while(n != null && n.data > sim)
				{
					c = n;
					n = n.parent;
				}
				if(c.children == null)
				{
					n = new TreeNode();
					n.setData(1.0);
					n.addChild(c);
					nn.addChild(n);
				}
				else
				{
					if(nn.children == null)
						nn.addChild(c);
					else
					{
						boolean flag = false;
						for(int i=0;i<nn.children.size()&&!flag;i++)
							if(nn.children.get(i) == c)
								flag = true;
						if(!flag)
							nn.addChild(c);
					}
				}
			}
		}
	}
	public void normalize()
	{
		if(children != null)
		{
			boolean allLeaf = true;
			for(int i=0;i<children.size() && allLeaf;i++)
				if(children.get(i).children != null)
					allLeaf = false;
			
			if(allLeaf == false)
			{
				for(int i=0;i<children.size();i++)
				{
					if(children.get(i).children != null)
						children.get(i).normalize();
					else
					{
						TreeNode n = new TreeNode();
						n.setData(1.0);
						n.addChild(children.get(i));
						children.set(i, n);
						n.setParent(this);
					}
				}
			}
		}
	}
	public void remove(List<Integer> idx)
	{
		if(children != null)
		{
			for(int i=0;i<children.size();i++)
			{
				TreeNode n = children.get(i);
				if(n.children != null)
				{
					n.remove(idx);
					if(n.children.size() == 0)
					{
						n.parent = null;
						children.remove(i);
						i--;
					}
				}
				else if(idx.contains(n.value))
				{
					n.parent = null;
					children.remove(i);
					i--;
				}
			}
		}
	}
}
