/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.services.EntityCachingService;
import org.jasig.portal.services.GroupService;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceCompositeGroupService extends ReferenceComponentGroupService
implements ICompositeGroupService
{
    // Factory for IEntities:
    protected IEntityStore entityFactory = null;

    // See CompositeGroupService.xml:
    protected IIndividualGroupService defaultService;
/**
 * ReferenceCompositeGroupService constructor comment.
 */
public ReferenceCompositeGroupService() throws GroupsException
{
    super();
//	initializeComponentServices();
}
/**
 * Returns groups that contain the <code>IGroupMember</code>.  Delegates to the
 * component services, but only after checking that they might actually contain
 * a membership for this member.
 * @param gm IGroupMember
 */
public Iterator findContainingGroups(IGroupMember gm) throws GroupsException
{
    Collection allGroups = new ArrayList();
    IIndividualGroupService service = null;

    for ( Iterator services = getComponentServices().values().iterator(); services.hasNext(); )
    {
        service = (IIndividualGroupService) services.next();
        if ( gm.isEntity() || service.isEditable() ||
          getComponentService(((IEntityGroup)gm).getServiceName()) == service )
        {
            {
                for ( Iterator groups = service.findContainingGroups(gm); groups.hasNext(); )
                    { allGroups.add((IEntityGroup) groups.next()); }
            }
        }
    }
    return allGroups.iterator();
}
/**
 * Returns a pre-existing <code>IEntityGroup</code> or null if the
 * <code>IGroupMember</code> does not exist.
 */
public IEntityGroup findGroup(String key) throws GroupsException
{
    CompositeEntityIdentifier ent = newCompositeEntityIdentifier(key);
    IIndividualGroupService service = getComponentService(ent);
    return ( service == null )
      ? null
      : service.findGroup(ent);
}
/**
 * Returns a pre-existing <code>IEntityGroup</code> or null if the
 * <code>IGroupMember</code> does not exist.
 */
public ILockableEntityGroup findGroupWithLock(String key, String lockOwner)
throws GroupsException
{
    CompositeEntityIdentifier ent = newCompositeEntityIdentifier(key);
    IIndividualGroupService service = getComponentService(ent);
    return ( service == null )
      ? null
      : service.findGroupWithLock(ent.getLocalKey(), lockOwner);
}
/**
 * @return IIndividualGroupService
 */
protected IIndividualGroupService getComponentService(Name serviceName)
{
    return (IIndividualGroupService)getComponentServices().get(serviceName);
}
/**
 * @return IIndividualGroupService
 */
protected IIndividualGroupService getComponentService(CompositeEntityIdentifier entId)
{
    return getComponentService(entId.getServiceName());
}
/**
 * Returns the <code>IIndividualGroupService</code> designated as the default service
 * in the configuration document.
 */
protected IIndividualGroupService getDefaultService()
{
    return defaultService;
}
/**
 * Returns an <code>IEntity</code> representing a portal entity.  This does
 * not guarantee that the entity actually exists.
 */
public IEntity getEntity(String key, Class type) throws GroupsException
{
    return getEntity(key, type, null);
}
/**
 * Returns an <code>IEntity</code> representing a portal entity.  This does
 * not guarantee that the entity actually exists.
 */
public IEntity getEntity(String key, Class type, String svcName) 
throws GroupsException
{
    IIndividualGroupService svc = null;
    if ( svcName == null )
        { svc = getDefaultService(); }
    else
    {   
        try
        {  
            Name n = GroupService.parseServiceName(svcName);
            svc = getComponentService(n);
        }
        catch (InvalidNameException ine)
            { throw new GroupsException("Invalid service name."); }
    }
    return ( svc == null ) ? null : svc.getEntity(key, type);
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
 * Returns an <code>IGroupMember</code> representing either a group or a
 * portal entity, based on the <code>EntityIdentifier</code>, which refers
 * to the UNDERLYING entity for the <code>IGroupMember</code>.
 */
public IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
throws GroupsException
{
    return getGroupMember(underlyingEntityIdentifier.getKey(),
      underlyingEntityIdentifier.getType());
}
/**
 * Assembles the group services composite.  Once the leaf services have been
 * retrieved, they are held in a (one-dimensional) Map.  The composite
 * identity of a service is preserved in its Map key, a javax.naming.Name.
 * Each node of the Name is the name of a component service, starting with
 * the service closest to the composite service and ending with the name of
 * the leaf service.  The key is built up layer by layer.
 *
 * @exception GroupsException
 */
protected void initializeComponentServices() throws GroupsException
{
    Name leafServiceName = null;
    try
    {
        GroupServiceConfiguration cfg = GroupServiceConfiguration.getConfiguration();
        List services = cfg.getServiceDescriptors();
        for ( Iterator it=services.iterator(); it.hasNext(); )
        {
            ComponentGroupServiceDescriptor descriptor =
              (ComponentGroupServiceDescriptor) it.next();
            String factoryName = descriptor.getServiceFactoryName();
            IComponentGroupServiceFactory factory =
              (IComponentGroupServiceFactory)Class.forName(factoryName).newInstance();
            IComponentGroupService service = factory.newGroupService(descriptor);

            // If it's a leaf service, add it to the Map.
            if ( service.isLeafService() )
            {
                leafServiceName = GroupService.parseServiceName(descriptor.getName());
                service.setServiceName(leafServiceName);
                getComponentServices().put(leafServiceName, service);
            }

            // Otherwise, get its leaf services and for each, push our node onto the service Name
            // and add the service to the Map.
            else
            {
                Map componentMap = service.getComponentServices();
                for ( Iterator components=componentMap.values().iterator(); components.hasNext(); )
                {
                    IIndividualGroupService leafService = (IIndividualGroupService)components.next();
                    leafServiceName = leafService.getServiceName();
                    leafServiceName.add(0,descriptor.getName());
                    getComponentServices().put(leafServiceName, leafService);
                }
            }
        }

        Name defaultServiceName = GroupService.parseServiceName(cfg.getDefaultService());
        defaultService = (IIndividualGroupService)getComponentService(defaultServiceName);

    }
    catch (Exception ex)
        { throw new GroupsException("Problem initializing component services: " + ex.getMessage()); }
}
/**
 * Returns a <code>CompositeEntityIdentifier</code> for the group identified
 * by <code>key</code>.
 */
protected CompositeEntityIdentifier newCompositeEntityIdentifier(String key)
throws GroupsException
{
    return new CompositeEntityIdentifier(key, org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE);
}
/**
 * Returns a new <code>IEntityGroup</code> from the named service.
 */
public IEntityGroup newGroup(Class type, Name serviceName) throws GroupsException {
    return getComponentService(serviceName).newGroup(type);
}
/**
 * Returns a pre-existing <code>IEntityGroup</code> or null if the
 * <code>IGroupMember</code> does not exist.
 */
protected IEntityGroup primFindGroup(String key) throws GroupsException
{
    CompositeEntityIdentifier ent = newCompositeEntityIdentifier(key);
    IIndividualGroupService service = getComponentService(ent);
    return ( service == null )
      ? null
      : service.findGroup(ent);
}
 /**
   * Find EntityIdentifiers for entities whose name matches the query string
   * according to the specified method and is of the specified type
   */
public EntityIdentifier[] searchForEntities(String query, int method, Class type)
throws GroupsException
{
    Set allIds = new HashSet();

    for ( Iterator services = getComponentServices().values().iterator(); services.hasNext(); )
    {
        IGroupService service = (IGroupService) services.next();
        EntityIdentifier[] ids = service.searchForEntities(query, method, type);
        for (int i=0; i<ids.length; i++)
            { allIds.add(ids[i]);}
    }
    return (EntityIdentifier[])allIds.toArray(new EntityIdentifier[allIds.size()]);
}
 /**
   * Find EntityIdentifiers for entities whose name matches the query string
   * according to the specified method, is of the specified type  and
   * descends from the specified group
   */
public EntityIdentifier[] searchForEntities(String query, int method, Class type, IEntityGroup ancestor)
throws GroupsException
{
    Set allIds = new HashSet();

    for ( Iterator services = getComponentServices().values().iterator(); services.hasNext(); )
    {
        IGroupService service = (IGroupService) services.next();
        EntityIdentifier[] ids = service.searchForEntities(query, method, type, ancestor);
        for (int i=0; i<ids.length; i++)
            { allIds.add(ids[i]);}
    }
    return (EntityIdentifier[])allIds.toArray(new EntityIdentifier[allIds.size()]);
}
 /**
   * Find EntityIdentifiers for groups whose name matches the query string
   * according to the specified method and matches the provided leaf type
   */
public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype)
throws GroupsException
{
    Set allIds = new HashSet();

    for ( Iterator services = getComponentServices().values().iterator(); services.hasNext(); )
    {
        IIndividualGroupService service = (IIndividualGroupService) services.next();
        EntityIdentifier[] ids = service.searchForGroups(query, method, leaftype);
        for (int i=0; i<ids.length; i++)
        {
            try
            {
                CompositeEntityIdentifier cei = new CompositeEntityIdentifier(ids[i].getKey(),ids[i].getType());
                cei.setServiceName(service.getServiceName());
                allIds.add(cei);
            }
            catch (javax.naming.InvalidNameException ine) {}
        }
    }
    return (EntityIdentifier[])allIds.toArray(new EntityIdentifier[allIds.size()]);
}
 /**
   * Find EntityIdentifiers for groups whose name matches the query string
   * according to the specified method, has the provided leaf type  and
   * descends from the specified group
   */
public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype, IEntityGroup ancestor)
throws GroupsException
{
    Set allIds = new HashSet();

    for ( Iterator services = getComponentServices().values().iterator(); services.hasNext(); )
    {
        IIndividualGroupService service = (IIndividualGroupService) services.next();
        EntityIdentifier[] ids = service.searchForGroups(query, method, leaftype, ancestor);
        for (int i=0; i<ids.length; i++)
          {
            try
            {
                CompositeEntityIdentifier cei = new CompositeEntityIdentifier(ids[i].getKey(),ids[i].getType());
                cei.setServiceName(service.getServiceName());
                allIds.add(cei);
            }
            catch (javax.naming.InvalidNameException ine) {}
          }
    }
    return (EntityIdentifier[])allIds.toArray(new EntityIdentifier[allIds.size()]);
}
/**
 * Insert the method's description here.
 * Creation date: (10/31/2002 10:58:53 AM)
 * @param newComponentServices java.util.Map
 */
