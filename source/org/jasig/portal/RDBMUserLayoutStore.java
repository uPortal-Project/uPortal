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
 */

package org.jasig.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.jasig.portal.channels.CError;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.CounterStoreFactory;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ICounterStore;
import org.jasig.portal.utils.IPortalDocument;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * SQL implementation for the 2.x relational database model
 * @author George Lindholm
 * @version $Revision$
 */
public class RDBMUserLayoutStore implements IUserLayoutStore {

  //This class is instantiated ONCE so NO class variables can be used to keep state between calls
  protected static int DEBUG = 0;
  protected static final String channelPrefix = "n";
  protected static final String folderPrefix = "s";
  protected IChannelRegistryStore crs;
  protected ICounterStore csdb;
  // I18n propertiy
  protected static final boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware");

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
    String locale;

    /**
     *
     */
    public LayoutStructure(int structId, int nextId,int childId,int chanId, String hidden, String unremovable, String immutable) {
      this.nextId = nextId;
      this.childId = childId;
      this.chanId = chanId;
      this.structId = structId;
      this.hidden = RDBMServices.dbFlag(hidden);
      this.immutable = RDBMServices.dbFlag(immutable);
      this.unremovable = RDBMServices.dbFlag(unremovable);

      if (DEBUG > 1) {
        System.err.println("New layout: id=" + structId + ", next=" + nextId + ", child=" + childId +", chan=" +chanId);
      }
    }

