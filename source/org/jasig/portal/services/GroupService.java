/* Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.services;

import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.groups.CompositeEntityIdentifier;
import org.jasig.portal.groups.CompositeServiceIdentifier;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.ICompositeGroupServiceFactory;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IGroupService;
import org.jasig.portal.groups.IGroupServiceFactory;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IPerson;

/**
 *  Bootstrap class for the IGroupService implementation.
 *
 * @author  Alex Vigdor
 * @author  Dan Ellentuck
 * @version $Revision$
 */

public class GroupService implements IGroupConstants
{
    // Singleton instance of the bootstrap class:
    private static GroupService instance = null;

    // Switch for composite/simple service
    private static boolean composite;

    // The group service:
    private IGroupService groupService = null;
    private ICompositeGroupService compositeGroupService = null;

    protected static final String GROUP_SERVICE_KEY = "org.jasig.portal.services.GroupService.key_";
    /** Creates new GroupService */
    private GroupService() throws GroupsException
    {
        super();
        initializeCompositeService();
    }
    /*
     * Returns a pre-existing <code>IEntityGroup</code> or null if the
     * <code>IGroupMember</code> does not exist.
     * @param key String - the group key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup findGroup(String key) throws GroupsException
    {
        return instance().ifindGroup(key);
    }
    /*
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     * @param key String - the group key.
     * @param lockOwner String - the owner of the lock, typically the user.
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    public static ILockableEntityGroup findLockableGroup(String key, String lockOwner)
    throws GroupsException
    {
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
        try 
            { instance().ifinishedSession(person); }
        catch (GroupsException ge)
            { LogService.log(LogService.ERROR, ge); }
    }
    /*
    * Returns the <code>ICompositeGroupService</code> implementation in use.
    * @return org.jasig.portal.groups.ICompositeGroupService
    */
    public static ICompositeGroupService getCompositeGroupService() throws GroupsException
    {
        return instance().compositeGroupService;
    }
/**
 * @return java.lang.String
 */
protected String getDefaultServiceName() throws GroupsException
{
    return (String)getServiceConfiguration().getAttributes().get("defaultService");
}
    /**
     * Refers to the PropertiesManager to get the key for the group
     * associated with 'name' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public static IEntityGroup getDistinguishedGroup(String name) throws GroupsException{
      return instance().igetDistinguishedGroup(name);
    }
/**
 * @return java.lang.String
 */
public String getDistinguishedGroupKey(String name)
{
    return PropertiesManager.getProperty(GROUP_SERVICE_KEY + name);
}
   /*
    * Returns an <code>IEntity</code> representing a portal entity.  This does
    * not guarantee that the entity actually exists.
    * @param key String - the group key.
    * @param type Class - the Class of the underlying IGroupMember.
    * @return org.jasig.portal.groups.IEntity
    */
    public static IEntity getEntity(String key, Class type)
    throws GroupsException
    {
        return getEntity(key, type, null);
   
    }
    /*
        * Returns an <code>IEntity</code> representing a portal entity.  This does
        * not guarantee that the entity actually exists.
        * @param key String - the group key.
        * @param type Class - the Class of the underlying IGroupMember.
        * @param service String - the name of the component service.
        * @return org.jasig.portal.groups.IEntity
        */
    public static IEntity getEntity(String key, Class type, String service)
    throws GroupsException
    {
        return instance().igetEntity(key, type, service);
    }
    /*
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity.  If the parm <code>type</code> is the group type,
     * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    public static IGroupMember getGroupMember(String key, Class type) throws GroupsException
    {
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
    /*
    * Returns the <code>IGroupService</code> implementation in use.
    * @return org.jasig.portal.groups.IGroupService
    */
    public static IGroupService getGroupService() throws GroupsException
    {
        return instance().groupService;
    }
    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public static IEntityGroup getRootGroup(Class type) throws GroupsException{
      return instance().igetRootGroup(type);
    }
/**
 * @return java.lang.String
 */
protected GroupServiceConfiguration getServiceConfiguration() throws GroupsException
{
    try
        { return GroupServiceConfiguration.getConfiguration(); }
    catch (Exception ex)
        { throw new GroupsException("Problem retrieving service configuration: " + ex.getMessage());}
}
    /*
     * Returns a pre-existing <code>IEntityGroup</code> or null if the
     * <code>IGroupMember</code> does not exist.
     * @param key String - the group key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup ifindGroup(String key) throws GroupsException
    {
        return compositeGroupService.findGroup(key);
    }
    /*
     * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
     * group is not found.
     * @param key String - the group key.
     * @param lockOwner String - typically the user.
     * @return org.jasig.portal.groups.ILockableEntityGroup
     */
    protected ILockableEntityGroup ifindLockableGroup(String key, String lockOwner)
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
    protected void ifinishedSession(IPerson person) throws GroupsException
    {
        IGroupMember gm = getGroupMember(person.getEntityIdentifier()); 
        try
        { 
            EntityCachingService.instance().remove(gm.getEntityIdentifier()); 
        }
        catch (CachingException ce)
        {
            throw new GroupsException("Problem removing group member " + gm.getKey() + " from cache: " + ce.getMessage());
        }
    }

/**
 * Refers to the PropertiesManager to get the key for the group
 * associated with 'name' and asks the group store implementation for the corresponding
 * <code>IEntityGroup</code>.
 */
protected IEntityGroup igetDistinguishedGroup(String name) throws GroupsException
{
    try
    {
        String key = getDistinguishedGroupKey(name);
        return compositeGroupService.findGroup(key);
    }
    catch (Exception ex)
        { throw new GroupsException("GroupService.getDistinguishedGroup(): could not find key for: " + name); }}
    /*
     * Returns an <code>IEntity</code> representing a pre-existing portal entity.
     * @param key String - the group key.
     * @param type Class - the Class of the underlying IGroupMember.
     * @return org.jasig.portal.groups.IEntity
     */
    protected IEntity igetEntity(String key, Class type, String service) throws GroupsException
    {
        return compositeGroupService.getEntity(key, type, service);
    }    
    /*
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity.  If the parm <code>type</code> is the group type,
     * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    protected IGroupMember igetGroupMember(String key, Class type) throws GroupsException
    {
        return compositeGroupService.getGroupMember(key, type);
    }
    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    protected IEntityGroup igetRootGroup(Class type) throws GroupsException
    {
        return igetDistinguishedGroup(type.getName());
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup inewGroup(Class type) throws GroupsException
    {
        return inewGroup(type, getDefaultServiceName());
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup inewGroup(Class type, String serviceName) throws GroupsException
    {
        try
            { return compositeGroupService.newGroup(type, parseServiceName(serviceName)); }
        catch (InvalidNameException ine)
            { throw new GroupsException("GroupService.inewGroup(): invalid service name: " + ine.getMessage());}
    }
    /**
     * @exception org.jasig.portal.groups.GroupsException
     */
    private void initialize() throws GroupsException
    {
      String eMsg = null;
      String factoryName =
        PropertiesManager.getProperty("org.jasig.portal.groups.GroupServiceFactory");

      if ( factoryName == null )
      {
          eMsg = "GroupService.initialize(): No entry for org.jasig.portal.groups.GroupServiceFactory in portal.properties.";
          LogService.log(LogService.ERROR, eMsg);
          throw new GroupsException(eMsg);
      }

      try
      {
          IGroupServiceFactory groupServiceFactory =
              (IGroupServiceFactory)Class.forName(factoryName).newInstance();
          groupService = groupServiceFactory.newGroupService();
      }
      catch (Exception e)
      {
          eMsg = "GroupService.initialize(): Problem creating groups service... " + e.getMessage();
          LogService.log(LogService.ERROR, eMsg);
          throw new GroupsException(eMsg);
      }
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
            LogService.log(LogService.ERROR, eMsg);
            throw new GroupsException(eMsg);
        }

