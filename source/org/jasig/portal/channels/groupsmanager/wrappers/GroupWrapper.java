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

package  org.jasig.portal.channels.groupsmanager.wrappers;

import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Returns an xml element for a given IEntityGroup or IEntityGroup key.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupWrapper extends GroupMemberWrapper {

   /** Creates new GroupWrapper */
   public GroupWrapper () {
      ELEMENT_TAGNAME = GROUP_TAGNAME;
   }

   /**
    * Returns an xml element for a given IEntityGroup.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public Element getXml (IGroupMember gm, Element anElem, Document aDoc) {
      String nextID;
      IEntityGroup entGrp = (IEntityGroup)gm;
      Element rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(ELEMENT_TAGNAME, aDoc, false));
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(): START, Element Expanded: " + rootElem.getAttribute("expanded"));
      try {
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): IEntityGroup: " + gm);
         String uid = rootElem.getAttribute("id");
         if (Utility.areEqual(uid, "")) {
            nextID = GroupsManagerXML.getNextUid();
            rootElem.setAttribute("id", nextID);
         }
         rootElem.setAttribute("key", gm.getKey());
         rootElem.setAttribute("entityType",gm.getLeafType().getName());
         rootElem.setAttribute("type", gm.getType().getName());
         rootElem.setAttribute("editable", String.valueOf(entGrp.isEditable()));
         boolean hasMems = gm.hasMembers();
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): gm.hasMembers(): " + hasMems);
         if (!hasMems) {
            rootElem.setAttribute("expanded", "false");
         }
         boolean isGroupExpanded = (Boolean.valueOf(rootElem.getAttribute("expanded")).booleanValue());
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): Expanded = " + isGroupExpanded);
         if (!Utility.areEqual(rootElem.getAttribute("selected"), "true")) {
            rootElem.setAttribute("selected", "false");
         }
         rootElem.setAttribute("hasMembers", String.valueOf(hasMems));
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): hasMembers = " + String.valueOf(hasMems));
         // If no rdf element, create it, otherwise refresh the element
         NodeList nList = rootElem.getElementsByTagName("rdf:RDF");
         if (nList.getLength() == 0) {
            Utility.logMessage("DEBUG", "GroupWrapper::getXml(): CREATING ELEMENT RDF");
            Element rdf = GroupsManagerXML.createRdfElement(entGrp, aDoc);
            Utility.logMessage("DEBUG", "GroupWrapper::getXml(): APPENDING rdf element TO GRPROOT");
            rootElem.appendChild(rdf);
         }
         else{
            GroupsManagerXML.refreshAllNodesIfRequired(aDoc, rootElem);
         }
         if (isGroupExpanded) {
            expandElement(gm, rootElem, aDoc);
         }
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): FINISHED");
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupWrapper::getXml(): ERROR retrieving entity "
               + e.toString());
         //throw new ChainedException("Exception from GroupsWrapper::getXml", e);
      }
      return  rootElem;
   }

    /**
    * Returns a GroupMember for a key.
    * @param aKey
    * @param aType
    * @return IGroupMember
    */
   protected IGroupMember retrieveGroupMember (String aKey, String aType) {
      return (IGroupMember)GroupsManagerXML.retrieveGroup(aKey);
   }

    /**
    * Returns the xml element for a given IEntityGroup, populated with child elements.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   private Element expandElement (IGroupMember gm, Element anElem, Document aDoc) {
      Utility.logMessage("DEBUG", "GroupWrapper::expandElement(): START");
      Utility.logMessage("DEBUG", "GroupWrapper::expandElement(): Group Member: " + gm);
      Utility.logMessage("DEBUG", "GroupWrapper::expandElement(): Element: " + anElem);
      //We only want to expand the element if the attribute is set to "true"
      if (!Utility.areEqual(anElem.getAttribute("expanded"), "true")) {return anElem;}
      java.util.Iterator gmItr = null;
      IGroupMember aChildGm = null;
      Element tempElem = null;
      try {
         Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  HERE COME THE KIDS");
         gmItr = gm.getMembers();
         String aKey = gm.getKey();
         // add new elements for new group members
         while (gmItr.hasNext()) {
            aChildGm = (IGroupMember)gmItr.next();
            String childKey = aChildGm.getKey();
            Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  " + aChildGm);
            boolean memberElementFound = false;
            String tagname = (aChildGm.isGroup() ? GROUP_TAGNAME : ENTITY_TAGNAME);
            memberElementFound = GroupsManagerXML.getNodesByTagNameAndKey(anElem, tagname,
                  childKey).hasNext();
            if (!memberElementFound) {
               tempElem = GroupsManagerXML.getGroupMemberXml(aChildGm,false, null, aDoc);
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  APPENDING "
                     + tempElem.getNodeName());
               anElem.appendChild(tempElem);
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  APPENDING ACCOMPLISHED");
            }
         }

         // Remove elements for groups that are no longer members
         // Remember that getChildNodes does not only return IGroupMember elements
         NodeList nList = anElem.getChildNodes();
         for (int i = 0; i < nList.getLength(); i++) {
            tempElem = (Element)nList.item(i);
            if (Utility.notEmpty(tempElem.getAttribute("entityType"))){
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  Checking if child element (id = " + tempElem.getAttribute("id") + ") still a member");
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  child element (key = " + tempElem.getAttribute("key") + ")");
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  child element (entityType = " + tempElem.getAttribute("entityType") + ")");
               aChildGm = GroupsManagerXML.retrieveGroupMemberForElement (tempElem);
               if (aChildGm == null || !gm.contains(aChildGm)){
                  Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  About to remove child element");
                  anElem.removeChild((Node)tempElem);
               }
            }
         }

      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupWrapper::expandElement(): ERROR expanding \nElement: "
            + anElem + "\nFor group member: " + gm + "\n" + e.toString());
         //throw new ChainedException("Exception from GroupsWrapper::expandElement", e);
      }
      return  anElem;
   }
}
