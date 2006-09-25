/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.GroupService;

/**
 * GroupMemberImpl summary first sentence goes here.
 * 
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IGroupMember
 */
public abstract class GroupMemberImpl implements IGroupMember
{
/*
 * The <code>EntityIdentifier</code> that uniquely identifies the entity,
 * e.g., the <code>IPerson</code>, <code>ChannelDefinition</code>, etc.,
 * that underlies the <code>IGroupMember</code>.
 */
    private EntityIdentifier underlyingEntityIdentifier;
    private static java.lang.Class defaultEntityType;

/*
 * The Set of keys to groups that contain this <code>IGroupMember</code>.
 * the groups themselves are cached by the service.
 */
    private Set groupKeys;
    private boolean groupKeysInitialized;
/**
 * GroupMemberImpl constructor
 */
public GroupMemberImpl(String key, Class type) throws GroupsException
{
    this(new EntityIdentifier(key, type));
}
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
}

/**
 * Adds the key of the <code>IEntityGroup</code> to our <code>Set</code> of group keys
 * by copying the keys, updating the copy, and replacing the old keys with the copy.
 * This lets us confine synchronization to the getter and setter methods for the keys.
 * @param eg org.jasig.portal.groups.IEntityGroup
 */
public void addGroup(IEntityGroup eg) throws GroupsException
{
    Set newGroupKeys = copyGroupKeys();
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
private Set copyGroupKeys() throws GroupsException
{
   return castAndCopyHashSet(getGroupKeys());
}

/**
 * Cast a Set to a HashSet, clone it, and down cast back to Set.
 * @return HashSet
 */
protected Set castAndCopyHashSet(Set s)
{
   return (Set)((HashSet)s).clone(); 
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
 * @return java.util.Iterator
 */
public java.util.Iterator getAllContainingGroups() throws GroupsException
{
    return primGetAllContainingGroups(new HashSet()).iterator();
}

/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllMembers() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return java.lang.String
 */
protected String getCacheKey() {
    return getEntityIdentifier().getKey();
}
/**
 * Returns the composite group service.
 */
protected ICompositeGroupService getCompositeGroupService() throws GroupsException
{
    return GroupService.getCompositeGroupService();
}
/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * Synchronize the collection of keys with adds and removes.
 * @return java.util.Iterator
 */
public java.util.Iterator getContainingGroups() throws GroupsException
{
    Collection groupsColl;

    Set groupKeys = getGroupKeys();
    groupsColl = new ArrayList(groupKeys.size());
    for (Iterator itr = groupKeys.iterator(); itr.hasNext(); )
    {
        String groupKey = (String) itr.next();
        groupsColl.add(getCompositeGroupService().findGroup(groupKey));
    }

    return groupsColl.iterator();
}
/**
 * @return java.lang.Class
 */
private java.lang.Class getDefaultEntityType()
{
    if (defaultEntityType == null)
    {
        Class cls = (new Object()).getClass();
        defaultEntityType = cls;
    }
    return defaultEntityType;
}
/**
 * @return java.util.Iterator
 */
private java.util.Iterator getEmptyIterator()
{
    return java.util.Collections.EMPTY_LIST.iterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return java.util.Set
 */
private synchronized java.util.Set getGroupKeys() throws GroupsException {
    if ( ! groupKeysInitialized )
        { initializeContainingGroupKeys(); }
    return groupKeys;
}
/**
 * @return java.lang.String
 */
public java.lang.String getKey() {
    return getUnderlyingEntityIdentifier().getKey();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException
{
    return null;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getMembers() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return java.lang.Class
 */
public java.lang.Class getType() {
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
    Set keys = new HashSet(10);
    for ( Iterator it = getCompositeGroupService().findContainingGroups(this); it.hasNext(); )
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

    if ( gm.isEntity() )
        { return false; }
    if ( this.isMemberOf(gm) )
        { return true; }
    return gm.deepContains(this); 
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
 * @param s java.lang.Set - A Set that groups are added to.
 * @return java.util.Set
 */
protected java.util.Set primGetAllContainingGroups(Set s) throws GroupsException
{
    Iterator i = getContainingGroups();
    while ( i.hasNext() )
    {
        GroupMemberImpl gmi = (GroupMemberImpl) i.next();
        s.add(gmi);
        gmi.primGetAllContainingGroups(s);
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
    Set newGroupKeys = copyGroupKeys();
    newGroupKeys.remove(eg.getEntityIdentifier().getKey());
    setGroupKeys(newGroupKeys);
}
/**
 * @param newGroupKeys Set
 */
private synchronized void setGroupKeys(Set newGroupKeys)
{
    groupKeys = newGroupKeys;
}
/**
 * @param newGroupKeysInitialized boolean
 */
protected void setGroupKeysInitialized(boolean newGroupKeysInitialized) {
    groupKeysInitialized = newGroupKeysInitialized;
}
}
