/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPermissionStore;

/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @author Dan Ellentuck (de3@columbia.edu)
 * @version $Revision$
 */
public class PermissionManagerImpl implements IPermissionManager
{
    private AuthorizationImpl authorizationService;
    protected static IPermissionStore permissionStore;
    private java.lang.String owner;
    
  /**
   * This constructor ensures that the PermissionManager will be created with an owner specified
   * @param newOwner the new owner
   * @param authService the authorization service
   */
  public PermissionManagerImpl (String newOwner, AuthorizationImpl authService)
  {
    super();
    owner = newOwner;
    authorizationService = authService;
  }
  
  /**
   * Retrieve an array of <code>IPermission</code> objects based on the given parameters.
   * Includes inherited <code>Permissions</code>.  Any null parameters will be ignored.
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
 * @return org.jasig.portal.security.provider.AuthorizationImpl
 */
AuthorizationImpl getAuthorizationService()
{
    return authorizationService;
}

/**
 * Returns <code>IAuthorizationPrincipals</code> granted <code>IPermissions</code>
 * by the owner of this <code>IPermissionManager</code>, for the given <code>activity</code>
 * and <code>target</code>.  If either parameter is null, it is ignored.
 *
 * @return IAuthorizationPrincipal[]
 * @param activity String - the Permission activity
 * @param target String - the Permission target
 */
public IAuthorizationPrincipal[] getAuthorizedPrincipals (String activity, String target)
throws AuthorizationException
{
    return getAuthorizationService().getAuthorizedPrincipals(getOwner(), activity, target);
}

/**
 * @return java.lang.String
 */
public java.lang.String getOwner() {
    return owner;
}

  /**
   * Retrieve an array of IPermission objects based on the given parameters. Any null parameters
   * will be ignored.
   *
   * @param activity String
   * @param target String
   * @return IPermission[]
   * @exception AuthorizationException
   */
public IPermission[] getPermissions (String activity, String target)
throws AuthorizationException
{
    return getAuthorizationService().getPermissionsForOwner(getOwner(), activity, target);
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
    return principal.getPermissions(getOwner(), activity, target);
}
}
