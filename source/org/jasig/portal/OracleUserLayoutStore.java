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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Oracle optimized SQL implementation of the 2.x relational database model
 * @author George Lindholm
 * @version $Revision$
 */

/**
 * Sequence numbers have the form of {Table Name}_SEQ and, at the moment, they must
 * have been created by hand before uPortal is started. See UP_SEQUENCE in properties/data.xml
 * for the tables that expect sequence counters, and the expected starting value.
 */
public final class OracleUserLayoutStore extends RDBMUserLayoutStore
    implements IUserLayoutStore {

    private static final Log log = LogFactory.getLog(OracleUserLayoutStore.class);
    
  public OracleUserLayoutStore() throws Exception {
    super();
  }

  /* DBCounter */
  /*
   * get&increment method.
   */

   /**
    * Get the next incremental value
   * @param tableName
   * @exception Exception
    */
  public int getIncrementIntegerId (String tableName) throws Exception {
    int id;
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT " + tableName + "_SEQ.NEXTVAL FROM DUAL";
        log.debug("OracleUserLayoutStore::getIncrementInteger(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          rs.next();            // If this doesn't work then the database is munged up
          id = rs.getInt(1);
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  id;
  }

  /**
   * Create a sequence counter
   * @param tableName
   * @exception Exception
   */
  public void createCounter (String tableName) throws Exception {
    createCounter(tableName, 1);
  }

  /**
   * Create a sequence counter, starting with a specific value
   * @param tableName
   * @param startAt
   * @exception Exception
   */
  protected void createCounter (String tableName, int startAt) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sInsert = "CREATE SEQUENCE " + tableName + "_SEQ INCREMENT BY 1 START WITH " + startAt + " NOMAXVALUE NOCYCLE";
        log.debug("OracleUserLayoutStore::createCounter(): " + sInsert);
        stmt.executeUpdate(sInsert);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Modify the current value of a counter
   * @param tableName
   * @param value
   * @exception Exception
   */
  public synchronized void setCounter (String tableName, int value) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {

        /* This is dangerous */
        String sUpdate = "DROP SEQUENCE " + tableName + "_SEQ";
        log.debug("OracleUserLayoutStore::setCounter(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
        createCounter(tableName, value);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }
}



