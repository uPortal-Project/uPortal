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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apereo.portal.AuthorizationException;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.permission.IPermissionActivity;
import org.apereo.portal.permission.IPermissionOwner;
import org.apereo.portal.permission.dao.IPermissionOwnerDao;
import org.apereo.portal.permission.target.IPermissionTarget;
import org.apereo.portal.permission.target.IPermissionTargetProviderRegistry;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPermissionPolicy;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.utils.cache.UsernameTaggedCacheEntryPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * If there exists a GRANT explicitly for the Principal for the Activity under consideration, this
 * permission policy will GRANT permission.
 *
 * <p>If there exists a GRANT for a group containing the Principal for the Activity under
 * consideration, and there is a path up the groups tree from the Principal to that GRANTed group
 * that is not interrupted by a Deny for an intervening group in the tree, then this permission
 * policy will GRANT permission.
 *
 * <p>Otherwise, this permission policy will DENY permission.
 *
 * <p>Examples: Principal (GRANT) -- Small group -- Bigger group -- Huge group Results in GRANT
 * because the Principal has an explicit permission.
 *
 * <p>Principal -- Small group -- Bigger group (GRANT) -- Huge group Results in GRANT because there
 * is an unblocked path to a containing group with GRANT.
 *
 * <p>Principal -- Small group (DENY) -- Bigger group (GRANT) -- Huge group Results in DENY because
 * there is no unblocked path to a grant -- the "Bigger group"'s GRANT does not apply because of the
 * intervening DENY.
 *
 * <p>Principal -- Small group (DENY) -- Bigger group -- Huge group Principal -- Some other group --
 * Bigger other group (GRANT) -- Huge group Results in GRANT because there is an unblocked path to a
 * GRANT.
 */
@Service("anyUnblockedGrantPermissionPolicy")
public class AnyUnblockedGrantPermissionPolicy implements IPermissionPolicy {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired private IPermissionOwnerDao permissionOwnerDao;

    @Autowired private IPermissionTargetProviderRegistry targetProviderRegistry;

    @Autowired
    @Qualifier(
            value =
                    "org.apereo.portal.security.provider.AnyUnblockedGrantPermissionPolicy.HAS_UNBLOCKED_GRANT")
    private Cache hasUnblockedGrantCache;

    @Override
    public boolean doesPrincipalHavePermission(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            IPermissionOwner owner,
            IPermissionActivity activity,
            IPermissionTarget target)
            throws AuthorizationException {

        /*
         * The API states that the service, owner, and activity arguments must
         * not be null. If for some reason they are null, log and fail closed.
         * In our case, the principal and target must also be non-null.
         */
        if (service == null
                || principal == null
                || owner == null
                || activity == null
                || target == null) {
            log.error(
                    "Null argument to AnyUnblockedGrantPermissionPolicy doesPrincipalHavePermission() method "
                            + "should not be possible.  This is indicative of a potentially serious bug in the permissions "
                            + "and authorization infrastructure;  service='{}', principal='{}', owner='{}', activity='{}', "
                            + "target='{}'",
                    service,
                    principal,
                    owner,
                    activity,
                    target,
                    new AuthorizationException("Null argument"));
            // fail closed
            return false;
        }

        // Is this user a super-user?  (Should this logic be moved to AuthorizationImpl?)
        final IPermissionActivity allPermissionsActivity =
                permissionOwnerDao.getPermissionActivity(
                        IPermission.PORTAL_SYSTEM, IPermission.ALL_PERMISSIONS_ACTIVITY);
        if (!activity.equals(
                allPermissionsActivity)) { // NOTE:  Must check to avoid infinite recursion
            final IPermissionOwner allPermissionsOwner =
                    permissionOwnerDao.getPermissionOwner(IPermission.PORTAL_SYSTEM);
            final IPermissionTarget allPermissionsTarget =
                    targetProviderRegistry
                            .getTargetProvider(allPermissionsActivity.getTargetProviderKey())
                            .getTarget(IPermission.ALL_TARGET);
            if (doesPrincipalHavePermission(
                    service,
                    principal,
                    allPermissionsOwner,
                    allPermissionsActivity,
                    allPermissionsTarget)) {
                // Stop checking;  just return true
                return true;
            }
        }

        /*
         * uPortal uses a few "special" targets that signal permission to
         * perform the specified activity over an entire class of targets;
         * see if one of those applies in this case.
         */
        IPermissionTarget collectiveTarget =
                null; // The "collective noun" representing a class of thing
        switch (target.getTargetType()) {
            case PORTLET:
                collectiveTarget =
                        targetProviderRegistry
                                .getTargetProvider(activity.getTargetProviderKey())
                                .getTarget(IPermission.ALL_PORTLETS_TARGET);
                break;
            case CATEGORY:
                collectiveTarget =
                        targetProviderRegistry
                                .getTargetProvider(activity.getTargetProviderKey())
                                .getTarget(IPermission.ALL_CATEGORIES_TARGET);
                break;
            case GROUP:
                collectiveTarget =
                        targetProviderRegistry
                                .getTargetProvider(activity.getTargetProviderKey())
                                .getTarget(IPermission.ALL_GROUPS_TARGET);
                break;
            default:
                // This sort of handling does not apply;  just pass through
        }
        /*
         * NOTE:  Cannot generalize to a collective target if we are already on
         * the collective target, else StackOverflowError.
         */
        if (collectiveTarget != null && !collectiveTarget.equals(target)) {
            if (doesPrincipalHavePermission(
                    service, principal, owner, activity, collectiveTarget)) {
                /*
                 * There is a collective for this class of target,
                 * and the user DOES have this special permission
                 */
                return true;
            }
        }

        // Search ourselves and all ancestors for an unblocked GRANT.
        boolean result;
        try {
            // Track groups we've already explored to avoid infinite loop
            final Set<IGroupMember> seenGroups = new HashSet<>();
            result =
                    hasUnblockedPathToGrantWithCache(
                            service, principal, owner, activity, target, seenGroups);
        } catch (Exception e) {
            log.error(
                    "Error searching for unblocked path to grant for principal [" + principal + "]",
                    e);
            // fail closed
            return false;
        }

        if (log.isTraceEnabled()) {
            if (result) {
                log.trace(
                        "Principal '{}' is granted permission to perform activity "
                                + "'{}' on target '{}' under permission owning system '{}' "
                                + "because this principal has an unblocked path to a GRANT.",
                        principal,
                        activity.getFname(),
                        target.getKey(),
                        owner.getFname());
            } else {
                log.trace(
                        "Principal '{}' is denied permission to perform activity '{}' "
                                + "on target '{}' under permission owning system '{}' because this "
                                + "principal does not have an unblocked path to a GRANT.",
                        principal,
                        activity.getFname(),
                        target.getKey(),
                        owner.getFname());
            }
        }

        return result;
    }

