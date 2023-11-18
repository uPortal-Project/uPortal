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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.naming.Name;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Reference implementation for <code>IEntityGroup</code>
 *
 * <p>Groups do not keep references to their members but instead cache member keys. The members are
 * cached externally. The rules for controlling access to the key caches are a bit obscure, but you
 * should understand them before writing code that updates groups. Access to the caches themselves
 * is synchronized via the cache getters and setters. All requests to get group members and to add
 * or remove group members ultimately go through these methods. The mutating methods, <code>
 * addChild()</code> and <code>removeChild()</code> however, do a copy-on-write. That is, they first
 * make a copy of the cache, add or remove the member key, and then replace the original cache with
 * the copy. This permits multiple read and write threads to run concurrently without throwing
 * <code>ConcurrentModificationExceptions</code>. But it still leaves open the danger of data races
 * because nothing in this class guarantees serialized write access. You must impose this from
 * without, either via explicit locking (<code>GroupService.getLockableGroup()</code>) or by
 * synchronizing access from the caller.
 *
 * @see IEntityGroup
 */
public class EntityGroupImpl extends GroupMemberImpl implements IEntityGroup {
    private String creatorID;
    private String name;
    private String description;
    private IIndividualGroupService localGroupService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cache childrenCache;

    // A group and its members share an entityType.
    private Class<? extends IBasicEntity> leafEntityType;

