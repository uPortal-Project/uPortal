/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class update the group with a new name. It then updates
 * all of the xml elements with the new name.
 * @author Don Fracapane
 * @version $Revision$
 */
public class UpdateGroup extends GroupsManagerCommand {

   public UpdateGroup () {
   }

   /**
    * This is the public method
    * @throws Exception
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData = sessionData.runtimeData;
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String theCommand = runtimeData.getParameter("grpCommand");
      String newName = runtimeData.getParameter("grpName");
      String newDescription = runtimeData.getParameter("grpDescription");
      String updId = getCommandArg(runtimeData);
      Node titleNode;
      Element updElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME,
            updId);
      String updKey = updElem.getAttribute("key");
      String retMsg;
      String curName = GroupsManagerXML.getElementValueForTagName(updElem, "dc:title");
      if (curName == null || curName.equals("")) {
         Utility.logMessage("ERROR", "UpdateGroup::execute(): Cannot find dc:title element for: "
               + updElem.getAttribute("name"));
         return;
      }
      String curDescription = GroupsManagerXML.getElementValueForTagName(updElem, "dc:description");
      boolean hasChanged = false;
      if (!Utility.areEqual(curName, newName)) {
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): Group name: '" + curName
               + "' will be updated to : '" + newName + "'");
         hasChanged = true;
      }
      if (!Utility.areEqual(curDescription, newDescription)) {
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): Group: '" + newDescription
               + "' will be updated to : '" + newDescription + "'");
         hasChanged = true;
      }
      // Notify user if nothing was changed
      if (!hasChanged) {
         Utility.logMessage("DEBUG", "UpdateGroup::execute(): Update was not applied because nothing has been changed.");
         retMsg = "Update was not applied. No changes were entered.";
         sessionData.feedback = retMsg;
         return;
      }
      //IEntityGroup updGroup = GroupsManagerXML.retrieveGroup(updKey);
      ILockableEntityGroup updGroup = sessionData.lockedGroup;
      if (updGroup == null) {
         retMsg = "Unable to retrieve Group!";
         sessionData.feedback = retMsg;
         return;
      }
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to update group: " +
            curName);
      updGroup.setName(newName);
      updGroup.setDescription(newDescription);
      updGroup.updateAndRenewLock();
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): About to update xml nodes for group: "
            + curName);
      // update all xml nodes for this group
      GroupsManagerXML.refreshAllNodes(model, updGroup);
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Finished");
   }
}
