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
import  org.w3c.dom.Element;
import  org.apache.xerces.dom.DocumentImpl;

/**
 * If the children xml elements have not already been created, this command will
 * retrieve the group members and created the elements. This command then sets
 * the expanded attribute for an element to "true" and lets the transformation
 * handle the tree expansion display.
 */

public class ExpandGroup extends org.jasig.portal.channels.groupsmanager.commands.GroupsManagerCommand {

   /** Creates new ExpandGroup */
   public ExpandGroup () {
   }

   /**
    * put your documentation comment here
    * @param runtimeData
    * @param staticData
    */
   public void execute (org.jasig.portal.ChannelRuntimeData runtimeData, ChannelStaticData staticData) {
      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Start");
      DocumentImpl xmlDoc = getXmlDoc(staticData);
      // Due to the networked relationship of groups, the next method has to return a list of elements.
      String elemUid = getCommandIds(runtimeData);
      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Uid of expanded element = "
            + elemUid);
      Element expandedElem = Utility.getElementByTagNameAndId(xmlDoc, GROUP_TAGNAME, elemUid);
      String rootOwner;
      if (expandedElem != null) {
         //Utility.printElement(expandElem,"Group to be expanded was found (not null): \n" );
         boolean hasGroupsXML = !(expandedElem.getElementsByTagName(GROUP_TAGNAME).getLength()
               == 0);
         boolean hasEntitiesXML = !(expandedElem.getElementsByTagName(ENTITY_TAGNAME).getLength()
               == 0);
         boolean hasMembers = (expandedElem.getAttribute("hasMembers").equals("true"));
         Utility.logMessage("DEBUG", "ExpandGroup::execute(): Expanded element has Members = "
               + hasMembers);

         /** @todo xmlCache: */
         if (hasMembers) {
            expandedElem.setAttribute("expanded", "true");
            //if (!(hasGroupsXML || hasPersonsXML)) {
            Utility.logMessage("DEBUG", "ExpandGroup::execute(): About to retrieve children");
            //rootOwner = Utility.asString((DocumentImpl) expandElem.getOwnerDocument());
            //Utility.logMessage("DEBUG", "ExpandGroup::execute(): testing new printElement expanded element owner = " + rootOwner);
            IEntityGroup entGrp = GroupsManagerXML.retrieveGroup(expandedElem.getAttribute("key"));
            GroupsManagerXML.getGroupMemberXml((IGroupMember)entGrp, true, expandedElem,
                  xmlDoc);
            Utility.printDoc(xmlDoc, "renderXML: +++++++++ After children are retrieved +++++++++");
            //}
         }
      }
   }
}



