/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Produces a DoNothingStatsRecorder, an implementation of IStatsRecorder
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
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



