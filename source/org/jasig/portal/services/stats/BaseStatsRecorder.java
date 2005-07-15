/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
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
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class BaseStatsRecorder implements IStatsRecorder {
    
    protected Log log = LogFactory.getLog(getClass());
    
  public void recordLogin(IPerson person) {}
  public void recordLogout(IPerson person) {}  
  public void recordSessionCreated(IPerson person) {}
  public void recordSessionDestroyed(IPerson person) {}
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {}
  public void recordChannelAddedToLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordChannelMovedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordFolderAddedToLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {}
  public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {}
  public void recordFolderMovedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {}
  public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {}
  public void recordChannelInstantiated(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordChannelRendered(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
  public void recordChannelTargeted(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {}
}



