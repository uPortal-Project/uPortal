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

import java.util.Arrays;
import java.util.Collections;

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

  public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("PermissionSetImpl: entitityIdentifier=[" + this.entityIdentifier + "]");
	sb.append(" permissions: [" + Arrays.toString(permissions) + "]");
	return sb.toString();
  }

}
