/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency.locking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;

/**
 * RDBMS-based store for <code>IEntityLocks</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class RDBMEntityLockStore implements IEntityLockStore {
    private static final Log log = LogFactory.getLog(RDBMEntityLockStore.class);
    private static IEntityLockStore singleton;

    // Constants for the LOCK table:
    private static String LOCK_TABLE = "UP_ENTITY_LOCK";
    private static String ENTITY_TYPE_COLUMN = "ENTITY_TYPE_ID";
    private static String ENTITY_KEY_COLUMN = "ENTITY_KEY";
    private static String EXPIRATION_TIME_COLUMN = "EXPIRATION_TIME";
    private static String LOCK_OWNER_COLUMN = "LOCK_OWNER";
    private static String LOCK_TYPE_COLUMN = "LOCK_TYPE";
    private static String EQ = " = ";
    private static String GT = " > ";
    private static String LT = " < ";
    private static String QUOTE = "'";

    private static String allLockColumns;
    private static String addSql;
    private static String deleteLockSql;
    private static String updateSql;

    // Prior to jdk 1.4, java.sql.Timestamp.getTime() truncated milliseconds.
    private static boolean timestampHasMillis;
    static {
      Date testDate = new Date();
      Timestamp testTimestamp = new Timestamp(testDate.getTime());
      timestampHasMillis = (testDate.getTime() == testTimestamp.getTime());
    }
/**
 * RDBMEntityGroupStore constructor.
 */
public RDBMEntityLockStore() throws LockingException
{
    super();
    initialize();
}

/**
 * Adds the lock to the underlying store.
 * @param lock
 */
public void add(IEntityLock lock) throws LockingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        primDeleteExpired(new Date(), lock.getEntityType(), lock.getEntityKey(), conn);
        primAdd(lock, conn);
    }

    catch (SQLException sqle)
        { throw new LockingException("Problem creating " + lock, sqle); }

    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * If this IEntityLock exists, delete it.
 * @param lock
 */
public void delete(IEntityLock lock) throws LockingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        primDelete(lock, conn);
    }

    catch (SQLException sqle)
        { throw new LockingException("Problem deleting " + lock, sqle); }
    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * Delete all IEntityLocks from the underlying store.
 */
public void deleteAll() throws LockingException
{
    Connection conn = null;
    Statement stmnt = null;
    try
    {
        String sql = "DELETE FROM " + LOCK_TABLE;
        if (log.isDebugEnabled())
            log.debug("RDBMEntityLockStore.deleteAll(): " + sql);

        conn = RDBMServices.getConnection();
        try
        {
            stmnt = conn.createStatement();
            int rc = stmnt.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                String msg = "Deleted " + rc + " locks.";
                log.debug("RDBMEntityLockStore.deleteAll(): " + msg);
            }
        }
        finally
            { if ( stmnt != null ) stmnt.close(); }
    }
    catch (SQLException sqle)
        { throw new LockingException("Problem deleting locks", sqle); }

    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * Delete all expired IEntityLocks from the underlying store.
 * @param expiration
 */
public void deleteExpired(Date expiration) throws LockingException
{
    deleteExpired(expiration, null, null);
}

/**
 * Delete IEntityLocks from the underlying store that have expired as of
 * <code>expiration</code>.  Params <code>entityType</code> and
 * <code>entityKey</code> are optional.
 *
 * @param expiration java.util.Date
 * @param entityType Class
 * @param entityKey String
 */
public void deleteExpired(Date expiration, Class entityType, String entityKey)
throws LockingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        primDeleteExpired(expiration, entityType, entityKey, conn);
    }

    catch (SQLException sqle)
        { throw new LockingException("Problem deleting expired locks", sqle); }
    finally
        { RDBMServices.releaseConnection(conn); }
}

/**
 * Delete all expired IEntityLocks from the underlying store.
 * @param lock IEntityLock
 */
