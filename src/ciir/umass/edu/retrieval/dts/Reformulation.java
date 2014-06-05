package ciir.umass.edu.retrieval.dts;

public class Reformulation {
	public String id = "";
	public String qtext = "";
	public double prob = 0.0;
	public String orig = "";
	
	public Reformulation(String qtext, double prob)
	{
		this.qtext = qtext;
		this.prob = prob;
	}
	public Reformulation(String id, String qtext, double prob)
	{
		this.id = id;
		this.qtext = qtext;
		this.prob = prob;
	}
	public Reformulation(String id, String qtext, double prob, String orig)
	{
		this.id = id;
		this.qtext = qtext;
		this.prob = prob;
		this.orig = orig;
	}
	public Reformulation(String qtext, double prob, String orig)
	{
		this.qtext = qtext;
		this.prob = prob;
		this.orig = orig;
	}
}
