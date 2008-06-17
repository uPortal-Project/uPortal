/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */

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