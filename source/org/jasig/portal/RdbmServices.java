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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  org.jasig.portal.services.LogService;
import  javax.servlet.*;
import  javax.servlet.jsp.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  java.sql.*;


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
  private static String m_channelRegistryStoreClassName = null;
  private static String m_userLayoutStoreClassName = null;
  private static String m_userIdentityStoreClassName = null;
  private static String m_userPreferencesStoreClassName = null;
  private static String m_coreStyleSheetDescriptionStoreClassName = null;
  public static int RETRY_COUNT = 5;
  private static String prevErrorMsg = "";      // reduce noise in log file
  static {
    loadProps();
  }

  /**
   * put your documentation comment here
   */
  protected static void loadProps () {
    try {
      if (!bPropsLoaded) {
        File jdbcPropsFile = new File(PortalSessionManager.getPortalBaseDir() + "properties" + File.separator + "rdbm.properties");
        Properties jdbcProps = new Properties();
        jdbcProps.load(new FileInputStream(jdbcPropsFile));
        sJdbcDriver = jdbcProps.getProperty("jdbcDriver");
        sJdbcUrl = jdbcProps.getProperty("jdbcUrl");
        sJdbcUser = jdbcProps.getProperty("jdbcUser");
        sJdbcPassword = jdbcProps.getProperty("jdbcPassword");
        m_channelRegistryStoreClassName = jdbcProps.getProperty("ChannelRegistryStore");
        m_userLayoutStoreClassName = jdbcProps.getProperty("UserLayoutStore");
        m_userIdentityStoreClassName = jdbcProps.getProperty("UserIdentityStore");
        m_userPreferencesStoreClassName = jdbcProps.getProperty("UserPreferencesStore");
        m_coreStyleSheetDescriptionStoreClassName = jdbcProps.getProperty("CoreStyleSheetDescriptionStore");
        bPropsLoaded = true;
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
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
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " was not found, please check the logs/rdbm.properties file and your classpath.");
        return  null;
      } catch (InstantiationException ie) {
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " could not be instantiated, please check the logs/rdbm.properties file.");
        return  null;
      } catch (IllegalAccessException iae) {
        LogService.instance().log(LogService.ERROR, "The driver " + sJdbcDriver + " could not be instantiated, please check the logs/rdbm.properties file.");
        return  null;
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
   * put your documentation comment here
   * @return
   */
  public static IUserLayoutStore getUserLayoutStoreImpl () {
    try {
      return  ((IUserLayoutStore)Class.forName(m_userLayoutStoreClassName).newInstance());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }

    /**
   * Return implementation for IUserIdentityStore
   * @return
   */
  public static IUserIdentityStore getUserIdentityStoreImpl () {
    try {
      return  ((IUserIdentityStore)Class.forName(m_userIdentityStoreClassName).newInstance());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @return
   */
  public static IUserPreferencesStore getUserPreferencesStoreImpl () {
    try {
      return  ((IUserPreferencesStore)Class.forName(m_userPreferencesStoreClassName).newInstance());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @return
   */
  public static ICoreStylesheetDescriptionStore getCoreStylesheetDescriptionImpl () {
    try {
      return  ((ICoreStylesheetDescriptionStore)Class.forName(m_coreStyleSheetDescriptionStoreClassName).newInstance());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @return
   */
  public static IChannelRegistryStore getChannelRegistryStoreImpl () {
    try {
      return  ((IChannelRegistryStore)Class.forName(m_channelRegistryStoreClassName).newInstance());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }
}



