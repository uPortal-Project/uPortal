/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;
 
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
