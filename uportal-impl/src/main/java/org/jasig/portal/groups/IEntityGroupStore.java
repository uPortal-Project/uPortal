/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;

/**
 * Interface for finding and maintaining <code>IEntityGroups</code>.
 * @author Dan Ellentuck
 * @version 1.0, 11/29/01
 */
public interface IEntityGroupStore extends IGroupConstants
{
/**
 * Answers if <code>group</code> contains <code>member</code>.
 * @return boolean
 * @param group org.jasig.portal.groups.IEntityGroup
 * @param member org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException;
/**
 * Delete this <code>IEntityGroup</code> from the data store.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void delete(IEntityGroup group) throws GroupsException;
/**
 * Returns an instance of the <code>IEntityGroup</code> from the data store.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.String
 */
public IEntityGroup find(String key) throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
 * @return java.util.Iterator
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public Iterator findContainingGroups(IGroupMember gm) throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntities</code> that are members of this <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException;
/**
 * Returns an instance of the <code>ILockableEntityGroup</code> from the data store.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.String
 */
public ILockableEntityGroup findLockable(String key) throws GroupsException;
/**
 * Returns a <code>String[]</code> containing the keys of  <code>IEntityGroups</code>
 * that are members of this <code>IEntityGroup</code>.  In a composite group
 * system, a group may contain a member group from a different service.  This is
 * called a foreign membership, and is only possible in an internally-managed
 * service.  A group store in such a service can return the key of a foreign member
 * group, but not the group itself, which can only be returned by its local store.
 *
 * @return String[]
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException;
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that are members of this <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public Iterator findMemberGroups(IEntityGroup group) throws GroupsException;
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
public IEntityGroup newInstance(Class entityType) throws GroupsException;
/**
 * Find EntityIdentifiers for groups whose name matches the query string
 * according to the specified method and matches the provided leaf type
 */
public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException;
/**
 * Adds or updates the <code>IEntityGroup</code> AND ITS MEMBERSHIPS to the
 * data store, as appropriate.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void update(IEntityGroup group) throws GroupsException;
/**
 * Commits the group memberships of the <code>IEntityGroup</code> to
 * the data store.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void updateMembers(IEntityGroup group) throws GroupsException;
}
