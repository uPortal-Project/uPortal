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

package  org.jasig.portal;

import  org.jasig.portal.services.LogService;
import  java.io.InputStream;
import  java.util.Date;
import  java.util.Properties;
import  java.sql.Connection;
import  java.sql.ResultSet;
import  java.sql.Statement;
import  java.sql.SQLException;
import  java.sql.DriverManager;

/**
 * Provides database access
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class RdbmServices {
  private static boolean bPropsLoaded = false;
  private static String sJdbcDriver = null;
  private static String sJdbcUrl = null;
  private static String sJdbcUser = null;
  private static String sJdbcPassword = null;
  public static int RETRY_COUNT = 5;
  private static String prevErrorMsg = "";      // reduce noise in log file

  protected static boolean supportsPreparedStatements = false;
  public static boolean supportsOuterJoins = false;
  public static boolean supportsTransactions = false;
  private static String tsStart = "";
  private static String tsEnd = "";

  static {
    try {
      loadProps();

      /** See what the database allows us to do
       *
       */
      Connection con = getConnection();
      try {
        String sql;

        /**
         * Transaction support
         */
        try {
          supportsTransactions = con.getMetaData().supportsTransactions();
        } catch (SQLException sqle) {}

        /**
         * Prepared statements
         */
        try {
          sql = "SELECT USER_ID FROM UP_USER WHERE USER_ID=?";
          java.sql.PreparedStatement pstmt = con.prepareStatement(sql);
          try {
            pstmt.clearParameters ();
            pstmt.setInt(1, 0);
            pstmt.executeQuery();
            supportsPreparedStatements = true;
          } finally {
            pstmt.close();
          }
        } catch (SQLException sqle) {
        }

        /**
         * Does the JDBC driver support the '{ts' (TimeStamp) metasyntax
         */
        try {
          sql = "UPDATE UP_USER SET LST_CHAN_UPDT_DT={ts '2001-01-01 00:00:00.0'} WHERE USER_ID=0";
          Statement stmt = con.createStatement();
          try {
            stmt.executeUpdate(sql);
            tsStart = "{ts ";
            tsEnd = "}";
          } finally {
            stmt.close();
          }
        } catch (SQLException sqle) {
        }

        LogService.instance().log(LogService.INFO, "Database supports: Prepared statements=" +
          supportsPreparedStatements + ", Transactions=" + supportsTransactions + ", {ts ...} syntax=" + tsEnd.equals("}"));
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
        InputStream inStream = RdbmServices.class.getResourceAsStream("/properties/rdbm.properties");
        Properties jdbcProps = new Properties();
        jdbcProps.load(inStream);
        sJdbcDriver = jdbcProps.getProperty("jdbcDriver");
        sJdbcUrl = jdbcProps.getProperty("jdbcUrl");
        sJdbcUser = jdbcProps.getProperty("jdbcUser");
        sJdbcPassword = jdbcProps.getProperty("jdbcPassword");
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
        Class.forName(sJdbcDriver).newInstance();
        conn = DriverManager.getConnection(sJdbcUrl, sJdbcUser, sJdbcPassword);
        prevErrorMsg = "";
      } catch (ClassNotFoundException cnfe) {
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " was not found, please check the rdbm.properties file and your classpath.");
        return null;
      } catch (InstantiationException ie) {
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " could not be instantiated, please check the rdbm.properties file.");
        return null;
      } catch (IllegalAccessException iae) {
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " could not be instantiated, please check the rdbm.properties file.");
        return null;
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
  static final protected void commit (Connection connection) throws SQLException {
    if (supportsTransactions) {
      connection.commit();
    }
  }

  /**
   * Set auto commit state for the connection
   * @param connection
   * @param autocommit
   */
  static final protected void setAutoCommit (Connection connection, boolean autocommit) throws SQLException {
    if (supportsTransactions) {
      connection.setAutoCommit(autocommit);
    }
  }

  /**
   * rollback unwanted changes to the database
   * @param connection
   */
  static final protected void rollback (Connection connection) throws SQLException {
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
  public static String dbFlag(boolean flag) {
    return (flag ? "Y" : "N");
  }

  /**
   * return boolean value of DB flag
   * @param String
   * @result boolean
   */
  public static boolean dbFlag(String flag) {
    return (flag != null && (flag.equalsIgnoreCase("Y") || flag.equalsIgnoreCase("T")) ? true : false);
  }

  /**
   * SQL format of current time
   * @result SQL TimeStamp
   */
  public static String sqlTimeStamp() {
    return sqlTimeStamp(System.currentTimeMillis());
  }
  /**
   * SQL format a long timestamp
   * @param date
   * @result SQL TimeStamp
   */
  public static String sqlTimeStamp(long date) {
    return tsStart + "'" + new java.sql.Timestamp(date).toString() + "'" + tsEnd;
  }
  /**
   * SQL format a Date
   * @param date
   * @result SQL TimeStamp
   */
  public static String sqlTimeStamp(Date date) {
    return sqlTimeStamp(date.getTime());
  }

  /**
   * Wrapper for/Emulator of PreparedStatement class
   */
  public final static class PreparedStatement {
    Connection con;
    String query;
    String activeQuery;
    java.sql.PreparedStatement pstmt;
    Statement stmt;
    int lastIndex;

    public PreparedStatement(Connection con, String query) throws SQLException {
      this.con = con;
      this.query = query;
      activeQuery = this.query;
      if (RdbmServices.supportsPreparedStatements) {
        pstmt = con.prepareStatement(query);
      } else {
        stmt = con.createStatement();
      }
    }

    public void clearParameters() throws SQLException {
      if (RdbmServices.supportsPreparedStatements) {
        pstmt.clearParameters();
      } else {
        lastIndex = 0;
        activeQuery = query;
      }
    }
    public void setInt(int index, int value) throws SQLException {
      if (RdbmServices.supportsPreparedStatements) {
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
      if (RdbmServices.supportsPreparedStatements) {
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
      if (RdbmServices.supportsPreparedStatements) {
        pstmt.setString(index, value);
      } else {
        if (index != lastIndex+1) {
          throw new SQLException("Out of order index");
        } else {
          int pos = activeQuery.indexOf("?");
          if (pos == -1) {
            throw new SQLException("Missing '?'");
          }
          activeQuery = activeQuery.substring(0, pos) + "'" + value + "'" + activeQuery.substring(pos+1);
          lastIndex = index;
        }
       }
    }
    public ResultSet executeQuery() throws SQLException {
      if (RdbmServices.supportsPreparedStatements) {
        return pstmt.executeQuery();
      } else {
        return stmt.executeQuery(activeQuery);
      }
    }

    public int executeUpdate() throws SQLException {
      if (RdbmServices.supportsPreparedStatements) {
        return pstmt.executeUpdate();
      } else {
        return stmt.executeUpdate(activeQuery);
      }
    }

    public String toString() {
      if (RdbmServices.supportsPreparedStatements) {
        return query;
      } else {
        return activeQuery;
      }
    }

    public void close() throws SQLException {
      if (RdbmServices.supportsPreparedStatements) {
        pstmt.close();
      } else {
        stmt.close();
      }
    }
  }


}



