/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
