/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

import org.jasig.portal.EntityTypes;

/**
 * Reference implementation for IEntityStore.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class RDBMEntityStore implements IEntityStore {
private static IEntityStore singleton;

/**
 * RDBMEntityStore constructor.
 */
public RDBMEntityStore()
{
    super();
}

/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 */
public IEntity newInstance(String key) throws GroupsException
{
    return newInstance(key, null);
}
/**
 * @return org.jasig.portal.groups.IEntity
 * @param key java.lang.String
 * @param type java.lang.Class
 */
public IEntity newInstance(String key, Class type) throws GroupsException
{
    if ( EntityTypes.getEntityTypeID(type) == null )
        { throw new GroupsException("Invalid group type: " + type); }
    return new EntityImpl(key, type);
}
/**
 * @return org.jasig.portal.groups.IEntityStore
 */
public static synchronized IEntityStore singleton()
{
    if (singleton == null)
        { singleton = new RDBMEntityStore(); }
    return singleton;
}
}
