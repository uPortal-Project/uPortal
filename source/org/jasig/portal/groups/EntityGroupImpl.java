/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.*;
import org.jasig.portal.*;

/**
 * Reference implementation for <code>IEntityGroup</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.groups.IEntityGroup
 */
public class EntityGroupImpl extends GroupMemberImpl implements IEntityGroup
{
    private java.lang.String creatorID;
    private java.lang.String name;
    private java.lang.String description;

    // A group and its members share an entityType.
    private java.lang.Class entityType;

    // Caches for the contained GroupMembers.
    private HashMap members;
    private boolean membersInitialized;

    // Remember the updates.
    private HashMap addedMembers;
    private HashMap removedMembers;
/**
 * EntityGroupImpl
 */
public EntityGroupImpl(String groupKey, Class groupType) throws GroupsException {
    super(groupKey);
    if ( isKnownEntityType(groupType) )
        { entityType = groupType; }
    else
        { throw new GroupsException("Unknown entity type: " + groupType); }
}
/**
 * Adds <code>GroupMember</code> gm to our member <code>Map</code> and conversely,
 * adds this to gm's group <code>Map</code>, after checking its <code>entityType</code>
 * and <code>name</code>.  Remember that we have added it so we can update the
 * database if necessary.
 * @return void
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public void addMember(IGroupMember gm) throws GroupsException
{
    try
        { checkProspectiveMember(gm); }
    catch (GroupsException ge)
        { throw new GroupsException("Could not add IGroupMember: " + ge.getMessage() );}

    Object cacheKey = ((GroupMemberImpl)gm).getCacheKey();

    if ( getRemovedMembers().containsKey(cacheKey) )
        { getRemovedMembers().remove(cacheKey); }
    else
        { getAddedMembers().put(cacheKey, gm); }

    primAddMember(gm);
}
/**
 * Checks to see if adding the prospect will create a circular reference.
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkForCircularReference(IGroupMember gm) throws GroupsException
{
    if ( gm.deepContains(this) )
        throw new GroupsException("Adding " + gm + " to " + this + " creates a circular reference.");
}
/**
 * Checks to see if the prospect already belongs to this.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkIfAlreadyMember(IGroupMember gm) throws GroupsException
{
    if ( this.contains(gm) )
        throw new GroupsException(gm + " is already a member of " + this);
}
/**
 * Checks to see if the prospect has the same <code>entityType</code> as this.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkProspectiveEntityType(IGroupMember gm) throws GroupsException
{
    if ( this.getLeafType() != gm.getLeafType() )
        throw new GroupsException(this + " and " + gm + " have different entity types.");
}
/**
 * A member must share the <code>entityType</code> of its containing <code>IEntityGroup</code>.
 * If it is a group, it must have a unique name within each of its containing groups and
 * the resulting group must not contain a circular reference.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkProspectiveMember(IGroupMember gm) throws GroupsException
{
    if ( gm.equals(this) )
    {
        throw new GroupsException("Attempt to add " + gm + " to itself.");
    }
    checkIfAlreadyMember(gm);
    checkProspectiveMemberEntityType(gm);

    if ( gm.isGroup() )
    {
        String newName = ((IEntityGroup)gm).getName();
        checkProspectiveMemberGroupName(newName);
        checkForCircularReference(gm);
    }
}
/**
 * Checks to see if the prospect has the same <code>entityType</code> as this.
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkProspectiveMemberEntityType(IGroupMember gm) throws GroupsException
{
    if ( this.getLeafType() != gm.getLeafType() )
        throw new GroupsException(this + " and " + gm + " have different entity types.");
}
/**
 * Checks to see if this <code>IEntityGroup</code> already has a member group named newName.
 * @param newName String
 * @exception org.jasig.portal.groups.GroupsException
 */
protected void checkProspectiveMemberGroupName(String newName) throws GroupsException
{
    if ( this.getMemberGroupNamed(newName) != null )
        throw new GroupsException(this + " already contains a group named " + newName + ".");
}
/**
 * Clear out caches for pending adds and deletes of group members.
 */
private void clearPendingUpdates()
{
    addedMembers = null;
    removedMembers = null;
}
/**
 * Checks if <code>GroupMember</code> gm is a member of this.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IGroupMember gm) throws GroupsException
{
    if ( ! isMembersInitialized() )
        { initializeMembers(); }

    Object cacheKey = ((GroupMemberImpl)gm).getCacheKey();
    return primGetMembers().containsKey(cacheKey);
}
/**
 * Checks recursively if <code>GroupMember</code> gm is a member of this.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean deepContains(IGroupMember gm) throws GroupsException
{
    if ( this.contains(gm) )
        return true;

    boolean found = false;
    Iterator it = getMembers();
    while ( it.hasNext() && !found )
    {
        IGroupMember myGm = (IGroupMember) it.next();
        found = myGm.deepContains(gm);
    }

    return found;
}
/**
 * Delegates to the factory.
 */
public void delete() throws GroupsException
{
    try
        { getFactory().delete(this); }
    catch ( Exception ex )
        { throw new GroupsException("Problem deleting " + this + " " + ex); }
}
/**
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj)
{
    if ( obj == null )
        return false;
    if ( obj == this )
        return true;
    if ( ! ( obj instanceof EntityGroupImpl))
        return false;

    return this.getKey().equals(((IGroupMember)obj).getKey());
}
/**
 * Delegate to our factory.
 */
public Iterator findContainingGroups() throws GroupsException
{
    return getFactory().findContainingGroups(this);
}
/**
 * Finds the <code>IEntities</code> that are members of this.  Delegate to
 * our factory.
 */
private Iterator findMemberEntities() throws GroupsException
{
    return getEntityFactory().findEntitiesForGroup(this);
}
/**
 * Delegate to our factory.
 */
public Iterator findMemberGroups() throws GroupsException
{
    return getFactory().findMemberGroups(this);
}
/**
 * @return java.util.HashMap
 */
protected HashMap getAddedMembers()
{
    if ( this.addedMembers == null )
        this.addedMembers = new HashMap();
    return addedMembers;
}
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IEntityGroup's</code> recursively-retrieved members that are
 * <code>IEntities</code>.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntities() throws GroupsException
{
    return primGetAllEntities(new HashSet()).iterator();
}
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of recursively-retrieved
 * <code>IGroupMembers</code> that are members of this <code>IEntityGroup</code>.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllMembers() throws GroupsException
{
    return primGetAllMembers(new HashSet()).iterator();
}
/**
 * @return java.lang.String
 */
public java.lang.String getCreatorID() {
    return creatorID;
}
/**
 * @return java.lang.String
 */
public java.lang.String getDescription() {
    return description;
}
/**
 * Returns an <code>Iterator</code> over this <code>IEntityGroup's</code>
 * members that are <code>IEntities</code>.
 * @return java.util.Iterator
 */
public java.util.Iterator getEntities() throws GroupsException
{
    Collection entities = new ArrayList();
    Iterator i = getMembers();
    while ( i.hasNext() )
    {
        IGroupMember m = (IGroupMember) i.next();
        if ( m.isEntity() )
            entities.add(m);
    }
    return entities.iterator();
}
/**
 * Returns the key of the underyling entity.
 * @return java.lang.String
 */
public String getEntityKey()
{
    return getKey();
}
/**
 * Returns the entity type of this groups's members.
 *
 * @return java.lang.Class
 * @see org.jasig.portal.EntityTypes
 */
 public java.lang.Class getEntityType() {
    return entityType;
}
/**
  * @return org.jasig.portal.groups.IEntityGroupStore
 */
private IEntityGroupStore getFactory() throws GroupsException {
    return getEntityGroupFactory();
}
/**
 * @return String
 */
public String getGroupID() {
    return getKey();
}
/**
 * Returns the entity type of this groups's members.
 *
 * @return java.lang.Class
 * @see org.jasig.portal.EntityTypes
 */
public java.lang.Class getLeafType() {
    return entityType;
}
/**
 * Returns the named <code>IEntityGroup</code> from our members Collection.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException
{
    if ( ! isMembersInitialized() )
        { initializeMembers(); }

    IGroupMember gm;
    for ( Iterator itr=getMembers(); itr.hasNext(); )
    {
        gm = (IGroupMember) itr.next();
        if (gm.isGroup() && ((IEntityGroup)gm).getName().equals(name))
            { return (IEntityGroup)gm; }
    }
    return null;
}
/**
 * Returns an <code>Iterator</code> over the <code>GroupMembers</code> in our
 * member <code>Collection</code>.
 * @return java.util.Iterator
 */
public java.util.Iterator getMembers() throws GroupsException
{
    if ( ! isMembersInitialized() )
        { initializeMembers(); }

    return primGetMembers().values().iterator();
}
/**
 * @return java.lang.String
 */
public java.lang.String getName() {
    return name;
}
/**
 * @return java.util.HashMap
 */
protected HashMap getRemovedMembers()
{
    if ( this.removedMembers == null )
        this.removedMembers = new HashMap();
    return removedMembers;
}
/**
 * Returns this object's type for purposes of caching and locking, as
 * opposed to the underlying entity type.
 *
 * @return java.lang.Class
 */
public Class getType()
{
    return org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE;
}
/**
 * @return org.jasig.portal.IBasicEntity
 */
public IBasicEntity getUnderlyingEntity() {
    return this;
}
/**
 * Answers if there are any added memberships not yet committed to the database.
 * @return boolean
 */
protected boolean hasAdds()
{
    return (addedMembers != null) && (addedMembers.size() > 0);
}
/**
 * Answers if there are any deleted memberships not yet committed to the database.
 * @return boolean
 */
protected boolean hasDeletes()
{
    return (removedMembers != null) && (removedMembers.size() > 0);
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode()
{
    return getGroupID().hashCode() + getName().hashCode();
}
/**
 * @return boolean
 */
public boolean hasMembers() throws GroupsException
{
    return getMembers().hasNext();
}
/**
 * Cache the <code>IEntity</code> members.
 */
private void initializeMemberEntities() throws GroupsException
{
    Iterator entities = findMemberEntities();
    while ( entities.hasNext() )
    {
        IEntity ie = (IEntity) entities.next();
        primAddMember(ie);
    }
}
/**
 * Cache the <code>IEntityGroup</code> members.
 * @return org.jasig.portal.groups.IGroupMember
 * @param name java.lang.String
 */
private void initializeMemberGroups() throws GroupsException
{
    Iterator groups = this.findMemberGroups();
    while ( groups.hasNext() )
    {
        IEntityGroup ug = (IEntityGroup) groups.next();
        primAddMember(ug);
    }
}
/**
 * Cache my members.
 */
private void initializeMembers() throws GroupsException
{
    initializeMemberEntities();
    initializeMemberGroups();
    setMembersInitialized(true);
}
/**
 * Answers if there are any added or deleted memberships not yet committed to the database.
 * @return boolean
 */
protected boolean isDirty()
{
    return hasAdds() || hasDeletes();
}
/**
 * @return boolean
 */
public boolean isGroup()
{
    return true;
}
/**
 * @return boolean
 */
private boolean isMembersInitialized() {
    return membersInitialized;
}
/**
 * Adds <code>IGroupMember</code> gm to our member <code>Map</code> and adds
 * this to gm's group <code>Map</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IGroupMember
 */
protected void primAddMember(IGroupMember gm)
{
    GroupMemberImpl gmi = (GroupMemberImpl) gm;
    gmi.addGroup(this);
    primGetMembers().put(gmi.getCacheKey(), gmi);
}
/**
 * Returns the <code>Set</code> of <code>IEntities</code> in our member <code>Collection</code>
 * and, recursively, in the <code>Collections</code> of our members.
 * @param users java.lang.Set - a Set that IEntity-GroupMembers are added to.
 * @return java.util.Set
 */
protected java.util.Set primGetAllEntities(Set entities) throws GroupsException
{
    Iterator i = getMembers();
    while ( i.hasNext() )
    {
        GroupMemberImpl gmi = (GroupMemberImpl) i.next();
        if ( gmi.isEntity() )
            { entities.add(gmi); }
        else
            { ((EntityGroupImpl)gmi).primGetAllEntities(entities); }
    }
    return entities;
}
/**
 * Returns the <code>Set</code> of <code>IGroupMembers</code> in our member
 * <code>Collection</code> and, recursively, in the <code>Collections</code>
 * of our members.
 * @param s java.lang.Set - a Set that members are added to.
 * @return java.util.Set
 */
protected java.util.Set primGetAllMembers(Set s) throws GroupsException
{
    Iterator i = getMembers();
    while ( i.hasNext() )
    {
        GroupMemberImpl gmi = (GroupMemberImpl) i.next();
        s.add(gmi);
        if ( gmi.isGroup() )
            { ((EntityGroupImpl)gmi).primGetAllMembers(s); }
    }
    return s;
}
/**
 * @return java.util.HashMap
 */
private java.util.HashMap primGetMembers() {
    if ( this.members == null )
        this.members = new HashMap();
    return members;
}
/**
 * Removes <code>IGroupMember</code> gm from our member <code>Map</code> and
 * removes this from gm's group <code>Map</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IGroupMember
 */
protected void primRemoveMember(IGroupMember gm)
{
    GroupMemberImpl gmi = (GroupMemberImpl) gm;
    gmi.removeGroup(this);
    primGetMembers().remove(gmi.getCacheKey());
}
/**
 * @param newName java.lang.String
 */
void primSetName(java.lang.String newName)
{
    name = newName;
}
/**
 * Removes <code>IGroupMember</code> gm from our member <code>Map</code> and,
 * conversely, remove this from gm's group <code>Map</code>.  Remember that we
 * have removed it so we can update the database, if necessary.
 * @return void
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public void removeMember(IGroupMember gm)
{
    Object cacheKey = ((GroupMemberImpl)gm).getCacheKey();

    if ( getAddedMembers().containsKey(cacheKey) )
        { getAddedMembers().remove(cacheKey); }
    else
        { getRemovedMembers().put(cacheKey, gm); }

    primRemoveMember(gm);
}
/**
 * @param newCreatorID java.lang.String
 */
public void setCreatorID(java.lang.String newCreatorID) {
    creatorID = newCreatorID;
}
/**
 * @param newDescription java.lang.String
 */
public void setDescription(java.lang.String newDescription) {
    description = newDescription;
}
/**
 * @param newMembersInitialized boolean
 */
private void setMembersInitialized(boolean newMembersInitialized) {
    membersInitialized = newMembersInitialized;
}
/**
 * @Exception GroupsException is thrown if a sibling group with the same name already exists.
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) throws GroupsException
{
    Iterator i = getContainingGroups();
    while ( i.hasNext() )
    {
        EntityGroupImpl eg = (EntityGroupImpl) i.next();
        try
            { eg.checkProspectiveMemberGroupName(newName); }
        catch (GroupsException ge)
            {throw new GroupsException("Cannot set Group name: " + ge.getMessage());}
    }

    primSetName(newName);
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString()
{
    return "EntityGroupImpl (" + getKey() + ") "  + getName();
}
/**
 * Delegate to the factory.
 */
public void update() throws GroupsException
{
    try
        { getFactory().update(this); }
    catch (Exception ex)
        { throw new GroupsException(ex.toString()); }
    clearPendingUpdates();
}
/**
 * Delegate to the factory.
 */
public void updateMembers() throws GroupsException
{
    try
        { getFactory().updateMembers(this); }
    catch (Exception ex)
        { throw new GroupsException("Problem updating memberships for " + this + " " + ex); }
    clearPendingUpdates();
}
}
