/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.groupsmanager;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * IGroupsManagerPermissions allows servants to be created using pluggable
 * permissions policies to reflect the needs of the master channel.
 * @author Don Fracapane
 * @version $Revision$
 */

public interface IGroupsManagerPermissions {

   /**
    * Answers if the principal is able to assign permissions on the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canAssignPermissions (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to create a subgroup under the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canCreateGroup (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to manage the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canManageMembers (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to delete the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canDelete (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to select the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canSelect (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to update the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canUpdate (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to view the group member.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canView (IAuthorizationPrincipal ap, IGroupMember gm);

   /**
    * Answers if the principal is able to view the group member properties.
    * @param ap IAuthorizationPrincipal
    * @param gm IGroupMember
    * @return boolean
    */
   public boolean canViewProperties (IAuthorizationPrincipal ap, IGroupMember gm);
}