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

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  org.jasig.portal.*;
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Document;

/**
 * This command removes the association of a member element to either an
 * IEntityGroup or an IInitialGroupContext. It then gathers all of the xml
 * nodes for the parent group and removes the child node of the removed member.
 * Removing an IGroupMember from an IInitialGroupContext means deleting
 * the reference to the IGroupMember in the IInitialGroupContextStore.
 */
public class RemoveMember extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /** Creates new RemoveMember */
   public RemoveMember () {
   }

   /**
    * The execute() method is the main method for the RemoveMember command.
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

     Utility.logMessage("DEBUG", "RemoveMember::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String theCommand = getCommand(runtimeData);
      String cmdIds = getCommandArg(runtimeData);
      Object parentGroup = null;
      IGroupMember childGm = null;
      String hasMbrs = "duh";
      String userID = getUserID(sessionData);
      Utility.logMessage("DEBUG", "RemoveMember::execute(): About to get parent and child keys");
      String parentID = Utility.parseStringDelimitedBy("parent.", cmdIds, "|");
      String childID = Utility.parseStringDelimitedBy("child.", cmdIds, "|");
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Uid of parent element = "
            + parentID + " child element = " + childID);
      try {
         Element parentElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME,
               parentID);
         if (parentElem == null) {
            Utility.logMessage("ERROR", "RemoveMember::execute(): Unable to retrieve parent element!");
            return;
         }
         Utility.logMessage("DEBUG", "RemoveMember::execute(): About to get child element = "
               + childID);
         Element childElem = GroupsManagerXML.getElementById(model, childID);
         if (childElem == null) {
            Utility.logMessage("ERROR", "RemoveMember::execute(): Unable to retrieve Child element!");
            return;
         }
         // The child will always be an IGroupMember
         childGm = GroupsManagerXML.retrieveGroupMemberForElementId(model, childID);
         // The parent could be an IGroupMember or an IInitialGroupContext.
         if (parentIsInitialGroupContext(parentID)) {
            // Put method in GroupsManagerXML and change render method to use it
            IInitialGroupContext igc = RDBMInitialGroupContextStore.singleton().find(userID,
                  childElem.getAttribute("key"));
            RDBMInitialGroupContextStore.singleton().delete(igc);
            hasMbrs = "true";
         }
         else {
            // check for null
            parentGroup = (Object)GroupsManagerXML.retrieveGroupMemberForElementId(model, parentID);
            removeChildFromGroup(parentGroup, childGm);
            hasMbrs = String.valueOf(((IEntityGroup)parentGroup).hasMembers());
            Utility.logMessage("DEBUG", "RemoveMember::execute(): Got the parent group ");
         }
         Utility.logMessage("DEBUG", "RemoveMember::execute(): about to remove child elements");
         Iterator parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME,
               parentElem.getAttribute("key"));
         Node parentNode;
         NodeList childNodes;
         Node childNode;
         while (parentNodes.hasNext()) {
            parentNode = (Node)parentNodes.next();
            childNodes = parentNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
               childNode = (org.w3c.dom.Node)childNodes.item(i);
               if (((Element)childNode).getAttribute("key").equals(childElem.getAttribute("key"))) {
                  parentNode.removeChild(childNode);
                  ((Element)parentNode).setAttribute("hasMembers", hasMbrs);
               }
            }
         }
      } catch (Exception ge) {
         // We let groups catch any error for the adds (ie. group member is already in the parent group).
         // Processing subsequent adds is allowed to continue.
         sessionData.feedback = sessionData.feedback + "\n Unable to remove : child";
      }
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Completed");
   }

   /**
    * This section removes the selected member from an IEntityGroup.
    * @param parentGroup
    * @param childGm
    * @exception ChainedException
    */
   public void removeChildFromGroup (Object parentGroup, IGroupMember childGm) throws ChainedException {
      Utility.logMessage("DEBUG", "RemoveMember::removeChildrenFromGroup(): about to remove child");
      if (parentGroup != null && childGm != null) {
         // parentEntGrp can be an EntityGroup or an InitialGroupContext
         // SHOULD WRAPPER HANDLE THIS
         try {
            ((IEntityGroup)parentGroup).removeMember(childGm);
            ((IEntityGroup)parentGroup).updateMembers();
         } catch (GroupsException ge) {
            String aMsg = "Unable to remove child from parent/n" + ge;
            Utility.logMessage("ERROR", aMsg);
            throw  new ChainedException(aMsg, ge);
         }
      }
      else {
         throw  new ChainedException("Parent and/or child group members were not found");
      }
   }
}
