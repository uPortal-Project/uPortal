/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * GroupMemberImpl summary first sentence goes here.
 * 
 * @author Dan Ellentuck
 * @see IGroupMember
 */
public abstract class GroupMemberImpl implements IGroupMember {

    /*
     * The <code>EntityIdentifier</code> that uniquely identifies the entity,
     * e.g., the <code>IPerson</code>, <code>ChannelDefinition</code>, etc.,
     * that underlies the <code>IGroupMember</code>.
     */
    private EntityIdentifier underlyingEntityIdentifier;
    private static volatile Class defaultEntityType;

/*
 * The Set of keys to groups that contain this <code>IGroupMember</code>.
 * the groups themselves are cached by the service.
 */
    private Set<String> groupKeys;
    private boolean groupKeysInitialized;

    private final Cache containingGroupsCache;
    private Logger log = LoggerFactory.getLogger(getClass());

/**
 * GroupMemberImpl constructor
 */
public GroupMemberImpl(EntityIdentifier newEntityIdentifier) throws GroupsException
{
    super();
    if ( isKnownEntityType(newEntityIdentifier.getType()) )
        { underlyingEntityIdentifier = newEntityIdentifier; }
    else
        { throw new GroupsException("Unknown entity type: " + newEntityIdentifier.getType()); }
    ApplicationContext context = ApplicationContextLocator.getApplicationContext();
    CacheManager cacheManager = context.getBean("cacheManager", CacheManager.class);
    this.containingGroupsCache = cacheManager.getCache("org.jasig.portal.groups.GroupMemberImpl.containingGroups");
}

/**
 * Adds the key of the <code>IEntityGroup</code> to our <code>Set</code> of group keys
 * by copying the keys, updating the copy, and replacing the old keys with the copy.
 * This lets us confine synchronization to the getter and setter methods for the keys.
 * @param eg org.jasig.portal.groups.IEntityGroup
 */
public void addGroup(IEntityGroup eg) throws GroupsException
{
    Set<String> newGroupKeys = copyGroupKeys();
    newGroupKeys.add(eg.getEntityIdentifier().getKey());
    setGroupKeys(newGroupKeys);
}

/**
 * @return boolean
 */
private boolean areGroupKeysInitialized() {
    return groupKeysInitialized;
}

/**
 * Default implementation, overridden on EntityGroupImpl.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
public boolean contains(IGroupMember gm) throws GroupsException
{
    return false;
}

/**
 * Clone the group keys.
 * @return Set
 */
private Set<String> copyGroupKeys() throws GroupsException
{
   return new HashSet<>(getGroupKeys());
}

/**
 * Default implementation, overridden on EntityGroupImpl.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
public boolean deepContains(IGroupMember gm) throws GroupsException
{
    return false;
}

/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IGroupMember's</code> recursively-retrieved parent groups.
 *
 * @return Iterator
 */
public Iterator getAllContainingGroups() throws GroupsException
{
    return primGetAllContainingGroups(this, new HashSet()).iterator();
}

/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return Iterator
 */
public Iterator getAllEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return Iterator
 */
public Iterator getAllMembers() throws GroupsException
{
    return getEmptyIterator();
}

/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * Synchronize the collection of keys with adds and removes.
 * @return Iterator
 */
public Iterator getContainingGroups() throws GroupsException {

    final EntityIdentifier cacheKey = getUnderlyingEntityIdentifier();
    Element element = containingGroupsCache.get(cacheKey);

    if (element == null) {
        log.debug("Constructing containingGroups for member='{}'", cacheKey);

        final Set<String> groupKeys = getGroupKeys();
        final List<IEntityGroup> groups = new ArrayList<>(groupKeys.size());
        for (Iterator<String> itr = groupKeys.iterator(); itr.hasNext();) {
            final String groupKey = (String) itr.next();
            groups.add(GroupService.getCompositeGroupService().findGroup(groupKey));
        }

        element = new Element(cacheKey, groups);
        containingGroupsCache.put(element);
    }

    @SuppressWarnings("unchecked")
    final List<IEntityGroup> rslt = (List<IEntityGroup>) element.getObjectValue();
    return rslt.iterator();

}

/**
 * @return Class
 */
private Class getDefaultEntityType()
{
    if (defaultEntityType == null)
    {
        Class cls = (new Object()).getClass();
        defaultEntityType = cls;
    }
    return defaultEntityType;
}
/**
 * @return Iterator
 */
private Iterator getEmptyIterator()
{
    return Collections.EMPTY_LIST.iterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return Iterator
 */
public Iterator getEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return Set
 */
private synchronized Set<String> getGroupKeys() throws GroupsException {
    if ( ! groupKeysInitialized )
        { initializeContainingGroupKeys(); }
    return groupKeys;
}
/**
 * @return String
 */
public String getKey() {
    return getUnderlyingEntityIdentifier().getKey();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException
{
    return null;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return Iterator
 */
public Iterator getMembers() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return Class
 */
public Class getType() {
    return getUnderlyingEntityIdentifier().getType();
}
/**
 * @return EntityIdentifier
 */
public EntityIdentifier getUnderlyingEntityIdentifier() {
    return underlyingEntityIdentifier;
}
/*
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
    return getKey().hashCode();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 */
public boolean hasMembers() throws GroupsException
{
    return false;
}
/**
 * Cache the keys for <code>IEntityGroups</code> that contain this <code>IGroupMember</code>.
 */
private void initializeContainingGroupKeys() throws GroupsException
{
    log.debug("Initialzing keys for groups that contain member {}. Finding existing containing groups", this.getKey());
    Set<String> keys = new HashSet(10);
    for (Iterator it = GroupService.getCompositeGroupService().findContainingGroups(this); it.hasNext(); )
    {
        IEntityGroup eg = (IEntityGroup) it.next();
        keys.add(eg.getEntityIdentifier().getKey());
    }
    setGroupKeys(keys);
    setGroupKeysInitialized(true);
}

/**
 * Answers if this <code>IGroupMember</code> is, recursively, a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException {

    if ( this.isMemberOf(gm) )
        { return true; }
    return gm.deepContains( this );
}
/**
 * @return boolean
 */
public boolean isEntity()
{
    return false;
}
/**
 * @return boolean
 */
public boolean isGroup()
{
    return false;
}
/**
 * @return boolean.
 */
protected boolean isKnownEntityType(Class anEntityType) throws GroupsException
{
    return ( org.jasig.portal.EntityTypes.getEntityTypeID(anEntityType) != null );
}

/**
 * Answers if this <code>IGroupMember</code> is a member of <code>IGroupMember</code> gm.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @return boolean
 */
public boolean isMemberOf(IGroupMember gm) throws GroupsException
{
    if ( gm==this || gm.isEntity() )
        { return false; }
    Object cacheKey = gm.getKey();
    return getGroupKeys().contains(cacheKey);
}
/**
 * Returns the <code>Set</code> of groups in our member <code>Collection</code> and,
 * recursively, in the <code>Collections</code> of our members.
 * @param member org.jasig.portal.groups.IGroupMember - The current group member in the recursive execution.
 * @param s Set - A Set that groups are added to.
 * @return Set
 */
protected Set primGetAllContainingGroups(IGroupMember member, Set s) throws GroupsException
{
    Iterator i = member.getContainingGroups();
    while ( i.hasNext() )
    {
        IGroupMember gm = (IGroupMember) i.next();
        // avoid stack overflow in case of circular group dependencies
        if (!s.contains(gm)) {
            s.add(gm);
            primGetAllContainingGroups(gm, s);
        }
    }
    return s;
}
/**
 * Removes the key of the <code>IEntityGroup</code> from our <code>Set</code> of group keys
 * by copying the keys, updating the copy, and replacing the old keys with the copy.
 * This lets us confine synchronization to the getter and setter methods for the keys.
 * @param eg org.jasig.portal.groups.IEntityGroup
 */
public void removeGroup(IEntityGroup eg) throws GroupsException
{
    Set<String> newGroupKeys = copyGroupKeys();
    newGroupKeys.remove(eg.getEntityIdentifier().getKey());
    setGroupKeys(newGroupKeys);
}
/**
 * @param newGroupKeys Set
 */
private synchronized void setGroupKeys(Set<String> newGroupKeys)
{
    groupKeys = newGroupKeys;
}
/**
 * @param newGroupKeysInitialized boolean
 */
protected void setGroupKeysInitialized(boolean newGroupKeysInitialized) {
    groupKeysInitialized = newGroupKeysInitialized;
}

    protected void invalidateInContainingGroupsCache(Set<IGroupMember> members) {
        for (IGroupMember member : members) {
            containingGroupsCache.remove(member.getEntityIdentifier());
        }
    }

}
