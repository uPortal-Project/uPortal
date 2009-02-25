/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils;

/**
 * @author George Lindholm <a href="mailto:George.Lindholm@ubc.ca">George.Lindholm@ubc.ca</a>
 * @version $Revision$
 * @since uPortal 2.5
 */

public interface IMovingAverageSample {
	public long getAverage();

	public long getHighMax();

	public long getLastSample();

	public long getMax();

	public long getMin();

	public long getTotalSamples();

}