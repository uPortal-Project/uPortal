/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

import  org.jasig.portal.utils.DTDResolver;
import  org.jasig.portal.services.LogService;
import  org.jasig.portal.services.AuthorizationService;
import  org.jasig.portal.security.IAuthorizationPrincipal;
import  org.jasig.portal.security.IPerson;
import  java.io.StringWriter;
import  java.sql.Connection;
import  java.sql.ResultSet;
import  java.sql.SQLException;
import  java.sql.Statement;
import  java.util.ArrayList;
import  java.util.Date;
import  java.util.HashMap;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.XMLSerializer;
import  org.w3c.dom.Document;
import  org.w3c.dom.Element;
import  org.w3c.dom.NamedNodeMap;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.apache.xerces.dom.DocumentImpl;

/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 */
public class RDBMChannelRegistryStore implements IChannelRegistryStore {
  private static final int DEBUG = 0;
  String sRegDtd = "channelRegistry.dtd";

  private static final Object channelLock = new Object();
  private static final HashMap channelCache = new HashMap();

  public RDBMChannelRegistryStore() throws Exception {
    if (RdbmServices.supportsOuterJoins) {
      if (RdbmServices.joinQuery instanceof RdbmServices.JdbcDb) {
        RdbmServices.joinQuery.addQuery("channel",
          "{oj UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID} WHERE");
      } else if (RdbmServices.joinQuery instanceof RdbmServices.PostgreSQLDb) {
         RdbmServices.joinQuery.addQuery("channel",
          "UP_CHANNEL UC LEFT OUTER JOIN UP_CHANNEL_PARAM UCP ON UC.CHAN_ID = UCP.CHAN_ID WHERE");
     } else if (RdbmServices.joinQuery instanceof RdbmServices.OracleDb) {
        RdbmServices.joinQuery.addQuery("channel",
          "UP_CHANNEL UC, UP_CHANNEL_PARAM UCP WHERE UC.CHAN_ID = UCP.CHAN_ID(+) AND");
      } else {
        throw new Exception("Unknown database driver");
      }
    }
  }

