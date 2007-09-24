/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.permissions;

import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * GroupsManagerAdminPermissions answers if the Authorization Principal is able to
 * perform specific actions on the target Group Member. The answer for an admin
 * user is always true.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerAdminPermissions extends GroupsManagerDefaultPermissions {

   /**
    * put your documentation comment here
    */
   public GroupsManagerAdminPermissions () {
   }

   protected static IGroupsManagerPermissions _instance = null;

   /**
    * Return the single instance of GroupsManagerAdminPermissions.
    * @return IGroupsManagerPermissions
    */
   public static synchronized IGroupsManagerPermissions getInstance(){
      if (_instance == null){
         _instance = new GroupsManagerAdminPermissions();
      }
      return _instance;
   }

   /**
    * Answers if principal can perform the activity on the target group member.
    * @param ap AuthorizationPrincipal
    * @param activity String
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean isAuthorized (IAuthorizationPrincipal ap, String activity, IGroupMember gm) {
      boolean answer = (Utility.hasValue(gm.getKey()) ? true : false);
      return  answer;
   }

}
