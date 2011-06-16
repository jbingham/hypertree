package com.sugen.util;


/**
 * Static methods for basic statistics.
 * 
 * @author Jonathan Bingham
 */
public class Statistics
{	
	public static double mean(double[] d)
	{
		double total = 0;
		for(int i = 0; i < d.length; i++)
			total += d[i];
		return (total / d.length);
	}
	
	public static double variance(double[] d)
	{
		return variance(d, mean(d));
	}
	
	public static double variance(double[] d, double mean)
	{
		double variance = 0;
		for(int i = 0; i < d.length; i++)
			variance += (d[i] - mean) * (d[i] - mean);
		variance /= d.length;
		return variance;
	}
	
	public static double standardOfDeviation(double[] d)
	{
		return Math.sqrt(variance(d));
	}
	
	public static double standardOfDeviation(double[] d, double mean)
	{
		return Math.sqrt(variance(d, mean));
	}
	
	public static double zScore(double sample, double[] distribution)
	{
		double mean = mean(distribution);
		double stdev = standardOfDeviation(distribution, mean);
		return zScore(sample, mean, stdev);
	}
	
	public static double zScore(double d, double mean, double stdev)
	{
		return (d - mean) / stdev;
	}
	
	public static double min(double[] d)
	{
		double min = Double.MAX_VALUE;
		for(int i = 0; i < d.length; i++)
			if(d[i] < min)
				min = d[i];
		return min;
	}
	
	public static double max(double[] d)
	{
		double max = Double.MIN_VALUE;
		for(int i = 0; i < d.length; i++)
			if(d[i] > max)
				max = d[i];
		return max;
	}
	
	public static double range(double[] d)
	{
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(int i = 0; i < d.length; i++)
		{
			if(d[i] > max)
				max = d[i];
			if(d[i] < min)
				min = d[i];
		}
		return max - min;
	}
}