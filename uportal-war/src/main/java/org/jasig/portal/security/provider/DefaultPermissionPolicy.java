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
        { throw new AuthorizationException(ge); }

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
