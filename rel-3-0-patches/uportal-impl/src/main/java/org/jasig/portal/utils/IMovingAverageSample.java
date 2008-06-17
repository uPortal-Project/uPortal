/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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