    private boolean hasUnblockedPathToGrantWithCache(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            IPermissionOwner owner,
            IPermissionActivity activity,
            IPermissionTarget target,
            Set<IGroupMember> seenGroups)
            throws GroupsException {

        final CacheKey cacheKey = getCacheKey(principal, owner, activity, target);
        Element element = hasUnblockedGrantCache.get(cacheKey);
        if (element == null) {
            final boolean answer =
                    hasUnblockedPathToGrant(
                            service, principal, owner, activity, target, seenGroups);
            element = new Element(cacheKey, answer);
            hasUnblockedGrantCache.put(element);
        }
        return (Boolean) element.getObjectValue();
    }

    /**
     * This method performs the actual, low-level checking of a single activity and target. Is IS
     * responsible for performing the same check for affiliated groups in the Groups hierarchy, but
     * it is NOT responsible for understanding the nuances of relationships some activities and/or
     * targets have with one another (e.g. MANAGE_APPROVED, ALL_PORTLETS, etc.). It performs the
     * following steps, in order:
     *
     * <ol>
     *   <li>Find out if the specified principal is <em>specifically</em> granted or denied; if an
     *       answer is found in this step, return it
     *   <li>Find out what groups this principal belongs to; convert each one to a principal and
     *       seek an answer by invoking ourselves recursively; if an answer is found in this step,
     *       return it
     *   <li>Return false (no explicit GRANT means no permission)
     * </ol>
     */
    private boolean hasUnblockedPathToGrant(
            IAuthorizationService service,
            IAuthorizationPrincipal principal,
            IPermissionOwner owner,
            IPermissionActivity activity,
            IPermissionTarget target,
            Set<IGroupMember> seenGroups)
            throws GroupsException {

        if (log.isTraceEnabled()) {
            log.trace(
                    "Searching for unblocked path to GRANT for principal '{}' to "
                            + "'{}' on target '{}' having already checked:  {}",
                    principal.getKey(),
                    activity.getFname(),
                    target.getKey(),
                    seenGroups);
        }

        /*
         * Step #1:  Specific GRANT/DENY attached to this principal
         */
        final IPermission[] permissions =
                service.getPermissionsForPrincipal(
                        principal, owner.getFname(), activity.getFname(), target.getKey());

        final Set<IPermission> activePermissions = removeInactivePermissions(permissions);
        final boolean denyExists =
                containsType(activePermissions, IPermission.PERMISSION_TYPE_DENY);
        if (denyExists) {
            // We need go no further;  DENY trumps both GRANT & inherited permissions
            return false;
        }
        final boolean grantExists =
                containsType(activePermissions, IPermission.PERMISSION_TYPE_GRANT);
        if (grantExists) {
            // We need go no further;  explicit GRANT at this level of the hierarchy
            if (log.isTraceEnabled()) {
                log.trace(
                        "Found unblocked path to this permission set including a GRANT:  {}",
                        activePermissions);
            }
            return true;
        }

        /*
         * Step #2:  Seek an answer from affiliated groups
         */
        IGroupMember principalAsGroupMember = service.getGroupMember(principal);
        if (seenGroups.contains(principalAsGroupMember)) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "Declining to re-examine principal '{}' for permission to '{}' "
                                + "on '{}' because this group is among already checked groups:  {}",
                        principal.getKey(),
                        activity.getFname(),
                        target.getKey(),
                        seenGroups);
            }
            return false;
        }
        seenGroups.add(principalAsGroupMember);
        Set<IEntityGroup> immediatelyContainingGroups = principalAsGroupMember.getParentGroups();
        for (IGroupMember parentGroup : immediatelyContainingGroups) {
            try {
                if (parentGroup != null) {
                    IAuthorizationPrincipal parentPrincipal = service.newPrincipal(parentGroup);
                    boolean parentHasUnblockedPathToGrant =
                            hasUnblockedPathToGrantWithCache(
                                    service, parentPrincipal, owner, activity, target, seenGroups);
                    if (parentHasUnblockedPathToGrant) {
                        return true;
                    }
                    // Parent didn't have a path to grant, fall through and try another parent (if
                    // any)
                }
            } catch (Exception e) {
                // problem evaluating this path, but let's not let it stop
                // us from exploring other paths.  Though a portion of the
                // group structure is broken, permission may be granted by
                // an unbroken portion
                log.error("Error evaluating permissions of parent group [" + parentGroup + "]", e);
            }
        }

        /*
         * Step #3:  No explicit GRANT means no permission
         */
        return false;
    }

    /**
     * Returns a Set containing those IPermission instances where the present date is neither after
     * the permission expiration if present nor before the permission start date if present. Only
     * permissions objects that have been filtered by this method should be checked.
     *
     * @return Potentially empty non-null Set of active permissions.
     */
    private Set<IPermission> removeInactivePermissions(final IPermission[] perms) {
        Date now = new Date();

        Set<IPermission> result = new HashSet<>(1);

        for (int i = 0; i < perms.length; i++) {
            IPermission p = perms[i];

            if ((p.getEffective() == null || !p.getEffective().after(now))
                    && (p.getExpires() == null || p.getExpires().after(now))) {
                result.add(p);
            }
        }

        return result;
    }

    /**
     * Returns true if a set of IPermission instances contains a permission of the specified type,
     * otherwise false. Permissions passed to this method <em>must</em> already be filtered of
     * inactive (expired) instances.
     *
     * @return True if the set contains a permission of the sought type, false otherwise
     * @throws IllegalArgumentException if input set or type is null.
     */
    private boolean containsType(final Set<IPermission> permissions, final String soughtType) {

        // Assertions
        if (permissions == null) {
            throw new IllegalArgumentException("Cannot check null set for contents.");
        }
        if (soughtType == null) {
            throw new IllegalArgumentException("Cannot search for type null.");
        }

        boolean result = false; // default

        for (IPermission p : permissions) {
            if (soughtType.equals(p.getType())) {
                result = true;
            }
        }

        return result;
    }

    protected CacheKey getCacheKey(
            IAuthorizationPrincipal principal,
            IPermissionOwner owner,
            IPermissionActivity activity,
            IPermissionTarget target) {

        CacheKey result;
        if (principal.isGroup()) {
            /*
             * Untagged keys for groups...
             */
            result =
                    CacheKey.build(
                            AnyUnblockedGrantPermissionPolicy.class.getName(),
                            principal.getPrincipalString(),
                            owner.getFname(),
                            activity.getFname(),
                            target.getKey());
        } else {
            /*
             * Keys tagged with the username for users;  this practice
             * will cause them to be purged if the user re-authenticates.
             */
            result =
                    CacheKey.buildTagged(
                            AnyUnblockedGrantPermissionPolicy.class.getName(),
                            UsernameTaggedCacheEntryPurger.createCacheEntryTag(principal.getKey()),
                            principal.getPrincipalString(),
                            owner.getFname(),
                            activity.getFname(),
                            target.getKey());
        }

        return result;
    }
}
