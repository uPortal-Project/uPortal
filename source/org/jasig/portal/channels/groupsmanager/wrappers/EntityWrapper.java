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
import  org.w3c.dom.Element;
import  org.apache.xerces.dom.DocumentImpl;
import  org.jasig.portal.groups.IGroupMember;
import  org.jasig.portal.groups.IEntity;


/**
 * Returns an xml element for a given IEntity or IEntity key.
 */
public class EntityWrapper extends GroupMemberWrapper {

   /** Creates new EntityWrapper */
   public EntityWrapper () {
      ELEMENT_TAGNAME = ENTITY_TAGNAME;
   }

   /**
    * Returns an xml element for a given IEntity.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public Element getXml (IGroupMember gm, Element anElem, DocumentImpl aDoc) {
      Element rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(ELEMENT_TAGNAME,
            aDoc, false));
      Utility.logMessage("DEBUG", "EntityWrapper.getXml(): START, Element: " + rootElem);
      try {
         IEntity ent = (IEntity) gm;
         rootElem.setAttribute("id", GroupsManagerXML.getNextUid());
         rootElem.setAttribute("key", gm.getKey());
         rootElem.setAttribute("type", gm.getLeafType().getName());
         rootElem.setAttribute("displayName", GroupsManagerXML.getEntityName(ent.getLeafType(),
               ent.getKey()));

         rootElem.setAttribute("selected", "false");
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityWrapper.getXml(): ERROR retrieving entity "
               + e.toString());
      }
      return  rootElem;
   }

    /**
    * Returns a GroupMember for a key.
    * @param aKey
    * @return IGroupMember
    */
   protected IGroupMember retrieveGroupMember (String aKey, String aType) {
      return (IGroupMember)GroupsManagerXML.retrieveEntity(aKey, aType);
   }

    /**
    * Answers if the GroupMember has to be retrieved.
    * @param anElem
    * @return boolean
    */
   protected boolean isRetrievalRequired (Element anElem) {
      // Retrieve the Entity only if some attributes are missing
      return (isElementFullyFormed(anElem));
   }

    /**
    * Answers whether the group element has all required attributes set. This will
    * be used to determine if the Entity will have to be retrieved in order to
    * populate the element with all required attributes. This test will fail if we are
    * using a cached element because the id attribute is not set in the cached element.
    * @param anElem
    * @return boolean
    */
   protected boolean isElementFullyFormed (Element anElem) {
      return (anElem.hasAttribute("id") && anElem.hasAttribute("key") && anElem.hasAttribute("type") && anElem.hasAttribute("displayName"));
   }

}

