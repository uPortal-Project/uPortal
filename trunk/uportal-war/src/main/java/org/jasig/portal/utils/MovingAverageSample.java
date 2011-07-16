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

import java.io.Serializable;

/**
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version $Revision$
 * @since uPortal 2.5
 */

public final class MovingAverageSample implements IMovingAverageSample, Serializable {
	//public static long SerialVersionUID = 2006l;

	public long average;

	public long highMax;

	public long lastSample;

	public long max;

	public long min;

	public long totalSamples;

	public MovingAverageSample() {
	}

	public MovingAverageSample(final long average, final long highMax,
			final long lastSample, final long max, final long min,
			final long totalSamples) {
		this.average = average;
		this.highMax = highMax;
		this.lastSample = lastSample;
		this.max = max;
		this.min = min;
		this.totalSamples = totalSamples;
	}

	public long getAverage() {
		return average;
	}

	public long getHighMax() {
		return highMax;
	}

	public long getLastSample() {
		return lastSample;
	}

	public long getMax() {
		return max;
	}

	public long getMin() {
		return min;
	}

	public long getTotalSamples() {
		return totalSamples;
	}
}
