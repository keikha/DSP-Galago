package ciir.umass.edu.sum.simmeasure;

public class Item {
	private String name = "";
	private double prob = 0.0;
	private double bgProb = 0.0;
	public Item(String name, double prob)
	{
		this.name = name;
		this.prob = prob;
		this.bgProb = -1.0;
	}
	public Item(String name, double prob, double bgProb)
	{
		this.name = name;
		this.prob = prob;
		this.bgProb = bgProb;
	}
	public boolean equals(Item i)
	{
		if(name.compareTo(i.name)==0)
			return true;
		return false;
	}
	public int compareTo(Item i)
	{
		return name.compareTo(i.name);
	}
	public String name()
	{
		return name;
	}
	public double prob()
	{
		return prob;
	}
	public double bgprob()
	{
		return bgProb;
	}
}
