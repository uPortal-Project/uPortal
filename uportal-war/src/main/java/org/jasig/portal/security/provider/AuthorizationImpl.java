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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.caching.RequestCache;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.PortletCategory;
import org.jasig.portal.portlet.om.PortletLifecycleState;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IPermissionPolicy;
import org.jasig.portal.security.IPermissionSet;
import org.jasig.portal.security.IPermissionStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.spring.locator.PortletCategoryRegistryLocator;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
@Service("authorizationService")
public class AuthorizationImpl implements IAuthorizationService {

    /** Instance of log in order to log events. */
    protected final Log log = LogFactory.getLog(getClass());

    /** Constant representing the separator used in the principal key. */
    private static final String PRINCIPAL_SEPARATOR = ".";

    /** Instance of the Permission Store for storing permission information. */
    private IPermissionStore permissionStore;

    /** The default Permission Policy this Authorization implementation will use. */
    private IPermissionPolicy defaultPermissionPolicy;
    
    /** Spring-configured portlet definition registry instance */
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    /** The cache to hold the list of principals. */
    private Ehcache principalCache;

    /** The cache to hold the list of principals. */
    private Ehcache entityParentsCache;

    /** The cache to hold permission resolution. */
    private Ehcache doesPrincipalHavePermissionCache;

    /** The class representing the permission set type. */
    private static final Class<IPermissionSet> PERMISSION_SET_TYPE = IPermissionSet.class;

    /** variable to determine if we should cache permissions or not. */
    private boolean cachePermissions = true;
    
    
    @Autowired
    public void setDefaultPermissionPolicy(IPermissionPolicy newDefaultPermissionPolicy) {
        this.defaultPermissionPolicy = newDefaultPermissionPolicy;
    }
    @Autowired
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }
    @Value("${org.jasig.portal.security.IAuthorizationService.cachePermissions}")
    public void setCachePermissions(boolean cachePermissions) {
        this.cachePermissions = cachePermissions;
    }
    @Autowired
    public void setPrincipalCache(@Qualifier(CacheFactory.PRINCIPAL_CACHE)  Ehcache principalCache) {
        this.principalCache = new SelfPopulatingCache(principalCache, new CacheEntryFactory() {
            @Override
            public Object createEntry(Object key) throws Exception {
                final Tuple<String, Class> principalKey = (Tuple<String, Class>)key;
                return primNewPrincipal(principalKey.first, principalKey.second);
            }
        });
    }
    @Autowired
    public void setEntityParentsCache(@Qualifier(CacheFactory.ENTITY_PARENTS_CACHE)  Ehcache entityParentsCache) {
        this.entityParentsCache = entityParentsCache;
    }
    @Autowired
    public void setDoesPrincipalHavePermissionCache(@Qualifier("org.jasig.portal.security.provider.AuthorizationImpl.PRINCIPAL_HAS_PERMISSION") Ehcache doesPrincipalHavePermissionCache) {
        this.doesPrincipalHavePermissionCache = doesPrincipalHavePermissionCache;
    }
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
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
        { EntityCachingService.getEntityCachingService().add(ps); }
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
          EntityCachingService.getEntityCachingService().get(this.PERMISSION_SET_TYPE, principal.getPrincipalString());
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
        { EntityCachingService.getEntityCachingService().remove(this.PERMISSION_SET_TYPE, ap.getPrincipalString()); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem removing permissions for " + ap + " from cache", ce); }
}

/**
* Updates the <code>IPermissionSet</code> in the entity cache.
*/
protected void cacheUpdate(IPermissionSet ps) throws AuthorizationException
{
    try
        { EntityCachingService.getEntityCachingService().update(ps); }
    catch (CachingException ce)
        { throw new AuthorizationException("Problem updating permissions for " + ps + " in cache", ce); }
}

