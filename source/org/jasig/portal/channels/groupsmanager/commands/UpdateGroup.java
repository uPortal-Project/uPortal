/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
    * @param sessionData
    * @throws Exception
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
      GroupsManagerXML.refreshAllNodes(sessionData.getUnrestrictedData(), updGroup);
      Utility.logMessage("DEBUG", "UpdateGroup::execute(): Finished");
   }
}
