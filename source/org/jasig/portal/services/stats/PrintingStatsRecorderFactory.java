/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

/**
 * Produces a PrintingStatsRecorder, an implementation of IStatsRecorder
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PrintingStatsRecorderFactory implements IStatsRecorderFactory {
  /**
   * Returns an new printing stats recorder
   * @return printingStatsRecorder, an IStatsRecorder implementation that prints stats messages to std out
   */
  public IStatsRecorder getStatsRecorder() {
    return new PrintingStatsRecorder();
  }
}



