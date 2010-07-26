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

package org.jasig.portal.security;
 
 import org.jasig.portal.AuthorizationException;

 /**
 * An interface for updating <code>Permissions</code> on behalf of a 
 * <code>Permission</code> owner.
 *
 * @author Dan Ellentuck
 * @version $Revision$ 
 */
public interface IUpdatingPermissionManager extends IPermissionManager {
/**
 * Adds <code>IPermissions</code> to the store for the owner of this 
 * <code>IPermissionManager</code>.   
 *
 * @param permissions IPermission[] 
 */
    public void addPermissions (IPermission[] permissions) throws AuthorizationException;
/**
 * Creates a new <code>IPermission</code> for the <code>principal</code> on behalf of the 
 * owner of this <code>IPermissionManager</code>.   
 *
 * @param principal IAuthorizationPrincipal
 */
    public IPermission newPermission(IAuthorizationPrincipal principal) throws AuthorizationException;
/**
 * Removes <code>IPermissions</code> from the store for the owner of this 
 * <code>IPermissionManager</code>.   
 *
 * @param permissions IPermission[] 
 */
    public void removePermissions (IPermission[] permissions) throws AuthorizationException;
/**
 * Updates <code>IPermissions</code> in the store for the owner of this 
 * <code>IPermissionManager</code>.   
 *
 * @param permissions IPermission[] 
 */
    public void updatePermissions (IPermission[] permissions) throws AuthorizationException;
}
