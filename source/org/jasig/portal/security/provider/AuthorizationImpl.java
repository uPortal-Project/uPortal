/**
 * Copyright ©  2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPermissionPolicy;
import org.jasig.portal.security.IPermissionSet;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class AuthorizationImpl implements IAuthorizationService {

    protected IPermissionStore permissionStore;
    protected IPermissionPolicy defaultPermissionPolicy;
    protected Map principalCache = new HashMap(100);
    protected String PERIOD_STRING = ".";
    protected Class PERMISSION_SET_TYPE;
    protected boolean cachePermissions;
    protected static IAuthorizationService singleton;
  /**
   *
   */
private AuthorizationImpl () throws AuthorizationException
{
    super();
    initialize();
}
/**
 * Adds <code>IPermissions</code> to the back end store.
 * @param permissions IPermission[]
 * @exception AuthorizationException
 */
public void addPermissions(IPermission[] permissions)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        getPermissionStore().add(permissions);
        if ( cachePermissions )
            { removeFromPermissionsCache(permissions); } 
    }
}
/**
 * Adds <code>IPermissions</code> for the <code>IAuthorizationPrincipal</code> to the store
 * and clears out the cache.
 * @param permissions IPermission[]
 * @param principal IAuthorizationPrincipal
 * @exception AuthorizationException
 * @deprecated as of uPortal 2.2.  A convenience method that was never used.
 */
public void addPermissions(IPermission[] permissions, IAuthorizationPrincipal principal)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        removeFromPermissionsCache(new IAuthorizationPrincipal[] {principal});
        getPermissionStore().add(permissions);
    }
}

