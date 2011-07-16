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
