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

import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * GroupsManagerAdminPermissions answers if the Authorization Principal is able to
 * perform specific actions on the target Group Member. The answer for an admin
 * user is always true.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerAdminPermissions
      implements IGroupsManagerPermissions, GroupsManagerConstants {

   protected static IGroupsManagerPermissions _instance = null;

   /**
    * put your documentation comment here
    */
   public GroupsManagerAdminPermissions () {
   }

   /**
    * Return the single instance of GroupsManagerDefaultPermissions.
    * @return IGroupsManagerPermissions
    */
   public static IGroupsManagerPermissions getInstance()
   {
      if (_instance == null){
         _instance = new GroupsManagerAdminPermissions();
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
      return  true;
   }

   /**
    * Answers if principal can create a group in the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
   */
   public boolean canCreateGroup (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canCreateGroup() not yet implemented.");
      return  true;
   }

   /**
    * Answers if principal can manage the members in the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canManageMembers (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canManageMembers() not yet implemented.");
      return  true;
   }

   /**
    * Answers if principal can delete the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canDelete (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canDelete() not yet implemented.");
      return  true;
   }

   /**
    * Answers if principal can select the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canSelect (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canSelect() not yet implemented.");
      return  true;
   }

   /**
    * Answers if principal can update the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canUpdate (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canRename() not yet implemented.");
      return  true;
   }

   /**
    * Answers if principal can view the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
    public boolean canView (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canView() not yet implemented.");
      return  true;
   }

   /**
    * Answers if the principal is able to view the group member properties.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canViewProperties (IAuthorizationPrincipal ap, IGroupMember gm) {
      //throw new java.lang.UnsupportedOperationException("Method canView() not yet implemented.");
      return  true;
   }
}
