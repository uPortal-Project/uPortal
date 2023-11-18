/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.concurrency.CachingException;
import org.apereo.portal.concurrency.caching.RequestCache;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.ICompositeGroupService;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.permission.dao.IPermissionOwnerDao;
import org.apereo.portal.permission.target.IPermissionTarget;
import org.apereo.portal.permission.target.IPermissionTargetProvider;
import org.apereo.portal.permission.target.IPermissionTargetProviderRegistry;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.security.*;
import org.apereo.portal.services.EntityCachingService;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.apereo.portal.spring.locator.PortletCategoryRegistryLocator;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.utils.cache.CacheFactory;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.apereo.portal.utils.cache.UsernameTaggedCacheEntryPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** */
@Service("authorizationService")
public class AuthorizationImpl implements IAuthorizationService {

    /**
     * Period during which this service will not complain (in the logs) about the same item of
     * missing data, currently 5 minutes. Without this throttle, the logs would quickly fill with
     * the same message when there is missing data.
     */
    private static final long MISSING_DATA_LOG_PERIOD_MILLIS = TimeUnit.MINUTES.toMillis(5L);

    /** Instance of log in order to log events. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Constant representing the separator used in the principal key. */
    private static final String PRINCIPAL_SEPARATOR = ".";

    /** Instance of the Permission Store for storing permission information. */
    private IPermissionStore permissionStore;

    /** The default Permission Policy this Authorization implementation will use. */
    private IPermissionPolicy defaultPermissionPolicy;

    /** Spring-configured portlet definition registry instance */
    private IPortletDefinitionRegistry portletDefinitionRegistry;

    /** Indicates permission activity to permissionTargetProvider. */
    private IPermissionOwnerDao permissionOwner;

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

    private Set<String> nonEntityPermissionTargetProviders = new HashSet<>();

    private Map<Object, Long> missingDataLogTracker = new ConcurrentHashMap<>();

    @Autowired private IPermissionOwnerDao permissionOwnerDao;

    @Autowired private IPermissionTargetProviderRegistry targetProviderRegistry;

    @Autowired
    public void setDefaultPermissionPolicy(IPermissionPolicy newDefaultPermissionPolicy) {
        this.defaultPermissionPolicy = newDefaultPermissionPolicy;
    }

