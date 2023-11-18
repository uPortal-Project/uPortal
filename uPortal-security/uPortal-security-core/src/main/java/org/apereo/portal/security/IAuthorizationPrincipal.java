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
import org.apereo.portal.portlet.om.PortletLifecycleState;

/**
 * An <code>IAuthorizationPrincipal</code> represents a portal entity to which <code>IPermissions
 * </code> have been granted. Such an entity could be an <code>IGroupMember</code>, an <code>
 * IChannel</code> or an <code>IPerson</code>.
 */
public interface IAuthorizationPrincipal {

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to manage this channel.
     *
     * @return boolean
     * @param channelPublishId int - the Channel Id
     * @exception AuthorizationException thrown when authorization information could not be
     *     retrieved.
     */
    boolean canManage(String channelPublishId) throws AuthorizationException;

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to publish (used only by
     * SLM).
     *
     * @return boolean
     * @exception AuthorizationException thrown when authorization information could not be
     *     retrieved.
     */
    boolean canManage(PortletLifecycleState state, String categoryId) throws AuthorizationException;

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to use the CONFIG
     * PortletMode on the specified channel
     *
     * @param channelPublishId
     * @return
     * @throws AuthorizationException
     */
    boolean canConfigure(String channelPublishId) throws AuthorizationException;

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to render this channel.
     *
     * @return boolean
     * @param channelPublishId int - the Channel publish Id
     * @exception AuthorizationException thrown when authorization information could not be
     *     retrieved.
     */
    boolean canRender(String channelPublishId) throws AuthorizationException;

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to subscribe to this
     * channel.
     *
     * @return boolean
     * @param channelPublishId int - the Channel Id
     * @exception AuthorizationException thrown when authorization information could not be
     *     retrieved.
     */
    boolean canSubscribe(String channelPublishId) throws AuthorizationException;

    /**
     * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>,
     * including inherited <code>Permissions</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    IPermission[] getAllPermissions() throws AuthorizationException;

    /**
     * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code> for the
     * specified <code>owner</code>, <code>activity</code> and <code>target</code>. This includes
     * inherited <code>IPermissions</code>. Null parameters are ignored, so <code>
     * getPermissions(null, null, null)</code> should retrieve all <code>IPermissions</code> for an
     * <code>IAuthorizationPrincipal</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    IPermission[] getAllPermissions(String owner, String activity, String target)
            throws AuthorizationException;

    /** Returns the key of the underlying entity. For users, the key will be the username. */
    String getKey();

    /**
     * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    IPermission[] getPermissions() throws AuthorizationException;

    /**
     * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code> for the
     * specified <code>owner</code>, <code>activity</code> and <code>target</code>. Null parameters
     * are ignored, so <code>getPermissions(null, null, null)</code> should retrieve all <code>
     * IPermissions</code> for an <code>IAuthorizationPrincipal</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    IPermission[] getPermissions(String owner, String activity, String target)
            throws AuthorizationException;

    /** @return java.lang.String */
    String getPrincipalString();

    /**
     * Return the Type of the underlying entity.
     *
     * @return java.lang.Class
     */
    Class getType();

    /**
     * Indicates whether the entity represented by this principal is a group (branch node) or user
     * (leaf node).
     *
     * @since 4.3
     */
    boolean isGroup();

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to perform the <code>
     * activity</code> on the <code>target</code>. Params <code>owner</code> and <code>activity
     * </code> must be non-null. If <code>target</code> is null, then the target is not checked.
     *
     * @return boolean
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    boolean hasPermission(String owner, String activity, String target)
            throws AuthorizationException;

    /**
     * Answers if this <code>IAuthorizationPrincipal</code> has permission to perform the <code>
     * activity</code> on the <code>target</code>, as evaluated by the <code>policy</code>. Params
     * <code>policy</code>, <code>owner</code> and <code>activity</code> must be non-null.
     *
     * @return boolean
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @param policy org.apereo.portal.security.IPermissionPolicy
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    boolean hasPermission(String owner, String activity, String target, IPermissionPolicy policy)
            throws AuthorizationException;
}
