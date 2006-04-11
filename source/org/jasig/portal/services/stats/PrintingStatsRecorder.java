/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;


/**
 * Prints portal statistics to std out.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated use PrintingEventHandler. IStatsRecorder implementation is replaced with a much more flexible system based on the Spring ApplicationEventPublisher 
 * and Event Listeners. For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class PrintingStatsRecorder extends MessageStatsRecorder {

  /**
   * Prints portal statistics messages to std out.
   * @param message, the message to print
   */    
  protected void outputMessage(String message) {
    System.out.println(message);
  }
  
}



