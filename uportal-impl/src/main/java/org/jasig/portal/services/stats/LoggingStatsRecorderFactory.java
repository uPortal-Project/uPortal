/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Produces a LoggingStatsRecorder, an implementation of IStatsRecorder
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system based on the Spring ApplicationEventPublisher 
 * and Event Listeners. For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class LoggingStatsRecorderFactory implements IStatsRecorderFactory {

  /**
   * Returns an new logging stats recorder
   * @return loggingStatsRecorder, an IStatsRecorder implementation that logs stats messages
   */
  public IStatsRecorder getStatsRecorder() {
    return new LoggingStatsRecorder();
  }
}



