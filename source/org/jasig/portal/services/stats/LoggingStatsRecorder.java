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
import org.jasig.portal.security.IPerson;
import org.jasig.portal.ChannelDefinition;

/**
 * Logs portal statistics to the portal's log.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class LoggingStatsRecorder extends MessageStatsRecorder {

  public void recordLogin(IPerson person) {
    String msg = super.getMessageForLogin(person);
    LogService.instance().log(LogService.INFO, msg);
  }
  
  public void recordLogout(IPerson person) {
    String msg = super.getMessageForLogout(person);
    LogService.instance().log(LogService.INFO, msg);
  }  
  
  public void recordSessionCreated(IPerson person) {
    String msg = super.getMessageForSessionCreated(person);
    LogService.instance().log(LogService.INFO, msg);
  }
  
  public void recordSessionDestroyed(IPerson person) {
    String msg = super.getMessageForSessionDestroyed(person);
    LogService.instance().log(LogService.INFO, msg);
  }
  
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionPublished(person, channelDef);
    LogService.instance().log(LogService.INFO, msg);
  }

  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionModified(person, channelDef);
    LogService.instance().log(LogService.INFO, msg);
  }

  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
    String msg = super.getMessageForChannelDefinitionRemoved(person, channelDef);
    LogService.instance().log(LogService.INFO, msg);
  }  
}



