/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.security.IPerson;

/**
 * Records creation of a new session in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordSessionCreatedWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  
  public RecordSessionCreatedWorkerTask(IPerson person) {
    this.person = person;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordSessionCreated(this.person);
  }
}



