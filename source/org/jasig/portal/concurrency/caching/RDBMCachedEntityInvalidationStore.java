/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.concurrency.caching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.services.LogService;

/**
 * RDBMS-based store for <code>CachedEntityInvalidations</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.concurrency.caching.CachedEntityInvalidation
 * @see org.jasig.portal.concurrency.IEntityCache
 */
public class RDBMCachedEntityInvalidationStore {
    private static RDBMCachedEntityInvalidationStore singleton;

    // Constants for the underlying table:
    private static String ENTITY_INVALIDATION_TABLE = "UP_ENTITY_CACHE_INVALIDATION";
    private static String ENTITY_TYPE_COLUMN = "ENTITY_TYPE_ID";
    private static String ENTITY_KEY_COLUMN = "ENTITY_KEY";
    private static String INVALIDATION_TIME_COLUMN = "INVALIDATION_TIME";
    private static String ENTITY_CACHE_ID_COLUMN = "ENTITY_CACHE_ID";
    private static String EQ = " = ";
    private static String NE = " != ";
    private static String GT = " > ";
    private static String LT = " < ";
    private static String QUOTE = "'";

    private static String allTableColumns;
    private static String addSql;
    private static String updateSql;

    // Prior to jdk 1.4, java.sql.Timestamp.getTime() truncated milliseconds.
    private static boolean timestampHasMillis;

/**
 * RDBMCachedEntityInvalidationStore constructor.
 */
public RDBMCachedEntityInvalidationStore() throws CachingException
{
    super();
    initialize();
}
/**
 * Adds/updates the row corresponding to this invalidation in the underlying store.
 * @param cachedEnt org.jasig.portal.concurrency.caching.CachedEntityInvalidation
 */
public void add(CachedEntityInvalidation cachedEnt) throws CachingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        if ( existsInStore(cachedEnt, conn) )
            { primUpdate(cachedEnt, conn); }
        else
            { primAdd(cachedEnt, conn); }
    }

    catch (SQLException sqle)
        { throw new CachingException("Problem adding " + cachedEnt + ": " + sqle.getMessage()); }

    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * Adds/updates the row corresponding to this entity in the underlying store.
 * @param entity org.jasig.portal.IBasicEntity entity
 */
public void add(IBasicEntity entity, int cacheID) throws CachingException
{
    CachedEntityInvalidation cachedEnt =
      newInstance(entity.getEntityIdentifier().getType(), entity.getEntityIdentifier().getKey(), new Date(), cacheID);
    add(cachedEnt);
}
/**
 * Delete all invalidations from the underlying store.
 */
public void deleteAll() throws CachingException
{
    Connection conn = null;
    Statement stmnt = null;
    try
    {
        String sql = "DELETE FROM " + ENTITY_INVALIDATION_TABLE;
        LogService.log(LogService.DEBUG, "RDBMInvalidCacheableEntityStore.deleteAll(): " + sql);

        conn = RDBMServices.getConnection();
        try
        {
            stmnt = conn.createStatement();
            int rc = stmnt.executeUpdate(sql);
            String msg = "Deleted " + rc + " rows.";
            LogService.log(LogService.DEBUG, "RDBMInvalidCacheableEntityStore.deleteAll(): " + msg);
        }
        finally
            { if ( stmnt != null ) stmnt.close(); }
    }
    catch (SQLException sqle)
        { throw new CachingException("Problem deleting locks: " + sqle.getMessage()); }

    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * Delete invalid entities the underlying store whose invalidation time is
 * before <code>invalidation</code>.
 *
 * @param expiration java.util.Date
 */
public void deleteBefore(Date expiration) throws CachingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        primDeleteBefore(expiration, conn);
    }

    catch (SQLException sqle)
        { throw new CachingException("Problem deleting invalid entities: " + sqle.getMessage()); }
    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * Answers if this entity is represented in the store.
 * @param ent org.jasig.portal.concurrency.CachedEntityInvalidation
 */
private boolean existsInStore(CachedEntityInvalidation ent, Connection conn)
throws CachingException
{
    return ( select(ent.getType(), ent.getKey(), null, null, conn).length > 0 );
}
 /**
 * Retrieve CachedEntityInvalidations from the underlying entity invalidation store.
 * Either or both of the parameters may be null.
 * @param entityType Class
 * @param entityKey String
 * @exception CachingException - wraps an Exception specific to the store.
 */
