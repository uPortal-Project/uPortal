/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
