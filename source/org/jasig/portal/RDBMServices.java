/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jasig.portal.services.LogService;

/**
 * Provides relational database access and helper methods.
 * A static routine determins if the database/driver supports
 * prepared statements and/or outer joins.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @author George Lindholm, george.lindholm@ubc.ca
 * @version $Revision$
 */
public class RDBMServices {
  private static boolean bPropsLoaded = false;
  private static String sJdbcDriver = null;
  private static String sJdbcUrl = null;
  private static String sJdbcUser = null;
  private static final Properties jdbcDriverProps = new Properties();
  private static Driver jdbcDriver;
  public static int RETRY_COUNT = 5;
  private static String prevErrorMsg = "";      // reduce noise in log file

  protected static final boolean getDatasourceFromJndi = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.RDBMServices.getDatasourceFromJndi");
  protected static final boolean usePreparedStatements = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.RDBMServices.usePreparedStatements");
  protected static boolean supportsPreparedStatements = false;
  public static boolean supportsOuterJoins = false;
  public static boolean supportsTransactions = false;
  private static String tsStart = "";
  private static String tsEnd = "";
  private static final JdbcDb jdbcDb= new JdbcDb("{oj UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID} WHERE");
  private static final PostgreSQLDb postgreSQLDb = new PostgreSQLDb("UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID WHERE");
  private static final OracleDb oracleDb = new OracleDb("UP_USER, UP_USER_LAYOUT WHERE UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID(+) AND");
  private static final JoinQueryString[] joinTests = {jdbcDb, postgreSQLDb, oracleDb};
  public static IJoinQueryString joinQuery = null;
  public static final String PORTAL_DB = "PortalDb"; // JNDI name for portal database
  public static final String PERSON_DB = "PersonDb"; // JNDI name for person database
  private static boolean useToDate = false; // Use TO_DATE() function