/**
* Adds the <code>IPermissionSet</code> to the entity cache.
*/
protected void cacheAdd(IPermissionSet ps) throws AuthorizationException
{
    try
        { EntityCachingService.instance().add(ps); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem adding permissions for " + ps + " to cache: " + ce.getMessage() ); }
}

/**
* Retrieves the <code>IPermissionSet</code> for the <code>IPermissionSet</code>
* from the entity cache.
*/
protected IPermissionSet cacheGet(IAuthorizationPrincipal principal) 
throws AuthorizationException
{
    try
    { 
        return (IPermissionSet)
          EntityCachingService.instance().get(PERMISSION_SET_TYPE, principal.getPrincipalString()); 
    }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem adding permissions for " + principal + " to cache: " + ce.getMessage() ); }
}

/**
* Removes the <code>IPermissionSet</code> for this principal from the 
* entity cache.
*/
protected void cacheRemove(IAuthorizationPrincipal ap) throws AuthorizationException
{
    try
        { EntityCachingService.instance().remove(PERMISSION_SET_TYPE, ap.getPrincipalString()); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem removing permissions for " + ap + " from cache: " + ce.getMessage() ); }
}

/**
* Updates the <code>IPermissionSet</code> in the entity cache.
*/
protected void cacheUpdate(IPermissionSet ps) throws AuthorizationException
{
    try
        { EntityCachingService.instance().update(ps); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem updating permissions for " + ps + " in cache: " + ce.getMessage() ); }
}

/**
 * This checks if the framework has granted principal a right to publish.  DO WE WANT SOMETHING THIS COARSE (de)?
 * @param principal IAuthorizationPrincipal
 * @return boolean
 */
public boolean canPrincipalPublish (IAuthorizationPrincipal principal) throws AuthorizationException
{
    return doesPrincipalHavePermission
      (principal, IPermission.PORTAL_FRAMEWORK, IPermission.CHANNEL_PUBLISHER_ACTIVITY, null);
}

/**
 * Answers if the principal has permission to RENDER this Channel.
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param channelPublishId int
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
public boolean canPrincipalRender(IAuthorizationPrincipal principal, int channelPublishId)
throws AuthorizationException
{
    return canPrincipalSubscribe(principal, channelPublishId);
}

/**
 * Answers if the principal has permission to SUBSCRIBE to this Channel.
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param channelPublishId int
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
public boolean canPrincipalSubscribe(IAuthorizationPrincipal principal, int channelPublishId)
throws AuthorizationException
{
    String owner = IPermission.PORTAL_FRAMEWORK;
    String target = IPermission.CHANNEL_PREFIX + channelPublishId;
    return doesPrincipalHavePermission
      (principal, owner, IPermission.CHANNEL_SUBSCRIBER_ACTIVITY, target);
}

/**
 * Answers if the owner has given the principal (or any of its parents) permission
 * to perform the activity on the target.  Params <code>owner</code> and
 * <code>activity</code> must be non-null.  If <code>target</code> is null, then
 * target is not checked.
 *
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public boolean doesPrincipalHavePermission(
    IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target)
throws AuthorizationException
{
     return doesPrincipalHavePermission(principal, owner, activity, target, getDefaultPermissionPolicy());
}

/**
 * Answers if the owner has given the principal permission to perform the activity on
 * the target, as evaluated by the policy.  Params <code>policy</code>, <code>owner</code>
 * and <code>activity</code> must be non-null.
 *
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public boolean doesPrincipalHavePermission(
    IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target,
    IPermissionPolicy policy)
throws AuthorizationException
{
    return policy.doesPrincipalHavePermission(this, principal, owner, activity, target);
}

/**
 * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for
 * the specified activity and target.  Null parameters will be ignored, that is, all
 * <code>IPermissions</code> matching the non-null parameters are retrieved.  So,
 * <code>getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions</code>
 * for a <code>Principal</code>.  Note that this includes <code>IPermissions</code> inherited
 * from groups the <code>Principal</code> belongs to.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public IPermission[] getAllPermissionsForPrincipal
    (IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target)
throws AuthorizationException
{
    IPermission[] perms = getPermissionsForPrincipal(principal, owner, activity, target);
    ArrayList al = new ArrayList(Arrays.asList(perms));
    Iterator i = getInheritedPrincipals(principal);
    while ( i.hasNext() )
    {
        IAuthorizationPrincipal p = (IAuthorizationPrincipal) i.next();
        perms = getPermissionsForPrincipal(p, owner, activity, target);
        al.addAll(Arrays.asList(perms));
    }
    return ((IPermission[])al.toArray(new IPermission[al.size()]));
}

/**
 * Does this mean all channels the principal could conceivably subscribe
 * to or all channels principal is specifically authorized to subscribe to,
 * or what?
 * 
 * @param principal IAuthorizationPrincipal
 * @return Vector (of channels?)
 * @exception AuthorizationException indicates authorization information could not
 */
public Vector getAuthorizedChannels(IAuthorizationPrincipal principal)
throws AuthorizationException
{
    return new Vector();
}

/**
 * Returns <code>IAuthorizationPrincipals</code> that have <code>IPermissions</code> for
 * the given owner, activity and target.
 *
 * @return IAuthorizationPrincipal[]
 * @param owner
 * @param activity
 * @param target
 */
public IAuthorizationPrincipal[] getAuthorizedPrincipals(String owner, String activity, String target)
throws AuthorizationException
{
    IPermission[] permissions = getPermissionsForOwner(owner, activity, target);
    return getPrincipalsFromPermissions(permissions);
}

/**
 * @return org.jasig.portal.security.IPermissionPolicy
 */
protected IPermissionPolicy getDefaultPermissionPolicy() {
    return defaultPermissionPolicy;
}

/**
 * @return org.jasig.portal.groups.IGroupMember
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 */
public IGroupMember getGroupMember(IAuthorizationPrincipal principal)
throws GroupsException
{
    return getGroupMemberForPrincipal(principal);
}

/**
 * @return org.jasig.portal.groups.IGroupMember
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 */
private IGroupMember getGroupMemberForPrincipal(IAuthorizationPrincipal principal)
throws GroupsException
{
    LogService.log (LogService.DEBUG,
       "AuthorizationImpl.getGroupMemberForPrincipal(): for principal " +
       principal.toString());

    IGroupMember gm = GroupService.getGroupMember(principal.getKey(), principal.getType());

    LogService.log (LogService.DEBUG,
       "AuthorizationImpl.getGroupMemberForPrincipal(): got group member " + gm);

    return gm;
}

/**
 * Hook into the Groups system by converting the <code>IAuthorizationPrincipal</code> to
 * an <code>IGroupMember</code>.  Returns ALL the groups the <code>IGroupMember</code>
 * (recursively) belongs to.
 * @param principal - org.jasig.portal.security.IAuthorizationPrincipal
 * @return java.util.Iterator over Collection of IEntityGroups
 */
private Iterator getGroupsForPrincipal(IAuthorizationPrincipal principal)
throws GroupsException
{
    IGroupMember gm = getGroupMemberForPrincipal(principal);
    return gm.getAllContainingGroups();
}

/**
 * Hook into the Groups system, find all containing groups, and convert the
 * them to <code>IAuthorizationPrincipals</code>.
 * @param principal - org.jasig.portal.security.IAuthorizationPrincipal
 * @return java.util.Iterator over Collection of IEntityGroups
 */
private Iterator getInheritedPrincipals(IAuthorizationPrincipal principal)
throws AuthorizationException
{
    Iterator i = null;
    ArrayList al = new ArrayList(5);

    try
        { i = getGroupsForPrincipal(principal); }
    catch ( GroupsException ge )
        { throw new AuthorizationException("Could not retrieve Groups for " + principal,ge) ; }

    while ( i.hasNext() )
    {
        IEntityGroup group = (IEntityGroup) i.next();
        IAuthorizationPrincipal p = getPrincipalForGroup(group);
        al.add(p);
    }
    return al.iterator();
}

/**
 * Returns the <code>IPermissions</code> owner has granted for the specified activity
 * and target.  Null parameters will be ignored, that is, all <code>IPermissions</code>
 * matching the non-null parameters are retrieved.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public IPermission[] getPermissionsForOwner(String owner, String activity, String target)
throws AuthorizationException
{
    return primRetrievePermissions(owner, null, activity, target);
}

/**
 * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for
 * the specified activity and target.  Null parameters will be ignored, that is, all
 * <code>IPermissions</code> matching the non-null parameters are retrieved.  So,
 * <code>getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions</code>
 * for a <code>Principal</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public IPermission[] getPermissionsForPrincipal
    (IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target)
throws AuthorizationException
{
    return primGetPermissionsForPrincipal(principal, owner, activity, target);
}

/**
 * @return org.jasig.portal.security.IPermissionStore
 */
private IPermissionStore getPermissionStore()
{
    return permissionStore;
}

/**
 * Returns <code>IAuthorizationPrincipal</code> associated with the <code>IPermission</code>.
 *
 * @return IAuthorizationPrincipal
 * @param permission IPermission
 */
public IAuthorizationPrincipal getPrincipal(IPermission permission)
throws AuthorizationException
{
    String principalString = permission.getPrincipal();
    int idx = principalString.indexOf(PERIOD_STRING);
    Integer typeId = new Integer(principalString.substring(0, idx));
    Class type = EntityTypes.getEntityType(typeId);
    String key = principalString.substring(idx + 1);
    return newPrincipal(key, type);
}

/**
 * @param group
 * @return user org.jasig.portal.security.IAuthorizationPrincipal
 */
private IAuthorizationPrincipal getPrincipalForGroup(IEntityGroup group)
{
    String key = group.getKey();
    Class type = EntityTypes.GROUP_ENTITY_TYPE;
    return newPrincipal(key, type);
}

/**
 * Returns <code>IAuthorizationPrincipals</code> associated with the <code>IPermission[]</code>.
 *
 * @return IAuthorizationPrincipal[]
 * @param permissions IPermission[]
 */
private IAuthorizationPrincipal[] getPrincipalsFromPermissions(IPermission[] permissions)
throws AuthorizationException
{
    Set principals = new HashSet();
    for ( int i=0; i<permissions.length; i++ )
    {
        IAuthorizationPrincipal principal = getPrincipal(permissions[i]);
        principals.add(principal);
    }
    return ((IAuthorizationPrincipal[])principals.toArray(new IAuthorizationPrincipal[principals.size()]));
}

/**
 * Returns the String used by an <code>IPermission</code> to represent an
 * <code>IAuthorizationPrincipal</code>.
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 */
public String getPrincipalString(IAuthorizationPrincipal principal)
{
    return getPrincipalString(principal.getType(), principal.getKey());
}
private String getPrincipalString(Class pType, String pKey) {
    Integer type = EntityTypes.getEntityTypeID(pType);
    return type + PERIOD_STRING + pKey;
}

/**
 * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for
 * the specified activity and target.  Null parameters will be ignored, that is, all
 * <code>IPermissions</code> matching the non-null parameters are retrieved.  So,
 * <code>getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions</code>
 * for a <code>Principal</code>.  Ignore any cached <code>IPermissions</code>.
 *
 * @return org.jasig.portal.security.IPermission[]
 * @param principal IAuthorizationPrincipal
 * @param owner java.lang.String
 * @param activity java.lang.String
 * @param target java.lang.String
 * @exception AuthorizationException indicates authorization information could not
 * be retrieved.
 */
public IPermission[] getUncachedPermissionsForPrincipal
        (IAuthorizationPrincipal principal,
        String owner,
        String activity,
        String target)
throws AuthorizationException
{
    String pString = getPrincipalString(principal);
    return primRetrievePermissions(owner, pString, activity, target);
}

/**
 *
 */
private void initialize() throws AuthorizationException
{
    String eMsg = null;
    String factoryName =
      PropertiesManager.getProperty("org.jasig.portal.security.IPermissionStore.implementation");
    String policyName =
      PropertiesManager.getProperty("org.jasig.portal.security.IPermissionPolicy.defaultImplementation");
    cachePermissions =
      PropertiesManager.getPropertyAsBoolean("org.jasig.portal.security.IAuthorizationService.cachePermissions");


    if ( factoryName == null )
    {
        eMsg = "AuthorizationImpl.initialize(): No entry for org.jasig.portal.security.IPermissionStore.implementation portal.properties.";
        LogService.log(LogService.ERROR, eMsg);
        throw new AuthorizationException(eMsg);
    }

    if ( policyName == null )
    {
        eMsg = "AuthorizationImpl.initialize(): No entry for org.jasig.portal.security.IPermissionPolicy.defaultImplementation portal.properties.";
        LogService.log(LogService.ERROR, eMsg);
        throw new AuthorizationException(eMsg);
    }

    try
    {
        permissionStore = (IPermissionStore)Class.forName(factoryName).newInstance();
    }
    catch (Exception e)
    {
        eMsg = "AuthorizationImpl.initialize(): Problem creating permission store... " + e.getMessage();
        LogService.log(LogService.ERROR, eMsg);
        throw new AuthorizationException(eMsg);
    }

    try
    {
        defaultPermissionPolicy = (IPermissionPolicy)Class.forName(policyName).newInstance();
    }
    catch (Exception e)
    {
        eMsg = "AuthorizationImpl.initialize(): Problem creating default permission policy... " + e.getMessage();
        LogService.log(LogService.ERROR, eMsg);
        throw new AuthorizationException(eMsg);
    }

    try 
    { 
        PERMISSION_SET_TYPE = Class.forName("org.jasig.portal.security.IPermissionSet");
    }
    catch (ClassNotFoundException cnfe)
    {
        eMsg = "AuthorizationImpl.initialize(): Problem initializing service. " + cnfe.getMessage();
        LogService.log(LogService.ERROR, eMsg);
        throw new AuthorizationException(eMsg);
    }
}

/**
 * Factory method for an <code>IPermission</code>.
 * @param owner String
 * @return org.jasig.portal.security.Permission
 */
public IPermission newPermission(String owner)
{
    return newPermission(owner, null);
}

/**
 * Factory method for an <code>IPermission</code>.
 * @param owner String
 * @param principal IAuthorizationPrincipal
 * @return org.jasig.portal.security.IPermission
 */
public IPermission newPermission(String owner, IAuthorizationPrincipal principal)
{
    IPermission p = getPermissionStore().newInstance(owner);
    if ( principal != null )
    {
        String pString = getPrincipalString(principal);
        p.setPrincipal(pString);
    }
    return p;
}

/**
 * Factory method for IPermissionManager.
 * @return org.jasig.portal.security.IPermissionManager
 * @param owner java.lang.String
 */
public IPermissionManager newPermissionManager(String owner)
{
    return new PermissionManagerImpl(owner, this);
}

/**
 * Factory method for IAuthorizationPrincipal.
 * First check the principal cache, and if not present, create the principal 
 * and cache it.  
 * @return org.jasig.portal.security.IAuthorizationPrincipal
 * @param key java.lang.String
 * @param type java.lang.Class
 */
public IAuthorizationPrincipal newPrincipal(String key, Class type)
{
    String principalKey = getPrincipalString(type, key);
    IAuthorizationPrincipal principal = (IAuthorizationPrincipal)getPrincipalCache().get(principalKey);
    if ( principal == null )
    {
        synchronized (this)
        {
            principal = (IAuthorizationPrincipal)getPrincipalCache().get(key);
            if ( principal == null )
            {
                principal = primNewPrincipal(key, type);
                Map cache = copyPrincipalCache();
                cache.put(principalKey, principal);
                setPrincipalCache(cache);
            }
        }  // end synchronized
    }
    return principal;
}

/**
 * Converts an <code>IGroupMember</code> into an <code>IAuthorizationPrincipal</code>.
 * @return org.jasig.portal.security.IAuthorizationPrincipal
 * @param groupMember org.jasig.portal.groups.IGroupMember
 */
public IAuthorizationPrincipal newPrincipal(IGroupMember groupMember)
throws GroupsException
{
    String key = groupMember.getKey();
    Class type = groupMember.getType();

    LogService.log (LogService.DEBUG,
       "AuthorizationImpl.newPrincipal(): for " + type + "(" + key + ")");

    return newPrincipal(key, type);
}

private IAuthorizationPrincipal primNewPrincipal(String key, Class type) {
    return new AuthorizationPrincipalImpl(key, type, this);
}

/**
 * Factory method for IUpdatingPermissionManager.
 * @return org.jasig.portal.security.IUpdatingPermissionManager
 * @param owner java.lang.String
 */
public IUpdatingPermissionManager newUpdatingPermissionManager(String owner)
{
    return new UpdatingPermissionManagerImpl(owner, this);
}

/**
 * Returns permissions for a principal.  First check the entity caching
 * service, and if the permissions have not been cached, retrieve and
 * cache them.  
 * @return IPermission[]
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 */
private IPermission[] primGetPermissionsForPrincipal(IAuthorizationPrincipal principal)
throws AuthorizationException
{
    if ( ! cachePermissions )
        { return getUncachedPermissionsForPrincipal(principal, null, null, null);}
        
    IPermissionSet ps = null;
    // Check the caching service for the Permissions first.
    ps = cacheGet(principal);
    
    if ( ps == null )
    synchronized ( principal )
    {
        ps = cacheGet(principal);
        if ( ps == null )
        {
            IPermission[] permissions = 
              getUncachedPermissionsForPrincipal(principal, null, null, null);
            ps = new PermissionSetImpl(permissions, principal);
            cacheAdd(ps);
        }
    }      // end synchronized
    return ps.getPermissions();
}

/**
 * @return IPermission[]
 * @param principal org.jasig.portal.security.IAuthorizationPrincipal
 * @param owner String
 * @param activity String
 * @param target String
 */
private IPermission[] primGetPermissionsForPrincipal
    (IAuthorizationPrincipal principal,
    String owner,
    String activity,
    String target)
throws AuthorizationException
{
        LogService.log (LogService.DEBUG,
          "AuthorizationImpl.primGetPermissionsForPrincipal(): " + 
          "Principal: " + principal + " owner: " + owner +
          " activity: " + activity + " target: " + target);

    
    IPermission[] perms = primGetPermissionsForPrincipal(principal);
    if ( owner == null && activity == null && target == null )
        { return perms; }
    ArrayList al = new ArrayList(perms.length);
    for ( int i=0; i<perms.length; i++ )
    {
        if (
            (owner == null || owner.equals(perms[i].getOwner())) &&
            (activity == null || activity.equals(perms[i].getActivity())) &&
            (target == null || target.equals(perms[i].getTarget()))
           )
            { al.add(perms[i]); }
    }
    
    LogService.log (LogService.DEBUG,
      "AuthorizationImpl.primGetPermissionsForPrincipal(): " + 
      "# permissions retrieved: " + al.size());


    return ((IPermission[])al.toArray(new IPermission[al.size()]));

}

/**
 * @return IPermission[]
 * @param owner String
 * @param principal String
 * @param activity String
 * @param target String
 */
private IPermission[] primRetrievePermissions(String owner, String principal, String activity, String target)
throws AuthorizationException
{
    return getPermissionStore().select(owner, principal, activity, target, null);
}

/**
 * Removes <code>IPermissions</code> for the <code>IAuthorizationPrincipals</code> from
 * the cache.
 * @param principals IAuthorizationPrincipal[]
 */
private void removeFromPermissionsCache(IAuthorizationPrincipal[] principals) 
throws AuthorizationException
{
    for ( int i=0; i<principals.length; i++ )
        { cacheRemove(principals[i]); }
}

/**
 * Removes <code>IPermissions</code> from the cache.
 * @param permissions IPermission[]
 */
private void removeFromPermissionsCache(IPermission[] permissions) 
throws AuthorizationException
{
    IAuthorizationPrincipal[] principals = getPrincipalsFromPermissions(permissions);
    removeFromPermissionsCache(principals);
}

/**
 * Removes <code>IPermissions</code> from the back end store.
 * @param permissions IPermission[]
 * @exception AuthorizationException
 */
public void removePermissions(IPermission[] permissions)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        getPermissionStore().delete(permissions);
        if ( cachePermissions )
            { removeFromPermissionsCache(permissions); }  
    }
}

/**
 * Removes <code>IPermissions</code> for the <code>IAuthorizationPrincipal</code> in the store
 * and clears out the cache.
 * @param permissions IPermission[]
 * @param principal IAuthorizationPrincipal
 * @exception AuthorizationException
 * @deprecated as of uPortal 2.2.  A convenience method that was never used.
 */
public void removePermissions(IPermission[] permissions, IAuthorizationPrincipal principal)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        removeFromPermissionsCache(new IAuthorizationPrincipal[] {principal});
        getPermissionStore().delete(permissions);
    }
}

