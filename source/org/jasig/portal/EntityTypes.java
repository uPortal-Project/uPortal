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

package org.jasig.portal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.services.LogService;
import org.jasig.portal.services.SequenceGenerator;

/**
 * This class provides access to the entity types used by <code>IBasicEntities</code>
 * and the classes in <code>org.jasig.portal.groups</code> and
 * <code>org.jasig.portal.concurrency.</code>.
 * <p>
 * Each type is associated with an <code>Integer</code> used to represent the
 * type in the portal data store.  This class translates between the
 * <code>Integer</code> and <code>Class</code> values.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.IBasicEntity
 */
public class EntityTypes {

    private static EntityTypes singleton;

    // Caches for EntityType instances.
    private Map entityTypesByID;
    private Map entityTypesByType;
    
    // Lock for crud operations.
    private Object updateLock = new Object();

    // Constant strings for ENTITY TYPE table:
    private static String ENTITY_TYPE_TABLE = "UP_ENTITY_TYPE";
    private static String TYPE_ID_COLUMN = "ENTITY_TYPE_ID";
    private static String TYPE_NAME_COLUMN = "ENTITY_TYPE_NAME";
    private static String DESCRIPTIVE_NAME_COLUMN = "DESCRIPTIVE_NAME";

    // For retrieving all types:
    public static int NULL_TYPE_ID = -1;

    public static Class GROUP_ENTITY_TYPE = org.jasig.portal.groups.IEntityGroup.class;
    public static Class LEAF_ENTITY_TYPE = org.jasig.portal.groups.IEntity.class;

