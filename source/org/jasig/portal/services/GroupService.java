/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

import org.jasig.portal.groups.*;
import org.jasig.portal.*;

/**
 *  Bootstrap class for the IGroupService implementation.
 *
 * @author  Alex Vigdor
 * @author  Dan Ellentuck
 * @version $Revision$
 */

public class GroupService
{
    // Singleton instance of the bootstrap class:
    private static GroupService instance = null;
    // The group service:
    private IGroupService groupService = null;
    /** Creates new GroupService */
    private GroupService() throws GroupsException
    {
        super();
        initialize();
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
    * Returns an <code>IEntity</code> representing a portal entity.  This does
    * not guarantee that the entity actually exists.
    * @param key String - the group key.
    * @param type Class - the Class of the underlying IGroupMember.
    * @return org.jasig.portal.groups.IGroupMember
    */
    public static IEntity getEntity(String key, Class type)
    throws GroupsException
    {
        return instance().igetEntity(key, type);
    }
    /**
     * Returns the distinguished group called "everyone".
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup getEveryoneGroup() throws GroupsException
    {
        return instance().igetEveryoneGroup();
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
     * Returns the distinguished group called "Portal Administrators".
     * @return org.jasig.portal.groups.IEntityGroup
     */
    public static IEntityGroup getPortalAdministratorsGroup() throws GroupsException
    {
        return instance().igetPortalAdministratorsGroup();
    }
  /*
   * Returns a pre-existing <code>IEntityGroup</code> or null if the
   * <code>IGroupMember</code> does not exist.
   * @param key String - the group key.
   * @return org.jasig.portal.groups.IEntityGroup
   */
    protected IEntityGroup ifindGroup(String key) throws GroupsException
    {
        return groupService.findGroup(key);
    }
   /*
    * Returns an <code>IEntity</code> representing a pre-existing portal entity.
    * @param key String - the group key.
    * @param type Class - the Class of the underlying IGroupMember.
    * @return org.jasig.portal.groups.IEntity
    */
    protected IEntity igetEntity(String key, Class type) throws GroupsException
    {
        return groupService.getEntity(key, type);
    }
    /**
     * Returns the distinguished group called "everyone".
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup igetEveryoneGroup() throws GroupsException
    {
        return groupService.getEveryoneGroup();
    }
  /*
   * Returns an <code>IGroupMember</code> representing either a group or a
   * portal entity.  If the parm <code>type</code> is the group type,
   * the <code>IGroupMember</code> is an <code>IEntityGroup</code> else it is
   * an <code>IEntity</code>.
   */
    protected IGroupMember igetGroupMember(String key, Class type) throws GroupsException
    {
        return groupService.getGroupMember(key, type);
    }
    /**
     * Returns the distinguished group called "Portal Administrators".
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup igetPortalAdministratorsGroup() throws GroupsException
    {
        return groupService.getPortalAdministratorsGroup();
    }
    /**
    * Returns a new <code>IEntityGroup</code> for the given Class with an unused
    * key.
    * @return org.jasig.portal.groups.IEntityGroup
    */
    protected IEntityGroup inewGroup(Class type) throws GroupsException {
        return groupService.newGroup(type);
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
        LogService.instance().log(LogService.ERROR, eMsg);
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
        LogService.instance().log(LogService.ERROR, eMsg);
        throw new GroupsException(eMsg);
    }
}
    public static synchronized GroupService instance() throws GroupsException {
        if ( instance==null ) {
            instance = new GroupService();
        }
        return instance;
    }
    /**
    * Returns a new <code>IEntityGroup</code> for the given Class with an unused
    * key.
    * @return org.jasig.portal.groups.IEntityGroup
    */
    public static IEntityGroup newGroup(Class type) throws GroupsException {
        return instance().inewGroup(type);
    }
}
