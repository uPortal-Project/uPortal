/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * We will only be creating groups. We do not create entities. Once we create
 * the new group, it will be added to a parent and default permissions will
 * be assigned if the user is not in the portal adminstrators groups that automatically
 * has full access.
 * All of the xml  nodes for the parent group will be found and if the node is
 * expanded, the new child node will be added.
 * @author Don Fracapane
 * @version $Revision$
 */
 public class CreateGroup extends GroupsManagerCommand {

   /** Creates new CreateGroup */
   public CreateGroup () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "CreateGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String parentID = getCommandArg(runtimeData);
      String newGrpName = runtimeData.getParameter("grpNewName");
      Utility.logMessage("DEBUG", "CreateGroup::execute(): New grp: " + newGrpName +
            " will be added to parent element = " + parentID);
      ILockableEntityGroup parentGroup = null;
      Class parentEntityType;
      Element parentElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, parentID);
      String parentKey = parentElem.getAttribute("key");
      String retMsg;
      Iterator parentNodes;
      if (parentElem == null) {
         retMsg = "Unable to find Parent element!";
         sessionData.feedback = retMsg;
         Utility.logMessage("ERROR", "CreateGroup::execute(): " + retMsg);
         return;
      }
      Utility.logMessage("DEBUG", "CreateGroup::execute(): Parent element was found!");

      //parentGroup = GroupsManagerXML.retrieveGroup(parentKey);
      parentGroup = sessionData.lockedGroup;
      if (parentGroup == null) {
         retMsg = "Unable to retrieve Parent Entity Group!";
         sessionData.feedback = retMsg;
         return;
      }
      parentEntityType = parentGroup.getLeafType();
      //parentEntityType = (Class) GroupsManagerXML.getEntityTypes().get("Person");
      Utility.logMessage("DEBUG", "CreateGroup::execute(): About to create new group: "
            + newGrpName + " Type: " + parentEntityType.getName());
      String userID = sessionData.user.getEntityIdentifier().getKey();
      Utility.logMessage("DEBUG", "CreateGroup::execute(): userID = " + userID);
      IEntityGroup childEntGrp = GroupService.newGroup(parentEntityType);
      childEntGrp.setName(newGrpName);
      childEntGrp.setCreatorID(userID);
      childEntGrp.update();
      Utility.logMessage("DEBUG", "CreateGroup::execute(): About to add new group: "
         + newGrpName);
      parentGroup.addMember((IGroupMember)childEntGrp);
      parentGroup.updateMembersAndRenewLock();
      parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME, parentKey);
      // add new group to all parent group xml nodes
      while (parentNodes.hasNext()) {
         Element parentNode = (Element)parentNodes.next();
         GroupsManagerXML.getGroupMemberXml((IGroupMember)parentGroup, true, parentNode,
               sessionData.getUnrestrictedData());
         ((Element)parentNode).setAttribute("hasMembers", "true");
      }

      /** Grant all permissions for the new group to the creator only if the user is
       *  not in the portal administrators group
       */
      if (!sessionData.isAdminUser){
         GroupsManagerXML.createPermissions(sessionData, childEntGrp);
      }
      // Parent was locked so no other thread or process could have changed it, but
      // child members could have changed.
      GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);

      Utility.logMessage("DEBUG", "CreateGroup::execute(): Finished");
   }
}
