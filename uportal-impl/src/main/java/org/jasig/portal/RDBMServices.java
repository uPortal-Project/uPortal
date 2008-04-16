/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.rdbm.IDatabaseMetadata;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.MovingAverage;
import org.jasig.portal.utils.MovingAverageSample;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;



/**
 * Provides relational database access and helper methods.
 * A static routine determines if the database/driver supports
 * prepared statements and/or outer joins.
 *
 * <p>This class provides database access as a service.  Via the class, uPortal
 * code can obtain a connection to the core uPortal database as well as to other
 * databases available via JNDI.  (Doing that JNDI lookup directly allows your
 * code to avoid dependence upon this class.)  This class provides
 * traditional getConnection() methods as well as static covers for getting a
 * reference to the backing DataSource.</p>
 *
 * <p>This class also provides helper methods for manipulating connections.
 * Mostof the methods are wrappers around methods on the underlying Connection
 * that handle (log and swallow) the SQLExceptions that the underlying methods
 * declare to be thrown (these helpers also catch
 * and log RuntimeExceptions encountered).  They provide an alternative to trying
 * and catching those methods using the JDBC APIs directly.</p>
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author George Lindholm, george.lindholm@ubc.ca
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @author Susan Bramhall <a href="mailto:susan.bramhall@yale.edu">susan.bramhall@yale.edu</a>
 * @version $Revision$ $Date$
 */
public class RDBMServices {
    /**
     * Name of the default portal database expected in the Spring application context
     */
    public static final String PORTAL_DB = "PortalDb";

    /**
     * Name of the {@link IDatabaseMetadata} expected in the Spring application context
     */
    public static final String PORTAL_DB_METADATA = "PortalDB.metadata";
    
    private static final Log LOG = LogFactory.getLog(RDBMServices.class);

    //DBFlag constants
    private static final String FLAG_TRUE = "Y";
    private static final String FLAG_TRUE_OTHER = "T";
    private static final String FLAG_FALSE = "N";

    // Metric counters
    private static final MovingAverage databaseTimes = new MovingAverage();
    private static MovingAverageSample lastDatabase = new MovingAverageSample();
    private static AtomicInteger activeConnections = new AtomicInteger();
    private static int maxConnections = 0;


    /**
     * Gets the default DataSource. If no server is found
     * a runtime exception will be thrown.  This method will never return null.
     * @return the core uPortal DataSource
     * @throws RuntimeException on failure
     * @deprecated Where possible code should be injected with a {@link DataSource} object via the Spring application context
     */
    @Deprecated
    public static DataSource getDataSource() {
        return getDataSource(PORTAL_DB);
    }

    /**
     * Gets a named DataSource from JNDI, with special handling for the PORTAL_DB
     * datasource. Successful lookups
     * are cached and not done again. Lookup failure is remembered and blocks retry
     * for a
     * number of milliseconds specified by {@link #JNDI_RETRY_TIME} to reduce
     * JNDI overhead and log spam.
     *
     * There are two ways in which we handle the core uPortal DataSource
     * specially.
     *
     * We determine and remember metadata in an DbMetaData object for the core
     * uPortal DataSource.  We do not compute this DbMetaData for any other
     * DataSource.
     *
     * We fall back on using rdbm.properties to construct our core uPortal
     * DataSource in the case where we cannot find it from JNDI.  If the portal
     * property org.jasig.portal.RDBMServices.getDatasourceFromJNDI is true,
     * we first
     * first try to get the connection by looking in the
     * JNDI context for the name defined by the portal property
     * org.jasig.portal.RDBMServices.PortalDatasourceJndiName .
     *
     * If we were not configured to check JNDI or we didn't find it in JNDI having
     * checked, we then fall back on rdbm.properties.
     *
     * @param name The name of the DataSource to get.
     * @return A named DataSource or <code>null</code> if one cannot be found.
     * @deprecated Where possible code should be injected with a {@link DataSource} object via the Spring application context
     */
    @Deprecated
    public static DataSource getDataSource(String name) {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final DataSource dataSource = (DataSource)applicationContext.getBean(name, DataSource.class);
        return dataSource;
    }

    /**
     * @return Return the current number of active connections
     */
    public static int getActiveConnectionCount() {
      return activeConnections.intValue();
    }

    /**
     * @return Return the maximum number of connections
     */
    public static int getMaxConnectionCount() {
      return maxConnections;
    }
    
    public static MovingAverageSample getLastDatabase() {
        return lastDatabase;
    }

    /**
     * Gets a database connection to the portal database.
     * If datasource not available a runtime exception is thrown
     * @return a database Connection object
     * @throws DataAccessException if unable to return a connection
     * @deprecated Where possible code should be injected with a {@link DataSource} object via the Spring application context
     */
    @Deprecated
    public static Connection getConnection() {
    	return getConnection(PORTAL_DB);
    }


