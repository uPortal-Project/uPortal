/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.channels;

import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URL;

/**
 * Bookmarks channel
 * @author Peter Kharchenko
 * @author Steven Toth
 * @author Bernie Durfee
 * @version $Revision$
 */
public class CBookmarks extends BaseChannel {
  // A DOM document where all the bookmark information will be contained
  protected DocumentImpl m_bookmarksXML;
  // Location of the stylesheet list file
  private static final String sslLocation = "CBookmarks/CBookmarks.ssl";
  // Define some constants to keep the state of the channel
  private final int VIEWMODE = 0;
  private final int EDITMODE = 1;
  private final int EDITNODEMODE = 3;
  private final int ADDNODEMODE = 4;
  private final int MOVENODEMODE = 5;
  private final int DELETENODEMODE = 6;
  // Start out in view mode by default
  private int m_currentState = 0;
  // Keep track of the node that the user is currently working with
  private String m_activeNodeID = null;
  private String m_activeNodeType = null;


  private DocumentImpl getBookmarkXML () {
    Connection connection = null;
    // Return the cached bookmarks if they've already been read in
    if (m_bookmarksXML != null) {
      return  (m_bookmarksXML);
    }
    try {
      String inputXML = null;
      // Create a new parser for the incoming bookmarks document
      DOMParser domParser = new DOMParser();
      // Get a connection to the database
      connection = getConnection();
      // Get the current user's ID
      int userid = staticData.getPerson().getID();
      // Attempt to retrieve the user's bookmark's
      String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID=" + userid;
      Statement stmt = connection.createStatement();
      try {
        // Get the result set
        LogService.instance().log(LogService.DEBUG, "CBookmarks.getBookmarkXML(): " + query);        
        ResultSet rs = stmt.executeQuery(query);
        try {
          if (rs.next()) {
            // If a result came back then use that for the XML...
            inputXML = rs.getString("BOOKMARK_XML");
          }
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
      if (inputXML == null || inputXML.length() == 0) {
        // ...or else use the bookmarks from the default user
        inputXML = getDefaultBookmarks();
      }
      // Turn validation on for the DOM parser to make sure it reads the DTD
      domParser.setFeature("http://xml.org/sax/features/validation", true);
      domParser.setEntityResolver(new org.jasig.portal.utils.DTDResolver("xbel-1.0.dtd"));
      // Parse the XML document containing the user's bookmarks
      domParser.parse(new InputSource(new StringReader(inputXML)));
      // Cache the bookmarks DOM locally
      m_bookmarksXML = (DocumentImpl)domParser.getDocument();
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    } finally {
      // Release the database connection
      if (connection != null) {
        releaseConnection(connection);
      }
    }
    // Return what is cached
    return  (m_bookmarksXML);
  }


  private String getDefaultBookmarks () {
    Connection connection = null;
    String inputXML = null;
    try {
      // Get a connection to the database
      connection = getConnection();
      // Get the bookmarks for the 'default' user
      IPerson person = this.staticData.getPerson();
      String defaultUser = (String)person.getAttribute("uPortalTemplateUserName");
      if (defaultUser == null || defaultUser.equals("")) {
        defaultUser = "system";
      }
      String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID = (SELECT USER_ID FROM UP_USER WHERE USER_NAME = '" + defaultUser + "')";
      Statement stmt = connection.createStatement();
      try {
        // Try to get the 'default' bookmarks from the database
        LogService.instance().log(LogService.DEBUG, "CBookmarks.getDefaultBookmarks(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        try {
          if (rs.next()) {
            // Use the 'default' user's bookmarks...
          inputXML = rs.getString("BOOKMARK_XML");
          }
          else {
            // Generate the XML here as a last resort
             StringBuffer strbuf = new StringBuffer();
             strbuf.append("<?xml version=\"1.0\"?>\n");
             strbuf.append("<!DOCTYPE xbel PUBLIC \"+//IDN python.org//DTD XML Bookmark Exchange Language 1.0//EN//XML\" \"http://www.python.org/topics/xml/dtds/xbel-1.0.dtd\">\n");
             strbuf.append("<xbel>\n");
             strbuf.append("  <title>Default Bookmarks</title>\n");
             strbuf.append("  <info>\n");
             strbuf.append("    <metadata owner=\"" + staticData.getPerson().getID() + "\"/>\n");
             strbuf.append("  </info>\n");
             strbuf.append("</xbel>");
             inputXML = strbuf.toString();
 
             LogService.instance().log(LogService.WARN, "CBookmarks.getDefaultBookmarks(): Could not find bookmarks for 'default' user");
          }
        } finally {
          rs.close();
        }
        // Now add a row to the database for the user
        String insert = "INSERT INTO UPC_BOOKMARKS (PORTAL_USER_ID, BOOKMARK_XML) VALUES (" + staticData.getPerson().getID()
            + ",'" + inputXML + "')";
        LogService.instance().log(LogService.DEBUG, "CBookmarks.getDefaultBookmarks(): " + insert);
        stmt.executeUpdate(insert);
      } finally {
        stmt.close();
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    } finally {
      if (connection != null) {
        releaseConnection(connection);
      }
      if (inputXML == null) {
        // ...or else just start with an empty set of bookmarks
        LogService.instance().log(LogService.ERROR, "CBookmarks.getDefaultBookmarks() - Could not retrieve default bookmark xml, using blank xml.");
        inputXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xbel></xbel>";
      }
    }
    return  (inputXML);
  }

  protected void saveXML () {
    if (m_bookmarksXML == null) {
      return;
    }
    Connection connection = getConnection();
    try {
      StringWriter stringWriter = new StringWriter();
      // Serialize the DOM tree to a string
      XMLSerializer xmlSerializer = new XMLSerializer(stringWriter, new OutputFormat(m_bookmarksXML));
      xmlSerializer.serialize(m_bookmarksXML);
      // Get a connection to the database
      String update = "UPDATE UPC_BOOKMARKS SET BOOKMARK_XML = '" + stringWriter.toString() + "' WHERE PORTAL_USER_ID = "
          + staticData.getPerson().getID();
      Statement stmt = connection.createStatement();
      try {
        LogService.instance().log(LogService.DEBUG, "CBookmarks.saveXML(): " + update);        
        stmt.executeUpdate(update);
      } finally {
        stmt.close();
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    } finally {
      releaseConnection(connection);
    }
  }

  /**
   *  Render the user's layout based on the current state of the channel
   */
  public void renderXML (ContentHandler out) {
    // Retrieve the command passed in by the user
    String command = runtimeData.getParameter("command");
    if (command != null) {
      if (command.equals("fold")) {
        // Get the ID of the specified folder to close
        String folderID = runtimeData.getParameter("ID");
        if (folderID != null) {
          Element folderElement = getBookmarkXML().getElementById(folderID);
          if (folderElement != null && folderElement.getNodeName().equals("folder")) {
            folderElement.setAttribute("folded", "yes");
          }
        }
      }
      else if (command.equals("unfold")) {
        // Get the ID of the specified folder to open
        String folderID = runtimeData.getParameter("ID");
        if (folderID != null) {
          Element folderElement = getBookmarkXML().getElementById(folderID);
          if (folderElement != null && folderElement.getNodeName().equals("folder")) {
            folderElement.setAttribute("folded", "no");
          }
        }
      }
      else if (command.equals("View")) {
        // Switch to view mode
        m_currentState = VIEWMODE;
        m_activeNodeID = null;
      }
      else if (command.equals("Edit")) {
        // Switch to edit mode
        m_currentState = EDITMODE;
        m_activeNodeID = null;
      }
      else if (command.equals("AddBookmark")) {
        if (m_currentState != ADDNODEMODE) {
          // Switch to add bookmark mode
          m_currentState = ADDNODEMODE;
          m_activeNodeID = null;
          m_activeNodeType = "bookmark";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null && submitButton.equals("Cancel")) {
            // The user pressed the cancel button so return to view mode
            m_activeNodeID = null;
            m_activeNodeType = null;
            m_currentState = VIEWMODE;
          }
          else if (submitButton != null && submitButton.equals("Add")) {
            // Check for the incoming parameters
            String bookmarkTitle = runtimeData.getParameter("BookmarkTitle");
            String bookmarkURL = runtimeData.getParameter("BookmarkURL");
            String bookmarkDesc = runtimeData.getParameter("BookmarkDescription");
            String folderID = runtimeData.getParameter("FolderRadioButton");
            if (bookmarkTitle == null || bookmarkTitle.length() < 1) {}
            else if (bookmarkURL == null || bookmarkURL.length() < 1) {}
            else if (folderID == null || folderID.length() < 1) {}
            else {
              Element folderElement;
              if (folderID.equals("RootLevel")) {
                folderElement = (Element)m_bookmarksXML.getElementsByTagName("xbel").item(0);
              }
              else {
                folderElement = m_bookmarksXML.getElementById(folderID);
              }
              if (folderElement == null) {}
              else {
                // Build the bookmark XML DOM
                Element bookmarkElement = m_bookmarksXML.createElement("bookmark");
                bookmarkElement.setAttribute("href", bookmarkURL);
                bookmarkElement.setAttribute("id", createUniqueID());
                // Create the title element
                Element titleElement = m_bookmarksXML.createElement("title");
                titleElement.appendChild(m_bookmarksXML.createTextNode(bookmarkTitle));
                bookmarkElement.appendChild(titleElement);
                // Create the desc element
                Element descElement = m_bookmarksXML.createElement("desc");
                descElement.appendChild(m_bookmarksXML.createTextNode(bookmarkDesc));
                bookmarkElement.appendChild(descElement);
                folderElement.appendChild(bookmarkElement);
                // Notify the DOM of the new ID
                m_bookmarksXML.putIdentifier(bookmarkElement.getAttribute("id"), bookmarkElement);
                // The user pressed the cancel button so return to view mode
                m_activeNodeID = null;
                m_activeNodeType = null;
                // Return to view mode
                m_currentState = VIEWMODE;
                // Save the user's XML
                saveXML();
              }
            }
          }
        }
      }
      else if (command.equals("AddFolder")) {
        if (m_currentState != ADDNODEMODE) {
          // Switch to add bookmark mode
          m_currentState = ADDNODEMODE;
          m_activeNodeID = null;
          m_activeNodeType = "folder";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null && submitButton.equals("Cancel")) {
            // The user pressed the cancel button so return to view mode
            m_activeNodeID = null;
            m_activeNodeType = null;
            m_currentState = VIEWMODE;
          }
          else if (submitButton != null && submitButton.equals("Add")) {
            // Check for the incoming parameters
            String folderTitle = runtimeData.getParameter("FolderTitle");
            String folderID = runtimeData.getParameter("FolderRadioButton");
            if (folderTitle == null || folderTitle.length() < 1) {}
            else if (folderID == null || folderID.length() < 1) {}
            else {
              Element folderElement;
              if (folderID.equals("RootLevel")) {
                folderElement = (Element)m_bookmarksXML.getElementsByTagName("xbel").item(0);
              }
              else {
                folderElement = m_bookmarksXML.getElementById(folderID);
              }
              if (folderElement == null) {}
              else {
                // Build the new folder XML node
                Element newFolderElement = m_bookmarksXML.createElement("folder");
                newFolderElement.setAttribute("id", createUniqueID());
                // Create the title element
                Element titleElement = m_bookmarksXML.createElement("title");
                titleElement.appendChild(m_bookmarksXML.createTextNode(folderTitle));
                newFolderElement.appendChild(titleElement);
                folderElement.appendChild(newFolderElement);
                // Notify the DOM of the new ID
                m_bookmarksXML.putIdentifier(newFolderElement.getAttribute("id"), newFolderElement);
                // The user pressed the cancel button so return to view mode
                m_activeNodeID = null;
                m_activeNodeType = null;
                // Return to view mode
                m_currentState = VIEWMODE;
                // Save the user's XML
                saveXML();
              }
            }
          }
        }
      }
      else if (command.equals("MoveNode")) {
        m_activeNodeID = runtimeData.getParameter("ID");
        m_currentState = MOVENODEMODE;
      }
      else if (command.equals("EditNode")) {
        m_activeNodeID = runtimeData.getParameter("ID");
        m_currentState = EDITNODEMODE;
      }
      else if (command.equals("DeleteBookmark")) {
        if (m_currentState != DELETENODEMODE) {
          m_currentState = DELETENODEMODE;
          m_activeNodeType = "bookmark";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null) {
            if (submitButton.equals("Cancel")) {
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("Delete")) {
              // Run through the passed in parameters and delete the bookmarks
              Enumeration e = runtimeData.keys();
              while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                if (key.startsWith("BookmarkCheckbox#")) {
                  String bookmarkID = key.substring(17);
                  Element bookmarkElement = m_bookmarksXML.getElementById(bookmarkID);
                  if (bookmarkElement != null && bookmarkElement.getNodeName().equals("bookmark")) {
                    bookmarkElement.getParentNode().removeChild(bookmarkElement);
                  }
                }
              }
              saveXML();
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("ConfirmDelete")) {}
          }
        }
      }
      else if (command.equals("DeleteFolder")) {
        if (m_currentState != DELETENODEMODE) {
          m_currentState = DELETENODEMODE;
          m_activeNodeType = "folder";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null) {
            if (submitButton.equals("Cancel")) {
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("Delete")) {
              // Run through the passed in parameters and delete the bookmarks
              Enumeration e = runtimeData.keys();
              while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                if (key.startsWith("FolderCheckbox#")) {
                  // The ID should come after the FolderCheckbox# part
                  String bookmarkID = key.substring(15);
                  // Find the folder in the DOM tree
                  Element folderElement = m_bookmarksXML.getElementById(bookmarkID);
                  // Remove the folder from the DOM tree
                  if (folderElement != null && folderElement.getNodeName().equals("folder")) {
                    folderElement.getParentNode().removeChild(folderElement);
                  }
                }
              }
              saveXML();
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("ConfirmDelete")) {}
          }
        }
      }
    }
    try {
      // Render content based on the current state of the channel
      switch (m_currentState) {
        case VIEWMODE:
          renderViewModeXML(out);
          break;
        case EDITMODE:
          renderEditModeXML(out);
          break;
        case EDITNODEMODE:
          renderEditNodeXML(out);
          break;
        case ADDNODEMODE:
          renderAddNodeXML(out);
          break;
        case MOVENODEMODE:
          renderMoveNodeXML(out);
          break;
        case DELETENODEMODE:
          renderDeleteNodeXML(out);
          break;
      }
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }


  private void renderViewModeXML (ContentHandler out) throws Exception {
    transformXML(out, "view_mode", getBookmarkXML());
  }

  private void renderEditModeXML (ContentHandler out) throws Exception {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "EditMode");
    transformXML(out, "edit_mode", getBookmarkXML(), parameters);
  }


  private void renderEditNodeXML (ContentHandler out) throws Exception {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "DeleteNode");
    transformXML(out, "delete_node", getBookmarkXML(), parameters);
  }

  private void renderAddNodeXML (ContentHandler out) throws Exception {
    Hashtable parameters = new Hashtable(1);
    if (m_activeNodeType == null) {
      LogService.instance().log(LogService.ERROR, "CBookmarks.renderAddNodeXML(): No active node type has been set");
      renderViewModeXML(out);
    }
    else if (m_activeNodeType.equals("bookmark")) {
      parameters.put("EditMode", "AddBookmark");
      transformXML(out, "add_node", getBookmarkXML(), parameters);
    }
    else if (m_activeNodeType.equals("folder")) {
      parameters.put("EditMode", "AddFolder");
      transformXML(out, "add_node", getBookmarkXML(), parameters);
    }
    else {
      LogService.instance().log(LogService.ERROR, "CBookmarks.renderAddNodeXML(): Unknown active node type - " + m_activeNodeType);
      renderViewModeXML(out);
    }
  }

  private void renderMoveNodeXML (ContentHandler out) throws Exception {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "MoveNode");
    transformXML(out, "move_node", getBookmarkXML(), parameters);
  }


  private void renderDeleteNodeXML (ContentHandler out) throws Exception {
    Hashtable parameters = new Hashtable(1);
    if (m_activeNodeType == null) {
      LogService.instance().log(LogService.ERROR, "CBookmarks.renderDeleteNodeXML(): No active node type has been set");
      renderViewModeXML(out);
    }
    else if (m_activeNodeType.equals("bookmark")) {
      parameters.put("EditMode", "DeleteBookmark");
      transformXML(out, "delete_node", getBookmarkXML(), parameters);
    }
    else if (m_activeNodeType.equals("folder")) {
      parameters.put("EditMode", "DeleteFolder");
      transformXML(out, "delete_node", getBookmarkXML(), parameters);
    }
    else {
      LogService.instance().log(LogService.ERROR, "CBookmarks.renderDeleteNodeXML(): Unknown active node type - " + m_activeNodeType);
      renderViewModeXML(out);
    }
  }


  private void transformXML (ContentHandler out, String stylesheetName, DocumentImpl inputXML) throws Exception {
    transformXML(out, stylesheetName, inputXML, null);
  }


  private void transformXML (ContentHandler out, String stylesheetName, DocumentImpl inputXML, Hashtable parameters) throws Exception {
    // Create the parameters hashtable if it is null
    if (parameters == null) {
      parameters = new Hashtable(1);
    }

    // Add the baseActionURL to the stylesheet parameters
    parameters.put("baseActionURL", runtimeData.getBaseActionURL());

    // Use the XSLT utility to perform the transformation
    XSLT xslt = new XSLT(this);
    xslt.setXML(inputXML);
    xslt.setXSL(sslLocation, stylesheetName, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameters(parameters);
    xslt.transform();
  }


  private String createUniqueID () {
    String uniqueID = "n" + System.currentTimeMillis();
    while (m_bookmarksXML.getElementById(uniqueID) != null) {
      uniqueID = "n" + System.currentTimeMillis();
    }
    return  (uniqueID);
  }


  private static String makeUrlSafe (String url) {
    // Return if the url is correctly formed
    if (url.toLowerCase().startsWith("http://")) {
      return  (url);
    }
    // Make sure the URL is well formed
    if (url.toLowerCase().startsWith("http:/")) {
      url = url.substring(0, 6) + "/" + url.substring(7);
      return  (url);
    }
    // If it's a mail link then be sure mailto: is on the front
    if (url.indexOf('@') != -1) {
      if (!url.toLowerCase().startsWith("mailto:")) {
        url = "mailto:" + url;
      }
      return  (url);
    }
    // Make sure http:// is on the front
    url = "http://" + url;
    return  (url);
  }


  private Connection getConnection () {
    try {
      RDBMServices rdbmServices = new RDBMServices();
      return  (rdbmServices.getConnection());
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      return  (null);
    }
  }


  private void releaseConnection (Connection connection) {
    try {
      RDBMServices rdbmServices = new RDBMServices();
      rdbmServices.releaseConnection(connection);
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }
}



