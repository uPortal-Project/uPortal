/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

/**
 * Records the publishing of a channel in a separate thread.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RecordChannelDefinitionPublishedWorkerTask extends StatsRecorderWorkerTask {
  
  IPerson person;
  ChannelDefinition channelDef;
  
  public RecordChannelDefinitionPublishedWorkerTask(IPerson person, ChannelDefinition channelDef) {
    this.person = person;
    this.channelDef = channelDef;
  }

  public void execute() throws Exception {
    this.statsRecorder.recordChannelDefinitionPublished(this.person, this.channelDef);
  }
}



