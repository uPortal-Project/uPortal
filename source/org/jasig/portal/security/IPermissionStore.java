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
 * Interface for creating, finding and maintaining <code>IPermissions</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

 public interface IPermissionStore {
	 
/**
 * Add the IPermissions to the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void add(IPermission[] perms) throws AuthorizationException;

/**
 * Add the IPermission to the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void add(IPermission perm) throws AuthorizationException;
    
/**
 * Remove the IPermissions from the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void delete(IPermission[] perms) throws AuthorizationException;
    
/**
 * Remove the IPermission from the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void delete(IPermission perm) throws AuthorizationException;
    
/**
 * Factory method for IPermissions
 */
    public IPermission newInstance(String owner);
    
/**
 * Update the IPermissions in the store.
 * @param perms org.jasig.portal.security.IPermission[]
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void update(IPermission[] perms) throws AuthorizationException;
    
/**
 * Update the IPermission in the store.
 * @param perm org.jasig.portal.security.IPermission
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public void update(IPermission perm) throws AuthorizationException;
    
/**
 * Select the IPermissions from the store.
 * @param owner String - the Permission owner
 * @param principal String - the Permission principal
 * @param activity String - the Permission activity 
 * @param target String - the Permission target
 * @param type String - the Permission type    
 * @exception org.jasig.portal.AuthorizationException - wraps an Exception specific to the store.
 */
    public IPermission[] select(String owner, String principal, String activity, String target, String type) 
    throws AuthorizationException;
}
