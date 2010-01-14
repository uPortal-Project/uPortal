/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.jasig.portal.channels.groupsmanager.wrappers;

import org.jasig.portal.channels.groupsmanager.CGroupsManagerUnrestrictedSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Returns an xml element for a given IEntity or IEntity key.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class EntityWrapper extends GroupMemberWrapper {

   /** Creates new EntityWrapper */
   public EntityWrapper () {
      ELEMENT_TAGNAME = ENTITY_TAGNAME;
   }

   /**
    * Returns an xml element for a given IEntity.
    * @param gm IGroupMember
    * @param anElem Element
    * @param sessionData CGroupsManagerUnrestrictedSessionData
    * @return Element
    */
   public Element getXml (IGroupMember gm, Element anElem, CGroupsManagerUnrestrictedSessionData sessionData) {
      Document aDoc = sessionData.model;
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
         // set user permissions for entity
         IGroupsManagerPermissions gmp = sessionData.gmPermissions;
         IAuthorizationPrincipal ap = sessionData.authPrincipal;
         applyPermissions (rootElem, gm, gmp, ap);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityWrapper.getXml(): ERROR retrieving entity "
               + e, e);
      }
      return  rootElem;
   }

    /**
    * Returns a GroupMember for a key.
    * @param aKey String
    * @param aType String
    * @return IGroupMember
    */
   protected IGroupMember retrieveGroupMember (String aKey, String aType) {
      return (IGroupMember)GroupsManagerXML.retrieveEntity(aKey, aType);
   }
}

