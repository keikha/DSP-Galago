package ciir.umass.edu.utilities;

public class AgMax implements Aggregator{
	public double value(double[] values)
	{
		double max = values[0];
		for(int i=1;i<values.length;i++)
		{
			if(max < values[i])
				max = values[i];
		}
		return max;
	}
}