    public LayoutStructure(int structId, int nextId,int childId,int chanId, String hidden, String unremovable, String immutable, String locale) {
      this.nextId = nextId;
      this.childId = childId;
      this.chanId = chanId;
      this.structId = structId;
      this.hidden = RDBMServices.dbFlag(hidden);
      this.immutable = RDBMServices.dbFlag(immutable);
      this.unremovable = RDBMServices.dbFlag(unremovable);
      this.locale = locale; // for i18n by Shoji

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

    public Element getStructureDocument(Document doc) throws Exception {
      Element structure = null;
      if (isChannel()) {
        ChannelDefinition channelDef = crs.getChannelDefinition(chanId);
        if (channelDef != null && channelApproved(channelDef.getApprovalDate())) {
            if (localeAware) {
                channelDef.setLocale(locale); // for i18n by Shoji
            }
          structure = channelDef.getDocument(doc, channelPrefix + structId);
        } else {
          // Create an error channel if channel is missing or not approved
          ChannelDefinition cd = new ChannelDefinition(chanId);
          cd.setTitle("Missing channel");
          cd.setName("Missing channel");
          cd.setTimeout(20000);
          String missingChannel = "Unknown";
          if (channelDef != null) {
            missingChannel = channelDef.getName();
          }
          structure = cd.getDocument(doc, channelPrefix + structId,
           "The '" + missingChannel + "' channel is no longer available. Please remove it from your layout.",
           CError.CHANNEL_MISSING_EXCEPTION);
        }
      } else {
        structure = doc.createElement("folder");
        ((IPortalDocument)doc).putIdentifier(folderPrefix+structId, structure);
        structure.setAttribute("ID", folderPrefix + structId);
        structure.setAttribute("name", name);
        structure.setAttribute("type", (type != null ? type : "regular"));
      }

      structure.setAttribute("hidden", (hidden ? "true" : "false"));
      structure.setAttribute("immutable", (immutable ? "true" : "false"));
      structure.setAttribute("unremovable", (unremovable ? "true" : "false"));
      if (localeAware) {
          structure.setAttribute("locale", locale);  // for i18n by Shoji
      }

      if (parameters != null) {
        for (int i = 0; i < parameters.size(); i++) {
          StructureParameter sp = (StructureParameter)parameters.get(i);

          if (!isChannel()) {        // Folder
            structure.setAttribute(sp.name, sp.value);
          } else {                    // Channel
            NodeList nodeListParameters = structure.getElementsByTagName("parameter");
            for (int j = 0; j < nodeListParameters.getLength(); j++) {
              Element parmElement = (Element)nodeListParameters.item(j);
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

  public RDBMUserLayoutStore () throws Exception {
    crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
    csdb = CounterStoreFactory.getCounterStoreImpl();
    if (RDBMServices.supportsOuterJoins) {
      if (RDBMServices.joinQuery instanceof RDBMServices.JdbcDb) {
        RDBMServices.joinQuery.addQuery("layout",
          "{oj UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID} WHERE");
        RDBMServices.joinQuery.addQuery("ss_struct", "{oj UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID} WHERE");
        RDBMServices.joinQuery.addQuery("ss_theme", "{oj UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID} WHERE");
      } else if (RDBMServices.joinQuery instanceof RDBMServices.PostgreSQLDb) {
         RDBMServices.joinQuery.addQuery("layout",
          "UP_LAYOUT_STRUCT ULS LEFT OUTER JOIN UP_LAYOUT_PARAM USP ON ULS.USER_ID = USP.USER_ID AND ULS.STRUCT_ID = USP.STRUCT_ID WHERE");
        RDBMServices.joinQuery.addQuery("ss_struct", "UP_SS_STRUCT USS LEFT OUTER JOIN UP_SS_STRUCT_PAR USP ON USS.SS_ID=USP.SS_ID WHERE");
        RDBMServices.joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS LEFT OUTER JOIN UP_SS_THEME_PARM UTP ON UTS.SS_ID=UTP.SS_ID WHERE");
     } else if (RDBMServices.joinQuery instanceof RDBMServices.OracleDb) {
        RDBMServices.joinQuery.addQuery("layout",
          "UP_LAYOUT_STRUCT ULS, UP_LAYOUT_PARAM USP WHERE ULS.STRUCT_ID = USP.STRUCT_ID(+) AND ULS.USER_ID = USP.USER_ID(+) AND");
        RDBMServices.joinQuery.addQuery("ss_struct", "UP_SS_STRUCT USS, UP_SS_STRUCT_PAR USP WHERE USS.SS_ID=USP.SS_ID(+) AND");
        RDBMServices.joinQuery.addQuery("ss_theme", "UP_SS_THEME UTS, UP_SS_THEME_PARM UTP WHERE UTS.SS_ID=UTP.SS_ID(+) AND");
      } else {
        throw new Exception("Unknown database driver");
      }
    }
  }

  /**
   * Registers a NEW structure stylesheet with the database.
   * @param ssd the Stylesheet description object
   * @return an <code>Integer</code> id for the registered Stylesheet description object
   */
  public Integer addStructureStylesheetDescription (StructureStylesheetDescription ssd) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // we assume that this is a new stylesheet.
        int id = csdb.getIncrementIntegerId("UP_SS_STRUCT");
        ssd.setId(id);
        String sQuery = "INSERT INTO UP_SS_STRUCT (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT) VALUES ("
            + id + ",'" + ssd.getStylesheetName() + "','" + ssd.getStylesheetURI() + "','" + ssd.getStylesheetDescriptionURI()
            + "','" + ssd.getStylesheetWordDescription() + "')";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addStructureStylesheetDescription(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // insert all stylesheet params
        for (Enumeration e = ssd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getStylesheetParameterDefaultValue(pName) + "','" + ssd.getStylesheetParameterWordDescription(pName)
              + "',1)";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addStructureStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all folder attributes
        for (Enumeration e = ssd.getFolderAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getFolderAttributeDefaultValue(pName) + "','" + ssd.getFolderAttributeWordDescription(pName)
              + "',2)";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addStructureStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all channel attributes
        for (Enumeration e = ssd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_STRUCT_PAR (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id
              + ",'" + pName + "','" + ssd.getChannelAttributeDefaultValue(pName) + "','" + ssd.getChannelAttributeWordDescription(pName)
              + "',3)";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addStructureStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // Commit the transaction
        RDBMServices.commit(con);
        return  new Integer(id);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Registers a NEW theme stylesheet with the database.
   * @param tsd Stylesheet description object
   * @return an <code>Integer</code> id of the registered Theme Stylesheet if successful;
   *                 <code>null</code> otherwise.
   */
  public Integer addThemeStylesheetDescription (ThemeStylesheetDescription tsd) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // we assume that this is a new stylesheet.
        int id = csdb.getIncrementIntegerId("UP_SS_THEME");
        tsd.setId(id);
        String sQuery = "INSERT INTO UP_SS_THEME (SS_ID,SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_URI,SAMPLE_ICON_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS) VALUES ("
            + id + ",'" + tsd.getStylesheetName() + "','" + tsd.getStylesheetURI() + "','" + tsd.getStylesheetDescriptionURI()
            + "','" + tsd.getStylesheetWordDescription() + "'," + tsd.getStructureStylesheetId() + ",'" + tsd.getSamplePictureURI()
            + "','" + tsd.getSampleIconURI() + "','" + tsd.getMimeType() + "','" + tsd.getDeviceType() + "','" + tsd.getSerializerName()
            + "','" + tsd.getCustomUserPreferencesManagerClass() + "')";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // insert all stylesheet params
        for (Enumeration e = tsd.getStylesheetParameterNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +
              ",'" + pName + "','" + tsd.getStylesheetParameterDefaultValue(pName) + "','" + tsd.getStylesheetParameterWordDescription(pName)
              + "',1)";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // insert all channel attributes
        for (Enumeration e = tsd.getChannelAttributeNames(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          sQuery = "INSERT INTO UP_SS_THEME_PARM (SS_ID,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE) VALUES (" + id +
              ",'" + pName + "','" + tsd.getChannelAttributeDefaultValue(pName) + "','" + tsd.getChannelAttributeWordDescription(pName)
              + "',3)";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        // Commit the transaction
        RDBMServices.commit(con);
        return  new Integer(id);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Update the theme stylesheet description.
   * @param stylesheetDescriptionURI
   * @param stylesheetURI
   * @param stylesheetId
   * @return true if update succeeded, otherwise false
   */
  public boolean updateThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId) {
    try {
      Document stylesheetDescriptionXML = getDOM(stylesheetDescriptionURI);
      String ssName = this.getRootElementTextValue(stylesheetDescriptionXML, "parentStructureStylesheet");
      // should thrown an exception
      if (ssName == null)
        return  false;
      // determine id of the parent structure stylesheet
      Integer ssId = getStructureStylesheetId(ssName);
      // stylesheet not found, should thrown an exception here
      if (ssId == null)
        return  false;
      ThemeStylesheetDescription sssd = new ThemeStylesheetDescription();
      sssd.setId(stylesheetId);
      sssd.setStructureStylesheetId(ssId.intValue());
      String xmlStylesheetName = this.getName(stylesheetDescriptionXML);
      String xmlStylesheetDescriptionText = this.getDescription(stylesheetDescriptionXML);
      sssd.setStylesheetName(xmlStylesheetName);
      sssd.setStylesheetURI(stylesheetURI);
      sssd.setStylesheetDescriptionURI(stylesheetDescriptionURI);
      sssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
      sssd.setMimeType(this.getRootElementTextValue(stylesheetDescriptionXML, "mimeType"));
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : setting mimetype=\""
          + sssd.getMimeType() + "\"");
      sssd.setSerializerName(this.getRootElementTextValue(stylesheetDescriptionXML, "serializer"));
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : setting serializerName=\""
          + sssd.getSerializerName() + "\"");
      sssd.setCustomUserPreferencesManagerClass(this.getRootElementTextValue(stylesheetDescriptionXML, "userPreferencesModuleClass"));
      sssd.setSamplePictureURI(this.getRootElementTextValue(stylesheetDescriptionXML, "samplePictureURI"));
      sssd.setSampleIconURI(this.getRootElementTextValue(stylesheetDescriptionXML, "sampleIconURI"));
      sssd.setDeviceType(this.getRootElementTextValue(stylesheetDescriptionXML, "deviceType"));
      // populate parameter and attriute tables
      this.populateParameterTable(stylesheetDescriptionXML, sssd);
      this.populateChannelAttributeTable(stylesheetDescriptionXML, sssd);
      updateThemeStylesheetDescription(sssd);
    } catch (Exception e) {
      LogService.log(LogService.DEBUG, e);
      return  false;
    }
    return  true;
  }

  /**
   * Update the structure stylesheet description
   * @param stylesheetDescriptionURI
   * @param stylesheetURI
   * @param stylesheetId
   * @return true if update succeeded, otherwise false
   */
  public boolean updateStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI, int stylesheetId) {
    try {
      Document stylesheetDescriptionXML = getDOM(stylesheetDescriptionURI);
      StructureStylesheetDescription fssd = new StructureStylesheetDescription();
      String xmlStylesheetName = this.getName(stylesheetDescriptionXML);
      String xmlStylesheetDescriptionText = this.getDescription(stylesheetDescriptionXML);
      fssd.setId(stylesheetId);
      fssd.setStylesheetName(xmlStylesheetName);
      fssd.setStylesheetURI(stylesheetURI);
      fssd.setStylesheetDescriptionURI(stylesheetDescriptionURI);
      fssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);

      // populate parameter and attriute tables
      this.populateParameterTable(stylesheetDescriptionXML, fssd);
      this.populateFolderAttributeTable(stylesheetDescriptionXML, fssd);
      this.populateChannelAttributeTable(stylesheetDescriptionXML, fssd);

      // now write out the database record
      updateStructureStylesheetDescription(fssd);

    } catch (Exception e) {
      LogService.log(LogService.DEBUG, e);
      return  false;
    }
    return  true;
  }

  /**
   * Add a structure stylesheet description
   * @param stylesheetDescriptionURI
   * @param stylesheetURI
   * @return an <code>Integer</code> id of the registered Structure Stylesheet description object if successful;
   *                      <code>null</code> otherwise.
   */
  public Integer addStructureStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI) {
    // need to read in the description file to obtain information such as name, word description and media list
    try {
      Document stylesheetDescriptionXML = getDOM(stylesheetDescriptionURI);
      StructureStylesheetDescription fssd = new StructureStylesheetDescription();
      String xmlStylesheetName = this.getName(stylesheetDescriptionXML);
      String xmlStylesheetDescriptionText = this.getDescription(stylesheetDescriptionXML);
      fssd.setStylesheetName(xmlStylesheetName);
      fssd.setStylesheetURI(stylesheetURI);
      fssd.setStylesheetDescriptionURI(stylesheetDescriptionURI);
      fssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);

      // populate parameter and attriute tables
      this.populateParameterTable(stylesheetDescriptionXML, fssd);
      this.populateFolderAttributeTable(stylesheetDescriptionXML, fssd);
      this.populateChannelAttributeTable(stylesheetDescriptionXML, fssd);

      // now write out the database record
      // first the basic record
      //UserLayoutStoreFactory.getUserLayoutStoreImpl().addStructureStylesheetDescription(xmlStylesheetName, stylesheetURI, stylesheetDescriptionURI, xmlStylesheetDescriptionText);
      return  addStructureStylesheetDescription(fssd);

    } catch (Exception e) {
      LogService.log(LogService.DEBUG, e);
    }
    return  null;
  }

  /**
   * Add theme stylesheet description
   * @param stylesheetDescriptionURI
   * @param stylesheetURI
   * @return an <code>Integer</code> id of the registered Theme Stylesheet if successful;
   *                 <code>null</code> otherwise.
   */
  public Integer addThemeStylesheetDescription (String stylesheetDescriptionURI, String stylesheetURI) {
    // need to read iN the description file to obtain information such as name, word description and mime type list
    try {
      Document stylesheetDescriptionXML = getDOM(stylesheetDescriptionURI);
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription() : stylesheet name = " + this.getName(stylesheetDescriptionXML));
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription() : stylesheet description = " + this.getDescription(stylesheetDescriptionXML));
      String ssName = this.getRootElementTextValue(stylesheetDescriptionXML, "parentStructureStylesheet");
      // should thrown an exception
      if (ssName == null)
        return  null;
      // determine id of the parent structure stylesheet
      Integer ssId = getStructureStylesheetId(ssName);
      // stylesheet not found, should thrown an exception here
      if (ssId == null)
        return  null;
      ThemeStylesheetDescription sssd = new ThemeStylesheetDescription();
      sssd.setStructureStylesheetId(ssId.intValue());
      String xmlStylesheetName = this.getName(stylesheetDescriptionXML);
      String xmlStylesheetDescriptionText = this.getDescription(stylesheetDescriptionXML);
      sssd.setStylesheetName(xmlStylesheetName);
      sssd.setStylesheetURI(stylesheetURI);
      sssd.setStylesheetDescriptionURI(stylesheetDescriptionURI);
      sssd.setStylesheetWordDescription(xmlStylesheetDescriptionText);
      sssd.setMimeType(this.getRootElementTextValue(stylesheetDescriptionXML, "mimeType"));
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription() : setting mimetype=\""
          + sssd.getMimeType() + "\"");
      sssd.setSerializerName(this.getRootElementTextValue(stylesheetDescriptionXML, "serializer"));
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addThemeStylesheetDescription() : setting serializerName=\""
          + sssd.getSerializerName() + "\"");
      sssd.setCustomUserPreferencesManagerClass(this.getRootElementTextValue(stylesheetDescriptionXML, "userPreferencesModuleClass"));
      sssd.setSamplePictureURI(this.getRootElementTextValue(stylesheetDescriptionXML, "samplePictureURI"));
      sssd.setSampleIconURI(this.getRootElementTextValue(stylesheetDescriptionXML, "sampleIconURI"));
      sssd.setDeviceType(this.getRootElementTextValue(stylesheetDescriptionXML, "deviceType"));

      // populate parameter and attriute tables
      this.populateParameterTable(stylesheetDescriptionXML, sssd);
      this.populateChannelAttributeTable(stylesheetDescriptionXML, sssd);

      return  addThemeStylesheetDescription(sssd);

    } catch (Exception e) {
      LogService.log(LogService.DEBUG, e);
    }
    return  null;
  }

  /**
   * Add a user profile
   * @param person
   * @param profile
   * @return userProfile
   * @exception Exception
   */
  public UserProfile addUserProfile (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    // generate an id for this profile
    Connection con = RDBMServices.getConnection();
    try {
      int id = csdb.getIncrementIntegerId("UP_USER_PROFILE");
      profile.setProfileId(id);
      Statement stmt = con.createStatement();
      try {
        String sQuery = "INSERT INTO UP_USER_PROFILE (USER_ID,PROFILE_ID,PROFILE_NAME,STRUCTURE_SS_ID,THEME_SS_ID,DESCRIPTION, LAYOUT_ID) VALUES ("
            + userId + "," + profile.getProfileId() + ",'" + profile.getProfileName() + "'," + profile.getStructureStylesheetId()
            + "," + profile.getThemeStylesheetId() + ",'" + profile.getProfileDescription() + "', "+profile.getLayoutId()+")";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::addUserProfile(): " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  profile;
  }

  /**
   * Checks if a channel has been approved
   * @param approved Date
   * @return boolean Channel is approved
   */
   protected static boolean channelApproved(java.util.Date approvedDate) {
      java.util.Date rightNow = new java.util.Date();
      return (approvedDate != null && rightNow.after(approvedDate));
   }

  /**
   * Create a layout
   * @param layoutStructure
   * @param doc
   * @param root
   * @param structId
   * @exception java.sql.SQLException
   */
   protected final void createLayout (HashMap layoutStructure, Document doc,
        Element root, int structId) throws java.sql.SQLException, Exception {
      while (structId != 0) {
        if (DEBUG>1) {
          System.err.println("CreateLayout(" + structId + ")");
        }
        LayoutStructure ls = (LayoutStructure) layoutStructure.get(new Integer(structId));
        Element structure = ls.getStructureDocument(doc);
        root.appendChild(structure);
        if (!ls.isChannel()) {          // Folder
          createLayout(layoutStructure, doc,  structure, ls.getChildId());
        }
        structId = ls.getNextId();
      }
  }

  /**
   * convert true/false into Y/N for database
   * @param value to check
   * @result boolean
   */
  protected static final boolean xmlBool (String value) {
      return (value != null && value.equals("true") ? true : false);
  }

  public void deleteUserProfile(IPerson person, int profileId) throws Exception {
    int userId = person.getID();
    deleteUserProfile(userId,profileId);
  }

  private Document getDOM(String uri) throws Exception {
    DOMResult result = new DOMResult();
    SAXSource source = new SAXSource(new InputSource(
      ResourceLoader.getResourceAsStream(this.getClass(), uri)));
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer emptytr = tFactory.newTransformer();
    emptytr.transform(source, result);

    // need to return a Document
    Node node = result.getNode();
    if (node instanceof Document) {
      return (Document)node;
    }

    Document dom = DocumentFactory.getNewDocument();
    dom.appendChild(dom.importNode(node, true));
    return dom;
  }

  private void deleteUserProfile(int userId, int profileId) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_PROFILE WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        // remove profile mappings
        sQuery= "DELETE FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        // remove parameter information
        sQuery= "DELETE FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

        sQuery= "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + Integer.toString(profileId);
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::deleteUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);

      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
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

  /**
   *
   * CoreStyleSheet
   *
   */
  public Hashtable getMimeTypeList () throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT A.MIME_TYPE, A.MIME_TYPE_DESCRIPTION FROM UP_MIME_TYPE A, UP_SS_MAP B WHERE B.MIME_TYPE=A.MIME_TYPE";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getMimeTypeList() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          Hashtable list = new Hashtable();
          while (rs.next()) {
            list.put(rs.getString("MIME_TYPE"), rs.getString("MIME_TYPE_DESCRIPTION"));
          }
          return list;
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Return the next available channel structure id for a user
   * @param person
   * @return the next available channel structure id
   */
  public String generateNewChannelSubscribeId (IPerson person) throws Exception {
    return  getNextStructId(person, channelPrefix);
  }

  /**
   * Return the next available folder structure id for a user
   * @param person
   * @return a <code>String</code> that is the next free structure ID
   * @exception Exception
   */
  public String generateNewFolderId (IPerson person) throws Exception {
    return  getNextStructId(person, folderPrefix);
  }

  /**
   * Return the next available structure id for a user
   * @param person
   * @param prefix
   * @return next free structure ID
   * @exception Exception
   */
  protected synchronized String getNextStructId (IPerson person, String prefix) throws Exception {
    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    try {
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
        for (int i = 0; i < 25; i++) {
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getNextStructId(): " + sQuery);
          ResultSet rs = stmt.executeQuery(sQuery);
          int currentStructId;
          try {
            rs.next();
            currentStructId = rs.getInt(1);
          } finally {
            rs.close();
          }
          int nextStructId = currentStructId + 1;
          try {
            String sUpdate = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + userId + " AND NEXT_STRUCT_ID="
                + currentStructId;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getNextStructId(): " + sUpdate);
            stmt.executeUpdate(sUpdate);
            RDBMServices.commit(con);
            return  prefix + nextStructId;
          } catch (SQLException sqle) {
            RDBMServices.rollback(con);
            // Assume a concurrent update. Try again after some random amount of milliseconds.
            Thread.sleep(java.lang.Math.round(java.lang.Math.random()* 3 * 1000)); // Retry in up to 3 seconds
          }
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    throw new SQLException("Unable to generate a new structure id for user " + userId);
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
   * Obtain structure stylesheet description object for a given structure stylesheet id.
   * @param stylesheetId the id of the structure stylesheet
   * @return structure stylesheet description
   */
  public StructureStylesheetDescription getStructureStylesheetDescription (int stylesheetId) throws Exception {
    StructureStylesheetDescription ssd = null;
    Connection con = null;
    try {
      con = RDBMServices.getConnection();
      Statement stmt = con.createStatement();
      int dbOffset = 0;
      String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT";
      if (RDBMServices.supportsOuterJoins) {
        sQuery += ",TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM " + RDBMServices.joinQuery.getQuery("ss_struct");
        dbOffset = 4;
      } else {
        sQuery += " FROM UP_SS_STRUCT USS WHERE";
      }
      sQuery += " USS.SS_ID=" + stylesheetId;

      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription(): " + sQuery);
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

        if (!RDBMServices.supportsOuterJoins) {
          rs.close();
          // retrieve stylesheet params and attributes
          sQuery = "SELECT TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
        }

        while (true) {
          if (!RDBMServices.supportsOuterJoins && !rs.next()) {
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + type + ").");
          }
          if (RDBMServices.supportsOuterJoins && !rs.next()) {
            break;
          }
        }
      } finally {
        try { rs.close(); } catch (Exception e) {}
        try { stmt.close(); } catch (Exception e) {}
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  ssd;
  }

  /**
   * Obtain ID for known structure stylesheet name
   * @param ssName name of the structure stylesheet
   * @return id or null if no stylesheet matches the name given.
   */
  public Integer getStructureStylesheetId (String ssName) throws Exception {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = RDBMServices.getConnection();
      RDBMServices.setAutoCommit(con, false);
      stmt = con.createStatement();
      String sQuery = "SELECT SS_ID FROM UP_SS_STRUCT WHERE SS_NAME='" + ssName + "'";
      rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        int id = rs.getInt("SS_ID");
        if (rs.wasNull()) {
          id = 0;
        }
        return new Integer(id);
      }
    } finally {
      RDBMServices.closeResultSet(rs);
      RDBMServices.closeStatement(stmt);
      RDBMServices.releaseConnection(con);
    }
    return null;
  }

  /**
   * Obtain a list of structure stylesheet descriptions that have stylesheets for a given
   * mime type.
   * @param mimeType
   * @return a mapping from stylesheet names to structure stylesheet description objects
   */
  public Hashtable getStructureStylesheetList (String mimeType) throws Exception {
    Connection con = RDBMServices.getConnection();
    Hashtable list = new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT A.SS_ID FROM UP_SS_STRUCT A, UP_SS_THEME B WHERE B.MIME_TYPE='" + mimeType + "' AND B.STRUCT_SS_ID=A.SS_ID";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetList() : " + sQuery);
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
      RDBMServices.releaseConnection(con);
    }
    return  list;
  }

  /**
   * Obtain a list of strcture stylesheet descriptions registered on the system
   * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link StructureStylesheetDescription} objects
   * @exception Exception
   */
  public Hashtable getStructureStylesheetList() throws Exception {
      Connection con = RDBMServices.getConnection();
      Hashtable list = new Hashtable();
      try {
          Statement stmt = con.createStatement();
          try {
              String sQuery = "SELECT SS_ID FROM UP_SS_STRUCT";
                  LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetList() : " + sQuery);
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
          RDBMServices.releaseConnection(con);
      }
      return  list;
  }


  public StructureStylesheetUserPreferences getStructureStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
    int userId = person.getID();
    StructureStylesheetUserPreferences ssup;
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        ResultSet rs = null;
        
        // get stylesheet description
        StructureStylesheetDescription ssd = getStructureStylesheetDescription(stylesheetId);
        
        // get user defined defaults
        int layoutId = this.getLayoutID(userId, profileId);

        if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);
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
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            // stylesheet param
            ssd.setStylesheetParameterDefaultValue(rs.getString(1), rs.getString(2));
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
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            String temp1=rs.getString(1); // Access columns left to right
            String temp2=rs.getString(2);
            int param_type = rs.getInt(3);
            int structId = rs.getInt(4);
            if (rs.wasNull()) {
              structId = 0;
            }
            int chanId = rs.getInt(5);
            if (rs.wasNull()) {
              chanId = 0;
            }

            if (param_type == 1) {
              // stylesheet param
              LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_SS_USER_ATTS is corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + temp1 + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 2) {
              // folder attribute
              ssup.setFolderAttributeValue(getStructId(structId,chanId), temp1, temp2);
            }
            else if (param_type == 3) {
              // channel attribute
              ssup.setChannelAttributeValue(getStructId(structId,chanId), temp1, temp2);
            }
            else {
              // unknown param type
              LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getStructureStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + temp1 + "\", param_type=" + Integer.toString(param_type));
            }
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  ssup;
  }

  /**
   * Obtain theme stylesheet description object for a given theme stylesheet id.
   * @param stylesheetId the id of the theme stylesheet
   * @return theme stylesheet description
   */
  public ThemeStylesheetDescription getThemeStylesheetDescription (int stylesheetId) throws Exception {
    ThemeStylesheetDescription tsd = null;
    Connection con = null;
    try {
      con = RDBMServices.getConnection();
      Statement stmt = con.createStatement();
      int dbOffset = 0;
      String sQuery = "SELECT SS_NAME,SS_URI,SS_DESCRIPTION_URI,SS_DESCRIPTION_TEXT,STRUCT_SS_ID,SAMPLE_ICON_URI,SAMPLE_URI,MIME_TYPE,DEVICE_TYPE,SERIALIZER_NAME,UP_MODULE_CLASS";
      if (RDBMServices.supportsOuterJoins) {
        sQuery += ",TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM " + RDBMServices.joinQuery.getQuery("ss_theme");
        dbOffset = 11;
      } else {
        sQuery += " FROM UP_SS_THEME UTS WHERE";
      }
      sQuery += " UTS.SS_ID=" + stylesheetId;
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription(): " + sQuery);
      ResultSet rs = stmt.executeQuery(sQuery);
      try {
        if (rs.next()) {
          tsd = new ThemeStylesheetDescription();
          tsd.setId(stylesheetId);
          tsd.setStylesheetName(rs.getString(1));
          tsd.setStylesheetURI(rs.getString(2));
          tsd.setStylesheetDescriptionURI(rs.getString(3));
          tsd.setStylesheetWordDescription(rs.getString(4));
          int ssId = rs.getInt(5);
          if (rs.wasNull()) {
            ssId = 0;
          }
          tsd.setStructureStylesheetId(ssId);
          tsd.setSampleIconURI(rs.getString(6));
          tsd.setSamplePictureURI(rs.getString(7));
          tsd.setMimeType(rs.getString(8));
          tsd.setDeviceType(rs.getString(9));
          tsd.setSerializerName(rs.getString(10));
          tsd.setCustomUserPreferencesManagerClass(rs.getString(11));
        }

        if (!RDBMServices.supportsOuterJoins) {
          rs.close();
          // retrieve stylesheet params and attributes
          sQuery = "SELECT TYPE,PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetDescription(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
        }
        while (true) {
          if (!RDBMServices.supportsOuterJoins && !rs.next()) {
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
            LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! Corrupted DB entry. (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + type + ").");
          }
          else {
            LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                + stylesheetId + " param_name=\"" + rs.getString(dbOffset + 2) + "\" type=" + type + ").");
          }
          if (RDBMServices.supportsOuterJoins && !rs.next()) {
            break;
          }
        }
      } finally {
        try { rs.close(); } catch (Exception e) {}
        try { stmt.close(); } catch (Exception e) {}
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  tsd;
  }

  /**
   * Obtain ID for known theme stylesheet name
   * @param tsName name of the theme stylesheet
   * @return id or null if no theme matches the name given.
   */
  public Integer getThemeStylesheetId (String tsName) throws Exception {
    Integer id = null;
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = RDBMServices.getConnection();
      stmt = con.createStatement();
      String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE SS_NAME='" + tsName + "'";
      rs = stmt.executeQuery(sQuery);
      if (rs.next()) {
        id = new Integer(rs.getInt("SS_ID"));
      }
    } finally {
      RDBMServices.closeResultSet(rs);
      RDBMServices.closeStatement(stmt);
      RDBMServices.releaseConnection(con);
    }
    return  id;
  }

  /**
   * Obtain a list of theme stylesheet descriptions for a given structure stylesheet
   * @param structureStylesheetId
   * @return a map of stylesheet names to  theme stylesheet description objects
   * @exception Exception
   */
  public Hashtable getThemeStylesheetList (int structureStylesheetId) throws Exception {
    Connection con = RDBMServices.getConnection();
    Hashtable list = new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE STRUCT_SS_ID=" + structureStylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetList() : " + sQuery);
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
      RDBMServices.releaseConnection(con);
    }
    return  list;
  }

  /**
   * Obtain a list of theme stylesheet descriptions registered on the system
   * @return a <code>Hashtable</code> mapping stylesheet id (<code>Integer</code> objects) to {@link ThemeStylesheetDescription} objects
   * @exception Exception
   */
  public Hashtable getThemeStylesheetList() throws Exception {
    Connection con = RDBMServices.getConnection();
    Hashtable list = new Hashtable();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetList() : " + sQuery);
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
      RDBMServices.releaseConnection(con);
    }
    return  list;
  }

