/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionSet;

/**
 * @author Dan Ellentuck, de3@columbia.edu
 * @version $Revision$
 *
 * Aggregates <code>IPermissions</code> for a single 
 * <code>IAuthorizationPrincipal</code>  so that the permissions can be 
 * cached as a unit by the <code>EntityCachingService</code>.
 * <p>
 * Separating caching of principals and permissions lets a client keep 
 * a reference to a principal over time while being guaranteed the 
 * latest version of its permissions.   
 */

public class PermissionSetImpl implements IPermissionSet {

    private EntityIdentifier entityIdentifier;
    private IPermission[] permissions;
    private static Class IPS_TYPE = IPermissionSet.class; 

public PermissionSetImpl(IPermission[] perms, IAuthorizationPrincipal principal) 
{
    this(perms, principal.getPrincipalString(), IPS_TYPE);
}
public PermissionSetImpl(IPermission[] perms, String key, Class type) 
{
    super();
    permissions = perms;
    entityIdentifier = new EntityIdentifier(key, type);
}
/** 
 * @return IPermission[]
 */
public IPermission[] getPermissions() {
    return permissions;
}

/** 
 * @return EntityIdentifier IPermission[]
 */
public EntityIdentifier getEntityIdentifier() {
    return entityIdentifier;
}
}
