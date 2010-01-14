/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Groups Manager command to release a lock on a group, return to browse mode
 *
 * @author Alex Vigdor
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class UnlockGroup extends GroupsManagerCommand{

  public UnlockGroup() {
  }

   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      Element parentElem = null;
      sessionData.mode = BROWSE_MODE;
      Document model = sessionData.model;
      String key = sessionData.lockedGroup.getLock().getEntityKey();
      Utility.logMessage("DEBUG", "UnlockGroup::execute(): Locked group key = " + key);
      sessionData.lockedGroup.getLock().release();
      sessionData.lockedGroup = null;
      String parentID = getParentId(sessionData.staticData);

      // Parent was locked so no other thread or process could have changed it, but
      // child members could have changed.
      // Parent element id is not always set.
      if (!Utility.areEqual(parentID, "")){
         parentElem = GroupsManagerXML.getElementById(model, parentID);
         sessionData.staticData.remove("groupParentId");
      }
      Utility.logMessage("DEBUG", "UnlockGroup::execute(): parentElem = " + parentElem);
      if (parentElem != null){
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);
      }
   }
}