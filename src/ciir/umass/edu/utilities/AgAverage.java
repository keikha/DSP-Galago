package ciir.umass.edu.utilities;

public class AgAverage implements Aggregator {
	public double value(double[] values)
	{
		double avg = 0.0;
		for(int i=0;i<values.length;i++)
			avg += values[i];
		return avg/values.length;
	}
}
