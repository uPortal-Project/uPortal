/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * Records the addition of a folder to a user's layout
 * in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordFolderAddedToLayoutWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  UserProfile profile;
  IUserLayoutFolderDescription folderDesc;
  
  public RecordFolderAddedToLayoutWorkerTask(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
    this.person = person;
    this.profile = profile;
    this.folderDesc = folderDesc;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordFolderAddedToLayout(this.person, this.profile, this.folderDesc);
  }
}



