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
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.XMLSerializer;
import  org.jasig.portal.security.provider.RoleImpl;

/**
 * SQL implementation for the 2.x relational database model
 * @author George Lindholm
 * @version $Revision$
 */
public class DBImpl implements IDBImpl
{
  //This class is instantiated ONCE so NO class variables can be used to keep state between calls

  static int DEBUG = 0;
  protected RdbmServices rdbmService = null;

  public void DBImpl() {
    rdbmService = new RdbmServices();
  }

  public static final void dumpDoc(Node node, String indent) {
    if (node == null) {
      return;
    }
    if (node instanceof Element) {
      System.err.print(indent + "element: tag=" + ((Element)node).getTagName() + " ");
    } else if (node instanceof Document) {
      System.err.print("document:");
    } else {
      System.err.print(indent + "node:");
    }
    System.err.println("name=" + node.getNodeName()+  " value=" + node.getNodeValue());
    NamedNodeMap nm = node.getAttributes();
    if (nm != null) {
      for (int i = 0 ; i < nm.getLength(); i++) {
        System.err.println(indent + " " + nm.item(i).getNodeName() + ": '" + nm.item(i).getNodeValue() + "'");
      }
      System.err.println(indent + "--");
    }
    if (node.hasChildNodes()) {
      dumpDoc(node.getFirstChild(), indent + "   ");
    }
    dumpDoc(node.getNextSibling(), indent);
  }

  protected static final void addChannelHeaderAttribute(String name, int value, Element channel, Element system)
  {
    addChannelHeaderAttribute(name, value+"", channel, system);
  }

  protected static final void addChannelHeaderAttribute(String name, String value, Element channel, Element system)
  {
    channel.setAttribute(name, value);
    system.setAttribute("H"+name, ""); // Tag as not being changeable
  }

  protected static final void addChannelHeaderAttributeFlag(String name, String value, Element channel, Element system)
  {
    addChannelHeaderAttribute(name, (value != null && value.equals("Y") ? "true" : "false"), channel, system);
  }

  protected static final void createChannelNodeHeaders(DocumentImpl doc, int chanId, String idTag, ResultSet rs, Element channel, Element system) throws java.sql.SQLException
  {

        String chanTitle = rs.getString("CHAN_TITLE");
        String chanDesc = rs.getString("CHAN_DESC");
        String chanClass = rs.getString("CHAN_CLASS");
        int chanPupblUsrId = rs.getInt("CHAN_PUBL_ID");
        java.sql.Timestamp chanPublDt = rs.getTimestamp("CHAN_PUBL_DT");
        int chanApvlId = rs.getInt("CHAN_APVL_ID");
        java.sql.Timestamp chanApvlDt = rs.getTimestamp("CHAN_APVL_DT");
        int chanTimeout = rs.getInt("CHAN_TIMEOUT");
        String chanMinimizable = rs.getString("CHAN_MINIMIZABLE");
        String chanEditable = rs.getString("CHAN_EDITABLE");
        String chanHasHelp = rs.getString("CHAN_HAS_HELP");
        String chanHasAbout = rs.getString("CHAN_HAS_ABOUT");
        String chanUnremovable = rs.getString("CHAN_UNREMOVABLE");
        String chanDetachable = rs.getString("CHAN_DETACHABLE");
        String chanName = rs.getString("CHAN_NAME");

        doc.putIdentifier(idTag, channel);
        addChannelHeaderAttribute("ID", idTag, channel, system);

        channel.setAttribute("chanID", chanId + "");
        system.setAttribute("chanID", chanId + "");
        system.setAttribute("HchanID", chanId + ""); // Tag as not being changeable
        if (DEBUG > 1) System.err.println("channel " + chanName + "@" + chanId + " has tag " + chanId);
        addChannelHeaderAttribute("name", chanName, channel, system);
        addChannelHeaderAttribute("class", chanClass, channel, system);
        addChannelHeaderAttribute("timeout", chanTimeout, channel, system);
        addChannelHeaderAttributeFlag("minimizable", chanMinimizable, channel, system);
        addChannelHeaderAttributeFlag("editable", chanEditable, channel, system);
        addChannelHeaderAttributeFlag("hasHelp", chanHasHelp, channel, system);
        addChannelHeaderAttributeFlag("hasAbout", chanHasAbout, channel, system);
        addChannelHeaderAttributeFlag("unremovable", chanUnremovable, channel, system);
        addChannelHeaderAttributeFlag("detachable", chanDetachable, channel, system);
  }
  protected static final void createChannelNodeParameters(DocumentImpl doc, ResultSet rs, Element channel, Element system) throws java.sql.SQLException
  {
    String chanParmNM = rs.getString("CHAN_PARM_NM");
    if (rs.wasNull()) {
      return;
    }

    String chanParmVal = rs.getString("CHAN_PARM_VAL");

    Element parameter = doc.createElement("parameter");
    parameter.setAttribute("name", chanParmNM);
    parameter.setAttribute("value", chanParmVal);
    String override = rs.getString("CHAN_PARM_OVRD");
    if (override != null && override.equals("N")) {
      system.setAttribute("D"+chanParmNM, "");
    }
    channel.appendChild(parameter);
  }

