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
 * Returns an xml element for an IGroupMember.
 */
public abstract class GroupMemberWrapper
      implements IGroupsManagerWrapper, GroupsManagerConstants {
   //SmartCache will hold group elements with a timeout of 5 minutes
   private static final SmartCache XML_ELEMENT_CACHE = new SmartCache(300);
   protected String ELEMENT_TAGNAME ;

   /** Creates new GroupMemberWrapper */
   public GroupMemberWrapper () {
   }

   /**
    * Returns an xml element for a given IGroupMember key. The element that is returned
    * could be the same one that is passed in (usually with the expanded attribute
    * set to "true", a cached element (no further work is required), or a new element
    * (all attributes have to be set after the GroupMember is retrieved).
    * @param aKey
    * @param aType
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public Element getXml (String aKey, String aType, Element anElem, DocumentImpl aDoc) {
      Utility.logMessage("DEBUG", "GroupMemberWrapper::getXml(" + aKey + "): START");
      Element rootElem = (anElem != null ? anElem : getNewElement (aKey, aDoc));
      Utility.logMessage("DEBUG", "GroupMemberWrapper::getXml(" + aKey + "): rootElem: " + rootElem);
      if (isRetrievalRequired(rootElem)) {
         IGroupMember gm = retrieveGroupMember(aKey, aType);
         Utility.logMessage("DEBUG", "GroupMemberWrapper::getXml(" + aKey + "): grp: " + gm);
         getXml(gm, rootElem, aDoc);
      }
      return  rootElem;
   }

   /**
    * Returns an xml element for a given IGroupMember.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public abstract Element getXml (IGroupMember gm, Element anElem, DocumentImpl aDoc) ;

    /**
    * Returns a GroupMember for a key.
    * @param aKey
    * @return IGroupMember
    */
   protected abstract IGroupMember retrieveGroupMember (String aKey, String aType) ;

    /**
    * Answers if the group member has to be retrieved.
    * @param anElem
    * @return boolean
    */
   protected abstract boolean isRetrievalRequired (Element anElem) ;

   /**
    * Returns the xml element cached for a given IGroupMember key.
    * @param aKey
    * @return Element
    */
   protected Element getCachedElement (String aKey) {
      Utility.logMessage("DEBUG", "GroupMemberWrapper::getCachedElement(): START, get KEY: " + aKey);
      Element cachedElem = (Element)XML_ELEMENT_CACHE.get(aKey);
      return  (cachedElem == null ? cachedElem : (Element) cachedElem.cloneNode(false));
   }

   /**
    * Puts an element into the xml element cached.
    * @param aKey
    * @param anElem
    */
   protected void putCachedElement (String aKey, Element anElem) {
      Utility.logMessage("DEBUG", "GroupMemberWrapper::putCachedElement(): START, put KEY: " + aKey + " ELEM: " + anElem);
      // has to be a deep copy because the group has an RDF element. This means the
      // element has to be cached before it is expanded with child elements.
      Element cachedElem = (Element) anElem.cloneNode(false);
      XML_ELEMENT_CACHE.put(aKey, cachedElem);
      return;
   }

   /**
    * Returns either the cached element for a given IGroupMember key or creates a
    * new element for the key.
    * @param aKey
    * @param aDoc
    * @return Element
    */
   protected Element getNewElement (String aKey, DocumentImpl aDoc) {
      Utility.logMessage("DEBUG", "GroupMemberWrapper::getNewElement(): START, KEY: " + aKey);
      Element retElem = (Element) getCachedElement(aKey);
      if (retElem != null) {
         Utility.logMessage("DEBUG", "GroupMemberWrapper::getNewElement(): Cached Elem found");
         retElem.setAttribute("id", GroupsManagerXML.getNextUid());
         aDoc.adoptNode(retElem);
         Utility.logMessage("DEBUG", "GroupMemberWrapper::getNewElement(): cachedElem adopted");
      }
      else {
         Utility.logMessage("DEBUG", "GroupMemberWrapper::getNewElement(): cachedElem NOT found");
         retElem = GroupsManagerXML.createElement(ELEMENT_TAGNAME, aDoc, false);
      }
      return  retElem;
   }

   /**
    * Removes an element from the cache.
    * @param aKey
    */
   public void removeCachedElement (Object aKey) {
      Utility.logMessage("DEBUG", "GroupMemberWrapper::removeCachedElement(): START, remove KEY: ");
      XML_ELEMENT_CACHE.remove(aKey);
      return;
   }

}



