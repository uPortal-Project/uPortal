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

/**
 * SQL interface for the 2.x relational model
 * @author George Lindholm
 * @version $Revision$
 */
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
 *
 */
public class DBImpl2
    extends DBImpl implements IDBImpl {

    static int DEBUG = 0;
  /*
   * This class is instantiated ONCE so NO class variables can be used
   */

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

    private void addChannelHeaderAttribute(String name, int value, Element channel, Element system)
    {
      addChannelHeaderAttribute(name, value+"", channel, system);
    }
    private void addChannelHeaderAttribute(String name, String value, Element channel, Element system)
    {
      channel.setAttribute(name, value);
      system.setAttribute("H"+name, ""); // Tag as not being changeable
    }
    private void addChannelHeaderAttributeFlag(String name, String value, Element channel, Element system)
    {
      addChannelHeaderAttribute(name, (value != null && value.equals("Y") ? "true" : "false"), channel, system);
    }
    private Element createChannelNode(Connection con, DocumentImpl doc, int chanId) throws java.sql.SQLException
    {
      Element channel = null;
      String sQuery = "SELECT * FROM UP_CHANNEL WHERE CHAN_ID=" + chanId;
      Logger.log (Logger.DEBUG, sQuery);

      ResultSet rs = null;
      Statement stmt = con.createStatement ();
      try {
        rs = stmt.executeQuery (sQuery);
        if (rs.next()) {
          String chanTitle = rs.getString("CHAN_TITLE");
          String chanDesc = rs.getString("CHAN_DESC");
          String chanClass = rs.getString("CHAN_CLASS");
          int chanPupblUsrId = rs.getInt("CHAN_PUBL_ID");
          java.sql.Timestamp chanPublDt = rs.getTimestamp("CHAN_PUBL_DT");
          int chanApvlId = rs.getInt("CHAN_APVL_ID");
          java.sql.Timestamp chanApvlDt = rs.getTimestamp("CHAN_APVL_DT");
          String chanIdTag = rs.getString("CHAN_ID_TAG");
          int chanPriority = rs.getInt("CHAN_PRIORITY");
          int chanTimeout = rs.getInt("CHAN_TIMEOUT");
          String chanMinimizable = rs.getString("CHAN_MINIMIZABLE");
          String chanEditable = rs.getString("CHAN_EDITABLE");
          String chanHasHelp = rs.getString("CHAN_HAS_HELP");
          String chanHasAbout = rs.getString("CHAN_HAS_ABOUT");
          String chanRemovable = rs.getString("CHAN_REMOVABLE");
          String chanDetachable = rs.getString("CHAN_DETACHABLE");
          String chanName = rs.getString("CHAN_NAME");
          rs.close();
          stmt.close();

          channel = doc.createElement("channel");
          Element system = doc.createElement("system");

          doc.putIdentifier(chanIdTag, channel);
          addChannelHeaderAttribute("ID", chanIdTag, channel, system);
          channel.setAttribute("chanID", chanId + "");
          system.setAttribute("chanID", chanId + "");
          system.setAttribute("HchanID", chanId + ""); // Tag as not being changeable
          if (DEBUG > 1) System.err.println("channel " + chanName + "@" + chanId + " has tag " + chanIdTag);
          addChannelHeaderAttribute("name", chanName, channel, system);
          addChannelHeaderAttribute("class", chanClass, channel, system);
          addChannelHeaderAttribute("timeout", chanTimeout, channel, system);
          addChannelHeaderAttribute("priority", chanPriority, channel, system);
          addChannelHeaderAttributeFlag("minimizable", chanMinimizable, channel, system);
          addChannelHeaderAttributeFlag("editable", chanEditable, channel, system);
          addChannelHeaderAttributeFlag("hasHelp", chanHasHelp, channel, system);
          addChannelHeaderAttributeFlag("hasAbout", chanHasAbout, channel, system);
          addChannelHeaderAttributeFlag("removable", chanRemovable, channel, system);
          addChannelHeaderAttributeFlag("detachable", chanDetachable, channel, system);

          sQuery = "SELECT * FROM UP_CHAN_PARAM WHERE CHAN_ID=" + chanId;
          Logger.log (Logger.DEBUG, sQuery);
          stmt = con.createStatement ();
          rs = stmt.executeQuery (sQuery);
            while(rs.next()) {
              String chanHDInd = rs.getString("CHAN_H_D_IND");
              String chanParmNM = rs.getString("CHAN_PARM_NM");
              String chanParmVal = rs.getString("CHAN_PARM_VAL");

              if (chanHDInd.equals("H")) {
                channel.setAttribute(chanParmNM, chanParmVal);
                String override = rs.getString("CHAN_PARM_OVRD");
                if (override != null && override.equals("N")) {
                  system.setAttribute("H"+chanParmNM, "");
                }
              } else if (chanHDInd.equals("D")) {
                Element parameter = doc.createElement("parameter");
                parameter.setAttribute("name", chanParmNM);
                parameter.setAttribute("value", chanParmVal);
                String override = rs.getString("CHAN_PARM_OVRD");
                if (override != null && override.equals("N")) {
                  system.setAttribute("D"+chanParmNM, "");
                }
                channel.appendChild(parameter);
              } else {
                throw new SQLException("Invalid value for CHAN_H_D_IND for channel " + chanId);
              }
            }
          channel.appendChild(system);
        }
      } finally {
        stmt.close();
        if (rs != null) {
          rs.close();
        }
      }
      return channel;
    }
    private static final NamedNodeMap findSystemNamedNodeMap(Element node, String tag)
    {
      NodeList nl = node.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        if (nl.item(i).getNodeName().equals(tag)) {
          return nl.item(i).getAttributes();
        }
      }
      return null;
    }

    private static final Element findSystemNode(Node node)
    {
      NodeList nl = node.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
        if (nl.item(i).getNodeName().equals("system")) {
          return (Element) nl.item(i);
        }
      }
      return null;
    }

    private void createLayout(Connection con, DocumentImpl doc, Element root, int userId, int profileId, int structId) throws java.sql.SQLException
    {

      if (structId == 0) { // End of line
        return;
      }

      Statement stmt = con.createStatement ();
      ResultSet rs;
      int nextStructId;
      int chldStructId;
      int priority;
      int externalId;
      String idTag;
      String name;
      String type;
      String hidden;
      String removable;
      String immutable;
      try {
        String sQuery = "SELECT * FROM UP_LAYOUT_STRUCT WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + profileId + " AND STRUCT_ID=" + structId;
        Logger.log (Logger.DEBUG, sQuery);
        rs = stmt.executeQuery (sQuery);

        rs.next();
        nextStructId = rs.getInt("NEXT_STRUCT_ID");
        chldStructId = rs.getInt("CHLD_STRUCT_ID");
        externalId = rs.getInt("EXTERNAL_ID");
        priority = rs.getInt("PRIORITY");
        idTag = rs.getString("ID_TAG");
        name = rs.getString("NAME");
        type = rs.getString("TYPE");
        hidden = rs.getString("HIDDEN");
        removable = rs.getString("REMOVABLE");
        immutable = rs.getString("IMMUTABLE");
      } finally {
        stmt.close();
      }

      String sQuery = "SELECT CHAN_ID FROM UP_USER_CHAN WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + profileId + " AND STRUCT_ID="+structId;
      Logger.log (Logger.DEBUG, sQuery);
      stmt = con.createStatement ();
      ResultSet rs2 = stmt.executeQuery (sQuery);
      if (rs2.next()) { // Channel
        int chanId = rs2.getInt("CHAN_ID");

        /* See if we have access to the channel */
        sQuery = "SELECT UC.CHAN_ID FROM UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR, UP_USER_ROLE UUR " +
          "WHERE UUR.USER_ID=" + userId + " AND UC.CHAN_ID=" + chanId +" AND UUR.ROLE_ID=UR.ROLE_ID AND UR.ROLE_ID=URC.ROLE_ID AND URC.CHAN_ID=UC.CHAN_ID";
        Logger.log (Logger.DEBUG, sQuery);
        ResultSet rs3 = stmt.executeQuery (sQuery);
        if (!rs3.next()) {
          /* No access to channel. Replace it with the error channel and a suitable message */

          /* !!!!!!!   Add code here someday !!!!!!!!!!!*/
          Logger.log(Logger.DEBUG, "No role access (ignored) for channel " + chanId + " for user " + userId);

        }

        Element channel = createChannelNode(con, doc, chanId);
        stmt = con.createStatement ();
        try {
          Element system = (Element) channel.getElementsByTagName("system").item(0);
          Element parameter = (Element) channel.getElementsByTagName("parameter").item(0);

          sQuery = "SELECT * FROM UP_STRUCT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + profileId + " AND STRUCT_ID=" + structId;
          Logger.log (Logger.DEBUG, sQuery);
          rs2 = stmt.executeQuery (sQuery);
          while (rs2.next ()) {
            String foldHDInd = rs2.getString("STRUCT_H_D_IND");
            String paramName = rs2.getString("STRUCT_PARM_NM");
            if (foldHDInd.equals("H")) {
              if (!system.hasAttribute("H" + paramName)) {
                channel.setAttribute(paramName, rs2.getString("STRUCT_PARM_VAL"));
              }
            } else if (foldHDInd.equals("D")) {
              if (!system.hasAttribute("D" + paramName)) {
                parameter.setAttribute(paramName, rs2.getString("STRUCT_PARM_VAL"));
              }
            } else {
              throw new SQLException("Invalid value for PARAM_H_D_IND for channel " + chanId);
            }
          }
        } finally {
         stmt.close();
        }

        root.appendChild(channel);
      } else { // Folder
          stmt = con.createStatement ();
          try {
            Element folder = doc.createElement("folder");
            Element system = doc.createElement("system");
            if (idTag == null) {
              Logger.log(Logger.ERROR, "No tag for " + name);
            }
            doc.putIdentifier(idTag, folder);
            addChannelHeaderAttribute("ID", idTag, folder, system);
            addChannelHeaderAttribute("priority", priority, folder, system);
            addChannelHeaderAttribute("name", name, folder, system);
            addChannelHeaderAttribute("type", (type != null ? type : "regular"), folder, system);
            addChannelHeaderAttribute("hidden", (hidden != null && hidden.equals("Y") ? "true" : "false"), folder, system);
            addChannelHeaderAttribute("immutable", (immutable == null || immutable.equals("Y") ? "true" : "false"), folder, system);
            addChannelHeaderAttribute("removable", (removable == null || removable.equals("Y") ? "true" : "false"), folder, system);
            sQuery = "SELECT * FROM UP_STRUCT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + profileId + " AND STRUCT_ID=" + structId;
            Logger.log (Logger.DEBUG, sQuery);
            rs2 = stmt.executeQuery (sQuery);
            while (rs2.next ()) {
              String foldHDInd = rs2.getString("STRUCT_H_D_IND");
              String paramName = rs2.getString("STRUCT_PARM_NM");
              folder.setAttribute(paramName, rs2.getString("STRUCT_PARM_VAL"));
            }
            root.appendChild(folder);
            createLayout(con, doc, folder, userId, profileId, chldStructId);
            folder.appendChild(system);
          } finally {
            stmt.close();
          }
      }

      createLayout(con, doc, root, userId, profileId, nextStructId);
    }

  /**
   *
   * UserLayout
   *
   */
  public Document getUserLayout (int userId, int profileId) throws Exception {
    Connection con = rdbmService.getConnection();
    String str_uLayoutXML = null;
    try {
      con = rdbmService.getConnection ();

      DocumentImpl doc = new DocumentImpl();
      Element root = doc.createElement("layout");

      Statement stmt = con.createStatement ();
      try {
        String selectString = "USER_ID=" + userId + " AND LAYOUT_ID=" + profileId;
        String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE " + selectString;
        Logger.log (Logger.DEBUG, sQuery);
        ResultSet rs = stmt.executeQuery (sQuery);

        if (rs.next ()) {
          int structId = rs.getInt("INIT_STRUCT_ID");
          if (structId == 0) {      // Grab the default "Guest" layout
            structId = 1;
          }
          createLayout(con, doc, root, userId, profileId, structId);
        }

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
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param layoutXML
   * @exception Exception
   */
  public void setUserLayout (int userId, int profileId, Document layoutXML) throws Exception {
    Connection con = rdbmService.getConnection();
    try {
          setAutoCommit(con, false); // Need an atomic update here

          String selectString = "USER_ID=" + userId + " AND LAYOUT_ID=" + profileId;

          Statement stmt = con.createStatement ();
          try {
            String sQuery = "DELETE UP_USER_CHAN WHERE " + selectString;
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate(sQuery);

            sQuery = "DELETE UP_STRUCT_PARAM WHERE " + selectString;
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate(sQuery);

            sQuery = "DELETE UP_LAYOUT_STRUCT WHERE " + selectString;
            Logger.log (Logger.DEBUG, sQuery);
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
          Logger.log(Logger.ERROR,e);
        } finally {
          rdbmService.releaseConnection (con);
        }
  }

  private class StructId {
    public int id = 1;
  }

  /**
   * convert true/false int Y/N for database
   * @param value to check
   * @result Y/N
   */
  private static String dbBool(String value)
  {
    if (value != null && value.equals("true")) {
      return "Y";
    } else {
      return "N";
    }
  }

  private int saveStructure(Node node, Statement stmt, int userId, int layoutId, StructId structId) throws java.sql.SQLException {
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
      sQuery = "INSERT INTO UP_LAYOUT_STRUCT " +
      "(USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID,EXTERNAL_ID,ID_TAG,NAME,TYPE,HIDDEN,IMMUTABLE,REMOVABLE) VALUES (" +
        userId + "," + layoutId + "," + saveStructId + "," + nextStructId + "," + childStructId + "," +
        "'" + structure.getAttribute("external_id") + "','" + structure.getAttribute("ID") + "'," +
        "'" + structure.getAttribute("name") + "','" + structure.getAttribute("type") + "'," +
        "'" + dbBool(structure.getAttribute("hidden")) + "','" + dbBool(structure.getAttribute("immutable")) + "'," +
        "'" + dbBool(structure.getAttribute("removable")) + "')";
      Logger.log(Logger.DEBUG, sQuery);
      stmt.execute(sQuery);

      Element system = findSystemNode(node);
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
            sQuery = "INSERT INTO UP_STRUCT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL, STRUCT_H_D_IND) VALUES ("+
              userId + "," + layoutId + "," + saveStructId + ",'" + nodeName + "','" + nm.item(i).getNodeValue() + "','" + structHDInd + "')";
            Logger.log(Logger.DEBUG, sQuery);
            stmt.execute(sQuery);
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
                sQuery = "INSERT INTO UP_STRUCT_PARAM (USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL, STRUCT_H_D_IND) VALUES ("+
                  userId + "," + layoutId + "," + saveStructId + ",'" + nodeName + "','" + nodeValue + "','" + structHDInd + "')";
                Logger.log(Logger.DEBUG, sQuery);
                stmt.execute(sQuery);
              }
            }
          }
      }

      if (node.getNodeName().equals("channel")) {
        sQuery = "INSERT INTO UP_USER_CHAN (USER_ID, LAYOUT_ID, STRUCT_ID, CHAN_ID) VALUES (" +
          userId + "," + layoutId + "," + saveStructId + "," + system.getAttribute("chanID") + ")";
        Logger.log(Logger.DEBUG, sQuery);
        stmt.execute(sQuery);
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
    Statement stmt = null;
    Connection con = rdbmService.getConnection();
    try {
        addChannel(id, title, doc);

      stmt = con.createStatement();

      // Set autocommit false for the connection
      setAutoCommit(con, false);

      for (int i = 0; i < catID.length; i++) {
        String sInsert = "INSERT INTO UP_CAT_CHAN (CHAN_ID, CAT_ID) VALUES (" + id + "," + catID[i]  + ")";
        Logger.log(Logger.DEBUG, "DBImpl2::addChannel(): " + sInsert);
        stmt.executeUpdate(sInsert);
      }

      // Commit the transaction
      commit(con);
    } catch (Exception e) {

      // Roll back the transaction
      rollback(con);

      throw  e;
    } finally {
      try {
        if (stmt != null)
          stmt.close();
      } catch (SQLException ex) {
        Logger.log(Logger.ERROR, ex);
      }
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
    Statement stmt = null;
    Connection con = rdbmService.getConnection();
    Element channel = (Element)doc.getFirstChild();

    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);
      stmt = con.createStatement();
      String sInsert = "INSERT INTO UP_CHANNEL (CHAN_ID, CHAN_TITLE, CHAN_DESC, CHAN_CLASS, " +
        "CHAN_PUBL_ID, CHAN_PUBL_DT, CHAN_APVL_ID, CHAN_APVL_DT, CHAN_ID_TAG, CHAN_PRIORITY, CHAN_TIMEOUT, " +
        "CHAN_MINIMIZABLE, CHAN_EDITABLE, CHAN_HAS_HELP, CHAN_HAS_ABOUT, CHAN_REMOVABLE, CHAN_DETACHABLE, CHAN_NAME) ";
      sInsert += "VALUES (" + id + ",'" + title + "','" + title + " Channel','" + channel.getAttribute("class") + "'," +
        "0,SYSDATE,0,SYSDATE" +
        ",'" + channel.getAttribute("ID") + "'," + "'" + channel.getAttribute("priority") + "'" +
        ",'" + channel.getAttribute("timeout") + "'," + "'" + dbBool(channel.getAttribute("minimizable")) + "'" +
        ",'" + dbBool(channel.getAttribute("editable")) + "'" +
        ",'" + dbBool(channel.getAttribute("hasHelp")) + "'," + "'" + dbBool(channel.getAttribute("hasAbout")) + "'" +
        ",'" + dbBool(channel.getAttribute("removable")) + "'," +"'" + dbBool(channel.getAttribute("detachable")) + "'" +
        ",'" + channel.getAttribute("name") + "')";
      Logger.log(Logger.DEBUG, "DBImpl2::addChannel(): " + sInsert);
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
            sInsert = "INSERT INTO UP_CHAN_PARAM (CHAN_ID, CHAN_PARM_NM, CHAN_PARM_VAL, CHAN_H_D_IND, CHAN_PARM_OVRD) VALUES ("+
             id + ",'" + nodeName + "','" + nodeValue + "','D','N')";
            Logger.log(Logger.DEBUG, sInsert);
            stmt.executeUpdate(sInsert);
          }
        }
      }

      commit(con);
    } catch (Exception e) {
      rollback(con);
      Logger.log(Logger.ERROR, e);
    } finally {
      try {
        if (stmt != null)
          stmt.close();
      } catch (SQLException ex) {
        Logger.log(Logger.ERROR, ex);
      }
      rdbmService.releaseConnection(con);
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
      String sQuery = "SELECT CL.CAT_ID, CL.CAT_TITLE, CHCL.CHAN_ID "+
        "FROM UP_CATEGORY CL, UP_CHANNEL CH, UP_CAT_CHAN CHCL " +
        "WHERE CH.CHAN_ID=CHCL.CHAN_ID AND CHCL.CAT_ID=CL.CAT_ID";

      if(catID!=null) sQuery += " AND CL.CAT_ID=" + catID;

      sQuery += " ORDER BY CL.CAT_TITLE, CH.CHAN_TITLE";
      Logger.log(Logger.DEBUG, "DBImpl2::getRegistryXML(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
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
        Element child = createChannelNode(con, chanDoc, chanId);
        if (DEBUG > 3) System.err.println("channel " + child.getAttribute("name") + " has ID " + child.getAttribute("ID"));
        cat.appendChild(child);

      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    DBImpl2.dumpDoc(cat, "- ");
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
      String sQuery = "SELECT TYPE_NAME, TYPE_DEF_URI FROM UP_CHAN_TYPES";
      Logger.log(Logger.DEBUG, "DBImpl2::getTypesXML(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
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
      String sQuery = "SELECT UC.CAT_ID, UC.CAT_TITLE "+
        "FROM UP_CATEGORY UC ";

      if(role != null && !role.equals("")) {
        sQuery += ", UP_CAT_CHAN, UCC, UP_CHANNEL UC, UP_ROLE_CHAN URC, UP_ROLE UR" +
          " WHERE UR.ROLE_TITLE='" + role + "' AND URC.ROLE_ID = UR.ROLE_ID AND URC.CHAN_ID = UC.CHAN_ID" +
          " AND UC.CHAN_ID = UCC.CHAN_ID AND UCC.CAT_ID = UC.CAT_ID";
      }

      sQuery += " ORDER BY UC.CAT_TITLE";
      Logger.log(Logger.DEBUG, "DBImpl2::getCategoryXML(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
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
      Logger.log(Logger.DEBUG, "DBImpl2::isUserInRole(): " + query);
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        return  (true);
      }
      else {
        return  (false);
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
    Connection con = rdbmService.getConnection();
    Vector roles = new Vector();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT ROLE_TITLE, ROLE_DESC FROM UP_ROLE";
      Logger.log(Logger.DEBUG, "DBImpl2::getAllRolessQuery(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      RoleImpl roleImpl = null;
      // Add all of the roles in the portal database to to the vector
      while (rs.next()) {
        roleImpl = new RoleImpl(rs.getString("ROLE_TITLE"));
        roles.add(roleImpl);
      }
      stmt.close();
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

      Statement stmt = con.createStatement();
      // Count the number of records inserted
      int recordsInserted = 0;
      for (int i = 0; i < roles.size(); i++) {
        String sQuery = "SELECT ROLE_ID FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
        Logger.log(Logger.DEBUG, "DBImpl2::setChannelRoles(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        rs.next();
        int roleId = rs.getInt("ROLE_ID");
        String sInsert = "INSERT INTO UP_ROLE_CHAN (CHAN_ID, ROLE_ID) VALUES (" + channelID + "," + roleId + ")";
        Logger.log(Logger.DEBUG, "DBImpl2::setChannelRoles(): " + sInsert);
        recordsInserted += stmt.executeUpdate(sInsert);
      }
      stmt.close();

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
      String query = "SELECT ROLE_TITLE, CHAN_ID FROM UP_ROLE_CHAN UCR, UP_ROLE UR, UP_CHANNEL UC " +
        "WHERE UC.CHAN_ID=" + channelID + " AND UC.CHAN_ID=URC.CHAN_ID AND URC.ROLE_ID=UR.ROLE_ID";
      Logger.log(Logger.DEBUG, "DBImpl2::getChannelRoles(): " + query);
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        channelRoles.addElement(rs.getString("ROLE_TITLE"));
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
      String query = "SELECT ROLE_TITLE, USER_ID FROM UP_USER_ROLE UUR, UP_ROLE UR, UP_USER UU " +
        "WHERE UU.USER_ID=" + userId + " AND UU.USER_ID=UUR.USER_ID AND UUR.ROLE_ID=UR.ROLE_ID";
      Logger.log(Logger.DEBUG, "DBImpl2::getUserRoles(): " + query);
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        userRoles.addElement(rs.getString("ROLE_TITLE"));
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
      int insertCount = 0;
      for (int i = 0; i < roles.size(); i++) {
        String query = "SELECT ROLE_ID, ROLE_TITLE FROM UP_ROLE WHERE ROLE_TITLE = '" + roles.elementAt(i) + "'";
        Logger.log(Logger.DEBUG, "DBImpl2::addUserRoles(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int roleId = rs.getInt("ROLE_ID");
        String insert = "INSERT INTO UP_USER_ROLE (USER_ID, ROLE_ID) VALUES (" + userId + ", " + roleId + ")";
        Logger.log(Logger.DEBUG, "DBImpl2::addUserRoles(): " + insert);
        insertCount = stmt.executeUpdate(insert);
        if (insertCount != 1) {
          Logger.log(Logger.ERROR, "AuthorizationBean addUserRoles(): SQL failed -> " + insert);
        }
      }

      // Commit the transaction
      commit(con);
    } catch (Exception e) {
      // Roll back the transaction
      rollback(con);
      throw  e;
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
      int deleteCount = 0;
      for (int i = 0; i < roles.size(); i++) {
        String delete = "DELETE FROM UP_USER_ROLE WHERE USER_ID=" + userId + " AND ROLE_ID=" + roles.elementAt(i);
        Logger.log(Logger.DEBUG, "DBImpl2::removeUserRoles(): " + delete);
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
    String query = "SELECT UP_USER.USER_ID, ENCRPTD_PSWD FROM UP_USER, UP_SHADOW WHERE UP_USER.USER_ID = UP_SHADOW.USER_ID AND "
        + "UP_USER.USER_NAME = '" + username + "'";
    Logger.log(Logger.ERROR, query);
    Connection con = rdbmService.getConnection();
    Statement stmt = null;
    ResultSet rset = null;
    try {
      stmt = con.createStatement();
      rset = stmt.executeQuery(query);
      if (rset.next()) {
       acct[1] = rset.getString("ENCRPTD_PSWD");
       acct[0] = rset.getInt("USER_ID") + "";
      }
    } finally {
      try {
        rset.close();
      } catch (Exception e) {}
      try {
        stmt.close();
      } catch (Exception e) {}
      rdbmService.releaseConnection(con);
    }
    return  acct;
  }

  /* DBCounter */

  /*
   * get&increment method.
   */
    public synchronized int getIncrementIntegerId(String tableName) throws Exception {
        Connection con=null;
        int id;
        try {
            con=rdbmService.getConnection();
            Statement stmt = con.createStatement ();

            String sQuery = "SELECT SEQUENCE_VALUE FROM UP_SEQUENCE WHERE SEQUENCE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
            rs.next();
            id = rs.getInt ("SEQUENCE_VALUE") + 1;
            sQuery = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE="+id+" WHERE SEQUENCE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate(sQuery);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return id;
    }

    public synchronized void createCounter(String tableName) throws Exception {
        Connection con=null;
        try {
            con=rdbmService.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "INSERT INTO UP_SEQUENCE ('SEQUENCE_NAME,SEQUENCE_VALUE') VALUES ('"+tableName+"',0)";
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }

    public synchronized void setCounter(String tableName,int value) throws Exception {
        Connection con=null;
        try {
            con=rdbmService.getConnection();
            Statement stmt = con.createStatement ();
            String sQuery = "UPDATE UP_SEQUENCE SET SEQUENCE_VALUE="+value+"WHERE SEQUENCE_NAME='" + tableName + "'";
            Logger.log (Logger.DEBUG, sQuery);
            stmt.executeUpdate (sQuery);
        } finally {
            rdbmService.releaseConnection (con);
        }
    }


  private void setAutoCommit(Connection connection, boolean autocommit)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
      {
        connection.setAutoCommit(autocommit);
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  private void commit(Connection connection)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
      {
        connection.commit();
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  private void rollback(Connection connection)
  {
    try
    {
      if(connection.getMetaData().supportsTransactions())
      {
        connection.rollback();
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }
}



