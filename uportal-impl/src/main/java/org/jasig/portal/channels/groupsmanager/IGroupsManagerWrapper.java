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

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.groups.IGroupMember;
import org.w3c.dom.Element;

/**
 * Defines the interface for a wrapper object to be used by CGroupssManager
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IGroupsManagerWrapper {

   /**
    * Return an element for an IEntity key
    * @param aKey String
    * @param aType String
    * @param anElem Element
    * @param sessionData CGroupsManagerUnrestrictedSessionData
    * @return Element
    */
   public Element getXml (String aKey, String aType, Element anElem, CGroupsManagerUnrestrictedSessionData sessionData);

   /**
    * Return an element for an IGroupMember holding an IEntity
    * @param gm IGroupMember
    * @param anElem Element
    * @param sessionData CGroupsManagerUnrestrictedSessionData
    * @return Element
    */
   public Element getXml (IGroupMember gm, Element anElem, CGroupsManagerUnrestrictedSessionData sessionData);
}



