/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.services;

import java.util.Iterator;
import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.groups.CompositeServiceIdentifier;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.ICompositeGroupServiceFactory;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.utils.threading.SingletonDoubleCheckedCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Bootstrap class for the IGroupService implementation.
 *
 * @author  Alex Vigdor
 * @author  Dan Ellentuck
 */

public final class GroupService implements IGroupConstants {

    private static final String GROUP_SERVICE_KEY = "org.jasig.portal.services.GroupService.key_";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    // Singleton instance of the bootstrap class:
    private static final SingletonDoubleCheckedCreator<GroupService> instance = new SingletonDoubleCheckedCreator<GroupService>() {
        @Override
        protected GroupService createSingleton(Object... args) {
            return new GroupService();
        }
    };

    // The group service:
    private ICompositeGroupService compositeGroupService = null;

    /** Creates new GroupService */
    private GroupService() throws GroupsException
    {
        super();
        initializeCompositeService();
    }

    /**
     * Returns the groups that contain the <code>IGroupMember</code>.
     * @param gm IGroupMember
     */
    public static Iterator findParentGroups(IGroupMember gm) throws GroupsException
    {
        LOGGER.trace("Invoking findParentGroups for IGroupMember [{}]", gm);
        return instance().ifindParentGroups(gm);
    }

    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if the
     * <code>IGroupMember</code> does not exist.
     * @param key String - the group key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup findGroup(String key) throws GroupsException
    {
        LOGGER.trace("Invoking findGroup for key='{}'", key);
        return instance().ifindGroup(key);
    }
    
    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     * @param key String - the group key.
     * @param lockOwner String - the owner of the lock, typically the user.
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    public static ILockableEntityGroup findLockableGroup(String key, String lockOwner)
    throws GroupsException
    {
        LOGGER.trace("Invoking findLockableGroup for key='{}', lockOwner='{}'", key, lockOwner);
        return instance().ifindLockableGroup(key, lockOwner);
    }

    /**
     * Receives notice that the UserInstance has been unbound from
     * the HttpSession.  In response, we remove the corresponding group member
     * from the cache.
     * @param person org.jasig.portal.security.IPerson
     */
    public static void finishedSession(IPerson person) 
    {
        LOGGER.trace("Invoking finishedSession for IPerson [{}]", person);
        try {
            instance().ifinishedSession(person);
        } catch (GroupsException ge) {
            LOGGER.error("Error upon session finishing for person [{}]", person, ge);
        }
    }

    /**
    * Returns the <code>ICompositeGroupService</code> implementation in use.
    * @return org.jasig.portal.groups.ICompositeGroupService
    */
    public static ICompositeGroupService getCompositeGroupService() throws GroupsException
    {
        LOGGER.trace("Invoking getCompositeGroupService()");
        return instance().compositeGroupService;
    }

    /**
     * @return java.lang.String
     */
    private String getDefaultServiceName() throws GroupsException {
        return (String) getServiceConfiguration().getAttributes().get(
                "defaultService");
    }
    /**
     * Refers to the PropertiesManager to get the key for the group
     * associated with 'name' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public static IEntityGroup getDistinguishedGroup(String name) throws GroupsException{
        LOGGER.trace("Invoking getDistinguishedGroup for name='{}'", name);
        return instance().igetDistinguishedGroup(name);
    }

    /**
     * @return java.lang.String
     */
    public String getDistinguishedGroupKey(String name) {
        return PropertiesManager.getProperty(GROUP_SERVICE_KEY + name, "");
    }

   /**
    * Returns an <code>IEntity</code> representing a portal entity.  This does
    * not guarantee that the entity actually exists.
    * @param key String - the group key.
    * @param type Class - the Class of the underlying IGroupMember.
    * @return org.jasig.portal.groups.IEntity
    */
    public static IEntity getEntity(String key, Class<?> type)
    throws GroupsException
    {
        return getEntity(key, type, null);
    }

    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does
     * not guarantee that the entity actually exists.
     * 
     * @param key
     *                String - the group key.
     * @param type
     *                Class - the Class of the underlying IGroupMember.
     * @param service
     *                String - the name of the component service.
     * @return org.jasig.portal.groups.IEntity
     */
    private static IEntity getEntity(String key, Class<?> type, String service)
            throws GroupsException {
        return instance().igetEntity(key, type, service);
    }