  static {
    try {
      loadProps();
      if (!bPropsLoaded && !getDatasourceFromJndi)
        {
        System.err.println("Unable to connect to database");
        throw new PortalException("Unable to connect to database");
      }

      /**
       * See what the database allows us to do
       */
      Connection con = getConnection();
      if (con == null) {
        System.err.println("Unable to connect to database");
        throw new SQLException("Unable to connect to database");
      }
      try {
        String sql;

        /**
         * Prepared statements
         */
        if (usePreparedStatements) {
          try {
            sql = "SELECT USER_ID FROM UP_USER WHERE USER_ID=?";
            java.sql.PreparedStatement pstmt = con.prepareStatement(sql);
            try {
              pstmt.clearParameters ();
              int userId = 0;
              pstmt.setInt(1, userId);
              ResultSet rs = pstmt.executeQuery();
              try {
                if (rs.next() && userId == rs.getInt(1)) {
                  supportsPreparedStatements = true;
                }
              } catch (SQLException sqle) {
              } finally {
                rs.close();
              }
            } finally {
              pstmt.close();
            }
          } catch (SQLException sqle) {}
        }

        /**
         * Do we support outer joins?
         */
        try {
          if (con.getMetaData().supportsOuterJoins()) {
            Statement stmt = con.createStatement();
            try {
              for (int i = 0; i < joinTests.length; i++) {
                sql = "SELECT COUNT(UP_USER.USER_ID) FROM " + joinTests[i].getTestJoin() + " UP_USER.USER_ID=0";
                try {
                  ResultSet rs = stmt.executeQuery(sql);
                  try {
                    rs.close();
                  } catch (SQLException sqle) {}
                  joinQuery = joinTests[i];
                  supportsOuterJoins = true;
                  break;
                } catch (SQLException sqle) {}
              }
            } finally {
              stmt.close();
            }
          }
        } catch (SQLException sqle) {
        }

        /**
         * Does the JDBC driver support the '{ts' (TimeStamp) metasyntax
         */
        try {
          sql = "SELECT USER_ID FROM UP_USER WHERE LST_CHAN_UPDT_DT={ts '2001-01-01 00:00:00.0'} AND USER_ID=0";
          Statement stmt = con.createStatement();
          try {
            ResultSet rs = stmt.executeQuery(sql);
            try {
              rs.close();
            } catch (SQLException sqle) {}
            tsStart = "{ts ";
            tsEnd = "}";
          } finally {
            stmt.close();
          }
        } catch (SQLException sqle) {
          // I guess not
          // Try TO_DATE()
          try {
            SimpleDateFormat oracleTime = new SimpleDateFormat(
                "yyyy MM dd HH:mm:ss");
            sql = "SELECT USER_ID FROM UP_USER WHERE LST_CHAN_UPDT_DT = TO_DATE('2001 01 01 00:00', 'YYYY MM DD HH24:MI:SS') AND USER_ID=0";
            Statement stmt = con.createStatement();
            try {
              ResultSet rs = stmt.executeQuery(sql);
              try {
                rs.close();
              } catch (SQLException sqle2) {
              }
            } finally {
              stmt.close();
            }

            useToDate = true;
          } catch (SQLException sqle3) {
          }

        }

        /**
         * Transaction support
         */
        String tranMsg = "";
        DatabaseMetaData md = con.getMetaData();
        if (md.supportsTransactions()) {
          try {
            con.setAutoCommit(false);
            Statement stmt = con.createStatement();
            try {
              sql = "UPDATE UP_USER SET LST_CHAN_UPDT_DT=" + sqlTimeStamp() + " WHERE USER_ID=0";
              stmt.executeUpdate(sql);
              con.rollback();
              supportsTransactions = true;  // Can't trust metadata
            } finally {
              stmt.close();
            }
          } catch (SQLException sqle) {
            tranMsg = " (driver lies)";
          }
        }

        LogService.log(LogService.INFO, md.getDatabaseProductName() +
          "/" + getJdbcDriver() + " (" + md.getDriverVersion() +
          ") database/driver supports:\n     Prepared statements=" + supportsPreparedStatements +
          ", Outer joins=" + supportsOuterJoins + ", Transactions=" + supportsTransactions + tranMsg +
          ", '{ts' metasyntax=" + tsEnd.equals("}") + ", TO_DATE()=" +
          useToDate);
      } finally {
        releaseConnection(con);
      }
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
  }

  /**
   * Loads the JDBC properties from rdbm.properties file.
   */
  protected static void loadProps () throws Exception {
  	InputStream inStream = null;
  try {
      if (!bPropsLoaded) {
        inStream = RDBMServices.class.getResourceAsStream("/properties/rdbm.properties");
        Properties jdbcProps = new Properties();
        jdbcProps.load(inStream);
        sJdbcDriver = jdbcProps.getProperty("jdbcDriver");
        sJdbcUrl = jdbcProps.getProperty("jdbcUrl");
        sJdbcUser = jdbcProps.getProperty("jdbcUser");
        jdbcDriverProps.put("user", sJdbcUser);
        jdbcDriverProps.put("password", jdbcProps.getProperty("jdbcPassword"));
        jdbcDriver = (java.sql.Driver)Class.forName(sJdbcDriver).newInstance();
        bPropsLoaded = true;
        }
      }
      catch (Exception e){
        // let caller handle situation where no proerties file is found.
        // When getting datasource from jndi properties file is optional
        // and would be used as a dallback only
        throw new RuntimeException(e);
        } finally {
        	if(inStream != null)
        		inStream.close();        	
        }
  }

  /**
   * Returns a connection produced by a DataSource found in the
   * JNDI context.  The DataSource should be configured and
   * loaded into JNDI by the J2EE container.
   * @param dbName the database name which will be retrieved from
   *   the JNDI context relative to "jdbc/"
   * @return a database Connection object or <code>null</code> if no Connection
   */
  public static Connection getConnection(String dbName) {
    Connection conn = null;
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context) initCtx.lookup("java:comp/env");
      DataSource ds = (DataSource)envCtx.lookup("jdbc/" + dbName);
      if (ds != null) {
        conn = ds.getConnection();
        // Make sure autocommit is set to true
        if (conn != null && !conn.getAutoCommit()) {
          conn.rollback();
          conn.setAutoCommit(true);
        }
      } else {
        LogService.log(LogService.ERROR, "The database '" + dbName + "' could not be found.");
      }
    } catch (javax.naming.NamingException ne) {
      LogService.log(LogService.ERROR, ne);
    } catch (SQLException sqle) {
      LogService.log(LogService.ERROR, sqle);
    }
    return conn;
  }

  /**
   * Gets a database connection to the portal database.
   * This method will first try
   * to get the connection by looking in the JNDI context if
   * org.jasig.portal.RDBMServices.get_datasource_from_jndi property
   * is enabled.  If not enabled,
   * the Connection will be produced by DriverManager.getConnection().
   * This method should probably be deprecated since obtaining connections
   * from JNDI is the preferred way to do it according to J2EE.
   * @return a database Connection object
   */
  public static Connection getConnection () {
    Connection conn = null;

    // Look in the JNDI context for the DataSource which produces the Connection
    if (getDatasourceFromJndi) {
      if ((conn = getConnection(PORTAL_DB)) != null)
        return conn;
    }

    if (bPropsLoaded)
    for (int i = 0; i < RETRY_COUNT && conn == null; ++i) {
      try {
        conn = jdbcDriver.connect(sJdbcUrl, jdbcDriverProps);
        // Make sure autocommit is set to true
        if (conn != null && !conn.getAutoCommit()) {
          conn.rollback();
          conn.setAutoCommit(true);
        }
        prevErrorMsg = "";
      } catch (SQLException SQLe) {
        String errMsg = SQLe.getMessage();
        if (!errMsg.equals(prevErrorMsg)) {                     // Only need to see one instance of this error
          LogService.log(LogService.WARN, "Driver " + sJdbcDriver + " produced error: " + SQLe.getMessage() + ". Trying to get connection again.");
          LogService.log(LogService.INFO, SQLe);
          prevErrorMsg = errMsg;
        }
      }
    }
    return  conn;
  }

  /**
   * Releases database connection
   * @param con a database Connection object
   */
  public static void releaseConnection (Connection con) {
    try {
      if (con != null)
        con.close();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
  }

  /**
   * Get the JDBC driver
   * @return the JDBC driver
   */
  public static String getJdbcDriver () {
    return  sJdbcDriver;
  }

  /**
   * Get the JDBC connection URL
   * @return the JDBC connection URL
   */
  public static String getJdbcUrl () {
    return  sJdbcUrl;
  }

  /**
   * Get the JDBC user
   * @return the JDBC connection URL
   */
  public static String getJdbcUser () {
    return  sJdbcUser;
  }

  /**
   * Service routines
   */

  /**
   * Commit pending transactions
   * @param connection
   */
  static final public void commit (Connection connection) throws SQLException {
    if (supportsTransactions) {
      connection.commit();
    }
  }

  /**
   * Set auto commit state for the connection
   * @param connection
   * @param autocommit
   */
  public static final void setAutoCommit (Connection connection, boolean autocommit) throws SQLException {
    if (supportsTransactions) {
      connection.setAutoCommit(autocommit);
    }
  }

  /**
   * rollback unwanted changes to the database
   * @param connection
   */
  public static final void rollback (Connection connection) throws SQLException {
    if (supportsTransactions) {
        connection.rollback();
    } else {
      LogService.log(LogService.SEVERE, "RDBMUserLayout::rollback() called, but JDBC/DB does not support transactions. User data most likely corrupted");
      throw new SQLException("Unable to rollback user data");
    }
  }

  /**
   * Return DB format of a boolean. "Y" for true and "N" for false.
   * @param flag true or false
   * @return either "Y" or "N"
   */
  public static final String dbFlag(boolean flag) {
    return (flag ? "Y" : "N");
  }

  /**
   * Return boolean value of DB flag, "Y" or "N".
   * @param flag either "Y" or "N"
   * @return boolean true or false
   */
  public static final boolean dbFlag(String flag) {
    return (flag != null && (flag.equalsIgnoreCase("Y") || flag.equalsIgnoreCase("T")) ? true : false);
  }

  /**
   * SQL format of current time
   * @return SQL TimeStamp
   */
  public static final String sqlTimeStamp() {
    return sqlTimeStamp(System.currentTimeMillis());
  }

  /**
   * SQL format a long timestamp
   * @param date
   * @return SQL TimeStamp
   */
  public static final String sqlTimeStamp(long date) {
    if (useToDate) {
      SimpleDateFormat toDateTime = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

      return "TO_DATE('" + toDateTime.format(new Date(date)) +
        "', 'YYYY MM DD HH24:MI:SS')";
    } else {
      return tsStart + "'" + new java.sql.Timestamp(date).toString() + "'" +
        tsEnd;
    }
  }

  /**
   * SQL format a Date
   * @param date
   * @return SQL TimeStamp or "NULL" if date is null
   */
  public static final String sqlTimeStamp(Date date) {
    if (date == null)
      return "NULL";
    else
      return sqlTimeStamp(date.getTime());
  }

  /**
   * Make a string SQL safe
   * @param sql
   * @return SQL safe string
   */
  public static final String sqlEscape (String sql) {
    if (sql == null) {
      return  "";
    }
    else {
      int primePos = sql.indexOf("'");
      if (primePos == -1) {
        return  sql;
      }
      else {
        StringBuffer sb = new StringBuffer(sql.length() + 4);
        int startPos = 0;
        do {
          sb.append(sql.substring(startPos, primePos + 1));
          sb.append("'");
          startPos = primePos + 1;
          primePos = sql.indexOf("'", startPos);
        } while (primePos != -1);
        sb.append(sql.substring(startPos));
        return  sb.toString();
      }
    }
  }

  /**
   * Wrapper for/Emulator of PreparedStatement class
   */
  public final static class PreparedStatement {
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
      if (RDBMServices.supportsPreparedStatements) {
        pstmt = con.prepareStatement(query);
      } else {
        stmt = con.createStatement();
      }
    }

    public void clearParameters() throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.clearParameters();
      } else {
        lastIndex = 0;
        activeQuery = query;
      }
    }

    public void setDate(int index, java.sql.Date value) throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.setDate(index, value);
      } else {
        if (index != lastIndex+1) {
          throw new SQLException("Out of order index");
        } else {
          int pos = activeQuery.indexOf("?");
          if (pos == -1) {
            throw new SQLException("Missing '?'");
          }
          activeQuery = activeQuery.substring(0, pos) + sqlTimeStamp(value) + activeQuery.substring(pos+1);
          lastIndex = index;
        }
      }
    }

    public void setInt(int index, int value) throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.setInt(index, value);
      } else {
        if (index != lastIndex+1) {
          throw new SQLException("Out of order index");
        } else {
          int pos = activeQuery.indexOf("?");
          if (pos == -1) {
            throw new SQLException("Missing '?'");
          }
          activeQuery = activeQuery.substring(0, pos) + value + activeQuery.substring(pos+1);
          lastIndex = index;
        }
      }
    }

    public void setNull(int index, int sqlType) throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.setNull(index, sqlType);
      } else {
        if (index != lastIndex+1) {
          throw new SQLException("Out of order index");
        } else {
          int pos = activeQuery.indexOf("?");
          if (pos == -1) {
            throw new SQLException("Missing '?'");
          }
          activeQuery = activeQuery.substring(0, pos) + "NULL" + activeQuery.substring(pos+1);
          lastIndex = index;
        }
      }
    }

    public void setString(int index, String value) throws SQLException {
      if (value == null || value.length() == 0 ) {
        setNull(index, java.sql.Types.VARCHAR);
      } else {
        if (RDBMServices.supportsPreparedStatements) {
            pstmt.setString(index, value);
        } else {
          if (index != lastIndex+1) {
            throw new SQLException("Out of order index");
          } else {
            int pos = activeQuery.indexOf("?");
            if (pos == -1) {
              throw new SQLException("Missing '?'");
            }
            activeQuery = activeQuery.substring(0, pos) + "'" + sqlEscape(value) + "'" + activeQuery.substring(pos+1);
            lastIndex = index;
          }
        }
      }
    }

    public void setTimestamp(int index, java.sql.Timestamp value) throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.setTimestamp(index, value);
      } else {
        if (index != lastIndex+1) {
          throw new SQLException("Out of order index");
        } else {
          int pos = activeQuery.indexOf("?");
          if (pos == -1) {
            throw new SQLException("Missing '?'");
          }
          activeQuery = activeQuery.substring(0, pos) + sqlTimeStamp(value) + activeQuery.substring(pos+1);
          lastIndex = index;
        }
      }
    }

    public ResultSet executeQuery() throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        return pstmt.executeQuery();
      } else {
        return stmt.executeQuery(activeQuery);
      }
    }

    public int executeUpdate() throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        return pstmt.executeUpdate();
      } else {
        return stmt.executeUpdate(activeQuery);
      }
    }

    public String toString() {
      if (RDBMServices.supportsPreparedStatements) {
        return query;
      } else {
        return activeQuery;
      }
    }

    public void close() throws SQLException {
      if (RDBMServices.supportsPreparedStatements) {
        pstmt.close();
      } else {
        stmt.close();
      }
    }
  }

  public interface IJoinQueryString {
    public String getQuery(String key) throws SQLException;
    public void addQuery(String key, String value) throws SQLException;
  }

  public abstract static class JoinQueryString implements IJoinQueryString {
    private HashMap queryStrings = new HashMap();
    private String testJoin;

    protected void setTestJoin(String query) {
      testJoin = query;
    }

    protected String getTestJoin() {return testJoin;}

    public String getQuery(String key) throws SQLException {
      String query = (String) queryStrings.get(key);
      if (query == null) {
        throw new SQLException("Missing query");
      }
      return query;
    }

    public void addQuery(String key, String value) throws SQLException {
       queryStrings.put(key, value);
    }

  }

  public static final class JdbcDb extends JoinQueryString {
    public JdbcDb(String testString) {
      setTestJoin(testString);
    }
  }

  public static final class OracleDb extends JoinQueryString {
    public OracleDb(String testString) {
      setTestJoin(testString);
    }
  }

  public static final class PostgreSQLDb extends JoinQueryString {
    public PostgreSQLDb(String testString) {
      setTestJoin(testString);
    }
  }

}

