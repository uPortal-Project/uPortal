package org.jasig.portal.security.provider;

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
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IUpdatingPermissionManager;

/**
 * @author Dan Ellentuck (de3@columbia.edu)
 * @version $Revision$
 */
public class UpdatingPermissionManagerImpl extends PermissionManagerImpl
implements IUpdatingPermissionManager
{

  /**
   * This constructor ensures that the PermissionManager will be created with an owner specified
   * @param owner
   */
  public UpdatingPermissionManagerImpl (String newOwner, AuthorizationImpl authService)
  {
    super(newOwner, authService);
  }
  /**
   * Add a new set of IPermission objects to the system.
   * @param newPermissions
   */
public void addPermissions (IPermission[] newPermissions) throws AuthorizationException
{
    getAuthorizationService().addPermissions(newPermissions);
}
  /**
   * Retrieve an array of <code>IPermission</code> objects based on the given parameters.
   * Includes inherited <code>IPermissions</code>.  Any null parameters will be ignored.
   *
   * @param principal IAuthorizationPrincipal
   * @param activity String
   * @param target String
   * @return IPermission[]
   * @exception AuthorizationException
   */
public IPermission[] getAllPermissions (IAuthorizationPrincipal principal, String activity, String target)
throws AuthorizationException
{
    return principal.getAllPermissions(getOwner(), activity, target);
}
  /**
   * Retrieve an array of IPermission objects based on the given parameters. Any null parameters
   * will be ignored.
   *
   * @param principal IAuthorizationPrincipal
   * @param activity String
   * @param target String
   * @return IPermission[]
   * @exception AuthorizationException
   */
public IPermission[] getPermissions (IAuthorizationPrincipal principal, String activity, String target)
throws AuthorizationException
{
    return getAuthorizationService().getUncachedPermissionsForPrincipal(principal, getOwner(), activity, target);
}
/**
 * Creates a new <code>IPermission</code> for the <code>principal</code> on behalf of the
 * owner of this <code>IPermissionManager</code>.
 *
 * @param principal IAuthorizationPrincipal
 */
public IPermission newPermission(IAuthorizationPrincipal principal) throws AuthorizationException
{
    return getAuthorizationService().newPermission(getOwner(), principal);
}
  /**
   * Remove set of IPermission objects from the system.
   * @param perms
   */
public void removePermissions (IPermission[] oldPermissions) throws AuthorizationException
{
    getAuthorizationService().removePermissions(oldPermissions);
}
  /**
   * Update a set of IPermission objects to the system.
   * @param perms
   */
public void updatePermissions (IPermission[] changedPermissions) throws AuthorizationException
{
    getAuthorizationService().updatePermissions(changedPermissions);
}
}
