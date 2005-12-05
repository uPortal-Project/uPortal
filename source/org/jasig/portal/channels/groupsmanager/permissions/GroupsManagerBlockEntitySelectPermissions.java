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
 * GroupsManagerBlockEntitySelectPermissions wraps a default permissions policy or an admin
 * policy and adds the function of blocking entity selection. 
 * @author Don Fracapane
 * @version $Revision$
 */
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
