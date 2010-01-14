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

import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;

/**
 * Defines an api for discovering entry points into a composite groups system
 * consisting of component group services.  These entry points are represented
 * by <code>IGroupMembers</code>.  The role of the <code>IGroupMember</code>is 
 * somewhat analogous to that of an <code>InitialContext</code> in JNDI.  Once
 * a client gets an <code>IGroupMember</code>, subsequent requests for navigating
 * the system or maintaining groups go thru the <code>IGroupMember</code> api 
 * and are serviced by the individual component services.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface ICompositeGroupService extends IComponentGroupService {

  /**
   * Returns the groups that contain the <code>IGroupMember</code>.
   * @param gm IGroupMember
   */
  public Iterator findContainingGroups(IGroupMember gm) throws GroupsException;
  /**
   * Returns a pre-existing <code>IEntityGroup</code> or null if it does not
   * exist.
   */
  public IEntityGroup findGroup(String key) throws GroupsException;
  /**
   * Returns a pre-existing <code>IEntityGroup</code> or null if it does not
   * exist.
   */
  public ILockableEntityGroup findGroupWithLock(String key, String owner) 
  throws GroupsException;
  /**
   * Returns an <code>IEntity</code> representing a portal entity.  This does
   * not guarantee that the entity actually exists.
   */
  public IEntity getEntity(String key, Class type) throws GroupsException;
  /**
   * Returns an <code>IEntity</code> representing a portal entity.  This does
   * not guarantee that the entity actually exists.
   */
  public IEntity getEntity(String key, Class type, String service) throws GroupsException;
  /**
   * Returns an <code>IGroupMember</code> representing either a group or a
   * portal entity.  If the parm <code>type</code> is the group type,
   * the <code>IGroupMember</code> is an <code>IEntityGroup</code>.  Otherwise
   * it is an <code>IEntity</code>.
   */
  public IGroupMember getGroupMember(String key, Class type) throws GroupsException;
  /**
   * Returns an <code>IGroupMember</code> representing either a group or a
   * portal entity, based on the <code>EntityIdentifier</code>, which refers
   * to the UNDERLYING entity for the <code>IGroupMember</code>.
   */
  public IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
  throws GroupsException;
  /**
   * Returns a new <code>IEntityGroup</code> for the given Class with an unused
   * key from the named service.
   */
  public IEntityGroup newGroup(Class type, Name serviceName) throws GroupsException;
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
}
