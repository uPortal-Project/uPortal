/**
 * Copyright (c) 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout.restrictions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.RDBMEntityStore;
import org.jasig.portal.services.LogService;

/**
 * The UserLayout node group service.
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public class NodeGroupService implements IGroupService {

    // Singleton instance:
    private static IGroupService groupService = null;

    // Factories for IGroupMembers:
    private IEntityStore entityStore = null;
    private IEntityGroupStore groupStore = null;

    /**
     * ReferenceGroupsService constructor.
     */
    private NodeGroupService() throws GroupsException {
            super();
            initialize();
    }

    /**
     * Removes the <code>IEntityGroup</code> from the cache and the store.
     * @param group IEntityGroup
     */
    public void deleteGroup(IEntityGroup group) throws GroupsException
    {
        getGroupStore().delete(group);
    }

    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if it
     * does not exist.
     */
    public IEntityGroup findGroup(String key) throws GroupsException {
      return groupStore.find(key);
    }

   /**
     * Returns the containg groups for the <code>IGroupMember</code>
     * @param gm IGroupMember
     */
    public Iterator findContainingGroups(IGroupMember gm) throws GroupsException
    {
        Collection groups = new ArrayList(10);
        IEntityGroup group = null;
        for ( Iterator it = getGroupStore().findContainingGroups(gm); it.hasNext(); )
        {
            group = (IEntityGroup) it.next();
            groups.add(group);
        }
        return groups.iterator();
    }

    /**
     * Returns the member groups for the <code>IEntityGroup</code>
     * @param eg IEntityGroup
     */
    public Iterator findMemberGroups(IEntityGroup eg) throws GroupsException
    {
        Collection groups = new ArrayList(10);
        IEntityGroup group = null;
        for ( Iterator it = getGroupStore().findMemberGroups(eg); it.hasNext(); )
        {
            group = (IEntityGroup) it.next();
            groups.add(group);
        }
        return groups.iterator();
    }

    /**
     * Updates the cache and the store with the new <code>IEntityGroup</code>.
     * @param group IEntityGroup
     */
    public void updateGroup(IEntityGroup group) throws GroupsException
    {
        getGroupStore().update(group);
    }

    /**
     * Synchronizes the store with the updated <code>IEntityGroup</code>.
     * @param group IEntityGroup
     */
    public void updateGroupMembers(IEntityGroup group) throws GroupsException
    {
        getGroupStore().updateMembers(group);
    }
    /**
     * Returns an <code>IEntity</code> representing a portal entity.  This does
     * not guarantee that the entity actually exists.
     */
    public IEntity getEntity(String key, Class type) throws GroupsException {
      return entityStore.newInstance(key, type);
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity.  If the parm <code>type</code> is the group type,
     * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    public IGroupMember getGroupMember(String key, Class type) throws GroupsException {
      IGroupMember gm = null;
      if ( type == EntityTypes.GROUP_ENTITY_TYPE )
        gm = findGroup(key);
      else
        gm = getEntity(key, type);
      return gm;
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity, based on the <code>EntityIdentifier</code>, which
     * refers to the UNDERLYING entity for the <code>IGroupMember</code>.
     */
    public IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
    throws GroupsException
    {
      return getGroupMember(underlyingEntityIdentifier.getKey(),
          underlyingEntityIdentifier.getType());
    }

     /**
     * Refers to the PropertiesManager to get the key for the group
     * associated with 'name' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public IEntityGroup getDistinguishedGroup(String name) throws GroupsException {
      String key = PropertiesManager.getProperty("org.jasig.portal.groups.ReferenceGroupService.key_"+name);
      if (key != null){
        return findGroup(key);
      }
      else {
        throw new GroupsException("ReferenceGroupService.getDistinguishedGroup(): no key found to match requested name");
      }
    }

    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public IEntityGroup getRootGroup(Class type) throws GroupsException {
      return getDistinguishedGroup(type.getName());
    }

    /**
     * @exception org.jasig.portal.groups.GroupsException.
     */
    private void initialize() throws GroupsException {
      String eMsg;
      entityStore = new RDBMEntityStore();

      String groupStoreName = PropertiesManager.getProperty ("org.jasig.portal.groups.EntityGroupFactory.implementation");

      if ( groupStoreName == null ) {
          eMsg = "NodeGroupService.initialize(): EntityGroupStoreImpl not specified in portal.properties";
          LogService.log(LogService.ERROR, eMsg);
          throw new GroupsException(eMsg);
      }

      try {
          groupStore = (IEntityGroupStore) Class.forName(groupStoreName).newInstance();
      } catch (Exception e) {
          eMsg = "NodeGroupService.initialize(): Failed to instantiate " + groupStoreName + " " + e;
          LogService.log(LogService.ERROR, eMsg);
          throw new GroupsException(eMsg);
      }

    }

    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     */
    public IEntityGroup newGroup(Class type) throws GroupsException {
      return groupStore.newInstance(type);
    }

    /**
     * @return org.jasig.portal.groups.IGroupService
     * @exception org.jasig.portal.groups.GroupsException
     */
    public static synchronized IGroupService getInstance() throws GroupsException {
      if ( groupService == null )
          groupService = new NodeGroupService();
      return groupService;
    }

   /**
     * Returns the implementation of <code>IEntityGroupStore</code> in use.
     */
  public IEntityGroupStore getGroupStore() throws GroupsException
    {
        return groupStore;
    }
  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupService method*/
    throw new java.lang.UnsupportedOperationException("Method searchForGroups() not yet implemented.");
  }
  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype, IEntityGroup ancestor) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupService method*/
    throw new java.lang.UnsupportedOperationException("Method searchForGroups() not yet implemented.");
  }
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupService method*/
    throw new java.lang.UnsupportedOperationException("Method searchForEntities() not yet implemented.");
  }
  public EntityIdentifier[] searchForEntities(String query, int method, Class type, IEntityGroup ancestor) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupService method*/
    throw new java.lang.UnsupportedOperationException("Method searchForEntities() not yet implemented.");
  }
}
