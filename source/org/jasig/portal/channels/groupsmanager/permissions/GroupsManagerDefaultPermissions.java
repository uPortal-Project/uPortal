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
      boolean answer = false;
      try {
         answer = ap.hasPermission(OWNER, activity, gm.getKey());
      } catch (AuthorizationException ae) {
         Utility.logMessage("ERROR", "GroupsManagerDefaultPermission::isAuthorized(): Raised AuthorizationException exception",
               ae);
         answer = false;
      }
      return  answer;
   }
}
