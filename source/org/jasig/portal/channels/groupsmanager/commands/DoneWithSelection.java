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
import  org.jasig.portal.security.IAuthorizationPrincipal;
import  org.jasig.portal.services.*;
import  org.w3c.dom.Document;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;


/** A select cycle could be started in Servant mode or it could be started by
 *  the AddMembers command. The AddMembers command sets the id of the parent
 *  group (ie. the group to which child  members will be added). Control is then
 *  passed to a selection view where the child group members will be selected
 *  for addition. When the selection has been completed by the user, the
 *  DoneWithSelection command will be invoked where the selected members are
 *  added to a collection of IAuthorizationPrincipal. If a parent group had
 *  been set the children groupmembers are actually added to the parent group.
 *  If in Servant mode, the collection is simply returned to the master channel.
 *  Alternatively, the CancelSelection command have been invoked by the user to
 *  cancel the selection process and reset the mode and view control parameters.
 */
/** @todo LOOK FOR COMMON FUNCTIONALITY AGAIN AND SPLIT OUT
 *  IE ID => ELEMENT => GROUPMEMBER (EITHER ENTITY OR GROUP)
 */
public class DoneWithSelection extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /** Creates new AddMember */
   public DoneWithSelection () {
   }

   /**
    * put your documentation comment here
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) {
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Start");
      String oldCommand = runtimeData.getParameter("grpCommand");
      // First: gather the selected items
      GroupsManagerCommandFactory cf = GroupsManagerCommandFactory.instance();
      String theCommand = "Select";
      runtimeData.setParameter("grpCommand", theCommand);
      IGroupsManagerCommand c = cf.get(theCommand);
      if (c != null) {
         c.execute(sessionData);
      }
      runtimeData.setParameter("grpCommand", oldCommand);
      String parentId = null;
      boolean hasParentId = hasParentId(staticData);
      try {
         Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Parent ID is set: "
               + hasParentId);
         IGroupMember[] princResults = null;
         Document xmlDoc = getXmlDoc(sessionData);
         Element rootElem = xmlDoc.getDocumentElement();
         NodeList nGroupList = rootElem.getElementsByTagName(GROUP_TAGNAME);
         NodeList nEntityList = rootElem.getElementsByTagName(ENTITY_TAGNAME);
         Vector gmCollection = new Vector();
         Element parentElem = null;
         String cmdResponse;
         try {
            Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Starting group process");
            addGroupMemberToCollection(gmCollection, nGroupList);
            Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Starting entity process");
            addGroupMemberToCollection(gmCollection, nEntityList);
            // null pointer exception
         } catch (org.jasig.portal.AuthorizationException ae) {
            Utility.logMessage("ERROR", "DoneWithSelection::execute():AuthorizationException /n "
                  + ae);
         } catch (ClassNotFoundException cnfe) {
            Utility.logMessage("ERROR", "DoneWithSelection::execute():ClassNotFoundException /n "
                  + cnfe);
         }
         // check if selections were made
         if (gmCollection.size() <1) {
            cmdResponse = runtimeData.getParameter("commandResponse") + "\n No groups or people were selected! ";
            runtimeData.setParameter("commandResponse", cmdResponse);
            return;
         }

         /** Presence of parentID means the process is not in servant mode. That is,
          * the master channel is the Groups Manager channel and AddMembers had
          * been selected
          */
         if (hasParentId) {
            parentId = getParentId(staticData);
            Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Parent ID is set to: "
                  + parentId);
            parentElem = GroupsManagerXML.getElementByTagNameAndId(xmlDoc, GROUP_TAGNAME, parentId);
            if (parentElem == null) {
               Utility.logMessage("ERROR", "DoneWithSelection::execute: Error parent element not found");
               return;
            }
            /** @todo refactor: */
            if (parentIsInitialGroupContext(staticData)) {
               addChildrenToContext(gmCollection, runtimeData, parentElem, xmlDoc);
            }
            else {
               addChildrenToGroup(gmCollection, runtimeData, parentElem, xmlDoc);
            }
            clearSelected(sessionData);
            runtimeData.setParameter("grpMode", "browse");
            runtimeData.setParameter("grpView", "edit");
            runtimeData.setParameter("grpViewId", parentId);
            staticData.remove("groupParentId");
         }
         else {
            princResults = (IGroupMember[])gmCollection.toArray(new IGroupMember[0]);
            if (princResults.length > 0) {
               staticData.put("princResults", princResults);
               staticData.setParameter("groupManagerFinished", "true");
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "DoneWithSelection Error: " + runtimeData.getParameter("commandResponse")
               + "/n" + e);
      }
   }

   /**
    * This method processes the xml document looking for selected groupmembers.
    * It then creates an instance of IGroupMember for each selected
    * member and passes the collection back.
    * @param gmCollection
    * @param nList
    * @exception AuthorizationException
    * @exception ClassNotFoundException
    */
   public void addGroupMemberToCollection (Vector gmCollection, NodeList nList) throws org.jasig.portal.AuthorizationException,
         ClassNotFoundException {
      boolean addit;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         if (elem.getAttribute("selected") != null && elem.getAttribute("selected").equals("true")) {
            addit = true;
            Iterator gmItr = gmCollection.iterator();
            while (gmItr.hasNext()) {
               IGroupMember ggmm = (IGroupMember)gmItr.next();
               if ((ggmm.getKey().equals(elem.getAttribute("key")))
                        && (ggmm.getType().equals(elem.getAttribute("type")))){
                       addit = false;
                       break;
               }
            }
            if (addit) {
               IGroupMember gm = Utility.retrieveGroupMemberForKeyAndType(elem.getAttribute("key"),elem.getAttribute("type"));
               gmCollection.add(gm);
               Utility.logMessage("DEBUG", "DoneWithSelection::addGroupMemberToCollection(): " +
                     "- adding group member" + elem.getAttribute("key"));
            }
         }
      }
   }

   /**
    * This section adds the selected members to an IEntityGroup.
    * @param gmCollection
    * @param runtimeData
    * @param parentElem
    * @param xmlDoc
    */
   public void addChildrenToGroup (Vector gmCollection, ChannelRuntimeData runtimeData,
      Element parentElem, Document xmlDoc) {
      Element parent;
      IEntityGroup parentGroup = null;
      IGroupMember childGm = null;
      Element childElem;
      String parentName;
      String childName = "";
      parentGroup = GroupsManagerXML.retrieveGroup(parentElem.getAttribute("key"));
      Iterator gmItr = gmCollection.iterator();
      while (gmItr.hasNext()) {
         childGm = (IGroupMember) gmItr.next();
         try {
            childName = GroupsManagerXML.getEntityName(childGm.getType(), childGm.getKey());
         }
         catch (Exception e){
            Utility.logMessage("ERROR", "DoneWithSelection::execute: erorr"+e);
         }
         parentName = parentGroup.getName();
         Utility.logMessage("DEBUG", "DoneWithSelection::execute: About to add child");
         try {
            // add to parent group
            parentGroup.addMember(childGm);
            // update parent group
            parentGroup.updateMembers();
            // get parent element(s) and add element for child group member
            Iterator parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(xmlDoc, GROUP_TAGNAME,
                  parentElem.getAttribute("key"));
            while (parentNodes.hasNext()) {
               parent = (Element)parentNodes.next();

               childElem = GroupsManagerXML.getGroupMemberXml(childGm, false, null, xmlDoc);
               parent.appendChild((Node)childElem);
               parent.setAttribute("hasMembers", "true");
            }
         } catch (GroupsException ge) {
            // We let groups catch any error for the adds (ie. group member is already in the parent group).
            // Processing subsequent adds is allowed to continue.
            String cmdResponse = runtimeData.getParameter("commandResponse") + "\n Unable to add : "
                  + childName + " to: " + parentName;
            runtimeData.setParameter("commandResponse", cmdResponse);
         }
      }
   }

   /**
    * This section adds the selected members to an IInitialContextGroup.
    * @param gmCollection
    * @param runtimeData
    * @param parentElem
    * @param xmlDoc
    */
   public void addChildrenToContext (Vector gmCollection, ChannelRuntimeData runtimeData,
         Element parentElem, Document xmlDoc) {
      // Considerations:
      // The parent element is myGroups and there is only one.
      String childName = "";
      IGroupMember childGm = null;
      String userID = runtimeData.getParameter("username");
      String ownerType = "p";
      int ordinal = 1;
      boolean expanded = false;
      /** @todo should put this in the RDBM add method */
      java.sql.Timestamp dateCreated = new java.sql.Timestamp(System.currentTimeMillis());
      Element childElem;
      Iterator gmItr = gmCollection.iterator();
      while (gmItr.hasNext()) {
         childGm = (IGroupMember) gmItr.next();
         String type = "";
         try{
            childName = GroupsManagerXML.getEntityName(childGm.getType(), childGm.getKey());
            type = childGm.getType().getName();
         }
         catch (Exception e){
            Utility.logMessage("ERROR", "DoneWithSelection::execute: erorr"+e);
         }
         String groupID = childGm.getKey();

         // can only add groups as initial group contexts
         if (type.equals(GROUP_CLASSNAME)) {
            Utility.logMessage("DEBUG", "DoneWithSelection::addChildrenToContext: About to add child");
            try {
               // add to users initial contexts
               IInitialGroupContext igc = Utility.createInitialGroupContext(userID, ownerType,
                     groupID, ordinal, expanded, dateCreated);
               // save to persistent source
               igc.update();

               // add child to user's igc node
               IEntityGroup entGrp = GroupsManagerXML.retrieveGroup(groupID);
               childElem = GroupsManagerXML.getGroupMemberXml((IGroupMember)entGrp, false,
                     null, xmlDoc);
               parentElem.appendChild((Node)childElem);
               parentElem.setAttribute("hasMembers", "true");
            } catch (Exception e) {
               String cmdResponse = runtimeData.getParameter("commandResponse") + "\n Unable to add : "
                     + childName;
               runtimeData.setParameter("commandResponse", cmdResponse);
            }
         }
      }
   }
}



