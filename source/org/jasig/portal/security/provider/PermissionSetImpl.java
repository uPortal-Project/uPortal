package org.jasig.portal.security.provider;
/**
 * Copyright (c) 2003 The JA-SIG Collaborative.  All rights reserved.
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
