/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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
package org.jasig.portal.security;

import java.util.Vector;

import org.jasig.portal.AuthorizationException;

/**
 * An <code>IAuthorizationPrincipal</code> represents a portal entity to which
 * <code>IPermissions</code> have been granted.  Such an entity could be an <code>IGroupMember</code>,
 * an <code>IChannel</code> or an <code>IPerson</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IAuthorizationPrincipal {
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to publish.
 * @return boolean
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
    boolean canPublish() throws AuthorizationException;
/**
 * Answers if this <code>IAuthoriztionPrincipal</code> has permission to render this channel.
 * @return boolean
 * @param channelPublishId int - the Channel publish Id
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
    boolean canRender(int channelPublishId) throws AuthorizationException;
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to subscribe to this channel.
 * @return boolean
 * @param channelPublishId int - the Channel Id
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
    boolean canSubscribe(int channelPublishId) throws AuthorizationException;
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>,
 * including inherited <code>Permissions</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public IPermission[] getAllPermissions() throws AuthorizationException;
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code> for the
 * specified <code>owner</code>, <code>activity</code> and <code>target</code>.  This includes
 * inherited <code>IPermissions</code>.  Null parameters are ignored, so
 * <code>getPermissions(null, null, null)</code> should retrieve all <code>IPermissions</code>
 * for an <code>IAuthorizationPrincipal</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public IPermission[] getAllPermissions(String owner, String activity, String target)
    throws AuthorizationException;
/**
 * Return a Vector of (?) IChannels.
 * @return java.util.Vector
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
    public Vector getAuthorizedChannels() throws AuthorizationException;
/**
 * Returns the key of the underlying entity.
 * @return java.lang.String
 */
    public String getKey();
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public IPermission[] getPermissions() throws AuthorizationException;
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code> for the
 * specified <code>owner</code>, <code>activity</code> and <code>target</code>.  Null parameters
 * are ignored, so <code>getPermissions(null, null, null)</code> should retrieve all
 * <code>IPermissions</code> for an <code>IAuthorizationPrincipal</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public IPermission[] getPermissions(String owner, String activity, String target)
    throws AuthorizationException;
/**
 * @return java.lang.String
 */
    public String getPrincipalString();
/**
 * Return the Type of the underlying entity.
 * @return java.lang.Class
 */
    public Class getType();
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to perform the
 * <code>activity</code> on the <code>target</code>.  Params <code>owner</code> and
 * <code>activity</code> must be non-null.  If <code>target</code> is null, then the
 * target is not checked.
 *
 * @return boolean
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public boolean hasPermission(String owner, String activity, String target) throws
    AuthorizationException;

/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to perform the
 * <code>activity</code> on the <code>target</code>, as evaluated by the 
 * <code>policy</code>.  Params <code>policy</code>, <code>owner</code> and
 * <code>activity</code> must be non-null.  
 *
 * @return boolean
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @param policy org.jasig.portal.security.IPermissionPolicy
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
    public boolean hasPermission(String owner, String activity, String target, IPermissionPolicy policy) 
    throws AuthorizationException;
}
