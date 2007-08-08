/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.cache.CacheFactoryLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public class AuthorizationImpl implements IAuthorizationService {

    /** Instance of log in order to log events. */
    protected final Log log = LogFactory.getLog(getClass());

    /** Constant representing the separator used in the principal key. */
    private static final String PRINCIPAL_SEPARATOR = ".";

    /** The static instance of the AuthorizationImpl for purposes of creating a AuthorizationImpl singleton. */
    private static final IAuthorizationService singleton;

    /** Instance of the Permission Store for storing permission information. */
    private IPermissionStore permissionStore;

    /** The default Permission Policy this Authorization implementation will use. */
    private IPermissionPolicy defaultPermissionPolicy;

    /** The cache to hold the list of principals. */
    private Map<String, IAuthorizationPrincipal> principalCache = CacheFactoryLocator.getCacheFactory().getCache(CacheFactory.PRINCIPAL_CACHE);

    /** The class representing the permission set type. */
    private Class PERMISSION_SET_TYPE;

    /** variable to determine if we should cache permissions or not. */
    private boolean cachePermissions;

    static {
        singleton = new AuthorizationImpl();
    }

  /**
   *
   */
    protected AuthorizationImpl ()
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
        if ( this.cachePermissions )
            { removeFromPermissionsCache(permissions); }
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
        { throw new AuthorizationException("Problem adding permissions for " + ps + " to cache", ce); }
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
          EntityCachingService.instance().get(this.PERMISSION_SET_TYPE, principal.getPrincipalString());
    }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem getting permissions for " + principal + " from cache", ce); }
}

