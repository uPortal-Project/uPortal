/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils;

/**
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class MovingAverage {

  public static final int SAMPLESIZE = 100;
  private long[] samples;
  private int ent = -1;
  private long sum = 0;
  private long totalSamples = 0;
  private long highMax = 0;

  public MovingAverage() {
	  samples = new long[SAMPLESIZE];
  }

  public synchronized MovingAverageSample add(final long sample) {

    final long lastSample = sample;
    final int first = ++ent % samples.length;
    if (totalSamples >= samples.length) {
      sum -= samples[first]; // We've wrapped, so we can remove the 'first' entry
    }
    sum += sample;
    samples[first] = sample;
    if (sample > highMax) {
      highMax = sample;
    }
    totalSamples++;

    long max = 0;
    long min = Long.MAX_VALUE;
    final long arraySize = Math.min(totalSamples, samples.length);
    for (int i = 0; i < arraySize; i++) {
      if (samples[i] > max) {
        max = samples[i];
      }
      if (samples[i] < min) {
        min = samples[i];
      }
    }

    return new MovingAverageSample(sum / arraySize, highMax, lastSample, max, min, totalSamples);
  }
}
