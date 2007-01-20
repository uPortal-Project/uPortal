/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.rdbm.DatabaseMetaDataImpl;
import org.jasig.portal.rdbm.IDatabaseMetadata;
import org.jasig.portal.rdbm.pool.IPooledDataSourceFactory;
import org.jasig.portal.rdbm.pool.PooledDataSourceFactoryFactory;
import org.jasig.portal.utils.MovingAverage;
import org.jasig.portal.utils.MovingAverageSample;
import org.springframework.dao.DataAccessResourceFailureException;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;



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
    public static final String PORTAL_DB = PropertiesManager.getProperty("org.jasig.portal.RDBMServices.PortalDatasourceJndiName", "PortalDb"); // JNDI name for portal database
    public static final String DEFAULT_DATABASE = "DEFAULT_DATABASE";

    private static boolean getDatasourceFromJndi = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.RDBMServices.getDatasourceFromJndi", true);
    private static final Log LOG = LogFactory.getLog(RDBMServices.class);

    //DBFlag constants
    private static final String FLAG_TRUE = "Y";
    private static final String FLAG_TRUE_OTHER = "T";
    private static final String FLAG_FALSE = "N";

    /** Specifies how long to wait before trying to look a JNDI data source that previously failed */
    private static final int JNDI_RETRY_TIME = PropertiesManager.getPropertyAsInt("org.jasig.portal.RDBMServices.jndiRetryDelay", 30000); // JNDI retry delay;

    private static Map namedDataSources =  Collections.synchronizedMap(new HashMap());
    private static Map namedDbServerFailures = Collections.synchronizedMap(new HashMap());

    /* info legacy utilities */
    private static String jdbcUrl;
    private static String jdbcUser;
    private static String jdbcDriver;

    private static IDatabaseMetadata dbMetaData = null;

    // Metric counters
    private static final MovingAverage databaseTimes = new MovingAverage();
    public static MovingAverageSample lastDatabase = new MovingAverageSample();
    private static AtomicInteger activeConnections = new AtomicInteger();
    private static int maxConnections = 0;


    /**
     * Gets the default DataSource. If no server is found
     * a runtime exception will be thrown.  This method will never return null.
     * @return the core uPortal DataSource
     * @throws RuntimeException on failure
     */
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
     */
    public static DataSource getDataSource(String name) {
        final String PROP_FILE = "/properties/rdbm.properties";

        if (DEFAULT_DATABASE.equals(name)) {
            name = PORTAL_DB;
        }

        DataSource ds = (DataSource)namedDataSources.get(name);

        // if already have a dtasource just return it
        if (ds!=null) return ds;

        // For non default database cache the datasource and return it
        if (!PORTAL_DB.equals(name)) {
            ds = getJndiDataSource(name);
            namedDataSources.put(name,ds);
            // For non default database return whatever we have (could be null)
            return ds;
        }

        // portal database is special - create metadata object too.
        if (getDatasourceFromJndi) {
            ds = getJndiDataSource(name);
            if (ds != null) {
                if (LOG.isInfoEnabled())
                    LOG.info("Creating DataSource instance for " + name);
                dbMetaData = new DatabaseMetaDataImpl(ds);
                namedDataSources.put(name, ds);
                return ds;
                }
            }

        // get here if not getDatasourceFromJndi OR jndi lookup returned null
        // try to get datasource via properties
        try {
            final InputStream jdbcPropStream = RDBMServices.class.getResourceAsStream(PROP_FILE);

            try {
                final Properties jdbpProperties = new Properties();
                jdbpProperties.load(jdbcPropStream);

                final IPooledDataSourceFactory pdsf = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();

                final String driverClass = jdbpProperties.getProperty("jdbcDriver");
                final String username = jdbpProperties.getProperty("jdbcUser");
                final String password = jdbpProperties.getProperty("jdbcPassword");
                final String url = jdbpProperties.getProperty("jdbcUrl");
                boolean usePool = true;
                if (jdbpProperties.getProperty("jdbcUsePool")!=null)
                    usePool = Boolean.valueOf(jdbpProperties.getProperty("jdbcUsePool")).booleanValue();


                if (usePool) {
                    //Try using a pooled DataSource
                    try {
                    	final boolean poolPreparedStatements = Boolean.valueOf(jdbpProperties.getProperty("poolPreparedStatements")).booleanValue();
                        ds = pdsf.createPooledDataSource(driverClass, username, password, url, poolPreparedStatements);

                        if (LOG.isInfoEnabled())
                            LOG.info("Creating DataSource instance for pooled JDBC");

                        namedDataSources.put(PORTAL_DB,ds);
                        jdbcUrl = url;
                        jdbcUser = username;
                        jdbcDriver = driverClass;
                        dbMetaData = new DatabaseMetaDataImpl(ds);
                    }
                    catch (Exception e) {
                        LOG.error("Error using pooled JDBC data source.", e);
                    }
                }

                if (ds == null && driverClass != null) {
                    //Pooled DataSource isn't being used or failed during creation
                    try {
                        final Driver d = (Driver)Class.forName(driverClass).newInstance();
                        ds = new GenericDataSource(d, url, username, password);

                        if (LOG.isInfoEnabled())
                            LOG.info("Creating DataSource for JDBC native");

                        namedDataSources.put(PORTAL_DB,ds);
                        jdbcUrl = url;
                        jdbcUser = username;
                        jdbcDriver = driverClass;
                        dbMetaData = new DatabaseMetaDataImpl(ds);
                    }
                    catch (Exception e) {
                        LOG.error("JDBC Driver Creation Failed. (" + driverClass + ")", e);
                    }
                }
            }
            finally {
                jdbcPropStream.close();
            }

        }
        catch (IOException ioe) {
            LOG.error("An error occured while reading " + PROP_FILE, ioe);
        }
        // if we failed to find a datasource then throw a runtime exception
        if (ds == null) {
                throw new RuntimeException("No JDBC DataSource or JNDI DataSource avalable.");
        }
        return ds;
       }

    /**
     * Does the JNDI lookup and returns datasource
     * @param name
     * @return
     */
    private static DataSource getJndiDataSource(String name) {

        final Long failTime = (Long)namedDbServerFailures.get(name);
        DataSource ds = null;

        if (failTime == null || (failTime.longValue() + JNDI_RETRY_TIME) <= System.currentTimeMillis()) {
            if (failTime != null) {
                namedDbServerFailures.remove(name);
            }

            try {
                final Context initCtx = new InitialContext();
                final Context envCtx = (Context)initCtx.lookup("java:comp/env");
                ds = (DataSource)envCtx.lookup("jdbc/" + name);


            }
            catch (Throwable t) {
                //Cache the failure to decrease lookup attempts and reduce log spam.
                namedDbServerFailures.put(name, new Long(System.currentTimeMillis()));
                if (LOG.isWarnEnabled())
                    LOG.warn("Error getting DataSource named (" + name + ") from JNDI.", t);
            }
        }
        else {
            if (LOG.isDebugEnabled()) {
                final long waitTime = (failTime.longValue() + JNDI_RETRY_TIME) - System.currentTimeMillis();
                LOG.debug("Skipping lookup on failed JNDI lookup for name (" + name + ") for approximately " + waitTime + " more milliseconds.");

            }
        }
        return ds;
    }

    /**
     * Return the current number of active connections
     * @return int
     */
    public static int getActiveConnectionCount() {
      return activeConnections.intValue();
    }

    /**
     * Return the maximum number of connections
     * @return int
     */
    public static int getMaxConnectionCount() {
      return maxConnections;
    }

    /**
     * Gets a database connection to the portal database.
     * If datasource not available a runtime exception is thrown
     * @return a database Connection object
     * @throws DataAccessException if unable to return a connection
     */
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
     */
    public static Connection getConnection(String dbName) {
    	if (DEFAULT_DATABASE.equals(dbName)){
    		dbName = PORTAL_DB;
    	}
    	DataSource ds = (DataSource) namedDataSources.get(dbName);
    	if (ds==null) {
    		ds = getDataSource(dbName);
    	}

    	if (ds != null) {
    		try {
    			final long start = System.currentTimeMillis();
    			final Connection c = ds.getConnection();
    			lastDatabase = databaseTimes.add(System.currentTimeMillis() - start); // metric
      			final int current = activeConnections.incrementAndGet();
      			if (current > maxConnections) {
      				maxConnections = current;
      			}
    			return c;
    		} catch (SQLException e) {
    			throw new DataAccessResourceFailureException
    			("RDBMServices sql error trying to get connection to "+dbName,e);
    		}
    	}
    	// datasource is still null so give up
    	throw new DataAccessResourceFailureException("RDBMServices fatally misconfigured such that getDataSource() returned null.");
    }

    /**
     * Releases database connection.
     * Unlike the underlying connection.close(), this method does not throw
     * SQLException or any other exception.  It will fail silently from the
     * perspective of calling code, logging errors using Commons Logging.
     * @param con a database Connection object
     */
    public static void releaseConnection(final Connection con) {
        try {
            int active = activeConnections.decrementAndGet();

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
     * Close a PreparedStatement. Simply delegates the call to
     * {@link #closeStatement(Statement)}
     * @param pst a database PreparedStatement object
     * @deprecated Use {@link #closeStatement(Statement)}.
     */
    public static void closePreparedStatement(final java.sql.PreparedStatement pst) {
        closeStatement(pst);
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
        return jdbcDriver;
    }

    /**
     * Gets the JDBC URL of the default uPortal database connections.
     *
     */
    public static String getJdbcUrl () {
        return jdbcUrl;
    }

    /**
     * Get the username under which we are connecting for the default uPortal
     * database connections.
     */
    public static String getJdbcUser () {
        return jdbcUser;
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
        if (flag)
            return FLAG_TRUE;
        else
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
        else {
            int primePos = sql.indexOf("'");

            if (primePos == -1) {
                return  sql;
            }
            else {
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
        }
    }

    /**
     * Get metadata about the default DataSource.
     * @return metadata about the default DataSource.
     */
    public static IDatabaseMetadata getDbMetaData() {
        if (dbMetaData==null) {
            // if metadata not yet populated, call getDataSource(), which
            // has side effect of populating dbMetaData.
            getDataSource();
        }
        return dbMetaData;
    }



    /**
     * Inner class implementation of DataSource.  We currently construct an instance
     * of this class from the properties defined in rdbm.properties when we are using
     * rdbm.properties to define our core uPortal DataSource.
     * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
     */
    public static class GenericDataSource implements DataSource {

        final private Driver driverRef;
        final private String userName;
        final private String password;
        final private String jdbcUrl;
        final private Properties jdbcProperties = new Properties();
        private PrintWriter log = null;

        /**
         * Create a new {@link GenericDataSource} with the wraps the specified
         * {@link Driver}.
         *
         * @param d The {@link Driver} to wrap.
         */
        public GenericDataSource(final Driver d, final String url, final String user, final String pass) {
            String argErr = "";
            if (d == null) {
                argErr += "Driver cannot be null. ";
            }
            if (url == null) {
                argErr += "url cannot be null. ";
            }
            if (user == null) {
                argErr += "user cannot be null. ";
            }
            if (pass == null) {
                argErr += "pass cannot be null. ";
            }
            if (!argErr.equals("")) {
                throw new IllegalArgumentException(argErr);
            }

            this.driverRef = d;
            this.jdbcUrl = url;
            this.userName = user;
            this.password = pass;

            this.jdbcProperties.put("user", this.userName);
            this.jdbcProperties.put("password", this.password);
        }

        /**
         * @see javax.sql.DataSource#getLoginTimeout()
         */
        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        /**
         * @see javax.sql.DataSource#setLoginTimeout(int)
         */
        public void setLoginTimeout(final int timeout) throws SQLException {
            //NOOP our timeout is always 0
        }

        /**
         * @see javax.sql.DataSource#getLogWriter()
         */
        public PrintWriter getLogWriter() throws SQLException {
            return this.log;
        }

        /**
         * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
         */
        public void setLogWriter(final PrintWriter out) throws SQLException {
            this.log = out;
        }

        /**
         * @see javax.sql.DataSource#getConnection()
         */
        public Connection getConnection() throws SQLException {
            return this.getConnection(this.userName, this.password);
        }

        /**
         * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
         */
        public Connection getConnection(final String user, final String pass) throws SQLException {
            final Properties tempProperties = new Properties();
            tempProperties.putAll(this.jdbcProperties);
            tempProperties.put("user", user);
            tempProperties.put("password", pass);

            return this.driverRef.connect(this.jdbcUrl, tempProperties);
        }

        /**
         * This method was introduced in Java SE 6 as part of the java.sql.Wrapper
         * interface that javax.sql.DataSource was changed to extend from.
         *
         * Returns true if this either implements the interface argument
         * or is directly or indirectly a wrapper for an object that does.
         * Returns false otherwise. If this implements the interface then
         * return true, else if this is a wrapper then return the result
         * of recursively calling <code>isWrapperFor</code> on the wrapped
         * object. If this does not implement the interface and is not a
         * wrapper, return false. This method should be implemented as a
         * low-cost operation compared to <code>unwrap</code> so that
         * callers can use this method to avoid expensive <code>unwrap</code>
         * calls that may fail. If this method returns true then calling
         * <code>unwrap</code> with the same argument should succeed.
         *
         * @param iface a Class defining an interface.
         * @return true if this implements the interface or directly or
         * indirectly wraps an object that does.
         * @throws java.sql.SQLException  if an error occurs while determining
         * whether this is a wrapper for an object with the given interface.
         * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
         */
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return (iface != null && iface.isAssignableFrom(this.getClass()));
		}

        /**
         * This method was introduced in Java SE 6 as part of the java.sql.Wrapper
         * interface that javax.sql.DataSource was changed to extend from.
         *
         * If the receiving object implements the interface passed in, then
         * the receiving object or a wrapper for the receiving object should
         * be returned.
         *
         * @param iface A Class defining an interface that the result must
         * implement
         * @return an object that implements the interface. May be a proxy for
         * the actual implementing object.
         * @throws java.sql.SQLException if a class is not a wrapper for another
         * class and does not implement the interface passed.
         * @see java.sql.Wrapper#unwrap(java.lang.Class)
         */
		public <T> T unwrap(Class<T> iface) throws SQLException {
			if (isWrapperFor(iface)) {
				return iface.cast(this);
			}
			throw new SQLException("org.jasig.portal.RDBMServices.GenericDataSource is not a Wrapper for " + iface.toString());
		}
    }

    /**
     * Wrapper for/Emulator of PreparedStatement class
     * @deprecated Instead of this class a wrapper around the DataSource, Connection and Prepared statement should be done in {@link DatabaseMetaDataImpl}
     */
    public static final class PreparedStatement {
        private String query;
        private String activeQuery;
        private java.sql.PreparedStatement pstmt;
        private Statement stmt;
        private int lastIndex;


        public PreparedStatement(Connection con, String query) throws SQLException {
            this.query = query;
            activeQuery = this.query;
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt = con.prepareStatement(query);
            }
            else {
                stmt = con.createStatement();
            }
        }

        public void clearParameters() throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.clearParameters();
            }
            else {
                lastIndex = 0;
                activeQuery = query;
            }
        }

        public void setDate(int index, java.sql.Date value) throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.setDate(index, value);
            }
            else {
                if (index != lastIndex + 1) {
                    throw new SQLException("Out of order index");
                }
                else {
                    int pos = activeQuery.indexOf("?");
                    if (pos == -1) {
                        throw new SQLException("Missing '?'");
                    }
                    activeQuery = activeQuery.substring(0, pos) + dbMetaData.sqlTimeStamp(value)
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public void setInt(int index, int value) throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.setInt(index, value);
            }
            else {
                if (index != lastIndex + 1) {
                    throw new SQLException("Out of order index");
                }
                else {
                    int pos = activeQuery.indexOf("?");
                    if (pos == -1) {
                        throw new SQLException("Missing '?'");
                    }
                    activeQuery = activeQuery.substring(0, pos) + value
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public void setNull(int index, int sqlType) throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.setNull(index, sqlType);
            }
            else {
                if (index != lastIndex + 1) {
                    throw new SQLException("Out of order index");
                }
                else {
                    int pos = activeQuery.indexOf("?");
                    if (pos == -1) {
                        throw new SQLException("Missing '?'");
                    }
                    activeQuery = activeQuery.substring(0, pos) + "NULL"
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public void setString(int index, String value) throws SQLException {
            if (value == null || value.length() == 0) {
                setNull(index, java.sql.Types.VARCHAR);
            }
            else {
                if (dbMetaData.supportsPreparedStatements()) {
                    pstmt.setString(index, value);
                }
                else {
                    if (index != lastIndex + 1) {
                        throw new SQLException("Out of order index");
                    }
                    else {
                        int pos = activeQuery.indexOf("?");
                        if (pos == -1) {
                            throw new SQLException("Missing '?'");
                        }
                        activeQuery = activeQuery.substring(0, pos) + "'" + sqlEscape(value) + "'"
                                + activeQuery.substring(pos + 1);
                        lastIndex = index;
                    }
                }
            }
        }

        public void setTimestamp(int index, java.sql.Timestamp value) throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.setTimestamp(index, value);
            }
            else {
                if (index != lastIndex + 1) {
                    throw new SQLException("Out of order index");
                }
                else {
                    int pos = activeQuery.indexOf("?");
                    if (pos == -1) {
                        throw new SQLException("Missing '?'");
                    }
                    activeQuery = activeQuery.substring(0, pos) + dbMetaData.sqlTimeStamp(value)
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public ResultSet executeQuery() throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                return pstmt.executeQuery();
            }
            else {
                return stmt.executeQuery(activeQuery);
            }
        }

        public int executeUpdate() throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                return pstmt.executeUpdate();
            }
            else {
                return stmt.executeUpdate(activeQuery);
            }
        }

        public String toString() {
            if (dbMetaData.supportsPreparedStatements()) {
                return query;
            }
            else {
                return activeQuery;
            }
        }

        public void close() throws SQLException {
            if (dbMetaData.supportsPreparedStatements()) {
                pstmt.close();
            }
            else {
                stmt.close();
            }
        }
    }

    /**
     * @return Returns the getDatasourceFromJndi.
     */
    public static boolean isGetDatasourceFromJndi() {
        return getDatasourceFromJndi;
    }
    /**
     * @param getDatasourceFromJndi The getDatasourceFromJndi to set.
     */
    public static void setGetDatasourceFromJndi(boolean getDatasourceFromJndi) {
        RDBMServices.getDatasourceFromJndi = getDatasourceFromJndi;
    }
}