public void deleteExpired(IEntityLock lock) throws LockingException
{
    deleteExpired(new Date(), lock.getEntityType(), lock.getEntityKey());
}

 /**
 * Retrieve IEntityLocks from the underlying store.  Any or all of the parameters
 * may be null.
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param expiration Date
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] find
    (Class entityType,
    String entityKey,
    Integer lockType,
    Date expiration,
    String lockOwner )
throws LockingException
{
    return select(entityType, entityKey, lockType, expiration, lockOwner);
}

 /**
 * Retrieve IEntityLocks from the underlying store.  Expiration must not be null.
 * @param expiration Date
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] findUnexpired (
    Date expiration,
    Class entityType,
    String entityKey,
    Integer lockType,
    String lockOwner)
throws LockingException
{
    Timestamp ts = new Timestamp(expiration.getTime());
    return selectUnexpired(ts, entityType, entityKey, lockType, lockOwner);
}

/**
 * SQL for inserting a row into the lock table.
 */
private static String getAddSql()
{
    if ( addSql == null )
    {
        addSql = "INSERT INTO " + LOCK_TABLE +
          "(" + getAllLockColumns() + ") VALUES (?, ?, ?, ?, ?)";
    }
    return addSql;
}

/**
 * @return java.lang.String
 */
private static java.lang.String getAllLockColumns()
{
    if ( allLockColumns == null )
    {
        StringBuffer buff = new StringBuffer(100);
        buff.append(ENTITY_TYPE_COLUMN);
        buff.append(", ");
        buff.append(ENTITY_KEY_COLUMN);
        buff.append(", ");
        buff.append(LOCK_TYPE_COLUMN);
        buff.append(", ");
        buff.append(EXPIRATION_TIME_COLUMN);
        buff.append(", ");
        buff.append(LOCK_OWNER_COLUMN);

        allLockColumns = buff.toString();
    }
    return allLockColumns;
}

/**
 * SQL for deleting a row on the lock table.
 */
private static String getDeleteLockSql()
{
    if ( deleteLockSql == null )
    {
        deleteLockSql = "DELETE FROM " + LOCK_TABLE +
          " WHERE " + ENTITY_TYPE_COLUMN + EQ + "?" +
            " AND " + ENTITY_KEY_COLUMN  + EQ + "?" +
            " AND " + EXPIRATION_TIME_COLUMN + EQ + "?" +
            " AND " + LOCK_TYPE_COLUMN + EQ + "?" +
            " AND " + LOCK_OWNER_COLUMN + EQ + "?";
    }
    return deleteLockSql;
}

/**
 * @return java.lang.String
 */
private static java.lang.String getSelectSql()
{
    return ( "SELECT " + getAllLockColumns() + " FROM " + LOCK_TABLE);
}

/**
 * SQL for updating a row on the lock table.
 */
private static String getUpdateSql()
{
    if ( updateSql == null )
    {
        updateSql = "UPDATE " + LOCK_TABLE +
        " SET " + EXPIRATION_TIME_COLUMN + EQ + "?, " + LOCK_TYPE_COLUMN + EQ + "?"+
          " WHERE " + ENTITY_TYPE_COLUMN + EQ + "?" +
            " AND " + ENTITY_KEY_COLUMN + EQ  + "?" +
            " AND " + LOCK_OWNER_COLUMN + EQ  + "?" +
            " AND " + EXPIRATION_TIME_COLUMN + EQ + "?" +
            " AND " + LOCK_TYPE_COLUMN + EQ + "?";
    }
    return updateSql;
}

/**
 * Cleanup the store by deleting locks expired an hour ago.
 */
private void initialize()throws LockingException
{
    Date expiration = new Date(System.currentTimeMillis() - (60*60*1000));
    deleteExpired(expiration, null, null);
}

/**
 * Extract values from ResultSet and create a new lock.
 * @return org.jasig.portal.groups.IEntityLock
 * @param rs java.sql.ResultSet
 */
