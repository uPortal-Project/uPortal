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

package  org.jasig.portal;

import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.CounterStoreFactory;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.GroupsException;

/**
 * Reference implementation of IChannelRegistryStoreOld.  This class is currently
 * acting as a wrapper for the real IChannelRegistryStore and will be eventually
 * removed.
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 * @deprecated Use {@link RDBMChannelRegistryStore} instead
 */
public class RDBMChannelRegistryStoreOld implements IChannelRegistryStoreOld {
  private static final int DEBUG = 0;

  private static final Object channelLock = new Object();
  private static final HashMap channelCache = new HashMap();

  protected static IChannelRegistryStore crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();

  /**
   * Returns an XML document which describes the channel registry.
   * Right now this is being stored as a string in a field but could be also implemented to get from multiple tables.
   * @return a string of XML
   * @throws java.lang.Exception
   */
  public Document getChannelRegistryXML () throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element registry = doc.createElement("registry");
    doc.appendChild(registry);

    IEntityGroup channelCategoriesGroup = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
    processGroupsRecursively(channelCategoriesGroup, registry);

    return doc;
  }

  private void processGroupsRecursively(IEntityGroup group, Element parentGroup) throws Exception {
    Document registryDoc = parentGroup.getOwnerDocument();
    Iterator iter = group.getMembers();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup()) {
        IEntityGroup memberGroup = (IEntityGroup)member;
        String key = memberGroup.getKey();
        String name = memberGroup.getName();
        String description = memberGroup.getDescription();

        // Create category element and append it to its parent
        Element categoryE = registryDoc.createElement("category");
        categoryE.setAttribute("ID", "cat" + key);
        categoryE.setAttribute("name", name);
        categoryE.setAttribute("description", description);
        parentGroup.appendChild(categoryE);
        processGroupsRecursively(memberGroup, categoryE);
      } else {
        IEntity channelDefMember = (IEntity)member;
        int channelPublishId = Integer.parseInt(channelDefMember.getKey());
        ChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
        if (channelDef != null) {
          // Make sure channel is approved
          Date approvalDate = channelDef.getApprovalDate();
          if (approvalDate != null && approvalDate.before(new Date())) {
            Element channelDefE = channelDef.getDocument(registryDoc, "chan" + channelPublishId);
            channelDefE = (Element)registryDoc.importNode(channelDefE, true);
            parentGroup.appendChild(channelDefE);
          }
        }
      }
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
  public Document getChannelTypesXML () throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element root = doc.createElement("channelTypes");
    doc.appendChild(root);

    ChannelType[] channelTypes = crs.getChannelTypes();
    for (int i = 0; i < channelTypes.length; i++) {
      int channelTypeId = channelTypes[i].getId();
      String javaClass = channelTypes[i].getJavaClass();
      String name = channelTypes[i].getName();
      String descr = channelTypes[i].getDescription();
      String cpdUri = channelTypes[i].getCpdUri();

      // <channelType>
      Element channelType = doc.createElement("channelType");
      channelType.setAttribute("ID", String.valueOf(channelTypeId));
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
    Element channel = (Element)chanXML.getFirstChild();

    String chanTitle = channel.getAttribute("title");
    String chanDesc = channel.getAttribute("description");
    String chanClass = channel.getAttribute("class");
    int chanTypeId = Integer.parseInt(channel.getAttribute("typeID"));
    int chanPupblUsrId = publisher.getID();
    int chanApvlId = -1;
    String timeout = channel.getAttribute("timeout");
    int chanTimeout = 0;
    if (timeout != null && timeout.trim().length() != 0) {
      chanTimeout  = Integer.parseInt(timeout);
    }
    String chanEditable = channel.getAttribute("editable");
    String chanHasHelp = channel.getAttribute("hasHelp");
    String chanHasAbout = channel.getAttribute("hasAbout");
    String chanName = channel.getAttribute("name");
    String chanFName = channel.getAttribute("fname");

    ChannelDefinition channelDef = new ChannelDefinition(id);
    channelDef.setTitle(chanTitle);
    channelDef.setDescription(chanDesc);
    channelDef.setJavaClass(chanClass);
    channelDef.setTypeId(chanTypeId);
    channelDef.setPublisherId(chanPupblUsrId);
    channelDef.setApproverId(chanApvlId);
    channelDef.setTimeout(chanTimeout);
    channelDef.setEditable(chanEditable != null && chanEditable.equals("true") ? true : false);
    channelDef.setHasHelp(chanHasHelp != null && chanHasHelp.equals("true") ? true : false);
    channelDef.setHasAbout(chanHasAbout != null && chanHasAbout.equals("true") ? true : false);
    channelDef.setName(chanName);
    channelDef.setFName(chanFName);

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

          channelDef.addParameter(paramName, paramValue, paramOverride);
        }
      }
    }

    crs.saveChannelDefinition(channelDef);

    // Delete existing category memberships for this channel
    String channelDefEntityKey = String.valueOf(channelDef.getId());
    IEntity channelDefEntity = GroupService.getEntity(channelDefEntityKey, ChannelDefinition.class);
    IEntityGroup topLevelCategory = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
    Iterator iter = topLevelCategory.getAllMembers();
    while (iter.hasNext()) {
      IGroupMember groupMember = (IGroupMember)iter.next();
      if (groupMember.isGroup()) {
        IEntityGroup group = (IEntityGroup)groupMember;
        group.removeMember(channelDefEntity);
        group.updateMembers();
      }
    }

    // For each category ID, add channel to category
    for (int i = 0; i < catID.length; i++) {
      catID[i] = catID[i].startsWith("cat") ? catID[i].substring(3) : catID[i];
      int iCatID = Integer.parseInt(catID[i]);
      ChannelCategory category = crs.getChannelCategory(iCatID);
      crs.addChannelToCategory(channelDef, category);
    }

    flushChannelEntry(id);
  }

  /** A method for approving a channel so that users are allowed to subscribe to it.
   *  This would be called by the publish channel or the administrator channel
   *  @param chanId Channel to approve
   *  @param approved Account approving the channel
   *  @param approveDate When should the channel appear
   */
  public void approveChannel(int chanId, IPerson approver, Date approveDate) throws Exception {
    crs.approveChannelDefinition(crs.getChannelDefinition(chanId), approver, approveDate);
    flushChannelEntry(chanId);
  }

  /** A method for getting the next available channel ID.
   * This would be called by a publish channel.
   */
  public int getNextId () throws PortalException {
    int nextID;
    try {
      nextID = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_CHANNEL");
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      throw  new PortalException("Unable to allocate new channel ID", e);
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
    String sChannelPublishId = chanID.startsWith("chan") ? chanID.substring(4) : chanID;
    int channelPublishId = Integer.parseInt(sChannelPublishId);
    crs.disapproveChannelDefinition(crs.getChannelDefinition(channelPublishId));
    flushChannelEntry(channelPublishId);
  }

  /**
   * A method for persisting the channel registry to a file or database.
   * @param registryXML an XML description of the channel registry
   */
  public void setRegistryXML (String registryXML) throws Exception{
    throw new Exception("not implemented yet");
  }

  /**
   * @param chanDoc
   * @return the chanDoc as an XML string
   */
  private String serializeDOM (Document chanDoc) {
    StringWriter stringOut = null;
    try {
      OutputFormat format = new OutputFormat(chanDoc); //Serialize DOM
      stringOut = new StringWriter(); //Writer will be a String
      XMLSerializer serial = new XMLSerializer(stringOut, format);
      serial.asDOMSerializer(); // As a DOM Serializer
      serial.serialize(chanDoc.getDocumentElement());
    } catch (java.io.IOException ioe) {
      LogService.instance().log(LogService.ERROR, ioe);
    }

    return  stringOut.toString();
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
  public ChannelDefinition getChannel(int chanId) {
    ChannelDefinition channelDef = null;
    try {
      Integer chanID = new Integer(chanId);
      channelDef  = (ChannelDefinition)channelCache.get(chanID);
      if (channelDef == null) {
        synchronized (channelLock) {
          channelDef = (ChannelDefinition)channelCache.get(chanID);
        }
      }
      if (channelDef == null) {
        throw new Exception("Channel " + chanID + " not in cache");
      }
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
    }
    return channelDef;
  }

  /**
   * Get a channel from the cache (it better be there)
   */
  public Element getChannelXML(int chanId, Document doc, String idTag) {
    ChannelDefinition channel = getChannel(chanId);
    if (channel != null) {
      return channel.getDocument(doc, idTag);
    } else {
      return null;
    }
  }

  /**
   * Get a channel from the cache or the store
   */
  public ChannelDefinition getChannel(int chanId, boolean cacheChannel, RDBMServices.PreparedStatement pstmtChannel, RDBMServices.PreparedStatement pstmtChannelParm) throws java.sql.SQLException {
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
        "RDBMChannelRegistryStore.getChannelDefinition(): Got channel " + chanId + " from the cache");
    }

    return channel;
  }

  /**
   * Read a channel definition from the data store
   */
  protected ChannelDefinition getChannelDefinition (int chanId, RDBMServices.PreparedStatement pstmtChannel, RDBMServices.PreparedStatement pstmtChannelParm) throws java.sql.SQLException {
    try {
      return crs.getChannelDefinition(chanId);
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
      throw new SQLException(e.getMessage());
    }
  }

  public final RDBMServices.PreparedStatement getChannelParmPstmt(Connection con) throws SQLException {
    return RDBMChannelRegistryStore.getChannelParamPstmt(con);
  }

  public final RDBMServices.PreparedStatement getChannelPstmt(Connection con) throws SQLException {
    return RDBMChannelRegistryStore.getChannelPstmt(con);
  }
}