  /**
   * Returns an XML document which describes the channel registry.
   * Right now this is being stored as a string in a field but could be also implemented to get from multiple tables.
   * @return a string of XML
   * @throws java.lang.Exception
   */
  public Document getChannelRegistryXML (IPerson person) throws SQLException, AuthorizationException {
    Document doc = new org.apache.xerces.dom.DocumentImpl();
    Element registry = doc.createElement("registry");
    doc.appendChild(registry);
    Connection con = RdbmServices.getConnection();
    try {
      RdbmServices.PreparedStatement chanStmt = new RdbmServices.PreparedStatement(con, "SELECT CHAN_ID FROM UP_CAT_CHAN WHERE CAT_ID=?");
      try {
        Statement stmt = con.createStatement();
        try {
          String userKey = "" + person.getID();
          Class userType = org.jasig.portal.security.IPerson.class;
          IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(userKey, userType);
          try {
            String query = "SELECT CAT_ID, CAT_TITLE, CAT_DESC FROM UP_CATEGORY WHERE PARENT_CAT_ID IS NULL ORDER BY CAT_TITLE";
            LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.getChannelRegistryXML(): " + query);
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
                appendChildCategoriesAndChannels(con, ap, chanStmt, category, catId);
              }
            } finally {
              rs.close();
            }
          } finally {
            // ap.close();
          }
        } finally {
          stmt.close();
        }
      } finally {
        chanStmt.close();
      }
    } finally {
      RdbmServices.releaseConnection(con);
    }
    return doc;
  }

  protected void appendChildCategoriesAndChannels (Connection con, IAuthorizationPrincipal ap, RdbmServices.PreparedStatement chanStmt, Element category, int catId) throws SQLException, AuthorizationException {
    Document doc = category.getOwnerDocument();
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = con.createStatement();
      String query = "SELECT CAT_ID, CAT_TITLE, CAT_DESC FROM UP_CATEGORY WHERE PARENT_CAT_ID=" + catId;
      LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.appendChildCategoriesAndChannels(): " + query);
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
        appendChildCategoriesAndChannels(con, ap, chanStmt, childCategory, childCatId);
      }

      // Append children channels
      chanStmt.clearParameters();
      chanStmt.setInt(1, catId);
      LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.appendChildCategoriesAndChannels(): " + chanStmt);
      rs = chanStmt.executeQuery();
      try {
        while (rs.next()) {
          int chanId = rs.getInt(1);
          if (ap.canSubscribe(chanId)) {
            Element channel = getChannelNode (chanId, con, (org.apache.xerces.dom.DocumentImpl)doc, "chan" + chanId);
            if (channel == null) {
              LogService.instance().log(LogService.WARN, "RDBMChannelRegistryStore.appendChildCategoriesAndChannels(): channel " + chanId +
                " in category " + catId + " does not exist in the store");
            } else {
              category.appendChild(channel);
            }
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
    Connection con = RdbmServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT TYPE_ID, TYPE, TYPE_NAME, TYPE_DESCR, TYPE_DEF_URI FROM UP_CHAN_TYPE";
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.getChannelTypesXML(): " + sQuery);
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
      RdbmServices.releaseConnection(con);
    }
    return doc;
  }

  /** A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param id the identifier for the channel
   * @param publisherId the identifier for the user who is publishing this channel
   * @param chanXML XML that describes the channel
   * @param catID an array of category IDs
   * @throws java.lang.Exception
   */
  public void addChannel (int id, IPerson publisher, Document chanXML, String catID[]) throws Exception {
    Connection con = RdbmServices.getConnection();
    try {
      addChannel(id, publisher, chanXML, con);
      // Set autocommit false for the connection
      RdbmServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // First delete existing categories for this channel
        String sDelete = "DELETE FROM UP_CAT_CHAN WHERE CHAN_ID=" + id;
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sDelete);
        int recordsDeleted = stmt.executeUpdate(sDelete);

        for (int i = 0; i < catID.length; i++) {
          // Take out "cat" prefix if its there
          String categoryID = catID[i].startsWith("cat") ? catID[i].substring(3) : catID[i];

          String sInsert = "INSERT INTO UP_CAT_CHAN (CHAN_ID, CAT_ID) VALUES (" + id + "," + categoryID + ")";
          LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sInsert);
          stmt.executeUpdate(sInsert);
        }
        // Commit the transaction
        RdbmServices.commit(con);
      } catch (SQLException sqle) {
        // Roll back the transaction
        RdbmServices.rollback(con);
        throw sqle;
      } finally {
        if (stmt != null)
          stmt.close();
      }
    } finally {
      RdbmServices.releaseConnection(con);
    }
  }

  /** A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param id the identifier for the channel
   * @param publisher the user who is publishing this channel
   * @param chanXML XML that describes the channel
   */
  public void addChannel (int id, IPerson publisher, Document chanXML) throws Exception {
    Connection con = RdbmServices.getConnection();
    try {
      addChannel(id, publisher, chanXML, con);
    } finally {
      RdbmServices.releaseConnection(con);
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
  private void addChannel (int id, IPerson publisher, Document doc, Connection con) throws SQLException {
    Element channel = (Element)doc.getFirstChild();
    // Set autocommit false for the connection
    RdbmServices.setAutoCommit(con, false);
    Statement stmt = con.createStatement();
    try {
      String sqlTitle = RdbmServices.sqlEscape(channel.getAttribute("title"));
      String sqlDescription = RdbmServices.sqlEscape(channel.getAttribute("description"));
      String sqlClass = channel.getAttribute("class");
      String sqlTypeID = channel.getAttribute("typeID");
      String sysdate = RdbmServices.sqlTimeStamp();
      String sqlTimeout = channel.getAttribute("timeout");
      String timeout = "0";
      if (sqlTimeout != null && sqlTimeout.trim().length() != 0) {
        timeout  = sqlTimeout;
      }
      String sqlEditable = RdbmServices.dbFlag(xmlBool(channel.getAttribute("editable")));
      String sqlHasHelp = RdbmServices.dbFlag(xmlBool(channel.getAttribute("hasHelp")));
      String sqlHasAbout = RdbmServices.dbFlag(xmlBool(channel.getAttribute("hasAbout")));
      String sqlName = RdbmServices.sqlEscape(channel.getAttribute("name"));
      String sqlFName = RdbmServices.sqlEscape(channel.getAttribute("fname"));

      String sQuery = "SELECT CHAN_ID FROM UP_CHANNEL WHERE CHAN_ID=" + id;
      LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);

      // If channel is already there, do an update, otherwise do an insert
      if (rs.next()) {
        String sUpdate = "UPDATE UP_CHANNEL SET " +
        "CHAN_TITLE='" + sqlTitle + "', " +
        "CHAN_DESC='" + sqlDescription + "', " +
        "CHAN_CLASS='" + sqlClass + "', " +
        "CHAN_TYPE_ID=" + sqlTypeID + ", " +
        "CHAN_PUBL_ID=" + publisher.getID() + ", " +
        "CHAN_PUBL_DT=" + sysdate + ", " +
        "CHAN_APVL_ID=NULL, " +
        "CHAN_APVL_DT=NULL, " +
        "CHAN_TIMEOUT=" + timeout + ", " +
        "CHAN_EDITABLE='" + sqlEditable + "', " +
        "CHAN_HAS_HELP='" + sqlHasHelp + "', " +
        "CHAN_HAS_ABOUT='" + sqlHasAbout + "', " +
        "CHAN_NAME='" + sqlName + "', " +
        "CHAN_FNAME='" + sqlFName + "' " +
        "WHERE CHAN_ID=" + id;
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
      } else {
        String sInsert = "INSERT INTO UP_CHANNEL (CHAN_ID, CHAN_TITLE, CHAN_DESC, CHAN_CLASS, CHAN_TYPE_ID, CHAN_PUBL_ID, CHAN_PUBL_DT,  CHAN_TIMEOUT, "
            + "CHAN_EDITABLE, CHAN_HAS_HELP, CHAN_HAS_ABOUT, CHAN_NAME, CHAN_FNAME) ";
        sInsert += "VALUES (" + id + ", '" + sqlTitle + "', '" + sqlDescription + "', '" + sqlClass + "', " + sqlTypeID + ", "
            + publisher.getID() + ", " + sysdate + ", " + timeout
            + ", '" + sqlEditable + "', '" + sqlHasHelp + "', '" + sqlHasAbout
            + "', '" + sqlName + "', '" + sqlFName + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sInsert);
        stmt.executeUpdate(sInsert);
      }

      // First delete existing parameters for this channel
      String sDelete = "DELETE FROM UP_CHANNEL_PARAM WHERE CHAN_ID=" + id;
      LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sDelete);
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
            LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): " + sInsert);
            stmt.executeUpdate(sInsert);
          }
        }
      }
      // Commit the transaction
      RdbmServices.commit(con);
      flushChannelEntry(id);
    } catch (SQLException sqle) {
      RdbmServices.rollback(con);
      throw  sqle;
    } finally {
      stmt.close();
    }
  }

  /** A method for approving a channel so that users are allowed to subscribe to it.
   *  This would be called by the publish channel or the administrator channel
   *  @param chanId Channel to approve
   *  @param approved Account approving the channel
   *  @param approveDate When should the channel appear
   */
  public void approveChannel(int chanId, IPerson approver, Date approveDate) throws Exception {
    Connection con = RdbmServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sUpdate = "UPDATE UP_CHANNEL SET CHAN_APVL_ID = " + approver.getID() +
        ", CHAN_APVL_DT = " + RdbmServices.sqlTimeStamp(approveDate) +
        " WHERE CHAN_ID = " + chanId;
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.approveChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);
      } finally {
        stmt.close();
      }
    } finally {
      RdbmServices.releaseConnection(con);
    }
   }

  /** A method for getting the next available channel ID.
   * This would be called by a publish channel.
   */
  public int getNextId () throws PortalException {
    int nextID;
    try {
      nextID = UserLayoutStoreFactory.getUserLayoutStoreImpl().getIncrementIntegerId("UP_CHANNEL");
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      throw  new GeneralRenderingException("Unable to allocate new channel ID");
    }
    return  nextID;
  }

  /**
   * Removes a channel from the channel registry.  The channel
   * is not actually deleted.  Rather its status as an "approved"
   * channel is revoked.
   * @param chanID, the ID of the channel to delete
   * @exception java.sql.SQLException
   */
  public void removeChannel (String chanID) throws Exception {
    Connection con = RdbmServices.getConnection();
    try {
      // Set autocommit false for the connection
      RdbmServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        chanID = chanID.startsWith("chan") ? chanID.substring(4) : chanID;

        // Delete channel/category associations
        //String sDelete = "DELETE FROM UP_CAT_CHAN WHERE CHAN_ID=" + chanID;
        //LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.removeChannel(): " + sDelete);
        //stmt.executeUpdate(sDelete);

        // Delete channel/role associations
        //sDelete = "DELETE FROM UP_ROLE_CHAN WHERE CHAN_ID=" + chanID;
        //LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.removeChannel(): " + sDelete);
        //stmt.executeUpdate(sDelete);

        // Delete channel.
        String sUpdate = "UPDATE UP_CHANNEL SET CHAN_APVL_DT=NULL WHERE CHAN_ID=" + chanID;
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.removeChannel(): " + sUpdate);
        stmt.executeUpdate(sUpdate);

        // Commit the transaction
        RdbmServices.commit(con);
      } catch (SQLException sqle) {
        // Roll back the transaction
        RdbmServices.rollback(con);
        throw sqle;
      } finally {
          stmt.close();
      }
    } finally {
      RdbmServices.releaseConnection(con);
    }
  }

  /**
   * A method for persisting the channel registry to a file or database.
   * @param registryXML an XML description of the channel registry
   */
  public void setRegistryXML (String registryXML) throws Exception{
    throw new Exception("not implemented yet");
  }

  /**
   * put your documentation comment here
   * @param chanDoc
   * @return
   */
  private String serializeDOM (Document chanDoc) {
    StringWriter stringOut = null;
    try {
      OutputFormat format = new OutputFormat(chanDoc);          //Serialize DOM
      stringOut = new StringWriter();           //Writer will be a String
      XMLSerializer serial = new XMLSerializer(stringOut, format);
      serial.asDOMSerializer();                 // As a DOM Serializer
      serial.serialize(chanDoc.getDocumentElement());
    } catch (java.io.IOException ioe) {
      LogService.instance().log(LogService.ERROR, ioe);
    }
    return  stringOut.toString();
    //LogService.instance().log(LogService.DEBUG, "STRXML = " + stringOut.toString());
  }

  /**
   * convert true/false into Y/N for database
   * @param value to check
   * @result boolean
   */
  protected static final boolean xmlBool (String value) {
      return (value != null && value.equals("true") ? true : false);
  }
    /**
     * Manage the Channel cache
     */
  /**
   * See if the channel is already in the cache
   * @param channel id
   */
  protected boolean channelCached(int chanId) {
    return channelCache.containsKey(new Integer(chanId));
  }

  /**
   * Remove channel entry from cache
   * @param channel id
   */
  public void flushChannelEntry(int chanId) {
    synchronized (channelLock) {
      if (channelCache.remove(new Integer(chanId)) != null) {
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannel(): flushed channel "
          + chanId + " from cache");
      }
    }
  }


  /**
   * Get a channel from the cache (it better be there)
   */
  public ChannelStoreDefinition getChannel(int chanId) {
    return (ChannelStoreDefinition)channelCache.get(new Integer(chanId));
  }

  /**
   * Get a channel from the cache (it better be there)
   */
  public Element getChannelXML(int chanId, DocumentImpl doc, String idTag) {
    ChannelStoreDefinition channel = getChannel(chanId);
    if (channel != null) {
      return channel.getDocument(doc, idTag);
    } else {
      return null;
    }
  }
  /**
   * Get a channel from the cache or the store
   */
  public ChannelStoreDefinition getChannel(int chanId, boolean cacheChannel, RdbmServices.PreparedStatement pstmtChannel, RdbmServices.PreparedStatement pstmtChannelParm) throws java.sql.SQLException {
    Integer chanID = new Integer(chanId);
    boolean inCache = true;
    ChannelStoreDefinition channel = (ChannelStoreDefinition)channelCache.get(chanID);
    if (channel == null) {
      synchronized (channelLock) {
        channel = (ChannelStoreDefinition)channelCache.get(chanID);
        if (channel == null || cacheChannel && channel.refreshMe()) {  // Still undefined or stale, let's get it
          channel = getChannelStoreDefinition(chanId, pstmtChannel, pstmtChannelParm);
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
        "RDBMChannelRegistryStore.getChannelStoreDefinition(): Got channel " + chanId + " from the cache");
    }

    return channel;
  }

  /**
   * Read a channel definition from the data store
   */
  protected ChannelStoreDefinition getChannelStoreDefinition (int chanId, RdbmServices.PreparedStatement pstmtChannel, RdbmServices.PreparedStatement pstmtChannelParm) throws java.sql.SQLException {
    ChannelStoreDefinition channel = null;

    pstmtChannel.clearParameters();
    pstmtChannel.setInt(1, chanId);
    LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.getChannel(): " + pstmtChannel);
    ResultSet rs = pstmtChannel.executeQuery();
    try {
      if (rs.next()) {
        channel = new ChannelStoreDefinition(chanId, rs.getString(1), rs.getString(2), rs.getString(3),
        rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getTimestamp(7), rs.getTimestamp(8), rs.getInt(9),
          rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13),
          rs.getString(14));

        int dbOffset = 0;
        if (pstmtChannelParm == null) { // we are using a join statement so no need for a new query
          dbOffset = 14;
        } else {
          rs.close();
          pstmtChannelParm.clearParameters();
          pstmtChannelParm.setInt(1, chanId);
          LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.getChannel(): " + pstmtChannelParm);
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
      "RDBMChannelRegistryStore.getChannelStoreDefinition(): Read channel " + chanId + " from the store");
    return  channel;
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

  public Element getChannelNode (int chanId, Connection con, DocumentImpl doc, String idTag) throws java.sql.SQLException {
    RdbmServices.PreparedStatement pstmtChannel = getChannelPstmt(con);
    try {
      RdbmServices.PreparedStatement pstmtChannelParm = getChannelParmPstmt(con);
      try {
        ChannelStoreDefinition cd = getChannel(chanId, false, pstmtChannel, pstmtChannelParm);
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

  public final RdbmServices.PreparedStatement getChannelParmPstmt(Connection con) throws SQLException {
    if (RdbmServices.supportsOuterJoins) {
      return null;
    } else {
      return new RdbmServices.PreparedStatement(con, "SELECT CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM UP_CHANNEL_PARAM WHERE CHAN_ID=?");
    }
  }

  public final RdbmServices.PreparedStatement getChannelPstmt(Connection con) throws SQLException {
    String sql;
    sql = "SELECT UC.CHAN_TITLE,UC.CHAN_DESC,UC.CHAN_CLASS,UC.CHAN_TYPE_ID,UC.CHAN_PUBL_ID,UC.CHAN_APVL_ID,UC.CHAN_PUBL_DT,UC.CHAN_APVL_DT,"+
      "UC.CHAN_TIMEOUT,UC.CHAN_EDITABLE,UC.CHAN_HAS_HELP,UC.CHAN_HAS_ABOUT,UC.CHAN_NAME,UC.CHAN_FNAME";

    if (RdbmServices.supportsOuterJoins) {
      sql += ",CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM " + RdbmServices.joinQuery.getQuery("channel");
    } else {
      sql += " FROM UP_CHANNEL UC WHERE";
    }
    sql += " UC.CHAN_ID=? AND CHAN_APVL_DT IS NOT NULL AND CHAN_APVL_DT <= " + RdbmServices.sqlTimeStamp();

    return new RdbmServices.PreparedStatement(con, sql);
  }
}



