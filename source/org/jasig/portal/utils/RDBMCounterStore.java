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


package org.jasig.portal.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.services.LogService;

/**
 * A reference implementation for the counter store
 *
 * @author George Lindholm, george.lindholm@ubc.ca
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class RDBMCounterStore implements ICounterStore {


    public synchronized void createCounter (String counterName) throws Exception {
        Connection con = RDBMServices.getConnection();
        try {
            RDBMServices.setAutoCommit(con, false);
            Statement stmt = con.createStatement();
            try {
                String sInsert = "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME,SEQUENCE_VALUE) VALUES ('" + counterName + "',0)";
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::createCounter(): " + sInsert);
                stmt.executeUpdate(sInsert);
                RDBMServices.commit(con);
            } catch (Exception e) {
                RDBMServices.rollback(con);
                throw e;
            } finally {
                stmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }


    public synchronized void setCounter (String counterName, int value) throws Exception {
        Connection con = RDBMServices.getConnection();
        try {
            RDBMServices.setAutoCommit(con, false);
            Statement stmt = con.createStatement();
            try {
                String sUpdate = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE=" + value + "WHERE SEQUENCE_NAME='" + counterName + "'";
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setCounter(): " + sUpdate);
                stmt.executeUpdate(sUpdate);
                RDBMServices.commit(con);
            } catch (Exception e) {
                RDBMServices.rollback(con);
                throw e;
            } finally {
                stmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }

    /*
     * get&increment method.
     */
    public synchronized int getIncrementIntegerId (String counterName) throws Exception {
        Connection con = RDBMServices.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            try {
                String sQuery = "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME='" + counterName + "'";
                for (int i = 0; i < 25; i++) 
                {
                    try 
                    {
                        LogService.log(LogService.DEBUG, "RDBMCounterStore::getIncrementInteger(): " + sQuery);
                        rs = stmt.executeQuery(sQuery);
                        int origId;
                        int nextId;
                        rs.next();
                        origId = rs.getInt(1);
                        nextId = origId + 1;
                        String sUpdate = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE=" + nextId + " WHERE SEQUENCE_NAME='" + counterName + "'" +
                            " AND SEQUENCE_VALUE=" + origId;
                        LogService.log(LogService.DEBUG, "RDBMCounterStore::getIncrementInteger(): " + sUpdate);
                        int rowsUpdated = stmt.executeUpdate(sUpdate);
                        if (rowsUpdated > 0) 
                            { return nextId; }
                        else 
                        {
                            // Assume concurrent update (from other server). Try again after some random amount of milliseconds. 
                            Thread.sleep(java.lang.Math.round(java.lang.Math.random()* 3 * 1000)); // Retry in up to 3 seconds
                        }
                    }  // end try
                    finally
                    {
                        if ( rs != null )
                            { rs.close(); } 
                    }
                }      // end for
            }          // end try 
            finally 
            {
                if (stmt != null)
                    { stmt.close(); } 
            }
        } catch (Exception e) {
            LogService.log(LogService.ERROR, e);
        } finally {
            RDBMServices.releaseConnection(con);
        }
        throw new Exception("Unable to increment counter for " + counterName);
    }

}
