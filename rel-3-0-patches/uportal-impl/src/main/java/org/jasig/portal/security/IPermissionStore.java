/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

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
