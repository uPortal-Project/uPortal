/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logs portal statistics to the portal's log as info. 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated Use LoggingEventHandler. IStatsRecorder implementation is replaced with a much more flexible system based on the Spring ApplicationEventPublisher 
 * and Event Listeners. For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class LoggingStatsRecorder extends MessageStatsRecorder {

  private static final Log log = LogFactory.getLog(LoggingStatsRecorder.class);

  public LoggingStatsRecorder() {
  }
  
  /**
   * Prints portal statistics messages to the portal log.
   * @param message, the message to print
   */   
  protected void outputMessage(String message) {
    log.info(message);
  }
}



