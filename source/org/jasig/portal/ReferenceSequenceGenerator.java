/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceSequenceGenerator implements ISequenceGenerator
{

    private static final Log log = LogFactory.getLog(ReferenceSequenceGenerator.class);

    /*
     * Private exception identifies problem incrementing counter likely
     * because of concurrent access.
     */
    private class DataIntegrityException extends SQLException {
        DataIntegrityException(String msg) {
            super(msg);
        }
    }
    private Random rand = new Random();

    // Constant strings for SEQUENCE table:
    private static String SEQUENCE_TABLE = "UP_SEQUENCE";
    private static String NAME_COLUMN = "SEQUENCE_NAME";
    private static String VALUE_COLUMN = "SEQUENCE_VALUE";
    private static int INITIAL_COUNTER_VALUE = 0;
    private static int NO_COUNTER_VALUE = -1;

    // SQL strings for SEQUENCE crud:
    private static String QUOTE = "'";
    private static String EQ = " = ";
    private static String DEFAULT_COUNTER_NAME = "DEFAULT";
    private static String selectCounterSql;
    private static String updateCounterSql;
    private static String updateCounterForIncrementSql;
/**
 * ReferenceOIDGenerator constructor comment.
 */
public ReferenceSequenceGenerator() {
    super();
}
/**
 * @param tableName java.lang.String
 * @exception SQLException
 */
public synchronized void createCounter (String tableName) throws SQLException
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        createCounter(tableName, conn);
    }
    finally
    {
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * @param tableName java.lang.String
 * @exception SQLException
 */
private void createCounter (String tableName, Connection conn) throws SQLException
{
    Statement stmt = null;
    try
    {
        stmt = conn.createStatement();
        String sql = getCreateCounterSql(tableName);

        if (log.isDebugEnabled())
            log.debug("ReferenceSequenceGenerator.createCounter: " + sql);

        int rc = stmt.executeUpdate(getCreateCounterSql(tableName));
        if (rc != 1)
            { throw new DataIntegrityException("Data integrity error; could not update counter."); }

    }
    catch ( SQLException sqle )
    {
        log.error("ReferenceSequenceGenerator::createCounter()", sqle);
        throw sqle;
    }
    finally
    {
        if ( stmt != null )
            { stmt.close(); }
    }
}
/**
 * @param table java.lang.String
 * @return java.lang.String
 */
private String getCreateCounterSql(String table)
{
    StringBuffer buff = new StringBuffer(100);
    buff.append("INSERT INTO ");
    buff.append(SEQUENCE_TABLE);
    buff.append(" (");
    buff.append(NAME_COLUMN);
    buff.append(", ");
    buff.append(VALUE_COLUMN);
    buff.append(") VALUES (");
    buff.append(sqlQuote(table));
    buff.append(", ");
    buff.append(INITIAL_COUNTER_VALUE);
    buff.append(")");
    return buff.toString();
}
/**
 * @return int
 * @param tableName java.lang.String
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
private int getCurrentCounterValue(String tableName, Connection conn)
throws SQLException
{
    ResultSet rs = null;
    PreparedStatement ps = null;
    try
    {
        ps = conn.prepareStatement(getSelectCounterSql() );
        try
        {
            ps.setString(1, tableName);
            if (log.isDebugEnabled())
                log.debug("ReferenceSequenceGenerator.getNextInt(): " + ps + 
                        " (" + tableName + ")");
            rs = ps.executeQuery();
            int currentInt = ( rs.next() ) ? rs.getInt(VALUE_COLUMN) : NO_COUNTER_VALUE;
            return currentInt;
        }
        finally
        {
            if ( rs != null )
                { rs.close(); }
        }
    }

    finally
    {
        if ( ps != null )
            { ps.close(); }
    }
}
/**
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext() throws Exception {
    return getNext(DEFAULT_COUNTER_NAME);
}
/**
 * @param table String
 * @return java.lang.String
 * @exception java.lang.Exception
 */
public String getNext(String table) throws Exception {
    return getNextInt(table) + "";
}
/**
 * @return int
 * @exception java.lang.Exception
 */
public int getNextInt() throws Exception {
    return getNextInt(DEFAULT_COUNTER_NAME);
}
/**
 * Increments the counter and returns the incremented value.  If the counter
 * does not exist, creates and then increments it to verify that it has been
 * created successfully.
 * @return int
 * @param tableName java.lang.String
 * @exception java.lang.Exception
 */
public synchronized int getNextInt(String tableName) throws Exception
{
    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        int current = getCurrentCounterValue(tableName, conn);

        if ( current == NO_COUNTER_VALUE )
        {
            createCounter(tableName, conn);
            current = INITIAL_COUNTER_VALUE;
        }

        return incrementCounter(tableName, current, conn);
    }

    catch ( SQLException sqle )
    {
        log.error("ReferenceSequenceGenerator.getNextInt()", sqle);
        throw sqle;
    }

    finally
        { RDBMServices.releaseConnection(conn); }
}
/**
 * @return java.lang.String
 */
