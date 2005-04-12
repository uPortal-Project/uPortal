/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.springframework.dao.DataAccessResourceFailureException;


/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class DatabaseMetaDataImpl implements IDatabaseMetadata {
    public static final class PostgreSQLDb extends JoinQueryString {
        public PostgreSQLDb(final String testString) {
            super(testString);
        }
    }

    public static final class OracleDb extends JoinQueryString {
        public OracleDb(final String testString) {
            super(testString);
        }
    }

    public static final class JdbcDb extends JoinQueryString {
        public JdbcDb(final String testString) {
          super(testString);
        }
    }

    private static final Log LOG = LogFactory.getLog(DatabaseMetaDataImpl.class);
    
    /** Define the oracle TO_DATE format */
    private static final SimpleDateFormat TO_DATE_FORMAT = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
    
    //Define the different join queries we know about with the
    //appropriately typed JoinQueryString implementation. 
    private static final JoinQueryString jdbcDb = new DatabaseMetaDataImpl.JdbcDb("{oj UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID} WHERE");
    private static final JoinQueryString postgreSQLDb = new DatabaseMetaDataImpl.PostgreSQLDb("UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID WHERE");
    private static final JoinQueryString oracleDb = new DatabaseMetaDataImpl.OracleDb("UP_USER, UP_USER_LAYOUT WHERE UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID(+) AND");
    
    /** Array of join tests to perform. */
    private static final JoinQueryString[] joinTests = {jdbcDb, postgreSQLDb, oracleDb};
    
    /** The {@link DataSource} that represents the server */
    final private DataSource dataSource;
    
    /** The {@link IJoinQueryString} to use for performing outer joins */
    private IJoinQueryString joinTest = null;
    
    //Database meta information
    private boolean useTSWrapper = false;
    private boolean useToDate = false;
    private boolean supportsTransactions = false;
    private boolean supportsPreparedStatements = false;
    private String transactionTestMsg = "";
    private String databaseProductName = null;
    private String databaseProductVersion = null;
    private String driverName = null;
    private String driverVersion = null;
    private String userName = null;
    private String dbUrl = null;
    
    
    /**
     * Creates a new {@link DatabaseMetaDataImpl} with the specified
     * {@link DataSource}.
     * 
     * @param ds The {@link DataSource} to use as the base for this server interface.
     */
    public DatabaseMetaDataImpl(final DataSource ds) {
        if (ds == null)
            throw new IllegalArgumentException("DataSource cannot be null");
        
        this.dataSource = ds;
        
        this.runDatabaseTests();
        if (LOG.isInfoEnabled())
            LOG.info(this.toString());
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#releaseConnection(java.sql.Connection)
     */
    public void releaseConnection(final Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        }
        catch (Exception e) {
            LOG.warn("An error occured while closing a connection.", e);
        }
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#getJoinQuery()
     */
    public final IJoinQueryString getJoinQuery() {
        return this.joinTest;
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#supportsOuterJoins()
     */
    public final boolean supportsOuterJoins() {
        return (this.joinTest != null);
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#supportsTransactions()
     */
    public final boolean supportsTransactions() {
        return this.supportsTransactions;
    }
    
    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#supportsPreparedStatements()
     */
    public final boolean supportsPreparedStatements() {
        return this.supportsPreparedStatements;
    }    

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#sqlTimeStamp()
     */
    public String sqlTimeStamp() {
        return this.sqlTimeStamp(System.currentTimeMillis());
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#sqlTimeStamp(long)
     */
    public String sqlTimeStamp(final long date) {
        final StringBuffer sqlTS = new StringBuffer();
        
        if (useToDate) {
            sqlTS.append("TO_DATE('");
            sqlTS.append(TO_DATE_FORMAT.format(new Date(date)));
            sqlTS.append("', 'YYYY MM DD HH24:MI:SS')");
        }
        else if (useTSWrapper) {
            sqlTS.append("{ts '");
            sqlTS.append(new Timestamp(date).toString());
            sqlTS.append("'}");
        }
        else {
            sqlTS.append("'");
            sqlTS.append(new Timestamp(date).toString());
            sqlTS.append("'");
        }
        
        return sqlTS.toString();
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseServer#sqlTimeStamp(java.util.Date)
     */
    public String sqlTimeStamp(final Date date) {
        if (date == null)
            return "NULL";
        else
            return this.sqlTimeStamp(date.getTime());
    }
    
    
    public String toString() {
        final StringBuffer dbInfo = new StringBuffer();
        
        dbInfo.append(this.databaseProductName);
        dbInfo.append(" (");
        dbInfo.append(this.databaseProductVersion);
        dbInfo.append(") / ");
        dbInfo.append(this.driverName);
        dbInfo.append(" (");
        dbInfo.append(this.driverVersion);
        dbInfo.append(") database/driver");
        dbInfo.append("\n");
        dbInfo.append("    Connected To: ");
        dbInfo.append(this.dbUrl);
        dbInfo.append("\n");
        dbInfo.append("    Supports:");        
        dbInfo.append("\n");
        dbInfo.append("        Prepared Statements:  ");
        dbInfo.append(this.supportsPreparedStatements());        
        dbInfo.append("\n");
        dbInfo.append("        Outer Joins:          ");
        dbInfo.append(this.supportsOuterJoins());
        dbInfo.append("\n");
        dbInfo.append("        Transactions:         ");
        dbInfo.append(this.supportsTransactions());
        dbInfo.append(this.transactionTestMsg);
        dbInfo.append("\n");
        dbInfo.append("        {ts metasyntax:       ");
        dbInfo.append(this.useTSWrapper);
        dbInfo.append("\n");
        dbInfo.append("        TO_DATE():            ");
        dbInfo.append(this.useToDate);              
        
        return dbInfo.toString();
    }



    
    /**
     * Run a set of tests on the database to provide better meta data.
     */
    private void runDatabaseTests() {
        final Connection conn;
        try {
            conn = this.dataSource.getConnection();
       //The order of these tests is IMPORTANT, each may depend on the
        //results of the previous tests.
        this.getMetaData(conn);
        this.testPreparedStatements(conn);
        this.testOuterJoins(conn);
        this.testTimeStamp(conn);
        this.testTransactions(conn);
        
        this.releaseConnection(conn);
        } catch (SQLException e) {
            LOG.error("Error during database initialization. "+e);
        }
     }
    
    /**
     * Gets meta data about the connection.
     * 
     * @param conn The connection to use.
     */
    private void getMetaData(final Connection conn) {
        try {
            final DatabaseMetaData dmd = conn.getMetaData();
            
            this.databaseProductName = dmd.getDatabaseProductName();
            this.databaseProductVersion = dmd.getDatabaseProductVersion();
            this.driverName = dmd.getDriverName();
            this.driverVersion = dmd.getDriverVersion();
            this.userName = dmd.getUserName();
            this.dbUrl = dmd.getURL();
        }
        catch (SQLException sqle) {
            LOG.error("Error getting database meta data.", sqle);
        }
    }
    
    /**
     * Tests the database for prepared statement support.
     * 
     * @param conn The connection to use. 
     */
    private void testPreparedStatements(final Connection conn) {
        try {
            final String pStmtTestQuery =
                "SELECT USER_ID " +
                "FROM UP_USER " +
                "WHERE USER_ID=?";
            
            final PreparedStatement pStmt = conn.prepareStatement(pStmtTestQuery);
            
            try {
                pStmt.clearParameters();
                final int userId = 0;
                pStmt.setInt(1, userId); //Set USER_ID=0
                final ResultSet rs = pStmt.executeQuery();
                
                try {
                    if (rs.next() && userId == rs.getInt(1)) {
                        this.supportsPreparedStatements = true;
                    }
                }
                finally {
                    RDBMServices.closeResultSet(rs);
                }
            }
            finally {
                RDBMServices.closeStatement(pStmt);
            }
        }
        catch (SQLException sqle) {
            LOG.error("PreparedStatements are not supported!", sqle);
        }
    }
    
    /**
     * Test the database to see if it really supports outer joins.
     * @param conn The connection to use.
     */
    private void testOuterJoins(final Connection conn) {
        try {
            if (conn.getMetaData().supportsOuterJoins()) {
                final Statement joinTestStmt = conn.createStatement();
                
                try {
                    for (int index = 0; index < joinTests.length; index++) {
                        final String joinTestQuery =
                            "SELECT COUNT(UP_USER.USER_ID) " +
                            "FROM " + joinTests[index].getTestJoin() + " UP_USER.USER_ID=0";
                        
                        try {
                            final ResultSet rs = joinTestStmt.executeQuery(joinTestQuery);
                            
                            RDBMServices.closeResultSet(rs);
                            
                            this.joinTest = joinTests[index];
                            if (LOG.isInfoEnabled())
                                LOG.info("Using join test: " + 
                                        this.joinTest.getClass().getName());
                            break;
                        }
                        catch (SQLException sqle) {
                            if (LOG.isInfoEnabled())
                                LOG.info("Join test failed: " + joinTests[index].getClass().getName() + 
                                        " with sql error: '" + sqle.getLocalizedMessage() + "' on statement: '" +
                                        joinTestQuery + "'");
                        }
                    }
                }
                finally {
                    RDBMServices.closeStatement(joinTestStmt);
                }
            }
        }
        catch (SQLException sqle) {
            LOG.warn("Error running join tests.", sqle);
        }
    }
    
    /**
     * Test the database to find the supported timestamp format
     * @param conn The connection to use.
     */
    private void testTimeStamp(final Connection conn) {
        try {
            //Try using {ts }
            final String timeStampTestQuery = 
                "SELECT USER_ID " +
                "FROM UP_USER " +
                "WHERE LST_CHAN_UPDT_DT={ts '2001-01-01 00:00:00.0'} AND USER_ID = 0";
            
            final PreparedStatement timeStampTestPStmt = conn.prepareStatement(timeStampTestQuery);
            
            try {
                final ResultSet rs = timeStampTestPStmt.executeQuery();
                
                RDBMServices.closeResultSet(rs);
                
                this.useTSWrapper = true;
            }
            finally {
                RDBMServices.closeStatement(timeStampTestPStmt);
            }
        }
        catch (SQLException sqle1) {
            LOG.info("Error running {ts } test.", sqle1);
            
            //Try using TO_DATE()
            try {
                final String toDateTestQuery = 
                    "SELECT USER_ID " +
                    "FROM UP_USER " +
                    "WHERE LST_CHAN_UPDT_DT=TO_DATE('2001 01 01 00:00', 'YYYY MM DD HH24:MI:SS') AND USER_ID=0";
                
                final PreparedStatement toDateTestPStmt = conn.prepareStatement(toDateTestQuery);
                
                try {
                    final ResultSet rs = toDateTestPStmt.executeQuery();
                    
                    RDBMServices.closeResultSet(rs);
                    
                    this.useToDate = true;
                }
                finally {
                    RDBMServices.closeStatement(toDateTestPStmt);
                }
            }
            catch (SQLException sqle2) {
                LOG.info("Error running TO_DATE() test.", sqle2);
            }
        }
    }
    
    /**
     * Test the database to see if it really supports transactions
     * @param conn The connection to use.
     */
    private void testTransactions(final Connection conn) {
        try {
            if (conn.getMetaData().supportsTransactions()) {
                conn.setAutoCommit(false); //Not using RDBMServices here, we want to see the exception if it happens
                
                final Statement transTestStmt = conn.createStatement();
                    
                try {
                    final String transTestUpdate = 
                        "UPDATE UP_USER " +
                        "SET LST_CHAN_UPDT_DT=" + this.sqlTimeStamp() + " " +
                        "WHERE USER_ID=0";
                    
                    transTestStmt.executeUpdate(transTestUpdate);
                    conn.rollback();
                    this.supportsTransactions = true;
                }
                finally {
                    RDBMServices.closeStatement(transTestStmt);
                }                
            }
        }
        catch (SQLException sqle) {
            LOG.warn("Error running transaction test (Transactions are not supported).", sqle);
            this.transactionTestMsg = " (driver lies)";
        }
    }
}
