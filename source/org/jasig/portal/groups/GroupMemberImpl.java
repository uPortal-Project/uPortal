/**
 * Copyright (c) 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups;

import org.jasig.portal.*;
import org.jasig.portal.security.*;
import org.jasig.portal.services.GroupService;
import java.util.*;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IGroupMember
 * @see IGroupMemberFactory
 */
public abstract class GroupMemberImpl implements IGroupMember
{
/*
 * The key to the underlying <code>IGroupMember</code>, which is either an <code>IEntity</code> or
 * an <code>IEntityGroup</code>.
 */
    private java.lang.String key;
/*
 * If an <code>IEntity</code>, the <code>type</code> of its underlying entity.  If an
 * <code>IEntityGroup</code>, the <code>entityType</code> of its <code>IEntities</code>.
 * This is analagous to <code>Class</code>, as applied to <code>Arrays</code> and their elements.
 */
    private static java.lang.Class defaultEntityType;
    private java.lang.Class entityType;

// Cache for the containing <code>IEntityGroups</code>.
    private HashMap groups;
    private boolean groupsInitialized;
/**
 * entityType will be defaulted to Object
 */
    public GroupMemberImpl(String newKey) throws GroupsException
{
    this(newKey, null);
}
/**
 * @exception GroupsException is thrown if the <code>entityType</code> is unknown.
 */
public GroupMemberImpl(String newKey, Class newEntityType) throws GroupsException
{
    super();
    setEntityType(newEntityType);
    key = newKey;
}
/**
 * Adds the <code>IEntityGroup</code> to our groups <code>Map</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public void addGroup(IEntityGroup eg)
{
    primGetGroups().put(eg.getName(), eg);
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IGroupMember gm) throws GroupsException
{
    return false;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
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
    return getKey() + new Boolean(isGroup()).hashCode();
}
/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * @return java.util.Iterator
 */
public java.util.Iterator getContainingGroups() throws GroupsException
{
    if ( ! isGroupsInitialized() )
        { initializeGroups(); }
    return primGetGroups().values().iterator();
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
 * @return org.jasig.portal.groups.IEntityStore
 */
protected IEntityStore getEntityFactory() throws GroupsException {
    return RDBMEntityStore.singleton();
}
/**
 * @return org.jasig.portal.groups.IEntityGroupStore
 */
protected IEntityGroupStore getEntityGroupFactory() throws GroupsException {
    return GroupService.getGroupService().getGroupStore();
}
/**
 * @return java.lang.Class
 */
public java.lang.Class getEntityType() {
    return entityType;
}
/**
 * @return java.lang.String
 */
public java.lang.String getKey() {
    return key;
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
 * Returns the type represented by the <code>IGroupMember</code>.  In the
 * case of an <code>IEntityGroup</code> this is <code>IEntityGroup</code>.  In
 * the case of an <code>IEntity</code> it is the <code>entityType</code> of
 * the <code>IEntity</code>.
 *
 * @return java.lang.Class
 */
public Class getType()
{
    return ( isGroup() )
        ? EntityTypes.GROUP_ENTITY_TYPE
        : getEntityType();
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
 * Load and cache <code>IEntityGroups</code> that contain this <code>IGroupMember</code>.
 * @return void
 */
private void initializeContainingGroups() throws GroupsException
{
    Iterator eGroups = getEntityGroupFactory().findContainingGroups(this);

    while (eGroups.hasNext())
    {
        IEntityGroup eg = (IEntityGroup) eGroups.next();
        addGroup(eg);
    }
}
/**
 * Load and cache <code>IEntityGroups</code> that contain this IGroupMember.
 */
private void initializeGroups() throws GroupsException
{
    initializeContainingGroups();
    setGroupsInitialized(true);
}
/**
 * Answers if this <code>IGroupMember</code> is, recursively, a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException{
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
 * @return boolean
 */
private boolean isGroupsInitialized() {
    return groupsInitialized;
}
/**
 * Answers if this <code>IGroupMember</code> is a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isMemberOf(IGroupMember gm) throws GroupsException
{
    return gm.contains(this);
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
 * @return java.util.HashMap
 */
private java.util.HashMap primGetGroups() {
    if ( this.groups == null )
        this.groups = new HashMap();
    return groups;
}
/**
 * Remove the <code>IEntityGroup</code> from our groups <code>Map</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public void removeGroup(IEntityGroup eg)
{
    primGetGroups().remove(eg.getName());
}
/**
 * @exception GroupsException is thrown if the <code>entityType</code> is unknown.
 */
private void setEntityType(Class newEntityType) throws GroupsException
{
    if ( newEntityType == null )
        entityType = getDefaultEntityType();
    else
    {
        Integer typeID = EntityTypes.getEntityTypeID(newEntityType);
        if ( typeID == null )
            throw new GroupsException("Unknown entity type: " + newEntityType);
        entityType = newEntityType;
    }
}
/**
 * @param newGroupsInitialized boolean
 */
private void setGroupsInitialized(boolean newGroupsInitialized) {
    groupsInitialized = newGroupsInitialized;
}
}
