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
import java.util.*;
import java.io.*;
import org.jasig.portal.*;
import org.jasig.portal.channels.groupsmanager.*;
import org.jasig.portal.groups.*;
import org.jasig.portal.services.*;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

/** This command delegates to the GroupsService to find entities requested
 *  by the user.
 */
public class Search extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public Search() {
   }

   /**
    * put your documentation comment here
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) {
      Utility.logMessage("DEBUG", "SearchForEntities::execute(): Start");
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Class type;
      String grpTypeName = null;
      EntityIdentifier[] results;
      String grpPrefix = "IEntityGroup::";
      boolean isGroupSearch;
      // if present, the command arg will be the ancestor
      String ancestorKey = getCommandArg(runtimeData);
      IEntityGroup entGrp = GroupsManagerXML.retrieveGroup(ancestorKey);
      String query = runtimeData.getParameter("grpQuery");
      String method = runtimeData.getParameter("grpMethod");
      int methodInt = Integer.getInteger(method).intValue();
      // For an EntityGroup search, the grpType will have the  form of "IEntityGroup::classname"
      // For an Entity search, the grpType will have the  form of "classname"
      String grpType = runtimeData.getParameter("grpType");
      String searchCriteria = "grpQuery." + query + "|" + "grpMethod." + method + "|"
            + "grpType." + grpType + "|" + "ancestor." + ancestorKey;
      try{
         if (grpType.startsWith(grpPrefix)){
            isGroupSearch = true;
            grpTypeName = grpType.substring(grpPrefix.length());
         }
         else{
            isGroupSearch = false;
            grpTypeName = grpType;
         }
         type = Class.forName(grpTypeName);
         if (isGroupSearch){
            if (entGrp != null){
               results = GroupService.searchForGroups(query, methodInt, type, entGrp);
            }
            else{
               results = GroupService.searchForGroups(query, methodInt, type);
            }
         }
         else{
            if (entGrp != null){
               results = GroupService.searchForEntities(query, methodInt, type, entGrp);
            }
            else{
               results = GroupService.searchForEntities(query, methodInt, type);
            }
         }
         /* addional attributes:
            canEdit = "false
            criteria = "query::method::type::ancestor"
         */
         Document model = sessionData.model;
         Element searchElem = GroupsManagerXML.createElement("Search [" + query + "]", model, false);
         searchElem.setAttribute("id", "srch:"+GroupsManagerXML.getNextUid());
         //searchElem.setAttribute("key", "");
         searchElem.setAttribute("expanded", String.valueOf(results.length > 0));
         searchElem.setAttribute("searchCriteria", searchCriteria);
         searchElem.setAttribute("canEdit", "false");
         model.appendChild(searchElem);

         for (int sub=0 ; sub < results.length ; sub++) {
            EntityIdentifier entID = results[sub];
            IGroupMember resultGroup = (IGroupMember)GroupsManagerXML.retrieveGroup(entID.getKey());
            Element result = GroupsManagerXML.getGroupMemberXml(resultGroup, false, null, model);
            model.appendChild(searchElem);
         }
         sessionData.highlightedGroupID = searchElem.getAttribute("id");
         if((sessionData.lockedGroup!=null) && (!sessionData.lockedGroup.getEntityIdentifier().getKey().equals(sessionData.highlightedGroupID)) && (!sessionData.mode.equals("select"))){
         try{
            sessionData.lockedGroup.getLock().release();
         }
         catch(Exception e){}
         sessionData.lockedGroup = null;
         sessionData.mode = BROWSE_MODE;
         }
      }
      catch (GroupsException ge){
         Utility.logMessage("ERROR", "Search failed for criteria = " + searchCriteria + "/n" + ge);
      }
      catch (ClassNotFoundException cnfe){
         Utility.logMessage("ERROR", "Unable to instantiate class for " + grpTypeName + "/n" + cnfe);
      }
   }
}



