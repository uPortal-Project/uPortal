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
package org.apereo.portal.groups;

import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.concurrency.CachingException;
import org.apereo.portal.concurrency.IEntityLock;
import org.apereo.portal.concurrency.LockingException;
import org.apereo.portal.services.EntityCachingService;
import org.apereo.portal.services.EntityLockService;
import org.apereo.portal.services.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Reference individual, or leaf, group service. */
public class ReferenceIndividualGroupService extends ReferenceCompositeGroupService
        implements IIndividualGroupService, ILockableGroupService {

    private static final Logger log =
            LoggerFactory.getLogger(ReferenceIndividualGroupService.class);

    // Describes the attributes of this service.  See compositeGroupServices.xml.
    protected ComponentGroupServiceDescriptor serviceDescriptor;

    protected IEntityGroupStore groupFactory;

    // Entity searcher
    protected IEntitySearcher entitySearcher;

    /** ReferenceGroupsService constructor. */
    public ReferenceIndividualGroupService() throws GroupsException {
        this(new ComponentGroupServiceDescriptor());
    }

    /** ReferenceGroupsService constructor. */
    public ReferenceIndividualGroupService(ComponentGroupServiceDescriptor svcDescriptor)
            throws GroupsException {
        super();
        serviceDescriptor = svcDescriptor;
        initialize();
    }

    /** Answers if <code>IGroupMembers</code> are being cached. */
    protected boolean cacheInUse() {
        return getServiceDescriptor().isCachingEnabled();
    }

    /**
     * Removes the <code>IEntityGroup</code> from the cache and the store.
     *
     * @param group IEntityGroup
     */
    @Override
    public void deleteGroup(IEntityGroup group) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        synchronizeGroupMembersOnDelete(group);
        getGroupStore().delete(group);
        if (cacheInUse()) {
            cacheRemove(group);
        }
    }

    /**
     * Removes the <code>ILockableEntityGroup</code> from its containing groups. The <code>finally
     * </code> block tries to release any groups that are still locked, which can occur if an
     * attempt to remove the group from one of its containing groups fails and throws a
     * GroupsException. In this event, we do not try to roll back any successful removes, since that
     * would probably fail anyway.
     *
     * @param group ILockableEntityGroup
     */
    private void removeDeletedGroupFromParentGroups(ILockableEntityGroup group)
            throws GroupsException {

        //    IEntityLock lock = null;
        List<ILockableEntityGroup> lockableGroups = new ArrayList<>();
        try {
            String lockOwner = group.getLock().getLockOwner();
            for (IEntityGroup containingGroup : group.getParentGroups()) {
                ILockableEntityGroup lockableGroup =
                        GroupService.findLockableGroup(containingGroup.getKey(), lockOwner);
                if (lockableGroup != null) {
                    lockableGroups.add(lockableGroup);
                }
            }
            for (ILockableEntityGroup lockableGroup : lockableGroups) {
                lockableGroup.removeChild(group);
                lockableGroup.updateMembers();
            }
        } catch (GroupsException ge) {
            throw new GroupsException(
                    "Could not remove deleted group " + group.getKey() + " from parent", ge);
        } finally {
            for (ILockableEntityGroup lockableGroup : lockableGroups) {
                IEntityLock lock = lockableGroup.getLock();
                try {
                    if (lock.isValid()) {
                        lock.release();
                    }
                } catch (LockingException le) {
                    log.error(
                            "ReferenceIndividualGroupService.removeDeletedGroupFromParentGroups(): "
                                    + "Problem unlocking parent group",
                            le);
                }
            }
        }
    }

    /**
     * Removes the <code>ILockableEntityGroup</code> from the cache and the store, including both
     * parent and child memberships.
     *
     * @param group ILockableEntityGroup
     */
    @Override
    public void deleteGroup(ILockableEntityGroup group) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        try {
            if (group.getLock().isValid()) {
                removeDeletedGroupFromParentGroups(group);
                deleteGroup((IEntityGroup) group);
            } else {
                throw new GroupsException(
                        "Could not delete group " + group.getKey() + " has invalid lock.");
            }
        } catch (LockingException le) {
            throw new GroupsException("Could not delete group " + group.getKey(), le);
        } finally {
            try {
                group.getLock().release();
            } catch (LockingException le) {
            }
        }
    }

    private EntityIdentifier[] filterEntities(EntityIdentifier[] entities, IEntityGroup ancestor)
            throws GroupsException {
        ArrayList ar = new ArrayList(entities.length);
        for (int i = 0; i < entities.length; i++) {
            IGroupMember gm = this.getGroupMember(entities[i]);
            if (ancestor.deepContains(gm)) {
                ar.add(entities[i]);
            }
        }
        return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
    }

    /**
     * Returns and caches the containing groups for the <code>IGroupMember</code>
     *
     * @param gm IGroupMember
     */
    @Override
    public Iterator findParentGroups(IGroupMember gm) throws GroupsException {
        log.debug("Finding containing groups for member {}", gm.getKey());
        Collection groups = new ArrayList(10);
        IEntityGroup group = null;
        Iterator it = getGroupStore().findParentGroups(gm);
        while (it.hasNext()) {
            group = (IEntityGroup) it.next();
            if (group == null) {
                log.error("null parent for group {}", gm.getKey());
            } else {
                group.setLocalGroupService(this);
                groups.add(group);
                if (cacheInUse()) {
                    try {
                        if (getGroupFromCache(group.getEntityIdentifier().getKey()) == null) {
                            cacheAdd(group);
                        }
                    } catch (CachingException ce) {
                        throw new GroupsException("Problem finding containing groups", ce);
                    }
                }
            }
        }
        return groups.iterator();
    }

    /** Returns a pre-existing <code>IEntityGroup</code> or null if it does not exist. */
    @Override
    public IEntityGroup findGroup(String key) throws GroupsException {
        return findGroup(newCompositeEntityIdentifier(key));
    }

    /** Returns a pre-existing <code>IEntityGroup</code> or null if it does not exist. */
    @Override
    public IEntityGroup findGroup(CompositeEntityIdentifier ent) throws GroupsException {
        return (cacheInUse()) ? findGroupWithCache(ent) : primFindGroup(ent.getLocalKey());
    }

    /** Returns a pre-existing <code>IEntityGroup</code> or null if it does not exist. */
    protected IEntityGroup findGroupWithCache(CompositeEntityIdentifier ent)
            throws GroupsException {
        try {
            IEntityGroup group = getGroupFromCache(ent.getKey());
            if (group == null) {
                group = primFindGroup(ent.getLocalKey());
                if (group != null) {
                    cacheAdd(group);
                }
            }
            return group;
        } catch (CachingException ce) {
            throw new GroupsException("Problem retrieving group " + ent.getKey(), ce);
        }
    }

    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the group is not found.
     */
    @Override
    public ILockableEntityGroup findGroupWithLock(String key, String owner) throws GroupsException {
        return findGroupWithLock(key, owner, 0);
    }

    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the group is not found.
     */
    @Override
    public ILockableEntityGroup findGroupWithLock(String key, String owner, int secs)
            throws GroupsException {

        throwExceptionIfNotInternallyManaged();

        Class groupType = ICompositeGroupService.GROUP_ENTITY_TYPE;
        try {
            IEntityLock lock =
                    (secs == 0)
                            ? EntityLockService.instance().newWriteLock(groupType, key, owner)
                            : EntityLockService.instance()
                                    .newWriteLock(groupType, key, owner, secs);

            ILockableEntityGroup group = groupFactory.findLockable(key);
            if (group == null) {
                lock.release();
            } else {
                group.setLock(lock);
                group.setLocalGroupService(this);
            }

            return group;
        } catch (LockingException le) {
            throw new GroupsException("Problem getting lock for group " + key, le);
        }
    }

    /**
     * Returns and caches the member groups for the <code>IEntityGroup</code>
     *
     * @param eg IEntityGroup
     */
    protected Iterator findLocalMemberGroups(IEntityGroup eg) throws GroupsException {
        Collection groups = new ArrayList(10);
        IEntityGroup group = null;
        Iterator it = getGroupStore().findMemberGroups(eg);
        while (it.hasNext()) {
            group = (IEntityGroup) it.next();
            if (group == null) {
                log.warn(
                        "A null IEntityGroup object was part of a list groupStore.findMemberGroups");
                continue;
            }
            group.setLocalGroupService(this);
            groups.add(group);
            if (cacheInUse()) {
                try {
                    if (getGroupFromCache(group.getEntityIdentifier().getKey()) == null) {
                        cacheAdd(group);
                    }
                } catch (CachingException ce) {
                    throw new GroupsException("Problem finding member groups", ce);
                }
            }
        }
        return groups.iterator();
    }

    /** Finds the <code>IEntities</code> that are members of <code>group</code>. */
    public Iterator findMemberEntities(IEntityGroup group) throws GroupsException {
        return getGroupStore().findEntitiesForGroup(group);
    }

    /**
     * Returns member groups for the <code>IEntityGroup</code>. First get the member groups that are
     * local to this service. Then retrieve the keys of all of the member groups and ask the
     * GroupService to find the groups we do not yet have.
     *
     * @param eg IEntityGroup
     */
    @Override
    public Iterator findMemberGroups(IEntityGroup eg) throws GroupsException {
        Map groups = new HashMap();
        IEntityGroup group = null;
        Iterator itr = findLocalMemberGroups(eg);
        while (itr.hasNext()) {
            group = (IEntityGroup) itr.next();
            groups.put(group.getKey(), group);
        }

        String[] memberGroupKeys = getGroupStore().findMemberGroupKeys(eg);
        for (int i = 0; i < memberGroupKeys.length; i++) {
            if (!groups.containsKey(memberGroupKeys[i])) {
                group = GroupService.findGroup(memberGroupKeys[i]);
                if (group != null) {
                    groups.put(group.getKey(), group);
                }
            }
        }
        return groups.values().iterator();
    }

    /**
     * Returns and members for the <code>IEntityGroup</code>.
     *
     * @param eg IEntityGroup
     */
    @Override
    public Iterator findMembers(IEntityGroup eg) throws GroupsException {
        Collection members = new ArrayList(10);
        Iterator it = null;

        for (it = findMemberGroups(eg); it.hasNext(); ) {
            members.add(it.next());
        }
        for (it = findMemberEntities(eg); it.hasNext(); ) {
            members.add(it.next());
        }

        return members.iterator();
    }

    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does not guarantee that
     * the underlying entity actually exists.
     */
    @Override
    public IEntity getEntity(String key, Class type) throws GroupsException {
        IEntity ent = primGetEntity(key, type);

        if (cacheInUse()) {
            try {
                IEntity cachedEnt = getEntityFromCache(ent.getEntityIdentifier().getKey());
                if (cachedEnt == null) {
                    cacheAdd(ent);
                } else {
                    ent = cachedEnt;
                }
            } catch (CachingException ce) {
                throw new GroupsException(
                        "Problem retrieving group member " + type + "(" + key + ")", ce);
            }
        }
        return ent;
    }

    /** Returns a cached <code>IEntityGroup</code> or null if it has not been cached. */
    protected IEntityGroup getGroupFromCache(String key) throws CachingException {
        return (IEntityGroup)
                EntityCachingService.instance().get(ICompositeGroupService.GROUP_ENTITY_TYPE, key);
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a portal entity. If the
     * parm <code>type</code> is the group type, the <code>IGroupMember</code> is an <code>
     * IEntityGroup</code> else it is an <code>IEntity</code>.
     */
    @Override
    public IGroupMember getGroupMember(String key, Class type) throws GroupsException {
        IGroupMember gm = null;
        if (type == ICompositeGroupService.GROUP_ENTITY_TYPE) gm = findGroup(key);
        else gm = getEntity(key, type);
        return gm;
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a portal entity, based on
     * the <code>EntityIdentifier</code>, which refers to the UNDERLYING entity for the <code>
     * IGroupMember</code>.
     */
    @Override
    public IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
            throws GroupsException {
        return getGroupMember(
                underlyingEntityIdentifier.getKey(), underlyingEntityIdentifier.getType());
    }

    /**
     * Returns the implementation of <code>IEntityGroupStore</code> whose class name was retrieved
     * by the PropertiesManager (see initialize()).
     */
    @Override
    public IEntityGroupStore getGroupStore() throws GroupsException {
        return groupFactory;
    }

    protected ComponentGroupServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    /** @exception GroupsException */
    private void initialize() throws GroupsException {
        String eMsg = null;
        String svcName = getServiceDescriptor().getName();
        if (log.isDebugEnabled()) log.debug("Service descriptor attributes: " + svcName);

        // print service descriptor attributes:
        for (String descriptorKey : getServiceDescriptor().keySet()) {
            Object descriptorValue = getServiceDescriptor().get(descriptorKey);
            if (descriptorValue != null) {
                if (log.isDebugEnabled()) log.debug("  " + descriptorKey + " : " + descriptorValue);
            }
        }

        String groupStoreFactoryName = getServiceDescriptor().getGroupStoreFactoryName();
        String entityStoreFactoryName = getServiceDescriptor().getEntityStoreFactoryName();
        String entitySearcherFactoryName = getServiceDescriptor().getEntitySearcherFactoryName();

        if (groupStoreFactoryName == null) {
            if (log.isInfoEnabled()) {
                log.info(
                        "ReferenceGroupService.initialize(): ("
                                + svcName
                                + ") No Group Store factory specified in service descriptor.");
            }
        } else {
            try {
                IEntityGroupStoreFactory groupStoreFactory =
                        (IEntityGroupStoreFactory)
                                Class.forName(groupStoreFactoryName).newInstance();
                groupFactory = groupStoreFactory.newGroupStore(getServiceDescriptor());
            } catch (Exception e) {
                eMsg =
                        "ReferenceIndividualGroupService.initialize(): Failed to instantiate group store ("
                                + svcName
                                + "): "
                                + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }

        if (entityStoreFactoryName == null) {
            if (log.isInfoEnabled())
                log.info(
                        "ReferenceIndividualGroupService.initialize(): "
                                + "No Entity Store Factory specified in service descriptor ("
                                + svcName
                                + ")");
        } else {
            try {
                IEntityStoreFactory entityStoreFactory =
                        (IEntityStoreFactory) Class.forName(entityStoreFactoryName).newInstance();
                entityFactory = entityStoreFactory.newEntityStore();
            } catch (Exception e) {
                eMsg =
                        "ReferenceIndividualGroupService.initialize(): Failed to instantiate entity store "
                                + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }

        if (entitySearcherFactoryName == null) {
            if (log.isInfoEnabled())
                log.info(
                        "ReferenceIndividualGroupService.initialize(): "
                                + "No Entity Searcher Factory specified in service descriptor.");
        } else {
            try {
                IEntitySearcherFactory entitySearcherFactory =
                        (IEntitySearcherFactory)
                                Class.forName(entitySearcherFactoryName).newInstance();
                entitySearcher = entitySearcherFactory.newEntitySearcher();
            } catch (Exception e) {
                eMsg =
                        "ReferenceIndividualGroupService.initialize(): Failed to instantiate entity searcher "
                                + e;
                log.error(eMsg);
                throw new GroupsException(eMsg, e);
            }
        }
    }

    /** Answers if the group can be updated or deleted in the store. */
    @Override
    public boolean isEditable(IEntityGroup group) throws GroupsException {
        return isInternallyManaged();
    }

    /** Answers if this service is managed by the portal and is therefore updatable. */
    protected boolean isInternallyManaged() {
        return getServiceDescriptor().isInternallyManaged();
    }

    /**
     * Answers if this service is a leaf in the composite; a service that actually operates on
     * groups.
     */
    @Override
    public boolean isLeafService() {
        return true;
    }

    /** Answers if this service is updateable by the portal. */
    @Override
    public boolean isEditable() {
        return isInternallyManaged();
    }

    /** Returns a new <code>IEntityGroup</code> for the given Class with an unused key. */
    @Override
    public IEntityGroup newGroup(Class type) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        IEntityGroup group = groupFactory.newInstance(type);
        group.setLocalGroupService(this);
        if (cacheInUse()) {
            cacheAdd(group);
        }
        return group;
    }

    /** Returns a pre-existing <code>IEntityGroup</code> or null if it does not exist. */
    @Override
    protected IEntityGroup primFindGroup(String localKey) throws GroupsException {
        IEntityGroup group = groupFactory.find(localKey);
        if (group != null) {
            group.setLocalGroupService(this);
        }
        return group;
    }

    private EntityIdentifier[] removeDuplicates(EntityIdentifier[] entities) {
        ArrayList ar = new ArrayList(entities.length);
        for (int i = 0; i < entities.length; i++) {
            if (!ar.contains(entities[i])) {
                ar.add(entities[i]);
            }
        }
        return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
    }

    @Override
    public EntityIdentifier[] searchForEntities(
            String query, IGroupConstants.SearchMethod method, Class type) throws GroupsException {
        return removeDuplicates(entitySearcher.searchForEntities(query, method, type));
    }

    @Override
    public EntityIdentifier[] searchForEntities(
            String query, IGroupConstants.SearchMethod method, Class type, IEntityGroup ancestor)
            throws GroupsException {
        return filterEntities(searchForEntities(query, method, type), ancestor);
    }

    @Override
    public EntityIdentifier[] searchForGroups(
            String query, IGroupConstants.SearchMethod method, Class leaftype)
            throws GroupsException {
        return removeDuplicates(groupFactory.searchForGroups(query, method, leaftype));
    }

    @Override
    public EntityIdentifier[] searchForGroups(
            String query,
            IGroupConstants.SearchMethod method,
            Class leaftype,
            IEntityGroup ancestor)
            throws GroupsException {
        return filterEntities(searchForGroups(query, method, leaftype), ancestor);
    }

    protected void throwExceptionIfNotInternallyManaged() throws GroupsException {
        if (!isInternallyManaged()) {
            throw new GroupsException("Group Service " + getServiceName() + " is not updatable.");
        }
    }

    /**
     * Update the store and the updated members.
     *
     * @param group IEntityGroup
     */
    @Override
    public void updateGroup(IEntityGroup group) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        getGroupStore().update(group);
        if (cacheInUse()) {
            cacheUpdate(group);
        }
        synchronizeGroupMembersOnUpdate(group);
    }

    /**
     * Updates the <code>ILockableEntityGroup</code> in the store and removes it from the cache.
     *
     * @param group ILockableEntityGroup
     */
    @Override
    public void updateGroup(ILockableEntityGroup group, boolean renewLock) throws GroupsException {
        throwExceptionIfNotInternallyManaged();

        try {
            if (!group.getLock().isValid()) {
                throw new GroupsException(
                        "Could not update group " + group.getKey() + " has invalid lock.");
            }

            //      updateGroup((IEntityGroup)group);
            getGroupStore().update(group);
            if (cacheInUse()) {
                cacheRemove(group);
            }
            synchronizeGroupMembersOnUpdate(group);

            if (renewLock) {
                group.getLock().renew();
            } else {
                group.getLock().release();
            }

        } catch (LockingException le) {
            throw new GroupsException("Problem updating group " + group.getKey(), le);
        }
    }

    /**
     * Update the store and the updated members.
     *
     * @param group IEntityGroup
     */
    @Override
    public void updateGroupMembers(IEntityGroup group) throws GroupsException {
        throwExceptionIfNotInternallyManaged();
        getGroupStore().updateMembers(group);
        if (cacheInUse()) {
            cacheUpdate(group);
        }
        synchronizeGroupMembersOnUpdate(group);
    }

    /**
     * Updates the <code>ILockableEntityGroup</code> in the store and removes it from the cache.
     *
     * @param group ILockableEntityGroup
     */
    @Override
    public void updateGroupMembers(ILockableEntityGroup group, boolean renewLock)
            throws GroupsException {
        throwExceptionIfNotInternallyManaged();

        try {
            if (!group.getLock().isValid()) {
                throw new GroupsException(
                        "Could not update group " + group.getKey() + " has invalid lock.");
            }

            getGroupStore().updateMembers(group);
            if (cacheInUse()) {
                cacheRemove(group);
            }
            synchronizeGroupMembersOnUpdate(group);

            if (renewLock) {
                group.getLock().renew();
            } else {
                group.getLock().release();
            }

        } catch (LockingException le) {
            throw new GroupsException("Problem updating group " + group.getKey(), le);
        }
    }

    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does not guarantee that
     * the underlying entity actually exists.
     */
    protected IEntity primGetEntity(String key, Class type) throws GroupsException {
        return entityFactory.newInstance(key, type);
    }

    /**
     * Remove the back pointers of the group members of the deleted group. Then update the cache to
     * invalidate copies on peer servers.
     *
     * @param group ILockableEntityGroup
     */
    protected void synchronizeGroupMembersOnDelete(IEntityGroup group) throws GroupsException {
        GroupMemberImpl gmi = null;

        for (IGroupMember iGroupMember : group.getChildren()) {
            gmi = (GroupMemberImpl) iGroupMember;
            gmi.invalidateInParentGroupsCache(Collections.singleton((IGroupMember) gmi));
            if (cacheInUse()) {
                cacheUpdate(gmi);
            }
        }
    }

    /**
     * Adjust the back pointers of the updated group members to either add or remove the parent
     * group. Then update the cache to invalidate copies on peer servers.
     *
     * @param group ILockableEntityGroup
     */
    protected void synchronizeGroupMembersOnUpdate(IEntityGroup group) throws GroupsException {
        EntityGroupImpl egi = (EntityGroupImpl) group;
        GroupMemberImpl gmi = null;

        for (IGroupMember iGroupMember : egi.getAddedMembers().values()) {
            gmi = (GroupMemberImpl) iGroupMember;
            gmi.invalidateInParentGroupsCache(Collections.singleton((IGroupMember) gmi));
            if (cacheInUse()) {
                cacheUpdate(gmi);
            }
        }

        for (IGroupMember iGroupMember : egi.getRemovedMembers().values()) {
            gmi = (GroupMemberImpl) iGroupMember;
            gmi.invalidateInParentGroupsCache(Collections.singleton((IGroupMember) gmi));
            if (cacheInUse()) {
                cacheUpdate(gmi);
            }
        }
    }
}