    /**
     * Returns an <code> IGroupMember </code> representing either a group or a
     * portal entity. If the parm <code> type </code> is the group type, the
     * <code> IGroupMember </code> is an <code> IEntityGroup </code> else it is
     * an <code> IEntity </code> .
     */
    public static IGroupMember getGroupMember(String key, Class<?> type) throws GroupsException
    {
        LOGGER.trace("Invoking getEntity for key='{}', type='{}'", key, type);
        return instance().igetGroupMember(key, type);
    }

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity, based on the <code>EntityIdentifier</code>, which
     * refers to the UNDERLYING entity for the <code>IGroupMember</code>.
     */
    public static IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
    throws GroupsException
    {
      return getGroupMember(underlyingEntityIdentifier.getKey(),
          underlyingEntityIdentifier.getType());
    }

    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public static IEntityGroup getRootGroup(Class<?> type) throws GroupsException{
        LOGGER.trace("Invoking getRootGroup for type='{}'", type);
        return instance().igetRootGroup(type);
    }

/**
 * @return java.lang.String
 */
private GroupServiceConfiguration getServiceConfiguration() throws GroupsException
{
    try
        { return GroupServiceConfiguration.getConfiguration(); }
    catch (Exception ex)
        { throw new GroupsException("Problem retrieving service configuration", ex);}
}

    /**
     * Returns the groups that contain the <code>IGroupMember</code>.
     * @param gm IGroupMember
     */
    private Iterator ifindParentGroups(IGroupMember gm) throws GroupsException
    {
        return compositeGroupService.findParentGroups(gm);
    }

    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if the
     * <code>IGroupMember</code> does not exist.
     * @param key String - the group key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup ifindGroup(String key) throws GroupsException
    {
        return compositeGroupService.findGroup(key);
    }
    
    /**
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     * @param key String - the group key.
     * @param lockOwner String - typically the user.
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    private ILockableEntityGroup ifindLockableGroup(String key, String lockOwner)
    throws GroupsException
    {
        return compositeGroupService.findGroupWithLock(key, lockOwner);
    }
    
    /**
     * Receives notice that the UserInstance has been unbound from the
     * HttpSession.  In response, we remove the corresponding group member from
     * the cache.  We use the roundabout route of creating a group member and 
     * then getting its EntityIdentifier because we need the EntityIdentifier 
     * for the group member, which is cached, not the EntityIdentifier for the 
     * IPerson, which is not.
     * @param person org.jasig.portal.security.IPerson
     */
    private void ifinishedSession(IPerson person) throws GroupsException
    {
        IGroupMember gm = getGroupMember(person.getEntityIdentifier()); 
        try
        { 
            final EntityIdentifier entityIdentifier = gm.getEntityIdentifier();
            EntityCachingService.getEntityCachingService().remove(entityIdentifier.getType(), entityIdentifier.getKey()); 
        }
        catch (CachingException ce)
        {
            throw new GroupsException("Problem removing group member " + gm.getKey() + " from cache", ce);
        }
    }

/**
 * Refers to the PropertiesManager to get the key for the group
 * associated with 'name' and asks the group store implementation for the corresponding
 * <code>IEntityGroup</code>.
 */
private IEntityGroup igetDistinguishedGroup(String name) throws GroupsException
{
    try
    {
        String key = getDistinguishedGroupKey(name);
        return compositeGroupService.findGroup(key);
    }
    catch (Exception ex){
    	throw new GroupsException("GroupService.getDistinguishedGroup(): "
            +"could not find key for: " + name,ex); 
    }
}

