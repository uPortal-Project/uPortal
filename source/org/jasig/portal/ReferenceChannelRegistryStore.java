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

import  org.jasig.portal.utils.DTDResolver;
import  org.jasig.portal.RdbmServices;
import  java.io.*;
import  java.util.HashMap;
import  java.util.Vector;
import  java.sql.Connection;
import  java.sql.ResultSet;
import  java.sql.Statement;
import  java.sql.SQLException;
import  org.apache.xalan.xpath.*;
import  org.apache.xalan.xslt.*;
import  org.apache.xml.serialize.*;
import  org.apache.xerces.dom.*;
import  org.w3c.dom.*;


/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 */
public class ReferenceChannelRegistryStore
    implements IChannelRegistryStore {
  private static DocumentImpl m_channelRegistryDOM = null;
  private static RdbmServices m_rdbmServices = null;
  private static HashMap m_channelHashMap = null;
  private static boolean transactionFlag = false;

  /**
   * put your documentation comment here
   * @return 
   */
  public Document getRegistryDOM () {
    if (transactionFlag == true) {
      buildChannelRegistryDOM();
    }
    if (m_channelRegistryDOM != null) {
      // Return the cached registry DOM if it exists
      return  (m_channelRegistryDOM);
    } 
    else {
      // Build the DOM and then return it if it doesn't exist
      buildChannelRegistryDOM();
      return  (m_channelRegistryDOM);
    }
  }

  /**
   * put your documentation comment here
   */
  private synchronized void buildChannelRegistryDOM () {
    transactionFlag = true;
    // Create a new DOM for the channel registry
    m_channelRegistryDOM = new DocumentImpl();
    // Create the registry element
    Element registryElement = m_channelRegistryDOM.createElement("registry");
    // Build up the registry element
    buildCategories(registryElement);
    // Build the HashMap for channels
    buildChannelHashMap();
    // Add the channels to the registry
    addChannels(registryElement.getElementsByTagName("category"));
    // Bind it as the root node
    m_channelRegistryDOM.appendChild(registryElement);
    transactionFlag = false;
  }

  /**
   * put your documentation comment here
   * @param registryElement
   */
  private void buildCategories (Element registryElement) {
    Connection connection = m_rdbmServices.getConnection();
    try {
      // Create a statement to talk to the database
      Statement statement = connection.createStatement();
      // Select category information from the UP_CATEGORY table
      String sQuery = "SELECT CAT_ID, CAT_TITLE, CAT_DESC FROM UP_CATEGORY ORDER BY CAT_TITLE";
      Logger.log(Logger.DEBUG, "DBImpl::getCategoryXML(): " + sQuery);
      // Execute the query
      ResultSet rs = statement.executeQuery(sQuery);
      while (rs.next()) {
        // Get the ID, title and description for the category
        String catID = rs.getString("CAT_ID");
        String catTitle = rs.getString("CAT_TITLE");
        String catDesc = rs.getString("CAT_DESC");
        // Build the category element
        Element categoryElement = m_channelRegistryDOM.createElement("category");
        String uniqueID = getUniqueID();
        categoryElement.setAttribute("regID", uniqueID);
        categoryElement.setAttribute("ID", catID);
        categoryElement.setAttribute("title", catTitle);
        categoryElement.setAttribute("desc", catDesc);
        // Append the category onto the root element
        registryElement.appendChild(categoryElement);
        // Add an identifier for the element
        m_channelRegistryDOM.putIdentifier(uniqueID, categoryElement);
      }
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param categoryElements
   */
  private void addChannels (NodeList categoryElements) {
    for (int i = 0; i < categoryElements.getLength(); i++) {
      // Get a list of all of the sub-categories
      NodeList subCategories = ((Element)categoryElements.item(i)).getElementsByTagName("category");
      // Recurse if there are subcategories
      if (subCategories != null && subCategories.getLength() > 0) {
        addChannels(subCategories);
      }
      // Get the category ID
      String catID = categoryElements.item(i).getAttributes().getNamedItem("ID").getNodeValue();
      // Select all of the channels that belong to this category
      String query = "SELECT CHAN_ID, CAT_ID FROM UP_CAT_CHAN WHERE CAT_ID = " + catID;
      // Get a connection to the database
      Connection connection = RdbmServices.getConnection();
      try {
        // Create a statement to talk to the database
        Statement statement = connection.createStatement();
        // Execute query
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
          // Get the channel element that belongs in the category
          Element channelElement = (Element)m_channelHashMap.get(rs.getString("CHAN_ID"));
          // Maks sure the channel exists
          if (channelElement != null) {
            // Add the channel element to the category
            categoryElements.item(i).appendChild(channelElement);
            // Add an identifier for the element
            m_channelRegistryDOM.putIdentifier(channelElement.getAttribute("ID"), channelElement);
          } 
          else {
            Logger.log(Logger.WARN, "UP_CAT_CHAN references a channel not in UP_CHANNEL, CHAN_ID=" + rs.getString("CHAN_ID"));
          }
        }
      } catch (Exception se) {
        se.printStackTrace();
        Logger.log(Logger.ERROR, "ChannelRegistryImpl.addChannels(): " + se.getMessage());
      } finally {
        m_rdbmServices.releaseConnection(connection);
      }
    }
  }

  /**
   * put your documentation comment here
   */
  private void buildChannelHashMap () {
    // Create a new hashmap to store channel elements
    m_channelHashMap = new HashMap(100);
    // Create a connection to the database
    Connection connection = m_rdbmServices.getConnection();
    try {
      // Create a statement to talk to the database
      Statement statement = connection.createStatement();
      // Select the channel IDs from the database
      String query = "SELECT CHAN_ID FROM UP_CHANNEL";
      // Execute the query
      ResultSet rs = statement.executeQuery(query);
      // Create some temporary storage for channel IDs
      Vector channelIDs = new Vector(100);
      while (rs.next()) {
        channelIDs.add(rs.getString("CHAN_ID"));
      }
      // Release the connection to the database
      m_rdbmServices.releaseConnection(connection);
      for (int i = 0; i < channelIDs.size(); i++) {
        // Load up the HashMap with channel elements
        m_channelHashMap.put(channelIDs.elementAt(i), buildChannelElement((String)channelIDs.elementAt(i)));
      }
    } catch (SQLException se) {
      Logger.log(Logger.ERROR, "ChannelRegistryImpl.buildChannelHashMap(): " + se.getMessage());
    } finally {
      m_rdbmServices.releaseConnection(connection);
    }
  }

  /**
   * put your documentation comment here
   * @param channelID
   * @return 
   */
  private Element buildChannelElement (String channelID) {
    // Create the channel element
    Element channelElement = m_channelRegistryDOM.createElement("channel");
    Connection connection = null;
    Statement statement = null;
    try {
      // Create a connection to the database
      connection = m_rdbmServices.getConnection();
      // Create a statement to talk to the database
      statement = connection.createStatement();
      // Select all of the channel information from UP_CHANNEL
      String sQuery = "SELECT * FROM UP_CHANNEL WHERE CHAN_ID=" + channelID;
      //  Execute the query
      ResultSet rs = statement.executeQuery(sQuery);
      if (rs.next()) {
        String uniqueID = getUniqueID();
        // ID to make the channel unique in the 
        channelElement.setAttribute("ID", rs.getString("CHAN_ID"));
        channelElement.setAttribute("chanID", rs.getString("CHAN_ID"));
        channelElement.setAttribute("title", rs.getString("CHAN_TITLE"));
        channelElement.setAttribute("desc", rs.getString("CHAN_DESC"));
        channelElement.setAttribute("class", rs.getString("CHAN_CLASS"));
        channelElement.setAttribute("publID", rs.getString("CHAN_PUBL_ID"));
        channelElement.setAttribute("publDT", rs.getString("CHAN_PUBL_DT"));
        channelElement.setAttribute("apvlID", rs.getString("CHAN_APVL_ID"));
        channelElement.setAttribute("apvlDT", rs.getString("CHAN_APVL_DT"));
        channelElement.setAttribute("idTag", rs.getString("CHAN_ID_TAG"));
        channelElement.setAttribute("priority", rs.getString("CHAN_PRIORITY"));
        channelElement.setAttribute("timeout", rs.getString("CHAN_TIMEOUT"));
        channelElement.setAttribute("minimizable", rs.getString("CHAN_MINIMIZABLE"));
        channelElement.setAttribute("editable", rs.getString("CHAN_EDITABLE"));
        channelElement.setAttribute("hasHelp", rs.getString("CHAN_HAS_HELP"));
        channelElement.setAttribute("hasAbout", rs.getString("CHAN_HAS_ABOUT"));
        channelElement.setAttribute("removable", rs.getString("CHAN_REMOVABLE"));
        channelElement.setAttribute("detachable", rs.getString("CHAN_DETACHABLE"));
        channelElement.setAttribute("name", rs.getString("CHAN_NAME"));
      }
      // Select all of the parameters for the channel
      sQuery = "SELECT * FROM UP_CHAN_PARAM WHERE CHAN_ID=" + channelID;
      Logger.log(Logger.DEBUG, sQuery);
      // Create a new statement to talk to the database
      statement = connection.createStatement();
      // Execute the query
      rs = statement.executeQuery(sQuery);
      Element parameterElement = null;
      while (rs.next()) {
        parameterElement = m_channelRegistryDOM.createElement("parameter");
        parameterElement.setAttribute("name", rs.getString("CHAN_PARM_NM"));
        parameterElement.setAttribute("value", rs.getString("CHAN_PARM_VAL"));
        channelElement.appendChild(parameterElement);
      }
      return  (channelElement);
    } catch (SQLException se) {
      Logger.log(Logger.ERROR, "ChannelRegistryImpl.getChannelElement(): " + se.getMessage());
      return  (null);
    } finally {
      m_rdbmServices.releaseConnection(connection);
    }
  }

  /**
   * put your documentation comment here
   * @param channelID
   * @return 
   */
  public Element getChannelElement (String channelID) {
    if (m_channelHashMap == null) {
      buildChannelHashMap();
    }
    return  ((Element)m_channelHashMap.get(channelID));
  }


  /** 
   * A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param catID an array of category IDs
   * @param chanXML XML that describes the channel
   * @param role an array of roles
   */
  public void addChannel (int id, String title, Document doc, String categoryID[]) {
    return;
  }

  /** 
   * A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param chanXML XML that describes the channel
   */
  public void addChannel (int id, String title, Document doc) {
    return;
  }

  /** 
   * A method for removing a channel from the registry.
   * This could be used by an admin channel to unpublish a channel from
   * certain categories, roles, or just remove it altogether.
   * @param catID an array of category IDs
   * @param chanID a channel ID
   * @param role an array of roles
   */
  public void removeChannel (String catID[], String chanID) {
    return;
  }

  /** 
   * A method for persiting the channel registry to a file or database.
   * @param registryXML an XML description of the channel registry
   */
  public void setRegistryDOM (Document registryDOM) {
    return;
  }

  /**
   * Get an ID that is unique in the channel registry DOM
   * @return 
   */
  private synchronized String getUniqueID () {
    String uniqueID = System.currentTimeMillis() + "";
    while (m_channelRegistryDOM.getElementById(uniqueID) != null) {
      uniqueID = System.currentTimeMillis() + "";
    }
    return  ("u" + uniqueID);
  }
}



