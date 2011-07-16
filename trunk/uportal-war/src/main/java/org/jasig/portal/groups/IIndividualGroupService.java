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
