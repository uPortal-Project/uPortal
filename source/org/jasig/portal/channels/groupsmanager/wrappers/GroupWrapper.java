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
public class GroupWrapper
      implements IGroupsManagerWrapper, GroupsManagerConstants {
   //SmartCache will hold group elements with a timeout of 5 minutes
   protected static final SmartCache grpsElementCache = new SmartCache(300);

   /** Creates new GroupWrapper */
   public GroupWrapper () {
   }

   /**
    * Returns an xml element for a given IEntityGroup key. The element that is returned
    * could be the same one that is passed in (usually with the expanded attribute
    * set to "true", a cached element (no further work is required), or a new element
    * (all attributes have to be set after the EntityGroups is retrieved).
    * @param aKey
    * @param anElem
    * @param aDoc
    * @return
    */
   public Element getXml (String aKey, String aType, Element anElem, DocumentImpl aDoc) {
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(" + aKey + "): START");
      Element rootElem = (anElem != null ? anElem : getNewElement (aKey, aDoc));
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(" + aKey + "): rootElem: " + rootElem);
      // Retrieve the EntityGroup only if some attributes are missing or the group
      // has been expanded. This test will fail if we are using a cached element.
      boolean isElemComplete = isElementFullyFormed(rootElem);
      boolean isElemExpanded = (Boolean.valueOf(rootElem.getAttribute("expanded")).booleanValue());
      Utility.logMessage("DEBUG", "GroupWrapper::getXml(" + aKey + "): EXPANDED: " + rootElem.getAttribute("expanded"));
      boolean isRetrievalRequired = (!isElemComplete || isElemExpanded);
      if (isRetrievalRequired) {
         IEntityGroup grp = GroupsManagerXML.retrieveGroup(aKey);
         Utility.logMessage("DEBUG", "GroupWrapper::getXml(" + aKey + "): grp: " + grp);
         getXml((IGroupMember)grp, rootElem, aDoc);
      }
      return  rootElem;
   }

   /**
    * Returns an xml element for a given IEntityGroup.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return
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
         rootElem.setAttribute("entityType",gm.getEntityType().getName());
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
    * Returns the xml element for a given IEntityGroup, populated with child elements.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public Element expandElement (IGroupMember gm, Element anElem, DocumentImpl aDoc) {
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
    * Returns the xml element cached for a given IEntityGroup key.
    * @param String aKey
    * @return Element
    */
   private Element getCachedElement (String aKey) {
      Utility.logMessage("DEBUG", "GroupWrapper::getCachedElement(): START, get KEY: " + aKey);
      Element cachedElem = (Element)grpsElementCache.get(aKey);
      return  (cachedElem == null ? cachedElem : (Element) cachedElem.cloneNode(false));
   }

   /**
    * Puts an element into the xml element cached.
    * @param String aKey
    * @param Element anElem
    * @return
    */
   private void putCachedElement (String aKey, Element anElem) {
      Utility.logMessage("DEBUG", "GroupWrapper::putCachedElement(): START, put KEY: " + aKey + " ELEM: " + anElem);
      // has to be a deep copy because the group has an RDF element. This means the
      // element has to be cached before it is expanded with child elements.
      Element cachedElem = (Element) anElem.cloneNode(false);
      grpsElementCache.put(aKey, cachedElem);
      return;
   }

   /**
    * Returns either the cached element for a given IEntityGroup key or creates a
    * new element for the key.
    * @param String aKey
    * @param aDoc
    * @return Element
    */
   private Element getNewElement (String aKey, DocumentImpl aDoc) {
      Utility.logMessage("DEBUG", "GroupWrapper::getNewElement(): START, KEY: " + aKey);
      Element retElem = (Element) getCachedElement(aKey);
      if (retElem != null) {
         Utility.logMessage("DEBUG", "GroupWrapper::getNewElement(): Cached Elem found");
         retElem.setAttribute("id", GroupsManagerXML.getNextUid());
         aDoc.adoptNode(retElem);
         Utility.logMessage("DEBUG", "GroupWrapper::getNewElement(): cachedElem adopted");
      }
      else {
         Utility.logMessage("DEBUG", "GroupWrapper::getNewElement(): cachedElem NOT found");
         retElem = GroupsManagerXML.createElement(GROUP_TAGNAME, aDoc, false);
      }
      return  retElem;
   }
   /**
    * Answers whether the group element has all required attributes set. This will
    * be used to determine if the EntityGroup will have to be retrieved in order to
    * populate the element with all required attributes. The only attribute that has
    * to be set from the EntityGroup is hasMembers. The others are can have default
    * values or values assigned without respect to the EntityGroup (eg. "id").
    * @param Element anElem
    * @return boolean
    */
   private boolean isElementFullyFormed (Element anElem) {
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