    @Autowired
    public void setPermissionStore(IPermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    @Value("${org.apereo.portal.security.IAuthorizationService.cachePermissions}")
    public void setCachePermissions(boolean cachePermissions) {
        this.cachePermissions = cachePermissions;
    }

    @Autowired
    public void setPrincipalCache(@Qualifier(CacheFactory.PRINCIPAL_CACHE) Ehcache principalCache) {
        this.principalCache =
                new SelfPopulatingCache(
                        principalCache,
                        new CacheEntryFactory() {
                            @Override
                            public Object createEntry(Object key) throws Exception {
                                final Tuple<String, Class> principalKey =
                                        (Tuple<String, Class>) key;
                                return primNewPrincipal(principalKey.first, principalKey.second);
                            }
                        });
    }

    @Autowired
    public void setEntityParentsCache(
            @Qualifier(CacheFactory.ENTITY_PARENTS_CACHE) Ehcache entityParentsCache) {
        this.entityParentsCache = entityParentsCache;
    }

    @Autowired
    public void setDoesPrincipalHavePermissionCache(
            @Qualifier(
                            "org.apereo.portal.security.provider.AuthorizationImpl.PRINCIPAL_HAS_PERMISSION")
                    Ehcache doesPrincipalHavePermissionCache) {
        this.doesPrincipalHavePermissionCache = doesPrincipalHavePermissionCache;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setPermissionOwner(IPermissionOwnerDao permissionOwner) {
        this.permissionOwner = permissionOwner;
    }

    public void setNonEntityPermissionTargetProviders(
            Set<String> nonEntityPermissionTargetProviders) {
        this.nonEntityPermissionTargetProviders = nonEntityPermissionTargetProviders;
    }

    /**
     * Adds <code>IPermissions</code> to the back end store.
     *
     * @param permissions IPermission[]
     * @exception AuthorizationException
     */
    @Override
    public void addPermissions(IPermission[] permissions) throws AuthorizationException {
        if (permissions.length > 0) {
            getPermissionStore().add(permissions);
            if (this.cachePermissions) {
                removeFromPermissionsCache(permissions);
            }
        }
    }

    /** Adds the <code>IPermissionSet</code> to the entity cache. */
    protected void cacheAdd(IPermissionSet ps) throws AuthorizationException {
        try {
            EntityCachingService.getEntityCachingService().add(ps);
        } catch (CachingException ce) {
            throw new AuthorizationException(
                    "Problem adding permissions for " + ps + " to cache", ce);
        }
    }

    /**
     * Retrieves the <code>IPermissionSet</code> for the <code>IPermissionSet</code> from the entity
     * cache.
     */
    protected IPermissionSet cacheGet(IAuthorizationPrincipal principal)
            throws AuthorizationException {
        try {
            return (IPermissionSet)
                    EntityCachingService.getEntityCachingService()
                            .get(this.PERMISSION_SET_TYPE, principal.getPrincipalString());
        } catch (CachingException ce) {
            throw new AuthorizationException(
                    "Problem getting permissions for " + principal + " from cache", ce);
        }
    }

    /** Removes the <code>IPermissionSet</code> for this principal from the entity cache. */
    protected void cacheRemove(IAuthorizationPrincipal ap) throws AuthorizationException {
        try {
            EntityCachingService.getEntityCachingService()
                    .remove(this.PERMISSION_SET_TYPE, ap.getPrincipalString());
        } catch (CachingException ce) {
            throw new AuthorizationException(
                    "Problem removing permissions for " + ap + " from cache", ce);
        }
    }

    @Override
    @RequestCache
    public boolean canPrincipalConfigure(
            IAuthorizationPrincipal principal, String portletDefinitionId)
            throws AuthorizationException {
        String owner = IPermission.PORTAL_PUBLISH;
        String target = IPermission.PORTLET_PREFIX + portletDefinitionId;

        // retrieve the indicated channel from the channel registry store and
        // determine its current lifecycle state
        IPortletDefinition portlet =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        if (portlet == null) {
            throw new AuthorizationException("Unable to locate portlet " + portletDefinitionId);
        }

        final String activity = IPermission.PORTLET_MODE_CONFIG;

        boolean isAllowed = doesPrincipalHavePermission(principal, owner, activity, target);
        logger.trace(
                "In canPrincipalConfigure() - principal.key=[{}], is allowed?=[{}]",
                principal.getKey(),
                isAllowed);
        return isAllowed;
    }
    /**
     * Answers if the principal has permission to MANAGE this Channel.
     *
     * @param principal IAuthorizationPrincipal The user who wants to manage the portlet
     * @param portletDefinitionId The Id of the portlet being managed
     * @return True if the specified user is allowed to manage the specified portlet; otherwise
     *     false
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public boolean canPrincipalManage(IAuthorizationPrincipal principal, String portletDefinitionId)
            throws AuthorizationException {

        final String owner = IPermission.PORTAL_PUBLISH;
        final String target = IPermission.PORTLET_PREFIX + portletDefinitionId;

        // Retrieve the indicated portlet from the portlet registry store and
        // determine its current lifecycle state.
        IPortletDefinition portlet =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        if (portlet == null) {
            /*
             * Is this what happens when a portlet is new?  Shouldn't we
             * be checking PORTLET_MANAGER_CREATED_ACTIVITY in that case?
             */
            return doesPrincipalHavePermission(
                    principal, owner, IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY, target);
        }

        /*
         * The following code assumes that later lifecycle states imply permission
         * for earlier lifecycle states.  For example, if a user has permission to
         * manage an expired channel, we assume s/he also has permission to
         * create, approve, and publish channels.  The following code counts
         * channels with auto-publish or auto-expiration dates set as requiring
         * publish or expiration permissions for management, even though the channel
         * may not yet be published or expired.
         */

        final IPortletLifecycleEntry highestLifecycleEntryDefined =
                portlet.getLifecycle().get(portlet.getLifecycle().size() - 1);

        String activity;
        switch (highestLifecycleEntryDefined.getLifecycleState()) {
            case CREATED:
                activity = IPermission.PORTLET_MANAGER_CREATED_ACTIVITY;
                break;
            case APPROVED:
                activity = IPermission.PORTLET_MANAGER_APPROVED_ACTIVITY;
                break;
            case PUBLISHED:
                activity = IPermission.PORTLET_MANAGER_ACTIVITY;
                break;
            case EXPIRED:
                activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
                break;
            case MAINTENANCE:
                activity = IPermission.PORTLET_MANAGER_MAINTENANCE_ACTIVITY;
                break;
            default:
                final String msg =
                        "Unrecognized portlet lifecycle state:  "
                                + highestLifecycleEntryDefined.getLifecycleState();
                throw new IllegalStateException(msg);
        }

        return doesPrincipalHavePermission(principal, owner, activity, target);
    }

