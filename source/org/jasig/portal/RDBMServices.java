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

import org.jasig.portal.services.LogService;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Driver;

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

  static {
    try {
      loadProps();

      /**
       * See what the database allows us to do
       */
      Connection con = getConnection();
      if (con == null) {
        System.err.println("Unable to connect to database");
        throw new SQLException("Unable to connect to database ");
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

        LogService.instance().log(LogService.INFO, md.getDatabaseProductName() +
          "/" + getJdbcDriver() + " (" + md.getDriverVersion() +
          ") database/driver supports:\n     Prepared statements=" + supportsPreparedStatements +
          ", Outer joins=" + supportsOuterJoins + ", Transactions=" + supportsTransactions + tranMsg +
          ", '{ts' metasyntax=" + tsEnd.equals("}"));
      } finally {
        releaseConnection(con);
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   * Loads the JDBC properties from rdbm.properties file.
   */
  protected static void loadProps () throws Exception {
      if (!bPropsLoaded) {
        InputStream inStream = RDBMServices.class.getResourceAsStream("/properties/rdbm.properties");
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

  /**
   * Gets a database connection
   * @return a database Connection object
   */
  public static Connection getConnection () {
    Connection conn = null;
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
          LogService.instance().log(LogService.WARN, "Driver " + sJdbcDriver + " produced error: " + SQLe.getMessage() + ". Trying to get connection again.");
          LogService.instance().log(LogService.INFO, SQLe);
          prevErrorMsg = errMsg;
        }
      }
    }
    return  conn;
  }

  /**
   * Releases database connection
   * @param a database Connection object
   */
  public static void releaseConnection (Connection con) {
    try {
      if (con != null)
        con.close();
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
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
      LogService.instance().log(LogService.SEVERE, "RDBMUserLayout::rollback() called, but JDBC/DB does not support transactions. User data most likely corrupted");
      throw new SQLException("Unable to rollback user data");
    }
  }

  /**
   * return DB format of a boolean
   * @param boolean
   * @result String
   */
  public static final String dbFlag(boolean flag) {
    return (flag ? "Y" : "N");
  }

  /**
   * return boolean value of DB flag
   * @param String
   * @result boolean
   */
  public static final boolean dbFlag(String flag) {
    return (flag != null && (flag.equalsIgnoreCase("Y") || flag.equalsIgnoreCase("T")) ? true : false);
  }

  /**
   * SQL format of current time
   * @result SQL TimeStamp
   */
  public static final String sqlTimeStamp() {
    return sqlTimeStamp(System.currentTimeMillis());
  }

  /**
   * SQL format a long timestamp
   * @param date
   * @result SQL TimeStamp
   */
  public static final String sqlTimeStamp(long date) {
    return tsStart + "'" + new java.sql.Timestamp(date).toString() + "'" + tsEnd;
  }

  /**
   * SQL format a Date
   * @param date
   * @result SQL TimeStamp
   */
  public static final String sqlTimeStamp(Date date) {
    return sqlTimeStamp(date.getTime());
  }

  /**
   * Make a string SQL safe
   * @param string
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
      if (queryStrings.containsKey(key)) {
        throw new SQLException("Trying to add duplicate query");
      }
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

