/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;

/**
 * Defines an api for discovering an entry point into the groups system,
 * represented by an <code>IGroupMember</code>.  This is analogous to getting an
 * <code>InitialContext</code> in JNDI.  Subsequent requests for navigating or
 * maintaining groups go thru the <code>IGroupMember</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IGroupService {

  /**
   * Returns a pre-existing <code>IEntityGroup</code> or null if the
   * <code>IGroupMember</code> does not exist.
   */
  public IEntityGroup findGroup(String key) throws GroupsException;

  /**
   * Returns an <code>IEntity</code> representing a portal entity.  This does
   * not guarantee that the entity actually exists.
   */
  public IEntity getEntity(String key, Class type) throws GroupsException;

  /**
   * Returns an <code>IGroupMember</code> representing either a group or a
   * portal entity.  If the parm <code>type</code> is the group type,
   * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
   * an <code>IEntity</code>.
   */
  public IGroupMember getGroupMember(String key, Class type) throws GroupsException;

  /**
   * Returns an <code>IGroupMember</code> representing either a group or a
   * portal entity, based on the <code>EntityIdentifier</code>, which
   * refers to the UNDERLYING entity for the <code>IGroupMember</code>.
   */
  public IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
  throws GroupsException;

  /**
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key.
   */
  public IEntityGroup newGroup(Class type) throws GroupsException;

  /**
   * Returns an <code>IEntityGroupStore</code>.
   */
  public IEntityGroupStore getGroupStore() throws GroupsException;

  /**
   * Removes the <code>IEntityGroup</code> from the store.
   */
  public void deleteGroup(IEntityGroup group) throws GroupsException;

  /**
   * Commits the updated <code>IEntityGroup</code> to the store.
   */
  public void updateGroup(IEntityGroup group) throws GroupsException;

  /**
   * Commits the updated <code>IEntityGroup</code> to the store.
   */
  public void updateGroupMembers(IEntityGroup group) throws GroupsException;

  /**
   * Returns the containing groups for the <code>IGroupMember</code>
   * @param gm IGroupMember
   */
  public Iterator findContainingGroups(IGroupMember gm) throws GroupsException;

  /**
   * Returns the member groups for the <code>IEntityGroup</code>
   * @param eg IEntityGroup
   */
  public Iterator findMemberGroups(IEntityGroup eg) throws GroupsException;

  /**
   * Find EntityIdentifiers for groups whose name matches the query string
   * according to the specified method and matches the provided leaf type
   */
  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException;

  /**
   * Find EntityIdentifiers for groups whose name matches the query string
   * according to the specified method, has the provided leaf type  and
   * descends from the specified group
   */
  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype, IEntityGroup ancestor) throws GroupsException;

  /**
   * Find EntityIdentifiers for entities whose name matches the query string
   * according to the specified method and is of the specified type
   */
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException;

  /**
   * Find EntityIdentifiers for entities whose name matches the query string
   * according to the specified method, is of the specified type  and
   * descends from the specified group
   */
  public EntityIdentifier[] searchForEntities(String query, int method, Class type, IEntityGroup ancestor) throws GroupsException;

}