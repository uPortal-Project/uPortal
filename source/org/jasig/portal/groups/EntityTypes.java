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

package org.jasig.portal.groups;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides access to the entity types used by <code>IEntityGroups</code>.
 * Type, to a group, is like <code>Class</code> to an <code>Array</code>.  It specifies
 * what kind of entities the <code>IEntityGroup</code> contains.
 * <p>
 * Each type is associated with an <code>Integer</code> which is used to store the type in
 * the portal data store.  This class translates between the <code>Integer</code> and
 * <code>Class</code> values.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 * @deprecated As of uPortal 2.1, replaced by {@link org.jasig.portal.EntityTypes}
 */
public class EntityTypes {
    private static final Log log = LogFactory.getLog(EntityTypes.class);
    private static EntityTypes singleton;

    // Caches for entityType classes.
    private Map entityTypesByID;
    private Map entityIDsByType;

    // Constant strings for ENTITY TYPE table:
    private static String ENTITY_TYPE_TABLE = "UP_ENTITY_TYPE";
    private static String TYPE_ID_COLUMN = "ENTITY_TYPE_ID";
    private static String TYPE_NAME_COLUMN = "ENTITY_TYPE_NAME";

    // SQL strings for ENTITY TYPE crud:
    private static String selectEntityTypesSql;

    // For retrieving all types:
    public static int NULL_TYPE_ID = -1;

    public static Class GROUP_ENTITY_TYPE = null;

/**
 * EntityTypes constructor comment.
 */
public EntityTypes()
{
    super();
    initialize();
}
/**
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntityTypeIDs()
{
    return entityTypesByID.keySet().iterator();
}
/**
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntityTypes()
{
    return entityTypesByID.values().iterator();
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Integer
 */
public Integer getEntityIDFromType(Class type)
{
    return (Integer)getEntityIDsByType().get(type);
}
/**
 * @return java.util.Map
 */
private java.util.Map getEntityIDsByType()
{
    if ( entityIDsByType == null )
        entityIDsByType = new HashMap(5);
    return entityIDsByType;
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public static Class getEntityType(Integer typeID)
{
    return singleton().getEntityTypeFromID(typeID);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public Class getEntityTypeFromID(Integer typeID)
{
    return (Class)getEntityTypesByID().get(typeID);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Class
 */
public static Integer getEntityTypeID(Class type)
{
    return singleton().getEntityIDFromType(type);
}
/**
 * @return java.util.Map
 */
private java.util.Map getEntityTypesByID()
{
    if ( entityTypesByID == null )
        entityTypesByID = new HashMap(5);
    return entityTypesByID;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getSelectEntityTypesSql()
{
    if ( selectEntityTypesSql == null )
    {
        selectEntityTypesSql =
            "SELECT " + TYPE_ID_COLUMN + ", " + TYPE_NAME_COLUMN + " FROM " + ENTITY_TYPE_TABLE;
    }
    return selectEntityTypesSql;
}
/**
 * Cache entityTypes.
 */
private void initialize()
{
    Connection conn = null;
    Integer typeID = null;
    Class entityType = null;

    try
    {
        GROUP_ENTITY_TYPE = Class.forName("org.jasig.portal.groups.IEntityGroup");

        conn = RDBMServices.getConnection();
        Statement stmnt = conn.createStatement();
        try
        {
            ResultSet rs = stmnt.executeQuery(getSelectEntityTypesSql());
            try
            {
                while (rs.next())
                {
                    typeID = new Integer(rs.getInt(1));
                    entityType = Class.forName(rs.getString(2));
                    getEntityIDsByType().put(entityType, typeID);
                    getEntityTypesByID().put(typeID, entityType);
                }
            }
            finally
                { rs.close(); }
                }
        finally
            { stmnt.close(); }
    }
    catch (Exception ex)
        { log.error( ex); }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * @return org.jasig.portal.groups.EntityTypes
 */
public static synchronized EntityTypes singleton()
{
    if ( singleton == null )
        { singleton = new EntityTypes(); }
    return singleton;
}
}
