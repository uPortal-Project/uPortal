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
import  org.jasig.portal.services.*;
import  org.jasig.portal.security.*;
import  org.w3c.dom.Element;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Document;
import  javax.xml.parsers.*;

/**
 * We will only be creating groups. We do not create entities. Once we create
 * the new group, it will be added to a parent and default permissions will
 * be assigned. The parent can be either an IEntityGroup or the collection of
 * IInitialGroupContext depending upon the value of the parent element id (0
 * indicates the new group is being set as an Initial Group Context.
 * If the parent is an IEntityGroup, all of the xml  nodes for the parent group
 * will be found and if the node is expanded, the new child node will be added.
 * If the new group is an IInitialGroupContext, a new reference to the new
 * IGroupMember will be written to the IInitialGroupContextStore.
 */
public class CreateGroup extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /** Creates new CreateGroup */
   public CreateGroup () {
   }

   /**
    * The execute() method is the main method for the CreateMember command.
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "CreateGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String theCommand = runtimeData.getParameter("grpCommand");
      String parentID = getCommandArg(runtimeData);
      boolean parentIsInitialGroupContext = parentIsInitialGroupContext(parentID);
      String newGrpName = runtimeData.getParameter("grpNewName");
      Utility.logMessage("DEBUG", "CreateGroup::execute(): New grp: " + newGrpName +
            " will be added to parent element = " + parentID);
      IEntityGroup parentGroup = null;
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
      try {
         // The parent could be an IGroupMember or an IInitialGroupContext.
         if (!parentIsInitialGroupContext) {
            parentGroup = GroupsManagerXML.retrieveGroup(parentKey);
            if (parentGroup == null) {
               retMsg = "Unable to retrieve Parent Entity Group!";
               sessionData.feedback = retMsg;
               return;
            }
            else {
               parentEntityType = parentGroup.getLeafType();
            }
         }
         else {
            /** @todo A list will be presented to the user who will select the type
             *  of group to create */
            parentEntityType = Class.forName((String) GroupsManagerXML.getEntityTypes().get("IPerson"));
         }
         Utility.logMessage("DEBUG", "CreateGroup::execute(): About to create new group: "
               + newGrpName);
         // Next line creates a group that will hold iEntities
         String userID = getUserID(sessionData);
         IEntityGroup childEntGrp = GroupService.newGroup(parentEntityType);
         childEntGrp.setName(newGrpName);
         childEntGrp.setCreatorID(userID);
         childEntGrp.update();
         Utility.logMessage("DEBUG", "CreateGroup::execute(): About to add new group: "
               + newGrpName);
         if (parentIsInitialGroupContext) {
            IInitialGroupContext igc = Utility.createInitialGroupContext(userID, "p",
                  childEntGrp.getKey(), 1, false, null);
            igc.update();
            Node parentNode = (Node)parentElem;
            Element childElem = GroupsManagerXML.getGroupMemberXml((IGroupMember)childEntGrp,
                  false, null, model);
            parentNode.appendChild((Node)childElem);
         }
         else {
            parentGroup.addMember((IGroupMember)childEntGrp);
            parentGroup.updateMembers();
            parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME, parentKey);
            // add new group to all parent group xml nodes
            while (parentNodes.hasNext()) {
               Element parentNode = (Element)parentNodes.next();
               GroupsManagerXML.getGroupMemberXml((IGroupMember)parentGroup, true, parentNode,
                     model);
               ((Element)parentNode).setAttribute("hasMembers", "true");
            }
         }

         /** Grant all permissions for the new group to the creator */
         /** @todo need to catch following exceptions for next block of code
          *  org.jasig.portal.AuthorizationException
          *  java.lang.IllegalAccessException
          *  java.lang.InstantiationException */
         ArrayList perms = new ArrayList();
         IUpdatingPermissionManager upm = AuthorizationService.instance().newUpdatingPermissionManager(OWNER);
         IAuthorizationPrincipal ap = staticData.getAuthorizationPrincipal();
         Utility.logMessage("DEBUG", "CreateGroup::execute(): The IAuthorizationPrincipal: " + ap);
         String[] activities = ((IPermissible)Class.forName(OWNER).newInstance()).getActivityTokens();
         IPermission prm;
         for (int a = 0; a < activities.length; a++) {
            prm = upm.newPermission(ap);
            prm.setActivity(activities[a]);
            prm.setTarget(childEntGrp.getKey());
            prm.setType("GRANT");
            perms.add(prm);
         }
         upm.addPermissions((IPermission[])perms.toArray(new IPermission[perms.size()]));

         // create permission elements
         /** @todo should make sure there is one and only one principal element */
         NodeList principals = model.getDocumentElement().getElementsByTagName("principal");
         Element princElem = (Element)principals.item(0);
         for (int p = 0; p < perms.size(); p++) {
            prm = (IPermission)perms.get(p);
            Element permElem = GroupsManagerXML.getPermissionXml(model, prm.getPrincipal(), prm.getActivity(), prm.getType(), prm.getTarget());
            /** @todo should we check if element already exists??? */
            princElem.appendChild(permElem);
         }
         // Parent was locked so no other thread or process could have changed it, but
         // child members could have changed.
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(model, parentElem);
      } catch (GroupsException ge) {
         retMsg = "Unable to create new group\n" + ge;
         sessionData.feedback = retMsg;
         Utility.logMessage("ERROR", "CreateGroup::execute(): " + retMsg + "\n" + ge);
      } catch (ClassNotFoundException cnfe) {
         retMsg = "Unable to instantiate class " + GROUP_CLASSNAME;
         sessionData.feedback = retMsg;
         Utility.logMessage("ERROR", "CreateGroup::execute(): " + retMsg + "\n" + cnfe);
      } catch (Exception e) {
         retMsg = "Unable to create group";
         sessionData.feedback = retMsg;
         Utility.logMessage("ERROR", "CreateGroup::execute(): " + retMsg + ".\n" + e);
      }
      Utility.logMessage("DEBUG", "CreateGroup::execute(): Finished");
   }
}
