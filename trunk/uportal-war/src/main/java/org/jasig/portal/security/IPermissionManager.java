/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
