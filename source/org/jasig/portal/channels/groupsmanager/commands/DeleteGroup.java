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
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.IEntityGroup;
import  org.jasig.portal.groups.IGroupMember;
import  org.jasig.portal.groups.GroupsException;
import  org.jasig.portal.services.GroupService;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * This command deletes an IEntityGroup and removes all of it's associations to
 * IEntityGroups and IInitialGroupContexts. It then gathers all of the xml
 * nodes for the parent group and removes the child node of the removed member.
 * Removing an IGroupMember from an IInitialGroupContext means deleting
 * the reference to the IGroupMember in the IInitialGroupContextStore.

 */
public class DeleteGroup extends GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public DeleteGroup () {
   }

   /**
    * put your documentation comment here
    * @param runtimeData
    * @param staticData
    */
   public void execute (org.jasig.portal.ChannelRuntimeData runtimeData, ChannelStaticData staticData) {
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Start");
      DocumentImpl xmlDoc = (DocumentImpl)staticData.get("xmlDoc");
      String userID = runtimeData.getParameter("username");
      String theCommand = runtimeData.getParameter("grpCommand");
      String delId = getCommandIds(runtimeData);
      Element delElem = Utility.getElementByTagNameAndId(xmlDoc, GROUP_TAGNAME, delId);
      String delKey = delElem.getAttribute("key");
      String elemName = delElem.getAttribute("name");
      String retMsg;
      Node parentNode;
      Node deletedNode;
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Group: " + elemName + "will be deleted");
      try {
         IEntityGroup delGroup = GroupsManagerXML.retrieveGroup(delKey);
         if (delGroup == null) {
            retMsg = "Unable to retrieve Group!";
            runtimeData.setParameter("commandResponse", retMsg);
            return;
         }
         Utility.logMessage("DEBUG", "DeleteGroup::execute(): About to delete group: "
               + elemName);
         delGroup.delete();
         Utility.logMessage("DEBUG", "DeleteGroup::execute(): About to delete xml nodes for group: "
               + elemName);
         // remove all xml nodes for this group
         Iterator deletedNodes = Utility.getNodesByTagNameAndKey(xmlDoc, GROUP_TAGNAME,
               delKey);
         IEntityGroup parentEntGrp = null;
         String hasMbrs = "duh";
         while (deletedNodes.hasNext()) {
            deletedNode = (Node)deletedNodes.next();
            parentNode = deletedNode.getParentNode();
            boolean parentIsInitialGroupContext = parentIsInitialGroupContext(((Element)parentNode).getAttribute("id"));
            if (parentIsInitialGroupContext) {
               IInitialGroupContext igc = RDBMInitialGroupContextStore.singleton().find(userID, delKey);
               RDBMInitialGroupContextStore.singleton().delete(igc);
               hasMbrs = "true";
            }
            else {
               String nodeKey = ((Element)parentNode).getAttribute("key");
               if (parentEntGrp == null || !parentEntGrp.getKey().equals(nodeKey)) {
                  parentEntGrp = GroupsManagerXML.retrieveGroup(nodeKey);
                  hasMbrs = String.valueOf(parentEntGrp.hasMembers());
               }
            }
            /** @todo xmlCache: */
            parentNode.removeChild(deletedNode);
            ((Element)parentNode).setAttribute("hasMembers", hasMbrs);
         }
         /** @todo have to remove permissions associated with group */
         runtimeData.setParameter("grpMode", "browse");
         runtimeData.setParameter("grpView", "tree");
         runtimeData.setParameter("grpViewId", "0");
      } catch (GroupsException ge) {
         retMsg = "Unable to create new group\n" + ge;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "DeleteGroup::execute(): " + retMsg + ge);
      } catch (Exception e) {
         retMsg = "Unable to delete group : " + elemName;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "DeleteGroup::execute(): " + retMsg + ".\n" + e);
      }
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Finished");
   }
}



