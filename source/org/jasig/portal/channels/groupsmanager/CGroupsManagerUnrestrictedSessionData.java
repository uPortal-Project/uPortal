/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;


/**
 * Session data that is a subset of CGroupsManagerSessionData.
 * 
 * @author Don Fracapane
 * @version $Revision$
 */
public class CGroupsManagerUnrestrictedSessionData
      implements GroupsManagerConstants {
   public Document model;
   public IPerson user;
   public boolean isAdminUser;
   public IGroupsManagerPermissions gmPermissions;
   public IAuthorizationPrincipal authPrincipal;

   /** 
    * Creates new CGroupsManagerUnrestrictedSessionData
    */
   public CGroupsManagerUnrestrictedSessionData () {}

   /** 
    * Creates new CGroupsManagerUnrestrictedSessionData
    * @param model Document
    * @param user IPerson
    * @param isAdminUser boolean
    * @param gmPermissions IGroupsManagerPermissions
    * @param authPrincipal IAuthorizationPrincipal
    */
   public CGroupsManagerUnrestrictedSessionData (Document model, IPerson user, boolean isAdminUser,
         IGroupsManagerPermissions gmPermissions, IAuthorizationPrincipal authPrincipal) {
      this.model = model;
      this.user = user;
      this.isAdminUser = isAdminUser;
      this.gmPermissions = gmPermissions;
      this.authPrincipal = authPrincipal;
   }
}

