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

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.UserLayoutChannelDescription;
import org.jasig.portal.layout.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * Formulates stats messages which can be logged, printed, etc.
 * Subclasses need to override <code>outputMessage(String message)</code>.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public abstract class MessageStatsRecorder implements IStatsRecorder {
 
  public void recordLogin(IPerson person) {
    String msg = getDisplayName(person) + 
                 " logged in successfully at " +
                 new java.util.Date();
    outputMessage(fixMsg(msg));
  }
  
  public void recordLogout(IPerson person) {
    String msg = getDisplayName(person) + 
                 " logged out at " +
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }  
  
  public void recordSessionCreated(IPerson person) {
    String msg = "Session created for " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }
  
  public void recordSessionDestroyed(IPerson person) {
    String msg = "Session destroyed for " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }
  
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
    String msg = "Channel '" +
                 channelDef.getName() + "' " +
                 "was published by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }

  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
    String msg = "Channel '" +
                 channelDef.getName() + "' " +
                 "was modified by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();  
    outputMessage(fixMsg(msg));                 
  }

  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
    String msg = "Channel '" +
                 channelDef.getName() + "' " +
                 "was removed by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }  
  
  public void recordChannelAddedToLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was added to layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }    
  
  public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was updated in layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   
  
  public void recordChannelMovedInLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was moved in layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   
  
  public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was removed from layout " + profile.getLayoutId() + " " + 
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   

  public void recordFolderAddedToLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {
    String msg = "Folder [" +
                 folderDesc.getName() + ", " + 
                 folderDesc.getId() + "] " +
                 "was added to layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }    
  
  public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {
    String msg = "Folder [" +
                 folderDesc.getName() + ", " + 
                 folderDesc.getId() + "] " +
                 "was updated in layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   
  
  public void recordFolderMovedInLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {
    String msg = "Folder [" +
                 folderDesc.getName() + ", " + 
                 folderDesc.getId() + "] " +
                 "was moved in layout " + profile.getLayoutId() + " " +
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   
  
  public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {
    String msg = "Folder [" +
                 folderDesc.getName() + ", " + 
                 folderDesc.getId() + "] " +
                 "was removed from layout " + profile.getLayoutId() + " " + 
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  } 
  
  public void recordChannelInstantiated(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was instantiated in layout " + profile.getLayoutId() + " " + 
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }   

  public void recordChannelRendered(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was rendered in layout " + profile.getLayoutId() + " " + 
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }

  public void recordChannelTargeted(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {
    String msg = "Channel [" +
                 channelDesc.getName() + ", " + 
                 channelDesc.getChannelPublishId() + ", " +
                 channelDesc.getChannelSubscribeId() + "] " +
                 "was targeted in layout " + profile.getLayoutId() + " " + 
                 "by " +
                 getDisplayName(person) + " " +
                 "at " + 
                 new java.util.Date();
    outputMessage(fixMsg(msg));                 
  }
  
  /**
   * Outputs the message formulated according
   * to the stat being recorded.  Subclasses
   * have the responsibility of implementing 
   * this method.
   * @param message, the message to output
   */    
  protected abstract void outputMessage(String message);
 
  /**
   * Creates a name suitable for displaying in a
   * stats message.  Indicates if user is a "guest" user.
   * @param person, the person
   * @return name, the display name for the user
   */  
  private String getDisplayName(IPerson person) {
    String name = null;
    if (person != null) {
      String userName = (String)person.getAttribute(person.USERNAME);
      if (person.isGuest()) {
        name = "GUEST_USER (" + fixNull(userName) + ")";
      } else {
        String firstName = (String)person.getAttribute("givenName");
        String lastName = (String)person.getAttribute("sn");
        name = fixNull(firstName) + " " + fixNull(lastName) + 
               " (" + fixNull(userName) + ")";
      }
    } else {
      name = "NULL_PERSON";
    }
    return name;
  }  
  
  /**
   * Replaces null String with an empty String
   * @param s, the string to fix
   * @return s, the fixed string
   */  
  private String fixNull(String s) {
    return s == null ? "" : s;
  }  
  
  /**
   * Prepends a string to each message to make it
   * clear that it came from the LoggingStatsRecorder
   * @param msg, the message to log
   * @return msg, the message prepended with a string that
   * identifies the message as having come from a stats recorder
   */  
  private String fixMsg(String msg) {
    return "STATS-RECORDER: " + msg;
  }
}



