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
public class EntityWrapper
      implements IGroupsManagerWrapper, GroupsManagerConstants {

   /** Creates new EntityWrapper */
   public EntityWrapper () {
   }

   /**
    * Returns an xml element for a given IEntity key.
    * @param aKey
    * @param anElem
    * @param aDoc
    * @return
    */
   public Element getXml (String aKey, String aType, Element anElem, DocumentImpl aDoc) {
      Element rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(ENTITY_TAGNAME,
            aDoc, false));
      IEntity ent = GroupsManagerXML.retrieveEntity(aKey, aType);
      getXml((IGroupMember)ent, rootElem, aDoc);
      return  rootElem;
   }

   /**
    * Returns an xml element for a given IEntity.
    * @param gm
    * @param anElem
    * @param aDoc
    * @return
    */
   public Element getXml (IGroupMember gm, Element anElem, DocumentImpl aDoc) {
      /** @todo xml cache: */
      Element rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(ENTITY_TAGNAME,
            aDoc, false));
      Utility.logMessage("DEBUG", "EntityWrapper.getXml(): START, Element: " + rootElem);
      try {
         //IEntity ent = GroupsManagerXML.retrieveEntity(gm.getKey());
         IEntity ent = (IEntity) gm;
         rootElem.setAttribute("id", GroupsManagerXML.getNextUid());
         rootElem.setAttribute("key", gm.getKey());
         rootElem.setAttribute("type", gm.getType().getName());
         rootElem.setAttribute("displayName", GroupsManagerXML.getGroupMemberName(ent.getKey(),
               ent.getEntityType().getName()));
         rootElem.setAttribute("selected", "false");
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityWrapper.getXml(): ERROR retrieving entity "
               + e.toString());
      }
      return  rootElem;
   }
}



