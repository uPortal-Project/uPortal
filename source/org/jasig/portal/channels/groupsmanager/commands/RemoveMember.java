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
import org.jasig.portal.channels.groupsmanager.ChainedException;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This command removes the association of a member element to an IEntityGroup
 * It then gathers all of the xml nodes for the parent group and removes the child
 * node of the removed member.
 * @author Don Fracapane
 * @version $Revision$
 */
public class RemoveMember extends GroupsManagerCommand {

   /** Creates new RemoveMember */
   public RemoveMember () {
   }

   /**
    * This is the public method
    * @throws Exception
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData = sessionData.runtimeData;
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
      //parentGroup = (Object)GroupsManagerXML.retrieveGroupMemberForElementId(model, parentID);
      parentGroup = sessionData.lockedGroup;
      removeChildFromGroup(parentGroup, childGm);
      hasMbrs = String.valueOf(((IEntityGroup)parentGroup).hasMembers());
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Got the parent group ");
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
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Completed");
   }

   /**
    * This section removes the selected member from an IEntityGroup.
    * @param parentGroup
    * @param childGm
    * @exception ChainedException
    */
   private void removeChildFromGroup (Object parentGroup, IGroupMember childGm) throws ChainedException {
      Utility.logMessage("DEBUG", "RemoveMember::removeChildrenFromGroup(): about to remove child");
      if (parentGroup != null && childGm != null) {
         try {
            ((IEntityGroup)parentGroup).removeMember(childGm);
            ((ILockableEntityGroup)parentGroup).updateMembersAndRenewLock();
         } catch (GroupsException ge) {
            String aMsg = "Unable to remove child from parent/n" + ge;
            Utility.logMessage("ERROR", aMsg);
            throw  new ChainedException(aMsg, ge);
         }
      }
      else {
         String suspect = (parentGroup==null ? "Parent" : "Child");
         throw  new ChainedException(suspect + " group member was not found");
      }
   }
}
