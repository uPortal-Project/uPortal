/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.services;

import org.jasig.portal.services.stats.IStatsRecorderFactory;
import org.jasig.portal.services.stats.IStatsRecorder;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.PropertiesManager;

/**
 * Stats recorder service. Various parts of the portal call
 * the methods in this service to record events such as 
 * when a user logs in, logs out, and subscribes to a channel.
 * The information is handed to an IStatsRecorder implementation
 * that is determined by the IStatsRecorderFactory implementation
 * that can be configured in portal.properties.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class StatsRecorder {
  protected static StatsRecorder statsRecorderInstance;
  protected IStatsRecorder statsRecorder;
  
  /**
   * Constructor with private access so that the StatsRecorder
   * maintains only one instance of itself.
   */
  private StatsRecorder() {
		try {
	    // Get a stats recorder from the stats recorder factory. 
      String statsRecorderFactoryName = PropertiesManager.getProperty("org.jasig.portal.services.stats.StatsRecorderFactory.implementation");      
			IStatsRecorderFactory statsRecorderFactory = (IStatsRecorderFactory)Class.forName(statsRecorderFactoryName).newInstance();
      statsRecorder = statsRecorderFactory.getStatsRecorder();	
		} catch (Exception e) {
			LogService.log(LogService.ERROR, e);
		}
  }
  
  /**
   * Creates an instance of this stats recorder service.
   * @return StatsRecorder instance
   */
  private final static synchronized StatsRecorder instance() {
    if (statsRecorderInstance == null) { 
      statsRecorderInstance = new StatsRecorder(); 
    }
    return statsRecorderInstance;
  }
  
  /**
   * Record the successful login of a user.
   * @param person, the person who is logging in
   */
  public static void recordLogin(IPerson person) {
    instance().statsRecorder.recordLogin(person);
  }

  /**
   * Record the logout of a user.
   * @param person, the person who is logging out
   */
  public static void recordLogout(IPerson person) {
    instance().statsRecorder.recordLogout(person);
  }
  
  /**
   * Record that a new session is created for a user.
   * @param person, the person whose session is being created
   */
  public static void recordSessionCreated(IPerson person) {
    instance().statsRecorder.recordSessionCreated(person);
  }
  
  /**
   * Record that a user's session is destroyed
   * (when the user logs out or his/her session
   * simply times out)
   * @param person, the person whose session is ending
   */
  public static void recordSessionDestroyed(IPerson person) {
    instance().statsRecorder.recordSessionDestroyed(person);
  }
}
