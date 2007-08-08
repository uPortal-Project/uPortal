/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.permissions;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;


/**
 * GroupsManagerDefaultPermissions answers if the Authorization Principal is able to
 * perform specific actions on the target Group Member.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerDefaultPermissions
      implements IGroupsManagerPermissions, GroupsManagerConstants {

   protected static IGroupsManagerPermissions _instance = null;

   /**
    * put your documentation comment here
    */
   public GroupsManagerDefaultPermissions () {
   }

   /**
    * Return the single instance of GroupsManagerDefaultPermissions.
    * @return IGroupsManagerPermissions
    */
   public static synchronized IGroupsManagerPermissions getInstance(){
      if (_instance == null){
         _instance = new GroupsManagerDefaultPermissions();
      }
      return _instance;
   }

   /**
    * Answers if principal can assign permissions to the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canAssignPermissions (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canAssignPermissions() not yet implemented.");
      return  isAuthorized(ap, "ASSIGNPERMISSIONS", gm);
   }

   /**
    * Answers if principal can create a group in the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
   */
   public boolean canCreateGroup (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canCreateGroup() not yet implemented.");
      return  isAuthorized(ap, "CREATE", gm);
   }

   /**
    * Answers if principal can manage the members in the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canManageMembers (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canManageMembers() not yet implemented.");
      return  isAuthorized(ap, "ADD/REMOVE", gm);
   }

   /**
    * Answers if principal can delete the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canDelete (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canDelete() not yet implemented.");
      return  isAuthorized(ap, "DELETE", gm);
   }

   /**
    * Answers if principal can update the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canUpdate (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canRename() not yet implemented.");
      return  isAuthorized(ap, "UPDATE", gm);
   }

   /**
    * Answers if principal can select the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canSelect (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canSelect() not yet implemented.");
      return  (gm.isEntity() || isAuthorized(ap, "SELECT", gm));
   }

   /**
    * Answers if principal can view the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canView (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canView() not yet implemented.");
      return  isAuthorized(ap, "VIEW", gm);
   }

   /**
    * Answers if the principal is able to view the group member properties. This is
    * a new permission. No one should have this permission in the permission store.
    * So initially, you may want to enforce your own policy for this permission in
    * order to keep the behavior the same as before this update was applied. For
    * example, you could always return a true or perhaps base this permission on
    * another permission, such as canSelect.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canViewProperties (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canView() not yet implemented.");
      //return canSelect (ap, gm);
      //return true;
      return  isAuthorized(ap, "VIEWPROPERTIES", gm);
   }

   /**
    * Answers if principal can perform the activity on the target group member.
    * @param ap AuthorizationPrincipal
    * @param activity String
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean isAuthorized (IAuthorizationPrincipal ap, String activity, IGroupMember gm) {
      /* If the gm key is null, we cannot meaningfully determine authorizations. The transient
      search element has a null key so all permissions should be denied.
      */
      boolean answer = false;
      if (gm.getKey() != null){
         try {
            answer = ap.hasPermission(OWNER, activity, gm.getKey());
         } catch (AuthorizationException ae) {
            Utility.logMessage("ERROR", "GroupsManagerDefaultPermission::isAuthorized(): Raised AuthorizationException exception",
                  ae);
            answer = false;
         }
      }
      return  answer;
   }
}
