/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;
import org.jasig.portal.groups.local.EntitySearcherImpl;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.portal.groups.local.searchers.RDBMChannelDefSearcher;
import org.jasig.portal.groups.local.searchers.RDBMPersonSearcher;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.EntityLockService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference group service.
 * 
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceGroupService implements ILockableGroupService
{
    private static final Log log = LogFactory.getLog(ReferenceGroupService.class);
    
    
    // Singleton instance:
    protected static IGroupService singleton = null;

    // Factories for IGroupMembers:
    protected IEntityStore entityFactory = null;
    protected IEntityGroupStore groupFactory = null;

    // Entity searcher
    protected IEntitySearcher entitySearcher = null;

    // Are group members cached?  See portal.properties.
    private boolean cacheInUse;
    /**
     * ReferenceGroupsService constructor.
     */
    private ReferenceGroupService() throws GroupsException
    {
        super();
        initialize();
    }
    
/**
 *
 */
protected void addGroupToCache(IEntityGroup group) throws CachingException
{
    EntityCachingService.instance().add(group);
}

    /**
     * Answers if <code>IGroupMembers</code> are being cached.
     */
  protected boolean cacheInUse()
    {
        return cacheInUse;
    }
    
/**
 * Removes the <code>IEntityGroup</code> from the cache and the store.
 * @param group IEntityGroup
 */
public void deleteGroup(IEntityGroup group) throws GroupsException
{
    if ( cacheInUse() )
    {
        try
            { removeGroupFromCache(group); }
        catch (CachingException ce)
            { throw new GroupsException("Problem deleting group " + group.getKey() + " : " + ce.getMessage()); }
    }
    getGroupStore().delete(group);
}

/**
 * Removes the <code>ILockableEntityGroup</code> from the cache and the store.
 * @param group ILockableEntityGroup
 */
public void deleteGroup(ILockableEntityGroup group) throws GroupsException
{
    try
    {
        if ( group.getLock().isValid() )
        {
            deleteGroup( (IEntityGroup)group );
            group.getLock().release();
        }
        else
            { throw new GroupsException("Could not delete group " + group.getKey() +
                " has invalid lock."); }
    }
    catch (LockingException le)
        { throw new GroupsException("Could not delete group " + group.getKey() +
                " : " + le.getMessage()); }
}

/**
 * Returns and caches the containing groups for the <code>IGroupMember</code>
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
        if (cacheInUse())
        {
            try
            {
                if ( getGroupFromCache(group.getEntityIdentifier().getKey()) == null )
                    { addGroupToCache(group); }
            }
            catch (CachingException ce)
                { throw new GroupsException("Problem finding containing groups: " + ce.getMessage()); }
        }
    }
    return groups.iterator();
}

    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if it
     * does not exist.
     */
    public IEntityGroup findGroup(String key) throws GroupsException
    {
      return (cacheInUse()) ? findGroupWithCache(key) : groupFactory.find(key);
    }
    
    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if it
     * does not exist.
     */
    protected IEntityGroup findGroupWithCache(String key) throws GroupsException
    {
        try
        {
            IEntityGroup group = getGroupFromCache(key);
            if (group == null)
            {
                group = groupFactory.find(key);
                if (group != null)
                    { addGroupToCache(group); }
            }
        return group;
        }
        catch (CachingException ce)
            { throw new GroupsException("Problem retrieving group " + key + " : " + ce.getMessage());}
    }
    
/**
 * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
 * group is not found.
 */
public ILockableEntityGroup findGroupWithLock(String key, String owner)
throws GroupsException
{
    return findGroupWithLock(key, owner, 0);
}

/**
 * Returns a pre-existing <code>ILockableEntityGroup</code> or null if the
 * group is not found.
 */
public ILockableEntityGroup findGroupWithLock(String key, String owner, int secs)
throws GroupsException
{
    Class groupType = org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE;
    try
    {
        IEntityLock lock =  ( secs == 0 )
            ? EntityLockService.instance().newWriteLock(groupType, key, owner)
            : EntityLockService.instance().newWriteLock(groupType, key, owner, secs);

        ILockableEntityGroup group = groupFactory.findLockable(key);
        if ( group == null )
           { lock.release(); }
        else
            { group.setLock(lock); }

        return group;
    }
    catch (LockingException le)
        { throw new GroupsException("Problem getting lock for group " + key + " : " + le.getMessage()); }

}

/**
 * Returns and caches the member groups for the <code>IEntityGroup</code>
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
        if (cacheInUse())
        {
            try
            {
                if ( getGroupFromCache(group.getEntityIdentifier().getKey()) == null )
                    { addGroupToCache(group); }
            }
            catch (CachingException ce)
                { throw new GroupsException("Problem finding member groups: " + ce.getMessage()); }
        }
    }
    return groups.iterator();
}

     /**
     * Refers to the PropertiesManager to get the key for the group
     * associated with 'name' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public IEntityGroup getDistinguishedGroup(String name) throws GroupsException{
      String key = PropertiesManager.getProperty("org.jasig.portal.groups.ReferenceGroupService.key_"+name);
      if (key != null){
        return findGroup(key);
      }
      else {
        throw new GroupsException("ReferenceGroupService.getDistinguishedGroup(): no key found to match requested name");
      }
    }
    
    /**
     * Returns an <code>IEntity</code> representing a portal entity.  This does
     * not guarantee that the entity actually exists.
     */
    public IEntity getEntity(String key, Class type) throws GroupsException
    {
      return entityFactory.newInstance(key, type);
    }
    
/**
 * Returns a cached <code>IEntityGroup</code> or null if it has not been cached.
 */
protected IEntityGroup getGroupFromCache(String key) throws CachingException
{
    return (IEntityGroup) EntityCachingService.instance().get(org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE, key);
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
     * Returns an <code>IGroupMember</code> representing either a group or a
     * portal entity.  If the parm <code>type</code> is the group type,
     * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
     * an <code>IEntity</code>.
     */
    public IGroupMember getGroupMember(String key, Class type) throws GroupsException
    {
      IGroupMember gm = null;
      if ( type == org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE )
        gm = findGroup(key);
      else
        gm = getEntity(key, type);
      return gm;
    }
    
    /**
     * Returns the implementation of <code>IEntityGroupStore</code> whose class name
     * was retrieved by the PropertiesManager (see initialize()).
     */
  public IEntityGroupStore getGroupStore() throws GroupsException
    {
        return groupFactory;
    }
    
    /**
     * Refers to the PropertiesManager to get the key for the root group
     * associated with 'type' and asks the group store implementation for the corresponding
     * <code>IEntityGroup</code>.
     */
    public IEntityGroup getRootGroup(Class type) throws GroupsException{
      return getDistinguishedGroup(type.getName());
    }
    
    /**
     * @exception org.jasig.portal.groups.GroupsException
     */
    private void initialize() throws GroupsException
    {
      String eMsg = null;
      entityFactory = new RDBMEntityStore();

      String groupFactoryName = PropertiesManager.getProperty
          ("org.jasig.portal.groups.EntityGroupFactory.implementation");

      if ( groupFactoryName == null )
      {
          eMsg = "ReferenceGroupService.initialize(): EntityGroupStoreImpl not specified in portal.properties";
          log.error( eMsg);
          throw new GroupsException(eMsg);
      }

      try
      {
          groupFactory = (IEntityGroupStore)Class.forName(groupFactoryName).newInstance();
      }
      catch (Exception e)
      {
          eMsg = "ReferenceGroupService.initialize(): Failed to instantiate " + groupFactoryName + " " + e;
          log.error( eMsg);
          throw new GroupsException(eMsg);
      }

    cacheInUse = PropertiesManager.getPropertyAsBoolean
          ("org.jasig.portal.groups.IEntityGroupService.useCache");

      ITypedEntitySearcher[] tes = new ITypedEntitySearcher[2];
      tes[0]=new RDBMChannelDefSearcher();
      tes[1]=new RDBMPersonSearcher();
      entitySearcher = new EntitySearcherImpl(tes);
    }
    
/**
 * Returns a new <code>IEntityGroup</code> for the given Class with an unused
 * key.
 */
public IEntityGroup newGroup(Class type) throws GroupsException
{
    try
    {
        IEntityGroup group = groupFactory. newInstance(type);
        addGroupToCache(group);
        return group;
    }
    catch (Exception e)
    {
        throw new GroupsException("ReferenceGroupService.newGroup(): " + e.getMessage());
    }
}

/**
 *
 */
protected void removeGroupFromCache(IEntityGroup group) throws CachingException
{
    EntityCachingService.instance().remove(group.getEntityIdentifier());
}

    /**
     * @return org.jasig.portal.groups.IGroupService
     * @exception org.jasig.portal.groups.GroupsException
     */
    public static synchronized IGroupService singleton() throws GroupsException
    {
      if ( singleton == null )
          { singleton = new ReferenceGroupService(); }
      return singleton;
    }
    
/**
 * Updates the cache and the store with the new <code>IEntityGroup</code>.
 * @param group IEntityGroup
 */
public void updateGroup(IEntityGroup group) throws GroupsException
{
    if ( cacheInUse() )
    {
        try
            { updateGroupInCache(group); }
        catch (CachingException ce)
            { throw new GroupsException("Problem updating group " + group.getKey() + " : " + ce.getMessage()); }
    }
    getGroupStore().update(group);
}

/**
 * Updates the <code>ILockableEntityGroup</code> in the cache and the store.
 * @param group ILockableEntityGroup
 */
public void updateGroup(ILockableEntityGroup group) throws GroupsException
{
    updateGroup(group, false);
}

/**
 * Updates the <code>ILockableEntityGroup</code> in the store and removes
 * it from the cache.
 * @param group ILockableEntityGroup
 */
public void updateGroup(ILockableEntityGroup group, boolean renewLock)
throws GroupsException
{
    try
    {
        if ( ! group.getLock().isValid() )
           { throw new GroupsException("Could not update group " + group.getKey() +
                " has invalid lock."); }

        if ( ! renewLock )
        {
            updateGroup((IEntityGroup)group);
            group.getLock().release();
        }
        else
        {
            getGroupStore().update(group);
            if ( cacheInUse)
                { removeGroupFromCache(group); }
            group.getLock().renew();
        }
    }
    catch (LockingException le)
        { throw new GroupsException("Problem updating group " + group.getKey() +
                " : " + le.getMessage()); }
    catch (CachingException ce)
        { throw new GroupsException("Problem updating group " + group.getKey() +
                " : " + ce.getMessage()); }
}

/**
 *
 */
protected void updateGroupInCache(IEntityGroup group) throws CachingException
{
    EntityCachingService.instance().update(group);
}

/**
 * Updates the cache and the store with the updated <code>IEntityGroup</code>.
 * @param group IEntityGroup
 */
public void updateGroupMembers(IEntityGroup group) throws GroupsException
{
    if ( cacheInUse() )
    {
        try
            { EntityCachingService.instance().update(group); }
        catch (CachingException ce)
            { throw new GroupsException("Problem updating members for group " + group.getKey() + " : " + ce.getMessage()); }
    }
    getGroupStore().updateMembers(group);
}

/**
 * Updates the <code>ILockableEntityGroup</code> in the cache and the store.
 * @param group ILockableEntityGroup
 */
public void updateGroupMembers(ILockableEntityGroup group) throws GroupsException
{
    updateGroupMembers(group, false);
}

/**
 * Updates the <code>ILockableEntityGroup</code> in the store and removes
 * it from the cache.
 * @param group ILockableEntityGroup
 */
public void updateGroupMembers(ILockableEntityGroup group, boolean renewLock)
throws GroupsException
{
    try
    {
        if ( ! group.getLock().isValid() )
           { throw new GroupsException("Could not update group " + group.getKey() +
                " has invalid lock."); }

        if ( ! renewLock )
        {
            updateGroupMembers((IEntityGroup)group);
            group.getLock().release();
        }
        else
        {
            getGroupStore().updateMembers(group);
            if ( cacheInUse)
                { removeGroupFromCache(group); }
            group.getLock().renew();
        }
    }
    catch (LockingException le)
        { throw new GroupsException("Problem updating group " + group.getKey() +
                " : " + le.getMessage()); }
    catch (CachingException ce)
        { throw new GroupsException("Problem updating group " + group.getKey() +
                " : " + ce.getMessage()); }
}

  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
    return removeDuplicates(groupFactory.searchForGroups(query,method,leaftype));
  }
  
  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype, IEntityGroup ancestor) throws GroupsException {
    return filterEntities(searchForGroups(query,method,leaftype),ancestor);
  }
  
  public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
    return removeDuplicates(entitySearcher.searchForEntities(query,method,type));
  }
  
  public EntityIdentifier[] searchForEntities(String query, int method, Class type, IEntityGroup ancestor) throws GroupsException {
    return filterEntities(searchForEntities(query,method,type),ancestor);
  }

  private EntityIdentifier[] filterEntities(EntityIdentifier[] entities, IEntityGroup ancestor) throws GroupsException{
    ArrayList ar = new ArrayList(entities.length);
    for(int i=0; i< entities.length;i++){
      IGroupMember gm = this.getGroupMember(entities[i]);
      if (ancestor.deepContains(gm)){
        ar.add(entities[i]);
      }
    }
    return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
  }

  private EntityIdentifier[] removeDuplicates(EntityIdentifier[] entities){
    ArrayList ar = new ArrayList(entities.length);
    for(int i=0; i< entities.length;i++){
      if (!ar.contains(entities[i])){
        ar.add(entities[i]);
      }
    }
    return (EntityIdentifier[]) ar.toArray(new EntityIdentifier[0]);
  }
}
