/**
 *  Copyright (c) 2000 The JA-SIG Collaborative. All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer. 2. Redistributions in
 *  binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or other
 *  materials provided with the distribution. 3. Redistributions of any form
 *  whatsoever must retain the following acknowledgment: "This product includes
 *  software developed by the JA-SIG Collaborative (http://www.jasig.org/)."
 *  THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 *  EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;

/**
 *  Provides database access
 *
 *@author     Ken Weiner
 *@created    June 18, 2001
 *@version    $Revision$
 */
public class RdbmServices extends GenericPortalBean {
    private static boolean bPropsLoaded = false;
    private static String sJdbcDriver = null;
    private static String sJdbcUrl = null;
    private static String sJdbcUser = null;
    private static String sJdbcPassword = null;


    /**
     *  Constructor which loades JDBC parameters from property file upon first
     *  invocation
     */
    public RdbmServices() {
        try {
            if (!bPropsLoaded) {
                File jdbcPropsFile = new File(getPortalBaseDir() + "properties" + File.separator + "rdbm.properties");
                Properties jdbcProps = new Properties();
                jdbcProps.load(new FileInputStream(jdbcPropsFile));

                sJdbcDriver = jdbcProps.getProperty("jdbcDriver");
                sJdbcUrl = jdbcProps.getProperty("jdbcUrl");
                sJdbcUser = jdbcProps.getProperty("jdbcUser");
                sJdbcPassword = jdbcProps.getProperty("jdbcPassword");

                bPropsLoaded = true;
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }


    /**
     *  Gets a database connection
     *
     *@return    a database Connection object
     */
    public static Connection getConnection() {
        Connection conn = null;

        try {
            Class.forName(sJdbcDriver).newInstance();
            conn = DriverManager.getConnection(sJdbcUrl, sJdbcUser, sJdbcPassword);
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }

        return conn;
    }


    /**
     *  Releases database connection
     *
     *@param  con  Description of Parameter
     */
    public static void releaseConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        }
        catch (Exception e) {
            Logger.log(Logger.ERROR, e);
        }
    }
}

