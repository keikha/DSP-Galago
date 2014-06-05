package ciir.umass.edu.utilities;

public class AgMin implements Aggregator { 
	public double value(double[] values)
	{
		double min = values[0];
		for(int i=1;i<values.length;i++)
		{
			if(min > values[i])
				min = values[i];
		}
		return min;
	}
}