public CachedEntityInvalidation[] find (Class entityType, String entityKey )
throws CachingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        return select(entityType, entityKey, null, null, conn);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
 /**
 * Retrieve CachedEntityInvalidations from the underlying store.  Any or all
 * of the parameters may be null.
 * @param invalidation Date
 * @param entityType Class
 * @param entityKey String
 * @param cacheID Integer - the cache ID we do NOT want to retrieve.
 * @exception CachingException - wraps an Exception specific to the store.
 */
public CachedEntityInvalidation[] findAfter (Date invalidation, Class entityType, String entityKey, Integer cacheID)
throws CachingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        return selectAfter(entityType, entityKey, invalidation, cacheID, conn);
    }
    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * SQL for inserting a row into the invalid entities table.
 */
private static String getAddSql()
{
    if ( addSql == null )
    {
        addSql = "INSERT INTO " + ENTITY_INVALIDATION_TABLE +
          "(" + getAllTableColumns() + ") VALUES (?, ?, ?, ?)";
    }
    return addSql;
}
/**
 * @return java.lang.String
 */
private static java.lang.String getAllTableColumns()
{
    if ( allTableColumns == null )
    {
        StringBuffer buff = new StringBuffer(100);
        buff.append(ENTITY_TYPE_COLUMN);
        buff.append(", ");
        buff.append(ENTITY_KEY_COLUMN);
        buff.append(", ");
        buff.append(INVALIDATION_TIME_COLUMN);
        buff.append(", ");
        buff.append(ENTITY_CACHE_ID_COLUMN);

        allTableColumns = buff.toString();
    }
    return allTableColumns;
}
/**
 * SQL for deleting old invalid entity rows.
 */
private static String getDeleteInvalidationsSql()
{
    return "DELETE FROM " + ENTITY_INVALIDATION_TABLE +
          " WHERE " + INVALIDATION_TIME_COLUMN + LT + "?";
}
/**
 * @return java.lang.String
 */
private static java.lang.String getSelectInvalidationsByTypeSql()
{
    return getSelectSql() +
      " WHERE " + ENTITY_TYPE_COLUMN + EQ + "?" +
        " AND " + INVALIDATION_TIME_COLUMN + GT + "?";
}
/**
 * @return java.lang.String
 */
private static java.lang.String getSelectSql()
{
    return ( "SELECT " + getAllTableColumns() + " FROM " + ENTITY_INVALIDATION_TABLE);
}
/**
 * SQL for updating a row on the lock table.
 */
private static String getUpdateSql()
{
    if ( updateSql == null )
    {
        updateSql = "UPDATE " + ENTITY_INVALIDATION_TABLE +
        " SET " + INVALIDATION_TIME_COLUMN + EQ + "?," +
                  ENTITY_CACHE_ID_COLUMN + EQ + "?" +
          " WHERE " + ENTITY_TYPE_COLUMN + EQ + "?" +
            " AND " + ENTITY_KEY_COLUMN + EQ  + "?" ;
    }
    return updateSql;
}
/**
 * Extract values from ResultSet and create a new <code>CachedEntityInvalidation</code>.
 * @return org.jasig.portal.concurrency.locking.CachedEntityInvalidation
 * @param rs java.sql.ResultSet
 */
private CachedEntityInvalidation instanceFromResultSet(java.sql.ResultSet rs)
throws  SQLException, CachingException
{
    CachedEntityInvalidation entity = null;

    Integer entityTypeID = new Integer(rs.getInt(1));
    Class entityType = EntityTypes.getEntityType(entityTypeID);
    String key = rs.getString(2);
    Timestamp ts = rs.getTimestamp(3);
    Date dt = new Date(getTimestampMillis(ts));
    int cacheID = rs.getInt(4);

    return newInstance(entityType, key, dt, cacheID);
}
/**
 * @return org.jasig.portal.concurrency.caching.CachedEntityInvalidation
 */
private CachedEntityInvalidation newInstance (Class type, String key, Date expiration, int cacheID)
{
    return new CachedEntityInvalidation(type, key, expiration, cacheID);
}
/**
 * Add the invalid entity to the underlying store.
 * @param ent org.jasig.portal.concurrency.caching.CachedEntityInvalidation
 * @param conn java.sql.Connection
 */
