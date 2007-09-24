/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * Records the moving of a folder in a user's layout
 * in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system 
 * based on the Spring ApplicationEventPublisher and Event Listeners. 
 * For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public class RecordFolderMovedInLayoutWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  UserProfile profile;
  IUserLayoutFolderDescription folderDesc;
  
  public RecordFolderMovedInLayoutWorkerTask(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
    this.person = person;
    this.profile = profile;
    this.folderDesc = folderDesc;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordFolderMovedInLayout(this.person, this.profile, this.folderDesc);
  }
}