    /**
     * Returns a connection produced by a DataSource found in the
     * JNDI context.  The DataSource should be configured and
     * loaded into JNDI by the J2EE container or may be the portal
     * default database.
     *
     * @param dbName the database name which will be retrieved from
     *   the JNDI context relative to "jdbc/"
     * @return a database Connection object or <code>null</code> if no Connection
     * @deprecated Where possible code should be injected with a {@link DataSource} object via the Spring application context
     */
    @Deprecated
    public static Connection getConnection(String dbName) {
		final DataSource dataSource = getDataSource(dbName);

        try {
            final long start = System.currentTimeMillis();
            final Connection c = dataSource.getConnection();
            lastDatabase = databaseTimes.add(System.currentTimeMillis() - start); // metric
            final int current = activeConnections.incrementAndGet();
            if (current > maxConnections) {
                maxConnections = current;
            }
            return c;
        }
        catch (SQLException e) {
            throw new DataAccessResourceFailureException(
                    "RDBMServices sql error trying to get connection to " + dbName, e);
        }
    }

    /**
     * Releases database connection.
     * Unlike the underlying connection.close(), this method does not throw
     * SQLException or any other exception.  It will fail silently from the
     * perspective of calling code, logging errors using Commons Logging.
     * @param con a database Connection object
     * @deprecated Where possible code should be injected with a {@link DataSource} object via the Spring application context
     */
    @Deprecated
    public static void releaseConnection(final Connection con) {
        try {
            activeConnections.decrementAndGet();

            con.close();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error closing Connection: " + con, e);
        }
    }



    //******************************************
    // Utility Methods
    //******************************************

    /**
     * Close a ResultSet
     * @param rs a database ResultSet object
     */
    public static void closeResultSet(final ResultSet rs) {
        try {
            rs.close();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error closing ResultSet: " + rs, e);
        }
    }

    /**
     * Close a Statement
     * @param st a database Statement object
     */
    public static void closeStatement(final Statement st) {
        try {
            st.close();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error closing Statement: " + st, e);
        }
    }

    /**
     * Commit pending transactions.
     * Unlike the underlying commit(), this method does not throw SQLException or
     * any other exception.  It will fail silently from the perspective of calling code,
     * logging any errors using Commons Logging.
     * @param connection
     */
    static final public void commit(final Connection connection) {
        try {
            connection.commit();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error committing Connection: " + connection, e);
        }
    }

    /**
     * Set auto commit state for the connection.
     * Unlike the underlying connection.setAutoCommit(), this method does not
     * throw SQLException or any other Exception.  It fails silently from the
     * perspective of calling code, logging any errors encountered using
     * Commons Logging.
     * @param connection
     * @param autocommit
     */
    public static final void setAutoCommit(final Connection connection, boolean autocommit) {
        try {
            connection.setAutoCommit(autocommit);
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error committing Connection: " + connection + " to: " + autocommit, e);
        }
    }

    /**
     * rollback unwanted changes to the database
     * @param connection
     */
    public static final void rollback(final Connection connection) throws SQLException {
        try {
            connection.rollback();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error rolling back Connection: " + connection, e);
        }
    }



    /**
     * Returns the name of the JDBC driver being used for the default
     * uPortal database connections.
     *
     * @return the name of the JDBC Driver.
     */
    public static String getJdbcDriver () {
        final IDatabaseMetadata dbMetaData = getDbMetaData();
        return dbMetaData.getJdbcDriver();
    }

    /**
     * Gets the JDBC URL of the default uPortal database connections.
     *
     */
    public static String getJdbcUrl () {
        final IDatabaseMetadata dbMetaData = getDbMetaData();
        return dbMetaData.getJdbcUrl();
    }

    /**
     * Get the username under which we are connecting for the default uPortal
     * database connections.
     */
    public static String getJdbcUser () {
        final IDatabaseMetadata dbMetaData = getDbMetaData();
        return dbMetaData.getJdbcUser();
    }


    //******************************************
    // Data Type / Formatting Methods
    //******************************************

    /**
     * Return DB format of a boolean. "Y" for true and "N" for false.
     * @param flag true or false
     * @return either "Y" or "N"
     */
    public static final String dbFlag(final boolean flag) {
        if (flag) {
            return FLAG_TRUE;
        }

        return FLAG_FALSE;
    }

    /**
     * Return boolean value of DB flag, "Y" or "N".
     * @param flag either "Y" or "N"
     * @return boolean true or false
     */
    public static final boolean dbFlag(final String flag) {
        return flag != null && (FLAG_TRUE.equalsIgnoreCase(flag) || FLAG_TRUE_OTHER.equalsIgnoreCase(flag));
    }

    /**
     * Make a string SQL safe
     * @param sql
     * @return SQL safe string
     */
    public static final String sqlEscape(final String sql) {
        if (sql == null) {
            return  "";
        }

        int primePos = sql.indexOf("'");

        if (primePos == -1) {
            return  sql;
        }

        final StringBuffer sb = new StringBuffer(sql.length() + 4);
        int startPos = 0;

        do {
            sb.append(sql.substring(startPos, primePos + 1));
            sb.append("'");
            startPos = primePos + 1;
            primePos = sql.indexOf("'", startPos);
        } while (primePos != -1);

        sb.append(sql.substring(startPos));
        return sb.toString();
    }

    /**
     * Get metadata about the default DataSource.
     * @return metadata about the default DataSource.
     */
    @Deprecated
    public static IDatabaseMetadata getDbMetaData() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IDatabaseMetadata databaseMetadata = (IDatabaseMetadata)applicationContext.getBean(PORTAL_DB_METADATA, IDatabaseMetadata.class);
        return databaseMetadata;
    }
}
