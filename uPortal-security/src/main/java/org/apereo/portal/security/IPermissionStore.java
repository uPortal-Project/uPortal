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
package org.apereo.portal.security;

import org.apereo.portal.AuthorizationException;

/**
 * Interface for creating, finding and maintaining <code>IPermissions</code>.
 *
 */
public interface IPermissionStore {

    /**
     * Add the IPermissions to the store.
     *
     * @param perms org.apereo.portal.security.IPermission[]
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void add(IPermission[] perms) throws AuthorizationException;

    /**
     * Add the IPermission to the store.
     *
     * @param perm org.apereo.portal.security.IPermission
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void add(IPermission perm) throws AuthorizationException;

    /**
     * Remove the IPermissions from the store.
     *
     * @param perms org.apereo.portal.security.IPermission[]
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void delete(IPermission[] perms) throws AuthorizationException;

    /**
     * Remove the IPermission from the store.
     *
     * @param perm org.apereo.portal.security.IPermission
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void delete(IPermission perm) throws AuthorizationException;

    /** Factory method for IPermissions */
    public IPermission newInstance(String owner);

    /**
     * Update the IPermissions in the store.
     *
     * @param perms org.apereo.portal.security.IPermission[]
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void update(IPermission[] perms) throws AuthorizationException;

    /**
     * Update the IPermission in the store.
     *
     * @param perm org.apereo.portal.security.IPermission
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public void update(IPermission perm) throws AuthorizationException;

    /**
     * Select the IPermissions from the store.
     *
     * @param owner String - the Permission owner
     * @param principal String - the Permission principal
     * @param activity String - the Permission activity
     * @param target String - the Permission target
     * @param type String - the Permission type
     * @exception AuthorizationException - wraps an Exception specific to the store.
     */
    public IPermission[] select(
            String owner, String principal, String activity, String target, String type)
            throws AuthorizationException;
}