private void primAdd(CachedEntityInvalidation ent, Connection conn)
throws SQLException, CachingException
{
    Integer typeID = EntityTypes.getEntityTypeID(ent.getType());
    String key = ent.getKey();
    Timestamp ts = new Timestamp(ent.getInvalidationTime().getTime());
    int cacheID = ent.getCacheID();

    try
    {
        RDBMServices.PreparedStatement ps =
            new RDBMServices.PreparedStatement(conn, getAddSql());
        try
        {
            ps.setInt(1, typeID.intValue()); // entity type
            ps.setString(2, key);            // entity key
            ps.setTimestamp(3, ts);          // invalidation time
            ps.setInt(4, cacheID);           // entity cache ID

            LogService.log(LogService.DEBUG,
                "RDBMInvalidCacheableEntityStore.primAdd(): " + ps +
                  " ( " + typeID.intValue() + ", " + key + ", " + ts + " )");

            int rc = ps.executeUpdate();
            if ( rc != 1 )
            {
                String errString = "Problem adding " + ent;
                LogService.log(LogService.ERROR, errString);
                throw new CachingException(errString);
            }
        }
        finally
            { if ( ps != null ) ps.close(); }
    }
    catch (java.sql.SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle);
        throw sqle;
    }
}
/**
 * Delete invalid entities from the underlying store whose invalidation is
 * before <code>invalidation</code>.
 *
 * @param expiration java.util.Date
 * @param conn Connection
 */
private void primDeleteBefore(Date expiration, Connection conn)
throws CachingException, SQLException
{
    Timestamp ts = new Timestamp(expiration.getTime());

    try
    {
        RDBMServices.PreparedStatement ps =
            new RDBMServices.PreparedStatement(conn, getDeleteInvalidationsSql());
        try
        {
            ps.setTimestamp(1, ts);

            LogService.log(LogService.DEBUG,
                "RDBMInvalidCacheableEntityStore.primDeleteBefore(): " + ps + " (" + ts + ")");

            int rc = ps.executeUpdate();
            LogService.log(LogService.DEBUG, "Rows deleted: " + rc);
        }
        finally
            { if ( ps != null ) ps.close(); }
    }

    catch (java.sql.SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle);
        throw sqle;
    }
}
 /**
 * Retrieve CachedEntityInvalidations from the underlying store.
 * @param sql String - the sql string used to select the entity lock rows.
 * @param conn Connection
 * @exception CachingException - wraps an Exception specific to the store.
 */
private CachedEntityInvalidation[] primSelect(String sql, Connection conn) throws CachingException
{
    Statement stmnt = null;
    ResultSet rs = null;
    List entities = new ArrayList();

    LogService.log(LogService.DEBUG, "RDBMInvalidCacheableEntityStore.primSelect(): " + sql);

    try
    {
        stmnt = conn.createStatement();
        try
        {
            rs = stmnt.executeQuery(sql);
            try
            {
                while ( rs.next() )
                    { entities.add(instanceFromResultSet(rs)); }
            }
            finally
                { rs.close(); }
        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle);
        throw new CachingException("Problem retrieving Invalid Entities " + sqle.getMessage());
    }

    return ((CachedEntityInvalidation[])entities.toArray(new CachedEntityInvalidation[entities.size()]));
}
/**
 * Updates the invalid enity's <code>expiration</code> in the underlying store.
 * @param ent org.jasig.portal.concurrency.caching.CachedEntityInvalidation
 * @param conn Connection
 */
private void primUpdate(CachedEntityInvalidation ent, Connection conn)
throws SQLException, CachingException
{
    Integer typeID = EntityTypes.getEntityTypeID(ent.getType());
    String key = ent.getKey();
    java.sql.Timestamp ts = new java.sql.Timestamp(ent.getInvalidationTime().getTime());
    int cacheID = ent.getCacheID();

    try
    {
        RDBMServices.PreparedStatement ps =
            new RDBMServices.PreparedStatement(conn, getUpdateSql());
        try
        {
            ps.setTimestamp(1, ts);           // updated invalidation
            ps.setInt(2, cacheID);            // updated entity cache ID
            ps.setInt(3, typeID.intValue());  // entity type
            ps.setString(4, key);             // entity key

            LogService.log(LogService.DEBUG,
                "RDBMInvalidCacheableEntityStore.primUpdate(): " + ps +
                                  " ( " + typeID.intValue() + ", " + key + ", " + ts + " )");

            int rc = ps.executeUpdate();
            if ( rc != 1 )
            {
                String errString = "Problem updating " + ent;
                LogService.log(LogService.ERROR, errString);
                throw new CachingException(errString);
            }
        }
        finally
            { if ( ps != null ) ps.close(); }
    }
    catch (java.sql.SQLException sqle)
    {
        LogService.log(LogService.ERROR, sqle);
        throw sqle;
    }
}
 /**
 * Retrieve CachedEntityInvalidation from the underlying store.  Any or all of
 * the parameters may be null.
 * @param entityType Class
 * @param entityKey String
 * @param invalidation Date
 * @param conn Connection
 * @exception CachingException - wraps an Exception specific to the store.
 */