private String getSelectCounterSql()
{
    if ( selectCounterSql == null )
    {
        selectCounterSql = "SELECT " + VALUE_COLUMN + " FROM " + SEQUENCE_TABLE +
        " WHERE " + NAME_COLUMN + EQ + "?";
    }
    return selectCounterSql;
}
/**
 * @return java.lang.String
 */
private String getUpdateCounterForIncrementSql()
{
    if ( updateCounterForIncrementSql == null )
    {
        updateCounterForIncrementSql =
          "UPDATE " + SEQUENCE_TABLE + " SET " + VALUE_COLUMN + EQ + " ? " +
          " WHERE " + NAME_COLUMN + EQ + "? AND " + VALUE_COLUMN + EQ + "?";
    }
    return updateCounterForIncrementSql;
}
/**
 * @return java.lang.String
 */
private String getUpdateCounterSql()
{
    if ( updateCounterSql == null )
    {
        updateCounterSql =
          "UPDATE " + SEQUENCE_TABLE + " SET " + VALUE_COLUMN + EQ + " ? " +
          " WHERE " + NAME_COLUMN + EQ + "?";
    }
    return updateCounterSql;
}
/**
 * Try to increment the counter for <code>tableName</code>.  If we catch a
 * <code>DataIntegrityException</code> -- which probably means some other
 * process is trying to increment the counter at the same time -- sleep
 * for a while and then try again, up to 20 times.
 *
 * @param tableName java.lang.String
 * @param currentCounterValue
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
private int incrementCounter(String tableName, int currentCounterValue, Connection conn)
throws Exception
{
    int current = currentCounterValue;
    boolean incremented=false;
    for (int i=0; i<20 && ! incremented; i++)
    {
        try
        {
            primIncrementCounter(tableName, current, conn);
            incremented = true;
        }
        catch ( DataIntegrityException die )
        {
            Thread.sleep(rand.nextInt(2000));
            current = getCurrentCounterValue(tableName, conn);
        }
    }
    if ( incremented )
        { return ++current; }
    else
        { throw new DataIntegrityException("Could not increment counter."); }
}
/**
 * @param tableName java.lang.String
 * @param currentCounterValue
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
private void primIncrementCounter(String tableName, int currentCounterValue, Connection conn)
throws SQLException
{
    PreparedStatement ps = null;
    int nextCounterValue = currentCounterValue + 1;
    try
    {
        ps = conn.prepareStatement(getUpdateCounterForIncrementSql() );
        try
        {
            ps.setInt(1, nextCounterValue);
            ps.setString(2, tableName);
            ps.setInt(3, currentCounterValue);
            if (log.isDebugEnabled())
                log.debug( "ReferenceSequenceGenerator.primIncrementCounter(): " + ps +
                  "(" + nextCounterValue + ", " + tableName + ", " + currentCounterValue + ")");
            int rc = ps.executeUpdate();
            if (rc != 1)
                { throw new DataIntegrityException("Data integrity error; could not update counter."); }
        }
        catch (SQLException sqle)
        {
            log.error(sqle.getMessage(), sqle);
            throw sqle;
        }
    }

    finally
    {
        if ( ps != null )
            { ps.close(); }
    }
}
/**
 * @param tableName java.lang.String
 * @param newCounterValue int
 * @exception java.lang.Exception
 */
public synchronized void setCounter (String tableName, int newCounterValue) throws Exception
{

    Connection conn = null;
    try
    {
        conn = RDBMServices.getConnection();
        setCounter(tableName, newCounterValue, conn);
    }

    catch ( SQLException sqle )
    {
        log.error("ReferenceSequenceGenerator::setCounter()", sqle);
        throw sqle;
    }

    finally
    {
        RDBMServices.releaseConnection(conn);
    }
}
/**
 * @param tableName java.lang.String
 * @param newCounterValue
 * @param conn java.sql.Connection
 * @exception java.sql.SQLException
 */
private void setCounter(String tableName, int newCounterValue, Connection conn)
throws SQLException
{
    PreparedStatement ps = null;
    try
    {
        ps = conn.prepareStatement(getUpdateCounterSql() );
        try
        {
            ps.setInt(1, newCounterValue);
            ps.setString(2, tableName);
            if (log.isDebugEnabled())
                log.debug("ReferenceSequenceGenerator.setCounter(): " + ps + 
                        "(" + newCounterValue + ", " + tableName + ")");
            int rc = ps.executeUpdate();
            if (rc != 1)
                { throw new SQLException("Data integrity error; could not update counter."); }
         }
        catch (SQLException sqle)
        {
            log.error("Error setting counter for table [" + tableName + "] " +
                    "to " + newCounterValue, sqle);
            throw sqle;
        }
    }

    finally
    {
        if ( ps != null )
            { ps.close(); }
    }
}
/**
 * @return java.lang.String
 */
private static java.lang.String sqlQuote(Object o)
{
    return QUOTE + o + QUOTE;
}
}