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

import  java.sql.*;
import  org.w3c.dom.Element;
import  org.apache.xerces.dom.DocumentImpl;


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
public class OracleUserLayoutStore extends RDBMUserLayoutStore
    implements IUserLayoutStore {

  /**
   * put your documentation comment here
   * @param con
   * @param doc
   * @param chanId
   * @param idTag
   * @return
   * @exception java.sql.SQLException
   */
  protected Element createChannelNode (Connection con, DocumentImpl doc, int chanId, String idTag) throws java.sql.SQLException {
    Element channel = null;
    String sQuery = "SELECT UC.*, CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM UP_CHANNEL UC, UP_CHAN_PARAM UCP WHERE UC.CHAN_ID="
        + chanId + " AND UC.CHAN_ID = UCP.CHAN_ID(+)";
    Logger.log(Logger.DEBUG, "OracleUserLayoutStore::createChannelNode(): " + sQuery);
    Statement stmt = con.createStatement();
    try {
      ResultSet rs = stmt.executeQuery(sQuery);
      try {
        if (rs.next()) {
          channel = doc.createElement("channel");
          Element system = doc.createElement("system");
          createChannelNodeHeaders(doc, chanId, idTag, rs, channel);
          do {
            createChannelNodeParameters(doc, rs, channel);
          } while (rs.next());
          rs.close();
          channel.appendChild(system);
        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }
    return  channel;
  }

  /**
   * put your documentation comment here
   * @param con
   * @param doc
   * @param stmt
   * @param root
   * @param userId
   * @param profileId
   * @param layoutId
   * @param structId
   * @exception java.sql.SQLException
   */
  protected void createLayout (Connection con, DocumentImpl doc, Statement stmt, Element root, int userId, int profileId,
      int layoutId, int structId) throws java.sql.SQLException {
    if (structId == 0) {        // End of line
      return;
    }
    int nextStructId;
    int chldStructId;
    int chanId;
    Element system = null;
    Element parameter = null;
    Element structure;
    String sQuery = "SELECT ULS.*,STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_STRUCT ULS, UP_STRUCT_PARAM USP WHERE ULS.USER_ID="
        + userId + " AND ULS.LAYOUT_ID = " + layoutId + " AND ULS.STRUCT_ID=" + structId + " AND ULS.STRUCT_ID = USP.STRUCT_ID(+)";
    Logger.log(Logger.DEBUG, "OracleUserLayoutStore::createLayout(): " + sQuery);
    ResultSet rs = stmt.executeQuery(sQuery);
    try {
      rs.next();
      nextStructId = rs.getInt("NEXT_STRUCT_ID");
      chldStructId = rs.getInt("CHLD_STRUCT_ID");
      chanId = rs.getInt("CHAN_ID");
      structure = createLayoutStructure(rs, chanId, userId, stmt, doc);
      if (chanId != 0) {        // Channel
        parameter = (Element)structure.getElementsByTagName("parameter").item(0);
      }
      do {
        createLayoutStructureParameter(chanId, rs, structure, parameter);
      } while (rs.next());
    } finally {
      rs.close();
    }
    root.appendChild(structure);
    if (chanId == 0) {          // Folder
      createLayout(con, doc, stmt, structure, userId, profileId, layoutId, chldStructId);
    }
    createLayout(con, doc, stmt, root, userId, profileId, layoutId, nextStructId);
  }

  /* DBCounter */
  /*
   * get&increment method.
   */
  public int getIncrementIntegerId (String tableName) throws Exception {
    int id;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT " + tableName + "_SEQ.NEXTVAL FROM DUAL";
        Logger.log(Logger.DEBUG, "OracleUserLayoutStore::getIncrementInteger(): " + sQuery);
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
      rdbmService.releaseConnection(con);
    }
    return  id;
  }

  /**
   * put your documentation comment here
   * @param tableName
   * @exception Exception
   */
  public void createCounter (String tableName) throws Exception {
    createCounter(tableName, 1);
  }

  /**
   * put your documentation comment here
   * @param tableName
   * @param startAt
   * @exception Exception
   */
  protected void createCounter (String tableName, int startAt) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sInsert = "CREATE SEQUENCE " + tableName + "_SEQ INCREMENT BY 1 START WITH " + startAt + " NOMAXVALUE NOCYCLE";
        Logger.log(Logger.DEBUG, "OracleUserLayoutStore::createCounter(): " + sInsert);
        stmt.executeUpdate(sInsert);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param tableName
   * @param value
   * @exception Exception
   */
  public synchronized void setCounter (String tableName, int value) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {

        /* This is dangerous */
        String sUpdate = "DROP SEQUENCE " + tableName + "_SEQ";
        Logger.log(Logger.DEBUG, "OracleUserLayoutStore::setCounter(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
        createCounter(tableName, value);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }
}



