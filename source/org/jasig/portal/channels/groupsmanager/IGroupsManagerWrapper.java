/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.groups.IGroupMember;
import org.w3c.dom.Element;

/**
 * Defines the interface for a wrapper object to be used by CGroupssManager
 * @author Don Fracapane
 * @version $Revision$
 */

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