@Override
@RequestCache
public boolean canPrincipalConfigure(IAuthorizationPrincipal principal, String portletDefinitionId) throws AuthorizationException {
    String owner = IPermission.PORTAL_PUBLISH;
    String target = IPermission.PORTLET_PREFIX + portletDefinitionId;
    
    // retrieve the indicated channel from the channel registry store and 
    // determine its current lifecycle state
    IPortletDefinition portlet = this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    if (portlet == null){
        throw new AuthorizationException("Unable to locate portlet " + portletDefinitionId);
    }
    
    final String activity = IPermission.PORTLET_MODE_CONFIG;
    return doesPrincipalHavePermission(principal, owner, activity, target);
}
/**
 * Answers if the principal has permission to MANAGE this Channel.
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param channelPublishId int
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
@RequestCache
public boolean canPrincipalManage(IAuthorizationPrincipal principal, String portletDefinitionId)
throws AuthorizationException
{
    String owner = IPermission.PORTAL_PUBLISH;
    String target = IPermission.PORTLET_PREFIX + portletDefinitionId;
    
    // retrieve the indicated channel from the channel registry store and 
    // determine its current lifecycle state
    IPortletDefinition portlet = this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    if (portlet == null){
    	return doesPrincipalHavePermission(principal, owner,
				IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY, target);
//    	throw new AuthorizationException("Unable to locate channel " + channelPublishId);
    }    
    PortletLifecycleState state = portlet.getLifecycleState();
    int order = state.getOrder();
    
    /*
     * The following code assumes that later lifecycle states imply permission
     * for earlier lifecycle states.  For example, if a user has permission to 
     * manage an expired channel, we assume s/he also has permission to 
     * create, approve, and publish channels.  The following code counts 
     * channels with auto-publish or auto-expiration dates set as requiring
     * publish or expiration permissions for management, even though the channel 
     * may not yet be published or expired.
     */
    
    String activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
	if ((order <= PortletLifecycleState.EXPIRED.getOrder() 
			|| portlet.getExpirationDate() != null)
			&& doesPrincipalHavePermission(principal, owner, activity, target)) {
		return true;
    } 
	
	activity = IPermission.PORTLET_MANAGER_ACTIVITY;
	if ((order <= PortletLifecycleState.PUBLISHED.getOrder() 
    		|| portlet.getPublishDate() != null)
			&& doesPrincipalHavePermission(principal, owner, activity, target)) {
    	return true;
    } 
	
	activity = IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY;
	log.debug("order: " + order + ", approved order: " + PortletLifecycleState.APPROVED.getOrder());
	if (order <= PortletLifecycleState.APPROVED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, target)) {
    	return true;
    } 
	
	activity = IPermission.PORTLET_MANAGER_CREATED_ACTIVITY;
	if (order <= PortletLifecycleState.CREATED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, target)) {
    	return true;
    }
    	
	// if no permissions were found, return false
	return false;
}

/**
 * This checks if the framework has granted principal a right to publish.  DO WE WANT SOMETHING THIS COARSE (de)?
 * @param principal IAuthorizationPrincipal
 * @return boolean
 */
@RequestCache
public boolean canPrincipalManage(IAuthorizationPrincipal principal, PortletLifecycleState state, String categoryId) throws AuthorizationException
{
//    return doesPrincipalHavePermission
//      (principal, IPermission.PORTAL_FRAMEWORK, IPermission.CHANNEL_PUBLISHER_ACTIVITY, null);
    String owner = IPermission.PORTAL_PUBLISH;
    
    // retrieve the indicated channel from the channel registry store and 
    // determine its current lifecycle state
    PortletCategory category = PortletCategoryRegistryLocator.getPortletCategoryRegistry().getPortletCategory(categoryId);
    if (category == null){
//    	return doesPrincipalHavePermission(principal, owner,
//				IPermission.CHANNEL_MANAGER_APPROVED_ACTIVITY, target);
    	throw new AuthorizationException("Unable to locate category " + categoryId);
    }    
    int order = state.getOrder();
    
    String activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
	if (order <= PortletLifecycleState.EXPIRED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, categoryId)) {
		return true;
    }
	
    activity = IPermission.PORTLET_MANAGER_ACTIVITY;
	if (order <= PortletLifecycleState.PUBLISHED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, categoryId)) {
    	return true;
    }
	
    activity = IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY;
	if (order <= PortletLifecycleState.APPROVED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, categoryId)) {
    	return true;
    }
	
    activity = IPermission.PORTLET_MANAGER_CREATED_ACTIVITY;
	if (order <= PortletLifecycleState.CREATED.getOrder()
			&& doesPrincipalHavePermission(principal, owner, activity, categoryId)) {
    	return true;
    }
    	
	return false;

}

/**
 * Answers if the principal has permission to RENDER this Channel.  This 
 * implementation currently delegates to the SUBSCRIBE permission.
 * 
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param channelPublishId int
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
@RequestCache
public boolean canPrincipalRender(IAuthorizationPrincipal principal, String portletDefinitionId)
throws AuthorizationException
{
	// This code simply assumes that anyone who can subscribe to a channel 
	// should be able to render it.  In the future, we'd like to update this
	// implementation to use a separate permission for rendering.
    return canPrincipalSubscribe(principal, portletDefinitionId);
}

/**
 * Answers if the principal has permission to SUBSCRIBE to this Channel.
 * @return boolean
 * @param principal IAuthorizationPrincipal
 * @param channelPublishId int
 * @exception AuthorizationException indicates authorization information could not be retrieved.
 */
