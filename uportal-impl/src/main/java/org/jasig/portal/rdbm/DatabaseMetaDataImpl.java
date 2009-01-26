/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$ $Date$
 */
public class DatabaseMetaDataImpl implements IDatabaseMetadata, InitializingBean {
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
    private static final JoinQueryString[] joinTests = {oracleDb, postgreSQLDb, jdbcDb};

    /** The {@link DataSource} that represents the server */
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    /** The {@link IJoinQueryString} to use for performing outer joins */
    private IJoinQueryString joinTest = null;
    private boolean dbmdSupportsOuterJoins = false;

    //Database meta information
    private boolean portalTablesExist = false;
    private boolean useTSWrapper = false;
    private boolean useToDate = false;
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
    public DatabaseMetaDataImpl(final DataSource ds, final PlatformTransactionManager transactionManager) {
        if (ds == null)
            throw new IllegalArgumentException("DataSource cannot be null");

        this.dataSource = ds;
        this.transactionManager = transactionManager;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
    	this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    	this.transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    	this.transactionTemplate.setReadOnly(true);
    	this.transactionTemplate.afterPropertiesSet();    	
    	
    	this.runDatabaseTests();
        if (LOG.isInfoEnabled())
            LOG.info(this.toString());            
    }

    private void releaseConnection(final Connection conn) {
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
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#getJoinQuery()
     */
    public final IJoinQueryString getJoinQuery() {
        return this.joinTest;
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#supportsOuterJoins()
     */
    public final boolean supportsOuterJoins() {
        return (this.joinTest != null);
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#supportsTransactions()
     */
    public final boolean supportsTransactions() {
    	//We never run on DBs that don't support transactions any more
        return true;
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#supportsPreparedStatements()
     */
    public final boolean supportsPreparedStatements() {
        //We never run on DBs that don't support prepared statements any more
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#getJdbcDriver()
     */
    public String getJdbcDriver() {
        return this.driverName;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#getJdbcUrl()
     */
    public String getJdbcUrl() {
        return this.dbUrl;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#getJdbcUser()
     */
    public String getJdbcUser() {
        return this.userName;
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#sqlTimeStamp()
     */
    public String sqlTimeStamp() {
        return this.sqlTimeStamp(System.currentTimeMillis());
    }

    /**
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#sqlTimeStamp(long)
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
     * @see org.jasig.portal.rdbm.IDatabaseMetadata#sqlTimeStamp(java.util.Date)
     */
    public String sqlTimeStamp(final Date date) {
        if (date == null) {
            return "NULL";
        }
         
        return this.sqlTimeStamp(date.getTime());
    }


    @Override
    public String toString() {
        final StringBuilder dbInfo = new StringBuilder();

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
        
        if (this.portalTablesExist) {
            dbInfo.append("    Supports:");
            dbInfo.append("\n");
            dbInfo.append("        Outer Joins:          ");
            dbInfo.append(this.supportsOuterJoins());
            dbInfo.append("\n");
            dbInfo.append("        {ts metasyntax:       ");
            dbInfo.append(this.useTSWrapper);
            dbInfo.append("\n");
            dbInfo.append("        TO_DATE():            ");
            dbInfo.append(this.useToDate);
        }
        else {
            dbInfo.append("    WARNING: uPortal tables do no exist, not all meta-data tests were executed.");
        }

        return dbInfo.toString();
    }




    /**
     * Run a set of tests on the database to provide better meta data.
     */
    private void runDatabaseTests() {
    	Connection conn = null;
    	try {
    		conn = this.dataSource.getConnection();
    		//The order of these tests is IMPORTANT, each may depend on the
    		//results of the previous tests.
    		this.getMetaData(conn);
    		final SimpleJdbcTemplate jdbcTemplate = new SimpleJdbcTemplate(this.dataSource);
    		
    		this.testDatabaseInitialized(jdbcTemplate);
    		if (this.portalTablesExist) {
        		this.testOuterJoins(jdbcTemplate);
        		this.testTimeStamp(jdbcTemplate);
    		}

    	} catch (SQLException e) {
    		LOG.error("Error during database initialization. ", e);
    		/*
    		 * We must throw a RuntimeException here to avoid starting the portal
    		 * with incorrect assumptions about what the database supports.
    		 */
    		throw new DataAccessResourceFailureException("Error during database initialization. ",e);
    	}finally{
    		this.releaseConnection(conn);
    	}
    }

    /**
     * Gets meta data about the connection.
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
            this.dbmdSupportsOuterJoins = dmd.supportsOuterJoins();
        }
        catch (SQLException sqle) {
            LOG.error("Error getting database meta data.", sqle);
        }
    }

    /**
     * Tests if the uPortal tables exist that are needed for this test. 
     */
    private void testDatabaseInitialized(final SimpleJdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.queryForInt("SELECT COUNT(USER_ID) FROM UP_USER");
            this.portalTablesExist = true;
        }
        catch (BadSqlGrammarException bsge) {
            LOG.warn("The uPortal database is not initialized, the database tests will not be performed.");
        }
    }

    /**
     * Test the database to see if it really supports outer joins.
     * @param conn The connection to use.
     */
    private void testOuterJoins(final SimpleJdbcTemplate jdbcTemplate) {
        if (this.dbmdSupportsOuterJoins) {
            for (final JoinQueryString joinQueryString : joinTests) {
                final String joinTestQuery =
                    "SELECT COUNT(UP_USER.USER_ID) " +
                    "FROM " + joinQueryString.getTestJoin() + " UP_USER.USER_ID=0";
                
                try {
                	transactionTemplate
							.execute(new TransactionCallbackWithoutResult() {
								public void doInTransactionWithoutResult(
										TransactionStatus status) {
									jdbcTemplate.getJdbcOperations().execute(
											joinTestQuery);
								}
							});
                    
                    this.joinTest = joinQueryString;
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Using join test: " + this.joinTest.getClass().getName());
                    }
                    break;
                }
                catch (DataAccessException e) {
                    final String logMessage = "Join test failed: " +joinQueryString.getClass().getName() + " on statement: '" + joinTestQuery + "':";
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(logMessage, e);
                    }
                    else {
                        LOG.info(logMessage + "\n" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Test the database to find the supported timestamp format
     */
    private void testTimeStamp(final SimpleJdbcTemplate jdbcTemplate) {
        try {
            //Try using {ts }
            final String timeStampTestQuery =
                "SELECT USER_ID " +
                "FROM UP_USER " +
                "WHERE LST_CHAN_UPDT_DT={ts '2001-01-01 00:00:00.0'} AND USER_ID = 0";

            jdbcTemplate.queryForList(timeStampTestQuery);
            this.useTSWrapper = true;
        }
        catch (DataAccessException dae1) {
            final String logMessage1 = "Error running {ts } test.";
            if (LOG.isDebugEnabled()) {
                LOG.debug(logMessage1, dae1);
            }
            else {
                LOG.info(logMessage1 + "\n" + dae1.getMessage());
            }

            //Try using TO_DATE()
            try {
                final String toDateTestQuery =
                    "SELECT USER_ID " +
                    "FROM UP_USER " +
                    "WHERE LST_CHAN_UPDT_DT>TO_DATE('2001 01 01 00:00', 'YYYY MM DD HH24:MI:SS') AND USER_ID=0";

                jdbcTemplate.queryForList(toDateTestQuery);
                this.useToDate = true;
            }
            catch (DataAccessException dae2) {
                final String logMessage2 = "Error running TO_DATE() test.";
                if (LOG.isDebugEnabled()) {
                    LOG.debug(logMessage2, dae2);
                }
                else {
                    LOG.info(logMessage2 + "\n" + dae2.getMessage());
                }
            }
        }
    }

}