  public ThemeStylesheetUserPreferences getThemeStylesheetUserPreferences (IPerson person, int profileId, int stylesheetId) throws Exception {
    int userId = person.getID();
    ThemeStylesheetUserPreferences tsup;
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // get stylesheet description
        ThemeStylesheetDescription tsd = getThemeStylesheetDescription(stylesheetId);
        // get user defined defaults
        
        int layoutId = this.getLayoutID(userId, profileId);
        ResultSet rs;

        if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            userId = rs.getInt(1);
          } finally {
            rs.close();
          }
        }                
        
        String sQuery = "SELECT PARAM_NAME, PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID="
            + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            // stylesheet param
            tsd.setStylesheetParameterDefaultValue(rs.getString(1), rs.getString(2));
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
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences(): " + sQuery);
        rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int param_type = rs.getInt(1);
            if (rs.wasNull()) {
              param_type = 0;
            }
            int structId = rs.getInt(4);
            if (rs.wasNull()) {
              structId = 0;
            }
            int chanId = rs.getInt(5);
            if (rs.wasNull()) {
              chanId = 0;
            }
            if (param_type == 1) {
              // stylesheet param
              LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  stylesheet global params should be specified in the user defaults table ! UP_SS_USER_ATTS is corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(2) + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 2) {
              // folder attribute
              LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() :  folder attribute specified for the theme stylesheet! UP_SS_USER_ATTS corrupt. (userId="
                  + Integer.toString(userId) + ", profileId=" + Integer.toString(profileId) + ", stylesheetId=" + Integer.toString(stylesheetId)
                  + ", param_name=\"" + rs.getString(2) + "\", param_type=" + Integer.toString(param_type));
            }
            else if (param_type == 3) {
              // channel attribute
              tsup.setChannelAttributeValue(getStructId(structId,chanId), rs.getString(2), rs.getString(3));
            }
            else {
              // unknown param type
              LogService.log(LogService.ERROR, "RDBMUserLayoutStore::getThemeStylesheetUserPreferences() : unknown param type encountered! DB corrupt. (userId="
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
      RDBMServices.releaseConnection(con);
    }
    return  tsup;
  }

  // private helper modules that retreive information from the DOM structure of the description files
  private String getName (Document descr) {
    NodeList names = descr.getElementsByTagName("name");
    Node name = null;
    for (int i = names.getLength() - 1; i >= 0; i--) {
      name = names.item(i);
      if (name.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        name = null;
    }
    if (name != null) {
      return  this.getTextChildNodeValue(name);
    }
    else {
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getName() : no \"name\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private String getRootElementTextValue (Document descr, String elementName) {
    NodeList names = descr.getElementsByTagName(elementName);
    Node name = null;
    for (int i = names.getLength() - 1; i >= 0; i--) {
      name = names.item(i);

      if (name.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        name = null;
    }
    if (name != null) {
      return  this.getTextChildNodeValue(name);
    }
    else {
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getRootElementTextValue() : no \"" + elementName + "\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private String getDescription (Document descr) {
    NodeList descriptions = descr.getElementsByTagName("description");
    Node description = null;
    for (int i = descriptions.getLength() - 1; i >= 0; i--) {
      description = descriptions.item(i);
      if (description.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        description = null;
    }
    if (description != null) {
      return  this.getTextChildNodeValue(description);
    }
    else {
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getDescription() : no \"description\" element was found under the \"stylesheetdescription\" node!");
      return  null;
    }
  }

  private void populateParameterTable (Document descr, CoreStylesheetDescription csd) {
    NodeList parametersNodes = descr.getElementsByTagName("parameters");
    Node parametersNode = null;
    for (int i = parametersNodes.getLength() - 1; i >= 0; i--) {
      parametersNode = parametersNodes.item(i);
      if (parametersNode.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        parametersNode = null;
    }
    if (parametersNode != null) {
      NodeList children = parametersNode.getChildNodes();
      for (int i = children.getLength() - 1; i >= 0; i--) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("parameter")) {
          Element parameter = (Element)children.item(i);
          // process a <parameter> node
          String name = parameter.getAttribute("name");
          String description = null;
          String defaultvalue = null;
          NodeList pchildren = parameter.getChildNodes();
          for (int j = pchildren.getLength() - 1; j >= 0; j--) {
            Node pchild = pchildren.item(j);
            if (pchild.getNodeType() == Node.ELEMENT_NODE) {
              if (pchild.getNodeName().equals("defaultvalue")) {
                defaultvalue = this.getTextChildNodeValue(pchild);
              }
              else if (pchild.getNodeName().equals("description")) {
                description = this.getTextChildNodeValue(pchild);
              }
            }
          }
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::populateParameterTable() : adding a stylesheet parameter : (\""
              + name + "\",\"" + defaultvalue + "\",\"" + description + "\")");
          csd.addStylesheetParameter(name, defaultvalue, description);
        }
      }
    }
  }

  private void populateFolderAttributeTable (Document descr, StructureStylesheetDescription cxsd) {
    NodeList parametersNodes = descr.getElementsByTagName("parameters");
    NodeList folderattributesNodes = descr.getElementsByTagName("folderattributes");
    Node folderattributesNode = null;
    for (int i = folderattributesNodes.getLength() - 1; i >= 0; i--) {
      folderattributesNode = folderattributesNodes.item(i);
      if (folderattributesNode.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        folderattributesNode = null;
    }
    if (folderattributesNode != null) {
      NodeList children = folderattributesNode.getChildNodes();
      for (int i = children.getLength() - 1; i >= 0; i--) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("attribute")) {
          Element attribute = (Element)children.item(i);
          // process a <attribute> node
          String name = attribute.getAttribute("name");
          String description = null;
          String defaultvalue = null;
          NodeList pchildren = attribute.getChildNodes();
          for (int j = pchildren.getLength() - 1; j >= 0; j--) {
            Node pchild = pchildren.item(j);
            if (pchild.getNodeType() == Node.ELEMENT_NODE) {
              if (pchild.getNodeName().equals("defaultvalue")) {
                defaultvalue = this.getTextChildNodeValue(pchild);
              }
              else if (pchild.getNodeName().equals("description")) {
                description = this.getTextChildNodeValue(pchild);
              }
            }
          }
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::populateFolderAttributeTable() : adding a stylesheet folder attribute : (\""
              + name + "\",\"" + defaultvalue + "\",\"" + description + "\")");
          cxsd.addFolderAttribute(name, defaultvalue, description);
        }
      }
    }
  }

  private void populateChannelAttributeTable (Document descr, CoreXSLTStylesheetDescription cxsd) {
    NodeList channelattributesNodes = descr.getElementsByTagName("channelattributes");
    Node channelattributesNode = null;
    for (int i = channelattributesNodes.getLength() - 1; i >= 0; i--) {
      channelattributesNode = channelattributesNodes.item(i);
      if (channelattributesNode.getParentNode().getNodeName().equals("stylesheetdescription"))
        break;
      else
        channelattributesNode = null;
    }
    if (channelattributesNode != null) {
      NodeList children = channelattributesNode.getChildNodes();
      for (int i = children.getLength() - 1; i >= 0; i--) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("attribute")) {
          Element attribute = (Element)children.item(i);
          // process a <attribute> node
          String name = attribute.getAttribute("name");
          String description = null;
          String defaultvalue = null;
          NodeList pchildren = attribute.getChildNodes();
          for (int j = pchildren.getLength() - 1; j >= 0; j--) {
            Node pchild = pchildren.item(j);
            if (pchild.getNodeType() == Node.ELEMENT_NODE) {
              if (pchild.getNodeName().equals("defaultvalue")) {
                defaultvalue = this.getTextChildNodeValue(pchild);
              }
              else if (pchild.getNodeName().equals("description")) {
                description = this.getTextChildNodeValue(pchild);
              }
            }
          }
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::populateChannelAttributeTable() : adding a stylesheet channel attribute : (\""
              + name + "\",\"" + defaultvalue + "\",\"" + description + "\")");
          cxsd.addChannelAttribute(name, defaultvalue, description);
        }
      }
    }
  }

  private Vector getVectorOfSimpleTextElementValues (Document descr, String elementName) {
    Vector v = new Vector();
    // find "stylesheetdescription" node, take the first one
    Element stylesheetdescriptionElement = (Element)(descr.getElementsByTagName("stylesheetdescription")).item(0);
    if (stylesheetdescriptionElement == null) {
      LogService.log(LogService.ERROR, "Could not obtain <stylesheetdescription> element");
      return  null;
    }
    NodeList elements = stylesheetdescriptionElement.getElementsByTagName(elementName);
    for (int i = elements.getLength() - 1; i >= 0; i--) {
      v.add(this.getTextChildNodeValue(elements.item(i)));
    }
    return  v;
  }

  private String getTextChildNodeValue (Node node) {
    if (node == null)
      return  null;
    NodeList children = node.getChildNodes();
    for (int i = children.getLength() - 1; i >= 0; i--) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE)
        return  child.getNodeValue();
    }
    return  null;
  }

  /**
   *   UserPreferences
   */
  private int getUserBrowserMapping (IPerson person, String userAgent) throws Exception {
    int userId = person.getID();
    int profileId = 0;
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT PROFILE_ID, USER_ID FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND USER_AGENT='" +
            userAgent + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserBrowserMapping(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            profileId = rs.getInt("PROFILE_ID");
            if (rs.wasNull()) {
              profileId = 0;
            }
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
      RDBMServices.releaseConnection(con);
    }
    return  profileId;
  }

  public Document getUserLayout (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    int realUserId = userId;
    ResultSet rs;
    Connection con = RDBMServices.getConnection();
    LocaleManager localeManager = profile.getLocaleManager();

    RDBMServices.setAutoCommit(con, false);          // May speed things up, can't hurt

    try {
      Document doc = DocumentFactory.getNewDocument();
      Element root = doc.createElement("layout");
      Statement stmt = con.createStatement();
      // A separate statement is needed so as not to interfere with ResultSet
      // of statements used for queries
      Statement insertStmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();
        // eventually, we need to fix template layout implementations so you can just do this:
        //        int layoutId=profile.getLayoutId();
        // but for now:
        int layoutId = this.getLayoutID(userId, profile.getProfileId());

       if (layoutId == 0) { // First time, grab the default layout for this user
          String sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            userId = rs.getInt(1);
            layoutId = rs.getInt(2);
          } finally {
            rs.close();
          }

          // Make sure the next struct id is set in case the user adds a channel
          sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + userId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
          int nextStructId;
          rs = stmt.executeQuery(sQuery);
          try {
            rs.next();
            nextStructId = rs.getInt(1);
          } finally {
            rs.close();
          }

          int realNextStructId = 0;

          if (realUserId != userId) {
            // But never make the existing value SMALLER, change it only to make it LARGER
            // (so, get existing value)
            sQuery = "SELECT NEXT_STRUCT_ID FROM UP_USER WHERE USER_ID=" + realUserId;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
            rs = stmt.executeQuery(sQuery);
            try {
              rs.next();
              realNextStructId = rs.getInt(1);
            } finally {
              rs.close();
            }
          }

          if (nextStructId > realNextStructId) {
            sQuery = "UPDATE UP_USER SET NEXT_STRUCT_ID=" + nextStructId + " WHERE USER_ID=" + realUserId;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }

          RDBMServices.commit(con); // Make sure it appears in the store
        }

       int firstStructId = -1;
       
       //Flags to enable a default layout lookup if it's needed
       boolean foundLayout = false;
       boolean triedDefault = false;
       
       //This loop is used to ensure a layout is found for a user. It tries
       //looking up the layout for the current userID. If one isn't found
       //the userID is replaced with the template user ID for this user and
       //the layout is searched for again. This loop should only ever loop once.
       do {
           String sQuery = "SELECT INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID = " + layoutId;
           LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
           rs = stmt.executeQuery(sQuery);
           try {
             if (rs.next()) {
               firstStructId = rs.getInt(1);
             } else {
               throw new Exception("RDBMUserLayoutStore::getUserLayout(): No INIT_STRUCT_ID in UP_USER_LAYOUT for " + userId + " and LAYOUT_ID " + layoutId);
             }
           } finally {
             rs.close();
           }

           String sql;
           if (localeAware) {
               // This needs to be changed to get the localized strings
               sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
             "ULS.UNREMOVABLE,ULS.IMMUTABLE";
           }  else {
               sql = "SELECT ULS.STRUCT_ID,ULS.NEXT_STRUCT_ID,ULS.CHLD_STRUCT_ID,ULS.CHAN_ID,ULS.NAME,ULS.TYPE,ULS.HIDDEN,"+
             "ULS.UNREMOVABLE,ULS.IMMUTABLE";
           }
           if (RDBMServices.supportsOuterJoins) {
             sql += ",USP.STRUCT_PARM_NM,USP.STRUCT_PARM_VAL FROM " + RDBMServices.joinQuery.getQuery("layout");
           } else {
             sql += " FROM UP_LAYOUT_STRUCT ULS WHERE ";
           }
           sql += " ULS.USER_ID=" + userId + " AND ULS.LAYOUT_ID=" + layoutId + " ORDER BY ULS.STRUCT_ID";
           LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
           rs = stmt.executeQuery(sql);
           
           //check for rows in the result set
           foundLayout = rs.next();
           
           if (!foundLayout && !triedDefault && userId == realUserId) {
               //If we didn't find any rows and we haven't tried the default user yet
               triedDefault = true;
               rs.close();
               
               //Get the default user ID and layout ID
               sQuery = "SELECT USER_DFLT_USR_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
               LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sQuery);
               rs = stmt.executeQuery(sQuery);
               try {
                 rs.next();
                 userId = rs.getInt(1);
                 layoutId = rs.getInt(2);
               } finally {
                 rs.close();
               }                
           }
           else {
               //We tried the default or actually found a layout
               break;
           }
       } while (!foundLayout);
       
       HashMap layoutStructure = new HashMap();
       StringBuffer structChanIds = new StringBuffer();
       
       try {
         int lastStructId = 0;
         LayoutStructure ls = null;
         String sepChar = "";
         if (foundLayout) {
            int structId = rs.getInt(1);
            // Result Set returns 0 by default if structId was null
            // Except if you are using poolman 2.0.4 in which case you get -1 back
            if (rs.wasNull()) {
              structId = 0;
            }
            readLayout: while (true) {
              if (DEBUG > 1) System.err.println("Found layout structureID " + structId);

              int nextId = rs.getInt(2);
              if (rs.wasNull()) {
                nextId = 0;
              }
              int childId = rs.getInt(3);
              if (rs.wasNull()) {
                childId = 0;
              }
              int chanId = rs.getInt(4);
              if (rs.wasNull()) {
                chanId = 0;
              }
              String temp5=rs.getString(5); // Some JDBC drivers require columns accessed in order
              String temp6=rs.getString(6); // Access 5 and 6 now, save till needed.

              // uPortal i18n
              int name_index, value_index;
              if (localeAware) {
                  ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(7),rs.getString(8),rs.getString(9),localeManager.getLocales()[0].toString());
                  name_index=10;
                  value_index=11;
              }  else {
                  ls = new LayoutStructure(structId, nextId, childId, chanId, rs.getString(7),rs.getString(8),rs.getString(9));
                  name_index=10;
                  value_index=11;
              }
              layoutStructure.put(new Integer(structId), ls);
              lastStructId = structId;
              if (!ls.isChannel()) {
                ls.addFolderData(temp5, temp6); // Plug in saved column values
              }
              if (RDBMServices.supportsOuterJoins) {
                do {
                  String name = rs.getString(name_index);
                  String value = rs.getString(value_index); // Oracle JDBC requires us to do this for longs
                  if (name != null) { // may not be there because of the join
                    ls.addParameter(name, value);
                  }
                  if (!rs.next()) {
                    break readLayout;
                  }
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } while (structId == lastStructId);
              } else { // Do second SELECT later on for structure parameters
                if (ls.isChannel()) {
                  structChanIds.append(sepChar + ls.chanId);
                  sepChar = ",";
                }
                if (rs.next()) {
                  structId = rs.getInt(1);
                  if (rs.wasNull()) {
                    structId = 0;
                  }
                } else {
                  break readLayout;
                }
              }
            } // while
          }
        } finally {
          rs.close();
        }

        if (!RDBMServices.supportsOuterJoins) { // Pick up structure parameters
          // first, get the struct ids for the channels
          String sql = "SELECT STRUCT_ID FROM UP_LAYOUT_STRUCT WHERE USER_ID=" + userId +
            " AND LAYOUT_ID=" + layoutId +
            " AND CHAN_ID IN (" + structChanIds.toString() + ") ORDER BY STRUCT_ID";

          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
          StringBuffer structIdsSB = new StringBuffer( "" );
          String sep = "";
          rs = stmt.executeQuery(sql);
          try {
            // use the results to build a correct list of struct ids to look for
            while( rs.next()) {
              structIdsSB.append(sep + rs.getString(1));
              sep = ",";
            }// while
          } finally {
            rs.close();
          } // be a good doobie


          sql = "SELECT STRUCT_ID, STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_PARAM WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId +
            " AND STRUCT_ID IN (" + structIdsSB.toString() + ") ORDER BY STRUCT_ID";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): " + sql);
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
          createLayout(layoutStructure, doc, root, firstStructId);
          layoutStructure.clear();

          long stopTime = System.currentTimeMillis();
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserLayout(): Layout document for user " + userId + " took " +
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
        insertStmt.close();
      }
      return  doc;
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  public UserProfile getUserProfileById (IPerson person, int profileId) throws Exception {
    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId + " AND PROFILE_ID=" + profileId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserProfileById(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          if (rs.next()) {
            String temp3 = rs.getString(3);
            String temp4 = rs.getString(4);
            int layoutId = rs.getInt(5);
            if (rs.wasNull()) {
              layoutId = 0;
            }
            int structSsId = rs.getInt(6);
            if (rs.wasNull()) {
              structSsId = 0;
            }
            int themeSsId = rs.getInt(7);
            if (rs.wasNull()) {
              themeSsId = 0;
            }
            return new UserProfile(profileId, temp3,temp4, layoutId,
                structSsId, themeSsId);
          }
          else {
            throw new Exception("Unable to find User Profile for user " + userId + " and profile " + profileId);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  public Hashtable getUserProfileList (IPerson person) throws Exception {
    int userId = person.getID();

    Hashtable pv = new Hashtable();
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID FROM UP_USER_PROFILE WHERE USER_ID="
            + userId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getUserProfileList(): " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            int layoutId = rs.getInt(5);
            if (rs.wasNull()) {
              layoutId = 0;
            }
            int structSsId = rs.getInt(6);
            if (rs.wasNull()) {
              structSsId = 0;
            }
            int themeSsId = rs.getInt(7);
            if (rs.wasNull()) {
              themeSsId = 0;
            }

            UserProfile upl = new UserProfile(rs.getInt(2), rs.getString(3), rs.getString(4),
                layoutId, structSsId, themeSsId);
            pv.put(new Integer(upl.getProfileId()), upl);
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return  pv;
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
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureChannelAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=3 AND PARAM_NAME='"
          + pName + "'";
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureChannelAttribute() : " + sQuery);
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
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureFolderAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=2 AND PARAM_NAME='"
          + pName + "'";
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureFolderAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  public void removeStructureStylesheetDescription (int stylesheetId) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        // detele all associated theme stylesheets
        String sQuery = "SELECT SS_ID FROM UP_SS_THEME WHERE STRUCT_SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
          while (rs.next()) {
            removeThemeStylesheetDescription(rs.getInt("SS_ID"));
          }
        } finally {
          rs.close();
        }
        sQuery = "DELETE FROM UP_SS_STRUCT WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // delete params
        sQuery = "DELETE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
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
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_TYPE=1 AND PARAM_NAME='"
          + pName + "'";
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeStructureStylesheetParam() : " + sQuery);
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
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeChannelAttribute() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_TYPE=3 AND PARAM_NAME='"
          + pName + "'";
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  public void removeThemeStylesheetDescription (int stylesheetId) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_SS_THEME WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // delete params
        sQuery = "DELETE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);

        // nuke all of the profiles that use it
        sQuery = "SELECT USER_ID,PROFILE_ID FROM UP_USER_PROFILE WHERE THEME_SS_ID="+stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        ResultSet rs = stmt.executeQuery(sQuery);
        try {
            while (rs.next()) {
              deleteUserProfile(rs.getInt("USER_ID"),rs.getInt("PROFILE_ID"));
          }
        } finally {
            rs.close();
        }

        // clean up user preferences - directly ( in case of loose params )
        sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        sQuery = "DELETE FROM UP_SS_USER_ATTS WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);


        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
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
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
      // clean up user preference tables
      sQuery = "DELETE FROM UP_SS_USER_PARM WHERE SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_TYPE=1 AND PARAM_NAME='"
          + pName + "'";
      LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::removeThemeStylesheetParam() : " + sQuery);
      stmt.executeQuery(sQuery);
    } finally {
      stmt.close();
    }
  }

  protected final int saveStructure (Node node, RDBMServices.PreparedStatement structStmt, RDBMServices.PreparedStatement parmStmt) throws java.sql.SQLException {
    if (node == null || node.getNodeName().equals("parameter")) { // No more or parameter node
      return  0;
    }
    Element structure = (Element)node;
    int saveStructId = Integer.parseInt(structure.getAttribute("ID").substring(1));
    int nextStructId = 0;
    int childStructId = 0;
    String sQuery;
    if (DEBUG > 0) {
      LogService.log(LogService.DEBUG, "-->" + node.getNodeName() + "@" + saveStructId);
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
      Integer eID = new Integer(externalId);
      structStmt.setInt(4, eID.intValue());
    } else {
      structStmt.setNull(4, java.sql.Types.NUMERIC);

    }
    if (node.getNodeName().equals("channel")) {
      int chanId = Integer.parseInt(node.getAttributes().getNamedItem("chanID").getNodeValue());
      structStmt.setInt(5, chanId);
      structStmt.setNull(6,java.sql.Types.VARCHAR);
    }
    else {
      structStmt.setNull(5,java.sql.Types.NUMERIC);
      structStmt.setString(6, structure.getAttribute("name"));
    }
    String structType = structure.getAttribute("type");
    structStmt.setString(7, structType);
    structStmt.setString(8, RDBMServices.dbFlag(xmlBool(structure.getAttribute("hidden"))));
    structStmt.setString(9, RDBMServices.dbFlag(xmlBool(structure.getAttribute("immutable"))));
    structStmt.setString(10, RDBMServices.dbFlag(xmlBool(structure.getAttribute("unremovable"))));

    LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::saveStructure(): " + structStmt);
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
            if( nodeValue != null && nodeValue.length() > 0 ) {
              parmStmt.clearParameters();
              parmStmt.setInt(1, saveStructId);
              parmStmt.setString(2, nodeName);
              parmStmt.setString(3, nodeValue);
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::saveStructure(): " + parmStmt);
              parmStmt.executeUpdate();
            }
          }
        }
      }
    }
    return  saveStructId;
  }

  public void setStructureStylesheetUserPreferences (IPerson person, int profileId, StructureStylesheetUserPreferences ssup) throws Exception {
    int userId = person.getID();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;
    try {
      con = RDBMServices.getConnection();

      // Set autocommit false for the connection
      int stylesheetId = ssup.getStylesheetId();
      RDBMServices.setAutoCommit(con, false);
      stmt = con.createStatement();
      try {
        // write out params
        for (Enumeration e = ssup.getParameterValues().keys(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          // see if the parameter was already there
          String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
              + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND PARAM_NAME='" + pName + "'";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
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
          rs.close();
          rs = null;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
          stmt.executeUpdate(sQuery);
        }
        
        String sSql = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sSql);
        stmt.executeUpdate(sSql);
        
        // write out folder attributes
        for (Enumeration e = ssup.getFolders(); e.hasMoreElements();) {
          String folderId = (String)e.nextElement();
          for (Enumeration attre = ssup.getFolderAttributeNames(); attre.hasMoreElements();) {
            String pName = (String)attre.nextElement();
            String pValue = ssup.getDefinedFolderAttributeValue(folderId, pName);
            if (pValue != null) {
              // store user preferences
              String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_ATTS WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID=" + folderId.substring(1) + " AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=2";
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID=" + folderId.substring(1) + " AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=2";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",1," + folderId.substring(1) + ",'" + pName + "',2,'" + pValue
                    + "')";
              }
              rs.close();
              rs = null;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
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
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID=" + channelId.substring(1) + " AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=3";
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=1 AND STRUCT_ID=" + channelId.substring(1) + " AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=3";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",1," + channelId.substring(1) + ",'" + pName + "',3,'" + pValue
                    + "')";
              }
              rs.close();
              rs = null;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setStructureStylesheetUserPreferences(): " + sQuery);
              stmt.executeUpdate(sQuery);
            }
          }
        }
        // Commit the transaction
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      }
    } finally {
      RDBMServices.closeResultSet(rs);
      RDBMServices.closeStatement(stmt);
      RDBMServices.releaseConnection(con);
    }
  }

  public void setThemeStylesheetUserPreferences (IPerson person, int profileId, ThemeStylesheetUserPreferences tsup) throws Exception {
    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      int stylesheetId = tsup.getStylesheetId();
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
          int layoutId = this.getLayoutID(userId, profileId);

          boolean firstLayout = false;
          if (layoutId == 0) { // First personal layout for this user/profile
            layoutId = 1;
            firstLayout = true;
          }
          
          //Check to see if the user has a matching layout
          String sSql = "SELECT * FROM UP_USER_LAYOUT WHERE USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId;
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
          ResultSet rs = stmt.executeQuery(sSql);

          try {
              //If not the default user is found and the layout rows from the
              //default user are copied for the current user.
              if (!rs.next()) {
                  int defaultUserId;

                  String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
                  LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);

                  ResultSet rs2 = stmt.executeQuery(sQuery);
                  try {
                      rs2.next();
                      defaultUserId = rs2.getInt(1);
                  }
                  finally {
                      rs2.close();
                  }

                  // Add to UP_USER_LAYOUT
                  sQuery =  "SELECT USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + defaultUserId;
                  LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);

                  rs2 = stmt.executeQuery(sQuery);

                  try {
                      while (rs2.next()) {
                          sQuery =
                              "INSERT INTO UP_USER_LAYOUT " +
                              "(USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID) " +
                              "VALUES(" +
                                  userId + "," +
                                  rs2.getInt("LAYOUT_ID") + "," +
                                  "'" + rs2.getString("LAYOUT_TITLE") + "'," +
                                  rs2.getInt("INIT_STRUCT_ID") + ")";

                          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
                          stmt.executeUpdate(sQuery);
                      }
                  }
                  finally {
                      rs2.close();
                  }
              }
          }
          finally {
              rs.close();
          }          
          
        // write out params
        for (Enumeration e = tsup.getParameterValues().keys(); e.hasMoreElements();) {
          String pName = (String)e.nextElement();
          // see if the parameter was already there
          String sQuery = "SELECT PARAM_VAL FROM UP_SS_USER_PARM WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId
              + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND PARAM_NAME='" + pName + "'";
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
          rs = stmt.executeQuery(sQuery);
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
          LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
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
                  + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND STRUCT_ID=" + channelId.substring(1) + " AND PARAM_NAME='" + pName
                  + "' AND PARAM_TYPE=3";
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
              rs = stmt.executeQuery(sQuery);
              if (rs.next()) {
                // update
                sQuery = "UPDATE UP_SS_USER_ATTS SET PARAM_VAL='" + pValue + "' WHERE USER_ID=" + userId + " AND PROFILE_ID="
                    + profileId + " AND SS_ID=" + stylesheetId + " AND SS_TYPE=2 AND STRUCT_ID=" + channelId.substring(1) + " AND PARAM_NAME='"
                    + pName + "' AND PARAM_TYPE=3";
              }
              else {
                // insert
                sQuery = "INSERT INTO UP_SS_USER_ATTS (USER_ID,PROFILE_ID,SS_ID,SS_TYPE,STRUCT_ID,PARAM_NAME,PARAM_TYPE,PARAM_VAL) VALUES ("
                    + userId + "," + profileId + "," + stylesheetId + ",2," + channelId.substring(1) + ",'" + pName + "',3,'" + pValue
                    + "')";
              }
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setThemeStylesheetUserPreferences(): " + sQuery);
              stmt.executeUpdate(sQuery);
            }
          }
        }
        
        if (firstLayout) {

            int defaultUserId;
            int defaultLayoutId;
            // Have to copy some of data over from the default user
            String sQuery = "SELECT USER_DFLT_USR_ID,USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
            rs = stmt.executeQuery(sQuery);
            try {
              rs.next();
              defaultUserId = rs.getInt(1);
              defaultLayoutId = rs.getInt(2);
            } finally {
              rs.close();
            }

            sQuery = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=1 WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }

        // Commit the transaction
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }


  public void setUserBrowserMapping (IPerson person, String userAgent, int profileId) throws Exception {
    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      // remove the old mapping and add the new one
      Statement stmt = con.createStatement();
      try {
        String sQuery = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID=" + userId + " AND USER_AGENT='" + userAgent + "'";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserBrowserMapping(): " + sQuery);
        stmt.executeUpdate(sQuery);
        sQuery = "INSERT INTO UP_USER_UA_MAP (USER_ID,USER_AGENT,PROFILE_ID) VALUES (" + userId + ",'" + userAgent + "',"
            + profileId + ")";
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserBrowserMapping(): " + sQuery);
        stmt.executeUpdate(sQuery);
        // Commit the transaction
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Save the user layout
   * @param person
   * @param profile
   * @param layoutXML
   * @throws Exception
   */
  public void setUserLayout (IPerson person, UserProfile profile, Document layoutXML, boolean channelsAdded) throws Exception {
    int userId = person.getID();
    int profileId=profile.getProfileId();
    int layoutId=0;
   ResultSet rs;
   Connection con = RDBMServices.getConnection();
    try {
      RDBMServices.setAutoCommit(con, false);                // Need an atomic update here
      Statement stmt = con.createStatement();
      try {
        long startTime = System.currentTimeMillis();

        // eventually we want to be able to just get layoutId from the profile, but because of the
        // template user layouts we have to do this for now ...
        layoutId = this.getLayoutID(userId, profileId);

        boolean firstLayout = false;
        if (layoutId == 0) { // First personal layout for this user/profile
          layoutId = 1;
          firstLayout = true;
        }

        String selectString = "USER_ID=" + userId + " AND LAYOUT_ID=" + layoutId;
        String sSql = "DELETE FROM UP_LAYOUT_PARAM WHERE " + selectString;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
        stmt.executeUpdate(sSql);
        sSql = "DELETE FROM UP_LAYOUT_STRUCT WHERE " + selectString;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
        stmt.executeUpdate(sSql);
        if (DEBUG > 1) {
          System.err.println("--> saving document");
          dumpDoc(layoutXML.getFirstChild().getFirstChild(), "");
          System.err.println("<--");
        }

        RDBMServices.PreparedStatement structStmt = new RDBMServices.PreparedStatement(con,
                                                        "INSERT INTO UP_LAYOUT_STRUCT " +
                                                        "(USER_ID, LAYOUT_ID, STRUCT_ID, NEXT_STRUCT_ID, CHLD_STRUCT_ID,EXTERNAL_ID,CHAN_ID,NAME,TYPE,HIDDEN,IMMUTABLE,UNREMOVABLE) " +
                                                        "VALUES ("+ userId + "," + layoutId + ",?,?,?,?,?,?,?,?,?,?)");
        try {
          RDBMServices.PreparedStatement parmStmt = new RDBMServices.PreparedStatement(con,
            "INSERT INTO UP_LAYOUT_PARAM " +
            "(USER_ID, LAYOUT_ID, STRUCT_ID, STRUCT_PARM_NM, STRUCT_PARM_VAL) " +
            "VALUES ("+ userId + "," + layoutId + ",?,?,?)");
          try {
            int firstStructId = saveStructure(layoutXML.getFirstChild().getFirstChild(), structStmt, parmStmt);
            
            //Check to see if the user has a matching layout
            sSql = "SELECT * FROM UP_USER_LAYOUT WHERE " + selectString;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
            rs = stmt.executeQuery(sSql);

            try {
                //If not the default user is found and the layout rows from the
                //default user are copied for the current user.
                if (!rs.next()) {
                    int defaultUserId;

                    String sQuery = "SELECT USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID=" + userId;
                    LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);

                    ResultSet rs2 = stmt.executeQuery(sQuery);
                    try {
                        rs2.next();
                        defaultUserId = rs2.getInt(1);
                    }
                    finally {
                        rs2.close();
                    }

                    // Add to UP_USER_LAYOUT
                    sQuery =  "SELECT USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID FROM UP_USER_LAYOUT WHERE USER_ID=" + defaultUserId;
                    LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);

                    rs2 = stmt.executeQuery(sQuery);

                    try {
                        while (rs2.next()) {
                            sQuery =
                                "INSERT INTO UP_USER_LAYOUT " +
                                "(USER_ID,LAYOUT_ID,LAYOUT_TITLE,INIT_STRUCT_ID) " +
                                "VALUES(" +
                                    userId + "," +
                                    rs2.getInt("LAYOUT_ID") + "," +
                                    "'" + rs2.getString("LAYOUT_TITLE") + "'," +
                                    rs2.getInt("INIT_STRUCT_ID") + ")";

                            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
                            stmt.executeUpdate(sQuery);
                        }
                    }
                    finally {
                        rs2.close();
                    }
                }
            }
            finally {
                rs.close();
            }

            //Update the users layout with the correct inital structure ID
            sSql = "UPDATE UP_USER_LAYOUT SET INIT_STRUCT_ID=" + firstStructId + " WHERE " + selectString;
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
            stmt.executeUpdate(sSql);

            // Update the last time the user saw the list of available channels
            if (channelsAdded) {
              sSql = "UPDATE UP_USER SET LST_CHAN_UPDT_DT=" + RDBMServices.sqlTimeStamp() +
                " WHERE USER_ID=" + userId;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sSql);
              stmt.executeUpdate(sSql);
            }

            if (firstLayout) {

              int defaultUserId;
              int defaultLayoutId;
              // Have to copy some of data over from the default user
              String sQuery = "SELECT USER_DFLT_USR_ID,USER_DFLT_LAY_ID FROM UP_USER WHERE USER_ID=" + userId;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
              rs = stmt.executeQuery(sQuery);
              try {
                rs.next();
                defaultUserId = rs.getInt(1);
                defaultLayoutId = rs.getInt(2);
              } finally {
                rs.close();
              }

              sQuery = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=1 WHERE USER_ID=" + userId + " AND PROFILE_ID=" + profileId;
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + sQuery);
              stmt.executeUpdate(sQuery);
              
              /* insert row(s) into up_ss_user_parm */
              sQuery = "SELECT USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL "+
                " FROM UP_SS_USER_PARM WHERE USER_ID="+defaultUserId;
              LogService.log(LogService.DEBUG, "RDBMUserIdentityStore::getPortalUID(): " + sQuery);
              if (DEBUG>0) System.err.println(sQuery);
              rs.close();
              rs = stmt.executeQuery(sQuery);
              Statement insertStmt = con.createStatement();
              try {
                  while (rs.next()) {
                     String insert = "INSERT INTO UP_SS_USER_PARM (USER_ID, PROFILE_ID, SS_ID, SS_TYPE, PARAM_NAME, PARAM_VAL) "+
                     "VALUES("+
                     userId+","+
                     rs.getInt("PROFILE_ID")+","+
                     rs.getInt("SS_ID")+","+
                     rs.getInt("SS_TYPE")+","+
                     "'"+rs.getString("PARAM_NAME")+"',"+
                     "'"+rs.getString("PARAM_VAL")+"')";
    
                     LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): " + insert);
                     if (DEBUG>0) System.err.println(insert);
                     insertStmt.executeUpdate(insert);
                  }
              }
              finally {
                  insertStmt.close();
              }
            }
            long stopTime = System.currentTimeMillis();
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::setUserLayout(): Layout document for user " + userId + " took " +
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
      RDBMServices.commit(con);
    } catch (Exception e) {
      RDBMServices.rollback(con);
      throw  e;
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Updates an existing structure stylesheet description with a new one. Old stylesheet
   * description is found based on the Id provided in the parameter structure.
   * @param ssd new stylesheet description
   */
  public void updateStructureStylesheetDescription (StructureStylesheetDescription ssd) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int stylesheetId = ssd.getId();
        String sQuery = "UPDATE UP_SS_STRUCT SET SS_NAME='" + ssd.getStylesheetName() + "',SS_URI='" + ssd.getStylesheetURI()
            + "',SS_DESCRIPTION_URI='" + ssd.getStylesheetDescriptionURI() + "',SS_DESCRIPTION_TEXT='" + ssd.getStylesheetWordDescription()
            + "' WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // first, see what was there before
        HashSet oparams = new HashSet();
        HashSet ofattrs = new HashSet();
        HashSet ocattrs = new HashSet();
        sQuery = "SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_SS_STRUCT_PAR WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
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
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
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
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
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
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else {
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + type +
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription(): " + sQuery);
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription(): " + sQuery);
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateStructureStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // Commit the transaction
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  /**
   * Updates an existing structure stylesheet description with a new one. Old stylesheet
   * description is found based on the Id provided in the parameter structure.
   * @param tsd new theme stylesheet description
   */
  public void updateThemeStylesheetDescription (ThemeStylesheetDescription tsd) throws Exception {
    Connection con = RDBMServices.getConnection();
    try {
      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        int stylesheetId = tsd.getId();

        String sQuery = "UPDATE UP_SS_THEME SET SS_NAME='" + tsd.getStylesheetName() + "',SS_URI='" + tsd.getStylesheetURI()+ "',SS_DESCRIPTION_URI='" + tsd.getStylesheetDescriptionURI() + "',SS_DESCRIPTION_TEXT='" + tsd.getStylesheetWordDescription() + "',SAMPLE_ICON_URI='"+tsd.getSampleIconURI()+"',SAMPLE_URI='"+tsd.getSamplePictureURI()+"',MIME_TYPE='"+tsd.getMimeType()+"',DEVICE_TYPE='"+tsd.getDeviceType()+"',SERIALIZER_NAME='"+tsd.getSerializerName()+"',UP_MODULE_CLASS='"+tsd.getCustomUserPreferencesManagerClass()+"' WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
        stmt.executeUpdate(sQuery);
        // first, see what was there before
        HashSet oparams = new HashSet();
        HashSet ocattrs = new HashSet();
        sQuery = "SELECT PARAM_NAME,PARAM_DEFAULT_VAL,PARAM_DESCRIPT,TYPE FROM UP_SS_THEME_PARM WHERE SS_ID=" + stylesheetId;
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
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
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else if (type == 2) {
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : encountered a folder attribute specified for a theme stylesheet ! DB is corrupt. (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + type +
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
                LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : " + sQuery);
                stmt.executeUpdate(sQuery);
              }
            }
            else {
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription() : encountered param of unknown type! (stylesheetId="
                  + stylesheetId + " param_name=\"" + rsOld.getString("PARAM_NAME") + "\" type=" + type +
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription(): " + sQuery);
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
            LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateThemeStylesheetDescription(): " + sQuery);
            stmt.executeUpdate(sQuery);
          }
        }
        // Commit the transaction
        RDBMServices.commit(con);
      } catch (Exception e) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw  e;
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  public void updateUserProfile (IPerson person, UserProfile profile) throws Exception {
    int userId = person.getID();
    Connection con = RDBMServices.getConnection();
    try {
      Statement stmt = con.createStatement();
      try {
        String sQuery = "UPDATE UP_USER_PROFILE SET LAYOUT_ID=" + profile.getLayoutId()
            + ", THEME_SS_ID=" + profile.getThemeStylesheetId() + ", STRUCTURE_SS_ID="
            + profile.getStructureStylesheetId() + ", DESCRIPTION='" + profile.getProfileDescription() + "', PROFILE_NAME='"
            + profile.getProfileName() + "' WHERE USER_ID = " + userId + " AND PROFILE_ID=" + profile.getProfileId();
        LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::updateUserProfile() : " + sQuery);
        stmt.executeUpdate(sQuery);
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }

  public void setSystemBrowserMapping (String userAgent, int profileId) throws Exception {
    this.setUserBrowserMapping(systemUser, userAgent, profileId);
  }

  private int getSystemBrowserMapping (String userAgent) throws Exception {
    return  getUserBrowserMapping(systemUser, userAgent);
  }

  public UserProfile getUserProfile (IPerson person, String userAgent) throws Exception {
    int profileId = getUserBrowserMapping(person, userAgent);
    if (profileId == 0)
      return  null;
    return  this.getUserProfileById(person, profileId);
  }

  public UserProfile getSystemProfile (String userAgent) throws Exception {
    int profileId = getSystemBrowserMapping(userAgent);
    if (profileId == 0)
      return  null;
    UserProfile up = this.getUserProfileById(systemUser, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  public UserProfile getSystemProfileById (int profileId) throws Exception {
    UserProfile up = this.getUserProfileById(systemUser, profileId);
    up.setSystemProfile(true);
    return  up;
  }

  public Hashtable getSystemProfileList () throws Exception {
    Hashtable pl = this.getUserProfileList(systemUser);
    for (Enumeration e = pl.elements(); e.hasMoreElements();) {
      UserProfile up = (UserProfile)e.nextElement();
      up.setSystemProfile(true);
    }
    return  pl;
  }

  public void updateSystemProfile (UserProfile profile) throws Exception {
    this.updateUserProfile(systemUser, profile);
  }

  public UserProfile addSystemProfile (UserProfile profile) throws Exception {
    return  addUserProfile(systemUser, profile);
  }

  public void deleteSystemProfile (int profileId) throws Exception {
    this.deleteUserProfile(systemUser, profileId);
  }

    private class SystemUser implements IPerson {
      public void setID(int sID) {}
      public int getID() {return 0;}

      public void setFullName(String sFullName) {}
      public String getFullName() {return "uPortal System Account";}

      public Object getAttribute (String key) {return null;}
      public Object[] getAttributeValues (String key) {return null;}
      public void setAttribute (String key, Object value) {}

      public Enumeration getAttributes () {return null;}
      public Enumeration getAttributeNames () {return null;}

      public boolean isGuest() {return(false);}

      public ISecurityContext getSecurityContext() { return(null); }
      public void setSecurityContext(ISecurityContext context) {}

      public EntityIdentifier getEntityIdentifier() {return null;}
    }

    private IPerson systemUser = new SystemUser(); // We should be getting this from the uPortal

  public UserPreferences getUserPreferences (IPerson person, int profileId) throws Exception {
    UserPreferences up = null;
    UserProfile profile = this.getUserProfileById(person, profileId);
    if (profile != null) {
      up = getUserPreferences(person, profile);
    }
    return  (up);
  }

  public UserPreferences getUserPreferences (IPerson person, UserProfile profile) throws Exception {
    int profileId = profile.getProfileId();
    UserPreferences up = new UserPreferences(profile);
    up.setStructureStylesheetUserPreferences(getStructureStylesheetUserPreferences(person, profileId, profile.getStructureStylesheetId()));
    up.setThemeStylesheetUserPreferences(getThemeStylesheetUserPreferences(person, profileId, profile.getThemeStylesheetId()));
    return  up;
  }

  public void putUserPreferences (IPerson person, UserPreferences up) throws Exception {
    // store profile
    UserProfile profile = up.getProfile();
    this.updateUserProfile(person, profile);
    this.setStructureStylesheetUserPreferences(person, profile.getProfileId(), up.getStructureStylesheetUserPreferences());
    this.setThemeStylesheetUserPreferences(person, profile.getProfileId(), up.getThemeStylesheetUserPreferences());
  }
  
  /**
   * Returns the current layout ID for the user and profile. If the profile doesn't exist or the
   * layout_id field is null 0 is returned.
   *
   * @param userId The userId for the profile
   * @param profileId The profileId for the profile
   * @return The layout_id field or 0 if it does not exist or is null
   * @throws SQLException
   */
  protected int getLayoutID(int userId, int profileId) throws SQLException {
      String query =
          "SELECT LAYOUT_ID " +
          "FROM UP_USER_PROFILE " +
          "WHERE USER_ID=? AND PROFILE_ID=?";

      Connection con = RDBMServices.getConnection();
      int layoutId = 0;
      
      try {
          PreparedStatement pstmt = con.prepareStatement(query);
    
          try {
              LogService.log(LogService.DEBUG, "RDBMUserLayoutStore::getLayoutID(userId=" + userId + ", profileId=" + profileId + " ): " + query);
        
              pstmt.setInt(1, userId);
              pstmt.setInt(2, profileId);
              ResultSet rs = pstmt.executeQuery();
        
              try {
                  if (rs.next()) {
                      layoutId = rs.getInt(1);
        
                      if (rs.wasNull()) {
                          layoutId = 0;
                      }
                  }
              }
              finally {
                  rs.close();
              }
          }
          finally {
              pstmt.close();
          }
      }
      finally {
          RDBMServices.releaseConnection(con);
      }

      return layoutId;
  }  
}