  protected Element createChannelNode(Connection con, DocumentImpl doc, int chanId, String idTag) throws java.sql.SQLException
  {
    Element channel = null;
    String sQuery = "SELECT * FROM UP_CHANNEL WHERE CHAN_ID=" + chanId;
    Logger.log (Logger.DEBUG, "DBImpl::createChannelNode(): " + sQuery);

    Statement stmt = con.createStatement();
    try {
      ResultSet rs = stmt.executeQuery (sQuery);
      try {
        if (rs.next()) {
          channel = doc.createElement("channel");
          Element system = doc.createElement("system");
          createChannelNodeHeaders(doc, chanId, idTag, rs, channel, system);
          rs.close();

          sQuery = "SELECT * FROM UP_CHAN_PARAM WHERE CHAN_ID=" + chanId;
          Logger.log (Logger.DEBUG, "DBImpl::createChannelNode(): " + sQuery);
          rs = stmt.executeQuery (sQuery);
          while(rs.next()) {
            createChannelNodeParameters(doc, rs, channel, system);
          }
          channel.appendChild(system);
        }
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }
    return channel;
  }

  protected static final NamedNodeMap findSystemNamedNodeMap(Element node, String tag)
  {
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i).getNodeName().equals(tag)) {
        return nl.item(i).getAttributes();
      }
    }
    return null;
  }

  protected static final Element findSystemNode(Node node)
  {
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i).getNodeName().equals("system")) {
        return (Element) nl.item(i);
      }
    }
    return null;
  }

  protected void createLayout(Connection con, DocumentImpl doc, Statement stmt, Element root, int userId, int profileId, int layoutId, int structId) throws java.sql.SQLException
  {
    if (structId == 0) { // End of line
      return;
    }

    int nextStructId;
    int chldStructId;
    int chanId;

    Element system = null;
    Element parameter = null;
    Element structure;
    String sQuery = "SELECT * FROM UP_LAYOUT_STRUCT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId + " AND STRUCT_ID=" + structId;
    Logger.log (Logger.DEBUG, "DBImpl::createLayout()" + sQuery);
    ResultSet rs = stmt.executeQuery (sQuery);
    try {
      rs.next();
      nextStructId = rs.getInt("NEXT_STRUCT_ID");
      chldStructId = rs.getInt("CHLD_STRUCT_ID");
      chanId = rs.getInt("CHAN_ID");

      structure = createLayoutStructure(rs, chanId, userId, stmt, doc);
    } finally {
      rs.close();
    }
    system = (Element) structure.getElementsByTagName("system").item(0);
    if (chanId != 0) { // Channel
      parameter = (Element) structure.getElementsByTagName("parameter").item(0);
    }

    sQuery = "SELECT * FROM UP_STRUCT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId + " AND STRUCT_ID=" + structId;
    Logger.log (Logger.DEBUG, "DBImpl::createLayout()" + sQuery);
    rs = stmt.executeQuery (sQuery);
    try {
      while (rs.next ()) {
        createLayoutStructureParameter(chanId, rs, structure, parameter, system);
     }
    } finally {
      rs.close();
    }
    root.appendChild(structure);

    if (chanId == 0) {  // Folder
      createLayout(con, doc, stmt, structure, userId, profileId, layoutId, chldStructId);
    }

    createLayout(con, doc, stmt, root, userId, profileId, layoutId, nextStructId);
  }

  protected static final void createLayoutStructureParameter(int chanId, ResultSet rs, Element structure, Element parameter, Element system) throws java.sql.SQLException
  {
    String paramName = rs.getString("STRUCT_PARM_NM");

    if (paramName != null) {
      String paramValue = rs.getString("STRUCT_PARM_VAL");
      if (chanId == 0) { // Folder
        structure.setAttribute(paramName, paramValue);
      } else { // Channel
          if (!system.hasAttribute("D" + paramName)) {
            parameter.setAttribute(paramName, paramValue);
          }
      }
    }
  }

  protected static boolean channelInUserRole(int chanId, int userId, Connection con) throws java.sql.SQLException
  {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT UC.CHAN_ID FROM UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR, UP_USER_ROLE UUR " +
          "WHERE UUR.USER_ID=" + userId + " AND UC.CHAN_ID=" + chanId +" AND UUR.ROLE_ID=UR.ROLE_ID AND UR.ROLE_ID=URC.ROLE_ID AND URC.CHAN_ID=UC.CHAN_ID";
        Logger.log (Logger.DEBUG, "DBImpl::channelInUserRole(): " + sQuery);
        ResultSet rs = stmt.executeQuery (sQuery);
        try {
          if (!rs.next()) {
            return false;
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    return true;
  }

  protected final Element createLayoutStructure(ResultSet rs, int chanId, int userId, Statement stmt, DocumentImpl doc) throws java.sql.SQLException
  {
    String idTag = rs.getString("ID_TAG");
    if (chanId != 0) { // Channel
      /* See if we have access to the channel */
      if (!channelInUserRole(chanId, userId, stmt.getConnection())) {
        /* No access to channel. Replace it with the error channel and a suitable message */

        /* !!!!!!!   Add code here someday !!!!!!!!!!!*/
        Logger.log(Logger.INFO, "DBImpl::createLayoutStructure(): No role access (ignored at the moment) for channel " + chanId + " for user " + userId);
      }

      return createChannelNode(stmt.getConnection(), doc, chanId, idTag);
    } else { // Folder
      String name = rs.getString("NAME");
      String type = rs.getString("TYPE");
      String hidden = rs.getString("HIDDEN");
      String unremovable = rs.getString("UNREMOVABLE");
      String immutable = rs.getString("IMMUTABLE");

      Element folder = doc.createElement("folder");
      Element system = doc.createElement("system");
      doc.putIdentifier(idTag, folder);
      addChannelHeaderAttribute("ID", idTag, folder, system);
      addChannelHeaderAttribute("name", name, folder, system);
      addChannelHeaderAttribute("type", (type != null ? type : "regular"), folder, system);
      addChannelHeaderAttribute("hidden", (hidden != null && hidden.equals("Y") ? "true" : "false"), folder, system);
      addChannelHeaderAttribute("immutable", (immutable == null || immutable.equals("Y") ? "true" : "false"), folder, system);
      addChannelHeaderAttribute("unremovable", (unremovable == null || unremovable.equals("Y") ? "true" : "false"), folder, system);

      folder.appendChild(system);
      return folder;
    }
  }

  /**
   * UserLayout
   * @param userId, the user ID
   * @param profileId, the profile ID
   * @return a DOM object representing the user layout
   * @throws Exception
   */
  public Document getUserLayout (int userId, int profileId) throws Exception {
    Connection con = rdbmService.getConnection();
    String str_uLayoutXML = null;
    con = rdbmService.getConnection ();
    try {
      DocumentImpl doc = new DocumentImpl();
      Element root = doc.createElement("layout");

      Statement stmt = con.createStatement ();
      try {
        long startTime = System.currentTimeMillis();
        String subSelectString = "SELECT LAYOUT_ID FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
        Logger.log (Logger.DEBUG, "DBImpl::getUserLayout()" + subSelectString);

        int layoutId;
        ResultSet rs = stmt.executeQuery (subSelectString);
        try {
          rs.next();
          layoutId = rs.getInt("LAYOUT_ID");
        } finally {
          rs.close();
        }

        String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
        Logger.log (Logger.DEBUG, "DBImpl::getUserLayout()" + sQuery);
        rs = stmt.executeQuery (sQuery);
        try {
          if (rs.next ()) {
            int structId = rs.getInt("INIT_STRUCT_ID");
            if (structId == 0) {      // Grab the default "Guest" layout
              structId = 1;           // Should look this up
            }
            createLayout(con, doc, stmt, root, userId, profileId, layoutId, structId);
          }
        } finally {
          rs.close();
        }
        long stopTime = System.currentTimeMillis();
        Logger.log(Logger.DEBUG, "DBImpl::getUserLayout() Layout document for user " + userId + " took " + (stopTime - startTime) + " milliseconds to create");

        doc.appendChild(root);
        if (DEBUG > 0) {
          System.err.println("--> created document");
          dumpDoc(doc, "");
          System.err.println("<--");
        }
      } finally {
        stmt.close();
      }

      return doc;
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Save the user layout
   * @param userId
   * @param profileId
   * @param layoutXML
   * @throws Exception
   */
  public void setUserLayout (int userId, int profileId, Document layoutXML) throws Exception {
    int layoutId = 0;
    Connection con = rdbmService.getConnection();
    try {
      setAutoCommit(con, false); // Need an atomic update here

      Statement stmt = con.createStatement ();
      try {
        String query = "SELECT LAYOUT_ID FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
        Logger.log (Logger.DEBUG, "DBImpl::setUserLayout()" + query);

        ResultSet rs = stmt.executeQuery (query);
        try {
          if (rs.next()) {
            layoutId = rs.getInt("LAYOUT_ID");
          }
        } finally {
          rs.close();
        }

        String selectString = "USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId;
        String sQuery = "DELETE FROM UP_STRUCT_PARAM WHERE " + selectString;
        Logger.log (Logger.DEBUG, "DBImpl::setUserLayout()" + sQuery);
        stmt.executeUpdate(sQuery);

        sQuery = "DELETE FROM UP_LAYOUT_STRUCT WHERE " + selectString;
        Logger.log (Logger.DEBUG, "DBImpl::setUserLayout()" + sQuery);
        stmt.executeUpdate(sQuery);
        if (DEBUG > 0) {
          System.err.println("--> saving document");
          dumpDoc(layoutXML.getFirstChild().getFirstChild(), "");
          System.err.println("<--");
        }
        saveStructure(layoutXML.getFirstChild().getFirstChild(), stmt, userId, profileId, new StructId());
      } finally {
        stmt.close();
      }

      commit(con);
    } catch (Exception e) {
      rollback(con);
      throw e;
    } finally {
      rdbmService.releaseConnection (con);
    }
  }

  protected class StructId {
    public int id = 1;
  }

  /**
   * convert true/false int Y/N for database
   * @param value to check
   * @result Y/N
   */
  protected static final String dbBool(String value)
  {
    if (value != null && value.equals("true")) {
      return "Y";
    } else {
      return "N";
    }
  }

  protected int saveStructure(Node node, Statement stmt, int userId, int layoutId, StructId structId) throws java.sql.SQLException {
      if (node == null) {
        return 0;
      } else if (node.getNodeName().equals("parameter")) {
        return 0;
      } else if (node.getNodeName().equals("system")) {
        return saveStructure(node.getNextSibling(), stmt, userId, layoutId, structId);
      }

      int saveStructId = structId.id++;
      int nextStructId = 0;
      int childStructId = 0;
      String sQuery;

      if (DEBUG > 0) Logger.log(Logger.DEBUG, "-->" + node.getNodeName() + "@" + saveStructId);
      if (node.hasChildNodes()) {
        childStructId = saveStructure(node.getFirstChild(), stmt, userId, layoutId, structId);
      }

      nextStructId = saveStructure(node.getNextSibling(), stmt, userId, layoutId, structId);

      Element structure = (Element) node;
      Element system = findSystemNode(node);

      String chanId = "NULL";
      String structName = "NULL";
      if (node.getNodeName().equals("channel")) {
        chanId = system.getAttribute("chanID");
      } else {
        structName = "'" + sqlEscape(structure.getAttribute("name")) + "'";
      }
      sQuery = "INSERT INTO UP_LAYOUT_STRUCT " +
      "(USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID,EXTERNAL_ID,CHAN_ID,ID_TAG,NAME,TYPE,HIDDEN,IMMUTABLE,UNREMOVABLE) VALUES (" +
        userId + "," + layoutId + "," + saveStructId + "," + nextStructId + "," + childStructId + "," +
        "'" + structure.getAttribute("external_id") + "'," + chanId + ",'" + structure.getAttribute("ID") + "'," +
        structName + ",'" + structure.getAttribute("type") + "'," +
        "'" + dbBool(structure.getAttribute("hidden")) + "','" + dbBool(structure.getAttribute("immutable")) + "'," +
        "'" + dbBool(structure.getAttribute("unremovable")) + "')";
      Logger.log(Logger.DEBUG, "DBImpl::saveStructure()" + sQuery);
      stmt.executeUpdate(sQuery);

      NamedNodeMap nm = node.getAttributes();
      if (nm != null) {
        for (int i = 0 ; i < nm.getLength(); i++) {
          String nodeName = nm.item(i).getNodeName();
          String nodeValue = nm.item(i).getNodeValue();
          String structHDInd = "H";
          if (DEBUG > 1) System.err.println(structHDInd+nodeName+"=" + nodeValue);
          if (system != null && system.hasAttribute(structHDInd + nodeName)) {
            if (DEBUG > 1) System.err.println("Not saving channel defined header value " + nodeName);
          } else {
            sQuery = "INSERT INTO UP_STRUCT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) VALUES ("+
              userId + "," + layoutId + "," + saveStructId + ",'" + nodeName + "','" + nm.item(i).getNodeValue() + "')";
            Logger.log(Logger.DEBUG, "DBImpl::saveStructure()" + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
      }

      NodeList parameters = node.getChildNodes();
      if (parameters != null) {
          for (int i = 0 ; i < parameters.getLength(); i++) {
            if (parameters.item(i).getNodeName().equals("parameter")) {
              Element parmElement = (Element) parameters.item(i);
              nm = parmElement.getAttributes();
              String nodeName = nm.getNamedItem("name").getNodeValue();
              String nodeValue = nm.getNamedItem("value").getNodeValue();
              String structHDInd = "D";
              if (DEBUG > 1) System.err.println(structHDInd + nodeName + "=" + nodeValue);
              if (system.hasAttribute(structHDInd + nodeName)) {
                if (DEBUG > 1) System.err.println("Not saving channel defined parameter value " + nodeName);
              } else {
                sQuery = "INSERT INTO UP_STRUCT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) VALUES ("+
                  userId + "," + layoutId + "," + saveStructId + ",'" + nodeName + "','" + nodeValue + "')";
                Logger.log(Logger.DEBUG, "DBImpl::saveStructure()" + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
          }
      }

      return saveStructId;
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
  public void addChannel (int id, String title, Document doc, String catID[]) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      addChannel(id, title, doc, con);

      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      try {
        for (int i = 0; i < catID.length; i++) {
          String sInsert = "INSERT INTO UP_CAT_CHAN (CHAN_ID, CAT_ID) VALUES (" + id + "," + catID[i]  + ")";
          Logger.log(Logger.DEBUG, "DBImpl::addChannel(): " + sInsert);
          stmt.executeUpdate(sInsert);
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
   * @param id
   * @param title
   * @param doc
   * @exception Exception
   */
  public void addChannel (int id, String title, Document doc) throws Exception {
    //System.out.println("Enterering ChannelRegistryImpl::addChannel()");
    Connection con = rdbmService.getConnection();
    try {
      addChannel(id, title, doc, con);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  protected static final String sqlEscape(String sql) {
    if (sql == null) {
      return "";
    } else {
      int primePos = sql.indexOf("'");
      if (primePos == -1) {
        return sql;
      } else {
        StringBuffer sb = new StringBuffer(sql.length() + 4);
        int startPos = 0;
        do {
          sb.append(sql.substring(startPos, primePos+1));
          sb.append("'");
          startPos = primePos + 1;
          primePos = sql.indexOf("'", startPos);
        } while (primePos != -1);
        sb.append(sql.substring(startPos));
        return sb.toString();
      }
    }
  }

  protected void addChannel (int id, String title, Document doc, Connection con)  throws Exception {
      Element channel = (Element)doc.getFirstChild();

      // Set autocommit false for the connection
      setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        String sysdate = "{ts'" + (new java.sql.Timestamp(System.currentTimeMillis())).toString() +
          "'}";
        String sqlTitle = sqlEscape(title);
        String sqlName = sqlEscape(channel.getAttribute("name"));
        String sInsert = "INSERT INTO UP_CHANNEL (CHAN_ID, CHAN_TITLE, CHAN_DESC, CHAN_CLASS, " +
          "CHAN_PUBL_ID, CHAN_PUBL_DT, CHAN_APVL_ID, CHAN_APVL_DT, CHAN_TIMEOUT, " +
          "CHAN_MINIMIZABLE, CHAN_EDITABLE, CHAN_HAS_HELP, CHAN_HAS_ABOUT, CHAN_UNREMOVABLE, CHAN_DETACHABLE, CHAN_NAME) ";
         sInsert += "VALUES (" + id + ",'" + sqlTitle + "','" + sqlTitle + " Channel','" + channel.getAttribute("class") + "'," +
          "0," + sysdate + ",0," + sysdate  +
          ",'" + channel.getAttribute("timeout") + "'," + "'" + dbBool(channel.getAttribute("minimizable")) + "'" +
          ",'" + dbBool(channel.getAttribute("editable")) + "'" +
          ",'" + dbBool(channel.getAttribute("hasHelp")) + "'," + "'" + dbBool(channel.getAttribute("hasAbout")) + "'" +
          ",'" + dbBool(channel.getAttribute("unremovable")) + "'," +"'" + dbBool(channel.getAttribute("detachable")) + "'" +
          ",'" + sqlName + "')";
        Logger.log(Logger.DEBUG, "DBImpl::addChannel(): " + sInsert);
        stmt.executeUpdate(sInsert);

        NodeList parameters = channel.getChildNodes();
        if (parameters != null) {
          for (int i = 0 ; i < parameters.getLength(); i++) {
            if (parameters.item(i).getNodeName().equals("parameter")) {
              Element parmElement = (Element) parameters.item(i);
              NamedNodeMap nm = parmElement.getAttributes();
              String nodeName = nm.item(0).getNodeName();
              String nodeValue = nm.item(0).getNodeValue();
              if (DEBUG > 1) System.err.println("D" +  nodeName + "=" + nodeValue);
              sInsert = "INSERT INTO UP_CHAN_PARAM (CHAN_ID, CHAN_PARM_NM, CHAN_PARM_VAL, CHAN_PARM_OVRD) VALUES ("+
               id + ",'" + nodeName + "','" + nodeValue + "','N')";
              Logger.log(Logger.DEBUG, "DBImpl::addChannel()" + sInsert);
              stmt.executeUpdate(sInsert);
            }
          }
        }

        // Commit the transaction
        commit(con);

      } catch (Exception e) {
        rollback(con);
        throw e;
      } finally {
        stmt.close();
      }

  }

  /**
   * put your documentation comment here
   * @param chanDoc
   * @param root
   * @param catID
   * @param role
   * @return
   * @exception Exception
   */
  public Element getRegistryXML (org.apache.xerces.dom.DocumentImpl chanDoc, Element root, String catID, String role) throws Exception {
    String catid = "";
    Element cat = null;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT CL.CAT_ID, CL.CAT_TITLE, CHCL.CHAN_ID "+
          "FROM UP_CATEGORY CL, UP_CHANNEL CH, UP_CAT_CHAN CHCL " +
         "WHERE CH.CHAN_ID=CHCL.CHAN_ID AND CHCL.CAT_ID=CL.CAT_ID";

        if(catID!=null) sQuery += " AND CL.CAT_ID=" + catID;

        sQuery += " ORDER BY CL.CAT_TITLE, CH.CHAN_TITLE";
        Logger.log(Logger.DEBUG, "DBImpl::getRegistryXML(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            String catnm = rs.getString(2);
            int chanId = rs.getInt(3);
            Node chan = null;
            String s = rs.getString(1);
            if (!s.equals(catid)) {
              if (catid.length() > 0) {
                root.appendChild(cat);
              }
              catid = s;
              cat = chanDoc.createElement("category");
              cat.setAttribute("ID", "cat" + catid);
              cat.setAttribute("name", catnm);
              chanDoc.putIdentifier(cat.getAttribute("ID"), cat);
            }
            Element child = createChannelNode(con, chanDoc, chanId, "xchan" + chanId);
            if (DEBUG > 3) System.err.println("channel " + child.getAttribute("name") + " has ID " + child.getAttribute("ID"));
            cat.appendChild(child);
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
    return  cat;
  }

  /**
   * put your documentation comment here
   * @param types
   * @param root
   * @param role
   * @exception Exception
   */
  public void getTypesXML (Document types, Element root, String role) throws Exception {
    String chanXML = null;
    String catid = "";
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT TYPE_NAME, TYPE_DEF_URI FROM UP_CHAN_TYPES";
        Logger.log(Logger.DEBUG, "DBImpl::getTypesXML(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            String name = rs.getString(1);
            String uri = rs.getString(2);
            Node chan = null;
            Element type = types.createElement("channelType");
            Element elem = types.createElement("name");
            elem.appendChild(types.createTextNode(name));
            type.appendChild(elem);
            elem = types.createElement("definition");
            elem.appendChild(types.createTextNode(uri));
            type.appendChild(elem);
            root.appendChild(type);
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

  /** Returns a string of XML which describes the channel categories.
   * @param role role of the current user
   */
  public void getCategoryXML (Document catsDoc, Element root, String role) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT UC.CAT_ID, UC.CAT_TITLE "+
          "FROM UP_CATEGORY UC ";

        if(role != null && !role.equals("")) {
          sQuery += ", UP_CAT_CHAN, UCC, UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR" +
            " WHERE UR.ROLE_TITLE='" + role + "' AND URC.ROLE_ID = UR.ROLE_ID AND URC.CHAN_ID = UC.CHAN_ID" +
            " AND UC.CHAN_ID = UCC.CHAN_ID AND UCC.CAT_ID = UC.CAT_ID";
        }

        sQuery += " ORDER BY UC.CAT_TITLE";
        Logger.log(Logger.DEBUG, "DBImpl::getCategoryXML(): " + sQuery);
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
   *
   *   ReferenceAuthorization
   *
   */
  /**
   * Is a user in this role
   */
  public boolean isUserInRole (int userId, String role) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      String query = "SELECT * FROM UP_USER_ROLE UUR, UP_ROLE UR, UP_USER UU " +
       "WHERE UU.USER_ID=" + userId + " UUR.USER_ID=UU.USER_ID AND UUR.ROLE_ID=UR.ROLE_ID " +
       "AND " + "UPPER(ROLE_TITLE)=UPPER('" + role + "')";
      Logger.log(Logger.DEBUG, "DBImpl::isUserInRole(): " + query);
      Statement stmt = con.createStatement();
      try {
        ResultSet rs = stmt.executeQuery(query);
        try {
          if (rs.next()) {
            return  (true);
          } else {
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
   * @exception Exception
   */
  public Vector getAllRoles () throws Exception {
    Vector roles = new Vector();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT ROLE_TITLE, ROLE_DESC FROM UP_ROLE";
        Logger.log(Logger.DEBUG, "DBImpl::getAllRolessQuery(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          RoleImpl roleImpl = null;
          // Add all of the roles in the portal database to to the vector
          while (rs.next()) {
            roleImpl = new RoleImpl(rs.getString("ROLE_TITLE"));
            roles.add(roleImpl);
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
        // Count the number of records inserted
        for (int i = 0; i < roles.size(); i++) {
          String sQuery = "SELECT ROLE_ID FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
          Logger.log(Logger.DEBUG, "DBImpl::setChannelRoles(): " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            int roleId = rs.getInt("ROLE_ID");
            String sInsert = "INSERT INTO UP_ROLE_CHAN (CHAN_ID, ROLE_ID) VALUES (" + channelID + "," + roleId + ")";
            Logger.log(Logger.DEBUG, "DBImpl::setChannelRoles(): " + sInsert);
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
   * @exception Exception
   */
  public void getChannelRoles (Vector channelRoles, int channelID) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT ROLE_TITLE, CHAN_ID FROM UP_ROLE_CHAN UCR, UP_ROLE UR, UP_CHANNEL UC " +
          "WHERE UC.CHAN_ID=" + channelID + " AND UC.CHAN_ID=URC.CHAN_ID AND URC.ROLE_ID=UR.ROLE_ID";
        Logger.log(Logger.DEBUG, "DbImpl::getChannelRoles(): " + query);
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
   * @param userId
   * @exception Exception
   */
  public void getUserRoles (Vector userRoles, int userId) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT ROLE_TITLE, USER_ID FROM UP_USER_ROLE UUR, UP_ROLE UR, UP_USER UU " +
          "WHERE UU.USER_ID=" + userId + " AND UU.USER_ID=UUR.USER_ID AND UUR.ROLE_ID=UR.ROLE_ID";
        Logger.log(Logger.DEBUG, "DbImpl::getUserRoles(): " + query);
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
   * @param userId
   * @param roles
   * @exception Exception
   */
  public void addUserRoles (int userId, Vector roles) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      try {
        int insertCount = 0;
        for (int i = 0; i < roles.size(); i++) {
          String query = "SELECT ROLE_ID, ROLE_TITLE FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
          Logger.log(Logger.DEBUG, "DbImpl::addUserRoles(): " + query);
          ResultSet rs = stmt.executeQuery(query);
          try {
            rs.next();
            int roleId = rs.getInt("ROLE_ID");
            String insert = "INSERT INTO UP_USER_ROLE (USER_ID, ROLE_ID) VALUES (" + userId + ", " + roleId + ")";
            Logger.log(Logger.DEBUG, "DbImpl::addUserRoles(): " + insert);
            insertCount = stmt.executeUpdate(insert);
            if (insertCount != 1) {
              Logger.log(Logger.ERROR, "AuthorizationBean addUserRoles(): SQL failed -> " + insert);
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
   * @param userId
   * @param roles
   * @exception Exception
   */
  public void removeUserRoles (int userId, Vector roles) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      try {
        int deleteCount = 0;
        for (int i = 0; i < roles.size(); i++) {
          String delete = "DELETE FROM UP_USER_ROLE WHERE USER_ID=" + userId + " AND ROLE_ID=" + roles.elementAt(i);
          Logger.log(Logger.DEBUG, "DbImpl::removeUserRoles(): " + delete);
          deleteCount = stmt.executeUpdate(delete);
          if (deleteCount != 1) {
            Logger.log(Logger.ERROR, "AuthorizationBean removeUserRoles(): SQL failed -> " + delete);
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
        String query = "SELECT UP_USER.USER_ID, ENCRPTD_PSWD, FIRST_NAME, LAST_NAME, EMAIL FROM UP_USER, UP_PERSON_DIR WHERE UP_USER.USER_ID = UP_PERSON_DIR.USER_ID AND "
          + "UP_USER.USER_NAME = '" + username + "'";
        Logger.log(Logger.DEBUG, "DBImpl::getUserAccountInformation()" + query);
        ResultSet rset = stmt.executeQuery(query);
        try {
          if (rset.next()) {
            acct[0] = rset.getInt("USER_ID") + "";
            acct[1] = rset.getString("ENCRPTD_PSWD");
            acct[2] = rset.getString("FIRST_NAME");
            acct[3] = rset.getString("LAST_NAME");
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
        String query = "SELECT FIRST_NAME, LAST_NAME, EMAIL FROM UP_USER, UP_PERSON_DIR "
            + "WHERE UP_USER.USER_ID = UP_PERSON_DIR.USER_ID AND "
            + "UP_USER.USER_NAME = '" + username + "'";
        Logger.log(Logger.DEBUG, "DBImpl::getUserDirectoryInformation()" + query);
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
    public synchronized int getIncrementIntegerId(String tableName) throws Exception {
      int id;
      Connection con=rdbmService.getConnection();
      try {
        Statement stmt = con.createStatement ();
        try {
          String sQuery = "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME='" + tableName + "'";
          Logger.log (Logger.DEBUG, "DBImpl::getIncrementInteger()" + sQuery);
          ResultSet rs = stmt.executeQuery (sQuery);
          try {
            rs.next();
            id = rs.getInt ("SEQUENCE_VALUE") + 1;
          } finally {
            rs.close();
          }

          String sInsert = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE="+id+" WHERE SEQUENCE_NAME='" + tableName + "'";
          Logger.log (Logger.DEBUG, "DBImpl::getIncrementInteger()" + sInsert);
          stmt.executeUpdate(sInsert);
        } finally {
          stmt.close();
        }
      } finally {
        rdbmService.releaseConnection (con);
      }
      return id;
    }

    public synchronized void createCounter(String tableName) throws Exception {
      Connection con=rdbmService.getConnection();
      try {
        Statement stmt = con.createStatement ();
        try {
            String sInsert = "INSERT INTO UP_SEQUENCE (SEQUENCE_NAME,SEQUENCE_VALUE/*/) VALUES ('"+tableName+"',0)";
            Logger.log (Logger.DEBUG, "DBImpl::createCounter()" + sInsert);
            stmt.executeUpdate (sInsert);
        } finally {
          stmt.close();
        }
      } finally {
        rdbmService.releaseConnection (con);
      }
    }

  public synchronized void setCounter(String tableName,int value) throws Exception
  {
      Connection con=rdbmService.getConnection();
      try {
          Statement stmt = con.createStatement ();
          try {
            String sUpdate = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE="+value+"WHERE SEQUENCE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, "DBImpl::setCounter()" + sUpdate);
            stmt.executeUpdate (sUpdate);
          } finally {
            stmt.close();
          }
      } finally {
          rdbmService.releaseConnection (con);
      }
  }

  static final protected void setAutoCommit(Connection connection, boolean autocommit)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
        connection.setAutoCommit(autocommit);
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  static final protected void commit(Connection connection)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
        connection.commit();
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  static final protected void rollback(Connection connection)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
        connection.rollback();
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   *   UserPreferences
   */
  public int getUserBrowserMapping (int userId, String userAgent) throws Exception {
    int profileId = 0;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT PROFILE_ID, USER_ID FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND USER_AGENT='" + userAgent
            + "'";
        Logger.log(Logger.DEBUG, "DBImpl::getUserBrowserMapping(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            profileId = rs.getInt("PROFILE_ID");
          } else {
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
   * @param userId
   * @param userAgent
   * @param profileId
   * @exception Exception
   */
  public void setUserBrowserMapping (int userId, String userAgent, int profileId) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      // remove the old mapping and add the new one
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID='" + userId + "' AND USER_AGENT='" + userAgent + "'";
        Logger.log(Logger.DEBUG, "DBImpl::setUserBrowserMapping(): " + sQuery);
        stmt.executeUpdate(sQuery);

        sQuery = "INSERT INTO UP_USER_UA_MAP (USER_ID,USER_AGENT,PROFILE_ID) VALUES (" + userId + ",'" + userAgent + "'," +
          profileId + ")";
        Logger.log(Logger.DEBUG, "DBImpl::setUserBrowserMapping(): " + sQuery);
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
   * @param userId
   * @param profileId
   * @return
   * @exception Exception
   */
  public UserProfile getUserProfileById (int userId, int profileId) throws Exception {
    UserProfile upl = null;
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILES WHERE USER_ID="
          + userId + " AND PROFILE_ID=" + profileId;
        Logger.log(Logger.DEBUG, "DBImpl::getUserProfileId(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            upl = new UserProfile(profileId, rs.getString("PROFILE_NAME"), rs.getString("DESCRIPTION"), rs.getInt("LAYOUT_ID"), rs.getInt("STRUCTURE_SS_ID"), rs.getInt("THEME_SS_ID"));
          } else {
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
   * @param userId
   * @return
   * @exception Exception
   */
  public Hashtable getUserProfileList (int userId) throws Exception {
    Hashtable pv = new Hashtable();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILES WHERE USER_ID=" + userId;
        Logger.log(Logger.DEBUG, "DBImpl::getUserProfileList(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            UserProfile upl = new UserProfile(rs.getInt("PROFILE_ID"), rs.getString("PROFILE_NAME"), rs.getString("DESCRIPTION"), rs.getInt("LAYOUT_ID"),
              rs.getInt("STRUCTURE_SS_ID"), rs.getInt("THEME_SS_ID"));
            pv.put(upl.getProfileName(), upl);
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
   * @param userId
   * @param profile
   * @exception Exception
   */
  public void setUserProfile (int userId, UserProfile profile) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // this is ugly, but we have to know wether to do INSERT or UPDATE
        String sQuery = "SELECT USER_ID, PROFILE_NAME FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
        Logger.log(Logger.DEBUG, "DBImpl::setUserProfile() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            sQuery = "UPDATE UP_USER_PROFILES SET THEME_SS_ID=" + profile.getThemeStylesheetId() + ", STRUCTURE_SS_ID="
                + profile.getStructureStylesheetId() + ", DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
                + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
          } else {
            sQuery = "INSERT INTO UP_USER_PROFILES (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION) VALUES ("
                + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
                + "," + profile.getThemeStylesheetId() + ",'" + profile.getProfileDescription() + "')";
          }
        } finally {
          rs.close();
        }
        Logger.log(Logger.DEBUG, "DBImpl::setUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

    public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (int userId, int profileId, int stylesheetId) throws Exception {
        ThemeStylesheetUserPreferences tsup;

        Connection con = rdbmService.getConnection();
        try {
            Statement stmt = con.createStatement();
            try {
                // get stylesheet description
                ThemeStylesheetDescription tsd=getThemeStylesheetDescription(stylesheetId);
                // get user defined defaults
                String sQuery = "SELECT PARAM_NAME, PARAM_VAL FROM UP_USER_SS_PARMS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2";
                Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetUserPreferences(): " + sQuery);
                ResultSet rs = stmt.executeQuery(sQuery);
                try {
                    while(rs.next()) {
                        // stylesheet param
                        tsd.setStylesheetParameterDefaultValue(rs.getString("PARAM_NAME"),rs.getString("PARAM_VAL"));
                        //			Logger.log(Logger.DEBUG,"DBImpl::getThemeStylesheetUserPreferences() :  read stylesheet param "+rs.getString("PARAM_NAME")+"=\""+rs.getString("PARAM_VAL")+"\"");
                    }
                } finally {
                    rs.close();
                }

                tsup=new ThemeStylesheetUserPreferences();
                tsup.setStylesheetId(stylesheetId);
                // fill stylesheet description with defaults
                for(Enumeration e=tsd.getStylesheetParameterNames(); e.hasMoreElements();) {
                    String pName=(String)e.nextElement();
                    tsup.putParameterValue(pName,tsd.getStylesheetParameterDefaultValue(pName));
                }
                for(Enumeration e=tsd.getChannelAttributeNames(); e.hasMoreElements();) {
                    String pName=(String)e.nextElement();
                    tsup.addChannelAttribute(pName,tsd.getChannelAttributeDefaultValue(pName));
                }


                // get user preferences
                sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE, NODE_ID FROM UP_USER_SS_ATTS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2";
                Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetUserPreferences(): " + sQuery);
                rs = stmt.executeQuery(sQuery);
                try {
                    while(rs.next()) {
                        int param_type=rs.getInt("PARAM_TYPE");
                        if(param_type==1) {
                            // stylesheet param
                            Logger.log(Logger.ERROR,"DBImpl::getThemeStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_USER_SS_ATTS is corrupt. (userId="+Integer.toString(userId)+", profileId="+Integer.toString(profileId)+", stylesheetId="+Integer.toString(stylesheetId)+", param_name=\""+rs.getString("PARAM_NAME")+"\", param_type="+Integer.toString(param_type));
                        } else if(param_type==2) {
                            // folder attribute
                            Logger.log(Logger.ERROR,"DBImpl::getThemeStylesheetUserPreferences() :  folder attribute specified for the theme stylesheet! UP_USER_SS_ATTS corrupt. (userId="+Integer.toString(userId)+", profileId="+Integer.toString(profileId)+", stylesheetId="+Integer.toString(stylesheetId)+", param_name=\""+rs.getString("PARAM_NAME")+"\", param_type="+Integer.toString(param_type));
                        } else if(param_type==3) {
                            // channel attribute
                            tsup.setChannelAttributeValue(rs.getString("NODE_ID"),rs.getString("PARAM_NAME"),rs.getString("PARAM_VAL"));
                            //Logger.log(Logger.DEBUG,"DBImpl::getThemeStylesheetUserPreferences() :  read folder attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("NODE_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
                        } else {
                            // unknown param type
                            Logger.log(Logger.ERROR,"DBImpl::getThemeStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="+Integer.toString(userId)+", profileId="+Integer.toString(profileId)+", stylesheetId="+Integer.toString(stylesheetId)+", param_name=\""+rs.getString("PARAM_NAME")+"\", param_type="+Integer.toString(param_type));
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

    public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (int userId, int profileId, int stylesheetId) throws Exception {
        StructureStylesheetUserPreferences ssup;

        Connection con = rdbmService.getConnection();
        try {
            Statement stmt = con.createStatement();
            try {
                // get stylesheet description
                StructureStylesheetDescription ssd=getStructureStylesheetDescription(stylesheetId);
                // get user defined defaults
                String sQuery = "SELECT PARAM_NAME, PARAM_VAL FROM UP_USER_SS_PARMS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1";
                Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetUserPreferences(): " + sQuery);
                ResultSet rs = stmt.executeQuery(sQuery);
                try {
                    while(rs.next()) {
                        // stylesheet param
                        ssd.setStylesheetParameterDefaultValue(rs.getString("PARAM_NAME"),rs.getString("PARAM_VAL"));
                        //Logger.log(Logger.DEBUG,"DBImpl::getStructureStylesheetUserPreferences() :  read stylesheet param "+rs.getString("PARAM_NAME")+"=\""+rs.getString("PARAM_VAL")+"\"");
                    }
                } finally {
                    rs.close();
                }

                ssup=new StructureStylesheetUserPreferences();
                ssup.setStylesheetId(stylesheetId);
                // fill stylesheet description with defaults
                for(Enumeration e=ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
                    String pName=(String)e.nextElement();
                    ssup.putParameterValue(pName,ssd.getStylesheetParameterDefaultValue(pName));
                }
                for(Enumeration e=ssd.getChannelAttributeNames(); e.hasMoreElements();) {
                    String pName=(String)e.nextElement();
                    ssup.addChannelAttribute(pName,ssd.getChannelAttributeDefaultValue(pName));
                }
                for(Enumeration e=ssd.getFolderAttributeNames(); e.hasMoreElements();) {
                    String pName=(String)e.nextElement();
                    ssup.addFolderAttribute(pName,ssd.getFolderAttributeDefaultValue(pName));
                }


                // get user preferences
                sQuery = "SELECT PARAM_NAME, PARAM_VAL, PARAM_TYPE, NODE_ID FROM UP_USER_SS_ATTS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1";
                Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetUserPreferences(): " + sQuery);
                rs = stmt.executeQuery(sQuery);
                try {
                    while(rs.next()) {
                        int param_type=rs.getInt("PARAM_TYPE");
                        if(param_type==1) {
                            // stylesheet param
                            Logger.log(Logger.ERROR,"DBImpl::getStructureStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_USER_SS_ATTS is corrupt. (userId="+Integer.toString(userId)+", profileId="+Integer.toString(profileId)+", stylesheetId="+Integer.toString(stylesheetId)+", param_name=\""+rs.getString("PARAM_NAME")+"\", param_type="+Integer.toString(param_type));
                        } else if(param_type==2) {
                            // folder attribute
                            ssup.setFolderAttributeValue(rs.getString("NODE_ID"),rs.getString("PARAM_NAME"),rs.getString("PARAM_VAL"));
                            //Logger.log(Logger.DEBUG,"DBImpl::getStructureStylesheetUserPreferences() :  read folder attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("NODE_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
                        } else if(param_type==3) {
                            // channel attribute
                            ssup.setChannelAttributeValue(rs.getString("NODE_ID"),rs.getString("PARAM_NAME"),rs.getString("PARAM_VAL"));
                            //Logger.log(Logger.DEBUG,"DBImpl::getStructureStylesheetUserPreferences() :  read channel attribute "+rs.getString("PARAM_NAME")+"("+rs.getString("NODE_ID")+")=\""+rs.getString("PARAM_VAL")+"\"");
                        } else {
                            // unknown param type
                            Logger.log(Logger.ERROR,"DBImpl::getStructureStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="+Integer.toString(userId)+", profileId="+Integer.toString(profileId)+", stylesheetId="+Integer.toString(stylesheetId)+", param_name=\""+rs.getString("PARAM_NAME")+"\", param_type="+Integer.toString(param_type));
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

    static protected final String getTextChildNodeValue(Node node) {
        if(node==null) return null;
        NodeList children=node.getChildNodes();
        for(int i=children.getLength()-1;i>=0;i--) {
            Node child=children.item(i);
            if(child.getNodeType()==Node.TEXT_NODE) return child.getNodeValue();
        }
        return null;
    }


  public void setStructureStylesheetUserPreferences (int userId, int profileId, StructureStylesheetUserPreferences ssup) throws Exception {
     Connection con = rdbmService.getConnection();
     try {
         // Set autocommit false for the connection
         int stylesheetId=ssup.getStylesheetId();
         setAutoCommit(con, false);
         Statement stmt = con.createStatement();
         try {
             // write out params
             for(Enumeration e=ssup.getParameterValues().keys(); e.hasMoreElements();) {
                 String pName=(String)e.nextElement();
                 // see if the parameter was already there
                String sQuery = "SELECT PARAM_VAL FROM UP_USER_SS_PARMS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND PARAM_NAME='"+pName+"'";
                Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
                ResultSet rs = stmt.executeQuery(sQuery);
                if(rs.next()) {
                    // update
                    sQuery="UPDATE UP_USER_SS_PARMS SET PARAM_VAL='"+ssup.getParameterValue(pName)+"' WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND PARAM_NAME='"+pName+"'";
                } else {
                    // insert
                    sQuery="INSERT INTO UP_USER_SS_PARMS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES ("+userId+","+profileId+","+stylesheetId+",1,'"+pName+"','"+ssup.getParameterValue(pName)+"')";
                }
                Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
                stmt.executeUpdate(sQuery);
             }
             // write out folder attributes
             for(Enumeration e=ssup.getFolders();e.hasMoreElements();) {
                 String folderId=(String) e.nextElement();
                 for(Enumeration attre=ssup.getFolderAttributeNames();attre.hasMoreElements();) {
                     String pName=(String)attre.nextElement();
                     String pValue=ssup.getDefinedFolderAttributeValue(folderId,pName);
                     if(pValue!=null) {
                         // store user preferences
                         String sQuery = "SELECT PARAM_VAL FROM UP_USER_SS_ATTS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND NODE_ID='"+folderId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=2";
                         Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
                         ResultSet rs = stmt.executeQuery(sQuery);
                         if(rs.next()) {
                             // update
                             sQuery="UPDATE UP_USER_SS_ATTS SET PARAM_VAL='"+pValue+"' WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND NODE_ID='"+folderId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=2";
                         } else {
                             // insert
                             sQuery="INSERT INTO UP_USER_SS_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,NODE_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("+userId+","+profileId+","+stylesheetId+",1,'"+folderId+"','"+pName+"',2,'"+pValue+"')";
                         }
                         Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
                         stmt.executeUpdate(sQuery);
                     }
                 }
             }
             // write out channel attributes
             for(Enumeration e=ssup.getChannels();e.hasMoreElements();) {
                 String channelId=(String) e.nextElement();
                 for(Enumeration attre=ssup.getChannelAttributeNames();attre.hasMoreElements();) {
                     String pName=(String)attre.nextElement();
                     String pValue=ssup.getDefinedChannelAttributeValue(channelId,pName);
                     if(pValue!=null) {
                         // store user preferences
                         String sQuery = "SELECT PARAM_VAL FROM UP_USER_SS_ATTS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND NODE_ID='"+channelId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=3";
                         Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
                         ResultSet rs = stmt.executeQuery(sQuery);
                         if(rs.next()) {
                             // update
                             sQuery="UPDATE UP_USER_SS_ATTS SET PARAM_VAL='"+pValue+"' WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=1 AND NODE_ID='"+channelId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=3";
                         } else {
                             // insert
                             sQuery="INSERT INTO UP_USER_SS_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,NODE_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("+userId+","+profileId+","+stylesheetId+",1,'"+channelId+"','"+pName+"',3,'"+pValue+"')";
                         }
                         Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences(): " + sQuery);
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

  public void setThemeStylesheetUserPreferences (int userId, int profileId, ThemeStylesheetUserPreferences tsup) throws Exception {
     Connection con = rdbmService.getConnection();
     try {
         // Set autocommit false for the connection
         int stylesheetId=tsup.getStylesheetId();
         setAutoCommit(con, false);
         Statement stmt = con.createStatement();
         try {
             // write out params
             for(Enumeration e=tsup.getParameterValues().keys(); e.hasMoreElements();) {
                 String pName=(String)e.nextElement();
                 // see if the parameter was already there
                String sQuery = "SELECT PARAM_VAL FROM UP_USER_SS_PARMS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2 AND PARAM_NAME='"+pName+"'";
                Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences(): " + sQuery);
                ResultSet rs = stmt.executeQuery(sQuery);
                if(rs.next()) {
                    // update
                    sQuery="UPDATE UP_USER_SS_PARMS SET PARAM_VAL='"+tsup.getParameterValue(pName)+"' WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2 AND PARAM_NAME='"+pName+"'";
                } else {
                    // insert
                    sQuery="INSERT INTO UP_USER_SS_PARMS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,PARAM_NAME,PARAM_VAL) VALUES ("+userId+","+profileId+","+stylesheetId+",2,'"+pName+"','"+tsup.getParameterValue(pName)+"')";
                }
                Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences(): " + sQuery);
                stmt.executeUpdate(sQuery);
             }
             // write out channel attributes
             for(Enumeration e=tsup.getChannels();e.hasMoreElements();) {
                 String channelId=(String) e.nextElement();
                 for(Enumeration attre=tsup.getChannelAttributeNames();attre.hasMoreElements();) {
                     String pName=(String)attre.nextElement();
                     String pValue=tsup.getDefinedChannelAttributeValue(channelId,pName);
                     if(pValue!=null) {
                         // store user preferences
                         String sQuery = "SELECT PARAM_VAL FROM UP_USER_SS_ATTS WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2 AND NODE_ID='"+channelId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=3";
                         Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences(): " + sQuery);
                         ResultSet rs = stmt.executeQuery(sQuery);
                         if(rs.next()) {
                             // update
                             sQuery="UPDATE UP_USER_SS_ATTS SET PARAM_VAL='"+pValue+"' WHERE USER_ID="+userId+" AND PROFILE_ID="+profileId+" AND SS_ID="+stylesheetId+" AND SS_TYPE=2 AND NODE_ID='"+channelId+"' AND PARAM_NAME='"+pName+"' AND PARAM_TYPE=3";
                         } else {
                             // insert
                             sQuery="INSERT INTO UP_USER_SS_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,NODE_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("+userId+","+profileId+","+stylesheetId+",2,'"+channelId+"','"+pName+"',3,'"+pValue+"')";
                         }
                         Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences(): " + sQuery);
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
   * @param userId
   * @param profile
   * @exception Exception
   */
  public void updateUserProfile (int userId, UserProfile profile) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "UPDATE UP_USER_PROFILES SET THEME_SS_ID=" + profile.getThemeStylesheetId() + ", STRUCTURE_SS_ID="
            + profile.getStructureStylesheetId() + ", DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
            + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
        Logger.log(Logger.DEBUG, "DBImpl::updateUserProfile() : " + sQuery);
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
   * @param userId
   * @param profile
   * @return
   * @exception Exception
   */
  public UserProfile addUserProfile (int userId, UserProfile profile) throws Exception {
    // generate an id for this profile
    Connection con = rdbmService.getConnection();
    try {
      int id = getIncrementIntegerId("UP_USER_PROFILES");
      profile.setProfileId(id);
      Statement stmt = con.createStatement();
      try {
        String sQuery = "INSERT INTO UP_USER_PROFILES (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION) VALUES ("
            + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
            + "," + profile.getThemeStylesheetId() + "','" + profile.getProfileDescription() + "')";
        Logger.log(Logger.DEBUG, "DBImpl::addUserProfile() : " + sQuery);
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
   * @param userId
   * @param profileId
   * @exception Exception
   */
  public void deleteUserProfile (int userId, int profileId) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        Logger.log(Logger.DEBUG, "DBImpl::deleteUserProfile() : " + sQuery);
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
   * @param chanDoc
   * @return
   */
  static final protected String serializeDOM (Document chanDoc) {
    StringWriter stringOut = null;
    try {
      OutputFormat format = new OutputFormat(chanDoc);          //Serialize DOM
      stringOut = new StringWriter();           //Writer will be a String
      XMLSerializer serial = new XMLSerializer(stringOut, format);
      serial.asDOMSerializer();                 // As a DOM Serializer
      serial.serialize(chanDoc.getDocumentElement());
    } catch (java.io.IOException ioe) {
      Logger.log(Logger.ERROR, ioe);
    }
    return  stringOut.toString();
    //Logger.log(Logger.DEBUG, "STRXML = " + stringOut.toString());
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
        String sQuery = "SELECT A.MIME_TYPE, A.MIME_TYPE_DESCRIPTION FROM UP_MIME_TYPES A, UP_SS_MAP B WHERE B.MIME_TYPE=A.MIME_TYPE";
        Logger.log(Logger.DEBUG, "DBImpl::getMimeTypeList() : " + sQuery);
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
    Hashtable list=new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
          String sQuery= "SELECT A.SS_ID FROM UP_STRUCT_SS A, UP_THEME_SS B WHERE B.MIME_TYPE='"+mimeType+"' AND B.STRUCT_SS_ID=A.SS_ID";
          Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetList() : " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          try {
              while (rs.next()) {
                  StructureStylesheetDescription ssd=getStructureStylesheetDescription(rs.getInt("SS_ID"));
                  if(ssd!=null)
                      list.put(new Integer(ssd.getId()),ssd);
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
    return list;
  }

  /**
   * Obtain a list of theme stylesheet descriptions for a given structure stylesheet
   * @param structureStylesheetName
   * @return a map of stylesheet names to  theme stylesheet description objects
   * @exception Exception
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId) throws Exception {
    Connection con = rdbmService.getConnection();
    Hashtable list=new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
          String sQuery= "SELECT SS_ID FROM UP_THEME_SS WHERE STRUCT_SS_ID="+structureStylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetList() : " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          try {
              while (rs.next()) {
                  ThemeStylesheetDescription tsd=getThemeStylesheetDescription(rs.getInt("SS_ID"));
                  if(tsd!=null)
                      list.put(new Integer(tsd.getId()),tsd);
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
    return list;
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
          String sQuery = "SELECT SS_ID FROM UP_THEME_SS WHERE STRUCT_SS_ID="+stylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::removeStructureStylesheetDescription() : " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          try {
              while(rs.next()) {
                  removeThemeStylesheetDescription(rs.getInt("SS_ID"));
              }
          } finally {
              rs.close();
          }

          sQuery = "DELETE FROM UP_STRUCT_SS WHERE SS_ID=" + stylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::removeStructureStylesheetDescription() : " + sQuery);
          stmt.executeUpdate(sQuery);

          // delete params
          sQuery="DELETE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::removeStructureStylesheetDescription() : " + sQuery);
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
          String sQuery = "DELETE FROM UP_THEME_SS WHERE SS_ID=" + stylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::removeThemeStylesheetDescription() : " + sQuery);
          stmt.executeUpdate(sQuery);

          // delete params
          sQuery="DELETE FROM UP_THEME_PARAMS WHERE SS_ID="+stylesheetId;
          Logger.log(Logger.DEBUG, "DBImpl::removeThemeStylesheetDescription() : " + sQuery);
          stmt.executeUpdate(sQuery);

          // clean up user preferences
          sQuery="DELETE FROM UP_USER_SS_PARMS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=2";
          Logger.log(Logger.DEBUG, "DBImpl::removeThemeStylesheetDescription() : " + sQuery);
          stmt.executeUpdate(sQuery);
          sQuery="DELETE FROM UP_USER_SS_ATTS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=2";
          Logger.log(Logger.DEBUG, "DBImpl::removeThemeStylesheetDescription() : " + sQuery);
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
   * @param userId
   * @param doc
   * @exception Exception
   */
  public void saveBookmarkXML (int userId, Document doc) throws Exception {
    StringWriter outString = new StringWriter();
    XMLSerializer xsl = new XMLSerializer(outString, new OutputFormat(doc));
    xsl.serialize(doc);

    Connection con = rdbmService.getConnection();
    try {
      Statement statem = con.createStatement();
      try {
        String sQuery = "UPDATE UPC_BOOKMARKS SET BOOKMARK_XML = '" + outString.toString() + "' WHERE PORTAL_USER_ID = " +
            userId;
        Logger.log(Logger.DEBUG, "DBImpl::saveBookmarkXML(): " + sQuery);
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
    public Integer getStructureStylesheetId(String ssName) throws Exception {
        Integer id=null;
        Connection con = rdbmService.getConnection();
        try {
            setAutoCommit(con, false);
            Statement stmt = con.createStatement();
            try {
                String sQuery = "SELECT SS_ID FROM UP_STRUCT_SS WHERE SS_NAME='"+ssName+"'";
                ResultSet rs = stmt.executeQuery(sQuery);
                if(rs.next()) {
                    id=new Integer(rs.getInt("SS_ID"));
                }
            } finally {
                stmt.close();
            }
        } finally {
            rdbmService.releaseConnection(con);
        }
        return id;
    }

    /**
     * Obtain ID for known theme stylesheet name
     * @param ssName name of the theme stylesheet
     * @return id or null if no theme matches the name given.
     */
    public Integer getThemeStylesheetId(String tsName) throws Exception {
        Integer id=null;
        Connection con = rdbmService.getConnection();
        try {
            Statement stmt = con.createStatement();
            try {
                String sQuery = "SELECT SS_ID FROM UP_THEME_SS WHERE SS_NAME='"+tsName+"'";
                ResultSet rs = stmt.executeQuery(sQuery);
                if(rs.next()) {
                    id=new Integer(rs.getInt("SS_ID"));
                }
            } finally {
                stmt.close();
            }
        } finally {
            rdbmService.releaseConnection(con);
        }
        return id;
    }

    /**
     * Remove (with cleanup) a theme stylesheet param
     * @param stylesheetId id of the theme stylesheet
     * @param pName name of the parameter
     * @param con active database connection
     */
    private void removeThemeStylesheetParam(int stylesheetId,String pName, Connection con) throws java.sql.SQLException {
        Statement stmt=con.createStatement();
        try {
            String sQuery="DELETE FROM UP_THEME_PARAMS WHERE SS_ID="+stylesheetId+" AND TYPE=1 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeThemeStylesheetParam() : "+sQuery);
            stmt.executeQuery(sQuery);
            // clean up user preference tables
            sQuery="DELETE FROM UP_USER_SS_PARMS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=2 AND PARAM_TYPE=1 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeThemeStylesheetParam() : "+sQuery);
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
    private void removeThemeChannelAttribute(int stylesheetId,String pName, Connection con) throws java.sql.SQLException {
        Statement stmt=con.createStatement();
        try {
            String sQuery="DELETE FROM UP_THEME_PARAMS WHERE SS_ID="+stylesheetId+" AND TYPE=3 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeThemeChannelAttribute() : "+sQuery);
            stmt.executeQuery(sQuery);
            // clean up user preference tables
            sQuery="DELETE FROM UP_USER_SS_ATTS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=2 AND PARAM_TYPE=3 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeThemeStylesheetParam() : "+sQuery);
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
             int stylesheetId=tsd.getId();

             String sQuery = "UPDATE UP_THEME_SS SET SS_NAME='"+ tsd.getStylesheetName() +"',SS_URI='"+ tsd.getStylesheetURI() +"',SS_DESCRIPTION_URI='"+ tsd.getStylesheetDescriptionURI()+"',SS_DESCRIPTION_TEXT='"+tsd.getStylesheetWordDescription()+"' WHERE SS_ID="+stylesheetId;
             Logger.log(Logger.DEBUG, "DBImpl::updateThemeStylesheetDescription() : " + sQuery);
             stmt.executeUpdate(sQuery);

             // first, see what was there before

             HashSet oparams=new HashSet();
             HashSet ocattrs=new HashSet();

             sQuery="SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_THEME_PARAMS WHERE SS_ID="+stylesheetId;
             Logger.log(Logger.DEBUG, "DBImpl::updateThemeStylesheetDescription() : " + sQuery);
             Statement stmtOld=con.createStatement();
             ResultSet rsOld = stmtOld.executeQuery(sQuery);
             try {
                 while(rsOld.next()) {
                     int type=rsOld.getInt("TYPE");
                     if(type==1) {
                         // stylesheet param
                         String pName=rsOld.getString("PARAM_NAME");
                         oparams.add(pName);
                         if(!tsd.containsParameterName(pName)) {
                             // delete param
                             removeThemeStylesheetParam(stylesheetId,pName,con);
                         } else {
                             // update param
                             sQuery = "UPDATE UP_THEME_PARAMS SET PARAM_DEFAULT_VAL='"+tsd.getStylesheetParameterDefaultValue(pName)+"',PARAM_DESCRIPT='"+tsd.getStylesheetParameterWordDescription(pName)+"' WHERE SS_ID=" + stylesheetId +" AND PARAM_NAME='"+pName+"' AND TYPE=1";
                             Logger.log(Logger.DEBUG, "DBImpl::updateThemeStylesheetDescription() : " + sQuery);
                             stmt.executeUpdate(sQuery);
                         }
                     } else if(type==2) {
                         Logger.log(Logger.DEBUG,"DBImpl::getThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! DB is corrupt. (stylesheetId="+stylesheetId+" param_name=\""+rsOld.getString("PARAM_NAME")+"\" type="+rsOld.getInt("TYPE")+").");
                     } else if(type==3) {
                         // channel attribute
                         String pName=rsOld.getString("PARAM_NAME");
                         ocattrs.add(pName);
                         if(!tsd.containsChannelAttribute(pName)) {
                             // delete channel attribute
                             removeThemeChannelAttribute(stylesheetId,pName,con);
                         } else {
                             // update channel attribute
                             sQuery = "UPDATE UP_THEME_PARAMS SET PARAM_DEFAULT_VAL='"+tsd.getChannelAttributeDefaultValue(pName)+"',PARAM_DESCRIPT='"+tsd.getChannelAttributeWordDescription(pName)+"' WHERE SS_ID=" + stylesheetId +" AND PARAM_NAME='"+pName+"' AND TYPE=3";
                             Logger.log(Logger.DEBUG, "DBImpl::updateThemeStylesheetDescription() : " + sQuery);
                             stmt.executeUpdate(sQuery);
                         }
                     } else {
                         Logger.log(Logger.DEBUG,"DBImpl::getThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="+stylesheetId+" param_name=\""+rsOld.getString("PARAM_NAME")+"\" type="+rsOld.getInt("TYPE")+").");
                     }
                 }
             } finally {
                 rsOld.close();
                 stmtOld.close();
             }

             // look for new attributes/parameters
             // insert all stylesheet params
             for(Enumeration e=tsd.getStylesheetParameterNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 if(!oparams.contains(pName)) {
                     sQuery = "INSERT INTO UP_THEME_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId +",'"+pName+"','"+ tsd.getStylesheetParameterDefaultValue(pName)+"','"+tsd.getStylesheetParameterWordDescription(pName)+"',1)";
                     Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                     stmt.executeUpdate(sQuery);
                 }
             }
             // insert all channel attributes
             for(Enumeration e=tsd.getChannelAttributeNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 if(!ocattrs.contains(pName)) {
                     sQuery = "INSERT INTO UP_THEME_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId +",'"+pName+"','"+ tsd.getChannelAttributeDefaultValue(pName)+"','"+tsd.getChannelAttributeWordDescription(pName)+"',3)";
                     Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
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
    private void removeStructureStylesheetParam(int stylesheetId,String pName, Connection con) throws java.sql.SQLException {
        Statement stmt=con.createStatement();
        try {
            String sQuery="DELETE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId+" AND TYPE=1 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureStylesheetParam() : "+sQuery);
            stmt.executeQuery(sQuery);
            // clean up user preference tables
            sQuery="DELETE FROM UP_USER_SS_PARMS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=1 AND PARAM_TYPE=1 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureStylesheetParam() : "+sQuery);
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
    private void removeStructureFolderAttribute(int stylesheetId,String pName, Connection con) throws java.sql.SQLException {
        Statement stmt=con.createStatement();
        try {
            String sQuery="DELETE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId+" AND TYPE=2 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureFolderAttribute() : "+sQuery);
            stmt.executeQuery(sQuery);
            // clean up user preference tables
            sQuery="DELETE FROM UP_USER_SS_ATTS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=1 AND PARAM_TYPE=2 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureFolderAttribute() : "+sQuery);
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
    private void removeStructureChannelAttribute(int stylesheetId,String pName, Connection con) throws java.sql.SQLException {
        Statement stmt=con.createStatement();
        try {
            String sQuery="DELETE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId+" AND TYPE=3 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureChannelAttribute() : "+sQuery);
            stmt.executeQuery(sQuery);
            // clean up user preference tables
            sQuery="DELETE FROM UP_USER_SS_ATTS WHERE SS_ID="+stylesheetId+" AND SS_TYPE=1 AND PARAM_TYPE=3 AND PARAM_NAME='"+pName+"'";
            Logger.log(Logger.DEBUG,"DBImpl::removeStructureChannelAttribute() : "+sQuery);
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
             int stylesheetId=ssd.getId();

             String sQuery = "UPDATE UP_STRUCT_SS SET SS_NAME='"+ ssd.getStylesheetName() +"',SS_URI='"+ ssd.getStylesheetURI() +"',SS_DESCRIPTION_URI='"+ ssd.getStylesheetDescriptionURI()+"',SS_DESCRIPTION_TEXT='"+ssd.getStylesheetWordDescription()+"' WHERE SS_ID="+stylesheetId;

             Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
             stmt.executeUpdate(sQuery);

             // first, see what was there before

             HashSet oparams=new HashSet();
             HashSet ofattrs=new HashSet();
             HashSet ocattrs=new HashSet();

             sQuery="SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId;
             Logger.log(Logger.DEBUG, "DBImpl::updateStructureStylesheetDescription() : " + sQuery);
             Statement stmtOld=con.createStatement();
             ResultSet rsOld = stmtOld.executeQuery(sQuery);
             try {
                 while(rsOld.next()) {
                     int type=rsOld.getInt("TYPE");
                     if(type==1) {
                         // stylesheet param
                         String pName=rsOld.getString("PARAM_NAME");
                         oparams.add(pName);
                         if(!ssd.containsParameterName(pName)) {
                             // delete param
                             removeStructureStylesheetParam(stylesheetId,pName,con);
                         } else {
                             // update param
                             sQuery = "UPDATE UP_STRUCT_PARAMS SET PARAM_DEFAULT_VAL='"+ssd.getStylesheetParameterDefaultValue(pName)+"',PARAM_DESCRIPT='"+ssd.getStylesheetParameterWordDescription(pName)+"' WHERE SS_ID=" + stylesheetId +" AND PARAM_NAME='"+pName+"' AND TYPE=1";
                             Logger.log(Logger.DEBUG, "DBImpl::updateStructureStylesheetDescription() : " + sQuery);
                             stmt.executeUpdate(sQuery);
                         }
                     } else if(type==2) {
                         // folder attribute
                         String pName=rsOld.getString("PARAM_NAME");
                         ofattrs.add(pName);
                         if(!ssd.containsFolderAttribute(pName)) {
                             // delete folder attribute
                             removeStructureFolderAttribute(stylesheetId,pName,con);
                         } else {
                             // update folder attribute
                             sQuery = "UPDATE UP_STRUCT_PARAMS SET PARAM_DEFAULT_VAL='"+ssd.getFolderAttributeDefaultValue(pName)+"',PARAM_DESCRIPT='"+ssd.getFolderAttributeWordDescription(pName)+"' WHERE SS_ID=" + stylesheetId +" AND PARAM_NAME='"+pName+"'AND TYPE=2";
                             Logger.log(Logger.DEBUG, "DBImpl::updateStructureStylesheetDescription() : " + sQuery);
                             stmt.executeUpdate(sQuery);
                         }
                     } else if(type==3) {
                         // channel attribute
                         String pName=rsOld.getString("PARAM_NAME");
                         ocattrs.add(pName);
                         if(!ssd.containsChannelAttribute(pName)) {
                             // delete channel attribute
                             removeStructureChannelAttribute(stylesheetId,pName,con);
                         } else {
                             // update channel attribute
                             sQuery = "UPDATE UP_STRUCT_PARAMS SET PARAM_DEFAULT_VAL='"+ssd.getChannelAttributeDefaultValue(pName)+"',PARAM_DESCRIPT='"+ssd.getChannelAttributeWordDescription(pName)+"' WHERE SS_ID=" + stylesheetId +" AND PARAM_NAME='"+pName+"' AND TYPE=3";
                             Logger.log(Logger.DEBUG, "DBImpl::updateStructureStylesheetDescription() : " + sQuery);
                             stmt.executeUpdate(sQuery);
                         }
                     } else {
                         Logger.log(Logger.DEBUG,"DBImpl::getStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="+stylesheetId+" param_name=\""+rsOld.getString("PARAM_NAME")+"\" type="+rsOld.getInt("TYPE")+").");
                     }
                 }
             } finally {
                 rsOld.close();
                 stmtOld.close();
             }

             // look for new attributes/parameters
             // insert all stylesheet params
             for(Enumeration e=ssd.getStylesheetParameterNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 if(!oparams.contains(pName)) {
                     sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId +",'"+pName+"','"+ ssd.getStylesheetParameterDefaultValue(pName)+"','"+ssd.getStylesheetParameterWordDescription(pName)+"',1)";
                     Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                     stmt.executeUpdate(sQuery);
                 }
             }

             // insert all folder attributes
             for(Enumeration e=ssd.getFolderAttributeNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 if(!ofattrs.contains(pName)) {
                     sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId +",'"+pName+"','"+ ssd.getFolderAttributeDefaultValue(pName)+"','"+ssd.getFolderAttributeWordDescription(pName)+"',2)";
                     Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                     stmt.executeUpdate(sQuery);
                 }
             }


             // insert all channel attributes
             for(Enumeration e=ssd.getChannelAttributeNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 if(!ocattrs.contains(pName)) {
                     sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + stylesheetId +",'"+pName+"','"+ ssd.getChannelAttributeDefaultValue(pName)+"','"+ssd.getChannelAttributeWordDescription(pName)+"',3)";
                     Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
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
             int id = getIncrementIntegerId("UP_STRUCT_SS");
             ssd.setId(id);
             String sQuery = "INSERT INTO UP_STRUCT_SS (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT) VALUES ("+ id + ",'"+ ssd.getStylesheetName() +"','"+ ssd.getStylesheetURI() +"','"+ ssd.getStylesheetDescriptionURI()+"','"+ssd.getStylesheetWordDescription()+"')";

             Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
             stmt.executeUpdate(sQuery);

             // insert all stylesheet params
             for(Enumeration e=ssd.getStylesheetParameterNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +",'"+pName+"','"+ ssd.getStylesheetParameterDefaultValue(pName)+"','"+ssd.getStylesheetParameterWordDescription(pName)+"',1)";
                 Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                 stmt.executeUpdate(sQuery);
             }
             // insert all folder attributes
             for(Enumeration e=ssd.getFolderAttributeNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +",'"+pName+"','"+ ssd.getFolderAttributeDefaultValue(pName)+"','"+ssd.getFolderAttributeWordDescription(pName)+"',2)";
                 Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                 stmt.executeUpdate(sQuery);
             }

             // insert all channel attributes
             for(Enumeration e=ssd.getChannelAttributeNames();e.hasMoreElements();) {
                 String pName = (String)e.nextElement();
                 sQuery = "INSERT INTO UP_STRUCT_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +",'"+pName+"','"+ ssd.getChannelAttributeDefaultValue(pName)+"','"+ssd.getChannelAttributeWordDescription(pName)+"',3)";
                 Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                 stmt.executeUpdate(sQuery);
             }


             // Commit the transaction
             commit(con);
             return new Integer(id);
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
        StructureStylesheetDescription ssd=null;
        Connection con = rdbmService.getConnection();
        Statement stmt = con.createStatement();
        try {
            String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT FROM UP_STRUCT_SS WHERE SS_ID="+stylesheetId;
            Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetDescription() : " + sQuery);
            ResultSet rs = stmt.executeQuery(sQuery);
            if(rs.next()){
                try {
                    ssd=new StructureStylesheetDescription();
                    ssd.setId(stylesheetId);
                    ssd.setStylesheetName(rs.getString("SS_NAME"));
                    ssd.setStylesheetURI(rs.getString("SS_URI"));
                    ssd.setStylesheetDescriptionURI(rs.getString("SS_DESCRIPTION_URI"));
                    ssd.setStylesheetWordDescription(rs.getString("SS_DESCRIPTION_TEXT"));
                } finally {
                    rs.close();
                }
            }
            // retreive stylesheet params and attributes
            sQuery="SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_STRUCT_PARAMS WHERE SS_ID="+stylesheetId;
            Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetDescription() : " + sQuery);
            rs = stmt.executeQuery(sQuery);
            try {
                while(rs.next()) {
                    int type=rs.getInt("TYPE");
                    if(type==1) {
                        // param
                        ssd.addStylesheetParameter(rs.getString("PARAM_NAME"),rs.getString("PARAM_DEFAULT_VAL"),rs.getString("PARAM_DESCRIPT"));
                    } else if(type==2) {
                        // folder attribute
                        ssd.addFolderAttribute(rs.getString("PARAM_NAME"),rs.getString("PARAM_DEFAULT_VAL"),rs.getString("PARAM_DESCRIPT"));
                    } else if(type==3) {
                        // channel attribute
                        ssd.addChannelAttribute(rs.getString("PARAM_NAME"),rs.getString("PARAM_DEFAULT_VAL"),rs.getString("PARAM_DESCRIPT"));
                    } else {
                        Logger.log(Logger.DEBUG,"DBImpl::getStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="+stylesheetId+" param_name=\""+rs.getString("PARAM_NAME")+"\" type="+rs.getInt("TYPE")+").");
                    }
                }
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
            rdbmService.releaseConnection(con);
        }
        return ssd;
    }

    /**
     * Obtain theme stylesheet description object for a given theme stylesheet id
     * @para id id of the theme stylesheet
     * @return theme stylesheet description
     */
    public ThemeStylesheetDescription getThemeStylesheetDescription (int stylesheetId) throws Exception {
        ThemeStylesheetDescription tsd=null;
        Connection con = rdbmService.getConnection();
        Statement stmt = con.createStatement();
        try {
            String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_ICON_URI,SAMPLE_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS FROM UP_THEME_SS WHERE SS_ID="+stylesheetId;
            Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetDescription() : " + sQuery);
            ResultSet rs = stmt.executeQuery(sQuery);
            try {
              if(rs.next()){
                    tsd=new ThemeStylesheetDescription();
                    tsd.setId(stylesheetId);
                    tsd.setStylesheetName(rs.getString("SS_NAME"));
                    tsd.setStylesheetURI(rs.getString("SS_URI"));
                    tsd.setStylesheetDescriptionURI(rs.getString("SS_DESCRIPTION_URI"));
                    tsd.setStylesheetWordDescription(rs.getString("SS_DESCRIPTION_TEXT"));
                    tsd.setStructureStylesheetId(rs.getInt("STRUCT_SS_ID"));
                    tsd.setSamplePictureURI(rs.getString("SAMPLE_URI"));
                    tsd.setSampleIconURI(rs.getString("SAMPLE_ICON_URI"));
                    tsd.setMimeType(rs.getString("MIME_TYPE"));
                    tsd.setDeviceType(rs.getString("DEVICE_TYPE"));
                    tsd.setSerializerName(rs.getString("SERIALIZER_NAME"));
                    tsd.setCustomUserPreferencesManagerClass(rs.getString("UP_MODULE_CLASS"));

              }
            } finally {
              rs.close();
            }
            // retreive stylesheet params and attributes
            sQuery="SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_THEME_PARAMS WHERE SS_ID="+stylesheetId;
            Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetDescription() : " + sQuery);
            rs = stmt.executeQuery(sQuery);
            try {
                while(rs.next()) {
                    int type=rs.getInt("TYPE");
                    if(type==1) {
                        // param
                        tsd.addStylesheetParameter(rs.getString("PARAM_NAME"),rs.getString("PARAM_DEFAULT_VAL"),rs.getString("PARAM_DESCRIPT"));
                    } else if(type==3) {
                        // channel attribute
                        tsd.addChannelAttribute(rs.getString("PARAM_NAME"),rs.getString("PARAM_DEFAULT_VAL"),rs.getString("PARAM_DESCRIPT"));
                    } else if(type==2) {
                        // folder attributes are not allowed here
                        Logger.log(Logger.ERROR,"DBImpl::getThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! Corrupted DB entry. (stylesheetId="+stylesheetId+" param_name=\""+rs.getString("PARAM_NAME")+"\" type="+rs.getInt("TYPE")+").");
                    }
                    else {
                        Logger.log(Logger.ERROR,"DBImpl::getThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="+stylesheetId+" param_name=\""+rs.getString("PARAM_NAME")+"\" type="+rs.getInt("TYPE")+").");
                    }
                }
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
            rdbmService.releaseConnection(con);
        }
        return tsd;
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
                int id = getIncrementIntegerId("UP_THEME_SS");
                tsd.setId(id);
                String sQuery = "INSERT INTO UP_THEME_SS (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_URI,SAMPLE_ICON_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS) VALUES ("+ id + ",'"+ tsd.getStylesheetName() +"','"+ tsd.getStylesheetURI() +"','"+ tsd.getStylesheetDescriptionURI()+"','"+tsd.getStylesheetWordDescription()+"',"+tsd.getStructureStylesheetId()+",'"+tsd.getSamplePictureURI()+"','"+tsd.getSampleIconURI()+"','"+tsd.getMimeType()+"','"+tsd.getDeviceType()+"','"+tsd.getSerializerName()+"','"+tsd.getCustomUserPreferencesManagerClass()+"')";

                Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);

                // insert all stylesheet params
                for(Enumeration e=tsd.getStylesheetParameterNames();e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    sQuery = "INSERT INTO UP_THEME_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +",'"+pName+"','"+ tsd.getStylesheetParameterDefaultValue(pName)+"','"+tsd.getStylesheetParameterWordDescription(pName)+"',1)";
                    Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                    stmt.executeUpdate(sQuery);
                }

                // insert all channel attributes
                for(Enumeration e=tsd.getChannelAttributeNames();e.hasMoreElements();) {
                    String pName = (String)e.nextElement();
                    sQuery = "INSERT INTO UP_THEME_PARAMS (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +",'"+pName+"','"+ tsd.getChannelAttributeDefaultValue(pName)+"','"+tsd.getChannelAttributeWordDescription(pName)+"',3)";
                    Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
                    stmt.executeUpdate(sQuery);
                }

                // Commit the transaction
                commit(con);
                return new Integer(id);
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



