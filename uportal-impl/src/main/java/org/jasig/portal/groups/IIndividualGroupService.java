/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Iterator;
/**
 * Defines a component group service that finds and maintains
 * <code>IGroupMembers</code> within a composite group service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IIndividualGroupService extends ICompositeGroupService {

  /**
   * Answers if <code>group</code> contains <code>member</code>.
   * @return boolean
   * @param group org.jasig.portal.groups.IEntityGroup
   * @param member org.jasig.portal.groups.IGroupMember
   */
  public boolean contains(IEntityGroup group, IGroupMember member) throws GroupsException;
  /**
   * Removes the <code>IEntityGroup</code> from the store.
   */
  public void deleteGroup(IEntityGroup group) throws GroupsException;
  /**
   * Returns a preexisting <code>IEntityGroup</code> from the store.
   * @param ent CompositeEntityIdentifier
   */
  public IEntityGroup findGroup(CompositeEntityIdentifier ent)
  throws GroupsException;
  /**
   * Returns an <code>Iterator</code> over the members of <code>group</code>.
   * @param group IEntityGroup
   */
  public Iterator findMembers(IEntityGroup group) throws GroupsException;
  /**
   * Answers if the group can be updated or deleted in the store.
   * @param group IEntityGroup
   */
  public boolean isEditable(IEntityGroup group) throws GroupsException;
  /**
   * Answers if the service can be updated by the portal.
   */
  public boolean isEditable();
  /**
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key.
   */
  public IEntityGroup newGroup(Class type) throws GroupsException;
  /**
   * Commits the updated <code>IEntityGroup</code> and its memberships to the
   * store.
   * @param group IEntityGroup
   */
  public void updateGroup(IEntityGroup group) throws GroupsException;
  /**
   * Commits the updated group memberships to the store.
   * @param group IEntityGroup
   */
  public void updateGroupMembers(IEntityGroup group) throws GroupsException;
}
