/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.GroupService;

/**
 * Reference implementation for <code>IEntityGroup</code>.
 * <p>
 * Groups do not keep references to their members but instead cache 
 * member keys.  The members are cached externally.  The rules
 * for controlling access to the key caches are a bit obscure, but you 
 * should understand them before writing code that updates groups.  
 * Access to the caches themselves is synchronized via the cache 
 * getters and setters.  All requests to get group members and to add or 
 * remove group members ultimately go through these methods.  The 
 * mutating methods, <code>addMember()</code> and <code>removeMember()</code> 
 * however, do a copy-on-write.  That is, they first make a copy of the 
 * cache, add or remove the member key, and then replace the original 
 * cache with the copy.  This permits multiple read and write threads to run 
 * concurrently without throwing <code>ConcurrentModificationExceptions</code>.  
 * But it still leaves open the danger of data races because nothing in 
 * this class guarantees serialized write access.  You must impose this 
 * from without, either via explicit locking (<code>GroupService.getLockableGroup()</code>) 
 * or by synchronizing access from the caller.  
 *  
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.groups.IEntityGroup
 * 
 * 
 */
public class EntityGroupImpl extends GroupMemberImpl implements IEntityGroup
{
    private String creatorID;
    private String name;
    private String description;
    protected IIndividualGroupService localGroupService;

