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

package org.jasig.portal.security.provider;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionPolicy;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class AuthorizationPrincipalImpl implements IAuthorizationPrincipal {
    private java.lang.String key;
    private java.lang.Class type;
    private org.jasig.portal.security.IAuthorizationService authorizationService;
    private java.lang.String principalString;
/**
 * Constructor for ReferenceAuthorizationPrincipal.
 */
public AuthorizationPrincipalImpl(String newKey, Class newType) 
{
    this(newKey, newType, null);
}
/**
 * Constructor for ReferenceAuthorizationPrincipal.
 */
public AuthorizationPrincipalImpl(String newKey, Class newType, IAuthorizationService authService) 
{
    super();
    key = newKey;
    type = newType;
    authorizationService = authService;
    initialize();
}
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to manage this channel.
 * @return boolean
 * @param  channelPublishId String - the Channel publish ID
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
public boolean canManage(String channelPublishId) throws org.jasig.portal.AuthorizationException {
    return getAuthorizationService().canPrincipalManage(this, channelPublishId);
}
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to publish.
 * @return boolean
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
public boolean canManage(PortletLifecycleState state, String categoryId) throws AuthorizationException {
	return getAuthorizationService().canPrincipalManage(this, state, categoryId);
}
@Override
public boolean canConfigure(String channelPublishId) throws AuthorizationException {
    return getAuthorizationService().canPrincipalConfigure(this, channelPublishId);
}
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to render this channel.
 * @return boolean
 * @param channelPublishId int - the Channel publish ID
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
public boolean canRender(String channelPublishId) throws org.jasig.portal.AuthorizationException {
    return getAuthorizationService().canPrincipalRender(this, channelPublishId);
}
/**
 * Answers if this <code>IAuthorizationPrincipal</code> has permission to subscribe to this channel.
 * @return boolean
 * @param  channelPublishId int - the Channel publish ID
 * @exception AuthorizationException thrown when authorization information could not be retrieved.
 */
public boolean canSubscribe(String channelPublishId) throws org.jasig.portal.AuthorizationException {
    return getAuthorizationService().canPrincipalSubscribe(this, channelPublishId);
}
/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj) 
{
    if ( obj == null )
        return false;
    if ( obj == this )
        return true;
    if ( ! ( obj instanceof IAuthorizationPrincipal))
        return false;

    IAuthorizationPrincipal otherAP = (IAuthorizationPrincipal) obj;
    return this.getKey().equals(otherAP.getKey()) &&
           this.getType() == otherAP.getType();
}
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>, including
 * inherited <code>IPermissions</code>.  
 * 
 * @return org.jasig.portal.security.IPermission[]
 * @exception AuthorizationException indicates authorization information could not 
 * be retrieved.
 */
public IPermission[] getAllPermissions() throws AuthorizationException 
{
    return getAllPermissions(null, null, null);
}
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code> for the 
 * specified <code>owner</code>, <code>activity</code> and <code>target</code>.  Inherited
 * <code>IPermissions</code> are included.  Null parameters are ignored, so 
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
throws AuthorizationException 
{
    return getAuthorizationService().getAllPermissionsForPrincipal(this, owner, activity, target);
}
/**
 * @return org.jasig.portal.security.IAuthorization
 */
IAuthorizationService getAuthorizationService() 
{
    return authorizationService;
}
/**
 * Return a Vector of IChannels.
 * @return a <code>java.util.Vector</code> of IChannels
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
public java.util.Vector getAuthorizedChannels() throws org.jasig.portal.AuthorizationException {
    return getAuthorizationService().getAuthorizedChannels(this);
}
/**
 * @return java.lang.String
 */
public java.lang.String getKey() {
	return key;
}
/**
 * Returns the <code>IPermissions</code> for this <code>IAuthorizationPrincipal</code>.  
 * 
 * @return org.jasig.portal.security.IPermission[]
 * @exception AuthorizationException indicates authorization information could not 
 * be retrieved.
 */
public IPermission[] getPermissions() throws AuthorizationException 
{
    return getPermissions(null, null, null);
}
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
throws AuthorizationException 
{
    return getAuthorizationService().getPermissionsForPrincipal(this, owner, activity, target);
}
/**
 * @return java.lang.String
 */
public String getPrincipalString() 
{
    return principalString;
}
/**
 * @return java.lang.Class
 */
public java.lang.Class getType() {
    return type;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() 
{
    return getKey().hashCode() + getType().hashCode();
}
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
public boolean hasPermission(String owner, String activity, String target) 
throws org.jasig.portal.AuthorizationException 
{
    return getAuthorizationService().doesPrincipalHavePermission(this, owner, activity, target);
}
/**
 * Set the value of the principal string.
 */
private void initialize() {
    principalString = getAuthorizationService().getPrincipalString(this); 
}
/**
 * @param newAuthorizationService org.jasig.portal.security.IAuthorizationService
 */
void setAuthorizationService(IAuthorizationService newAuthorizationService) {
    authorizationService = newAuthorizationService;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() 
{
    return getPrincipalString();
}

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
throws AuthorizationException
{
    return getAuthorizationService().doesPrincipalHavePermission(this, owner, activity, target, policy);
}
}
