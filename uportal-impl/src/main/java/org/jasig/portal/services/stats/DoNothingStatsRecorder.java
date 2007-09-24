/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Does absolutely nothing with the recorded statistics.
 * If you want to process the statistics, use a different
 * IStatsRecorder implementation.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system based on the Spring ApplicationEventPublisher 
 * and Event Listeners. For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class DoNothingStatsRecorder extends BaseStatsRecorder {
    // does nothing.
}