private CachedEntityInvalidation[] select
    (Class entityType,
     String entityKey,
     Date invalidation,
     Integer cacheID,
     Connection conn)
throws CachingException
{
    StringBuffer sqlQuery = new StringBuffer( getSelectSql() + " WHERE 1 = 1");

    if ( entityType != null )
    {
        Integer typeID = EntityTypes.getEntityTypeID(entityType);
        sqlQuery.append(" AND " + ENTITY_TYPE_COLUMN + EQ + typeID);
    }

    if ( entityKey != null )
    {
        sqlQuery.append(" AND " + ENTITY_KEY_COLUMN + EQ + sqlQuote(entityKey));
    }

    if ( invalidation != null )
    {
        Timestamp ts = new Timestamp(invalidation.getTime());
        sqlQuery.append(" AND " + INVALIDATION_TIME_COLUMN + EQ + printTimestamp(ts));
    }
    
    if ( cacheID != null )
    {
        sqlQuery.append(" AND " + ENTITY_CACHE_ID_COLUMN + EQ + cacheID);
    }

    return primSelect(sqlQuery.toString(), conn);
}
 /**
 * Retrieve CachedEntityInvalidations from the underlying store.  Any or all
 * of the parameters may be null.
 * @param entityType Class
 * @param entityKey String
 * @param invalidation Date
 * @param cacheID Integer - the cache ID we do NOT want to retrieve.
 * @param conn Connection
 * @exception CachingException - wraps an Exception specific to the store.
 */
private CachedEntityInvalidation[] selectAfter
    (Class entityType,
     String entityKey,
     Date invalidation,
     Integer cacheID,
     Connection conn)
throws CachingException
{
    StringBuffer sqlQuery = new StringBuffer( getSelectSql() + " WHERE 1 = 1");

    if ( entityType != null )
    {
        Integer typeID = EntityTypes.getEntityTypeID(entityType);
        sqlQuery.append(" AND " + ENTITY_TYPE_COLUMN + EQ + typeID);
    }

    if ( entityKey != null )
    {
        sqlQuery.append(" AND " + ENTITY_KEY_COLUMN + EQ + sqlQuote(entityKey));
    }

    if ( invalidation != null )
    {
        Timestamp ts = new Timestamp(invalidation.getTime());
        sqlQuery.append(" AND " + INVALIDATION_TIME_COLUMN + GT + printTimestamp(ts));
    }
    
    if ( cacheID != null )
    {
        sqlQuery.append(" AND " + ENTITY_CACHE_ID_COLUMN + NE + cacheID);
    }

    return primSelect(sqlQuery.toString(), conn);
}
/**
 * @return org.jasig.portal.concurrency.caching.RDBMCachedEntityInvalidationStore
 */
public static synchronized RDBMCachedEntityInvalidationStore singleton()
throws CachingException
{
    if ( singleton == null )
        { singleton = new RDBMCachedEntityInvalidationStore(); }
    return singleton;
}
/**
 * @return java.lang.String
 */
private static java.lang.String sqlQuote(Object o)
{
    return QUOTE + o + QUOTE;
}

/**
 * @return long
 */
private static long getTimestampMillis(Timestamp ts)
{
    if ( timestampHasMillis )
        { return ts.getTime(); }
    else
        { return (ts.getTime() + ts.getNanos() / 1000000); }
}

/**
 * Clear invalidations more than 1 hour old.
 */
private void initialize() throws CachingException
{
    Date anHourAgo = new Date( System.currentTimeMillis() - 60 * 60 * 1000 );
    deleteBefore(anHourAgo);
    Date testDate = new Date();
    Timestamp testTimestamp = new Timestamp(testDate.getTime());
    timestampHasMillis = (testDate.getTime() == testTimestamp.getTime());
}

/**
 * @return java.lang.String
 */
private static java.lang.String printTimestamp(Timestamp ts)
{
    return RDBMServices.sqlTimeStamp(getTimestampMillis(ts));
}
}
