/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