private IEntityLock instanceFromResultSet(java.sql.ResultSet rs)
throws  SQLException, LockingException
{
    Integer entityTypeID = new Integer(rs.getInt(1));
    Class entityType = EntityTypes.getEntityType(entityTypeID);
    String key = rs.getString(2);
    int lockType = rs.getInt(3);
    Timestamp ts = rs.getTimestamp(4);
    String lockOwner = rs.getString(5);

    return newInstance(entityType, key, lockType, ts, lockOwner);
}

/**
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 */
private IEntityLock newInstance (
    Class entityType,
    String entityKey,
    int lockType,
    Date expirationTime,
    String lockOwner ) throws LockingException
{
    return new EntityLockImpl(entityType, entityKey, lockType, expirationTime, lockOwner);
}

/**
 * Add the lock to the underlying store.
 * @param lock org.jasig.portal.concurrency.locking.IEntityLock
 * @param conn java.sql.Connection
 */
private void primAdd(IEntityLock lock, Connection conn)
throws SQLException, LockingException
{
    Integer typeID = EntityTypes.getEntityTypeID(lock.getEntityType());
    String key = lock.getEntityKey();
    int lockType = lock.getLockType();
    Timestamp ts = new Timestamp(lock.getExpirationTime().getTime());
    String owner = lock.getLockOwner();

    try
    {
        PreparedStatement ps =
            conn.prepareStatement(getAddSql());
        try
        {
            ps.setInt(1, typeID.intValue()); // entity type
            ps.setString(2, key);            // entity key
            ps.setInt(3, lockType);          // lock type
            ps.setTimestamp(4, ts);          // lock expiration
            ps.setString(5, owner);          // lock owner

            if (log.isDebugEnabled())
                log.debug(
                        "RDBMEntityLockStore.primAdd(): " + ps);

            int rc = ps.executeUpdate();
            if ( rc != 1 )
            {
                String errString = "Problem adding " + lock;
                log.error( errString);
                throw new LockingException(errString);
            }
        }
        finally
            { if ( ps != null ) ps.close(); }
    }
    catch (java.sql.SQLException sqle)
    {
        log.error(sqle, sqle);
        throw sqle;
    }
}

/**
 * Delete the IEntityLock from the underlying store.
 * @param lock
 * @param conn the database connection
 */
private void primDelete(IEntityLock lock, Connection conn) throws LockingException, SQLException
{
    Integer typeID = EntityTypes.getEntityTypeID(lock.getEntityType());
    String key = lock.getEntityKey();
    int lockType = lock.getLockType();
    Timestamp ts = new Timestamp(lock.getExpirationTime().getTime());
    String owner = lock.getLockOwner();

    try
    {
        PreparedStatement ps =
            conn.prepareStatement(getDeleteLockSql());
        try
        {
            ps.setInt(1, typeID.intValue());  // entity type
            ps.setString(2, key);             // entity key
            ps.setTimestamp(3, ts);           // lock expiration
            ps.setInt(4, lockType)   ;        // lock type
            ps.setString(5, owner);           // lock owner

            if (log.isDebugEnabled())
                log.debug(
                        "RDBMEntityLockStore.primDelete(): " + ps);

            int rc = ps.executeUpdate();
            if (log.isDebugEnabled())
                log.debug("RDBMEntityLockStore.primDelete(): deleted " + rc + " lock(s).");
        }
        finally
            { if ( ps != null ) ps.close(); }
    }
    catch (java.sql.SQLException sqle)
    {
        log.error(sqle, sqle);
        throw sqle;
    }
}

/**
 * Delete IEntityLocks from the underlying store that have expired as of
 * <code>expiration</code>.  Params <code>entityType</code> and
 * <code>entityKey</code> are optional.
 *
 * @param expiration java.util.Date
 * @param entityType Class
 * @param entityKey String
 * @param conn Connection
 */