        ICompositeGroupServiceFactory serviceFactory =
          (ICompositeGroupServiceFactory)Class.forName(factoryName).newInstance();
        compositeGroupService = serviceFactory.newGroupService();
    }
    catch (Exception e)
    {
        eMsg = "GroupService.initialize(): Problem creating groups service... " + e.getMessage();
        LogService.log(LogService.ERROR, eMsg);
        throw new GroupsException(eMsg);
    }
}
    public static synchronized GroupService instance() throws GroupsException {
        if ( instance==null ) {
            instance = new GroupService();
        }
        return instance;
    }
    /*
     * Answer if the underlying group service is a composite service,
     * implementing ICompositeGroupService.
     */
    public static boolean isComposite()
    {
        return composite;
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup newGroup(Class type) throws GroupsException {
        return instance().inewGroup(type);
    }
    /**
     * Returns a new <code>IEntityGroup</code> for the given Class with an unused
     * key.
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup newGroup(Class type, String serviceName) throws GroupsException {
        return instance().inewGroup(type, serviceName);
    }
/**
 * Extracts the final node from the String form of a composite key.
 * @return String
 * @exception javax.naming.InvalidNameException
 */
public static String parseLocalKey(String compositeKey)
throws InvalidNameException, GroupsException
{
    return new CompositeEntityIdentifier(compositeKey, new Object().getClass()).getLocalKey();
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
  public static EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
    return instance().compositeGroupService.searchForEntities(query,method,type);
  }
  public static EntityIdentifier[] searchForEntities(String query, int method, Class type, IEntityGroup ancestor) throws GroupsException {
    return instance().compositeGroupService.searchForEntities(query,method,type,ancestor);
  }
  public static EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
    return instance().compositeGroupService.searchForGroups(query,method,leaftype);
  }
  public static EntityIdentifier[] searchForGroups(String query, int method, Class leaftype, IEntityGroup ancestor) throws GroupsException {
    return instance().compositeGroupService.searchForGroups(query,method,leaftype,ancestor);
  }
public static void startUp() throws GroupsException
{
    instance();
}
}
