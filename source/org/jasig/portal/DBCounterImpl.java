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


/**
 * A class to keep track and generate incremental
 * IDs for various tables.
 * @author Peter Kharchenko
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.jasig.portal.utils.DTDResolver;


public class DBCounterImpl {

    private static RdbmServices rdbmService = new RdbmServices ();
    private static Connection connection=null;

    public DBCounterImpl() {
    }

    private Connection getConnection() {
        if(connection==null)
            connection=rdbmService.getConnection();
        return connection;
    }

    /*
     * get&increment method.
     */
    public synchronized Integer getIncrementIntegerId(String tableName) {
        Connection con=null;
        Integer id=null;
        try {
            con=this.getConnection();
            Statement stmt = con.createStatement ();
            String str_id=null;

            String sQuery = "SELECT ID FROM UP_COUNTERS WHERE TABLE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
            if (rs.next ()) {
                    str_id = rs.getString ("ID");
            }
            if(str_id!=null) {
                id=new Integer(Integer.parseInt(str_id)+1);
                sQuery = "UPDATE UP_COUNTERS SET ID="+id.intValue()+"WHERE TABLE_NAME='" + tableName + "'";
                stmt.executeUpdate(sQuery);
            }
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return id;
    }

    public synchronized void createCounter(String tableName) {
        Connection con=null;
        try {
            con=this.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "INSERT INTO UP_COUNTERS ('TABLE_NAME,ID') VALUES ('"+tableName+"',0)";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }

    public synchronized void setCounter(String tableName,int value) {
        Connection con=null;
        try {
            con=this.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "UPDATE UP_COUNTERS SET ID="+value+"WHERE TABLE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate (sQuery);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }
}