private void primDeleteExpired(Date expiration, Class entityType, String entityKey, Connection conn)
throws LockingException, SQLException
{
    Statement stmnt = null;
    Timestamp ts = new Timestamp(expiration.getTime());

    StringBuffer buff = new StringBuffer(100);
    buff.append("DELETE FROM " + LOCK_TABLE + " WHERE " + EXPIRATION_TIME_COLUMN + LT);
    buff.append(printTimestamp(ts));
    if ( entityType != null )
    {
        Integer typeID = EntityTypes.getEntityTypeID(entityType);
        buff.append(" AND " + ENTITY_TYPE_COLUMN + EQ + typeID);
    }
    if ( entityKey != null )
    {
        buff.append(" AND " + ENTITY_KEY_COLUMN + EQ + sqlQuote(entityKey));
    }

    String sql = buff.toString();

    if (log.isDebugEnabled())
        log.debug("RDBMEntityLockStore.deleteExpired(): " + sql);

    try
    {
        stmnt = conn.createStatement();
        int rc = stmnt.executeUpdate(sql);
        if (log.isDebugEnabled()) {
            String msg = "Deleted " + rc + " expired locks.";
            log.debug("RDBMEntityLockStore.deleteExpired(): " + msg);
        }

    }

    catch (SQLException sqle)
        { throw new LockingException("Problem deleting expired locks", sqle); }

    finally
        { if ( stmnt != null ) stmnt.close(); }
}
 /**
 * Retrieve IEntityLocks from the underlying store.
 * @param sql String - the sql string used to select the entity lock rows.
 * @exception LockingException - wraps an Exception specific to the store.
 */
