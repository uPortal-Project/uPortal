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
 * Utility class for talking to the 1.x database
 * @author George Lindholm
 * @version $Revision$
 */
import  org.w3c.dom.*;
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
 * put your documentation comment here
 */
public class DBImpl
    implements IDBImpl {

  /**
   *
   * UserLayout
   *
   */
  public Document getUserLayout (int userId, int profileId) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    String str_uLayoutXML = null;
    try {
      Statement stmt = con.createStatement();
      // for now, the profileId parameter gets ignored. Need to restructure UP_USERS table to sepearate layouts, so they can be profile-specific
      String sQuery = "SELECT USER_LAYOUT_XML, ID FROM UP_USERS WHERE ID=" + userId;
      Logger.log(Logger.DEBUG, "DBImpl::getUserLayout(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        str_uLayoutXML = rs.getString("USER_LAYOUT_XML");
      }
      stmt.close();
    } finally {
      rdbmService.releaseConnection(con);
    }
    if (str_uLayoutXML != null) {
      DTDResolver userLayoutDtdResolver = new DTDResolver("userLayout.dtd");
      // read in the layout DOM
      // note that we really do need to have a DOM structure here in order to introduce
      // persistent changes on the level of userLayout.
      //org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
      org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
      parser.setEntityResolver(userLayoutDtdResolver);
      // set parser features
      parser.setFeature("http://apache.org/xml/features/validation/dynamic", true);
      parser.parse(new org.xml.sax.InputSource(new StringReader(str_uLayoutXML)));
      return  parser.getDocument();
    }
    return  null;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param layoutXML
   * @exception Exception
   */
  public void setUserLayout (int userId, int profileId, Document layoutXML) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    String sQuery = "";
    try {
      StringWriter outString = new StringWriter();
      org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat();
      format.setOmitXMLDeclaration(false);
      format.setIndenting(false);
      org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer(outString, format);
      xsl.serialize(layoutXML);
      String str_userLayoutXML = outString.toString();
      try {
        // for now, the profileName parameter gets ignored. Need to restructure UP_USERS table to sepearate layouts, so they can be profile-specific
        Statement stmt = con.createStatement();
        sQuery = "UPDATE UP_USERS SET USER_LAYOUT_XML='" + str_userLayoutXML + "' WHERE ID=" + userId;
        Logger.log(Logger.DEBUG, "DBImpl::setUserLayout(): " + sQuery);
        stmt.executeUpdate(sQuery);
        stmt.close();
      } catch (SQLException e) {
        // oracle fails if you try to process a string literal of more than 4k (sLayoutXml), so do this:
        sQuery = "UPDATE UP_USERS SET USER_LAYOUT_XML=? WHERE ID='" + userId + "'";
        Logger.log (Logger.DEBUG, "DBImpl::setUserLayout(): " +sQuery);
        PreparedStatement pstmt = con.prepareStatement (sQuery);
        pstmt.clearParameters ();
        pstmt.setCharacterStream (1, new StringReader (str_userLayoutXML), str_userLayoutXML.length ());
        int iUpdated = pstmt.executeUpdate ();
        pstmt.close ();
      }

    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   *   UserPreferences
   *
   */
  public int getUserBrowserMapping (int userId, String userAgent) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    int profileId = 0;
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT PROFILE_ID, USER_ID FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND USER_AGENT='" + userAgent
          + "'";
      Logger.log(Logger.DEBUG, "DBImpl::getUserBrowserMapping(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        profileId = rs.getInt("PROFILE_ID");
      }
      else {
        return  0;
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      // remove the old mapping and add the new one
      Statement stmt = con.createStatement();
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, STRUCTURE_SS_NAME, THEME_SS_NAME,DESCRIPTION FROM UP_USER_PROFILES WHERE USER_ID="
          + userId + " AND PROFILE_ID=" + profileId;
      Logger.log(Logger.DEBUG, "DBImpl::getUserProfileId(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        upl = new UserProfile(profileId, rs.getString("PROFILE_NAME"), rs.getString("STRUCTURE_SS_NAME"), rs.getString("THEME_SS_NAME"),
            rs.getString("DESCRIPTION"));
      }
      else {
        return  null;
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT USER_ID, PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME, THEME_SS_NAME, DESCRIPTION FROM UP_USER_PROFILES WHERE USER_ID="
          + userId;
      Logger.log(Logger.DEBUG, "DBImpl::getUserProfileList(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      while (rs.next()) {
        UserProfile upl = new UserProfile(rs.getInt("PROFILE_ID"), rs.getString("PROFILE_NAME"), rs.getString("STRUCTURE_SS_NAME"),
            rs.getString("THEME_SS_NAME"), rs.getString("DESCRIPTION"));
        pv.put(upl.getProfileName(), upl);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      // this is ugly, but we have to know wether to do INSERT or UPDATE
      String sQuery = "SELECT USER_ID, PROFILE_NAME FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profile.getProfileId();
      Logger.log(Logger.DEBUG, "DBImpl::setUserProfile() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        sQuery = "UPDATE UP_USER_PROFILES SET THEME_SS_NAME='" + profile.getThemeStylesheetName() + "', STRUCTURE_SS_NAME='"
            + profile.getStructureStylesheetName() + "', DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
            + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
        Logger.log(Logger.DEBUG, "DBImpl::setUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);
      }
      else {
        sQuery = "INSERT INTO UP_USER_PROFILES (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME,THEME_SS_NAME,DESCRIPTION) VALUES ("
            + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "','" + profile.getStructureStylesheetName()
            + "','" + profile.getThemeStylesheetName() + "','" + profile.getProfileDescription() + "')";
        Logger.log(Logger.DEBUG, "DBImpl::setUserProfile() : " + sQuery);
        stmt.executeQuery(sQuery);
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @return
   * @exception Exception
   */
  public Document getStructureStylesheetUserPreferences (int userId, int profileId, String stylesheetName) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Document upXML = null;
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT USER_ID, USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID=" + userId + " AND STYLESHEET_NAME='"
          + stylesheetName + "' AND PROFILE_ID=" + profileId;
      Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetUserPreferences() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      String str_upXML = null;
      if (rs.next()) {
        str_upXML = rs.getString("USER_PREFERENCES_XML");
      }
      if (str_upXML != null) {
        Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetUserPreferences() : " + str_upXML);
        DOMParser parser = new DOMParser();
        parser.parse(new org.xml.sax.InputSource(new StringReader(str_upXML)));
        upXML = parser.getDocument();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  upXML;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @return
   * @exception Exception
   */
  public Document getThemeStylesheetUserPreferences (int userId, int profileId, String stylesheetName) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Document upXML = null;
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT USER_ID, USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID=" + userId + " AND STYLESHEET_NAME='"
          + stylesheetName + "' AND PROFILE_ID=" + profileId;
      Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetUserPreferences() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      String str_upXML = null;
      if (rs.next())
        str_upXML = rs.getString("USER_PREFERENCES_XML");
      if (str_upXML != null) {
        Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetUserPreferences() : " + str_upXML);
        DOMParser parser = new DOMParser();
        parser.parse(new org.xml.sax.InputSource(new StringReader(str_upXML)));
        upXML = parser.getDocument();
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
    return  upXML;
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @param upXML
   * @exception Exception
   */
  public void setStructureStylesheetUserPreferences (int userId, int profileId, String stylesheetName, Document upXML) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // update the database
      StringWriter outString = new StringWriter();
      OutputFormat format = new OutputFormat(upXML);
      format.setOmitXMLDeclaration(true);
      XMLSerializer xsl = new XMLSerializer(outString, format);
      xsl.serialize(upXML);
      Statement stmt = con.createStatement();
      // this is ugly, but we have to know wether to do INSERT or UPDATE
      String sQuery = "SELECT USER_ID, USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID=" + userId + " AND STYLESHEET_NAME='"
          + stylesheetName + "' AND PROFILE_ID=" + profileId;
      Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='" + outString.toString() + "' WHERE USER_ID = " + userId
            + " AND STYLESHEET_NAME='" + stylesheetName + "' AND PROFILE_ID=" + profileId;
        Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences() : " + sQuery);
        stmt.executeUpdate(sQuery);
      }
      else {
        sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_ID,PROFILE_ID,NAME,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES (" +
            userId + "," + profileId + ",'" + stylesheetName + "','" + outString.toString() + "')";
        Logger.log(Logger.DEBUG, "DBImpl::setStructureStylesheetUserPreferences() : " + sQuery);
        stmt.executeQuery(sQuery);
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param userId
   * @param profileId
   * @param stylesheetName
   * @param upXML
   * @exception Exception
   */
  public void setThemeStylesheetUserPreferences (int userId, int profileId, String stylesheetName, Document upXML) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // update the database
      StringWriter outString = new StringWriter();
      OutputFormat format = new OutputFormat(upXML);
      format.setOmitXMLDeclaration(true);
      XMLSerializer xsl = new XMLSerializer(outString, format);
      xsl.serialize(upXML);
      Statement stmt = con.createStatement();
      // this is ugly, but we have to know wether to do INSERT or UPDATE
      String sQuery = "SELECT USER_ID, USER_PREFERENCES_XML FROM UP_USER_SS_PREFS WHERE USER_ID=" + userId + " AND STYLESHEET_NAME='"
          + stylesheetName + "' AND PROFILE_ID=" + profileId;
      Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        sQuery = "UPDATE UP_USER_SS_PREFS SET USER_PREFERENCES_XML='" + outString.toString() + "' WHERE USER_ID = " + userId
            + " AND STYLESHEET_NAME='" + stylesheetName + "' AND PROFILE_ID=" + profileId;
        Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences() : " + sQuery);
        stmt.executeUpdate(sQuery);
      }
      else {
        sQuery = "INSERT INTO UP_USER_SS_PREFS (USER_ID,PROFILE_ID,STYLESHEET_NAME,USER_PREFERENCES_XML) VALUES (" + userId
            + "," + profileId + ",'" + stylesheetName + "','" + outString.toString() + "')";
        Logger.log(Logger.DEBUG, "DBImpl::setThemeStylesheetUserPreferences() : " + sQuery);
        stmt.executeQuery(sQuery);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "UPDATE UP_USER_PROFILES SET THEME_SS_NAME='" + profile.getThemeStylesheetName() + "', STRUCTURE_SS_NAME='"
          + profile.getStructureStylesheetName() + "', DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
          + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
      Logger.log(Logger.DEBUG, "DBImpl::updateUserProfile() : " + sQuery);
      stmt.executeUpdate(sQuery);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      DBCounterImpl dbc = new DBCounterImpl();
      Integer id = dbc.getIncrementIntegerId("UP_USER_PROFILES");
      if (id == null)
        return  null;
      profile.setProfileId(id.intValue());
      Statement stmt = con.createStatement();
      String sQuery = "INSERT INTO UP_USER_PROFILES (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_NAME,THEME_SS_NAME,DESCRIPTION) VALUES ("
          + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "','" + profile.getStructureStylesheetName()
          + "','" + profile.getThemeStylesheetName() + "','" + profile.getProfileDescription() + "')";
      Logger.log(Logger.DEBUG, "DBImpl::addUserProfile() : " + sQuery);
      stmt.executeQuery(sQuery);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "DELETE FROM UP_USER_PROFILES WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
      Logger.log(Logger.DEBUG, "DBImpl::deleteUserProfile() : " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   *   ChannelRegistry
   *
   */
  public void addChannel (int id, String title, Document doc, String catID[]) throws Exception {
    Statement stmt = null;
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
        addChannel(id, title, doc);
        
      stmt = con.createStatement();

      // Set autocommit false for the connection
      setAutoCommit(con, false);

      for (int i = 0; i < catID.length; i++) {
        String sInsert = "INSERT INTO UP_CHAN_CLASS (CLASS_ID, CHAN_ID) ";
        sInsert += "VALUES (" + catID[i] + "," + id + ")";
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      stmt = con.createStatement();
      String sInsert = "INSERT INTO UP_CHANNELS (CHAN_ID, TITLE, PUB_ID, APPROVED, CHANNEL_XML) ";
      sInsert += "VALUES (" + id + ",'" + title + "',0,0," + "'" + serializeDOM(doc) + "')";
      stmt.executeUpdate(sInsert);
      Logger.log(Logger.DEBUG, "DBImpl::addChannel(): " + sInsert);
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
      Logger.log(Logger.ERROR, ioe);
    }
    return  stringOut.toString();
    //Logger.log(Logger.DEBUG, "STRXML = " + stringOut.toString());
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT CL.CLASS_ID, CL.NAME, CH.CHANNEL_XML FROM UP_CLASS CL, UP_CHANNELS CH, UP_CHAN_CLASS CHCL "
          + "WHERE CH.CHAN_ID=CHCL.CHAN_ID AND CHCL.CLASS_ID=CL.CLASS_ID";
      if (catID != null)
        sQuery += " AND CL.CLASS_ID=" + catID;
      sQuery += " ORDER BY CL.NAME, CH.TITLE";
      Logger.log(Logger.DEBUG, "DBImpl::getRegistryXML(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      while (rs.next()) {
        String catnm = rs.getString(2);
        String chxml = rs.getString(3);
        Node chan = null;
        String s = rs.getString(1);
        if (!s.equals(catid)) {
          if (catid.length() > 0)
            root.appendChild(cat);
          catid = s;
          cat = chanDoc.createElement("category");
          cat.setAttribute("ID", "cat" + catid);
          cat.setAttribute("name", catnm);
          chanDoc.putIdentifier(cat.getAttribute("ID"), cat);
        }
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.parse(new org.xml.sax.InputSource(new StringReader(chxml)));
        Document doc = parser.getDocument();
        chan = doc.getDocumentElement();
        chanDoc.putIdentifier(((Element)chan).getAttribute("ID"), (Element)chan);
        cat.appendChild(chanDoc.importNode(chan, true));
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT NAME, DEF_URI FROM UP_CHAN_TYPES";
      Logger.log(Logger.DEBUG, "DBImpl::getTypesXML(): " + sQuery);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT CL.CLASS_ID, CL.NAME FROM UP_CLASS CL ";
      if (role != null)
        sQuery += " AND ROLE=" + role;
      sQuery += " ORDER BY CL.NAME";
      Logger.log(Logger.DEBUG, "DBImpl::getCategoryXML(): " + sQuery);
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
   * CoreStyleSheet
   *
   */
  public void getMimeTypeList (Hashtable list) throws Exception {
    String sQuery = "SELECT A.MIME_TYPE, A.MIME_TYPE_DESCRIPTION FROM UP_MIME_TYPES A, UP_SS_MAP B WHERE B.MIME_TYPE=A.MIME_TYPE";
    Logger.log(Logger.DEBUG, "DBImpl::getMimeTypeList() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    try {
      ResultSet rs = stmt.executeQuery(sQuery);
      while (rs.next()) {
        list.put(rs.getString("MIME_TYPE"), rs.getString("MIME_TYPE_DESCRIPTION"));
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param mimeType
   * @param list
   * @exception Exception
   */
  public void getStructureStylesheetList (String mimeType, Hashtable list) throws Exception {
    String sQuery = "SELECT A.STYLESHEET_NAME, A.STYLESHEET_DESCRIPTION_TEXT FROM UP_STRUCT_SS A, UP_SS_MAP B WHERE B.MIME_TYPE='"
        + mimeType + "' AND B.STRUCT_SS_NAME=A.STYLESHEET_NAME";
    Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetList() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sQuery);
      while (rs.next()) {
        list.put(rs.getString("STYLESHEET_NAME"), rs.getString("STYLESHEET_DESCRIPTION_TEXT"));
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param structureStylesheetName
   * @param list
   * @exception Exception
   */
  public void getThemeStylesheetList (String structureStylesheetName, Hashtable list) throws Exception {
    String sQuery = "SELECT A.STYLESHEET_NAME, A.STYLESHEET_DESCRIPTION_TEXT FROM UP_THEME_SS A, UP_SS_MAP B WHERE B.STRUCT_SS_NAME='"
        + structureStylesheetName + "' AND A.STYLESHEET_NAME=B.THEME_SS_NAME";
    Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetList() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(sQuery);
      while (rs.next()) {
        list.put(rs.getString("STYLESHEET_NAME"), rs.getString("STYLESHEET_DESCRIPTION_TEXT"));
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @return
   * @exception Exception
   */
  public String[] getStructureStylesheetDescription (String stylesheetName) throws Exception {
    String sQuery = "SELECT * FROM UP_STRUCT_SS WHERE STYLESHEET_NAME='" + stylesheetName + "'";
    Logger.log(Logger.DEBUG, "DBImpl::getStructureStylesheetDescription() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(sQuery);
    rs.next();
    // retreive database values
    String[] db = new String[] {
      null, null, null, null
    };
    db[0] = rs.getString("STYLESHEET_NAME");
    db[1] = rs.getString("STYLESHEET_DESCRIPTION_TEXT");
    db[2] = rs.getString("STYLESHEET_URI");
    db[3] = rs.getString("STYLESHEET_DESCRIPTION_URI");
    return  db;
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @return
   * @exception Exception
   */
  public String[] getThemeStylesheetDescription (String stylesheetName) throws Exception {
    String sQuery = "SELECT * FROM UP_THEME_SS WHERE STYLESHEET_NAME='" + stylesheetName + "'";
    Logger.log(Logger.DEBUG, "DBImpl::getThemeStylesheetDescription() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    ResultSet rs = stmt.executeQuery(sQuery);
    rs.next();
    // retreive database values
    String[] db = new String[] {
      null, null, null, null
    };
    db[0] = rs.getString("STYLESHEET_NAME");
    db[1] = rs.getString("STYLESHEET_DESCRIPTION_TEXT");
    db[2] = rs.getString("STYLESHEET_URI");
    db[3] = rs.getString("STYLESHEET_DESCRIPTION_URI");
    return  db;
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @exception Exception
   */
  public void removeStructureStylesheetDescription (String stylesheetName) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      // note that we don't delete from UP_THEME_SS_MAP table.
      // Information contained in that table belongs to theme-stage stylesheet. Let them fix it.
      String sQuery = "DELETE FROM UP_STRUCT_SS WHERE STYLESHEET_NAME='" + stylesheetName + "'; DELETE FROM UP_SS_MAP WHERE STRUC_SS_NAME='"
          + stylesheetName + "';";
      Logger.log(Logger.DEBUG, "DBImpl::removeStructureStylesheetDescription() : " + sQuery);
      stmt.executeUpdate(sQuery);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param stylesheetName
   * @exception Exception
   */
  public void removeThemeStylesheetDescription (String stylesheetName) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "DELETE FROM UP_THEME_SS WHERE STYLESHEET_NAME='" + stylesheetName + "'; DELETE FROM UP_SS_MAP WHERE THEME_SS_NAME='"
          + stylesheetName + "';";
      Logger.log(Logger.DEBUG, "DBImpl::removeThemeStylesheetDescription() : " + sQuery);
      stmt.executeUpdate(sQuery);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param xmlStylesheetName
   * @param stylesheetURI
   * @param stylesheetDescriptionURI
   * @param xmlStylesheetDescriptionText
   * @exception Exception
   */
  public void addStructureStylesheetDescription (String xmlStylesheetName, String stylesheetURI, String stylesheetDescriptionURI,
      String xmlStylesheetDescriptionText) throws Exception {
    String sQuery = "INSERT INTO UP_STRUCT_SS (STYLESHEET_NAME, STYLESHEET_URI, STYLESHEET_DESCRIPTION_URI, STYLESHEET_DESCRIPTION_TEXT) VALUES ('"
        + xmlStylesheetName + "','" + stylesheetURI + "','" + stylesheetDescriptionURI + "','" + xmlStylesheetDescriptionText
        + "')";
    Logger.log(Logger.DEBUG, "DBImpl::addStructureStylesheetDescription() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Statement stmt = con.createStatement();
    try {
      stmt.executeUpdate(sQuery);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param xmlStylesheetName
   * @param stylesheetURI
   * @param stylesheetDescriptionURI
   * @param xmlStylesheetDescriptionText
   * @param mimeType
   * @param enum
   * @exception Exception
   */
  public void addThemeStylesheetDescription (String xmlStylesheetName, String stylesheetURI, String stylesheetDescriptionURI,
      String xmlStylesheetDescriptionText, String mimeType, Enumeration enum) throws Exception {
    String sQuery = "INSERT INTO UP_THEME_SS (STYLESHEET_NAME,STYLESHEET_URI,STYLESHEET_DESCRIPTION_URI,STYLESHEET_DESCRIPTION_TEXT) VALUES ('"
        + xmlStylesheetName + "','" + stylesheetURI + "','" + stylesheetDescriptionURI + "','" + xmlStylesheetDescriptionText
        + "')";
    Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();

    // Set autocommit false for the connection
    setAutoCommit(con, false);

    try {
      Statement stmt = con.createStatement();
      stmt.executeUpdate(sQuery);
      while (enum.hasMoreElements()) {
        String ssName = (String)enum.nextElement();
        sQuery = "INSERT INTO UP_SS_MAP (THEME_SS_NAME,STRUCT_SS_NAME,MIME_TYPE) VALUES ('" + xmlStylesheetName + "','" +
            ssName + "','" + mimeType + "');";
        Logger.log(Logger.DEBUG, "DBImpl::addThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
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
   * CBookmarks
   *
   *
   */
  public Document getBookmarkXML (int userId) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      String sQuery = "SELECT PORTAL_USER_ID, BOOKMARK_XML FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID=" + userId;
      Logger.log(Logger.DEBUG, "DBImpl::getBookmarkXML(): " + sQuery);
      ResultSet statem = con.createStatement().executeQuery(sQuery);
      DOMParser domP = new DOMParser();
      String inputXML;
      if (statem.next()) {
        inputXML = statem.getString("BOOKMARK_XML");
      }
      else {
        sQuery = "SELECT UP_USERS.USER_NAME, UPC_BOOKMARKS.BOOKMARK_XML FROM UPC_BOOKMARKS, UP_USERS WHERE UP_USERS.USER_NAME = 'system' AND UP_USERS.ID = UPC_BOOKMARKS.PORTAL_USER_ID";
        Logger.log(Logger.DEBUG, "DBImpl::getBookmarkXML(): " + sQuery);
        statem = con.createStatement().executeQuery(sQuery);
        statem.next();
        inputXML = statem.getString("BOOKMARK_XML");
        Statement cstate = con.createStatement();
        sQuery = "INSERT INTO UPC_BOOKMARKS VALUES ('" + userId + "','" + userId + "','" + inputXML + "')";
        Logger.log(Logger.DEBUG, "DBImpl::getBookmarkXML(): " + sQuery);
        cstate.executeQuery(sQuery);
      }
      domP.parse(new InputSource(new StringReader(inputXML)));
      return  domP.getDocument();
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      StringWriter outString = new StringWriter();
      XMLSerializer xsl = new XMLSerializer(outString, new OutputFormat(doc));
      xsl.serialize(doc);
      Statement statem = con.createStatement();
      String sQuery = "UPDATE UPC_BOOKMARKS SET BOOKMARK_XML = '" + outString.toString() + "' WHERE PORTAL_USER_ID = " +
          userId;
      Logger.log(Logger.DEBUG, "DBImpl::saveBookmarkXML(): " + sQuery);
      statem.executeUpdate(sQuery);
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   *
   *   ReferenceAuthorization
   *
   */
  public boolean isUserInRole (int userId, String role) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      String query = "SELECT * FROM UP_USER_ROLES WHERE ID=" + userId + "') AND " + "UPPER(ROLE)=UPPER('" + role +
          "')";
      Logger.log(Logger.DEBUG, "DBImpl::isUserInRole(): " + query);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    Vector roles = new Vector();
    try {
      Statement stmt = con.createStatement();
      String sQuery = "SELECT ROLE, DESCR FROM UP_ROLES";
      Logger.log(Logger.DEBUG, "DBImpl::getAllRolessQuery(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      RoleImpl roleImpl = null;
      // Add all of the roles in the portal database to to the vector
      while (rs.next()) {
        roleImpl = new RoleImpl(rs.getString("ROLE"));
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      // Count the number of records inserted
      int recordsInserted = 0;
      for (int i = 0; i < roles.size(); i++) {
        String sInsert = "INSERT INTO UP_CHAN_ROLES (CHAN_ID, ROLE) VALUES ('" + channelID + "','" + roles.elementAt(i) +
            "')";
        Logger.log(Logger.DEBUG, "DBImpl::setChannelRoles(): " + sInsert);
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
   * put your documentation comment here
   * @param channelRoles
   * @param channelID
   * @exception Exception
   */
  public void getChannelRoles (Vector channelRoles, int channelID) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String query = "SELECT ROLE, CHAN_ID FROM UP_CHAN_ROLES WHERE CHAN_ID='" + channelID + "'";
      Logger.log(Logger.DEBUG, "DBImpl::getChannelRoles(): " + query);
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        channelRoles.addElement(rs.getString("ROLE"));
      }
    } finally {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * put your documentation comment here
   * @param userRoles
   * @param userId
   * @exception Exception
   */
  public void getUserRoles (Vector userRoles, int userId) throws Exception {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      Statement stmt = con.createStatement();
      String query = "SELECT ROLE, ID FROM UP_USER_ROLES WHERE ID='" + userId + "'";
      Logger.log(Logger.DEBUG, "DBImpl::getUserRoles(): " + query);
      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        userRoles.addElement(rs.getString("ROLE"));
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      int insertCount = 0;
      for (int i = 0; i < roles.size(); i++) {
        String insert = "INSERT INTO UP_USER_ROLES (ID, ROLE) VALUES (" + userId + ", " + roles.elementAt(i) + ")";
        Logger.log(Logger.DEBUG, "DBImpl::addUserRoles(): " + insert);
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
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    try {
      // Set autocommit false for the connection
      setAutoCommit(con, false);

      Statement stmt = con.createStatement();
      int deleteCount = 0;
      for (int i = 0; i < roles.size(); i++) {
        String delete = "DELETE FROM UP_USER_ROLES WHERE ID=" + userId + " AND ROLE=" + roles.elementAt(i);
        Logger.log(Logger.DEBUG, "DBImpl::removeUserRoles(): " + delete);
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
    String query = "SELECT ID, FIRST_NAME, LAST_NAME, UP_SHADOW.PASSWORD " + "FROM UP_USERS, UP_SHADOW WHERE " + "UP_USERS.USER_NAME = UP_SHADOW.USER_NAME AND "
        + "UP_USERS.USER_NAME = ?";
    RdbmServices rdbmService = new RdbmServices();
    Connection con = rdbmService.getConnection();
    PreparedStatement stmt = null;
    ResultSet rset = null;
    try {
      stmt = con.prepareStatement(query);
      stmt.setString(1, username);
      rset = stmt.executeQuery();
      if (rset.next()) {
        acct[3] = rset.getString("LAST_NAME");
        acct[2] = rset.getString("FIRST_NAME");
        acct[1] = rset.getString("PASSWORD");
        acct[0] = rset.getInt("ID") + "";
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



