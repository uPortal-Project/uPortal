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
 * An interface for retrieving <code>IAuthorizationPrincipals</code> and
 * their <code>Permissions</code> on behalf of a <code>Permission</code> owner.
 *
 * @author Dan Ellentuck
 * @version $Revision$ 
 */
public interface IPermissionManager {
/**
 * Returns <code>IPermissions</code> granted to the <code>IAuthorizationPrincipal</code> 
 * by the owner of this <code>IPermissionManager</code>, for the given <code>activity</code> 
 * and <code>target</code>.  This includes inherited <code>IPermissions</code>.  If any 
 * parameter is null, it is ignored.  
 *
 * @return IPermission[]
 * @param principal IAuthorizationPrincipal
 * @param activity String - the Permission activity
 * @param target String - the Permission target
 */
    public IPermission[] getAllPermissions(IAuthorizationPrincipal principal, String activity, String target) 
    throws AuthorizationException;
/**
 * Returns <code>IAuthorizationPrincipals</code> granted <code>Permissions</code>
 * by the owner of this <code>IPermissionManager</code>, for the given <code>activity</code> 
 * and <code>target</code>.  If either parameter is null, it is ignored.  
 *
 * @return IAuthorizationPrincipal[]
 * @param activity String - the Permission activity
 * @param target String - the Permission target
 */
    public IAuthorizationPrincipal[] getAuthorizedPrincipals (String activity, String target) 
    throws AuthorizationException;
/**
 * @return java.lang.String
 */
    public String getOwner();
  /**
   * Retrieve an array of IPermission objects based on the given parameters. Any null parameters
   * will be ignored.
   *
   * @param activity String
   * @param target String
   * @return Permission[]
   * @exception AuthorizationException
   */
    public IPermission[] getPermissions (String activity, String target) throws AuthorizationException;
/**
 * Returns <code>IPermissions</code> granted to the <code>IAuthorizationPrincipal</code> 
 * by the owner of this <code>IPermissionManager</code>, for the given <code>activity</code> 
 * and <code>target</code>.  If any parameter is null, it is ignored.  
 *
 * @return IPermission[]
 * @param principal IAuthorizationPrincipal
 * @param activity String - the Permission activity
 * @param target String - the Permission target
 */
    public IPermission[] getPermissions(IAuthorizationPrincipal principal, String activity, String target) 
    throws AuthorizationException;
}