private IEntityLock[] primSelect(String sql) throws LockingException
{
    Connection conn = null;
    Statement stmnt = null;
    ResultSet rs = null;
    List locks = new ArrayList();

    if (log.isDebugEnabled())
        log.debug("RDBMEntityLockStore.primSelect(): " + sql);

    try
    {
        conn = RDBMServices.getConnection();
        stmnt = conn.createStatement();
        try
        {
            rs = stmnt.executeQuery(sql);
            try
            {
                while ( rs.next() )
                    { locks.add(instanceFromResultSet(rs)); }
            }
            finally
                { rs.close(); }
        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        log.error(sqle, sqle);
        throw new LockingException("Problem retrieving EntityLocks", sqle);
    }
    finally
        { RDBMServices.releaseConnection(conn); }

    return ((IEntityLock[])locks.toArray(new IEntityLock[locks.size()]));
}
/**
 * Updates the lock's <code>expiration</code> and <code>lockType</code> in the
 * underlying store.  The SQL is over-qualified to make sure the row has not been
 * updated since the lock was last checked.
 * @param lock
 * @param newExpiration java.util.Date
 * @param newType Integer
 * @param conn Connection
 */
private void primUpdate(IEntityLock lock, Date newExpiration, Integer newType, Connection conn)
throws SQLException, LockingException
{
    Integer typeID = EntityTypes.getEntityTypeID(lock.getEntityType());
    String key = lock.getEntityKey();
    int oldLockType = lock.getLockType();
    int newLockType = ( newType == null ) ? oldLockType : newType.intValue();
    java.sql.Timestamp oldTs = new java.sql.Timestamp(lock.getExpirationTime().getTime());
    java.sql.Timestamp newTs = new java.sql.Timestamp(newExpiration.getTime());
    String owner = lock.getLockOwner();

    try
    {
        PreparedStatement ps =
            conn.prepareStatement(getUpdateSql());
        try
        {
            ps.setTimestamp(1, newTs);  // new expiration
            ps.setInt(2, newLockType);  // new lock type
            ps.setInt(3, typeID.intValue());  // entity type
            ps.setString(4, key);       // entity key
            ps.setString(5, owner);     // lock owner
            ps.setTimestamp(6, oldTs);  // old expiration
            ps.setInt(7, oldLockType);  // old lock type;

            if (log.isDebugEnabled())
                log.debug(
                        "RDBMEntityLockStore.primUpdate(): " + ps);

            int rc = ps.executeUpdate();
            if ( rc != 1 )
            {
                String errString = "Problem updating " + lock;
                log.error( errString);
                throw new LockingException(errString);
            }
        }
        finally
            { if ( ps != null ) ps.close(); }
    }
    catch (java.sql.SQLException sqle)
    {
        log.error(sqle, sqle);
        throw sqle;
    }
}
 /**
 * Retrieve IEntityLocks from the underlying store.  Any or all of the parameters
 * may be null.
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param expiration Date
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
private IEntityLock[] select
    (Class entityType,
    String entityKey,
    Integer lockType,
    Date expiration,
    String lockOwner )
throws LockingException
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

    if ( lockType != null )
    {
        sqlQuery.append(" AND " + LOCK_TYPE_COLUMN + EQ + lockType);
    }

    if ( expiration != null )
    {
        Timestamp ts = new Timestamp(expiration.getTime());
        sqlQuery.append(" AND " + EXPIRATION_TIME_COLUMN + EQ + printTimestamp(ts));
    }

    if ( lockOwner != null )
    {
        sqlQuery.append(" AND " + LOCK_OWNER_COLUMN + EQ + sqlQuote(lockOwner));
    }

    return primSelect(sqlQuery.toString());
}
 /**
 * Retrieve IEntityLocks from the underlying store.  Expiration must not be null.
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
private IEntityLock[] selectUnexpired
    (Timestamp ts,
    Class entityType,
    String entityKey,
    Integer lockType,
    String lockOwner)
throws LockingException
{
    StringBuffer sqlQuery = new StringBuffer(getSelectSql());

    sqlQuery.append(" WHERE " + EXPIRATION_TIME_COLUMN + GT + printTimestamp(ts));

    if ( entityType != null )
    {
        Integer typeID = EntityTypes.getEntityTypeID(entityType);
        sqlQuery.append(" AND " + ENTITY_TYPE_COLUMN + EQ + typeID);
    }

    if ( entityKey != null )
    {
        sqlQuery.append(" AND " + ENTITY_KEY_COLUMN + EQ + sqlQuote(entityKey));
    }

    if ( lockType != null )
    {
        sqlQuery.append(" AND " + LOCK_TYPE_COLUMN + EQ + lockType);
    }

    if ( lockOwner != null )
    {
        sqlQuery.append(" AND " + LOCK_OWNER_COLUMN + EQ + sqlQuote(lockOwner));
    }

    return primSelect(sqlQuery.toString());
}
/**
 * @return org.jasig.portal.concurrency.locking.RDBMEntityLockStore
 */
public static synchronized IEntityLockStore singleton() throws LockingException
{
    if ( singleton == null )
        { singleton = new RDBMEntityLockStore(); }
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
 * @param lock org.jasig.portal.groups.IEntityLock
 * @param newExpiration java.util.Date
 */
public void update(IEntityLock lock, java.util.Date newExpiration)
throws LockingException
{
    update(lock, newExpiration, null);
}
/**
 * Updates the lock's <code>expiration</code> and <code>lockType</code> in the
 * underlying store.  Param <code>lockType</code> may be null.
 * @param lock
 * @param newExpiration java.util.Date
 * @param newLockType Integer
 */
public void update(IEntityLock lock, Date newExpiration, Integer newLockType)
throws LockingException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        if ( newLockType != null )
            { primDeleteExpired(new Date(), lock.getEntityType(), lock.getEntityKey(), conn); }
        primUpdate(lock, newExpiration, newLockType, conn);
    }

    catch (SQLException sqle)
        { throw new LockingException("Problem updating " + lock, sqle); }
    finally
        { RDBMServices.releaseConnection(conn); }
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
 * @return java.lang.String
 */
private static java.lang.String printTimestamp(Timestamp ts)
{
    return RDBMServices.sqlTimeStamp(getTimestampMillis(ts));
}
}
