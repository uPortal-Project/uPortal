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

import  org.w3c.dom.Document;
import  org.w3c.dom.Node;
import  org.w3c.dom.NamedNodeMap;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.apache.xerces.dom.*;
import  org.apache.xerces.parsers.DOMParser;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  org.xml.sax.EntityResolver;
import  org.xml.sax.InputSource;
import  org.jasig.portal.utils.DTDResolver;
import  org.jasig.portal.services.LogService;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.XMLSerializer;
import  org.jasig.portal.security.IRole;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.security.IPerson;

/**
 * SQL implementation for the 2.x relational database model
 * @author George Lindholm
 * @version $Revision$
 */
public class RDBMUserLayoutStore
    implements IUserLayoutStore {
  //This class is instantiated ONCE so NO class variables can be used to keep state between calls
  static int DEBUG = 0;
  protected static RdbmServices rdbmService = null;
  protected static final String channelPrefix = "n";
  protected static final String folderPrefix = "s";
  protected static boolean supportsPreparedStatements = false;
  protected static boolean supportsOuterJoins = false;
  protected static boolean supportsTransactions = false;

  static class DbStrings {
    String testJoin;
    String layoutStructure;
    String channel;
    String structureStylesheet;
    String themeStylesheet;
    public DbStrings(String testJoin, String layoutStructure, String channel, String structureStylesheet,
      String themeStylesheet) {
      this.testJoin = testJoin;
      this.layoutStructure = layoutStructure;
      this.channel = channel;
      this.structureStylesheet = structureStylesheet;
      this.themeStylesheet = themeStylesheet;
    }
  }
  static final DbStrings jdbcDb = new DbStrings(
    "FROM {oj UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID} WHERE",
    "FROM {oj UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.STRUCT_ID = USP.STRUCT_ID} WHERE",
    "FROM {oj UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID} WHERE",
    "FROM {oj UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID} WHERE",
    "FROM {oj UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID} WHERE");

  /* Some database with broken jdbc drivers */
  static final DbStrings PostgreSQLDb = new DbStrings(
    "FROM UP_USER LEFT OUTER JOIN UP_USER_LAYOUT ON UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID WHERE",
    "FROM UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.STRUCT_ID = USP.STRUCT_ID WHERE",
    "FROM UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID WHERE",
    "FROM UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID WHERE",
    "FROM UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID WHERE");
  static final DbStrings OracleDb = new DbStrings(
    "FROM UP_USER, UP_USER_LAYOUT WHERE UP_USER.USER_ID = UP_USER_LAYOUT.USER_ID(+) AND",
    "FROM UP_LAYOUT_STRUCT ULS, UP_LAYOUT_PARAM USP WHERE ULS.STRUCT_ID = USP.STRUCT_ID(+) AND",
    "FROM UP_CHANNEL UC, UP_CHANNEL_PARAM UCP WHERE UC.CHAN_ID = UCP.CHAN_ID(+) AND",
    "FROM UP_SS_STRUCT USS, UP_SS_STRUCT_PAR USP WHERE USS.SS_ID=USP.SS_ID(+) AND",
    "FROM UP_SS_THEME UTS, UP_SS_THEME_PARM UTP WHERE UTS.SS_ID=UTP.SS_ID(+) AND");

  static final DbStrings[] joinDbStrings = {jdbcDb, PostgreSQLDb, OracleDb};
  static DbStrings dbStrings;
  static {
      String sql;
      rdbmService = new RdbmServices();
      Connection con = rdbmService.getConnection();
      try {
        sql = "SELECT USER_ID FROM UP_USER WHERE USER_ID=?";
        try {
          PreparedStatement pstmt = con.prepareStatement(sql);
          try {
            pstmt.clearParameters ();
            pstmt.setInt(1, 0);
            pstmt.executeQuery();
            supportsPreparedStatements = false;
          } finally {
            pstmt.close();
          }
        } catch (SQLException sqle) {
          System.err.println(sqle + ":" + sql);
        }

        try {
          if (con.getMetaData().supportsOuterJoins()) {
            Statement stmt = con.createStatement();
            DbStrings joinDb = null;
            try {
              for (int i = 0; i < joinDbStrings.length; i++) {
                sql = "SELECT UP_USER.USER_ID " + joinDbStrings[i].testJoin + " UP_USER.USER_ID=1";
                try {
                  stmt.executeQuery(sql);
                  dbStrings = joinDbStrings[i];
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

        try {
          supportsTransactions = con.getMetaData().supportsTransactions();
        } catch (SQLException sqle) {}

        LogService.instance().log(LogService.INFO, "Database supports: Outer Joins=" + supportsOuterJoins +", Prepared statements=" +
          supportsPreparedStatements + ", Transactions=" + supportsTransactions);
      } finally {
        rdbmService.releaseConnection(con);
      }
  }

  private static final Object channelLock = new Object();
  static final HashMap channelCache = new HashMap();

  protected final class ChannelDefinition {
    int chanId = -1;
    String chanTitle = "";
    String chanDesc = "";
    String chanClass = "";
    int chanTypeId;
    int chanPupblUsrId;
    int chanApvlId;
    Timestamp chanPublDt;
    Timestamp chanApvlDt;
    int chanTimeout;
    boolean chanMinimizable;
    boolean chanEditable;
    boolean chanHasHelp;
    boolean chanHasAbout;
    //   boolean chanUnremovable;
    boolean chanDetachable;
    String chanName = "";
    String chanFName = "";
    ArrayList parameters;

    private class ChannelParameter {
      String name;
      String value;
      boolean override;

      public ChannelParameter(String name, String value, String override) {
        this(name, value, override != null && override.equals("Y"));
      }
      public ChannelParameter(String name, String value, boolean override) {
        this.name = name;
        this.value = value;
        this.override = override;
      }
    }

    public Timestamp getchanApvlDt() { return chanApvlDt;}

    public ChannelDefinition(int chanId, String chanTitle) {
      this.chanId = chanId;
      this.chanTitle = chanTitle;
    }

    public ChannelDefinition(int chanId, String chanTitle, String chanDesc, String chanClass, int chanTypeId, int chanPupblUsrId, int chanApvlId,
      Timestamp chanPublDt, Timestamp chanApvlDt, int chanTimeout, String chanMinimizable, String chanEditable, String chanHasHelp,
      String chanHasAbout, /*   String chanUnremovable, */ String chanDetachable, String chanName, String chanFName) {
        this(chanId, chanTitle, chanDesc, chanClass, chanTypeId, chanPupblUsrId, chanApvlId, chanPublDt,  chanApvlDt, chanTimeout,
              chanMinimizable != null && chanMinimizable.equalsIgnoreCase("Y"),
              chanEditable!= null && chanEditable.equalsIgnoreCase("Y"),
              chanHasHelp!= null && chanHasHelp.equalsIgnoreCase("Y"),
              chanHasAbout!= null && chanHasAbout.equalsIgnoreCase("Y"),
              /* chanUnremovable!= null && chanUnremovable.equalsIgnoreCase("Y"), */
              chanDetachable!= null && chanDetachable.equalsIgnoreCase("Y"),
              chanName, chanFName);
    }

    public ChannelDefinition(int chanId, String chanTitle, String chanDesc, String chanClass, int chanTypeId, int chanPupblUsrId, int chanApvlId,
      Timestamp chanPublDt, Timestamp chanApvlDt, int chanTimeout, boolean chanMinimizable, boolean chanEditable, boolean chanHasHelp,
      boolean chanHasAbout, /*   boolean chanUnremovable, */ boolean chanDetachable, String chanName, String chanFName) {

      this.chanId = chanId;
      this.chanTitle = chanTitle;
      this.chanDesc = chanDesc;
      this.chanClass = chanClass;
      this.chanTypeId = chanTypeId;
      this.chanPupblUsrId = chanPupblUsrId;
      this.chanApvlId = chanApvlId;
      this.chanPublDt = chanPublDt;
      this.chanApvlDt = chanApvlDt;
      this.chanTimeout = chanTimeout;
      this.chanMinimizable =chanMinimizable;
      this.chanEditable = chanMinimizable;
      this.chanHasHelp = chanHasHelp;
      this.chanHasAbout = chanHasAbout;
      //   this.chanUnremovable = chanUnremovable;
      this.chanDetachable = chanDetachable;
      this.chanName = chanName;
      this.chanFName =chanFName;
      }

      public void addParameter(String name, String value, String override) {
        if (parameters == null) {
          parameters = new ArrayList(5);
        }

        parameters.add(new ChannelParameter(name, value, override));
      }

      /**
       * Minimum attributes a channel must have
       */
      private Element getBase(DocumentImpl doc, String idTag, String chanClass, boolean minimizable,
        boolean editable, boolean hasHelp, boolean  hasAbout, boolean detachable) {
        Element channel = doc.createElement("channel");
        doc.putIdentifier(idTag, channel);
        channel.setAttribute("ID", idTag);
        channel.setAttribute("chanID", chanId + "");
        if (DEBUG > 1) {
          System.err.println("channel " + chanName + "@" + chanId + " has tag " + chanId);
        }
        channel.setAttribute("timeout", chanTimeout + "");
        channel.setAttribute("name", chanName);
        channel.setAttribute("title", chanTitle);
        channel.setAttribute("fname", chanFName);
        channel.setAttribute("class", chanClass);
        channel.setAttribute("typeID", chanTypeId + "");
        channel.setAttribute("minimizable", minimizable ? "true" : "false");
        channel.setAttribute("editable", editable ? "true" : "false");
        channel.setAttribute("hasHelp", hasHelp ? "true" : "false");
        channel.setAttribute("hasAbout", hasAbout ? "true" : "false");
        channel.setAttribute("detachable", detachable ? "true" : "false");
        return channel;
      }
      private final Element nodeParameter(DocumentImpl doc, String name, String value) {
        Element parameter = doc.createElement("parameter");
        parameter.setAttribute("name", name);
        parameter.setAttribute("value", value);
        return parameter;
      }

      private final void addParameters(DocumentImpl doc, Element channel) {
        if (parameters != null) {
          for (int i = 0; i < parameters.size(); i++) {
            ChannelParameter cp = (ChannelParameter) parameters.get(i);

            Element parameter = nodeParameter(doc, cp.name, cp.value);
            if (cp.override) {
              parameter.setAttribute("override", "yes");
            }
            channel.appendChild(parameter);
          }
        }
      }
      /**
       * Display a message where this channel should be
       */
      public Element getDocument(DocumentImpl doc, String idTag, String statusMsg) {
        Element channel = getBase(doc, idTag, "org.jasig.portal.channels.CError", false, false, false,
                                  false, false);
        addParameters(doc, channel);
        channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
        channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
        return channel;
      }
      /**
       * return an xml representation of this channel
       */
      public Element getDocument(DocumentImpl doc, String idTag) {
        Element channel = getBase(doc, idTag, chanClass, chanMinimizable, chanEditable, chanHasHelp,
          chanHasAbout, chanDetachable);
        channel.setAttribute("description", chanDesc);
        //    channel.setAttribute("unremovable", chanUnremovable ? "true" : "false");
        addParameters(doc, channel);
        return channel;
      }

      /**
       * Is it time to reload me from the data store
       */
      public boolean refreshMe() {
        return false;
      }
  }

  /**
   * Wrapper for/Emulator of PreparedStatement class
   */
  protected static class MyPreparedStatement {
    Connection con;
    String query;
    String activeQuery;
    PreparedStatement pstmt;
    Statement stmt;
    int lastIndex;

    public MyPreparedStatement(Connection con, String query) throws SQLException {
      this.con = con;
      this.query = query;
      activeQuery = this.query;
      if (supportsPreparedStatements) {
        pstmt = con.prepareStatement(query);
      } else {
        stmt = con.createStatement();
      }
    }

    public void clearParameters() throws SQLException {
      if (supportsPreparedStatements) {
        pstmt.clearParameters();
      } else {
        lastIndex = 0;
        activeQuery = query;
      }
    }
    public void setInt(int index, int value) throws SQLException {
      if (supportsPreparedStatements) {
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
      if (supportsPreparedStatements) {
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
      if (supportsPreparedStatements) {
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
      if (supportsPreparedStatements) {
        return pstmt.executeQuery();
      } else {
        return stmt.executeQuery(activeQuery);
      }
    }

    public int executeUpdate() throws SQLException {
      if (supportsPreparedStatements) {
        return pstmt.executeUpdate();
      } else {
        return stmt.executeUpdate(activeQuery);
      }
    }

    public String toString() {
      if (supportsPreparedStatements) {
        return query;
      } else {
        return activeQuery;
      }
    }

    public void close() throws SQLException {
      if (supportsPreparedStatements) {
        pstmt.close();
      } else {
        stmt.close();
      }
    }
  }
  /**
   * put your documentation comment here
   */
  public RDBMUserLayoutStore () {
  }

  /**
   * Dump a document tree structure on stdout
   * @param node
   * @param indent
   */
  public static final void dumpDoc (Node node, String indent) {
    if (node == null) {
      return;
    }
    if (node instanceof Element) {
      System.err.print(indent + "element: tag=" + ((Element)node).getTagName() + " ");
    }
    else if (node instanceof Document) {
      System.err.print("document:");
    }
    else {
      System.err.print(indent + "node:");
    }
    System.err.println("name=" + node.getNodeName() + " value=" + node.getNodeValue());
    NamedNodeMap nm = node.getAttributes();
    if (nm != null) {
      for (int i = 0; i < nm.getLength(); i++) {
        System.err.println(indent + " " + nm.item(i).getNodeName() + ": '" + nm.item(i).getNodeValue() + "'");
      }
      System.err.println(indent + "--");
    }
    if (node.hasChildNodes()) {
      dumpDoc(node.getFirstChild(), indent + "   ");
    }
    dumpDoc(node.getNextSibling(), indent);
  }

   protected static final MyPreparedStatement getChannelPstmt(Connection con) throws SQLException {
    String sql;
    sql = "SELECT UC.CHAN_TITLE,UC.CHAN_DESC,UC.CHAN_CLASS,UC.CHAN_TYPE_ID,UC.CHAN_PUBL_ID,UC.CHAN_APVL_ID,UC.CHAN_PUBL_DT,UC.CHAN_APVL_DT,"+
      "UC.CHAN_TIMEOUT,UC.CHAN_MINIMIZABLE,UC.CHAN_EDITABLE,UC.CHAN_HAS_HELP,UC.CHAN_HAS_ABOUT,UC.CHAN_UNREMOVABLE,UC.CHAN_DETACHABLE,UC.CHAN_NAME,UC.CHAN_FNAME";

    if (supportsOuterJoins) {
      sql += ",CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC " + dbStrings.channel;
    } else {
      sql += " FROM UP_CHANNEL UC WHERE";
    }
    sql += " UC.CHAN_ID=?";

    return new MyPreparedStatement(con, sql);
  }
  protected static final MyPreparedStatement getChannelParmPstmt(Connection con) throws SQLException {
    if (supportsOuterJoins) {
      return null;
    } else {
      return new MyPreparedStatement(con, "SELECT CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM UP_CHANNEL_PARAM WHERE CHAN_ID=?");
    }
  }
  /**
   * put your documentation comment here
   * @param con
   * @param doc
   * @param chanId
   * @param idTag
   * @return
   * @exception java.sql.SQLException
   */

  protected Element getChannelNode (int chanId, Connection con, DocumentImpl doc, String idTag) throws java.sql.SQLException {
    MyPreparedStatement pstmtChannel = getChannelPstmt(con);
    try {
      MyPreparedStatement pstmtChannelParm = getChannelParmPstmt(con);
      try {
        ChannelDefinition cd = getChannel(chanId, false, pstmtChannel, pstmtChannelParm);
        if (cd != null) {
          return cd.getDocument(doc, idTag);
        } else {
          return null;
        }
      } finally {
        if (pstmtChannelParm != null) {
          pstmtChannelParm.close();
        }
      }
    } finally {
      pstmtChannel.close();
    }
  }

    /**
     * Manage the Channel cache
     */

     /**
      * Get a channel from the cache (it better be there)
      */
  protected ChannelDefinition getChannel(int chanId) {
    return (ChannelDefinition)channelCache.get(new Integer(chanId));
  }

     /**
      * Get a channel from the cache (it better be there)
      */
  protected Element getChannel(int chanId, DocumentImpl doc, String idTag) {
    ChannelDefinition channel = getChannel(chanId);
    if (channel != null) {
      return channel.getDocument(doc, idTag);
    } else {
      return null;
    }
  }

  /**
   * See if the channel is already in the cache
   */
  protected boolean channelCached(int chanId) {
    return channelCache.containsKey(new Integer(chanId));
  }

  /**
   * Get a channel from the cache or the store
   */
  protected ChannelDefinition getChannel(int chanId, boolean cacheChannel, MyPreparedStatement pstmtChannel, MyPreparedStatement pstmtChannelParm) throws java.sql.SQLException {
    Integer chanID = new Integer(chanId);
    boolean inCache = true;
    ChannelDefinition channel = (ChannelDefinition)channelCache.get(chanID);
    if (channel == null) {
      synchronized (channelLock) {
        channel = (ChannelDefinition)channelCache.get(chanID);
        if (channel == null || cacheChannel && channel.refreshMe()) {  // Still undefined or stale, let's get it
          channel = getChannelDefinition(chanId, pstmtChannel, pstmtChannelParm);
          inCache = false;
          if (cacheChannel) {
            channelCache.put(chanID, channel);
            if (DEBUG > 1) {
              System.err.println("Cached channel " + chanId);
            }
          }
        }
      }
    }

    if (inCache) {
      LogService.instance().log(LogService.DEBUG,
        "RDBMUserLayoutStore::getChannelDefinition(): Got channel " + chanId + " from the cache");
    }

    return channel;
  }
  /**
   * Read a channel definition from the data store
   */
  protected ChannelDefinition getChannelDefinition (int chanId, MyPreparedStatement pstmtChannel, MyPreparedStatement pstmtChannelParm) throws java.sql.SQLException {
    ChannelDefinition channel = null;

    pstmtChannel.clearParameters();
    pstmtChannel.setInt(1, chanId);
    LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getChannel(): " + pstmtChannel);
    ResultSet rs = pstmtChannel.executeQuery();
    try {
      if (rs.next()) {
        channel = new ChannelDefinition(chanId, rs.getString(1), rs.getString(2), rs.getString(3),
        rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getTimestamp(7), rs.getTimestamp(8), rs.getInt(9),
          rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), // rs.getString(14),
          rs.getString(15), rs.getString(16), rs.getString(17));

        int dbOffset = 0;
        if (pstmtChannelParm == null) { // we are using a join statement so no need for a new query
          dbOffset = 17;
        } else {
          rs.close();
          pstmtChannelParm.clearParameters();
          pstmtChannelParm.setInt(1, chanId);
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getChannel(): " + pstmtChannelParm);
          rs = pstmtChannelParm.executeQuery();
        }
        while (true) {
          if (pstmtChannelParm != null && !rs.next()) {
            break;
          }
          channel.addParameter(rs.getString(dbOffset + 1), rs.getString(dbOffset + 2),rs.getString(dbOffset + 3));
          if (pstmtChannelParm == null && !rs.next()) {
            break;
          }
        }
      }
    } finally {
      rs.close();
    }

    LogService.instance().log(LogService.DEBUG,
      "RDBMUserLayoutStore::getChannelDefinition(): Read channel " + chanId + " from the store");
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
   protected final void createLayout (HashMap layoutStructure, DocumentImpl doc,
        Element root, int structId, UserInRole uir) throws java.sql.SQLException, Exception {
      while (structId != 0) {
        if (DEBUG>1) {
          System.err.println("CreateLayout(" + structId + ")");
        }
        LayoutStructure ls = (LayoutStructure) layoutStructure.get(new Integer(structId));
        Element structure = ls.getStructureDocument(doc, uir);
        root.appendChild(structure);
        if (!ls.isChannel()) {          // Folder
          createLayout(layoutStructure, doc,  structure, ls.getChildId(), uir);
        }
        structId = ls.getNextId();
      }
  }



  /**
   * put your documentation comment here
   * @param approved Date
   * @return boolean Channel is approved
   */
   protected static boolean channelApproved(java.sql.Timestamp approvedDate) {
      java.sql.Timestamp rightNow = new java.sql.Timestamp(System.currentTimeMillis());
      return (approvedDate != null && rightNow.after(approvedDate));
   }

  /**
   * put your documentation comment here
   * @param chanId
   * @param userId
   * @param con
   * @return
   * @exception java.sql.SQLException
   */
  protected static boolean channelInUserRole (int chanId, int userId, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "SELECT UC.CHAN_ID FROM UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR, UP_USER_ROLE UUR " + "WHERE UUR.USER_ID="
          + userId + " AND UC.CHAN_ID=" + chanId + " AND UUR.ROLE_ID=UR.ROLE_ID AND UR.ROLE_ID=URC.ROLE_ID AND URC.CHAN_ID=UC.CHAN_ID";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::channelInUserRole(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      try {
        return  rs.next();
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }
  }

  private final class UserInRole {
    MyPreparedStatement pstmtUserInRole;
    MyPreparedStatement pstmtReadAll;
    public UserInRole(Connection con, String roleIds) throws SQLException {
      String sQuery = "SELECT UC.CHAN_ID FROM UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR " +
        "WHERE UC.CHAN_ID=? AND UR.ROLE_ID IN (" + roleIds + ") AND UR.ROLE_ID=URC.ROLE_ID AND URC.CHAN_ID=UC.CHAN_ID";
      pstmtUserInRole = new MyPreparedStatement(con, sQuery);
      sQuery = "SELECT COUNT(ROLE_ID) FROM UP_ROLE_CHAN WHERE CHAN_ID=?";
      pstmtReadAll = new MyPreparedStatement(con, sQuery);
    }
    public boolean isAllowed(int chanId) throws SQLException {

      // First see if everyone has access to this channel
      pstmtReadAll.clearParameters();
      pstmtReadAll.setInt(1, chanId);
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::UserInRole::isAllowed(): " + pstmtReadAll);
      ResultSet rs = pstmtReadAll.executeQuery();
      try {
        if (rs.next() && rs.getInt(1) == 0) { // none implies all
          return true;
        }
      } finally {
        rs.close();
      }

      // See if there is an explicit permission to the channel
      pstmtUserInRole.clearParameters();
      pstmtUserInRole.setInt(1, chanId);
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::UserInRole::isAllowed(): " + pstmtUserInRole);
      rs = pstmtUserInRole.executeQuery();
      try {
        if (rs.next()) {
          return true;
        }
      } finally {
        rs.close();
      }

      return false;
    }

    public void close() {
      try {
        if (pstmtUserInRole != null) {
          pstmtUserInRole.close();
        }
      } catch (Exception e) {}
      try {
        if (pstmtReadAll != null) {
          pstmtReadAll.close();
        }
      } catch (Exception e) {}
    }
  }
  /**
   * LayoutStructure
   * Encapsulate the layout structure
   */
   protected final class LayoutStructure {
    private class StructureParameter {
      String name;
      String value;
      public StructureParameter(String name, String value) {
        this.name = name;
        this.value = value;
      }
    }

    int structId;
    int nextId;
    int childId;
    int chanId;
    String name;
    String type;
    boolean hidden;
    boolean unremovable;
    boolean immutable;
    ArrayList parameters;

    /**
     *
     */
    public LayoutStructure(int structId, int nextId,int childId,int chanId, String hidden, String unremovable, String immutable) {
      this.nextId = nextId;
      this.childId = childId;
      this.chanId = chanId;
      this.structId = structId;
      this.hidden = hidden != null && hidden.equals("Y");
      this.unremovable = immutable != null && immutable.equals("Y");
      this.immutable = unremovable != null && unremovable.equals("Y");

      if (DEBUG > 1) {
        System.err.println("New layout: id=" + structId + ", next=" + nextId + ", child=" + childId +", chan=" +chanId);
      }
    }

    public void addFolderData(String name, String type) {
      this.name = name;
      this.type = type;
    }

    public boolean isChannel () {return chanId != 0;}

    public void addParameter(String name, String value) {
      if (parameters == null) {
        parameters = new ArrayList(5);
      }

      parameters.add(new StructureParameter(name, value));
    }

    public int getNextId () {return nextId;}
    public int getChildId () {return childId;}
    public int getChanId () {return chanId;}

    public Element getStructureDocument(DocumentImpl doc, UserInRole uir) throws Exception {
      Element structure = null;

      if (isChannel()) {
        if (uir.isAllowed(chanId)) {
          structure = getChannel(chanId, doc, channelPrefix + structId);
        } else {            // No access
          ChannelDefinition channel= getChannel(chanId);
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureDocument(): no access to channel "
            + chanId);
          if (channel != null) {
            structure = channel.getDocument(doc, channelPrefix + structId,
              "You do not have permission to use this channel.");
          }
        }

        if (structure == null) {
          // Can't find channel
          ChannelDefinition cd = new ChannelDefinition(chanId, "Missing channel");
          structure = cd.getDocument(doc, channelPrefix + structId,
           "This channel no longer exists. You should remove it from your layout.");
        }
      } else {
        structure = doc.createElement("folder");
        doc.putIdentifier(folderPrefix + structId, structure);
        structure.setAttribute("ID", folderPrefix + structId);
        structure.setAttribute("name", name);
        structure.setAttribute("type", (type != null ? type : "regular"));
      }

      structure.setAttribute("hidden", (hidden ? "true" : "false"));
      structure.setAttribute("immutable", (immutable ? "true" : "false"));
      structure.setAttribute("unremovable", (unremovable ? "true" : "false"));

      if (parameters != null) {
        for (int i = 0; i < parameters.size(); i++) {
          StructureParameter sp = (StructureParameter)parameters.get(i);

          if (!isChannel()) {        // Folder
            structure.setAttribute(sp.name, sp.value);
          } else {                    // Channel
            NodeList parameters = structure.getElementsByTagName("parameter");
            for (int j = 0; j < parameters.getLength(); j++) {
              Element parmElement = (Element)parameters.item(j);
              NamedNodeMap nm = parmElement.getAttributes();

              String nodeName = nm.getNamedItem("name").getNodeValue();
              if (nodeName.equals(sp.name)) {
                Node override = nm.getNamedItem("override");
                if (override != null && override.getNodeValue().equals("yes")) {
                  Node valueNode = nm.getNamedItem("value");
                  valueNode.setNodeValue(sp.value);
                }
              }
            }
          }
        }
      }
      return structure;
    }
  }

  public Document getUserLayout (IPerson person, int profileId) throws Exception {
    int userId = person.getID();
    int realUserId = userId;
    Connection con = rdbmService.getConnection();
    setAutoCommit(con, false);          // May speed things up, can't hurt

    try {
      DocumentImpl doc = new DocumentImpl();
      Element root = doc.createElement("layout");
      Statement stmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" +
            profileId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
          rs.next();
          layoutId = rs.getInt(1);
        } finally {
          rs.close();
        }

        if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            userId = rs.getInt(1);
            layoutId = rs.getInt(2);
          } finally {
            rs.close();
          }

          // Make sure the next struct id is set in case the user adds a channel
          sQuery = "SELECT MAX(STRUCT_ID) FROM UP_LAYOUT_STRUCT WHERE USER_ID=" + userId +
            " AND LAYOUT_ID=" + layoutId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          int nextStructId;
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            nextStructId = rs.getInt(1);
          } finally {
            rs.close();
          }
          sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + realUserId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
          stmt.executeUpdate(sQuery);
          commit(con); // Make sure it appears in the store
        }

        int firstStructId = -1;
        String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          rs.next();
          firstStructId = rs.getInt(1);
        } finally {
          rs.close();
        }

        String sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
          "ULS.UNREMOVABLE,ULS.IMMUTABLE";
        if (supportsOuterJoins) {
          sql += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL " + dbStrings.layoutStructure;
        } else {
          sql += " FROM UP_LAYOUT_STRUCT ULS WHERE ";
        }
        sql += " ULS.USER_ID=" + userId + " AND ULS.LAYOUT_ID=" + layoutId + " ORDER BY ULS.STRUCT_ID";
        HashMap layoutStructure = new HashMap();
        ArrayList chanIds = new ArrayList();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          StringBuffer structParms = new StringBuffer();
        rs = stmt.executeQuery(sql);
        try {
          int lastStructId = 0;
          LayoutStructure ls = null;
          String sepChar = "";
          if (rs.next()) {
            int structId = rs.getInt(1);
            if (DEBUG > 1) System.err.println("Read layout structrure " + structId);
            readLayout: while (true) {
              int chanId = rs.getInt(4);
              ls = new LayoutStructure(structId,rs.getInt(2), rs.getInt(3), chanId, rs.getString(7),rs.getString(8),rs.getString(9));
              layoutStructure.put(new Integer(structId), ls);
              lastStructId = structId;
              if (!ls.isChannel()) {
                ls.addFolderData(rs.getString(5), rs.getString(6));
              } else {
                chanIds.add(new Integer(chanId)); // For later
              }
              if (supportsOuterJoins) {
                do {
                  String name = rs.getString(10);
                  String value = rs.getString(11); // Oracle JDBC requires us to do this for longs
                  if (name != null) { // may not be there because of the join
                    ls.addParameter(name, value);
                  }
                  if (!rs.next()) {
                    break readLayout;
                  }
                  structId = rs.getInt(1);
                } while (structId == lastStructId);
              } else { // Do second SELECT later on for structure parameters
                if (ls.isChannel()) {
                  structParms.append(sepChar + ls.chanId);
                  sepChar = ",";
                }
                if (rs.next()) {
                  structId = rs.getInt(1);
                } else {
                  break readLayout;
                }
              }
            } // while
          }
        } finally {
          rs.close();
        }

        /**
        * We have to retrieve the channel defition after the layout structure
        * since retrieving the channel data from the DB may interfere with the
        * layout structure ResultSet (in other words, Oracle is a pain to program for)
        */
        if (chanIds.size() > 0) {
          MyPreparedStatement pstmtChannel = getChannelPstmt(con);
          try {
            MyPreparedStatement pstmtChannelParm = getChannelParmPstmt(con);
            try {
              // Pre-prime the channel pump
              for (int i = 0; i < chanIds.size(); i++) {
                int chanId = ((Integer) chanIds.get(i)).intValue();
                getChannel(chanId, true, pstmtChannel, pstmtChannelParm);
                if (DEBUG > 1) {
                  System.err.println("Precached " + chanId);
                }
              }
            } finally {
              if (pstmtChannelParm != null) {
                pstmtChannelParm.close();
              }
            }
          } finally {
            pstmtChannel.close();
          }
          chanIds.clear();
        }

        if (!supportsOuterJoins) { // Pick up structure parameters
          sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structParms.toString() + ") ORDER BY STRUCT_ID";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          rs = stmt.executeQuery(sql);
          try {
            if (rs.next()) {
              int structId = rs.getInt(1);
              readParm: while(true) {
                LayoutStructure ls = (LayoutStructure)layoutStructure.get(new Integer(structId));
                int lastStructId = structId;
                do {
                  ls.addParameter(rs.getString(2), rs.getString(3));
                  if (!rs.next()) {
                    break readParm;
                  }
                } while ((structId = rs.getInt(1)) == lastStructId);
              }
            }
          } finally {
            rs.close();
          }
        }

        if (layoutStructure.size() > 0) { // We have a layout to work with
          String roleIds = ""; // Roles that this user belongs to

          sQuery = " SELECT ROLE_ID FROM UP_USER_ROLE WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            String sep = "";
            while (rs.next()) {
              roleIds += sep + rs.getInt(1);
              sep = ",";
            }
          } finally {
            rs.close();
          }

          UserInRole uir = new UserInRole(con, roleIds);
          try {
            createLayout(layoutStructure, doc, root, firstStructId, uir);
          } finally {
            uir.close();
          }

          long stopTime = System.currentTimeMillis();
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
            (stopTime - startTime) + " milliseconds to create");
          doc.appendChild(root);
          if (DEBUG > 1) {
            System.err.println("--> created document");
            dumpDoc(doc, "");
            System.err.println("<--");
          }
        }
      } finally {
        stmt.close();
      }
      return  doc;
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Save the user layout
   * @param person
   * @param profileId
   * @param layoutXML
   * @throws Exception
   */
  public void setUserLayout (IPerson person, int profileId, Document layoutXML) throws Exception {
    int userId = person.getID();
    int layoutId = 0;
    Connection con = rdbmService.getConnection();
    try {
      setAutoCommit(con, false);                // Need an atomic update here
      Statement stmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();

        String query = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        try {
          rs.next();
          layoutId = rs.getInt(1);
        } finally {
          rs.close();
        }
        boolean firstLayout = false;
        if (layoutId == 0) { // First personal layout for this user/profile
          layoutId = 1;
          firstLayout = true;
        }

        String selectString = "USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId;
        String sSql = "DELETE FROM UP_LAYOUT_PARAM WHERE " + selectString;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
        stmt.executeUpdate(sSql);
        sSql = "DELETE FROM UP_LAYOUT_STRUCT WHERE " + selectString;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
        stmt.executeUpdate(sSql);
        if (DEBUG > 1) {
          System.err.println("--> saving document");
          dumpDoc(layoutXML.getFirstChild().getFirstChild(), "");
          System.err.println("<--");
        }
        MyPreparedStatement structStmt = new MyPreparedStatement(con,
          "INSERT INTO UP_LAYOUT_STRUCT " +
          "(USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID,EXTERNAL_ID,CHAN_ID,NAME,TYPE,HIDDEN,IMMUTABLE,UNREMOVABLE) " +
          "VALUES ("+ userId + "," + layoutId + ",?,?,?,?,?,?,?,?,?,?)");
        try {
          MyPreparedStatement parmStmt = new MyPreparedStatement(con,
            "INSERT INTO UP_LAYOUT_PARAM " +
            "(USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) " +
            "VALUES ("+ userId + "," + layoutId + ",?,?,?)");
          try {
            int firstStructId = saveStructure(layoutXML.getFirstChild().getFirstChild(), structStmt, parmStmt);
            sSql = "UPDATE UP_USER_LAYOUT SET INIT_STRUCT_ID=" + firstStructId + " WHERE " + selectString;
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
            stmt.executeUpdate(sSql);

            if (firstLayout) {

              int defaultUserId;
              int defaultLayoutId;
              // Have to copy some of data over from the default user
              String sQuery = "SELECT USER_DFLT_USR_ID,USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
              rs = stmt.executeQuery(sQuery);
              try {
                rs.next();
                defaultUserId = rs.getInt(1);
                defaultLayoutId = rs.getInt(2);
              } finally {
                rs.close();
              }
              sQuery = "UPDATE UP_USER SET CURR_LAY_ID=" + layoutId + " WHERE USER_ID=" + userId;
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
              stmt.executeUpdate(sQuery);

              sQuery = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=1 WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
              stmt.executeQuery(sQuery);

              /**
               *  Let's hope that the default user data hasn't changed since we loaded the layout
               */

              /* insert row(s) into up_ss_user_atts */
              String Insert = "INSERT INTO UP_SS_USER_ATTS (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL) "+
                " SELECT "+userId+", PROFILE_ID, SS_ID, SS_TYPE, STRUCT_ID, PARAM_NAME, PARAM_TYPE, PARAM_VAL "+
                " FROM UP_SS_USER_ATTS WHERE USER_ID="+defaultUserId;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + Insert);
              stmt.executeUpdate(Insert);

              /* insert row(s) into up_ss_user_parm */
              Insert = "INSERT INTO UP_SS_USER_PARM (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL) "+
                " SELECT "+userId+", PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL "+
                " FROM UP_SS_USER_PARM WHERE USER_ID="+defaultUserId;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + Insert);
              stmt.executeUpdate(Insert);


            }
            long stopTime = System.currentTimeMillis();
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): Layout document for user " + userId + " took " +
                (stopTime - startTime) + " milliseconds to save");
          } finally {
            parmStmt.close();
          }
        } finally {
          structStmt.close();
        }
       } finally {
        stmt.close();
      }
      commit(con);
    } catch (Exception e) {
      rollback(con);
      throw  e;
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * convert true/false into Y/N for database
   * @param value to check
   * @result Y/N
   */
  protected static final String dbBool (String value) {
      return (value != null && value.equals("true") ? "Y" : "N");
  }

  /**
   * put your documentation comment here
   * @param nodeup
   * @param stmt
   * @param userId
   * @param layoutId
   * @param structId
   * @return
   * @exception java.sql.SQLException
   */
  protected final int saveStructure (Node node, MyPreparedStatement structStmt, MyPreparedStatement parmStmt) throws java.sql.SQLException {
    if (node == null || node.getNodeName().equals("parameter")) { // No more or parameter node
      return  0;
    }
    Element structure = (Element)node;
    int saveStructId = Integer.parseInt(structure.getAttribute("ID").substring(1));
    int nextStructId = 0;
    int childStructId = 0;
    String sQuery;
    if (DEBUG > 0) {
      LogService.instance().log(LogService.DEBUG, "-->" + node.getNodeName() + "@" + saveStructId);
    }
    if (node.hasChildNodes()) {
      childStructId = saveStructure(node.getFirstChild(), structStmt, parmStmt);
    }
    nextStructId = saveStructure(node.getNextSibling(), structStmt, parmStmt);
    structStmt.clearParameters();
    structStmt.setInt(1, saveStructId);
    structStmt.setInt(2, nextStructId);
    structStmt.setInt(3, childStructId);

    String externalId = structure.getAttribute("external_id");
    if (externalId != null && externalId.trim().length() > 0) {
      structStmt.setString(4, externalId.trim());
    } else {
      structStmt.setNull(4, java.sql.Types.VARCHAR);

    }
    if (node.getNodeName().equals("channel")) {
      int chanId = Integer.parseInt(node.getAttributes().getNamedItem("chanID").getNodeValue());
      structStmt.setInt(5, chanId);
      structStmt.setNull(6,java.sql.Types.VARCHAR);
    }
    else {
      structStmt.setNull(5,java.sql.Types.NUMERIC);
      structStmt.setString(6, sqlEscape(structure.getAttribute("name")));
    }
    String structType = structure.getAttribute("type");
    if (structType.length() > 0) {
      structStmt.setString(7, structType);
    } else {
      structStmt.setNull(7,java.sql.Types.VARCHAR);
    }
    structStmt.setString(8, dbBool(structure.getAttribute("hidden")));
    structStmt.setString(9, dbBool(structure.getAttribute("immutable")));
    structStmt.setString(10, dbBool(structure.getAttribute("unremovable")));
    LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::saveStructure(): " + structStmt);
    structStmt.executeUpdate();

    NodeList parameters = node.getChildNodes();
    if (parameters != null) {
      for (int i = 0; i < parameters.getLength(); i++) {
        if (parameters.item(i).getNodeName().equals("parameter")) {
          Element parmElement = (Element)parameters.item(i);
          NamedNodeMap nm = parmElement.getAttributes();
          String nodeName = nm.getNamedItem("name").getNodeValue();
          String nodeValue = nm.getNamedItem("value").getNodeValue();

          Node override = nm.getNamedItem("override");
          if (DEBUG > 0) {
            System.err.println(nodeName + "=" + nodeValue);
          }
          if (override == null || !override.getNodeValue().equals("yes")) {
            if (DEBUG > 0)
              System.err.println("Not saving channel defined parameter value " + nodeName);
          }
          else {
            parmStmt.clearParameters();
            parmStmt.setInt(1, saveStructId);
            parmStmt.setString(2, nodeName);
            parmStmt.setString(3, nodeValue);
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::saveStructure(): " + parmStmt);
            parmStmt.executeUpdate();
          }
        }
      }
    }
    return  saveStructId;
  }

  /**
   *
   *   UserPreferences
   *
   */
  /**
   *
   *   ChannelRegistry
   *
   */
  public void addChannel (int id, int publisherId, Document chanXML, String catID[]) throws SQLException {
    Connection con = rdbmService.getConnection();
    try {
      addChannel(id, publisherId, chanXML, con);
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // First delete existing categories for this channel
        String sDelete = "DELETE FROM UP_CAT_CHAN WHERE CHAN_ID=" + id;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sDelete);
        int recordsDeleted = stmt.executeUpdate(sDelete);

        for (int i = 0; i < catID.length; i++) {
          // Take out "cat" prefix if its there
          String categoryID = catID[i].startsWith("cat") ? catID[i].substring(3) : catID[i];

          String sInsert = "INSERT INTO UP_CAT_CHAN (CHAN_ID, CAT_ID) VALUES (" + id + "," + categoryID + ")";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sInsert);
          stmt.executeUpdate(sInsert);
        }
        // Commit the transaction
        commit(con);
      } catch (SQLException sqle) {
        // Roll back the transaction
        rollback(con);
        throw sqle;
      } finally {
        if (stmt != null)
          stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Publishes a channel.
   * @param id
   * @param publisherId
   * @param chanXML
   * @exception java.sql.SQLException
   */
  public void addChannel (int id, int publisherId, Document chanXML) throws SQLException {
    Connection con = rdbmService.getConnection();
    try {
      addChannel(id, publisherId, chanXML, con);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Removes a channel from the channel registry.  The channel
   * is not actually deleted.  Rather its status as an "approved"
   * channel is revoked.
   * @param chanID, the ID of the channel to delete
   * @exception java.sql.SQLException
   */
  public void removeChannel (String chanID) throws SQLException {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        chanID = chanID.startsWith("chan") ? chanID.substring(4) : chanID;

        // Delete channel/category associations
        //String sDelete = "DELETE FROM UP_CAT_CHAN WHERE CHAN_ID=" + chanID;
        //LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeChannel(): " + sDelete);
        //stmt.executeUpdate(sDelete);

        // Delete channel/role associations
        //sDelete = "DELETE FROM UP_ROLE_CHAN WHERE CHAN_ID=" + chanID;
        //LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeChannel(): " + sDelete);
        //stmt.executeUpdate(sDelete);

        // Delete channel.
        String sUpdate = "UPDATE UP_CHANNEL SET CHAN_APVL_DT=NULL WHERE CHAN_ID=" + chanID;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);

        // Commit the transaction
        commit(con);
      } catch (SQLException sqle) {
        // Roll back the transaction
        rollback(con);
        throw sqle;
      } finally {
        if (stmt != null)
          stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param sql
   * @return
   */
  protected static final String sqlEscape (String sql) {
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
   * put your documentation comment here
   * @param chanId
   * @param approverId
   * @exception Exception
   */
  public void approveChannel(int chanId, int approverId, java.sql.Timestamp approveDate) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sUpdate = "UPDATE UP_CHANNEL SET CHAN_APVL_ID = " + approverId + ", CHAN_APVL_DT = " +
        "{ts '" + approveDate.toString() + "'}" +
        " WHERE CHAN_ID = " + chanId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::approveChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Publishes a channel.
   * @param id
   * @param publisherId
   * @param doc
   * @param con
   * @exception Exception
   */
  protected void addChannel (int id, int publisherId, Document doc, Connection con) throws SQLException {
    Element channel = (Element)doc.getFirstChild();
    // Set autocommit false for the connection
    setAutoCommit(con, false);
    Statement stmt = con.createStatement();
    try {
      String sqlTitle = sqlEscape(channel.getAttribute("title"));
      String sqlDescription = sqlEscape(channel.getAttribute("description"));
      String sqlClass = channel.getAttribute("class");
      String sqlTypeID = channel.getAttribute("typeID");
      String sysdate = "{ts '" + new Timestamp(System.currentTimeMillis()).toString() + "'}";
      String sqlTimeout = channel.getAttribute("timeout");
      String sqlMinimizable = dbBool(channel.getAttribute("minimizable"));
      String sqlEditable = dbBool(channel.getAttribute("editable"));
      String sqlHasHelp = dbBool(channel.getAttribute("hasHelp"));
      String sqlHasAbout = dbBool(channel.getAttribute("hasAbout"));
      String sqlUnremovable = dbBool(channel.getAttribute("unremovable"));
      String sqlDetachable = dbBool(channel.getAttribute("detachable"));
      String sqlName = sqlEscape(channel.getAttribute("name"));
      String sqlFName = sqlEscape(channel.getAttribute("fname"));

      String sQuery = "SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_ID=" + id;
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);

      // If channel is already there, do an update, otherwise do an insert
      if (rs.next()) {
        String sUpdate = "UPDATE UP_CHANNEL SET " +
        "CHAN_TITLE='" + sqlTitle + "', " +
        "CHAN_DESC='" + sqlDescription + "', " +
        "CHAN_CLASS='" + sqlClass + "', " +
        "CHAN_TYPE_ID=" + sqlTypeID + ", " +
        "CHAN_PUBL_ID=" + publisherId + ", " +
        "CHAN_PUBL_DT=" + sysdate + ", " +
        "CHAN_APVL_ID=NULL, " +
        "CHAN_APVL_DT=NULL, " +
        "CHAN_TIMEOUT='" + sqlTimeout + "', " +
        "CHAN_MINIMIZABLE='" + sqlMinimizable + "', " +
        "CHAN_EDITABLE='" + sqlEditable + "', " +
        "CHAN_HAS_HELP='" + sqlHasHelp + "', " +
        "CHAN_HAS_ABOUT='" + sqlHasAbout + "', " +
        "CHAN_UNREMOVABLE='" + sqlUnremovable + "', " +
        "CHAN_DETACHABLE='" + sqlDetachable + "', " +
        "CHAN_NAME='" + sqlName + "', " +
        "CHAN_FNAME='" + sqlFName + "' " +
        "WHERE CHAN_ID=" + id;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
      } else {
        String sInsert = "INSERT INTO UP_CHANNEL (CHAN_ID, CHAN_TITLE, CHAN_DESC, CHAN_CLASS, CHAN_TYPE_ID, CHAN_PUBL_ID, CHAN_PUBL_DT,  CHAN_TIMEOUT, "
            + "CHAN_MINIMIZABLE, CHAN_EDITABLE, CHAN_HAS_HELP, CHAN_HAS_ABOUT, CHAN_UNREMOVABLE, CHAN_DETACHABLE, CHAN_NAME, CHAN_FNAME) ";
        sInsert += "VALUES (" + id + ", '" + sqlTitle + "', '" + sqlDescription + "', '" + sqlClass + "', " + sqlTypeID + ", "
            + publisherId + ", " + sysdate + ", '" + sqlTimeout + "', '" + sqlMinimizable
            + "', '" + sqlEditable + "', '" + sqlHasHelp
            + "', '" + sqlHasAbout + "', '" + sqlUnremovable
            + "', '" + sqlDetachable + "', '" + sqlName + "', '" + sqlFName + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sInsert);
        stmt.executeUpdate(sInsert);
      }

      // First delete existing parameters for this channel
      String sDelete = "DELETE FROM UP_CHANNEL_PARAM WHERE CHAN_ID=" + id;
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sDelete);
      int recordsDeleted = stmt.executeUpdate(sDelete);

      NodeList parameters = channel.getChildNodes();
      if (parameters != null) {
        for (int i = 0; i < parameters.getLength(); i++) {
          if (parameters.item(i).getNodeName().equals("parameter")) {
            Element parmElement = (Element)parameters.item(i);
            NamedNodeMap nm = parmElement.getAttributes();
            String paramName = null;
            String paramValue = null;
            String paramOverride = "NULL";
            for (int j = 0; j < nm.getLength(); j++) {
              Node param = nm.item(j);
              String nodeName = param.getNodeName();
              String nodeValue = param.getNodeValue();
              if (DEBUG > 1) {
                System.err.println(nodeName + "=" + nodeValue);
              }
              if (nodeName.equals("name")) {
                paramName = nodeValue;
              } else if (nodeName.equals("value")) {
                paramValue = nodeValue;
              } else if (nodeName.equals("override") && nodeValue.equals("yes")) {
                paramOverride = "'Y'";
              }
            }
            if (paramName == null && paramValue == null) {
              throw new RuntimeException("Invalid parameter node");
            }
            String sInsert = "INSERT INTO UP_CHANNEL_PARAM (CHAN_ID, CHAN_PARM_NM, CHAN_PARM_VAL, CHAN_PARM_OVRD) VALUES (" + id +
                ",'" + paramName + "','" + paramValue + "'," + paramOverride + ")";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): " + sInsert);
            stmt.executeUpdate(sInsert);
          }
        }
      }
      // Commit the transaction
      commit(con);
      synchronized (channelLock) {
        if (channelCache.remove(new Integer(id)) != null) {
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addChannel(): flushed channel "
            + id + " from cache");
        }
      }
    } catch (SQLException sqle) {
      rollback(con);
      throw  sqle;
    } finally {
      stmt.close();
    }
  }

  public Document getChannelRegistryXML () throws SQLException {
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    Element registry = doc.createElement("registry");
    doc.appendChild(registry);
    Connection con = rdbmService.getConnection();
    try {
      MyPreparedStatement chanStmt = new MyPreparedStatement(con, "SELECT CHAN_ID FROM UP_CAT_CHAN WHERE CAT_ID=?");
      try {
        Statement stmt = con.createStatement();
        try {
          String query = "SELECT CAT_ID, CAT_TITLE, CAT_DESC FROM UP_CATEGORY WHERE PARENT_CAT_ID IS NULL ORDER BY CAT_TITLE";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getChannelRegistryXML(): " + query);
          ResultSet rs = stmt.executeQuery(query);
          try {
            while (rs.next()) {
              int catId = rs.getInt(1);
              String catTitle = rs.getString(2);
              String catDesc = rs.getString(3);

              // Top level <category>
              Element category = doc.createElement("category");
              category.setAttribute("ID", "cat" + catId);
              category.setAttribute("name", catTitle);
              category.setAttribute("description", catDesc);
              ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(category.getAttribute("ID"), category);
              registry.appendChild(category);

              // Add child categories and channels
              appendChildCategoriesAndChannels(con, chanStmt, category, catId);
            }
          } finally {
            rs.close();
          }
        } finally {
          stmt.close();
        }
      } finally {
        chanStmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return doc;
  }

  protected void appendChildCategoriesAndChannels (Connection con, MyPreparedStatement chanStmt, Element category, int catId) throws SQLException {
    Document doc = category.getOwnerDocument();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      String query = "SELECT CAT_ID, CAT_TITLE, CAT_DESC FROM UP_CATEGORY WHERE PARENT_CAT_ID=" + catId;
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::appendChildCategoriesAndChannels(): " + query);
      rs = stmt.executeQuery(query);
      while (rs.next()) {
        int childCatId = rs.getInt(1);
        String childCatTitle = rs.getString(2);
        String childCatDesc = rs.getString(3);

        // Child <category>
        Element childCategory = doc.createElement("category");
        childCategory.setAttribute("ID", "cat" + childCatId);
        childCategory.setAttribute("name", childCatTitle);
        childCategory.setAttribute("description", childCatDesc);
        ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(childCategory.getAttribute("ID"), childCategory);
        category.appendChild(childCategory);

        // Append child categories and channels recursively
        appendChildCategoriesAndChannels(con, chanStmt, childCategory, childCatId);
      }

      // Append children channels
      chanStmt.clearParameters();
      chanStmt.setInt(1, catId);
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::appendChildCategoriesAndChannels(): " + chanStmt);
      rs = chanStmt.executeQuery();
      try {
        while (rs.next()) {
          int chanId = rs.getInt(1);
          Element channel = getChannelNode (chanId, con, (org.apache.xerces.dom.DocumentImpl)doc, "chan" + chanId);
          if (channel == null) {
            LogService.instance().log(LogService.WARN, "RDBMUserLayoutStore::appendChildCategoriesAndChannels(): channel " + chanId +
              " in category " + catId + " does not exist in the store");
          } else {
            category.appendChild(channel);
          }
        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }
  }

  /**
   * Get channel types xml.
   * It will look something like this:
   * <p><code>
   *
   *<channelTypes>
   *  <channelType ID="0">
   *    <class>org.jasig.portal.channels.CImage</class>
   *    <name>Image</name>
   *    <description>Simple channel to display an image with optional
   *        caption and subcaption</description>
   *    <cpd-uri>webpages/media/org/jasig/portal/channels/CImage/CImage.cpd</cpd-uri>
   *  </channelType>
   *  <channelType ID="1">
   *    <class>org.jasig.portal.channels.CWebProxy</class>
   *    <name>Web Proxy</name>
   *    <description>Incorporate a dynamic HTML or XML application</description>
   *    <cpd-uri>webpages/media/org/jasig/portal/channels/CWebProxy/CWebProxy.cpd</cpd-uri>
   *  </channelType>
   *</channelTypes>
   *
   * </code></p>
   * @return types, the channel types as a Document
   * @throws java.sql.SQLException
   */
  public Document getChannelTypesXML () throws SQLException {
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    Element root = doc.createElement("channelTypes");
    doc.appendChild(root);
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT TYPE_ID, TYPE, TYPE_NAME, TYPE_DESCR, TYPE_DEF_URI FROM UP_CHAN_TYPE";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getChannelTypesXML(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int ID = rs.getInt(1);
            String javaClass = rs.getString(2);
            String name = rs.getString(3);
            String descr = rs.getString(4);
            String cpdUri = rs.getString(5);

            // <channelType>
            Element channelType = doc.createElement("channelType");
            channelType.setAttribute("ID", String.valueOf(ID));

            Element elem = null;

            // <class>
            elem = doc.createElement("class");
            elem.appendChild(doc.createTextNode(javaClass));
            channelType.appendChild(elem);

            // <name>
            elem = doc.createElement("name");
            elem.appendChild(doc.createTextNode(name));
            channelType.appendChild(elem);

            // <description>
            elem = doc.createElement("description");
            elem.appendChild(doc.createTextNode(descr));
            channelType.appendChild(elem);

            // <cpd-uri>
            elem = doc.createElement("cpd-uri");
            elem.appendChild(doc.createTextNode(cpdUri));
            channelType.appendChild(elem);

            root.appendChild(channelType);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return doc;
  }

  /** Returns a string of XML which describes the channel categories.
   * @param role role of the current user
   */
  public void getCategoryXML (Document catsDoc, Element root, String role) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT UC.CAT_ID, UC.CAT_TITLE " + "FROM UP_CATEGORY UC ";
        if (role != null && !role.equals("")) {
          sQuery += ", UP_CAT_CHAN, UCC, UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR" + " WHERE UR.ROLE_TITLE='" + role +
              "' AND URC.ROLE_ID = UR.ROLE_ID AND URC.CHAN_ID = UC.CHAN_ID" + " AND UC.CHAN_ID = UCC.CHAN_ID AND UCC.CAT_ID = UC.CAT_ID";
        }
        sQuery += " ORDER BY UC.CAT_TITLE";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getCategoryXML(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          Element cat = null;
          while (rs.next()) {
            String catnm = rs.getString(2);
            String id = rs.getString(1);
            cat = catsDoc.createElement("category");
            cat.setAttribute("ID", id);
            cat.setAttribute("name", catnm);
            root.appendChild(cat);
          }
          catsDoc.appendChild(root);
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Get the next structure Id
   * @parameter userId
   * @result next free structure ID
   */
  public String getNextStructChannelId (IPerson person) throws Exception {
    return  getNextStructId(person, channelPrefix);
  }

  /**
   * put your documentation comment here
   * @param person
   * @return
   * @exception Exception
   */
  public String getNextStructFolderId (IPerson person) throws Exception {
    return  getNextStructId(person, folderPrefix);
  }

  /**
   * Return the Structure ID tag
   * @param  structId
   * @param  chanId
   * @return ID tag
   */
  protected String getStructId(int structId, int chanId) {
    if (chanId == 0) {
      return folderPrefix + structId;
    } else {
      return channelPrefix + structId;
    }
  }
  /**
   * put your documentation comment here
   * @param person
   * @param prefix
   * @return
   * @exception Exception
   */
  protected String getNextStructId (IPerson person, String prefix) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getNextStructId(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          rs.next();
          int currentStructId = rs.getInt(1);
          int nextStructId = currentStructId + 1;
          sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + userId + " AND NEXT_STRUCT_ID="
              + currentStructId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getNextStructId(): " + sQuery);
          stmt.executeUpdate(sQuery);
          return  prefix + nextStructId;
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   *   ReferenceAuthorization
   *
   */
  /**
   * Is a user in this role
   */
  public boolean isUserInRole (IPerson person, String role) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      String query = "SELECT * FROM UP_USER_ROLE UUR, UP_ROLE UR, UP_USER UU " + "WHERE UU.USER_ID=" + userId + " UUR.USER_ID=UU.USER_ID AND UUR.ROLE_ID=UR.ROLE_ID "
          + "AND " + "UPPER(ROLE_TITLE)=UPPER('" + role + "')";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::isUserInRole(): " + query);
      Statement stmt = con.createStatement();
      try {
        ResultSet rs = stmt.executeQuery(query);
        try {
          if (rs.next()) {
            return  (true);
          }
          else {
            return  (false);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @return
   * @exception java.sql.SQLException
   */
  public Vector getAllRoles () throws SQLException {
    Vector roles = new Vector();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT ROLE_TITLE, ROLE_DESC FROM UP_ROLE";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getAllRolessQuery(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          IRole role = null;
          // Add all of the roles in the portal database to to the vector
          while (rs.next()) {
            role = new org.jasig.portal.security.provider.RoleImpl(rs.getString(1));
            role.setAttribute("description", rs.getString(2));
            roles.add(role);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  (roles);
  }

  /**
   * put your documentation comment here
   * @param channelID
   * @param roles
   * @return
   * @exception Exception
   */
  public int setChannelRoles (int channelID, Vector roles) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      int recordsInserted = 0;
      Statement stmt = con.createStatement();
      try {
        // First delete existing roles for this channel
        String sDelete = "DELETE FROM UP_ROLE_CHAN WHERE CHAN_ID=" + channelID;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setChannelRoles(): " + sDelete);
        int recordsDeleted = stmt.executeUpdate(sDelete);
        // Count the number of records inserted
        for (int i = 0; i < roles.size(); i++) {
          String sQuery = "SELECT ROLE_ID FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setChannelRoles(): " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            int roleId = rs.getInt("ROLE_ID");
            String sInsert = "INSERT INTO UP_ROLE_CHAN (CHAN_ID, ROLE_ID) VALUES (" + channelID + "," + roleId + ")";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setChannelRoles(): " + sInsert);
            recordsInserted += stmt.executeUpdate(sInsert);
          } finally {
            rs.close();
          }
        }
      } finally {
        stmt.close();
      }
      // Commit the transaction
      commit(con);
      return  (recordsInserted);
    } catch (Exception e) {
      // Roll back the transaction
      rollback(con);
      throw  e;
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Get the roles that a channel belongs to
   * @param channelRoles
   * @param channelID
   * @exception java.sql.SQLException
   */
  public void getChannelRoles (Vector channelRoles, int channelID) throws SQLException {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT UR.ROLE_TITLE, UC.CHAN_ID FROM UP_ROLE_CHAN URC, UP_ROLE UR, UP_CHANNEL UC " + "WHERE UC.CHAN_ID="
            + channelID + " AND UC.CHAN_ID=URC.CHAN_ID AND URC.ROLE_ID=UR.ROLE_ID";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getChannelRoles(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        try {
          while (rs.next()) {
            channelRoles.addElement(rs.getString("ROLE_TITLE"));
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Get the roles that a user belongs to
   * @param userRoles
   * @param person
   * @exception Exception
   */
  public void getUserRoles (Vector userRoles, IPerson person) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT ROLE_TITLE, USER_ID FROM UP_USER_ROLE UUR, UP_ROLE UR, UP_USER UU " + "WHERE UU.USER_ID="
            + userId + " AND UU.USER_ID=UUR.USER_ID AND UUR.ROLE_ID=UR.ROLE_ID";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserRoles(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        try {
          while (rs.next()) {
            userRoles.addElement(rs.getString("ROLE_TITLE"));
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param roles
   * @exception Exception
   */
  public void addUserRoles (IPerson person, Vector roles) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int insertCount = 0;
        for (int i = 0; i < roles.size(); i++) {
          String query = "SELECT ROLE_ID, ROLE_TITLE FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addUserRoles(): " + query);
          ResultSet rs = stmt.executeQuery(query);
          try {
            rs.next();
            int roleId = rs.getInt("ROLE_ID");
            String insert = "INSERT INTO UP_USER_ROLE (USER_ID, ROLE_ID) VALUES (" + userId + ", " + roleId + ")";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addUserRoles(): " + insert);
            insertCount = stmt.executeUpdate(insert);
            if (insertCount != 1) {
              LogService.instance().log(LogService.ERROR, "AuthorizationBean addUserRoles(): SQL failed -> " + insert);
            }
          } finally {
            rs.close();
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param roles
   * @exception Exception
   */
  public void removeUserRoles (IPerson person, Vector roles) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int deleteCount = 0;
        for (int i = 0; i < roles.size(); i++) {
          String delete = "DELETE FROM UP_USER_ROLE WHERE USER_ID=" + userId + " AND ROLE_ID=" + roles.elementAt(i);
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeUserRoles(): " + delete);
          deleteCount = stmt.executeUpdate(delete);
          if (deleteCount != 1) {
            LogService.instance().log(LogService.ERROR, "AuthorizationBean removeUserRoles(): SQL failed -> " + delete);
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   * Authorization
   *
   */
  public String[] getUserAccountInformation (String username) throws Exception {
    String[] acct = new String[] {
      null, null, null, null
    };
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT  ENCRPTD_PSWD, FIRST_NAME, LAST_NAME, EMAIL FROM UP_PERSON_DIR WHERE "
            + "USER_NAME = '" + username + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserAccountInformation(): " + query);
        ResultSet rset = stmt.executeQuery(query);
        try {
          if (rset.next()) {
            acct[0] = rset.getString("ENCRPTD_PSWD");
            acct[1] = rset.getString("FIRST_NAME");
            acct[2] = rset.getString("LAST_NAME");
          }
        } finally {
          rset.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  acct;
  }

  /**
   *
   * Example Directory Information
   * Normally directory information would come from a real directory server using
   * for example, LDAP.  The reference inplementation uses the database for
   * directory information.
   */
  public String[] getUserDirectoryInformation (String username) throws Exception {
    String[] acct = new String[] {
      null, null, null
    };
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT FIRST_NAME, LAST_NAME, EMAIL FROM UP_USER, UP_PERSON_DIR " + "WHERE UP_USER.USER_ID = UP_PERSON_DIR.USER_ID AND "
            + "UP_USER.USER_NAME = '" + username + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserDirectoryInformation(): " + query);
        ResultSet rset = stmt.executeQuery(query);
        try {
          if (rset.next()) {
            acct[0] = rset.getString("FIRST_NAME");
            acct[1] = rset.getString("LAST_NAME");
            acct[2] = rset.getString("EMAIL");
          }
        } finally {
          rset.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  acct;
  }

  /* DBCounter */
  /*
   * get&increment method.
   */
  public synchronized int getIncrementIntegerId (String tableName) throws Exception {
    int id;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME='" + tableName + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getIncrementInteger(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          rs.next();
          id = rs.getInt("SEQUENCE_VALUE") + 1;
        } finally {
          rs.close();
        }
        String sInsert = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE=" + id + " WHERE SEQUENCE_NAME='" + tableName + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getIncrementInteger(): " + sInsert);
        stmt.executeUpdate(sInsert);
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
  public synchronized void createCounter (String tableName) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sInsert = "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME,SEQUENCE_VALUE/*/) VALUES ('" + tableName + "',0)";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::createCounter(): " + sInsert);
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
        String sUpdate = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE=" + value + "WHERE SEQUENCE_NAME='" + tableName + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setCounter(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param connection
   * @param autocommit
   */
  static final protected void setAutoCommit (Connection connection, boolean autocommit) {
    try {
      if (supportsTransactions)
        connection.setAutoCommit(autocommit);
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param connection
   */
  static final protected void commit (Connection connection) {
    try {
      if (supportsTransactions)
        connection.commit();
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param connection
   */
  static final protected void rollback (Connection connection) {
    try {
      if (supportsTransactions)
        connection.rollback();
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /**
   *   UserPreferences
   */
  public int getUserBrowserMapping (IPerson person, String userAgent) throws Exception {
    int userId = person.getID();
    int profileId = 0;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT PROFILE_ID, USER_ID FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND USER_AGENT='" +
            userAgent + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserBrowserMapping(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            profileId = rs.getInt("PROFILE_ID");
          }
          else {
            return  0;
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  profileId;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param userAgent
   * @param profileId
   * @exception Exception
   */
  public void setUserBrowserMapping (IPerson person, String userAgent, int profileId) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      // remove the old mapping and add the new one
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID='" + userId + "' AND USER_AGENT='" + userAgent + "'";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserBrowserMapping(): " + sQuery);
        stmt.executeUpdate(sQuery);
        sQuery = "INSERT INTO UP_USER_UA_MAP (USER_ID,USER_AGENT,PROFILE_ID) VALUES (" + userId + ",'" + userAgent + "',"
            + profileId + ")";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserBrowserMapping(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @return
   * @exception Exception
   */
  public UserProfile getUserProfileById (IPerson person, int profileId) throws Exception {
    int userId = person.getID();
    UserProfile upl = null;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId + " AND PROFILE_ID=" + profileId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserProfileId(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            upl = new UserProfile(profileId, rs.getString("PROFILE_NAME"), rs.getString("DESCRIPTION"), rs.getInt("LAYOUT_ID"),
                rs.getInt("STRUCTURE_SS_ID"), rs.getInt("THEME_SS_ID"));
          }
          else {
            return  null;
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  upl;
  }

  /**
   * put your documentation comment here
   * @param person
   * @return
   * @exception Exception
   */
  public Hashtable getUserProfileList (IPerson person) throws Exception {
    int userId = person.getID();

    Hashtable pv = new Hashtable();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserProfileList(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            UserProfile upl = new UserProfile(rs.getInt("PROFILE_ID"), rs.getString("PROFILE_NAME"), rs.getString("DESCRIPTION"),
                rs.getInt("LAYOUT_ID"), rs.getInt("STRUCTURE_SS_ID"), rs.getInt("THEME_SS_ID"));
            pv.put(new Integer(upl.getProfileId()), upl);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  pv;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profile
   * @exception Exception
   */
  public void setUserProfile (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // this is ugly, but we have to know wether to do INSERT or UPDATE
        String sQuery = "SELECT USER_ID, PROFILE_NAME FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID="
            + profile.getProfileId();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserProfile() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            sQuery = "UPDATE UP_USER_PROFILE SET THEME_SS_ID=" + profile.getThemeStylesheetId() + ", STRUCTURE_SS_ID=" +
                profile.getStructureStylesheetId() + ", DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
                + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
          }
          else {
            sQuery = "INSERT INTO UP_USER_PROFILE (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION) VALUES ("
                + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
                + "," + profile.getThemeStylesheetId() + ",'" + profile.getProfileDescription() + "')";
          }
        } finally {
          rs.close();
        }
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setUserProfile(): " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param stylesheetId
   * @return
   * @exception Exception
   */
  public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
    int userId = person.getID();
    ThemeStylesheetUserPreferences tsup;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // get stylesheet description
        ThemeStylesheetDescription tsd = getThemeStylesheetDescription(stylesheetId);
        // get user defined defaults
        String sQuery = "SELECT PARAM_NAME, PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID="
            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            // stylesheet param
            tsd.setStylesheetParameterDefaultValue(rs.getString(1), rs.getString(2));
            //			LogService.instance().log(LogService.DEBUG,"RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  read stylesheet param "+rs.getString("PARAM_NAME")+"=\""+rs.getString("PARAM_VAL")+"\"");
          }
        } finally {
          rs.close();
        }
        tsup = new ThemeStylesheetUserPreferences();
        tsup.setStylesheetId(stylesheetId);
        // fill stylesheet description with defaults
        for (Enumeration e = tsd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          tsup.putParameterValue(pName, tsd.getStylesheetParameterDefaultValue(pName));
        }
        for (Enumeration e = tsd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          tsup.addChannelAttribute(pName, tsd.getChannelAttributeDefaultValue(pName));
        }
        // get user preferences
        sQuery = "SELECT PARAM_TYPE, PARAM_NAME, PARAM_VAL, ULS.STRUCT_ID, CHAN_ID FROM UP_SS_USER_ATTS UUSA, UP_LAYOUT_STRUCT ULS WHERE UUSA.USER_ID=" + userId + " AND PROFILE_ID="
            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND UUSA.STRUCT_ID = ULS.STRUCT_ID AND UUSA.USER_ID = ULS.USER_ID";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int param_type = rs.getInt(1);
            if (param_type == 1) {
              // stylesheet param
              LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_SS_USER_ATTS is corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(2) + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 2) {
              // folder attribute
              LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  folder attribute specified for the theme stylesheet! UP_SS_USER_ATTS corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(2) + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 3) {
              // channel attribute
              tsup.setChannelAttributeValue(getStructId(rs.getInt(4),rs.getInt(5)), rs.getString(2), rs.getString(3));
              //LogService.instance().log(LogService.DEBUG,"RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  read folder attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("STRUCT_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
            }
            else {
              // unknown param type
              LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(2) + "\", param_type=" + Integer.toString(param_type));
            }
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  tsup;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param stylesheetId
   * @return
   * @exception Exception
   */
  public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
    int userId = person.getID();
    StructureStylesheetUserPreferences ssup;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // get stylesheet description
        StructureStylesheetDescription ssd = getStructureStylesheetDescription(stylesheetId);
        // get user defined defaults
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" +
            profileId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + subSelectString);
        int layoutId;
        ResultSet rs = stmt.executeQuery(subSelectString);
        try {
          rs.next();
          layoutId = rs.getInt(1);
        } finally {
          rs.close();
        }

        if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            userId = rs.getInt(1);
          } finally {
            rs.close();
          }
        }

        String sQuery = "SELECT PARAM_NAME, PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID="
            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            // stylesheet param
            ssd.setStylesheetParameterDefaultValue(rs.getString(1), rs.getString(2));
            //LogService.instance().log(LogService.DEBUG,"RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  read stylesheet param "+rs.getString("PARAM_NAME")+"=\""+rs.getString("PARAM_VAL")+"\"");
          }
        } finally {
          rs.close();
        }
        ssup = new StructureStylesheetUserPreferences();
        ssup.setStylesheetId(stylesheetId);
        // fill stylesheet description with defaults
        for (Enumeration e = ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          ssup.putParameterValue(pName, ssd.getStylesheetParameterDefaultValue(pName));
        }
        for (Enumeration e = ssd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          ssup.addChannelAttribute(pName, ssd.getChannelAttributeDefaultValue(pName));
        }
        for (Enumeration e = ssd.getFolderAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          ssup.addFolderAttribute(pName, ssd.getFolderAttributeDefaultValue(pName));
        }
        // get user preferences
        sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE, ULS.STRUCT_ID, CHAN_ID FROM UP_SS_USER_ATTS UUSA, UP_LAYOUT_STRUCT ULS WHERE UUSA.USER_ID=" + userId + " AND PROFILE_ID="
            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND UUSA.STRUCT_ID = ULS.STRUCT_ID AND UUSA.USER_ID = ULS.USER_ID";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int param_type = rs.getInt(3);
            if (param_type == 1) {
              // stylesheet param
              LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_SS_USER_ATTS is corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(1) + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 2) {
              // folder attribute
              ssup.setFolderAttributeValue(getStructId(rs.getInt(4),rs.getInt(5)), rs.getString(1), rs.getString(2));
              //LogService.instance().log(LogService.DEBUG,"RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  read folder attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("STRUCT_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
            }
            else if (param_type == 3) {
              // channel attribute
              ssup.setChannelAttributeValue(getStructId(rs.getInt(4),rs.getInt(5)), rs.getString(1), rs.getString(2));
              //LogService.instance().log(LogService.DEBUG,"RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  read channel attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("STRUCT_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
            }
            else {
              // unknown param type
              LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(1) + "\", param_type=" + Integer.toString(param_type));
            }
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  ssup;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param ssup
   * @exception Exception
   */
  public void setStructureStylesheetUserPreferences (IPerson person, int profileId, StructureStylesheetUserPreferences ssup) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      int stylesheetId = ssup.getStylesheetId();
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // write out params
        for (Enumeration e = ssup.getParameterValues().keys(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          // see if the parameter was already there
          String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
              + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_NAME='" + pName + "'";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          if (rs.next()) {
            // update
            sQuery = "UPDATE UP_SS_USER_PARM SET PARAM_VAL='" + ssup.getParameterValue(pName) + "' WHERE USER_ID=" + userId
                + " AND PROFILE_ID=" + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_NAME='" + pName
                + "'";
          }
          else {
            // insert
            sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (" + userId
                + "," + profileId + "," + stylesheetId + ",1,'" + pName + "','" + ssup.getParameterValue(pName) + "')";
          }
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // write out folder attributes
        for (Enumeration e = ssup.getFolders(); e.hasMoreElements();) {
          String folderId = (String)e.nextElement();
          for (Enumeration attre = ssup.getFolderAttributeNames(); attre.hasMoreElements();) {
            String pName = (String)attre.nextElement();
            String pValue = ssup.getDefinedFolderAttributeValue(folderId, pName);
            if (pValue != null) {
              // store user preferences
              String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID='" + folderId.substring(1) + "' AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=2";
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              ResultSet rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID='" + folderId.substring(1) + "' AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=2";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",1,'" + folderId.substring(1) + "','" + pName + "',2,'" + pValue
                    + "')";
              }
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              stmt.executeUpdate(sQuery);
            }
          }
        }
        // write out channel attributes
        for (Enumeration e = ssup.getChannels(); e.hasMoreElements();) {
          String channelId = (String)e.nextElement();
          for (Enumeration attre = ssup.getChannelAttributeNames(); attre.hasMoreElements();) {
            String pName = (String)attre.nextElement();
            String pValue = ssup.getDefinedChannelAttributeValue(channelId, pName);
            if (pValue != null) {
              // store user preferences
              String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID='" + channelId.substring(1) + "' AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=3";
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              ResultSet rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID='" + channelId.substring(1) + "' AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=3";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",1,'" + channelId.substring(1) + "','" + pName + "',3,'" + pValue
                    + "')";
              }
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              stmt.executeUpdate(sQuery);
            }
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @param tsup
   * @exception Exception
   */
  public void setThemeStylesheetUserPreferences (IPerson person, int profileId, ThemeStylesheetUserPreferences tsup) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      int stylesheetId = tsup.getStylesheetId();
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // write out params
        for (Enumeration e = tsup.getParameterValues().keys(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          // see if the parameter was already there
          String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
              + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_NAME='" + pName + "'";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          if (rs.next()) {
            // update
            sQuery = "UPDATE UP_SS_USER_PARM SET PARAM_VAL='" + tsup.getParameterValue(pName) + "' WHERE USER_ID=" + userId
                + " AND PROFILE_ID=" + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_NAME='" + pName
                + "'";
          }
          else {
            // insert
            sQuery = "INSERT INTO UP_SS_USER_PARM (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES (" + userId
                + "," + profileId + "," + stylesheetId + ",2,'" + pName + "','" + tsup.getParameterValue(pName) + "')";
          }
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // write out channel attributes
        for (Enumeration e = tsup.getChannels(); e.hasMoreElements();) {
          String channelId = (String)e.nextElement();
          for (Enumeration attre = tsup.getChannelAttributeNames(); attre.hasMoreElements();) {
            String pName = (String)attre.nextElement();
            String pValue = tsup.getDefinedChannelAttributeValue(channelId, pName);
            if (pValue != null) {
              // store user preferences
              String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND STRUCT_ID='" + channelId.substring(1) + "' AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=3";
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
              ResultSet rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND STRUCT_ID='" + channelId.substring(1) + "' AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=3";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",2,'" + channelId.substring(1) + "','" + pName + "',3,'" + pValue
                    + "')";
              }
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
              stmt.executeUpdate(sQuery);
            }
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profile
   * @exception Exception
   */
  public void updateUserProfile (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "UPDATE UP_USER_PROFILE SET THEME_SS_ID=" + profile.getThemeStylesheetId() + ", STRUCTURE_SS_ID="
            + profile.getStructureStylesheetId() + ", DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
            + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profile
   * @return
   * @exception Exception
   */
  public UserProfile addUserProfile (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    // generate an id for this profile
    Connection con = rdbmService.getConnection();
    try {
      int id = getIncrementIntegerId("UP_USER_PROFILE");
      profile.setProfileId(id);
      Statement stmt = con.createStatement();
      try {
        String sQuery = "INSERT INTO UP_USER_PROFILE (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION) VALUES ("
            + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
            + "," + profile.getThemeStylesheetId() + ",'" + profile.getProfileDescription() + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addUserProfile(): " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  profile;
  }

  /**
   * put your documentation comment here
   * @param person
   * @param profileId
   * @exception Exception
   */
  public void deleteUserProfile (IPerson person, int profileId) throws Exception {
    int userId = person.getID();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   * CoreStyleSheet
   *
   */
  public void getMimeTypeList (Hashtable list) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT A.MIME_TYPE, A.MIME_TYPE_DESCRIPTION FROM UP_MIME_TYPE A, UP_SS_MAP B WHERE B.MIME_TYPE=A.MIME_TYPE";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getMimeTypeList() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            list.put(rs.getString("MIME_TYPE"), rs.getString("MIME_TYPE_DESCRIPTION"));
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Obtain a list of structure stylesheet descriptions that have stylesheets for a given
   * mime type.
   * @param mimeType
   * @return a mapping from stylesheet names to structure stylesheet description objects
   */
  public Hashtable getStructureStylesheetList (String mimeType) throws Exception {
    Connection con = rdbmService.getConnection();
    Hashtable list = new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT A.SS_ID FROM UP_SS_STRUCT A, UP_SS_THEME B WHERE B.MIME_TYPE='" + mimeType + "' AND B.STRUCT_SS_ID=A.SS_ID";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetList() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            StructureStylesheetDescription ssd = getStructureStylesheetDescription(rs.getInt("SS_ID"));
            if (ssd != null)
              list.put(new Integer(ssd.getId()), ssd);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  list;
  }

  /**
   * Obtain a list of theme stylesheet descriptions for a given structure stylesheet
   * @param structureStylesheetName
   * @return a map of stylesheet names to  theme stylesheet description objects
   * @exception Exception
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId) throws Exception {
    Connection con = rdbmService.getConnection();
    Hashtable list = new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE STRUCT_SS_ID=" + structureStylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetList() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            ThemeStylesheetDescription tsd = getThemeStylesheetDescription(rs.getInt("SS_ID"));
            if (tsd != null)
              list.put(new Integer(tsd.getId()), tsd);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  list;
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @exception Exception
   */
  public void removeStructureStylesheetDescription (int stylesheetId) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // detele all associated theme stylesheets
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE STRUCT_SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            removeThemeStylesheetDescription(rs.getInt("SS_ID"));
          }
        } finally {
          rs.close();
        }
        sQuery = "DELETE FROM UP_SS_STRUCT WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // delete params
        sQuery = "DELETE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // clean up user preferences
        // should we do something about profiles ?
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @exception Exception
   */
  public void removeThemeStylesheetDescription (int stylesheetId) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_SS_THEME WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // delete params
        sQuery = "DELETE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // clean up user preferences
        sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // nuke the profiles as well ?
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param person
   * @param doc
   * @exception Exception
   */
  public void saveBookmarkXML (IPerson person, Document doc) throws Exception {
    int userId = person.getID();
    StringWriter outString = new StringWriter();
    XMLSerializer xsl = new XMLSerializer(outString, new OutputFormat(doc));
    xsl.serialize(doc);
    Connection con = rdbmService.getConnection();
    try {
      Statement statem = con.createStatement();
      try {
        String sQuery = "UPDATE UPC_BOOKMARKS SET BOOKMARK_XML = '" + outString.toString() + "' WHERE PORTAL_USER_ID = "
            + userId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::saveBookmarkXML(): " + sQuery);
        statem.executeUpdate(sQuery);
      } finally {
        statem.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Obtain ID for known structure stylesheet name
   * @param ssName name of the structure stylesheet
   * @return id or null if no stylesheet matches the name given.
   */
  public Integer getStructureStylesheetId (String ssName) throws Exception {
    Integer id = null;
    Connection con = rdbmService.getConnection();
    try {
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SS_ID FROM UP_SS_STRUCT WHERE SS_NAME='" + ssName + "'";
        ResultSet rs = stmt.executeQuery(sQuery);
        if (rs.next()) {
          id = new Integer(rs.getInt("SS_ID"));
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
   * Obtain ID for known theme stylesheet name
   * @param ssName name of the theme stylesheet
   * @return id or null if no theme matches the name given.
   */
  public Integer getThemeStylesheetId (String tsName) throws Exception {
    Integer id = null;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE SS_NAME='" + tsName + "'";
        ResultSet rs = stmt.executeQuery(sQuery);
        if (rs.next()) {
          id = new Integer(rs.getInt("SS_ID"));
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
   * Remove (with cleanup) a theme stylesheet param
   * @param stylesheetId id of the theme stylesheet
   * @param pName name of the parameter
   * @param con active database connection
   */
  private void removeThemeStylesheetParam (int stylesheetId, String pName, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "DELETE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId + " AND TYPE=1 AND PARAM_NAME='" + pName
          + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_TYPE=1 AND PARAM_NAME='"
          + pName + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  /**
   * Remove (with cleanup) a theme stylesheet channel attribute
   * @param stylesheetId id of the theme stylesheet
   * @param pName name of the attribute
   * @param con active database connection
   */
  private void removeThemeChannelAttribute (int stylesheetId, String pName, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "DELETE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId + " AND TYPE=3 AND PARAM_NAME='" + pName
          + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeChannelAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_TYPE=3 AND PARAM_NAME='"
          + pName + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  /**
   * Updates an existing structure stylesheet description with a new one. Old stylesheet
   * description is found based on the Id provided in the parameter structure.
   * @param ssd new stylesheet description
   */
  public void updateThemeStylesheetDescription (ThemeStylesheetDescription tsd) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int stylesheetId = tsd.getId();
        String sQuery = "UPDATE UP_SS_THEME SET SS_NAME='" + tsd.getStylesheetName() + "',SS_URI='" + tsd.getStylesheetURI()
            + "',SS_DESCRIPTION_URI='" + tsd.getStylesheetDescriptionURI() + "',SS_DESCRIPTION_TEXT='" + tsd.getStylesheetWordDescription()
            + "' WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // first, see what was there before
        HashSet oparams = new HashSet();
        HashSet ocattrs = new HashSet();
        sQuery = "SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
        Statement stmtOld = con.createStatement();
        ResultSet rsOld = stmtOld.executeQuery(sQuery);
        try {
          while (rsOld.next()) {
            int type = rsOld.getInt("TYPE");
            if (type == 1) {
              // stylesheet param
              String pName = rsOld.getString("PARAM_NAME");
              oparams.add(pName);
              if (!tsd.containsParameterName(pName)) {
                // delete param
                removeThemeStylesheetParam(stylesheetId, pName, con);
              }
              else {
                // update param
                sQuery = "UPDATE UP_SS_THEME_PARM SET PARAM_DEFAULT_VAL='" + tsd.getStylesheetParameterDefaultValue(pName)
                    + "',PARAM_DESCRIPT='" + tsd.getStylesheetParameterWordDescription(pName) + "' WHERE SS_ID=" + stylesheetId
                    + " AND PARAM_NAME='" + pName + "' AND TYPE=1";
                LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else if (type == 2) {
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! DB is corrupt. (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + rsOld.getInt("TYPE") +
                  ").");
            }
            else if (type == 3) {
              // channel attribute
              String pName = rsOld.getString("PARAM_NAME");
              ocattrs.add(pName);
              if (!tsd.containsChannelAttribute(pName)) {
                // delete channel attribute
                removeThemeChannelAttribute(stylesheetId, pName, con);
              }
              else {
                // update channel attribute
                sQuery = "UPDATE UP_SS_THEME_PARM SET PARAM_DEFAULT_VAL='" + tsd.getChannelAttributeDefaultValue(pName) +
                    "',PARAM_DESCRIPT='" + tsd.getChannelAttributeWordDescription(pName) + "' WHERE SS_ID=" + stylesheetId
                    + " AND PARAM_NAME='" + pName + "' AND TYPE=3";
                LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else {
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + rsOld.getInt("TYPE") +
                  ").");
            }
          }
        } finally {
          rsOld.close();
          stmtOld.close();
        }
        // look for new attributes/parameters
        // insert all stylesheet params
        for (Enumeration e = tsd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          if (!oparams.contains(pName)) {
            sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId
                + ",'" + pName + "','" + tsd.getStylesheetParameterDefaultValue(pName) + "','" + tsd.getStylesheetParameterWordDescription(pName)
                + "',1)";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // insert all channel attributes
        for (Enumeration e = tsd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          if (!ocattrs.contains(pName)) {
            sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId
                + ",'" + pName + "','" + tsd.getChannelAttributeDefaultValue(pName) + "','" + tsd.getChannelAttributeWordDescription(pName)
                + "',3)";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Remove (with cleanup) a structure stylesheet param
   * @param stylesheetId id of the structure stylesheet
   * @param pName name of the parameter
   * @param con active database connection
   */
  private void removeStructureStylesheetParam (int stylesheetId, String pName, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "DELETE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId + " AND TYPE=1 AND PARAM_NAME='" + pName
          + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=1 AND PARAM_NAME='"
          + pName + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  /**
   * Remove (with cleanup) a structure stylesheet folder attribute
   * @param stylesheetId id of the structure stylesheet
   * @param pName name of the attribute
   * @param con active database connection
   */
  private void removeStructureFolderAttribute (int stylesheetId, String pName, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "DELETE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId + " AND TYPE=2 AND PARAM_NAME='" + pName
          + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureFolderAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=2 AND PARAM_NAME='"
          + pName + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureFolderAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  /**
   * Remove (with cleanup) a structure stylesheet channel attribute
   * @param stylesheetId id of the structure stylesheet
   * @param pName name of the attribute
   * @param con active database connection
   */
  private void removeStructureChannelAttribute (int stylesheetId, String pName, Connection con) throws java.sql.SQLException {
    Statement stmt = con.createStatement();
    try {
      String sQuery = "DELETE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId + " AND TYPE=3 AND PARAM_NAME='" + pName
          + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureChannelAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=3 AND PARAM_NAME='"
          + pName + "'";
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureChannelAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  /**
   * Updates an existing structure stylesheet description with a new one. Old stylesheet
   * description is found based on the Id provided in the parameter structure.
   * @param ssd new stylesheet description
   */
  public void updateStructureStylesheetDescription (StructureStylesheetDescription ssd) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int stylesheetId = ssd.getId();
        String sQuery = "UPDATE UP_SS_STRUCT SET SS_NAME='" + ssd.getStylesheetName() + "',SS_URI='" + ssd.getStylesheetURI()
            + "',SS_DESCRIPTION_URI='" + ssd.getStylesheetDescriptionURI() + "',SS_DESCRIPTION_TEXT='" + ssd.getStylesheetWordDescription()
            + "' WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // first, see what was there before
        HashSet oparams = new HashSet();
        HashSet ofattrs = new HashSet();
        HashSet ocattrs = new HashSet();
        sQuery = "SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
        Statement stmtOld = con.createStatement();
        ResultSet rsOld = stmtOld.executeQuery(sQuery);
        try {
          while (rsOld.next()) {
            int type = rsOld.getInt("TYPE");
            if (type == 1) {
              // stylesheet param
              String pName = rsOld.getString("PARAM_NAME");
              oparams.add(pName);
              if (!ssd.containsParameterName(pName)) {
                // delete param
                removeStructureStylesheetParam(stylesheetId, pName, con);
              }
              else {
                // update param
                sQuery = "UPDATE UP_SS_STRUCT_PAR SET PARAM_DEFAULT_VAL='" + ssd.getStylesheetParameterDefaultValue(pName)
                    + "',PARAM_DESCRIPT='" + ssd.getStylesheetParameterWordDescription(pName) + "' WHERE SS_ID=" + stylesheetId
                    + " AND PARAM_NAME='" + pName + "' AND TYPE=1";
                LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else if (type == 2) {
              // folder attribute
              String pName = rsOld.getString("PARAM_NAME");
              ofattrs.add(pName);
              if (!ssd.containsFolderAttribute(pName)) {
                // delete folder attribute
                removeStructureFolderAttribute(stylesheetId, pName, con);
              }
              else {
                // update folder attribute
                sQuery = "UPDATE UP_SS_STRUCT_PAR SET PARAM_DEFAULT_VAL='" + ssd.getFolderAttributeDefaultValue(pName) +
                    "',PARAM_DESCRIPT='" + ssd.getFolderAttributeWordDescription(pName) + "' WHERE SS_ID=" + stylesheetId
                    + " AND PARAM_NAME='" + pName + "'AND TYPE=2";
                LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else if (type == 3) {
              // channel attribute
              String pName = rsOld.getString("PARAM_NAME");
              ocattrs.add(pName);
              if (!ssd.containsChannelAttribute(pName)) {
                // delete channel attribute
                removeStructureChannelAttribute(stylesheetId, pName, con);
              }
              else {
                // update channel attribute
                sQuery = "UPDATE UP_SS_STRUCT_PAR SET PARAM_DEFAULT_VAL='" + ssd.getChannelAttributeDefaultValue(pName) +
                    "',PARAM_DESCRIPT='" + ssd.getChannelAttributeWordDescription(pName) + "' WHERE SS_ID=" + stylesheetId
                    + " AND PARAM_NAME='" + pName + "' AND TYPE=3";
                LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else {
              LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + rsOld.getInt("TYPE") +
                  ").");
            }
          }
        } finally {
          rsOld.close();
          stmtOld.close();
        }
        // look for new attributes/parameters
        // insert all stylesheet params
        for (Enumeration e = ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          if (!oparams.contains(pName)) {
            sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" +
                stylesheetId + ",'" + pName + "','" + ssd.getStylesheetParameterDefaultValue(pName) + "','" + ssd.getStylesheetParameterWordDescription(pName)
                + "',1)";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // insert all folder attributes
        for (Enumeration e = ssd.getFolderAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          if (!ofattrs.contains(pName)) {
            sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" +
                stylesheetId + ",'" + pName + "','" + ssd.getFolderAttributeDefaultValue(pName) + "','" + ssd.getFolderAttributeWordDescription(pName)
                + "',2)";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // insert all channel attributes
        for (Enumeration e = ssd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          if (!ocattrs.contains(pName)) {
            sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" +
                stylesheetId + ",'" + pName + "','" + ssd.getChannelAttributeDefaultValue(pName) + "','" + ssd.getChannelAttributeWordDescription(pName)
                + "',3)";
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // Commit the transaction
        commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Registers a NEW structure stylesheet with the database.
   * @param tsd Stylesheet description object
   */
  public Integer addStructureStylesheetDescription (StructureStylesheetDescription ssd) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // we assume that this is a new stylesheet.
        int id = getIncrementIntegerId("UP_SS_STRUCT");
        ssd.setId(id);
        String sQuery = "INSERT INTO UP_SS_STRUCT (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT) VALUES ("
            + id + ",'" + ssd.getStylesheetName() + "','" + ssd.getStylesheetURI() + "','" + ssd.getStylesheetDescriptionURI()
            + "','" + ssd.getStylesheetWordDescription() + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // insert all stylesheet params
        for (Enumeration e = ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getStylesheetParameterDefaultValue(pName) + "','" + ssd.getStylesheetParameterWordDescription(pName)
              + "',1)";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all folder attributes
        for (Enumeration e = ssd.getFolderAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getFolderAttributeDefaultValue(pName) + "','" + ssd.getFolderAttributeWordDescription(pName)
              + "',2)";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all channel attributes
        for (Enumeration e = ssd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getChannelAttributeDefaultValue(pName) + "','" + ssd.getChannelAttributeWordDescription(pName)
              + "',3)";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // Commit the transaction
        commit(con);
        return  new Integer(id);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Obtain structure stylesheet description object for a given structure stylesheet id
   * @para id id of the structure stylesheet
   * @return structure stylesheet description
   */
  public StructureStylesheetDescription getStructureStylesheetDescription (int stylesheetId) throws Exception {
    StructureStylesheetDescription ssd = null;
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    try {
      int dbOffset = 0;
      String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT";
      if (supportsOuterJoins) {
        sQuery += ",TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT " + dbStrings.structureStylesheet;
        dbOffset = 4;
      } else {
        sQuery += " FROM UP_SS_STRUCT USS WHERE";
      }
      sQuery += " USS.SS_ID=" + stylesheetId;

      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      try {
        if (rs.next()) {
          ssd = new StructureStylesheetDescription();
          ssd.setId(stylesheetId);
          ssd.setStylesheetName(rs.getString(1));
          ssd.setStylesheetURI(rs.getString(2));
          ssd.setStylesheetDescriptionURI(rs.getString(3));
          ssd.setStylesheetWordDescription(rs.getString(4));
        }

        if (!supportsOuterJoins) {
          rs.close();
          // retrieve stylesheet params and attributes
          sQuery = "SELECT TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
        }

        while (true) {
          if (!supportsOuterJoins && !rs.next()) {
            break;
          }

          int type = rs.getInt(dbOffset + 1);
          if (rs.wasNull()){
            break;
          }
          if (type == 1) {
            // param
            ssd.addStylesheetParameter(rs.getString(dbOffset + 2), rs.getString(dbOffset + 3), rs.getString(dbOffset + 4));
          }
          else if (type == 2) {
            // folder attribute
            ssd.addFolderAttribute(rs.getString(dbOffset + 2), rs.getString(dbOffset + 3), rs.getString(dbOffset + 4));
          }
          else if (type == 3) {
            // channel attribute
            ssd.addChannelAttribute(rs.getString(dbOffset + 2), rs.getString(dbOffset + 3), rs.getString(dbOffset + 4));
          }
          else {
            LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + rs.getInt(dbOffset + 1) + ").");
          }
          if (supportsOuterJoins && !rs.next()) {
            break;
          }

        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
      rdbmService.releaseConnection(con);
    }
    return  ssd;
  }

  /**
   * Obtain theme stylesheet description object for a given theme stylesheet id
   * @para id id of the theme stylesheet
   * @return theme stylesheet description
   */
  public ThemeStylesheetDescription getThemeStylesheetDescription (int stylesheetId) throws Exception {
    ThemeStylesheetDescription tsd = null;
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    try {
      int dbOffset = 0;
      String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_ICON_URI,SAMPLE_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS";
      if (supportsOuterJoins) {
        sQuery += ",TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT " + dbStrings.themeStylesheet;
        dbOffset = 11;
      } else {
        sQuery += " FROM UP_SS_THEME UTS WHERE";
      }
      sQuery += " UTS.SS_ID=" + stylesheetId;
      LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      try {
        if (rs.next()) {
          tsd = new ThemeStylesheetDescription();
          tsd.setId(stylesheetId);
          tsd.setStylesheetName(rs.getString(1));
          tsd.setStylesheetURI(rs.getString(2));
          tsd.setStylesheetDescriptionURI(rs.getString(3));
          tsd.setStylesheetWordDescription(rs.getString(4));
          tsd.setStructureStylesheetId(rs.getInt(5));
          tsd.setSampleIconURI(rs.getString(6));
          tsd.setSamplePictureURI(rs.getString(7));
          tsd.setMimeType(rs.getString(8));
          tsd.setDeviceType(rs.getString(9));
          tsd.setSerializerName(rs.getString(10));
          tsd.setCustomUserPreferencesManagerClass(rs.getString(11));
        }

        if (!supportsOuterJoins) {
          rs.close();
          // retrieve stylesheet params and attributes
          sQuery = "SELECT TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
        }
        while (true) {
          if (!supportsOuterJoins && !rs.next()) {
            break;
          }
          int type = rs.getInt(dbOffset + 1);
          if (rs.wasNull()) {
            break;
          }
          if (type == 1) {
            // param
            tsd.addStylesheetParameter(rs.getString(dbOffset + 2), rs.getString(dbOffset + 3), rs.getString(dbOffset + 4));
          }
          else if (type == 3) {
            // channel attribute
            tsd.addChannelAttribute(rs.getString(dbOffset + 2), rs.getString(dbOffset + 3), rs.getString(dbOffset + 4));
          }
          else if (type == 2) {
            // folder attributes are not allowed here
            LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! Corrupted DB entry. (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + rs.getInt(dbOffset + 1) + ").");
          }
          else {
            LogService.instance().log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + rs.getInt(dbOffset + 1) + ").");
          }
          if (supportsOuterJoins && !rs.next()) {
            break;
          }
        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
      rdbmService.releaseConnection(con);
    }
    return  tsd;
  }

  /**
   * Registers a NEW theme stylesheet with the database.
   * @param tsd Stylesheet description object
   */
  public Integer addThemeStylesheetDescription (ThemeStylesheetDescription tsd) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // we assume that this is a new stylesheet.
        int id = getIncrementIntegerId("UP_SS_THEME");
        tsd.setId(id);
        String sQuery = "INSERT INTO UP_SS_THEME (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_URI,SAMPLE_ICON_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS) VALUES ("
            + id + ",'" + tsd.getStylesheetName() + "','" + tsd.getStylesheetURI() + "','" + tsd.getStylesheetDescriptionURI()
            + "','" + tsd.getStylesheetWordDescription() + "'," + tsd.getStructureStylesheetId() + ",'" + tsd.getSamplePictureURI()
            + "','" + tsd.getSampleIconURI() + "','" + tsd.getMimeType() + "','" + tsd.getDeviceType() + "','" + tsd.getSerializerName()
            + "','" + tsd.getCustomUserPreferencesManagerClass() + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // insert all stylesheet params
        for (Enumeration e = tsd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +
              ",'" + pName + "','" + tsd.getStylesheetParameterDefaultValue(pName) + "','" + tsd.getStylesheetParameterWordDescription(pName)
              + "',1)";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all channel attributes
        for (Enumeration e = tsd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +
              ",'" + pName + "','" + tsd.getChannelAttributeDefaultValue(pName) + "','" + tsd.getChannelAttributeWordDescription(pName)
              + "',3)";
          LogService.instance().log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // Commit the transaction
        commit(con);
        return  new Integer(id);
      } catch (Exception e) {
        // Roll back the transaction
        rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }
}



