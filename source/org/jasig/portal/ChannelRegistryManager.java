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
 */

package  org.jasig.portal;

import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import java.util.Set;
import java.sql.SQLException;
import org.apache.xpath.XPathAPI;

/**
 * Manages the channel registry which is a listing of published channels
 * that one can subscribe to (add to their layout).
 * Also currently manages the channel types data. (maybe this should be managed
 * by another class  -Ken)
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelRegistryManager {
  protected static final IChannelRegistryStore chanRegStore = RdbmServices.getChannelRegistryStoreImpl();
  protected static final int registryCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_registry_cache_timeout");
  protected static final int chanTypesCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_types_cache_timeout");
  protected static final SmartCache channelRegistryCache = new SmartCache(registryCacheTimeout);
  protected static final SmartCache channelTypesCache = new SmartCache(chanTypesCacheTimeout);
  private static final String CHANNEL_REGISTRY_CACHE_KEY = "channelRegistryCacheKey";
  private static final String CHANNEL_TYPES_CACHE_KEY = "channelTypesCacheKey";

  /**
   * Returns the channel registry as a Document.  This list is not filtered by roles.
   * @return the channel registry as a Document
   */
  public static Document getChannelRegistry() throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    if (channelRegistry == null)
    {
      // Channel registry has expired, so get it and cache it
      try {
        channelRegistry = chanRegStore.getChannelRegistryXML();
      } catch (Exception e) {
        throw new GeneralRenderingException(e.getMessage());
      }

      if (channelRegistry != null)
      {
        channelRegistryCache.put(CHANNEL_REGISTRY_CACHE_KEY, channelRegistry);
        LogService.instance().log(LogService.INFO, "Caching channel registry.");
      }
    }
    return channelRegistry;
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param the channel ID
   * @return the channel element matching chanID
   * @throws PortalException
   */
  public static Element getChannel (String chanID) throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    Element channelE = null;
    try {
      // This is unfortunately dependent on Xalan 2.  Is there a way to use a standard interface?
      channelE = (Element)XPathAPI.selectSingleNode(channelRegistry, "(//channel[@ID = '" + chanID + "'])[1]");
    } catch (javax.xml.transform.TransformerException te) {
      throw new GeneralRenderingException("Not able to find channel " + chanID + " within channel registry: " + te.getMessageAndLocation());
    }
    return channelE;
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param the channel ID
   * @return the channel element matching chanID
   * @throws org.jasig.portal.PortalException
   */
  public static NodeList getCategories (String chanID) throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    NodeList categories = null;
    try {
      // This is unfortunately dependent on Xalan 2.  Is there a way to use a standard interface?
      categories = (NodeList)XPathAPI.selectNodeList(channelRegistry, "//category[channel/@ID = '" + chanID + "']");
    } catch (javax.xml.transform.TransformerException te) {
      throw new GeneralRenderingException("Not able to find channel " + chanID + " within channel registry: " + te.getMessageAndLocation());
    }
    return categories;
  }

  /**
   * Publishes a channel.
   * @param the channel XML fragment
   * @param a list of categories that the channel belongs to
   * @param a list of roles that are permitted to subscribe to the channel
   * @param the user ID of the channel publisher
   * @throws java.lang.Exception
   */
  public static void publishChannel (Element channel, Set categoryIDs, Set roles, int publisherID) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);

    // Use current channel ID if modifying previously published channel, otherwise get a new ID
    int ID = 0;
    String chanID = channel.getAttribute("ID");
    if (chanID != null && chanID.trim().length() > 0) {
      ID = Integer.parseInt(chanID.startsWith("chan") ? chanID.substring(4) : chanID);
      LogService.instance().log(LogService.INFO, "Attempting to modify channel " + ID + "...");
    }
    else {
      ID = chanRegStore.getNextId();
      LogService.instance().log(LogService.INFO, "Attempting to publish new channel " + ID + "...");
    }

    // Add channel
    String[] catIDs = (String[])categoryIDs.toArray(new String[0]);
    Document channelDoc = DocumentFactory.getNewDocument();
    channelDoc.appendChild(channelDoc.importNode(channel, true));
    chanRegStore.addChannel(ID, publisherID, channelDoc, catIDs);

    // Set roles
    java.util.Vector vRoles = new java.util.Vector(roles);
    int rolesSet = new org.jasig.portal.services.Authorization().setChannelRoles(ID, vRoles);

    // Approve channel
    chanRegStore.approveChannel(ID, publisherID, new java.sql.Timestamp(System.currentTimeMillis()));

    LogService.instance().log(LogService.INFO, "Channel " + ID + " has been published/modified.");
  }

  /**
   * Removes a channel from the channel registry.
   * @param channel ID, the channel ID
   * @throws java.lang.Exception
   */
  public static void removeChannel (String channelID) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);
    // Remove the channel
    chanRegStore.removeChannel(channelID);
  }

  /**
   * Returns the publishable channel types as a Document.
   * @return a list of channel types as a Document
   */
  public static Document getChannelTypes() throws PortalException {
    Document channelTypes = (Document)channelTypesCache.get(CHANNEL_TYPES_CACHE_KEY);
    if (channelTypes == null)
    {
      // Channel types doc has expired, so get it and cache it
      try {
        channelTypes = chanRegStore.getChannelTypesXML();
      } catch (Exception e) {
        throw new GeneralRenderingException(e.getMessage());
      }

      if (channelTypes != null)
      {
        channelTypesCache.put(CHANNEL_TYPES_CACHE_KEY, channelTypes);
        LogService.instance().log(LogService.INFO, "Caching channel types.");
      }
    }
    return channelTypes;
  }
}






