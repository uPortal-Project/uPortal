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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.rdbm.DatabaseServerImpl;
import org.jasig.portal.rdbm.IDatabaseServer;
import org.jasig.portal.rdbm.IJoinQueryString;
import org.jasig.portal.rdbm.JoinQueryString;
import org.jasig.portal.rdbm.pool.IPooledDataSourceFactory;
import org.jasig.portal.rdbm.pool.PooledDataSourceFactoryFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;



/**
 * Provides relational database access and helper methods.
 * A static routine determins if the database/driver supports
 * prepared statements and/or outer joins.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author George Lindholm, george.lindholm@ubc.ca
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class RDBMServices {
    public static final String PORTAL_DB = PropertiesManager.getProperty("org.jasig.portal.RDBMServices.PortalDatasourceJndiName", "PortalDb"); // JNDI name for portal database
    public static final String PERSON_DB = PropertiesManager.getProperty("org.jasig.portal.RDBMServices.PersonDatasourceJndiName", "PersonDb"); // JNDI name for person database
    public static final String DEFAULT_DATABASE = "DEFAULT_DATABASE";

    private static final boolean getDatasourceFromJndi = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.RDBMServices.getDatasourceFromJndi", true);
    private static final Log LOG = LogFactory.getLog(RDBMServices.class);

    //DBFlag constants
    private static final String FLAG_TRUE = "Y";
    private static final String FLAG_TRUE_OTHER = "T";
    private static final String FLAG_FALSE = "N";

    /** Specifies how long to wait before trying to look a JNDI data source that previously failed */
    private static final int JNDI_RETRY_TIME = PropertiesManager.getPropertyAsInt("org.jasig.portal.RDBMServices.jndiRetryDelay", 30000); // JNDI retry delay;


    public static IJoinQueryString joinQuery = null;
    public static boolean supportsOuterJoins = false;
    public static boolean supportsTransactions = false;
    public static int RETRY_COUNT = 5;

    protected static boolean supportsPreparedStatements = false;
    protected static final boolean usePreparedStatements = true;

    private static boolean rdbmPropsLoaded = false;
    private static Map namedDbServers =  Collections.synchronizedMap(new HashMap());
    private static Map namedDbServerFailures = Collections.synchronizedMap(new HashMap());
    private static IDatabaseServer jdbcDbServer = null;

    private static final Object syncObject = new Object();
    private static int activeConnections;

    /**
     * Perform one time initialization of the data source
     */
    static {
        loadRDBMServer();

        //Cache lookups to the two JNDI data sources we "know" about
        if (getDatasourceFromJndi) {
            getDatabaseServer(PORTAL_DB);
            getDatabaseServer(PERSON_DB);
        }

        //Legacy support for the public fields
        final IDatabaseServer dbs = getDatabaseServer();
        if(dbs != null) {
            joinQuery = dbs.getJoinQuery();
            supportsOuterJoins = dbs.supportsOuterJoins();
            supportsTransactions = dbs.supportsTransactions();
            supportsPreparedStatements = dbs.supportsPreparedStatements();
        }
        else {
            final RuntimeException re = new IllegalStateException("No default database could be found after static initialization.");
            LOG.fatal("Error initializing RDBMServices", re);
            throw re;
        }
    }

    /**
     * Loads a JDBC data source from rdbm.properties. Attempts to uses
     * a connection pooling data source wrapper for added performance.
     */
    private synchronized static void loadRDBMServer() {
        final String PROP_FILE = "/properties/rdbm.properties";

        if (!rdbmPropsLoaded) {
            final InputStream jdbcPropStream = RDBMServices.class.getResourceAsStream(PROP_FILE);

            try {
                try {
                    final Properties jdbpProperties = new Properties();
                    jdbpProperties.load(jdbcPropStream);

                    final IPooledDataSourceFactory pdsf = PooledDataSourceFactoryFactory.getPooledDataSourceFactory();

                    final String driverClass = jdbpProperties.getProperty("jdbcDriver");
                    final String username = jdbpProperties.getProperty("jdbcUser");
                    final String password = jdbpProperties.getProperty("jdbcPassword");
                    final String url = jdbpProperties.getProperty("jdbcUrl");
                    final boolean usePool = Boolean.valueOf(jdbpProperties.getProperty("jdbcUsePool")).booleanValue();

                    if (usePool) {
                        //Try using a pooled DataSource
                        try {
                            final DataSource ds = pdsf.createPooledDataSource(driverClass, username, password, url);

                            if (LOG.isInfoEnabled())
                                LOG.info("Creating IDatabaseServer instance for pooled JDBC");
                            jdbcDbServer = new DatabaseServerImpl(ds);
                        }
                        catch (Exception e) {
                            LOG.error("Error using pooled JDBC data source.", e);
                        }
                    }

                    if (jdbcDbServer == null) {
                        //Pooled DataSource isn't being used or failed during creation
                        try {
                            final Driver d = (Driver)Class.forName(driverClass).newInstance();
                            final DataSource ds = new GenericDataSource(d, url, username, password);

                            if (LOG.isInfoEnabled())
                                LOG.info("Creating IDatabaseServer instance for JDBC");
                            jdbcDbServer = new DatabaseServerImpl(ds);
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

            if (!getDatasourceFromJndi && jdbcDbServer == null) {
                throw new RuntimeException("No JDBC DataSource or JNDI DataSource avalable.");
            }

            rdbmPropsLoaded = true;
        }
    }

    /**
     * Gets the default {@link IDatabaseServer}. If getDatasourceFromJndi
     * is true {@link #getDatabaseServer(String)} is called with
     * {@link RDBMServices#PORTAL_DB} as the argument. If no server is found
     * the jdbc based server loaded from rdbm.properties is used.
     *
     * @return The default {@link IDatabaseServer}.
     */
    public static IDatabaseServer getDatabaseServer() {
        IDatabaseServer dbs = null;

        if (getDatasourceFromJndi) {
            dbs = getDatabaseServer(PORTAL_DB);
        }

        if (dbs == null) {
            dbs = jdbcDbServer;
        }

        return dbs;
    }

    /**
     * Gets a named {@link IDatabaseServer} from JNDI. Successfull lookups
     * are cached and not done again. Failed lookups are cached for the
     * number of milliseconds specified by {@link #JNDI_RETRY_TIME} to reduce
     * JNDI overhead and log spam.
     *
     * @param name The name of the {@link IDatabaseServer} to get.
     * @return A named {@link IDatabaseServer} or <code>null</code> if one cannot be found.
     */
    public static IDatabaseServer getDatabaseServer(final String name) {
        if (DEFAULT_DATABASE.equals(name)) {
            return getDatabaseServer();
        }

        IDatabaseServer dbs = (IDatabaseServer)namedDbServers.get(name);

        if (dbs == null) {
            final Long failTime = (Long)namedDbServerFailures.get(name);

            if (failTime == null || (failTime.longValue() + JNDI_RETRY_TIME) <= System.currentTimeMillis()) {
                if (failTime != null) {
                    namedDbServerFailures.remove(name);
                }

                try {
                    final Context initCtx = new InitialContext();
                    final Context envCtx = (Context)initCtx.lookup("java:comp/env");
                    final DataSource ds = (DataSource)envCtx.lookup("jdbc/" + name);

                    if (ds != null) {
                        if (LOG.isInfoEnabled())
                            LOG.info("Creating IDatabaseServer instance for " + name);
                        dbs = new DatabaseServerImpl(ds);
                        namedDbServers.put(name, dbs);
                    }
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
        }

        return dbs;
    }

    /**
     * Return the current number of active connections
     * @return int
     */
    public static int getActiveConnectionCount() {
      return activeConnections;
    }

    /**
     * Gets a database connection to the portal database.
     *
     * This implementation will first try to get the connection by looking in the
     * JNDI context for the name defined by the portal property
     * org.jasig.portal.RDBMServices.PortalDatasourceJndiName
     * if the org.jasig.portal.RDBMServices.getDatasourceFromJndi
     * property is enabled.
     *
     * If not enabled, the Connection will be produced by
     * {@link java.sql.Driver#connect(java.lang.String, java.util.Properties)}
     *
     * @return a database Connection object
     * @throws DataAccessException if unable to return a connection
     */
    public static Connection getConnection() {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null) {
          synchronized(syncObject) {
            activeConnections++;
          }
          return dbs.getConnection();
        }
        throw new DataAccessResourceFailureException("RDBMServices fatally misconfigured such that getDatabaseServer() returned null.");
    }


    /**
     * Returns a connection produced by a DataSource found in the
     * JNDI context.  The DataSource should be configured and
     * loaded into JNDI by the J2EE container.
     * @param dbName the database name which will be retrieved from
     *   the JNDI context relative to "jdbc/"
     * @return a database Connection object or <code>null</code> if no Connection
     */
    public static Connection getConnection(final String dbName) {
        final IDatabaseServer dbs = getDatabaseServer(dbName);

        if (dbs != null) {
          synchronized(syncObject) {
            activeConnections++;
          }
          return dbs.getConnection();
        }
        throw new DataAccessResourceFailureException("RDBMServices fatally misconfigured such that getDatabaseServer() returned null.");
    }

    /**
     * Releases database connection
     * @param con a database Connection object
     */
    public static void releaseConnection(final Connection con) {
        try {
          synchronized(syncObject) {
            activeConnections--;
          }

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
     * Commit pending transactions
     * @param connection
     */
    static final public void commit(final Connection connection) throws SQLException {
        try {
            connection.commit();
        }
        catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Error committing Connection: " + connection, e);
        }
    }

    /**
     * Set auto commit state for the connection
     * @param connection
     * @param autocommit
     */
    public static final void setAutoCommit(final Connection connection, boolean autocommit) throws SQLException {
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
     * This implementation calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#getJdbcDriver()} on the returned instance.
     *
     * @see IDatabaseServer#getJdbcDriver()
     * @return the name of the JDBC Driver.
     * @throws DataAccessException if unable to determine name of driver.
     */
    public static String getJdbcDriver () {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null)
            return dbs.getJdbcDriver();

        throw new DataAccessResourceFailureException("RDBMServices " +
                "fatally misconfigured such that getDatabaseServer() returned null.");
    }

    /**
     * Gets the JDBC URL of the default uPortal database connections.
     *
     * This implementation calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#getJdbcUrl()} on the returned instance.
     *
     * @see IDatabaseServer#getJdbcUrl()
     * @throws DataAccessException on internal failure
     * @return the JDBC URL of the default uPortal database connections
     */
    public static String getJdbcUrl () {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null)
            return dbs.getJdbcUrl();

        throw new DataAccessResourceFailureException("RDBMServices " +
            "fatally misconfigured such that getDatabaseServer() returned null.");
    }

    /**
     * Get the username under which we are connecting for the default uPortal
     * database connections.
     *
     * This implementation calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#getJdbcUser()} on the returned instance.
     *
     * @see IDatabaseServer#getJdbcUser()
     * @return the username under which we are connecting for default connections
     * @throws DataAccessException on internal failure
     */
    public static String getJdbcUser () {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null)
            return dbs.getJdbcUser();

        throw new DataAccessResourceFailureException("RDBMServices " +
            "fatally misconfigured such that getDatabaseServer() returned null.");
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
     * Returns a String representing the current time
     * in SQL suitable for use with the default database connections.
     *
     * This implementation calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#sqlTimeStamp()} on the returned instance.
     *
     * @see IDatabaseServer#sqlTimeStamp()
     * @return SQL representing the current time
     */
    public static final String sqlTimeStamp() {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null) {
            return dbs.sqlTimeStamp();
        }
        else {
            return localSqlTimeStamp(System.currentTimeMillis());
        }
    }

    /**
     * Calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#sqlTimeStamp(long)} on the returned instance.
     *
     * @see IDatabaseServer#sqlTimeStamp(long)
     */
    public static final String sqlTimeStamp(final long date) {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null) {
            return dbs.sqlTimeStamp(date);
        }
        else {
            return localSqlTimeStamp(date);
        }
    }

    /**
     * Calls {@link #getDatabaseServer()} then calls
     * {@link IDatabaseServer#sqlTimeStamp(Date)} on the returned instance.
     *
     * @see IDatabaseServer#sqlTimeStamp(Date)
     */
    public static final String sqlTimeStamp(final Date date) {
        final IDatabaseServer dbs = getDatabaseServer();

        if (dbs != null) {
            return dbs.sqlTimeStamp(date);
        }
        else {
            if (date == null)
                return "NULL";
            else
                return localSqlTimeStamp(date.getTime());
        }
    }

    /**
     * Utility method if there is no default {@link IDatabaseServer}
     * instance.
     *
     * @param date The date in milliseconds to convert into a SQL TimeStamp.
     * @return A SQL TimeStamp representing the date.
     */
    private static final String localSqlTimeStamp(final long date) {
        final StringBuffer sqlTS = new StringBuffer();

        sqlTS.append("'");
        sqlTS.append(new Timestamp(date).toString());
        sqlTS.append("'");

        return sqlTS.toString();
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
     * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
     * @version $Revision $
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

    }

    /**
     * Wrapper for/Emulator of PreparedStatement class
     * @deprecated Instead of this class a wrapper around the DataSource, Connection and Prepared statement should be done in {@link DatabaseServerImpl}
     */
    public static final class PreparedStatement {
        private Connection con;
        private String query;
        private String activeQuery;
        private java.sql.PreparedStatement pstmt;
        private Statement stmt;
        private int lastIndex;


        public PreparedStatement(Connection con, String query) throws SQLException {
            this.con = con;
            this.query = query;
            activeQuery = this.query;
            if (supportsPreparedStatements) {
                pstmt = con.prepareStatement(query);
            }
            else {
                stmt = con.createStatement();
            }
        }

        public void clearParameters() throws SQLException {
            if (supportsPreparedStatements) {
                pstmt.clearParameters();
            }
            else {
                lastIndex = 0;
                activeQuery = query;
            }
        }

        public void setDate(int index, java.sql.Date value) throws SQLException {
            if (supportsPreparedStatements) {
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
                    activeQuery = activeQuery.substring(0, pos) + sqlTimeStamp(value)
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public void setInt(int index, int value) throws SQLException {
            if (supportsPreparedStatements) {
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
            if (supportsPreparedStatements) {
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
                if (supportsPreparedStatements) {
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
            if (supportsPreparedStatements) {
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
                    activeQuery = activeQuery.substring(0, pos) + sqlTimeStamp(value)
                            + activeQuery.substring(pos + 1);
                    lastIndex = index;
                }
            }
        }

        public ResultSet executeQuery() throws SQLException {
            if (supportsPreparedStatements) {
                return pstmt.executeQuery();
            }
            else {
                return stmt.executeQuery(activeQuery);
            }
        }

        public int executeUpdate() throws SQLException {
            if (supportsPreparedStatements) {
                return pstmt.executeUpdate();
            }
            else {
                return stmt.executeUpdate(activeQuery);
            }
        }

        public String toString() {
            if (supportsPreparedStatements) {
                return query;
            }
            else {
                return activeQuery;
            }
        }

        public void close() throws SQLException {
            if (supportsPreparedStatements) {
                pstmt.close();
            }
            else {
                stmt.close();
            }
        }
    }

    public static final class JdbcDb extends JoinQueryString {
        public JdbcDb(final String testString) {
          super(testString);
        }
    }

    public static final class OracleDb extends JoinQueryString {
        public OracleDb(final String testString) {
            super(testString);
        }
    }

    public static final class PostgreSQLDb extends JoinQueryString {
        public PostgreSQLDb(final String testString) {
            super(testString);
        }
    }
}