    /**
     * This checks if the framework has granted principal a right to publish. DO WE WANT SOMETHING
     * THIS COARSE (de)?
     *
     * @param principal IAuthorizationPrincipal
     * @return boolean
     */
    @Override
    @RequestCache
    public boolean canPrincipalManage(
            IAuthorizationPrincipal principal, PortletLifecycleState state, String categoryId)
            throws AuthorizationException {
        //    return doesPrincipalHavePermission
        //      (principal, IPermission.PORTAL_FRAMEWORK, IPermission.CHANNEL_PUBLISHER_ACTIVITY,
        // null);
        String owner = IPermission.PORTAL_PUBLISH;

        // retrieve the indicated channel from the channel registry store and
        // determine its current lifecycle state
        PortletCategory category =
                PortletCategoryRegistryLocator.getPortletCategoryRegistry()
                        .getPortletCategory(categoryId);
        if (category == null) {
            //    	return doesPrincipalHavePermission(principal, owner,
            //				IPermission.CHANNEL_MANAGER_APPROVED_ACTIVITY, target);
            throw new AuthorizationException("Unable to locate category " + categoryId);
        }
        int order = state.getOrder();

        String activity = IPermission.PORTLET_MANAGER_MAINTENANCE_ACTIVITY;
        if (order <= PortletLifecycleState.MAINTENANCE.getOrder()
                && doesPrincipalHavePermission(principal, owner, activity, categoryId)) {
            return true;
        }

        activity = IPermission.PORTLET_MANAGER_EXPIRED_ACTIVITY;
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
     * Answers if the principal has permission to RENDER this Channel. This implementation currently
     * delegates to the SUBSCRIBE permission.
     *
     * @return boolean
     * @param principal IAuthorizationPrincipal
     * @param portletDefinitionId
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public boolean canPrincipalRender(IAuthorizationPrincipal principal, String portletDefinitionId)
            throws AuthorizationException {
        // This code simply assumes that anyone who can subscribe to a channel
        // should be able to render it.  In the future, we'd like to update this
        // implementation to use a separate permission for rendering.
        return canPrincipalSubscribe(principal, portletDefinitionId);
    }

    @Override
    @RequestCache
    public boolean canPrincipalBrowse(
            IAuthorizationPrincipal principal, String portletDefinitionId) {

        // Retrieve the indicated portlet from the channel registry store.
        IPortletDefinition portlet =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        if (portlet == null) {
            return false;
        }
        return canPrincipalBrowse(principal, portlet);
    }

    @Override
    @RequestCache
    public boolean canPrincipalBrowse(
            IAuthorizationPrincipal principal, IPortletDefinition portlet) {
        String owner = IPermission.PORTAL_SUBSCRIBE;

        String target = PermissionHelper.permissionTargetIdForPortletDefinition(portlet);

        PortletLifecycleState state = portlet.getLifecycleState();

        /*
         * Each channel lifecycle state now has its own browse permission.  The
         * following logic checks the appropriate permission for the lifecycle.
         */
        String permission;
        if (state.equals(PortletLifecycleState.PUBLISHED)
                || state.equals(PortletLifecycleState.MAINTENANCE)) {
            // NB:  There is no separate BROWSE permission for MAINTENANCE
            // mode;  everyone simply sees the 'out of service' message
            permission = IPermission.PORTLET_BROWSE_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.APPROVED)) {
            permission = IPermission.PORTLET_BROWSE_APPROVED_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.CREATED)) {
            permission = IPermission.PORTLET_BROWSE_CREATED_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.EXPIRED)) {
            permission = IPermission.PORTLET_BROWSE_EXPIRED_ACTIVITY;
        } else {
            throw new AuthorizationException(
                    "Unrecognized lifecycle state for channel "
                            + portlet.getPortletDefinitionId().getStringId());
        }

        // Test the appropriate permission.
        return doesPrincipalHavePermission(principal, owner, permission, target);
    }

    /**
     * Answers if the principal has permission to SUBSCRIBE to this Channel.
     *
     * @return boolean
     * @param principal IAuthorizationPrincipal
     * @param portletDefinitionId
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public boolean canPrincipalSubscribe(
            IAuthorizationPrincipal principal, String portletDefinitionId) {
        String owner = IPermission.PORTAL_SUBSCRIBE;

        // retrieve the indicated channel from the channel registry store and
        // determine its current lifecycle state
        IPortletDefinition portlet =
                this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId);
        if (portlet == null) {
            return false;
        }

        String target = PermissionHelper.permissionTargetIdForPortletDefinition(portlet);

        PortletLifecycleState state = portlet.getLifecycleState();

        /*
         * Each channel lifecycle state now has its own subscribe permission.  The
         * following logic checks the appropriate permission for the lifecycle.
         */
        String permission;
        if (state.equals(PortletLifecycleState.PUBLISHED)
                || state.equals(PortletLifecycleState.MAINTENANCE)) {
            // NB:  There is no separate SUBSCRIBE permission for MAINTENANCE
            // mode;  everyone simply sees the 'out of service' message
            permission = IPermission.PORTLET_SUBSCRIBER_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.APPROVED)) {
            permission = IPermission.PORTLET_SUBSCRIBER_APPROVED_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.CREATED)) {
            permission = IPermission.PORTLET_SUBSCRIBER_CREATED_ACTIVITY;
        } else if (state.equals(PortletLifecycleState.EXPIRED)) {
            permission = IPermission.PORTLET_SUBSCRIBER_EXPIRED_ACTIVITY;
        } else {
            throw new AuthorizationException(
                    "Unrecognized lifecycle state for channel " + portletDefinitionId);
        }

        // Test the appropriate permission.
        return doesPrincipalHavePermission(principal, owner, permission, target);
    }

    /**
     * Answers if the owner has given the principal (or any of its parents) permission to perform
     * the activity on the target. Params <code>owner</code> and <code>activity</code> must be
     * non-null. If <code>target</code> is null, then target is not checked.
     *
     * @return boolean
     * @param principal IAuthorizationPrincipal
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public boolean doesPrincipalHavePermission(
            IAuthorizationPrincipal principal, String owner, String activity, String target)
            throws AuthorizationException {
        return doesPrincipalHavePermission(
                principal, owner, activity, target, getDefaultPermissionPolicy());
    }

    /**
     * Answers if the owner has given the principal permission to perform the activity on the
     * target, as evaluated by the policy. Params <code>policy</code>, <code>owner</code> and <code>
     * activity</code> must be non-null.
     *
     * @return boolean
     * @param principal IAuthorizationPrincipal
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public boolean doesPrincipalHavePermission(
            IAuthorizationPrincipal principal,
            String owner,
            String activity,
            String target,
            IPermissionPolicy policy)
            throws AuthorizationException {

        final CacheKeyBuilder<Serializable, Serializable> cacheKeyBuilder =
                CacheKey.builder(AuthorizationImpl.class.getName());
        final String username = principal.getKey();
        if (IPerson.class.equals(principal.getType())) {
            cacheKeyBuilder.addTag(UsernameTaggedCacheEntryPurger.createCacheEntryTag(username));
        }
        cacheKeyBuilder.addAll(
                policy.getClass(), username, principal.getType(), owner, activity, target);

        final CacheKey key = cacheKeyBuilder.build();

        final Element element = this.doesPrincipalHavePermissionCache.get(key);
        if (element != null) {
            return (Boolean) element.getValue();
        }

        boolean rslt = false; // fail closed

        /*
         * Convert to (strongly-typed) Java objects based on interfaces in
         * o.j.p.permission before we make the actual check with IPermissionPolicy;
         * parameters that communicate something of the nature of the things they
         * represent helps us make the check(s) more intelligently.  This objects
         * were retro-fitted to IPermissionPolicy in uP 4.3;  perhaps we should do
         * the same to IAuthorizationService itself?
         */
        final IPermissionOwner ipOwner = permissionOwnerDao.getPermissionOwner(owner);
        final IPermissionActivity ipActivity =
                permissionOwnerDao.getPermissionActivity(owner, activity);
        if (ipActivity != null) {
            final IPermissionTargetProvider targetProvider =
                    targetProviderRegistry.getTargetProvider(ipActivity.getTargetProviderKey());
            final IPermissionTarget ipTarget = targetProvider.getTarget(target);
            rslt =
                    policy.doesPrincipalHavePermission(
                            this, principal, ipOwner, ipActivity, ipTarget);
        } else {
            /*
             * This circumstance means that a piece of the fundamental Permissions data expected by
             * the code is missing in the database.  It normally happens when a newer version of the
             * uPortal code is run against an existing database, and a required data update was
             * overlooked.  This condition is not great, but probably not catastrophic;  it means
             * that no one will (or can) have the new permission.  This method returns false.
             *
             * Administrators, however, have permission to do anything, including this unknown
             * activity.  It's most common in uPortal for only Administrators to have access to
             * exotic activities, so in most cases this omission is a wash.
             *
             * We need to log a WARNing, but this method is invoked a lot, and we don't want to do
             * it incessantly.
             */
            final Long now = System.currentTimeMillis();
            final String missingDataTrackerKey = owner + ":" + activity;
            final Long lastLogMessageTime = missingDataLogTracker.get(missingDataTrackerKey);
            if (lastLogMessageTime == null
                    || lastLogMessageTime < now - MISSING_DATA_LOG_PERIOD_MILLIS) {
                logger.warn(
                        "Activity '{}' is not defined for owner '{}';  only admins will be "
                                + "able to access this function;  this warning usually means that expected data "
                                + "was not imported",
                        activity,
                        owner);
                missingDataLogTracker.put(missingDataTrackerKey, now);
            }
            // This pass becomes a check for superuser (Portal Administrators)
            rslt =
                    doesPrincipalHavePermission(
                            principal,
                            IPermission.PORTAL_SYSTEM,
                            IPermission.ALL_PERMISSIONS_ACTIVITY,
                            IPermission.ALL_TARGET,
                            policy);
        }

        this.doesPrincipalHavePermissionCache.put(new Element(key, rslt));

        return rslt;
    }

    /**
     * Retrieves a specific {@code IPortletPermissionHandler} based on the provided {@code
     * PortletPermissionType}.
     *
     * @param requiredPermissionType The type of portlet permission required.
     * @return An implementation of {@code IPortletPermissionHandler} corresponding to the provided
     *     permission type.
     * @throws IllegalArgumentException If the provided permission type is unknown.
     */
    @Override
    public IPortletPermissionHandler getPermission(PortletPermissionType requiredPermissionType) {
        switch (requiredPermissionType) {
            case BROWSE:
                return new BrowsePermissionHandler(this);
            case CONFIGURE:
                return new ConfigurePermissionHandler(this);
            case MANAGE:
                return new ManagePermissionHandler(this);
            case RENDER:
                return new RenderPermissionHandler(this);
            case SUBSCRIBE:
                return new SubscribePermissionHandler(this);
            default:
                throw new IllegalArgumentException(
                        "Unknown requiredPermissionType: " + requiredPermissionType);
        }
    }

    /**
     * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for the
     * specified activity and target. Null parameters will be ignored, that is, all <code>
     * IPermissions</code> matching the non-null parameters are retrieved. So, <code>
     * getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions
     * </code> for a <code>Principal</code>. Note that this includes <code>IPermissions</code>
     * inherited from groups the <code>Principal</code> belongs to.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param principal IAuthorizationPrincipal
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    public IPermission[] getAllPermissionsForPrincipal(
            IAuthorizationPrincipal principal, String owner, String activity, String target)
            throws AuthorizationException {
        IPermission[] perms = getPermissionsForPrincipal(principal, owner, activity, target);
        ArrayList<IPermission> al = new ArrayList<>(Arrays.asList(perms));
        Iterator i = getInheritedPrincipals(principal);
        while (i.hasNext()) {
            IAuthorizationPrincipal p = (IAuthorizationPrincipal) i.next();
            perms = getPermissionsForPrincipal(p, owner, activity, target);
            al.addAll(Arrays.asList(perms));
        }

        logger.trace(
                "query for all permissions for principal=[{}], owner=[{}], activity=[{}], target=[{}] returned permissions [{}]",
                principal,
                owner,
                activity,
                target,
                al);

        return ((IPermission[]) al.toArray(new IPermission[al.size()]));
    }

    /**
     * Returns <code>IAuthorizationPrincipals</code> that have <code>IPermissions</code> for the
     * given owner, activity and target.
     *
     * @return IAuthorizationPrincipal[]
     * @param owner
     * @param activity
     * @param target
     */
    public IAuthorizationPrincipal[] getAuthorizedPrincipals(
            String owner, String activity, String target) throws AuthorizationException {
        IPermission[] permissions = getPermissionsForOwner(owner, activity, target);
        return getPrincipalsFromPermissions(permissions);
    }

    /** @return org.apereo.portal.security.IPermissionPolicy */
    protected IPermissionPolicy getDefaultPermissionPolicy() {
        return this.defaultPermissionPolicy;
    }

    /**
     * @return org.apereo.portal.groups.IGroupMember
     * @param principal org.apereo.portal.security.IAuthorizationPrincipal
     */
    @Override
    public IGroupMember getGroupMember(IAuthorizationPrincipal principal) throws GroupsException {
        return getGroupMemberForPrincipal(principal);
    }

    /**
     * @return org.apereo.portal.groups.IGroupMember
     * @param principal org.apereo.portal.security.IAuthorizationPrincipal
     */
    private IGroupMember getGroupMemberForPrincipal(IAuthorizationPrincipal principal)
            throws GroupsException {

        IGroupMember gm = GroupService.getGroupMember(principal.getKey(), principal.getType());

        logger.debug(
                "AuthorizationImpl.getGroupMemberForPrincipal(): principal [{}] got group member [{}]",
                principal,
                gm);

        return gm;
    }

    /**
     * Hook into the Groups system by converting the <code>IAuthorizationPrincipal</code> to an
     * <code>IGroupMember</code>. Returns ALL the groups the <code>IGroupMember</code> (recursively)
     * belongs to.
     *
     * @param principal - org.apereo.portal.security.IAuthorizationPrincipal
     * @return java.util.Iterator over Collection of IEntityGroups
     */
    private Iterator getGroupsForPrincipal(IAuthorizationPrincipal principal)
            throws GroupsException {
        IGroupMember gm = getGroupMemberForPrincipal(principal);
        return gm.getAncestorGroups().iterator();
    }

    /**
     * Hook into the Groups system, find all containing groups, and convert the them to <code>
     * IAuthorizationPrincipals</code>.
     *
     * @param principal - org.apereo.portal.security.IAuthorizationPrincipal
     * @return java.util.Iterator over Collection of IEntityGroups
     */
    private Iterator getInheritedPrincipals(IAuthorizationPrincipal principal)
            throws AuthorizationException {
        Iterator i = null;
        ArrayList<IAuthorizationPrincipal> al = new ArrayList<>(5);

        try {
            i = getGroupsForPrincipal(principal);
        } catch (GroupsException ge) {
            throw new AuthorizationException("Could not retrieve Groups for " + principal, ge);
        }

        while (i.hasNext()) {
            IEntityGroup group = (IEntityGroup) i.next();
            IAuthorizationPrincipal p = getPrincipalForGroup(group);
            al.add(p);
        }
        return al.iterator();
    }

    /**
     * Returns the <code>IPermissions</code> owner has granted for the specified activity and
     * target. Null parameters will be ignored, that is, all <code>IPermissions</code> matching the
     * non-null parameters are retrieved.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    public IPermission[] getPermissionsForOwner(String owner, String activity, String target)
            throws AuthorizationException {
        return primRetrievePermissions(owner, null, activity, target);
    }

    /**
     * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for the
     * specified activity and target. Null parameters will be ignored, that is, all <code>
     * IPermissions</code> matching the non-null parameters are retrieved. So, <code>
     * getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions
     * </code> for a <code>Principal</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param principal IAuthorizationPrincipal
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    @Override
    @RequestCache
    public IPermission[] getPermissionsForPrincipal(
            IAuthorizationPrincipal principal, String owner, String activity, String target)
            throws AuthorizationException {
        return primGetPermissionsForPrincipal(principal, owner, activity, target);
    }

    public IPermission[] getPermissionsForTarget(final String owner, final String target) {
        return getPermissionStore().select(owner, null, null, target, null);
    }

    /** @return org.apereo.portal.security.IPermissionStore */
    private IPermissionStore getPermissionStore() {
        return this.permissionStore;
    }

    /**
     * Returns <code>IAuthorizationPrincipal</code> associated with the <code>IPermission</code>.
     *
     * @return IAuthorizationPrincipal
     * @param permission IPermission
     */
    @Override
    public IAuthorizationPrincipal getPrincipal(IPermission permission)
            throws AuthorizationException {
        String principalString = permission.getPrincipal();
        int idx = principalString.indexOf(PRINCIPAL_SEPARATOR);
        Integer typeId = Integer.valueOf(principalString.substring(0, idx));
        Class type = EntityTypesLocator.getEntityTypes().getEntityTypeFromID(typeId);
        String key = principalString.substring(idx + 1);
        return newPrincipal(key, type);
    }

    /**
     * @param group
     * @return user org.apereo.portal.security.IAuthorizationPrincipal
     */
    private IAuthorizationPrincipal getPrincipalForGroup(IEntityGroup group) {
        String key = group.getKey();
        Class type = ICompositeGroupService.GROUP_ENTITY_TYPE;
        return newPrincipal(key, type);
    }

    /**
     * Returns <code>IAuthorizationPrincipals</code> associated with the <code>IPermission[]</code>.
     *
     * @return IAuthorizationPrincipal[]
     * @param permissions IPermission[]
     */
    private IAuthorizationPrincipal[] getPrincipalsFromPermissions(IPermission[] permissions)
            throws AuthorizationException {
        Set principals = new HashSet();
        for (int i = 0; i < permissions.length; i++) {
            IAuthorizationPrincipal principal = getPrincipal(permissions[i]);
            principals.add(principal);
        }
        return ((IAuthorizationPrincipal[])
                principals.toArray(new IAuthorizationPrincipal[principals.size()]));
    }

    /**
     * Returns the String used by an <code>IPermission</code> to represent an <code>
     * IAuthorizationPrincipal</code>.
     *
     * @param principal org.apereo.portal.security.IAuthorizationPrincipal
     */
    @Override
    public String getPrincipalString(IAuthorizationPrincipal principal) {
        return getPrincipalString(principal.getType(), principal.getKey());
    }

    private String getPrincipalString(Class pType, String pKey) {
        Integer type = EntityTypesLocator.getEntityTypes().getEntityIDFromType(pType);
        return type + PRINCIPAL_SEPARATOR + pKey;
    }

    /**
     * Returns the <code>IPermissions</code> owner has granted this <code>Principal</code> for the
     * specified activity and target. Null parameters will be ignored, that is, all <code>
     * IPermissions</code> matching the non-null parameters are retrieved. So, <code>
     * getPermissions(principal,null, null, null)</code> should retrieve all <code>IPermissions
     * </code> for a <code>Principal</code>. Ignore any cached <code>IPermissions</code>.
     *
     * @return org.apereo.portal.security.IPermission[]
     * @param principal IAuthorizationPrincipal
     * @param owner java.lang.String
     * @param activity java.lang.String
     * @param target java.lang.String
     * @exception AuthorizationException indicates authorization information could not be retrieved.
     */
    public IPermission[] getUncachedPermissionsForPrincipal(
            IAuthorizationPrincipal principal, String owner, String activity, String target)
            throws AuthorizationException {
        String pString = getPrincipalString(principal);
        return primRetrievePermissions(owner, pString, activity, target);
    }

    /**
     * Factory method for an <code>IPermission</code>.
     *
     * @param owner String
     * @param principal IAuthorizationPrincipal
     * @return org.apereo.portal.security.IPermission
     */
    @Override
    public IPermission newPermission(String owner, IAuthorizationPrincipal principal) {
        IPermission p = getPermissionStore().newInstance(owner);
        if (principal != null) {
            String pString = getPrincipalString(principal);
            p.setPrincipal(pString);
        }
        return p;
    }

    /**
     * Factory method for IPermissionManager.
     *
     * @return org.apereo.portal.security.IPermissionManager
     * @param owner java.lang.String
     */
    @Override
    public IPermissionManager newPermissionManager(String owner) {
        return new PermissionManagerImpl(owner, this);
    }

    /**
     * Factory method for IAuthorizationPrincipal. First check the principal cache, and if not
     * present, create the principal and cache it.
     *
     * @return org.apereo.portal.security.IAuthorizationPrincipal
     * @param key java.lang.String
     * @param type java.lang.Class
     */
    @Override
    public IAuthorizationPrincipal newPrincipal(String key, Class type) {
        final Tuple<String, Class> principalKey = new Tuple<>(key, type);
        final Element element = this.principalCache.get(principalKey);
        // principalCache is self populating, it can never return a null entry
        return (IAuthorizationPrincipal) element.getObjectValue();
    }

    /**
     * Converts an <code>IGroupMember</code> into an <code>IAuthorizationPrincipal</code>.
     *
     * @return org.apereo.portal.security.IAuthorizationPrincipal
     * @param groupMember org.apereo.portal.groups.IGroupMember
     */
    @Override
    public IAuthorizationPrincipal newPrincipal(IGroupMember groupMember) throws GroupsException {
        String key = groupMember.getKey();
        Class type = groupMember.getType();

        logger.debug("AuthorizationImpl.newPrincipal(): for {} ({})", type, key);

        return newPrincipal(key, type);
    }

    private IAuthorizationPrincipal primNewPrincipal(String key, Class type) {
        return new AuthorizationPrincipalImpl(key, type, this);
    }

    /**
     * Factory method for IUpdatingPermissionManager.
     *
     * @return org.apereo.portal.security.IUpdatingPermissionManager
     * @param owner java.lang.String
     */
    @Override
    public IUpdatingPermissionManager newUpdatingPermissionManager(String owner) {
        return new UpdatingPermissionManagerImpl(owner, this);
    }

    /**
     * Returns permissions for a principal. First check the entity caching service, and if the
     * permissions have not been cached, retrieve and cache them.
     *
     * @return IPermission[]
     * @param principal org.apereo.portal.security.IAuthorizationPrincipal
     */
    private IPermission[] primGetPermissionsForPrincipal(IAuthorizationPrincipal principal)
            throws AuthorizationException {
        if (!this.cachePermissions) {
            return getUncachedPermissionsForPrincipal(principal, null, null, null);
        }

        IPermissionSet ps = null;
        // Check the caching service for the Permissions first.
        ps = cacheGet(principal);

        if (ps == null)
            synchronized (principal) {
                ps = cacheGet(principal);
                if (ps == null) {
                    IPermission[] permissions =
                            getUncachedPermissionsForPrincipal(principal, null, null, null);
                    ps = new PermissionSetImpl(permissions, principal);
                    cacheAdd(ps);
                }
            } // end synchronized
        return ps.getPermissions();
    }

    /**
     * @return IPermission[]
     * @param principal org.apereo.portal.security.IAuthorizationPrincipal
     * @param owner String
     * @param activity String
     * @param target String
     */
    private IPermission[] primGetPermissionsForPrincipal(
            IAuthorizationPrincipal principal, String owner, String activity, String target)
            throws AuthorizationException {

        /*
         * Get a list of all permissions for the specified principal, then iterate
         * through them to build a list of the permissions matching the specified criteria.
         */

        IPermission[] perms = primGetPermissionsForPrincipal(principal);
        if (owner == null && activity == null && target == null) {
            return perms;
        }

        // If there are no permissions left, no need to look through group mappings.
        if (perms.length == 0) {
            return perms;
        }

        Set<String> containingGroups;

        if (target != null) {

            final Element element = this.entityParentsCache.get(target);
            if (element != null) {
                containingGroups = (Set<String>) element.getObjectValue();
            } else {
                containingGroups = new HashSet<>();

                // Ignore target entity lookups for the various synthetic ALL targets
                if (!IPermission.ALL_CATEGORIES_TARGET.equals(target)
                        && !IPermission.ALL_GROUPS_TARGET.equals(target)
                        && !IPermission.ALL_PORTLETS_TARGET.equals(target)
                        && !IPermission.ALL_TARGET.equals(target)) {

                    // UP-4410; It would be ideal if the target string indicated it was a group or
                    // entity that might be
                    // a member of a group so we could determine whether to check what groups the
                    // target entity might be
                    // contained within to see if the principal has permission to the containing
                    // group, but it does not
                    // (too significant to refactor database values at this point).  If the owner
                    // and activity strings map to
                    // a type of target that might be a group name or entity name, create a set of
                    // the groups the target
                    // entity is contained in.
                    boolean checkTargetForContainingGroups = true;
                    if (owner != null && activity != null) {
                        IPermissionActivity permissionActivity =
                                permissionOwner.getPermissionActivity(owner, activity);
                        if (nonEntityPermissionTargetProviders.contains(
                                permissionActivity.getTargetProviderKey())) {
                            checkTargetForContainingGroups = false;
                        }
                    }
                    if (checkTargetForContainingGroups) {
                        logger.debug(
                                "Target '{}' is an entity. Checking for group or groups containing entity",
                                target);

                        IGroupMember targetEntity = GroupService.findGroup(target);
                        if (targetEntity == null) {
                            if (target.startsWith(IPermission.PORTLET_PREFIX)) {
                                targetEntity =
                                        GroupService.getGroupMember(
                                                target.replace(IPermission.PORTLET_PREFIX, ""),
                                                IPortletDefinition.class);
                            } else {
                                targetEntity = GroupService.getGroupMember(target, IPerson.class);
                            }
                        }

                        if (targetEntity != null) {
                            for (IEntityGroup ancestor : targetEntity.getAncestorGroups()) {
                                containingGroups.add(ancestor.getKey());
                            }
                        }
                    }
                }

                this.entityParentsCache.put(new Element(target, containingGroups));
            }

        } else {
            containingGroups = new HashSet<>();
        }

        List<IPermission> al = new ArrayList<>(perms.length);

        for (int i = 0; i < perms.length; i++) {
            String permissionTarget = perms[i].getTarget();

            if (
            // owner matches
            (owner == null || owner.equals(perms[i].getOwner()))
                    &&
                    // activity matches
                    (activity == null || activity.equals(perms[i].getActivity()))
                    &&
                    // target matches or is a member of the current permission target
                    (target == null
                            || target.equals(permissionTarget)
                            || containingGroups.contains(permissionTarget))) {

                al.add(perms[i]);
            }
        }

        logger.trace(
                "AuthorizationImpl.primGetPermissionsForPrincipal(): "
                        + "Principal: {} owner: {} activity: {} target: {} : permissions retrieved: {}",
                principal,
                owner,
                activity,
                target,
                al);
        logger.debug(
                "AuthorizationImpl.primGetPermissionsForPrincipal(): "
                        + "Principal: {} owner: {} activity: {} target: {} : number of permissions retrieved: {}",
                principal,
                owner,
                activity,
                target,
                al.size());

        return ((IPermission[]) al.toArray(new IPermission[al.size()]));
    }

    /**
     * @return IPermission[]
     * @param owner String
     * @param principal String
     * @param activity String
     * @param target String
     */
    private IPermission[] primRetrievePermissions(
            String owner, String principal, String activity, String target)
            throws AuthorizationException {
        return getPermissionStore().select(owner, principal, activity, target, null);
    }

    /**
     * Removes <code>IPermissions</code> for the <code>IAuthorizationPrincipals</code> from the
     * cache.
     *
     * @param principals IAuthorizationPrincipal[]
     */
    private void removeFromPermissionsCache(IAuthorizationPrincipal[] principals)
            throws AuthorizationException {
        for (int i = 0; i < principals.length; i++) {
            cacheRemove(principals[i]);
        }
    }

    /**
     * Removes <code>IPermissions</code> from the cache.
     *
     * @param permissions IPermission[]
     */
    private void removeFromPermissionsCache(IPermission[] permissions)
            throws AuthorizationException {
        IAuthorizationPrincipal[] principals = getPrincipalsFromPermissions(permissions);
        removeFromPermissionsCache(principals);
    }

    /**
     * Removes <code>IPermissions</code> from the back end store.
     *
     * @param permissions IPermission[]
     * @exception AuthorizationException
     */
    @Override
    public void removePermissions(IPermission[] permissions) throws AuthorizationException {
        if (permissions.length > 0) {
            getPermissionStore().delete(permissions);
            if (this.cachePermissions) {
                removeFromPermissionsCache(permissions);
            }
        }
    }

    /**
     * Updates <code>IPermissions</code> in the back end store.
     *
     * @param permissions IPermission[]
     * @exception AuthorizationException
     */
    @Override
    public void updatePermissions(IPermission[] permissions) throws AuthorizationException {
        if (permissions.length > 0) {
            getPermissionStore().update(permissions);
            if (this.cachePermissions) {
                removeFromPermissionsCache(permissions);
            }
        }
    }
}
