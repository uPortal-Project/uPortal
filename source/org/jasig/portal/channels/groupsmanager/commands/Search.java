/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.ChannelRuntimeData;
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
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      Utility.logMessage("DEBUG", "SearchForEntities::execute(): Start");
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
      sr.setCreatorID("CGroupsManager");
      for (int sub = 0; sub < results.length; sub++) {
         EntityIdentifier entID = results[sub];
         IGroupMember resultGroup = GroupService.getGroupMember(entID);
         sr.addMember(resultGroup);
      }
      Element searchElem = GroupsManagerXML.getGroupMemberXml(sr, true, null, sessionData.getUnrestrictedData());
      searchElem.setAttribute("searchResults", "true");
      model.getDocumentElement().appendChild(searchElem);
      this.setCommandArg(sessionData.runtimeData, searchElem.getAttribute("id"));
      GroupsManagerCommandFactory.get("Highlight").execute(sessionData);
   }
}
