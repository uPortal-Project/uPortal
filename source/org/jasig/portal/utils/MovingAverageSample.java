/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
