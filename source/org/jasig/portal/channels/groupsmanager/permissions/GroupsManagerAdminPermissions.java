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

import org.jasig.portal.channels.groupsmanager.Utility;
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
