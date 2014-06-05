package ciir.umass.edu.sum.simmeasure;

public class SimCosine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private ItemArrayList l1 = null;
	private ItemArrayList l2 = null;
	
	public SimCosine(ItemArrayList l1, ItemArrayList l2)
	{
		this.l1 = l1;
		this.l2 = l2;
	}
	
	public double value()
	{
		int i = 0;
		int j = 0;
		double num = 0.0;
		double denom1 = 0.0;
		double denom2 = 0.0;
		while(i < l1.size() && j < l2.size())
		{
			int v = l1.get(i).compareTo(l2.get(j));
			if(v == 0)
			{
				num += l1.get(i).prob() * l2.get(j).prob();
				denom1 += l1.get(i).prob()*l1.get(i).prob();
				denom2 += l2.get(j).prob()*l2.get(j).prob();
				i++;
				j++;
			}
			else if(v < 0)
			{
				denom1 += l1.get(i).prob()*l1.get(i).prob();
				i++;
			}
			else //v > 0
			{
				denom2 += l2.get(j).prob()*l2.get(j).prob();
				j++;
			}
		}
		while(i < l1.size())
		{
			denom1 += l1.get(i).prob()*l1.get(i).prob();
			i++;
		}
		while(j < l2.size())
		{
			denom2 += l2.get(j).prob()*l2.get(j).prob();
			j++;
		}
		return num / (Math.sqrt(denom1) * Math.sqrt(denom2));
	}
}
