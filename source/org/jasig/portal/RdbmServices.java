/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import  java.sql.Connection;
import  java.sql.DriverManager;
import  java.io.File;
import  java.io.FileNotFoundException;
import  java.io.FileInputStream;
import  java.io.IOException;
import  java.util.Properties;
import  org.jasig.portal.Logger;


/**
 * Provides database access
 * @author Ken Weiner
 * @version $Revision$
 */
public class RdbmServices extends GenericPortalBean {
  private static String sJdbcDriver = null;
  private static String sJdbcUrl = null;
  private static String sJdbcUser = null;
  private static String sJdbcPassword = null;
  static {
    // This should point to the rdbm.properties file
    String propertiesFilePath = getPortalBaseDir() + "properties" + File.separator + "rdbm.properties";
    File jdbcPropsFile = new File(propertiesFilePath);
    // Give up if the properties file does not exist
    if (!jdbcPropsFile.exists()) {
      Logger.log(Logger.ERROR, "Cannot find rdbm.properties at: " + propertiesFilePath);
    } 
    else {
      Properties jdbcProps = new Properties();
      try {
        jdbcProps.load(new FileInputStream(jdbcPropsFile));
      } catch (Exception e) {
        Logger.log(Logger.ERROR, "RdbmServices.static: Could not load " + propertiesFilePath);
        Logger.log(Logger.ERROR, e);
      }
      sJdbcDriver = jdbcProps.getProperty("jdbcDriver");
      sJdbcUrl = jdbcProps.getProperty("jdbcUrl");
      sJdbcUser = jdbcProps.getProperty("jdbcUser");
      sJdbcPassword = jdbcProps.getProperty("jdbcPassword");
      try {
        Class.forName(sJdbcDriver).newInstance();
      } catch (ClassNotFoundException e) {
        Logger.log(Logger.ERROR, "RdbmServices.static: Could not load JDBC driver " + sJdbcDriver);
        Logger.log(Logger.ERROR, e);
      } catch (InstantiationException ie) {
        Logger.log(Logger.ERROR, "RdbmServices.static: Could not instantiate JDBC driver " + sJdbcDriver);
        Logger.log(Logger.ERROR, ie);
      } catch (IllegalAccessException iae) {
        Logger.log(Logger.ERROR, "RdbmServices.static: Could not instantiate JDBC driver " + sJdbcDriver);
        Logger.log(Logger.ERROR, iae);
      }
    }
  }

  /**
   * Gets a database connection
   * @return a database Connection object
   */
  public static Connection getConnection () {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(sJdbcUrl, sJdbcUser, sJdbcPassword);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, "RdbmServices.getConnection(): Problem getting connection to " + sJdbcUrl);
      Logger.log(Logger.ERROR, e);
    }
    return  conn;
  }

  /**
   * Releases database connection
   * @param a database Connection object
   */
  public static void releaseConnection (Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      Logger.log(Logger.ERROR, "RdbmServices.releaseConnection(): Problem releasing connection");
      Logger.log(Logger.ERROR, e);
    }
  }
}



