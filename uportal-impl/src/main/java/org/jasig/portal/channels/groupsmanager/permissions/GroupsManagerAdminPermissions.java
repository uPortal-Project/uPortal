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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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
