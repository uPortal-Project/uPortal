/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
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
  public void updateGroup(ILockableEntityGroup group) throws GroupsException;
  /**
   * Commits the updated <code>ILockableEntityGroup</code> to the store.
   */
  public void updateGroupMembers(ILockableEntityGroup group) throws GroupsException;
}