    /*
     * References to updated group members.  These updates do not become visible to
     * the members until the update is committed.
     */
    private HashMap<String, IGroupMember> addedMembers;
    private HashMap<String, IGroupMember> removedMembers;
    /** EntityGroupImpl */
    public EntityGroupImpl(String groupKey, Class<? extends IBasicEntity> entityType)
            throws GroupsException {
        super(new CompositeEntityIdentifier(groupKey, ICompositeGroupService.GROUP_ENTITY_TYPE));
        if (isKnownEntityType(entityType)) {
            leafEntityType = entityType;
        } else {
            throw new GroupsException("Unknown entity type: " + entityType);
        }
        ApplicationContext context = ApplicationContextLocator.getApplicationContext();
        CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);
        this.childrenCache =
                cacheManager.getCache("org.apereo.portal.groups.EntityGroupImpl.children");
    }
    /**
     * Adds <code>IGroupMember</code> gm to our member <code>Map</code> and conversely, adds <code>
     * this</code> to gm's group <code>Map</code>, after checking that the addition does not violate
     * group rules. Remember that we have added it so we can update the database if necessary.
     *
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    @Override
    public void addChild(IGroupMember gm) throws GroupsException {

        try {
            checkProspectiveMember(gm);
        } catch (GroupsException ge) {
            throw new GroupsException("Could not add IGroupMember", ge);
        }

        if (!this.contains(gm)) {
            String cacheKey = gm.getEntityIdentifier().getKey();
            if (getRemovedMembers().containsKey(cacheKey)) {
                getRemovedMembers().remove(cacheKey);
            } else {
                getAddedMembers().put(cacheKey, gm);
            }
        }

        primAddMember(gm);
    }

    /**
     * A member must share the <code>entityType</code> of its containing <code>IEntityGroup</code>.
     * If it is a group, it must have a unique name within each of its containing groups and the
     * resulting group must not contain a circular reference. Removed the requirement for unique
     * group names. (03-04-2004, de)
     *
     * @param gm org.apereo.portal.groups.IGroupMember
     * @exception GroupsException
     */
    private void checkProspectiveMember(IGroupMember gm) throws GroupsException {
        if (gm.equals(this)) {
            throw new GroupsException("Attempt to add " + gm + " to itself.");
        }

        // Type check:
        if (this.getLeafType() != gm.getLeafType()) {
            throw new GroupsException(this + " and " + gm + " have different entity types.");
        }

        // Circular reference check:
        if (gm.isGroup() && gm.asGroup().deepContains(this)) {
            throw new GroupsException(
                    "Adding " + gm + " to " + this + " creates a circular reference.");
        }
    }
    /** Clear out caches for pending adds and deletes of group members. */
    protected void clearPendingUpdates() {
        addedMembers = null;
        removedMembers = null;
    }

    /**
     * Checks if <code>GroupMember</code> gm is a member of this.
     *
     * @return boolean
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    @Override
    public boolean contains(IGroupMember gm) throws GroupsException {
        return getChildren().contains(gm);
    }

    private synchronized Set<IGroupMember> buildChildrenSet() throws GroupsException {
        logger.debug("Constructing children for group='{}'", getUnderlyingEntityIdentifier());

        final Set<IGroupMember> result = new HashSet<>();
        for (Iterator it = getLocalGroupService().findMembers(this); it.hasNext(); ) {
            final IGroupMember member = (IGroupMember) it.next();
            result.add(member);
        }

        return Collections.unmodifiableSet(result);
    }

    /**
     * Checks recursively if <code>GroupMember</code> gm is a member of this.
     *
     * @return boolean
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    @Override
    public boolean deepContains(IGroupMember gm) throws GroupsException {
        if (this.contains(gm)) {
            return true;
        }

        boolean found = false;
        Iterator<IEntityGroup> it = getMemberGroups();
        while (it.hasNext() && !found) {
            IEntityGroup group = it.next();
            if (group != null) {
                found = group.deepContains(gm);
            } else {
                // Something bad has happened:  we've abruptly lost a group node to
                // which this group node refers.  This is an ERROR condition, but we
                // shouldn't simply throw an exception because we know (from
                // experience) it could make the portal unusable.  We definitely,
                // need, however, to send a strong message.
                String msg =
                        "Groups Integrety Error:  Group '"
                                + this.getName()
                                + "' refers to a child group that is no longer available";
                logger.error(msg);
            }
        }

        return found;
    }
    /** Delegates to the factory. */
    @Override
    public void delete() throws GroupsException {
        getLocalGroupService().deleteGroup(this);
    }

    /** @return HashMap */
    public HashMap<String, IGroupMember> getAddedMembers() {
        if (this.addedMembers == null) this.addedMembers = new HashMap<>();
        return addedMembers;
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Set</code> of recursively-retrieved <code>
     * IGroupMembers</code> that are members of this <code>IEntityGroup</code>.
     *
     * @return Iterator
     */
    @Override
    public Set<IGroupMember> getDescendants() throws GroupsException {
        return primGetAllMembers(new HashSet<IGroupMember>());
    }
    /**
     * Returns the <code>EntityIdentifier</code> cast to a <code>CompositeEntityIdentifier</code> so
     * that its service nodes can be pushed and popped.
     *
     * @return CompositeEntityIdentifier
     */
    private CompositeEntityIdentifier getCompositeEntityIdentifier() {
        return (CompositeEntityIdentifier) getEntityIdentifier();
    }
    /** @return String */
    @Override
    public String getCreatorID() {
        return creatorID;
    }
    /** @return String */
    @Override
    public String getDescription() {
        return description;
    }

    /** @return EntityIdentifier */
    @Override
    public EntityIdentifier getEntityIdentifier() {
        return getUnderlyingEntityIdentifier();
    }

    /**
     * Returns the entity type of this groups's members.
     *
     * @return Class
     * @see org.apereo.portal.EntityTypes
     */
    @Override
    public Class<? extends IBasicEntity> getLeafType() {
        return leafEntityType;
    }
    /** @return IIndividualGroupService */
    protected IIndividualGroupService getLocalGroupService() {
        return localGroupService;
    }
    /**
     * Returns the key from the group service of origin.
     *
     * @return String
     */
    @Override
    public String getLocalKey() {
        return getCompositeEntityIdentifier().getLocalKey();
    }

    /**
     * Returns an <code>Iterator</code> over the groups in our member <code>Collection</code>.
     * Reflects pending changes.
     *
     * @return Iterator
     */
    private Iterator<IEntityGroup> getMemberGroups() throws GroupsException {

        Set<IEntityGroup> result = new HashSet<>();
        for (IGroupMember child : getChildren()) {
            if (child.isGroup()) {
                result.add((IEntityGroup) child);
            }
        }

        return result.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over the <code>GroupMembers</code> in our member <code>
     * Collection</code>. Reflects pending changes.
     *
     * @return Iterator
     */
    @Override
    public Set<IGroupMember> getChildren() throws GroupsException {

        final EntityIdentifier cacheKey = getUnderlyingEntityIdentifier();
        Element element = childrenCache.get(cacheKey);

        if (element == null) {
            final Set<IGroupMember> children = buildChildrenSet();
            element = new Element(cacheKey, children);
            childrenCache.put(element);
        }

        @SuppressWarnings("unchecked")
        final Set<IGroupMember> result = (Set<IGroupMember>) element.getObjectValue();
        return result;
    }

    /** @return String */
    @Override
    public String getName() {
        return name;
    }
    /** @return HashMap */
    public HashMap<String, IGroupMember> getRemovedMembers() {
        if (this.removedMembers == null) this.removedMembers = new HashMap<>();
        return removedMembers;
    }
    /**
     * Returns the Name of the group service of origin.
     *
     * @return javax.naming.Nme
     */
    @Override
    public Name getServiceName() {
        return getCompositeEntityIdentifier().getServiceName();
    }
    /**
     * Returns this object's type for purposes of caching and locking, as opposed to the underlying
     * entity type.
     *
     * @return Class
     */
    @Override
    public Class<?> getType() {
        return ICompositeGroupService.GROUP_ENTITY_TYPE;
    }
    /**
     * Answers if there are any added memberships not yet committed to the database.
     *
     * @return boolean
     */
    /* package-private */ boolean hasAdds() {
        return (addedMembers != null) && (addedMembers.size() > 0);
    }
    /**
     * Answers if there are any deleted memberships not yet committed to the database.
     *
     * @return boolean
     */
    /* package-private */ boolean hasDeletes() {
        return (removedMembers != null) && (removedMembers.size() > 0);
    }

    /** @return boolean */
    @Override
    public boolean hasMembers() throws GroupsException {
        return !getChildren().isEmpty();
    }

    /**
     * Answers if there are any added or deleted memberships not yet committed to the database.
     *
     * @return boolean
     */
    public boolean isDirty() {
        return hasAdds() || hasDeletes();
    }
    /**
     * Answers if this <code>IEntityGroup</code> can be changed or deleted.
     *
     * @return boolean
     * @exception GroupsException
     */
    @Override
    public boolean isEditable() throws GroupsException {
        return getLocalGroupService().isEditable(this);
    }
    /** @return boolean */
    @Override
    public boolean isGroup() {
        return true;
    }

    /**
     * Adds the <code>IGroupMember</code> key to the appropriate member key cache by copying the
     * cache, adding to the copy, and then replacing the original with the copy. At this point,
     * <code>gm</code> does not yet have <code>this</code> in its containing group cache. That cache
     * entry is not added until update(), when changes are committed to the store.
     *
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    private void primAddMember(IGroupMember gm) throws GroupsException {
        final EntityIdentifier cacheKey = getUnderlyingEntityIdentifier();
        Element element = childrenCache.get(cacheKey);

        @SuppressWarnings("unchecked")
        final Set<IGroupMember> set =
                element != null ? (Set<IGroupMember>) element.getObjectValue() : buildChildrenSet();

        final Set<IGroupMember> children = new HashSet<>(set);
        children.add(gm);
        childrenCache.put(new Element(cacheKey, children));
    }

    /**
     * Returns the <code>Set</code> of <code>IGroupMembers</code> in our member <code>Collection
     * </code> and, recursively, in the <code>Collections</code> of our members.
     *
     * @param result Set - a Set that members are added to.
     * @return Set
     */
    private Set<IGroupMember> primGetAllMembers(Set<IGroupMember> result) throws GroupsException {
        for (IGroupMember gm : getChildren()) {
            result.add(gm);
            if (gm.isGroup()) {
                ((EntityGroupImpl) gm).primGetAllMembers(result);
            }
        }
        return result;
    }

    /**
     * Removes the <code>IGroupMember</code> key from the appropriate key cache, by copying the
     * cache, removing the key from the copy and replacing the original with the copy. At this
     * point, <code>gm</code> still has <code>this</code> in its containing groups cache. That cache
     * entry is not removed until update(), when changes are committed to the store.
     *
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    private void primRemoveMember(IGroupMember gm) throws GroupsException {
        final EntityIdentifier cacheKey = getUnderlyingEntityIdentifier();
        Element element = childrenCache.get(cacheKey);

        @SuppressWarnings("unchecked")
        final Set<IGroupMember> set =
                element != null ? (Set<IGroupMember>) element.getObjectValue() : buildChildrenSet();

        final Set<IGroupMember> children = new HashSet<>(set);
        children.remove(gm);
        childrenCache.put(new Element(cacheKey, children));
    }

    /** @param newName String */
    public void primSetName(String newName) {
        name = newName;
    }
    /**
     * Removes <code>IGroupMember</code> gm from our member <code>Map</code> and, conversely, remove
     * this from gm's group <code>Map</code>. Remember that we have removed it so we can update the
     * database, if necessary.
     *
     * @param gm org.apereo.portal.groups.IGroupMember
     */
    @Override
    public void removeChild(IGroupMember gm) throws GroupsException {
        String cacheKey = gm.getEntityIdentifier().getKey();

        if (getAddedMembers().containsKey(cacheKey)) {
            getAddedMembers().remove(cacheKey);
        } else {
            getRemovedMembers().put(cacheKey, gm);
        }

        primRemoveMember(gm);
    }
    /** @param newCreatorID String */
    @Override
    public void setCreatorID(String newCreatorID) {
        creatorID = newCreatorID;
    }
    /** @param newDescription String */
    @Override
    public void setDescription(String newDescription) {
        description = newDescription;
    }
    /** @param newIndividualGroupService IIndividualGroupService */
    @Override
    public void setLocalGroupService(IIndividualGroupService newIndividualGroupService)
            throws GroupsException {
        localGroupService = newIndividualGroupService;
        setServiceName(localGroupService.getServiceName());
    }

    /**
     * We used to check duplicate sibling names but no longer do.
     *
     * @param newName String
     */
    @Override
    public void setName(String newName) throws GroupsException {
        primSetName(newName);
    }
    /** Sets the service Name of the group service of origin. */
    public void setServiceName(Name newServiceName) throws GroupsException {
        try {
            getCompositeEntityIdentifier().setServiceName(newServiceName);
        } catch (javax.naming.InvalidNameException ine) {
            throw new GroupsException("Problem setting service name", ine);
        }
    }
    /**
     * Returns a String that represents the value of this object.
     *
     * @return a string representation of the receiver
     */
    @Override
    public String toString() {
        return "EntityGroupImpl (" + getKey() + ") " + getName();
    }
    /** Delegate to the factory. */
    @Override
    public void update() throws GroupsException {
        getLocalGroupService().updateGroup(this);
        clearPendingUpdates();
    }
    /** Delegate to the factory. */
    @Override
    public void updateMembers() throws GroupsException {

        // Track objects to invalidate
        Set<IGroupMember> invalidate = new HashSet<>();
        invalidate.addAll(getAddedMembers().values());
        invalidate.addAll(getRemovedMembers().values());

        getLocalGroupService().updateGroupMembers(this);
        clearPendingUpdates();

        // Invalidate objects that changed their relationship with us
        this.invalidateInParentGroupsCache(invalidate);
    }

    /** Casts to IEntityGroup. */
    @Override
    public IEntityGroup asGroup() {
        return (IEntityGroup) this;
    }
}
