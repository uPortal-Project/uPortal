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

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerCommandFactory;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.SearchResultsGroupImpl;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** This command delegates to the GroupsService to find entities requested
 *  by the user.
 * @author Don Fracapane
 * @version $Revision$
 */
public class Search extends GroupsManagerCommand {
   private static String grpPrefix = "IEntityGroup::";
   private static String[] methods;
   static {
      methods = new String[5];
      methods[1] = " is ";
      methods[2] = " starts with ";
      methods[3] = " ends with ";
      methods[4] = " contains ";
   }

   /**
    * put your documentation comment here
    */
   public Search () {
   }

   /**
    * This is the public method
    * @throws Exception
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      Utility.logMessage("DEBUG", "SearchForEntities::execute(): Start");
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData = sessionData.runtimeData;
      Class type;
      String grpTypeName = null;
      EntityIdentifier[] results;
      String label = null;
      boolean isGroupSearch;
      // if present, the command arg will be the ancestor
      String ancestorKey = getCommandArg(runtimeData);
      IEntityGroup entGrp = GroupsManagerXML.retrieveGroup(ancestorKey);
      String query = runtimeData.getParameter("grpQuery");
      String method = runtimeData.getParameter("grpMethod");
      int methodInt = Integer.parseInt(method);
      // For an EntityGroup search, the grpType will have the  form of "IEntityGroup::classname"
      // For an Entity search, the grpType will have the  form of "classname"
      String grpType = runtimeData.getParameter("grpType");
      String searchCriteria = "grpQuery." + query + "|" + "grpMethod." + method + "|"
            + "grpType." + grpType + "|" + "ancestor." + ancestorKey;
      if (grpType.startsWith(grpPrefix)) {
         isGroupSearch = true;
         grpTypeName = grpType.substring(grpPrefix.length());
      }
      else {
         isGroupSearch = false;
         grpTypeName = grpType;
      }
      type = Class.forName(grpTypeName);
      if (isGroupSearch) {
         label = "Group of " + org.jasig.portal.EntityTypes.getDescriptiveName(type) +
               "s";
         if (entGrp != null) {
            results = GroupService.searchForGroups(query, methodInt, type, entGrp);
         }
         else {
            results = GroupService.searchForGroups(query, methodInt, type);
         }
      }
      else {
         label = org.jasig.portal.EntityTypes.getDescriptiveName(type);
         if (entGrp != null) {
            results = GroupService.searchForEntities(query, methodInt, type, entGrp);
         }
         else {
            results = GroupService.searchForEntities(query, methodInt, type);
         }
      }
      Document model = getXmlDoc(sessionData);
      IEntityGroup sr = new SearchResultsGroupImpl(type);
      sr.setName("Search Results");
      sr.setDescription("Search for a " + label + " whose name" + methods[methodInt] + "'" + query + "'");
      for (int sub = 0; sub < results.length; sub++) {
         EntityIdentifier entID = results[sub];
         IGroupMember resultGroup = GroupService.getGroupMember(entID);
         sr.addMember(resultGroup);
      }
      Element searchElem = GroupsManagerXML.getGroupMemberXml(sr, true, null, model);
      searchElem.setAttribute("searchResults", "true");
      model.getDocumentElement().appendChild(searchElem);
      this.setCommandArg(sessionData.runtimeData, searchElem.getAttribute("id"));
      GroupsManagerCommandFactory.get("Highlight").execute(sessionData);
   }
}
