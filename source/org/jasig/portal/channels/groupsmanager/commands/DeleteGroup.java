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
import  org.jasig.portal.services.*;
import  org.jasig.portal.security.*;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
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
         // Needed to delete cached element
         IGroupsManagerWrapper rap = (IGroupsManagerWrapper)GroupsManagerWrapperFactory.instance().get(ENTITY_TAGNAME);

         IEntityGroup delGroup = GroupsManagerXML.retrieveGroup(delKey);
         if (delGroup == null) {
            retMsg = "Unable to retrieve Group!";
            runtimeData.setParameter("commandResponse", retMsg);
            return;
         }
         Utility.logMessage("DEBUG", "DeleteGroup::execute(): About to delete group: "
               + elemName);
         // remove permissions associated with group
         deletePermissions((IGroupMember)delGroup);
         // delete the group
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
            parentNode.removeChild(deletedNode);
            ((Element)parentNode).setAttribute("hasMembers", hasMbrs);
            // remove element from SmartCache
            rap.removeCachedElement(delKey);
         }

         /** Remove the permission elements in the xmlDoc */
         Node principalNode = (Node)xmlDoc.getDocumentElement().getElementsByTagName("principal").item(0);
         NodeList permElems = xmlDoc.getElementsByTagName("permission");
         /** If we delete from the bottom up, the NodeList elements shift down
          *  everytime we delete an element. Since the elements that we are looking
          *  for are sequential and because we increment the counter at the end of
          *  the loop, the element that we should process next slips down into the
          *  slot that we just processed. We therefore end up deleting every other
          *  element. The solution is to delete from the top down.
          */
         for (int i = permElems.getLength() - 1; i > -1; i--) {
            Element permElem = (Element)permElems.item(i);
            if (permElem.getAttribute("target").equals(delKey)) {
               principalNode.removeChild(permElem);
            }
         }
         runtimeData.setParameter("grpMode", "browse");
         runtimeData.setParameter("grpView", "tree");
         runtimeData.setParameter("grpViewId", "0");
      } catch (GroupsException ge) {
         retMsg = "Unable to delete group : " + elemName;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "DeleteGroup::execute(): " + retMsg + ge);
      } catch (ChainedException ce) {
         retMsg = "Unable to delete group : " + elemName;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "DeleteGroup::execute(): " + retMsg + ".\n" + ce);
      } catch (Exception e) {
         retMsg = "Unable to delete group : " + elemName;
         runtimeData.setParameter("commandResponse", retMsg);
         Utility.logMessage("ERROR", "DeleteGroup::execute(): " + retMsg + ".\n" + e);
      }
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Finished");
   }

   /**
    * Removes all of the permissions for a GroupMember. We need to get permissions
    * for the group as a principal and as a target. I am merging the 2 arrays into a
    * single array in order to use the transaction management in the RDBMPermissionsImpl.
    * If an exception is generated, I do not delete the group or anything else.
    * Possible Exceptions: AuthorizationException and GroupsException
    * @param grpMbr
    * @throws ChainedException
    */
   public static void deletePermissions (IGroupMember grpMbr) throws ChainedException{
      try {
         String grpKey = grpMbr.getKey();
         // first we retrieve all permissions for which the group is the principal
         IAuthorizationPrincipal iap = AuthorizationService.instance().newPrincipal(grpMbr);
         IPermission[] perms1 = iap.getAllPermissions();

         // next we retrieve all permissions for which the group is the target
         IUpdatingPermissionManager upm = AuthorizationService.instance().newUpdatingPermissionManager(OWNER);
         IPermission[] perms2 = upm.getPermissions(null, grpKey);

         // merge the permissions
         IPermission[] allPerms = new IPermission[perms1.length + perms2.length];
         System.arraycopy(perms1,0,allPerms,0,perms1.length);
         System.arraycopy(perms2,0,allPerms,perms1.length,perms2.length);

         upm.removePermissions(allPerms);
      }
      catch (Exception e) {
         String errMsg = "Utility::deletePermissions(): Error removing permissions for " + grpMbr;
         Utility.logMessage("ERROR", errMsg);
         throw new ChainedException(errMsg, e);
      }
   }
}



