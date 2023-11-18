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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.spring.locator.ApplicationContextLocator;
import org.apereo.portal.spring.locator.EntityTypesLocator;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.utils.cache.UsernameTaggedCacheEntryPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * This is the base class for every node in the graph.
 *
 * @see IGroupMember
 */
public abstract class GroupMemberImpl implements IGroupMember {

    /*
     * The <code>EntityIdentifier</code> that uniquely identifies the entity,
     * e.g., the <code>IPerson</code>, <code>ChannelDefinition</code>, etc.,
     * that underlies the <code>IGroupMember</code>.
     */
    private EntityIdentifier underlyingEntityIdentifier;

    private final Cache parentGroupsCache;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** GroupMemberImpl constructor */
    public GroupMemberImpl(EntityIdentifier newEntityIdentifier) throws GroupsException {
        super();
        if (isKnownEntityType(newEntityIdentifier.getType())) {
            underlyingEntityIdentifier = newEntityIdentifier;
        } else {
            throw new GroupsException("Unknown entity type: " + newEntityIdentifier.getType());
        }
        ApplicationContext context = ApplicationContextLocator.getApplicationContext();
        CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);
        this.parentGroupsCache =
                cacheManager.getCache("org.apereo.portal.groups.GroupMemberImpl.parentGroups");
    }

    /**
     * Returns an <code>Iterator</code> over the <code>Set</code> of this <code>IGroupMember's
     * </code> recursively-retrieved parent groups.
     *
     * @return Iterator
     */
    @Override
    public Set<IEntityGroup> getAncestorGroups() throws GroupsException {
        return primGetAncestorGroups(this, new HashSet<>());
    }

    /**
     * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
     * Synchronize the collection of keys with adds and removes.
     *
     * @return Iterator
     */
    @Override
    public Set<IEntityGroup> getParentGroups() throws GroupsException {

        final EntityIdentifier cacheKey = getUnderlyingEntityIdentifier();
        Element element = parentGroupsCache.get(cacheKey);

        if (element == null) {
            final Set<IEntityGroup> groups = buildParentGroupsSet();
            element = new Element(cacheKey, groups);
            parentGroupsCache.put(element);
        }

        @SuppressWarnings("unchecked")
        final Set<IEntityGroup> result = (Set<IEntityGroup>) element.getObjectValue();
        return result;
    }

    private synchronized Set<IEntityGroup> buildParentGroupsSet() throws GroupsException {
        logger.debug(
                "Constructing containingGroups for member='{}'", getUnderlyingEntityIdentifier());

        final Set<IEntityGroup> result = new HashSet<>();
        for (Iterator it = GroupService.getCompositeGroupService().findParentGroups(this);
                it.hasNext(); ) {
            final IEntityGroup eg = (IEntityGroup) it.next();
            result.add(eg);
        }

        return Collections.unmodifiableSet(result);
    }

    /** @return String */
    @Override
    public String getKey() {
        return getUnderlyingEntityIdentifier().getKey();
    }

    /** @return Class */
    @Override
    public Class getType() {
        return getUnderlyingEntityIdentifier().getType();
    }

    /** @return EntityIdentifier */
    @Override
    public EntityIdentifier getUnderlyingEntityIdentifier() {
        return underlyingEntityIdentifier;
    }

    /**
     * Answers if this <code>IGroupMember</code> is, recursively, a member of <code>IGroupMember
     * </code> gm.
     */
    @Override
    public boolean isDeepMemberOf(IEntityGroup group) throws GroupsException {
        return isMemberOf(group) ? true : group.deepContains(this);
    }

    /** @return boolean */
    @Override
    public boolean isGroup() {
        return false;
    }

    /** @return boolean. */
    protected boolean isKnownEntityType(Class anEntityType) throws GroupsException {
        return (EntityTypesLocator.getEntityTypes().getEntityIDFromType(anEntityType) != null);
    }

    /** Answers if this <code>IGroupMember</code> is a member of <code>IGroupMember</code> gm. */
    @Override
    public boolean isMemberOf(IEntityGroup group) throws GroupsException {
        return getParentGroups().contains(group);
    }

    /** @throws UnsupportedOperationException */
    @Override
    public IEntityGroup asGroup() {
        throw new UnsupportedOperationException("This member is not a group:  " + this.getKey());
    }

    /**
     * Returns the <code>Set</code> of groups in our member <code>Collection</code> and,
     * recursively, in the <code>Collections</code> of our members.
     *
     * @param member org.apereo.portal.groups.IGroupMember - The current group member in the
     *     recursive execution.
     * @param result Set - A Set that groups are added to.
     * @return Set
     */
    protected Set<IEntityGroup> primGetAncestorGroups(IGroupMember member, Set<IEntityGroup> result)
            throws GroupsException {
        for (IEntityGroup group : member.getParentGroups()) {
            // avoid stack overflow in case of circular group dependencies
            if (!result.contains(group)) {
                result.add(group);
                primGetAncestorGroups(group, result);
            }
        }
        return result;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((underlyingEntityIdentifier == null)
                                ? 0
                                : underlyingEntityIdentifier.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        GroupMemberImpl other = (GroupMemberImpl) obj;
        if (underlyingEntityIdentifier == null) {
            if (other.underlyingEntityIdentifier != null) return false;
        } else if (!underlyingEntityIdentifier.equals(other.underlyingEntityIdentifier))
            return false;
        return true;
    }

    protected void invalidateInParentGroupsCache(Set<IGroupMember> members) {
        for (IGroupMember member : members) {
            parentGroupsCache.remove(member.getEntityIdentifier());
        }
    }

    protected CacheKey getCacheKey(EntityIdentifier entityIdentifier) {
        // Use tagged keys for users (only) so the cache entries will be dropped when they
        // authenticate
        return IPerson.class.equals(entityIdentifier.getType())
                ? CacheKey.buildTagged(
                        getClass().getName(),
                        UsernameTaggedCacheEntryPurger.createCacheEntryTag(
                                entityIdentifier.getKey()),
                        entityIdentifier)
                : CacheKey.build(getClass().getName(), entityIdentifier);
    }
}
