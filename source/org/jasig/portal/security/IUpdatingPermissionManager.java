package org.jasig.portal.security;

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
