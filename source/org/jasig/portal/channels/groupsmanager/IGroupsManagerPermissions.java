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