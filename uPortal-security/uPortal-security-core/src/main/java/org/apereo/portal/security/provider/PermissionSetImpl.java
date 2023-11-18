/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.provider;

import java.util.Arrays;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionSet;

/**
 * Aggregates <code>IPermissions</code> for a single <code>IAuthorizationPrincipal</code> so that
 * the permissions can be cached as a unit by the <code>EntityCachingService</code>.
 *
 * <p>Separating caching of principals and permissions lets a client keep a reference to a principal
 * over time while being guaranteed the latest version of its permissions.
 */
public class PermissionSetImpl implements IPermissionSet {

    private EntityIdentifier entityIdentifier;
    private IPermission[] permissions;
    private static Class IPS_TYPE = IPermissionSet.class;

    public PermissionSetImpl(IPermission[] perms, IAuthorizationPrincipal principal) {
        this(perms, principal.getPrincipalString(), IPS_TYPE);
    }

    public PermissionSetImpl(IPermission[] perms, String key, Class type) {
        super();
        permissions = perms;
        entityIdentifier = new EntityIdentifier(key, type);
    }
    /** @return IPermission[] */
    @Override
    public IPermission[] getPermissions() {
        return permissions;
    }

    /** @return EntityIdentifier IPermission[] */
    @Override
    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("PermissionSetImpl: entityIdentifier=[" + this.entityIdentifier + "]");
        sb.append(" permissions: [" + Arrays.toString(permissions) + "]");
        return sb.toString();
    }
}