    /**
     * Returns an <code>IEntity</code> representing a pre-existing portal entity.
     * @param key String - the group key.
     * @param type Class - the Class of the underlying IGroupMember.
     * @return org.jasig.portal.groups.IEntity
     */
    private IEntity igetEntity(String key, Class<?> type, String service) throws GroupsException
    {
        return compositeGroupService.getEntity(key, type, service);
    }    
    
    /**
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity.  If the parm <code>type</code> is the group type,
     * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    private IGroupMember igetGroupMember(String key, Class<?> type) throws GroupsException
    {
        return compositeGroupService.getGroupMember(key, type);
    }
    
    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    private IEntityGroup igetRootGroup(Class<?> type) throws GroupsException
    {
        return igetDistinguishedGroup(type.getName());
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup inewGroup(Class<?> type) throws GroupsException
    {
        return inewGroup(type, getDefaultServiceName());
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    private IEntityGroup inewGroup(Class<?> type, String serviceName) throws GroupsException
    {
        try
            { return compositeGroupService.newGroup(type, parseServiceName(serviceName)); }
        catch (InvalidNameException ine)
            { throw new GroupsException("GroupService.inewGroup(): invalid service name", ine);}
    }

/**
 * @exception org.jasig.portal.groups.GroupsException
 */
private void initializeCompositeService() throws GroupsException
{
    String eMsg = null;
    try
    {
        GroupServiceConfiguration cfg = getServiceConfiguration();
        String factoryName = (String)cfg.getAttributes().get("compositeFactory");

        if ( factoryName == null )
        {
            eMsg = "GroupService.initialize(): No entry for CompositeServiceFactory in configuration";
            LOGGER.error(eMsg);
            throw new GroupsException(eMsg);
        }

        ICompositeGroupServiceFactory serviceFactory =
          (ICompositeGroupServiceFactory)Class.forName(factoryName).newInstance();
        compositeGroupService = serviceFactory.newGroupService();
    }
    catch (Exception e)
    {
        eMsg = "GroupService.initialize(): Problem creating groups service... " + e.getMessage();
        LOGGER.error(eMsg, e);
        throw new GroupsException(eMsg, e);
    }
}
    private static GroupService instance() throws GroupsException {
        return instance.get();
    }

    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup newGroup(Class<?> type) throws GroupsException {
        LOGGER.trace("Invoking newGroup for type='{}'", type);
        return instance().inewGroup(type);
    }

/**
 * Converts the String form of a service name into a Name.
 * @return javax.naming.Name
 * @exception InvalidNameException
 * @exception GroupsException
 */
public static Name parseServiceName(String serviceName)
throws InvalidNameException, GroupsException
{
    return new CompositeServiceIdentifier(serviceName).getServiceName();
}
  public static EntityIdentifier[] searchForEntities(String query, int method, Class<?> type) throws GroupsException {
    LOGGER.trace("Invoking searchForEntities for query='{}', method='{}', type='{}'", query, method, type);
    return instance().compositeGroupService.searchForEntities(query,method,type);
  }
  public static EntityIdentifier[] searchForEntities(String query, int method, Class<?> type, IEntityGroup ancestor) throws GroupsException {
    LOGGER.trace("Invoking searchForEntities for query='{}', method='{}', type='{}', ancestor='{}'", query, method, type, ancestor);
    return instance().compositeGroupService.searchForEntities(query,method,type,ancestor);
  }
  public static EntityIdentifier[] searchForGroups(String query, int method, Class<?> leaftype) throws GroupsException {
    LOGGER.trace("Invoking searchForGroups for query='{}', method='{}', leaftype='{}'", query, method, leaftype);
    return instance().compositeGroupService.searchForGroups(query,method,leaftype);
  }
  public static EntityIdentifier[] searchForGroups(String query, int method, Class<?> leaftype, IEntityGroup ancestor) throws GroupsException {
    LOGGER.trace("Invoking searchForGroups for query='{}', method='{}', leaftype='{}', ancestor='{}'", query, method, leaftype, ancestor);
    return instance().compositeGroupService.searchForGroups(query,method,leaftype,ancestor);
  }
}