    // A group and its members share an entityType.
    private java.lang.Class leafEntityType;

/*
 * The Sets of keys to the members of this group.  The <code>IGroupMembers</code>
 * themselves are cached by the service.
 */
    private Set memberGroupKeys;
    private Set memberEntityKeys;
    private boolean memberKeysInitialized;

/*
 * References to updated group members.  These updates do not become visible to
 * the members until the update is committed.
 */
    private HashMap addedMembers;
    private HashMap removedMembers;
/**
 * EntityGroupImpl
 */
public EntityGroupImpl(String groupKey, Class entityType) 
throws GroupsException
{
    super(new CompositeEntityIdentifier(groupKey, org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE));
    if ( isKnownEntityType(entityType) )
        { leafEntityType = entityType; }
    else
        { throw new GroupsException("Unknown entity type: " + entityType); }
}
/**
 * Adds <code>IGroupMember</code> gm to our member <code>Map</code> and conversely,
 * adds <code>this</code> to gm's group <code>Map</code>, after checking that the 
 * addition does not violate group rules.  Remember that we have added it so we can 
 * update the database if necessary.
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public void addMember(IGroupMember gm) throws GroupsException
{
    try
        { checkProspectiveMember(gm); }
    catch (GroupsException ge)
        { throw new GroupsException("Could not add IGroupMember", ge);}

    if ( ! this.contains( gm ) )
    {
        Object cacheKey = gm.getEntityIdentifier().getKey();

        if ( getRemovedMembers().containsKey(cacheKey) )
            { getRemovedMembers().remove(cacheKey); }
        else
            { getAddedMembers().put(cacheKey, gm); }

        if ( memberKeysInitialized )
            { primAddMember(gm); } 
    }
}
/**
 * @return boolean
 */
protected boolean areMemberKeysInitialized() {
    return memberKeysInitialized;
}
/**
 * A member must share the <code>entityType</code> of its containing <code>IEntityGroup</code>.
 * If it is a group, it must have a unique name within each of its containing groups and
 * the resulting group must not contain a circular reference.
 * Removed the requirement for unique group names.  (03-04-2004, de)
 * @param gm org.jasig.portal.groups.IGroupMember
 * @exception org.jasig.portal.groups.GroupsException
 */
private void checkProspectiveMember(IGroupMember gm) throws GroupsException
{
    if ( gm.equals(this) )
        { throw new GroupsException("Attempt to add " + gm + " to itself."); }

    // Type check:
    if ( this.getLeafType() != gm.getLeafType() )
        { throw new GroupsException(this + " and " + gm + " have different entity types."); } 

    // Circular reference check:
    if ( gm.isGroup() &&  gm.deepContains(this) )
        { throw new GroupsException("Adding " + gm + " to " + this + " creates a circular reference."); }
}
/**
 * Clear out caches for pending adds and deletes of group members.
 */
protected void clearPendingUpdates()
{
    addedMembers = null;
    removedMembers = null;
}
/**
 * Clone the member entity keys.
 * @return Set
 */
private Set copyMemberEntityKeys() throws GroupsException
{
   return castAndCopyHashSet(getMemberEntityKeys());
}
/**
 * Clone the member group keys.
 * @return Set
 */
private Set copyMemberGroupKeys() throws GroupsException
{
   return castAndCopyHashSet(getMemberGroupKeys());
}
/**
 * Checks if <code>GroupMember</code> gm is a member of this.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IGroupMember gm) throws GroupsException
{
    if ( areMemberKeysInitialized() )
    {
        Object cacheKey = gm.getKey();
        return getMemberGroupKeys().contains(cacheKey) ||
               getMemberEntityKeys().contains(cacheKey);
    }
    else
    {
        return gm.isMemberOf( this );
    }
}
/**
 * Checks recursively if <code>GroupMember</code> gm is a member of this.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean deepContains(IGroupMember gm) throws GroupsException
{
    if ( this.contains(gm) )
        { return true; }

    boolean found = false;
    Iterator it = getMemberGroups();
    while ( it.hasNext() && !found )
    {
        IEntityGroup group = (IEntityGroup) it.next();
        found = group.deepContains(gm);
    }

    return found;
}
/**
 * Delegates to the factory.
 */
public void delete() throws GroupsException
{
    getLocalGroupService().deleteGroup(this);
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
 * @return java.util.HashMap
 */
public HashMap getAddedMembers()
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
 * Returns the <code>EntityIdentifier</code> cast to a 
 * <code>CompositeEntityIdentifier</code> so that its service nodes
 * can be pushed and popped.
 *
 * @return CompositeEntityIdentifier
 */
protected CompositeEntityIdentifier getCompositeEntityIdentifier()
{
    return (CompositeEntityIdentifier)getEntityIdentifier();
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
    return getMemberEntities();
}
/**
 * @return EntityIdentifier
 */
public EntityIdentifier getEntityIdentifier() {
    return getUnderlyingEntityIdentifier();
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
 * Returns the entity type of this groups's leaf members.
 *
 * @return java.lang.Class
 * @see org.jasig.portal.EntityTypes
 */
 public java.lang.Class getEntityType() {
    return leafEntityType;
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
    return leafEntityType;
}
/**
 * @return IIndividualGroupService
 */
protected IIndividualGroupService getLocalGroupService() {
    return localGroupService;
}
/**
 * Returns the key from the group service of origin.
 * @return String
 */
public String getLocalKey()
{
    return getCompositeEntityIdentifier().getLocalKey();
}
/**
 * Returns an <code>Iterator</code> over the entities in our member
 * <code>Collection</code>.
 * @return java.util.Iterator
 */
protected java.util.Iterator getMemberEntities() throws GroupsException
{
    Collection members = new ArrayList();
    for ( Iterator i = getMemberEntityKeys().iterator(); i.hasNext(); )
    {
        String key = (String) i.next();
        members.add(getLocalGroupService().getEntity(key, getLeafType()));
    }
    return members.iterator();
}
/**
 * @return java.util.Set
 */
private synchronized Set getMemberEntityKeys() throws GroupsException{
    if ( ! areMemberKeysInitialized() )
        { initializeMembers(); }
    return memberEntityKeys;
}
/**
 * @return java.util.Set
 */
private synchronized Set getMemberGroupKeys() throws GroupsException {
    if ( ! areMemberKeysInitialized() )
        { initializeMembers(); }
    return memberGroupKeys;
}
/**
 * Returns the named member <code>IEntityGroup</code>.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException
{
    IGroupMember gm;
    for ( Iterator itr=getMemberGroups(); itr.hasNext(); )
    {
        gm = (IGroupMember) itr.next();
        if ( ((IEntityGroup)gm).getName().equals(name) )
            { return (IEntityGroup)gm; }
    }
    return null;
}
/**
 * Returns an <code>Iterator</code> over the groups in our member
 * <code>Collection</code>.
 * @return java.util.Iterator
 */
protected java.util.Iterator getMemberGroups() throws GroupsException
{
    Collection members = new ArrayList();
    for ( Iterator i = getMemberGroupKeys().iterator(); i.hasNext(); )
    {
        String key = (String) i.next();
        members.add(GroupService.findGroup(key));
    }
    return members.iterator();
}
/**
 * Returns an <code>Iterator</code> over the <code>GroupMembers</code> in our
 * member <code>Collection</code>.
 * @return java.util.Iterator
 */
public java.util.Iterator getMembers() throws GroupsException
{
    Collection members = new ArrayList(100);
    Iterator itr = null;

    for ( itr = getMemberGroups(); itr.hasNext(); )
        { members.add(itr.next()); }
    for ( itr = getMemberEntities(); itr.hasNext(); )
        { members.add(itr.next()); }

    return members.iterator();
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
public HashMap getRemovedMembers()
{
    if ( this.removedMembers == null )
        this.removedMembers = new HashMap();
    return removedMembers;
}
/**
 * @return IGroupService
 */
protected GroupService getService() throws GroupsException {
    return GroupService.instance();
}
/**
 * Returns the Name of the group service of origin.
 * @return javax.naming.Nme
 */
public Name getServiceName()
{
    return getCompositeEntityIdentifier().getServiceName();
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
 * Answers if there are any added memberships not yet committed to the database.
 * @return boolean
 */
public boolean hasAdds()
{
    return (addedMembers != null) && (addedMembers.size() > 0);
}
/**
 * Answers if there are any deleted memberships not yet committed to the database.
 * @return boolean
 */
public boolean hasDeletes()
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
    return getKey().hashCode();
}
/**
 * @return boolean
 */
public boolean hasMembers() throws GroupsException
{
    return getMembers().hasNext();
}
/**
 * Cache the <code>IEntityGroup</code> members.
 */
private void initializeMembers() throws GroupsException
{
    Set groupKeys = new HashSet();
    Set entityKeys = new HashSet(100);
    
    for ( Iterator it = getLocalGroupService().findMembers(this); it.hasNext(); )
    {
        IGroupMember gm = (IGroupMember) it.next();
        Set cache = ( gm.isGroup() ) ? groupKeys : entityKeys;
        cache.add(gm.getKey());
    }
    setMemberEntityKeys(entityKeys);
    setMemberGroupKeys(groupKeys);
    setMemberKeysInitialized(true);
}
/**
 * Answers if there are any added or deleted memberships not yet committed to the database.
 * @return boolean
 */
public boolean isDirty()
{
    return hasAdds() || hasDeletes();
}
/**
 * Answers if this <code>IEntityGroup</code> can be changed or deleted.
 * @return boolean
 * @exception GroupsException
 */
public boolean isEditable() throws GroupsException
{
    return getLocalGroupService().isEditable(this);
}
/**
 * @return boolean
 */
public boolean isGroup()
{
    return true;
}
/**
 * Adds the <code>IGroupMember</code> key to the appropriate member key
 * cache by copying the cache, adding to the copy, and then replacing the
 * original with the copy.  At this point, <code>gm</code> does not yet 
 * have <code>this</code> in its containing group cache.  That cache entry 
 * is not added until update(), when changes are committed to the store.
 * @param gm org.jasig.portal.groups.IGroupMember
 */
protected void primAddMember(IGroupMember gm) throws GroupsException
{
    Set cache = (gm.isGroup()) ? copyMemberGroupKeys() : copyMemberEntityKeys();
    cache.add(gm.getKey());
    if ( gm.isGroup() )
        setMemberGroupKeys(cache);
    else
        setMemberEntityKeys(cache);
}
/**
 * Returns the <code>Set</code> of <code>IEntities</code> in our member <code>Collection</code>
 * and, recursively, in the <code>Collections</code> of our members.
 * @param entities a Set that IEntity-GroupMembers are added to.
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
 * Removes the <code>IGroupMember</code> key from the appropriate key cache, by
 * copying the cache, removing the key from the copy and replacing the original
 * with the copy.  At this point, <code>gm</code> still has <code>this</code> 
 * in its containing groups cache.  That cache entry is not removed until update(), 
 * when changes are committed to the store.  
 * @param gm org.jasig.portal.groups.IGroupMember
 */
protected void primRemoveMember(IGroupMember gm) throws GroupsException
{
    Set cache = (gm.isGroup()) ? copyMemberGroupKeys() : copyMemberEntityKeys();
    cache.remove(gm.getKey());
    if ( gm.isGroup() )
        setMemberGroupKeys(cache);
    else
        setMemberEntityKeys(cache);
}
/**
 * @param newName java.lang.String
 */
public void primSetName(java.lang.String newName)
{
    name = newName;
}
/**
 * Removes <code>IGroupMember</code> gm from our member <code>Map</code> and,
 * conversely, remove this from gm's group <code>Map</code>.  Remember that we
 * have removed it so we can update the database, if necessary.
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public void removeMember(IGroupMember gm) throws GroupsException
{
    Object cacheKey = gm.getEntityIdentifier().getKey();

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
 * @param newIndividualGroupService IIndividualGroupService
 */
public void setLocalGroupService(IIndividualGroupService newIndividualGroupService)
throws GroupsException
{
    localGroupService = newIndividualGroupService;
    setServiceName(localGroupService.getServiceName());
}
/**
 * @param newMemberKeysInitialized boolean
 */
private void setMemberKeysInitialized(boolean newMemberKeysInitialized) {
    memberKeysInitialized = newMemberKeysInitialized;
}
/**
 * @param newMemberEntityKeys Set
 */
private synchronized void setMemberEntityKeys(Set newMemberEntityKeys)
{
    memberEntityKeys = newMemberEntityKeys;
}
/**
 * @param newMemberGroupKeys Set
 */
private synchronized void setMemberGroupKeys(Set newMemberGroupKeys)
{
    memberGroupKeys = newMemberGroupKeys;
}
/**
 * We used to check duplicate sibling names but no longer do.  
 * @param newName java.lang.String
 */
public void setName(java.lang.String newName) throws GroupsException
{
    primSetName(newName);
}
/**
 * Sets the service Name of the group service of origin.
 */
public void setServiceName(Name newServiceName) throws GroupsException
{
    try 
        { getCompositeEntityIdentifier().setServiceName(newServiceName); }
    catch (javax.naming.InvalidNameException ine)
        { throw new GroupsException("Problem setting service name", ine); }

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
    getLocalGroupService().updateGroup(this);
    clearPendingUpdates();
}
/**
 * Delegate to the factory.
 */
public void updateMembers() throws GroupsException
{
    getLocalGroupService().updateGroupMembers(this);
    clearPendingUpdates();
}
}
