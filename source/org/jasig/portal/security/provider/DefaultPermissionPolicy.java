/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security.provider;

import java.util.Date;
import java.util.Iterator;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionPolicy;

/**
 * Implements a strategy for answering the basic authorization question: does the
 * <code>principal</code> have permission to perform the <code>activity</code> on
 * the <code>target</code>.
 *
 * @author Dan Ellentuck (de3@columbia.edu)
 * @version $Revision$
 */
public class DefaultPermissionPolicy implements IPermissionPolicy {
/**
 * DefaultPermissionPolicy constructor.
 */
public DefaultPermissionPolicy() {
    super();
}
/**
 * Answers if the owner has authorized the principal to perform the activity
 * on the target, based on permissions provided by the service.  Params
 * <code>service</code>, <code>owner</code> and <code>activity</code> must
 * be non-null.
 *
 * @return boolean
 * @param service org.jasig.portal.security.IAuthorizationService
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception org.jasig.portal.AuthorizationException
 */
public boolean doesPrincipalHavePermission
   (IAuthorizationService service,
    IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target)
throws org.jasig.portal.AuthorizationException
{
   IPermission[] perms = service.getPermissionsForPrincipal(principal, owner, activity, target);

    // We found a permission associated with this principal.
    if ( perms.length == 1 )
        { return permissionIsGranted(perms[0]); }

    // Should never be.
    if ( perms.length > 1 )
        { throw new AuthorizationException("Duplicate permissions for: " + perms[0]); }

    // No permissions for this principal.  Check inherited permissions.
    boolean hasPermission = false;
    try
    {
        Iterator i = service.getGroupMember(principal).getAllContainingGroups();
        while ( i.hasNext() && ! hasPermission )
        {
            IAuthorizationPrincipal prn = service.newPrincipal( (IGroupMember) i.next() );
            hasPermission = primDoesPrincipalHavePermission(prn, owner, activity, target, service);
         }
    }
    catch ( GroupsException ge )
        { throw new AuthorizationException(ge.getMessage(),ge); }

    return hasPermission;
}
/**
 * Checks that the permission is explicitly granted and not expired.
 * @return boolean
 * @param permission org.jasig.portal.security.IPermission
 */
private boolean permissionIsGranted(IPermission p)
{
    Date now = new Date();
    return
        (p.getType().equals(IPermission.PERMISSION_TYPE_GRANT)) &&
        (p.getEffective() == null || ! p.getEffective().after(now)) &&
        (p.getExpires() == null || p.getExpires().after(now));
}
/**
 * Answers if this specific principal (as opposed to its parents) has the permission.
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved or was invalid.
 */
private boolean primDoesPrincipalHavePermission(
    IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target,
    IAuthorizationService service)
throws AuthorizationException
{
    IPermission[] perms = service.getPermissionsForPrincipal(principal, owner, activity, target);

    if ( perms.length == 0 )
        { return false; }

    if ( perms.length == 1 )
        { return permissionIsGranted(perms[0]); }
    else
        { throw new AuthorizationException("Duplicate permissions for: " + perms[0]); }
}
}