/**
 * @param newDefaultPermissionPolicy org.jasig.portal.security.IPermissionPolicy
 */
protected void setDefaultPermissionPolicy(IPermissionPolicy newDefaultPermissionPolicy) {
    defaultPermissionPolicy = newDefaultPermissionPolicy;
}

/**
 * @param newPermissionStore org.jasig.portal.security.provider.ReferencePermissionStore
 */
private void setPermissionStore(IPermissionStore newPermissionStore) {
    permissionStore = newPermissionStore;
}

/**
 * @return org.jasig.portal.security.provider.IAuthorizationService
 */
public static synchronized IAuthorizationService singleton()
throws AuthorizationException
{
    if ( singleton == null )
        { singleton = new AuthorizationImpl(); }
    return singleton;
}

/**
 * Updates <code>IPermissions</code> in the back end store.
 * @param permissions IPermission[]
 * @exception AuthorizationException
 */
public void updatePermissions(IPermission[] permissions)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        getPermissionStore().update(permissions);
        if ( cachePermissions )
            { removeFromPermissionsCache(permissions); }
    }
}

/**
 * Updates <code>IPermissions</code> for the <code>IAuthorizationPrincipal</code>, in the
 * store and clears out the cache.
 * @param permissions IPermission[]
 * @param principal IAuthorizationPrincipal
 * @exception AuthorizationException
 * @deprecated as of uPortal 2.2.  A convenience method that was never used.
 */
public void updatePermissions(IPermission[] permissions, IAuthorizationPrincipal principal)
throws AuthorizationException
{
    if (permissions.length > 0)
    {
        removeFromPermissionsCache(new IAuthorizationPrincipal[] {principal});
        getPermissionStore().update(permissions);
    }
}

/**
 * @return Map
 */
private synchronized Map getPrincipalCache() {
    return principalCache;
}

/**
 * @param map
 */
private synchronized void setPrincipalCache(Map map) {
    principalCache = map;
}

/**
 * @return Map
 */
private Map copyPrincipalCache() {
    HashMap hm = (HashMap)getPrincipalCache();
    return (Map)hm.clone();
}

}