/**
* Removes the <code>IPermissionSet</code> for this principal from the
* entity cache.
*/
protected void cacheRemove(IAuthorizationPrincipal ap) throws AuthorizationException
{
    try
        { EntityCachingService.instance().remove(this.PERMISSION_SET_TYPE, ap.getPrincipalString()); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem removing permissions for " + ap + " from cache", ce); }
}

/**
* Updates the <code>IPermissionSet</code> in the entity cache.
*/
protected void cacheUpdate(IPermissionSet ps) throws AuthorizationException
{
    try
        { EntityCachingService.instance().update(ps); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem updating permissions for " + ps + " in cache", ce); }
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
    ArrayList<IPermission> al = new ArrayList<IPermission>(Arrays.asList(perms));
    Iterator i = getInheritedPrincipals(principal);
    while ( i.hasNext() )
    {
        IAuthorizationPrincipal p = (IAuthorizationPrincipal) i.next();
        perms = getPermissionsForPrincipal(p, owner, activity, target);
        al.addAll(Arrays.asList(perms));
    }
    
    if (log.isTraceEnabled()) {
    	log.trace("query for all permissions for prcinipal=[" + principal + "], owner=[" + owner + 
    			"], activity=[" + activity + "], target=[" + target + "] returned permissions [" + al + "]");
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
    return this.defaultPermissionPolicy;
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

    IGroupMember gm = GroupService.getGroupMember(principal.getKey(), principal.getType());

    if (log.isDebugEnabled()) {
        log.debug("AuthorizationImpl.getGroupMemberForPrincipal(): principal [" + principal + "] " +
                "got group member [" + gm + "]");
    }
        
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
    ArrayList<IAuthorizationPrincipal> al = new ArrayList<IAuthorizationPrincipal>(5);

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
    return this.permissionStore;
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
    int idx = principalString.indexOf(PRINCIPAL_SEPARATOR);
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
    return type + PRINCIPAL_SEPARATOR + pKey;
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

    private void initialize() throws IllegalArgumentException {
        final boolean DEFAULT_CACHE_PERMISSIONS = false;

         String factoryName = PropertiesManager.getProperty(
             "org.jasig.portal.security.IPermissionStore.implementation", null);
         String policyName = PropertiesManager
             .getProperty(
                 "org.jasig.portal.security.IPermissionPolicy.defaultImplementation",
                 null);
         this.cachePermissions = PropertiesManager.getPropertyAsBoolean(
             "org.jasig.portal.security.IAuthorizationService.cachePermissions",
             DEFAULT_CACHE_PERMISSIONS);

         if (factoryName == null) {
             final String eMsg = "AuthorizationImpl.initialize(): No entry for org.jasig.portal.security.IPermissionStore.implementation portal.properties.";
             log.error(eMsg);
             throw new IllegalArgumentException(eMsg);
         }

         if (policyName == null) {
             final String eMsg = "AuthorizationImpl.initialize(): No entry for org.jasig.portal.security.IPermissionPolicy.defaultImplementation portal.properties.";
             log.error(eMsg);
             throw new IllegalArgumentException(eMsg);
         }

         try {
             this.permissionStore = (IPermissionStore)Class.forName(factoryName)
                 .newInstance();
         }
         catch (Exception e) {
             final String eMsg = "AuthorizationImpl.initialize(): Problem creating permission store... ";
             log.error(eMsg, e);
             throw new IllegalArgumentException(eMsg);
         }

         try {
             this.defaultPermissionPolicy = (IPermissionPolicy)Class.forName(
                 policyName).newInstance();
         }
         catch (Exception e) {
             final String eMsg = "AuthorizationImpl.initialize(): Problem creating default permission policy... ";
             log.error(eMsg, e);
             throw new IllegalArgumentException(eMsg);
         }

         try {
             this.PERMISSION_SET_TYPE = Class
                 .forName("org.jasig.portal.security.IPermissionSet");
         }
         catch (ClassNotFoundException cnfe) {
             final String eMsg = "AuthorizationImpl.initialize(): Problem initializing service. ";
             log.error(eMsg, cnfe);
             throw new IllegalArgumentException(eMsg);
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
 * Factory method for IAuthorizationPrincipal. First check the principal
 * cache, and if not present, create the principal and cache it.
 *
 * @return org.jasig.portal.security.IAuthorizationPrincipal
 * @param key java.lang.String
 * @param type java.lang.Class
 */
public IAuthorizationPrincipal newPrincipal(String key, Class type) {
    final String principalKey = getPrincipalString(type, key);

    IAuthorizationPrincipal principal = null;

    synchronized (this.principalCache) {
        if (this.principalCache.containsKey(principalKey)) {
            principal = (IAuthorizationPrincipal) this.principalCache.get(principalKey);
        } else {
            principal = primNewPrincipal(key, type);
            this.principalCache.put(principalKey, principal);
        }
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

    if (log.isDebugEnabled())
        log.debug("AuthorizationImpl.newPrincipal(): for " + type + "(" + key + ")");

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
    if ( ! this.cachePermissions )
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



    IPermission[] perms = primGetPermissionsForPrincipal(principal);
    if ( owner == null && activity == null && target == null )
        { return perms; }
    
    List<IPermission> al = new ArrayList<IPermission>(perms.length);
    
    for ( int i=0; i<perms.length; i++ )
    {
        if (
            (owner == null || owner.equals(perms[i].getOwner())) &&
            (activity == null || activity.equals(perms[i].getActivity())) &&
            (target == null || target.equals(perms[i].getTarget()))
           )
            { al.add(perms[i]); }
    }


    
    if (log.isTraceEnabled()) {
        log.trace(
                "AuthorizationImpl.primGetPermissionsForPrincipal(): " +
                "Principal: " + principal + " owner: " + owner +
                " activity: " + activity + " target: " + target + " : permissions retrieved: " + al);
    } else if (log.isDebugEnabled()) {
        log.debug(
                "AuthorizationImpl.primGetPermissionsForPrincipal(): " +
                "Principal: " + principal + " owner: " + owner +
                " activity: " + activity + " target: " + target + " : number of permissions retrieved: " + al.size());
    }


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
        if ( this.cachePermissions )
            { removeFromPermissionsCache(permissions); }
    }
}

/**
 * @param newDefaultPermissionPolicy org.jasig.portal.security.IPermissionPolicy
 */
protected void setDefaultPermissionPolicy(IPermissionPolicy newDefaultPermissionPolicy) {
    this.defaultPermissionPolicy = newDefaultPermissionPolicy;
}

/**
 * @return org.jasig.portal.security.provider.IAuthorizationService
 */
public static IAuthorizationService singleton()
{
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
        if ( this.cachePermissions )
            { removeFromPermissionsCache(permissions); }
    }
}
}