@RequestCache
public boolean canPrincipalSubscribe(IAuthorizationPrincipal principal, String portletDefinitionId)
{
    String owner = IPermission.PORTAL_SUBSCRIBE;
    String target = IPermission.PORTLET_PREFIX + portletDefinitionId;
    
    // retrieve the indicated channel from the channel registry store and 
    // determine its current lifecycle state
    IPortletDefinition portlet = this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
    if (portlet == null){
    	return false;
    }    
    PortletLifecycleState state = portlet.getLifecycleState();
    
    /*
     * Each channel lifecycle state now has its own subscribe permission.  The
     * following logic checks the appropriate permission for the lifecycle.
     */
    String permission;
    if (state.equals(PortletLifecycleState.PUBLISHED)) {
    	permission = IPermission.PORTLET_SUBSCRIBER_ACTIVITY;
    } else if (state.equals(PortletLifecycleState.APPROVED)) {
    	permission = IPermission.PORTLET_SUBSCRIBER_APPROVED_ACTIVITY;
    } else if (state.equals(PortletLifecycleState.CREATED)) {
    	permission = IPermission.PORTLET_SUBSCRIBER_CREATED_ACTIVITY;
    } else if (state.equals(PortletLifecycleState.EXPIRED)) {
    	permission = IPermission.PORTLET_SUBSCRIBER_EXPIRED_ACTIVITY;
    } else {
			throw new AuthorizationException(
					"Unrecognized lifecycle state for channel "
							+ portletDefinitionId);
    }

    // test the appropriate permission
    return doesPrincipalHavePermission(principal, owner, permission, target);

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
@RequestCache
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
    @Override
    @RequestCache
    public boolean doesPrincipalHavePermission(IAuthorizationPrincipal principal, String owner, String activity,
            String target, IPermissionPolicy policy) throws AuthorizationException {
        final CacheKey key = CacheKey.build(AuthorizationImpl.class.getName(), policy.getClass(), principal.getKey(),
                principal.getType(), owner, activity, target);

        final Element element = this.doesPrincipalHavePermissionCache.get(key);
        if (element != null) {
            return (Boolean) element.getValue();
        }

        final boolean doesPrincipalHavePermission = policy.doesPrincipalHavePermission(this,
                principal,
                owner,
                activity,
                target);
        
        this.doesPrincipalHavePermissionCache.put(new Element(key, doesPrincipalHavePermission));

        return doesPrincipalHavePermission;
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
@RequestCache
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
@RequestCache
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
    final Tuple<String, Class> principalKey = new Tuple<String, Class>(key, type);
    final Element element = this.principalCache.get(principalKey);
    //principalCache is self populating, it can never return a null entry
    return (IAuthorizationPrincipal)element.getObjectValue();
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

    /*
     * Get a list of all permissions for the specified principal, then iterate
     * through them to build a list of the permissions matching the specified
     * criteria.
     */

    IPermission[] perms = primGetPermissionsForPrincipal(principal);
    if ( owner == null && activity == null && target == null )
        { return perms; }

	Set<String> containingGroups;
	
	if (target != null) {
		
        final Element element = this.entityParentsCache.get(target);
        if (element != null) {
            containingGroups = (Set<String>) element.getObjectValue();
        }
        else {
        	containingGroups = new HashSet<String>();
        	IGroupMember targetEntity = GroupService.findGroup(target);
    		if (targetEntity == null) {
    			if (target.startsWith(IPermission.PORTLET_PREFIX)) {
    				targetEntity = GroupService.getGroupMember(target.replace(IPermission.PORTLET_PREFIX, ""), IPortletDefinition.class);
    			} else {
    				targetEntity = GroupService.getGroupMember(target, IPerson.class);
    			}
    		}
    		
    		if (targetEntity != null) {
    			for (Iterator containing = targetEntity.getAllContainingGroups(); containing.hasNext();) {
    				containingGroups.add(((IEntityGroup)containing.next()).getKey());
    			}
    		}
    		
    		this.entityParentsCache.put(new Element(target, containingGroups));
        	
        }

		
	} else {
		containingGroups = new HashSet<String>();
	}

    List<IPermission> al = new ArrayList<IPermission>(perms.length);
    
    for ( int i=0; i<perms.length; i++ ) {
        String permissionTarget = perms[i].getTarget();
        
        if (
        		// owner matches
        		(owner == null || owner.equals(perms[i].getOwner())) &&
        		// activity matches
                (activity == null || activity.equals(perms[i].getActivity())) &&
                // target matches or is a member of the current permission target
                (target == null || target.equals(permissionTarget) 
                		|| containingGroups.contains(permissionTarget))    
            ) {
        	
            al.add(perms[i]);
        } 
        
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
