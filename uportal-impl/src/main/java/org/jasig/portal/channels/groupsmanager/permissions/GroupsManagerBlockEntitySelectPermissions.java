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

import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.channels.groupsmanager.IGroupsManagerPermissions;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;


/**
 * GroupsManagerBlockEntitySelectPermissions answers if the Authorization Principal is able to
 * perform specific actions on the target Group Member.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class GroupsManagerBlockEntitySelectPermissions 
        implements IGroupsManagerPermissions, GroupsManagerConstants {
    
    /**
     * put your documentation comment here
     */
    public GroupsManagerBlockEntitySelectPermissions () {
        new GroupsManagerBlockEntitySelectPermissions(GroupsManagerDefaultPermissions.getInstance());
    }

   /**
    * put your documentation comment here
    */
   public GroupsManagerBlockEntitySelectPermissions (IGroupsManagerPermissions pMgr) {
       permMgr = pMgr;
   }

   protected IGroupsManagerPermissions permMgr;
   
   public boolean canAssignPermissions (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canAssignPermissions(ap, gm);
   }

   public boolean canCreateGroup (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canCreateGroup(ap, gm);
   }

   public boolean canManageMembers (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canManageMembers(ap, gm);
   }

   public boolean canDelete (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canDelete(ap, gm);
   }

    /**
    * Answers if principal can select the target group member.
    * @param ap AuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canSelect (IAuthorizationPrincipal ap, IGroupMember gm) {
       //throw new java.lang.UnsupportedOperationException("Method canSelect() not yet implemented.");
       return  (gm.isGroup() && permMgr.canSelect(ap, gm));
    }

   public boolean canUpdate (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canUpdate(ap, gm);
   }

   public boolean canView (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canView(ap, gm);
   }

   public boolean canViewProperties (IAuthorizationPrincipal ap, IGroupMember gm) {
       return  permMgr.canViewProperties(ap, gm);
   }
}