    private class EntityType
    {
        private Class type;
        private Integer typeId;
        private String descriptiveName;
        private EntityType(Class cl, Integer id, String description) {
            super();
            type = cl; typeId = id; descriptiveName = description;
        }
        private Class getType() { return type; }
        private Integer getTypeId() { return typeId; }
        private String getDescriptiveName() { return descriptiveName; }
        public String toString() {
            String desc = (descriptiveName) == null ? "" : descriptiveName;
            return desc + " (" + getTypeId() + ") " + getType().getName();
        }
    }
/**
 * EntityTypes constructor comment.
 */
private EntityTypes()
{
    super();
    initialize();
}

/**
 * Add the new type if it does not already exist.
 */
public static void addIfNecessary(Class newType, String description)
throws java.lang.Exception
{
    singleton().addEntityTypeIfNecessary(newType, description);
}

/**
 * Add the new type if it does not already exist in the cache.
 */
private void addEntityType(Class newType, String description)
throws java.lang.Exception
{
    if ( getEntityTypesByType().get(newType) == null )
    {
        int nextKey = getNextKey();
        EntityType et = new EntityType(newType, new Integer(nextKey), description);
        insertEntityType(et);
        primAddEntityType(et);
    }
}

/**
 * Check if we have the type in our cache.  If not, re-retrieve.  Someone 
 * might have added it since we last retrieved.  If the type is not
 * found, try to add it to the store.  If the add is not successful,
 * re-retrieve again.  If the type is still not found, rethrow the 
 * SQLException.  Synchronize on update lock to serialize adds, deletes 
 * and updates while letting reads proceed.
 */
public void addEntityTypeIfNecessary(Class newType, String description)
throws java.lang.Exception
{
    synchronized (updateLock) {
        if ( getEntityTypesByType().get(newType) == null )
        {
            refresh();
            if ( getEntityTypesByType().get(newType) == null )
            {
                try
                {
                    addEntityType(newType, description);
                }
                catch (Exception ex)
                {
                    refresh();
                    if ( getEntityTypesByType().get(newType) == null )
                    {
                        String errString = "Attempt to add entity type failed: " + ex.getMessage();
                        LogService.log (LogService.ERROR, errString);
                        throw ex;
                    }
                }  // end catch
            }      // end if
        }          // end if
    }              // end synchronized
}
/**
 * Synchronize on update lock to serialize adds, deletes and updates
 * while letting reads proceed.
 */
public void deleteEntityType(Class type) throws SQLException
{
    synchronized (updateLock) {

        refresh();
        EntityType et = (EntityType)getEntityTypesByType().get(type);
        if ( et != null )
        {
            deleteEntityType(et);
            primRemoveEntityType(et);
        }
    }
}
/**
 * delete EntityType from the store.
 */
private void deleteEntityType(EntityType et) throws SQLException
{
    Connection conn = null;
    RDBMServices.PreparedStatement ps = null;
    try
    {
        conn = RDBMServices.getConnection();
        try
        {
            ps = new RDBMServices.PreparedStatement(conn, getDeleteEntityTypeSql());

            ps.setInt(1, et.getTypeId().intValue());
            ps.setString(2, et.getType().getName());

            LogService.log(LogService.DEBUG, "EntityTypes.deleteEntityType(): " + ps + "(" +
              et.getTypeId() + ", " + et.getType() + ")" );

            int rc = ps.executeUpdate();

            if ( rc != 1 )
            {
                String errString = "Problem deleting type " + et;
                LogService.log (LogService.ERROR, errString);
                throw new SQLException(errString);
            }
        }
        finally
        {
            try
            {
                if (ps != null) { ps.close(); }
            }
            finally
            {
                RDBMServices.releaseConnection(conn); 
            }
        }
    }
    catch (java.sql.SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        throw sqle;
    }
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllColumnNames()
{
    return TYPE_ID_COLUMN + ", " + TYPE_NAME_COLUMN + ", " +  DESCRIPTIVE_NAME_COLUMN;
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
    Collection types = new ArrayList(getEntityTypesByType().size());
    for (Iterator i = entityTypesByID.values().iterator(); i.hasNext(); )
    {
        EntityType et = (EntityType) i.next();
        types.add(et.getType());
    }
    return types.iterator();
}
/**
 * @return java.lang.String
 */
private static java.lang.String getDeleteEntityTypeSql()
{
    return "DELETE FROM " + ENTITY_TYPE_TABLE + " WHERE " +
      TYPE_ID_COLUMN + " = ? AND " + TYPE_NAME_COLUMN + " = ?";
}
/**
 * Interface to the entity types cache.
 * @return java.lang.String
 */
public static String getDescriptiveName(Class type)
{
    return singleton().getDescriptiveNameForType(type);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.String
 */
public String getDescriptiveNameForType(Class type)
{
    EntityType et = (EntityType)getEntityTypesByType().get(type);
    return et.getDescriptiveName();
}
/**
 * Interface to the entity types cache.
 * @return java.lang.Integer
 */
public Integer getEntityIDFromType(Class type)
{
    EntityType et = (EntityType)getEntityTypesByType().get(type);
    return (et == null) ? null : et.getTypeId();
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
public Class getEntityTypeFromID(Integer id)
{
    EntityType et = (EntityType)getEntityTypesByID().get(id);
    return (et == null) ? null : et.getType();
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
private synchronized Map getEntityTypesByID()
{
    return entityTypesByID;
}
private Map cloneHashMap(Map m)
{
    return ((Map)((HashMap)m).clone());
}
/**
 * @return java.util.Map
 */
private synchronized Map getEntityTypesByType()
{
    return entityTypesByType;
}

/**
 * @return java.lang.String
 */
private static String getInsertEntityTypeSql()
{
    return "INSERT INTO " + ENTITY_TYPE_TABLE + " (" + getAllColumnNames() + ") VALUES (?, ?, ?)";
}
/**
 * @return int
 * @exception java.lang.Exception
 */
private int getNextKey() throws java.lang.Exception
{
    return SequenceGenerator.instance().getNextInt(ENTITY_TYPE_TABLE);
}
/**
 * @return java.lang.String
 */
private static java.lang.String getSelectEntityTypesSql()
{
    return "SELECT " + getAllColumnNames() + " FROM " + ENTITY_TYPE_TABLE;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getUpdateEntityTypeSql()
{
    return "UPDATE " + ENTITY_TYPE_TABLE + " SET "  + DESCRIPTIVE_NAME_COLUMN +
      " = ? WHERE " + TYPE_ID_COLUMN + " = ?";
}
/**
 * Cache entityTypes.
 */
private void initialize()
{
    initializeCaches();
    Connection conn = null;
    Integer typeID = null;
    Class entityType = null;
    String description = null;
    EntityType et = null;

    try
    {
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
                    description = rs.getString(3);
                    et = new EntityType(entityType, typeID, description);
                    primAddEntityType(et);
                }
            }
            finally
                { rs.close(); }
        }
        finally
            { stmnt.close(); }
    }
    catch (Exception ex)
        { LogService.log (LogService.ERROR, ex); }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Cache entityTypes.
 */
private void initializeCaches()
{
    entityTypesByID = new HashMap(10);
    entityTypesByType = new HashMap(10);
}
/**
 * Cache entityTypes.
 */
private void insertEntityType(EntityType et) throws SQLException
{
    Connection conn = null;
    RDBMServices.PreparedStatement ps = null;
    try
    {
        conn = RDBMServices.getConnection();
        try
        {
            ps = new RDBMServices.PreparedStatement(conn, getInsertEntityTypeSql());

            ps.setInt(1, et.getTypeId().intValue());
            ps.setString(2, et.getType().getName());
            ps.setString(3, et.getDescriptiveName());

            LogService.log(LogService.DEBUG, "EntityTypes.insertEntityType(): " + ps + "(" +
              et.getTypeId() + ", " + et.getType() + ", " + et.getDescriptiveName() + ")" );

            int rc = ps.executeUpdate();

            if ( rc != 1 )
            {
                String errString = "Problem adding entity type " + et;
                LogService.log (LogService.ERROR, errString);
                throw new SQLException(errString);
            }
        }
        finally
        {
            try
            {
                if (ps != null) { ps.close(); }
            }
            finally
            {
                RDBMServices.releaseConnection(conn); 
            }
        }
    }
    catch (java.sql.SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        throw sqle;
    }
}
/**
 * Copy on write to prevent ConcurrentModificationExceptions.
 */
private void primAddEntityType(EntityType et)
{
    Map typesByType = cloneHashMap(getEntityTypesByType());
    typesByType.put(et.getType(), et);
    Map typesByID = cloneHashMap(getEntityTypesByID());
    typesByID.put(et.getTypeId(), et);
    setEntityTypesByType(typesByType);
    setEntityTypesByID(typesByID);
}
/**
 * Copy on write to prevent ConcurrentModificationExceptions.
 */
private void primRemoveEntityType(EntityType et)
{
    Map typesByType = cloneHashMap(getEntityTypesByType());
    typesByType.remove(et.getType());
    Map typesByID = cloneHashMap(getEntityTypesByID());
    typesByID.remove(et.getTypeId());
    setEntityTypesByType(typesByType);
    setEntityTypesByID(typesByID);
}
/**
 * Interface to the entity types cache.
 * @return java.lang.String
 */
public static synchronized void refresh()
{
    singleton().initialize();
}
public synchronized void setEntityTypesByID(Map m)
{
    entityTypesByID = m;   
}
public synchronized void setEntityTypesByType(Map m)
{
    entityTypesByType = m;   
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
/**
 * Synchronize on update lock to serialize adds, deletes and updates
 * while letting reads proceed.
 */
public void updateEntityType(Class type, String newDescription) throws Exception
{
    synchronized (updateLock) {
        refresh();
        EntityType et = (EntityType)getEntityTypesByType().get(type);
        if ( et == null )
        {
            addEntityType(type, newDescription);
        }
        else
        {
            et.descriptiveName = newDescription;
            updateEntityType(et);
            primAddEntityType(et);
        }
    }
}
/**
 * Cache entityTypes.
 */
private void updateEntityType(EntityType et) throws SQLException
{
    Connection conn = null;
    RDBMServices.PreparedStatement ps = null;
    try
    {
        conn = RDBMServices.getConnection();
        try
        {
            ps = new RDBMServices.PreparedStatement(conn, getUpdateEntityTypeSql());

            ps.setString(1, et.getDescriptiveName());
            ps.setInt(2, et.getTypeId().intValue());

            LogService.log(LogService.DEBUG, "EntityTypes.updateEntityType(): " + ps + "(" +
              et.getType() + ", " + et.getDescriptiveName() + ", " + et.getTypeId() + ")" );

            int rc = ps.executeUpdate();

            if ( rc != 1 )
            {
                String errString = "Problem updating type " + et;
                LogService.log (LogService.ERROR, errString);
                throw new SQLException(errString);
            }
        }
        finally
        {
            try
            {
                if (ps != null) { ps.close(); }
            }
            finally
            {
                RDBMServices.releaseConnection(conn); 
            }
        }
    }
    catch (java.sql.SQLException sqle)
    {
        LogService.log (LogService.ERROR, sqle);
        throw sqle;
    }
}
}
