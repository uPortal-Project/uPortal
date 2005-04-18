/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * Records the updating of a folder in a user's layout
 * in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordFolderUpdatedInLayoutWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  UserProfile profile;
  IUserLayoutFolderDescription folderDesc;
  
  public RecordFolderUpdatedInLayoutWorkerTask(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
    this.person = person;
    this.profile = profile;
    this.folderDesc = folderDesc;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordFolderUpdatedInLayout(this.person, this.profile, this.folderDesc);
  }
}



