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
import  org.w3c.dom.Document;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;


/**
 * put your documentation comment here
 */
public abstract class GroupsManagerCommand
      implements org.jasig.portal.channels.groupsmanager.IGroupsManagerCommand, org.jasig.portal.channels.groupsmanager.GroupsManagerConstants {

   /**
    * GroupsManagerCommand is the parent of all Groups Manager commands. It
    * hold the commone functionality of all commands.
    */
   public GroupsManagerCommand () {
   }

   /**
    * put your documentation comment here
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) {}

   /**
    * Returns the grpCommand parameter from runtimeData
    * @param runtimeData
    * @return String
    */
   public String getCommand (org.jasig.portal.ChannelRuntimeData runtimeData) {
      return  (String)runtimeData.getParameter("grpCommand");
   }

   /**
    * Returns the userID from the user object
    * @param runtimeData
    * @return String
    */
   public String getUserID (CGroupsManagerSessionData sessionData) {
      return  String.valueOf(sessionData.user.getID());
   }

   
   /**
    * Returns the grpCommandIds parameter from runtimeData. The string usually
    * hold one element ID but could contain a string of delimited ids. (See
    * RemoveMember command).
    * @param runtimeData
    * @return String
    */
   public String getCommandArg (org.jasig.portal.ChannelRuntimeData runtimeData) {
      return  (String)runtimeData.getParameter("grpCommandArg");
   }

   /**
    * Returns the groupParentId parameter from staticData
    * @param staticData
    * @return String
    */
   public String getParentId (ChannelStaticData staticData) {
      return  staticData.getParameter("groupParentId");
   }

   /**
    * Returns the cached xml document from staticData
    * @param staticData
    * @return Document
    */
   public Document getXmlDoc (CGroupsManagerSessionData sessionData) {
      //return  (Document)staticData.get("xmlDoc");sessionData.model
      return  (Document)sessionData.model;
   }

   /**
    * Answers if the parentGroupId has been set. If it has not been set, this
    * would indicate that Groups Manager is in Servant mode.
    * @param staticData
    * @return boolean
    */
   public boolean hasParentId (ChannelStaticData staticData) {
      String pk = getParentId(staticData);
      if (pk == null) {
         return  false;
      }
      if (pk.equals("")) {
         return  false;
      }
      Utility.logMessage("Debug", "GroupsManagerCommand::hasParentId: Value is set to default: "
            + pk);
      return  true;
   }

   /**
    * The method answers if the parent is an InitialGroupContext.
    * Some functions operate on a colletion of selected item. In this case,
    * the command that starts the function sets the parent element at the
    * start. After selection is completed, the function operates on the
    * collection on behalf of the parent. This is best illustrated by looking
    * at the AddMembers command and the DoneWithSelection command.
    * @param staticData
    * @return boolean
    */
   public boolean parentIsInitialGroupContext (ChannelStaticData staticData) {
      return  ((hasParentId(staticData) && getParentId(staticData).equals("0")));
   }

   /**
    * The method answers if the parent is an InitialGroupContext.
    * @param parentID
    * @return boolean
    */
   public boolean parentIsInitialGroupContext (String parentID) {
      return  (parentID != null && parentID.equals("0"));
   }

   /**
    * clear out the selection list
    * @param staticData
    */
   public void clearSelected (CGroupsManagerSessionData sessionData) {
      ChannelStaticData staticData = sessionData.staticData;
      Element rootElem = getXmlDoc(sessionData).getDocumentElement();
      NodeList nGroupList = rootElem.getElementsByTagName(GROUP_TAGNAME);
      NodeList nPersonList = rootElem.getElementsByTagName(ENTITY_TAGNAME);
      NodeList nList = nGroupList;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         elem.setAttribute("selected", "false");
      }
      nList = nPersonList;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         elem.setAttribute("selected", "false");
      }
      return;
   }
}
