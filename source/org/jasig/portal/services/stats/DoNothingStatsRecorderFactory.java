/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Produces a DoNothingStatsRecorder, an implementation of IStatsRecorder
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system based on the Spring ApplicationEventPublisher 
 * and Event Listeners. For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class DoNothingStatsRecorderFactory implements IStatsRecorderFactory {
  /**
   * Returns an new do-nothing stats recorder
   * @return doNothingStatsRecorder, an IStatsRecorder implementation that does nothing
   */
  public IStatsRecorder getStatsRecorder() {
    return new DoNothingStatsRecorder();
  }
}



