/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
