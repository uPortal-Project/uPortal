/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.security.IPerson;

/**
 * Records a user logout in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordLogoutWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  
  public RecordLogoutWorkerTask(IPerson person) {
    this.person = person;
  }

  public void run() {
    statsRecorder.recordLogout(person);
  }
}



