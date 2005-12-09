/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.permissions;

import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;


/**
 * GroupsManagerBlockEntitySelectPermissions answers if the Authorization Principal is able to
 * perform specific actions on the target Group Member.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerBlockEntitySelectPermissions extends GroupsManagerDefaultPermissions {

   /**
    * put your documentation comment here
    */
   public GroupsManagerBlockEntitySelectPermissions () {
   }

   protected static IGroupsManagerPermissions _instance = null;

   /**
    * Return the single instance of GroupsManagerBlockEntitySelectPermissions.
    * @return IGroupsManagerPermissions
    */
   public static synchronized IGroupsManagerPermissions getInstance(){
      if (_instance == null){
         _instance = new GroupsManagerBlockEntitySelectPermissions();
      }
      return _instance;
   }

    /**
    * Answers if principal can select the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canSelect (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canSelect() not yet implemented.");
      return  (gm.isGroup() && isAuthorized(ap, "SELECT", gm));
   }
}
