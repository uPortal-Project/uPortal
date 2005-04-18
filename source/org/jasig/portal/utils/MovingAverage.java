/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

/**
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class MovingAverage {
  private long[] samples;
  private int ent = -1;
  private long sum = 0;
  private long totalSamples = 0;
  private long highMax = 0;

  public MovingAverage(final int sampleSize) {
    samples = new long[sampleSize];
  }

  public static class Sample {
    public long average;
    public long highMax;
    public long lastSample;
    public long max;
    public long min;
    public long totalSamples;

    public Sample() {
    }

    public Sample(final long average, final long highMax, final long lastSample,
                  final long max,
                  final long min, final long totalSamples) {
      this.average = average;
      this.highMax = highMax;
      this.lastSample = lastSample;
      this.max = max;
      this.min = min;
      this.totalSamples = totalSamples;
    }
  }

  public synchronized Sample add(final long sample) {

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

    return new Sample(sum / arraySize, highMax, lastSample, max, min, totalSamples);
  }
}
