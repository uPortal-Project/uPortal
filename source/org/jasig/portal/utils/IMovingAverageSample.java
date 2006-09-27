package org.jasig.portal.utils;

public interface IMovingAverageSample {
	public long getAverage();

	public long getHighMax();

	public long getLastSample();

	public long getMax();

	public long getMin();

	public long getTotalSamples();

}