/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.sql.Connection;
import java.util.Date;

import javax.sql.DataSource;

/**
 * This interface represents a single database server. It provides methods
 * to get connections to the server and other meta information about the
 * server.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public interface IDatabaseServer {

    /**
     * Gets a {@link DataSource} which can provide connections and meta
     * information for the database server.
     * 
     * @return A {@link DataSource} for the server.
     */
    public DataSource getDataSource();

    /**
     * Gets a {@link Connection} to the database server. This is generally
     * implemented as a convience method which wraps the calls
     * {@link #getDataSource()} and {@link DataSource#getConnection()}. If
     * a {@link java.sql.SQLException} is thrown while geting the connection
     * <code>null</code> is returned.
     * 
     * @return A {@link Connection} to the database server or <code>null</code> if an error occurs.
     */
    public Connection getConnection();
    
    /**
     * Releases the database {@link Connection}.
     * 
     * @param conn The {@link Connection} to release.
     */
    public void releaseConnection(Connection conn);

    /**
     * Gets the name of the JDBC driver being used for this server.
     * 
     * @return The name of the JDBC driver for the server.
     */
    public String getJdbcDriver();

    /**
     * Gets the JDBC URL used to connect to the server.
     * 
     * @return The JDBC URL for the server.
     */
    public String getJdbcUrl();

    /**
     * Gets the user name used to connect to the server.
     * 
     * @return The user name for the server.
     */
    public String getJdbcUser();

    /**
     * Gets the appropriate {@link IJoinQueryString} implemenation for
     * the database. If {@link #supportsOuterJoins()} returns <code>false</code>
     * this will return <code>null</code>.
     * 
     * @return The appropriate {@link IJoinQueryString} implemenation.
     */
    public IJoinQueryString getJoinQuery();

    /**
     * Returns <code>true</code> if the database server supports outer
     * joins. The query to use if this returns <code>true</code> can
     * be retrieved from the {@link #getJoinQuery()} method.
     * 
     * @return <code>true</code> if the server supports outer joins.
     */
    public boolean supportsOuterJoins();

    /**
     * Returns <code>true</code> if the database server supports transactions.
     * 
     * @return <code>true</code> if the server supports transactions.
     */
    public boolean supportsTransactions();
    
    /**
     * Returns <code>true</code> if the database server supports prepared statements.
     * 
     * @return <code>true</code> if the server supports prepared statements.
     */
    public boolean supportsPreparedStatements();    

    /**
     * SQL TimeStamp format of current time.
     * 
     * @return SQL TimeStamp of the current time.
     */
    public String sqlTimeStamp();

    /**
     * SQL TimeStamp format a long.
     * 
     * @param date The time in milliseconds to format.
     * @return SQL TimeStamp of the specified time.
     */
    public String sqlTimeStamp(long date);

    /**
     * SQL TimeStamp format a Date.
     * 
     * @param date The date to format.
     * @return SQL TimeStamp or "NULL" if date is null.
     */
    public String sqlTimeStamp(Date date);
}