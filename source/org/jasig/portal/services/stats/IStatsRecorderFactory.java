/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
 
package org.jasig.portal.services.stats;

/**
 * A factory that produces IStatsRecorder implementations.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface IStatsRecorderFactory {
  /**
   * Obtains the IStatsRecorderImplementation
   * @return statsRecorder, the IStatsRecorder implementation
   */
  public IStatsRecorder getStatsRecorder();
}
