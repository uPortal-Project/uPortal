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

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.utils.SmartCache;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  org.apache.xerces.dom.DocumentImpl;


/**
 * Returns an xml element for a given IEntityGroup or IEntityGroup key.
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
   public Element getXml (IGroupMember gm, Element anElem, DocumentImpl aDoc) {
      String nextID;
      IEntityGroup entGrp = (IEntityGroup)gm;
      Element rootElem = (anElem != null ? anElem : getNewElement (gm.getKey(), aDoc));
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(): START, Element Expanded: " + rootElem.getAttribute("expanded"));
      try {
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): IEntityGroup: " + gm);
         String uid = rootElem.getAttribute("id");
         if (uid == null || uid.equals("")) {
            nextID = GroupsManagerXML.getNextUid();
            rootElem.setAttribute("id", nextID);
         }
         rootElem.setAttribute("key", gm.getKey());
         rootElem.setAttribute("entityType",gm.getLeafType().getName());
         rootElem.setAttribute("type", gm.getType().getName());
         boolean hasMems = gm.hasMembers();
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): gm.hasMembers(): " + hasMems);
         if (!hasMems) {
            rootElem.setAttribute("expanded", "false");
         }
         boolean isGroupExpanded = (Boolean.valueOf(rootElem.getAttribute("expanded")).booleanValue());
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): Expanded = " + isGroupExpanded);
         if (rootElem.getAttribute("selected") == null || !rootElem.getAttribute("selected").equals("true")) {
            rootElem.setAttribute("selected", "false");
         }
         rootElem.setAttribute("hasMembers", String.valueOf(hasMems));
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(): hasMembers = " + String.valueOf(hasMems));
         // no need to recreate the rdf element
         NodeList nList = rootElem.getElementsByTagName("rdf:RDF");
         if (nList.getLength() == 0) {
            Utility.logMessage("DEBUG", "GroupWrapper::getXml(): CREATING ELEMENT RDF");
            Element rdf = GroupsManagerXML.createRdfElement(entGrp.getName(), entGrp.getDescription(),
                  entGrp.getCreatorID(), aDoc);
            Utility.logMessage("DEBUG", "GroupWrapper::getXml(): APPENDING rdf element TO GRPROOT");
            rootElem.appendChild(rdf);
         }
         // check if element is in the cache
         if (getCachedElement(gm.getKey()) == null) {
            putCachedElement(gm.getKey(), rootElem);
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
    * @return IGroupMember
    */
   protected IGroupMember retrieveGroupMember (String aKey, String aType) {
      return (IGroupMember)GroupsManagerXML.retrieveGroup(aKey);
   }

    /**
    * Answers if the group member has to be retrieved.
    * @param anElem
    * @return boolean
    */
   protected boolean isRetrievalRequired (Element anElem) {
      // Retrieve the EntityGroup only if some attributes are missing or the group
      // has been expanded.
      String aKey = anElem.getAttribute("key");
      boolean isElemComplete = isElementFullyFormed(anElem);
      boolean isElemExpanded = (Boolean.valueOf(anElem.getAttribute("expanded")).booleanValue());
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(" + aKey + "): EXPANDED: " + anElem.getAttribute("expanded"));
      return (!isElemComplete || isElemExpanded);
   }

    /**
    * Returns the xml element for a given IEntityGroup, populated with child elements.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   private Element expandElement (IGroupMember gm, Element anElem, DocumentImpl aDoc) {
      Utility.logMessage("DEBUG", "GroupWrapper::expandElement(): START");
      java.util.Iterator gmItr = null;
      IGroupMember aChildGm = null;
      Element tempElem = null;
      try {
         boolean isGroupExpanded = (Boolean.valueOf(anElem.getAttribute("expanded")).booleanValue());
         if (isGroupExpanded) {
            Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  HERE COME THE KIDS");
            try {
               gmItr = gm.getMembers();
            } catch (Exception e) {
               Utility.logMessage("ERROR", "GroupWrapper::expandElement():  \n" + e.toString());
            }
            NodeList nl = anElem.getChildNodes();
            String aKey = gm.getKey();
            while (gmItr.hasNext()) {
               aChildGm = (IGroupMember)gmItr.next();
               String childKey = aChildGm.getKey();
               Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  " + aChildGm);
               boolean isPresent = false;
               if (aChildGm.isGroup()){
                isPresent = Utility.getNodesByTagNameAndKey(anElem, GROUP_TAGNAME,
                     childKey).hasNext();
               }
               else {
                  isPresent = Utility.getNodesByTagNameAndKey(anElem, ENTITY_TAGNAME,
                     childKey).hasNext();
               }
               if (!isPresent) {
                  tempElem = GroupsManagerXML.getGroupMemberXml(aChildGm,false, null, aDoc);
                  Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  APPENDING "
                        + tempElem.getNodeName());
                  anElem.appendChild(tempElem);
                  Utility.logMessage("DEBUG", "GroupWrapper::expandElement():  APPENDING ACCOMPLISHED");
               }
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupWrapper::expandElement(): ERROR retrieving entity "
               + e.toString());
         //throw new ChainedException("Exception from GroupsWrapper::expandElement", e);
      }
      return  anElem;
   }

   /**
    * Answers whether the group element has all required attributes set. This will
    * be used to determine if the EntityGroup will have to be retrieved in order to
    * populate the element with all required attributes. The only attribute that has
    * to be set from the EntityGroup is hasMembers. The others are can have default
    * values or values assigned without respect to the EntityGroup (eg. "id"). This
    * test will fail if we are using a cached element because the id attribute is
    * not set in the cached element.
    * @param anElem
    * @return boolean
    */
   protected boolean isElementFullyFormed (Element anElem) {
      Utility.logMessage("DEBUG", "GroupWrapper::isElementFullyFormed(): START, ELEM: " + anElem);
      //check if element is null
      boolean isGood = (anElem != null);
      //check for required attributes
      if (isGood) {
         isGood = (anElem.hasAttribute("hasMembers") && anElem.hasAttribute("id") && anElem.hasAttribute("key") && anElem.hasAttribute("expanded") && anElem.hasAttribute("selected"));
      }
      //check for presence of rdf element
      if (isGood) {
         NodeList nList = anElem.getElementsByTagName("rdf:RDF");
         isGood = (nList.getLength() != 0);
      }
      Utility.logMessage("DEBUG", "GroupWrapper::isElementFullyFormed(): RETVAL: " + isGood);
      return  isGood;
   }

}



