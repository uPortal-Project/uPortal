/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.log4j.Priority;
import org.jasig.portal.services.LogService;

/**
 * Logs portal statistics to the portal's log.  Contains
 * set and get methods to control the log priority.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LoggingStatsRecorder extends MessageStatsRecorder {

  // Unfortunately this is tied to Apache's Log4J.
  // It would be nice if the LogService was 
  // logger implementation agnostic!
  private Priority priority;
  
  public LoggingStatsRecorder() {
    this.priority = LogService.INFO;
  }
  
  public LoggingStatsRecorder(Priority priority) {
    this.priority = priority;
  }  
  
  public void setPriority(Priority priority) {
    this.priority = priority;
  }
  
  public Priority getPriority() {
    return this.priority;
  }
  
  /**
   * Prints portal statistics messages to the portal log.
   * @param message, the message to print
   */   
  protected void outputMessage(String message) {
    LogService.log(priority, message);
  }
     
}



