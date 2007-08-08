/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

/**
 * Extends IGroupService with methods for finding and maintaining 
 * <code>ILockableEntityGroups</code>.  
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface ILockableGroupService extends IGroupService {

  /**
   * Removes the <code>ILockableEntityGroup</code> from the store.
   */
  public void deleteGroup(ILockableEntityGroup group) throws GroupsException;
  /**
   * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
   * group is not found.
   * @return org.jasig.portal.groups.ILockableEntityGroup
   * @param key String - the group key.
   * @param owner String - the lock owner.
   */
  public ILockableEntityGroup findGroupWithLock(String key, String owner) 
  throws GroupsException;
  /**
   * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
   * group is not found.
   * @return org.jasig.portal.groups.ILockableEntityGroup
   * @param key String - the group key.
   * @param owner String - the lock owner.
   * @param durationSecs int - the duration of the lock in seconds.
   */
  public ILockableEntityGroup findGroupWithLock(String key, String owner, int durationSecs) 
  throws GroupsException;
  /**
   * Commits the updated <code>ILockableEntityGroup</code> to the store.
   */
  public void updateGroup(ILockableEntityGroup group) 
  throws GroupsException;
  /**
   * Commits the updated <code>ILockableEntityGroup</code> to the store.
   */
  public void updateGroupMembers(ILockableEntityGroup group) 
  throws GroupsException;
  /**
   * Commits the updated <code>ILockableEntityGroup</code> to the store and
   * renews the lock.
   */
  public void updateGroup(ILockableEntityGroup group, boolean renewLock) 
  throws GroupsException;
  /**
   * Commits the updated <code>ILockableEntityGroup</code> to the store and 
   * renews the lock.
   */
  public void updateGroupMembers(ILockableEntityGroup group, boolean renewLock) 
  throws GroupsException;
}