protected void setComponentServices(java.util.Map newComponentServices) {
    componentServices = newComponentServices;
}

 /**
 *
 */
protected void cacheAdd(IGroupMember gm) throws GroupsException
{
    try
        { EntityCachingService.instance().add(gm); }
    catch (CachingException ce)
        { throw new GroupsException("Problem adding group member " + gm.getKey() + " to cache: " + ce.getMessage() ); }
}

 /**
 *
 */
protected void cacheRemove(IGroupMember gm) throws GroupsException
{
    try
        { EntityCachingService.instance().remove(gm.getEntityIdentifier()); }
    catch (CachingException ce)
        { throw new GroupsException("Problem removing group member " + gm.getKey() + " from cache: " + ce.getMessage() ); }
}

 /**
 *
 */
protected void cacheUpdate(IGroupMember gm) throws GroupsException
{
    try
        { EntityCachingService.instance().update(gm); }
    catch (CachingException ce)
        { throw new GroupsException("Problem updating group member " + gm.getKey() + " in cache: " + ce.getMessage() ); }
}

/**
 * Returns a cached <code>IEntity</code> or null if it has not been cached.
 */
protected IEntity getEntityFromCache(String key) throws CachingException
{
    return (IEntity) EntityCachingService.instance().get(org.jasig.portal.EntityTypes.LEAF_ENTITY_TYPE, key);
}
}
