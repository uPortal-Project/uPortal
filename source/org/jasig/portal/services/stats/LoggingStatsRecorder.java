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

package org.jasig.portal.services.stats;

import org.jasig.portal.services.LogService;
import org.jasig.portal.layout.UserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.UserProfile;
import org.jasig.portal.ChannelDefinition;
import org.apache.log4j.Priority;

/**
 * Logs portal statistics to the portal's log.  Contains
 * set and get methods to control the log priority.
 * @author Ken Weiner, kweiner@interactivebusiness.com
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
  
  public void recordLogin(IPerson person) {
    String msg = super.getMessageForLogin(person);
    LogService.instance().log(priority, msg);
  }
  
  public void recordLogout(IPerson person) {
    String msg = super.getMessageForLogout(person);
    LogService.instance().log(priority, msg);
  }  
  
  public void recordSessionCreated(IPerson person) {
    String msg = super.getMessageForSessionCreated(person);
    LogService.instance().log(priority, msg);
  }
  
  public void recordSessionDestroyed(IPerson person) {
    String msg = super.getMessageForSessionDestroyed(person);
    LogService.instance().log(priority, msg);
  }
  
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionPublished(person, channelDef);
    LogService.instance().log(priority, msg);
  }

  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionModified(person, channelDef);
    LogService.instance().log(priority, msg);
  }

  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionRemoved(person, channelDef);
    LogService.instance().log(priority, msg);
  }  

  public void recordChannelAddedToLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = super.getMessageForChannelAddedToLayout(person, profile, channelDesc);
   LogService.instance().log(priority, msg);
  }    
  
  public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = super.getMessageForChannelRemovedFromLayout(person, profile, channelDesc);
    LogService.instance().log(priority, msg);
  }    
}



