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

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
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
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "CreateGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String theCommand = runtimeData.getParameter("grpCommand");
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
