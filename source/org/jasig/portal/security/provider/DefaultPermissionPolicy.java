/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 * @param p org.jasig.portal.security.IPermission
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
