/**
 * Copyright � 2002 The JA-SIG Collaborative.  All rights reserved.
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

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.LayoutEvent;
import org.jasig.portal.layout.LayoutEventListener;
import org.jasig.portal.layout.LayoutMoveEvent;
import org.jasig.portal.layout.UserLayoutChannelDescription;
import org.jasig.portal.layout.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.StatsRecorder;

/**
 * Listens to events generated by an <code>IUserLayoutManager</code>.
 * The information in the event is passed along to the
 * <code>StatsRecorder</code> service.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class StatsRecorderLayoutEventListener implements LayoutEventListener {
  
  IPerson person;
  UserProfile profile;

  /**
   * Constructor for <code>StatsRecorderLayoutEventListener</code>.
   * @param person, the owner of the layout for which this listener is listening
   * @param profile, the profile of the layout for which this listener is listening
   */  
  public StatsRecorderLayoutEventListener(IPerson person, UserProfile profile) {
    this.person = person;
    this.profile = profile;
  }
 
  // Channels...
  public void channelAdded(LayoutEvent ev) {
    UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription)ev.getNodeDescription();
    StatsRecorder.recordChannelAddedToLayout(person, profile, channelDesc);
  }
  
  public void channelUpdated(LayoutEvent ev) {
    UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription)ev.getNodeDescription();
    StatsRecorder.recordChannelUpdatedInLayout(person, profile, channelDesc);
  }
  
  public void channelMoved(LayoutMoveEvent ev) {
    UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription)ev.getNodeDescription();
    StatsRecorder.recordChannelMovedInLayout(person, profile, channelDesc);
  }
  
  public void channelDeleted(LayoutMoveEvent ev) {
    UserLayoutChannelDescription channelDesc = (UserLayoutChannelDescription)ev.getNodeDescription();
    StatsRecorder.recordChannelRemovedFromLayout(person, profile, channelDesc);    
  }

  // Folders...
  public void folderAdded(LayoutEvent ev) {
    UserLayoutFolderDescription folderDesc = (UserLayoutFolderDescription)ev.getNodeDescription();
    StatsRecorder.recordFolderAddedToLayout(person, profile, folderDesc);    
  }
  
  public void folderUpdated(LayoutEvent ev) {
    UserLayoutFolderDescription folderDesc = (UserLayoutFolderDescription)ev.getNodeDescription();
    StatsRecorder.recordFolderUpdatedInLayout(person, profile, folderDesc);    
  }
  
  public void folderMoved(LayoutMoveEvent ev) {
    UserLayoutFolderDescription folderDesc = (UserLayoutFolderDescription)ev.getNodeDescription();
    StatsRecorder.recordFolderMovedInLayout(person, profile, folderDesc);    
  }
  
  public void folderDeleted(LayoutMoveEvent ev) {
    UserLayoutFolderDescription folderDesc = (UserLayoutFolderDescription)ev.getNodeDescription();
    StatsRecorder.recordFolderRemovedFromLayout(person, profile, folderDesc);    
  }

  // Layout...
  public void layoutLoaded() {
  }
  
  public void layoutSaved() {
  }
}
