/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

/**
 * Records the moving of a channel in a user's layout
 * in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordChannelMovedInLayoutWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  UserProfile profile;
  IUserLayoutChannelDescription channelDesc;
  
  public RecordChannelMovedInLayoutWorkerTask(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
    this.person = person;
    this.profile = profile;
    this.channelDesc = channelDesc;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordChannelMovedInLayout(this.person, this.profile, this.channelDesc);
  }
}



