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
 * <p>This class can optionally be used as a base class for
 * any custom stats recorders.  It does absolutely nothing 
 * with the recorded statistics.</p>  
 * <p>If you extend this class,
 * you can override any of the <code>IStatsRecorder</code> 
 * methods that you are interested in and ignore the rest.
 * Extending this class will also shield you from having to
 * implement any newly added methods to 
 * <code>IStatsRecorder</code> in the future.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class BaseStatsRecorder implements IStatsRecorder {
  public void recordLogin(IPerson person) {}
  public void recordLogout(IPerson person) {}  
  public void recordSessionCreated(IPerson person) {}
  public void recordSessionDestroyed(IPerson person) {}
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelAddedToLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordChannelMovedInLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordFolderAddedToLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {}
  public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {}
  public void recordFolderMovedInLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {}
  public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, UserLayoutFolderDescription folderDesc) {}
  public void recordChannelInstantiated(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordChannelRendered(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
  public void recordChannelTargeted(IPerson person, UserProfile profile, UserLayoutChannelDescription channelDesc) {}
}



