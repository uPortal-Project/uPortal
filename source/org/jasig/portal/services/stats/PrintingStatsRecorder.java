/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;


/**
 * Prints portal statistics to std out.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
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



