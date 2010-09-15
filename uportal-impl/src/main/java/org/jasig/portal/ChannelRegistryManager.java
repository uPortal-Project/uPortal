/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.jasig.portal;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.ModifiedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedChannelDefinitionPortalEvent;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.CommonUtils;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SmartCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Manages the channel registry which is a listing of published channels
 * that one can subscribe to (add to their layout).
 * Also currently manages the channel types data and CPD documents.
 * (maybe these should be managed by another class  -Ken)
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public class ChannelRegistryManager {
    
    private static final Log log = LogFactory.getLog(ChannelRegistryManager.class);
    
  protected static final IChannelRegistryStore crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();

  /**
     * Default value for registryCacheTimeout.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final int DEFAULT_REGISTRY_CACHE_TIMEOUT = 900;
    
    /**
     * Default value for chanTypesCacheTimeout.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final int DEFAULT_CHAN_TYPES_CACHE_TIMEOUT = 900;
    
    /**
     * Default value for cpdCacheTimeout.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final int DEFAULT_CPD_CACHE_TIMEOUT = 900;
    
    /**
     * Default value for localeAware.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final boolean DEFAULT_LOCALE_AWARE = false;

  // Cache timeout properties
  /**
   * Timeout, in seconds, of the cache of the registry of channels.
   */
  protected static final int registryCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_registry_cache_timeout", DEFAULT_REGISTRY_CACHE_TIMEOUT);

  /**
   * Timeout, in seconds, of the cache of channel types.
   */
  protected static final int chanTypesCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_types_cache_timeout", DEFAULT_CHAN_TYPES_CACHE_TIMEOUT);

  /**
   * Timeout, in seconds, of the cache of channel publishing documents.
   */
  protected static final int cpdCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.cpd_cache_timeout", DEFAULT_CPD_CACHE_TIMEOUT);

  // i18n properties
  
  /**
   * Boolean indicating whether internationalization will be used.
   */
  protected static final boolean localeAware = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.i18n.LocaleManager.locale_aware", DEFAULT_LOCALE_AWARE);

  
  
 
  // Caches
  protected static final SmartCache channelRegistryCache = new SmartCache(registryCacheTimeout);
  protected static final SmartCache channelTypesCache = new SmartCache(chanTypesCacheTimeout);
  protected static final SmartCache cpdCache = new SmartCache(cpdCacheTimeout);

  // Cache keys
  private static final String CHANNEL_REGISTRY_CACHE_KEY = "channelRegistryCacheKey";
  private static final String CHANNEL_TYPES_CACHE_KEY = "channelTypesCacheKey";
  private static final String CPD_CACHE_KEY = "cpdCacheKey";

  // Permission constants
  private static final String FRAMEWORK_OWNER = "UP_FRAMEWORK";
  private static final String SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
  private static final String GRANT_PERMISSION_TYPE = "GRANT";

  /**
   * Returns a copy of the channel registry as a Document.
   * This document is not filtered according to a user's channel permissions.
   * For a filtered list, see  <code>getChannelRegistry(IPerson person)</code>
   * @return a copy of the channel registry as a Document
   */
  public static Document getChannelRegistry() throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    if (channelRegistry == null) {
      // Channel registry has expired, so get it and cache it
      try {
        channelRegistry = getChannelRegistryXML();
      } catch (Exception e) {
        throw new PortalException(e);
      }

      if (channelRegistry != null) {
        channelRegistryCache.put(CHANNEL_REGISTRY_CACHE_KEY, channelRegistry);
        log.info( "Caching channel registry.");
      } else {
    	  throw new PortalException("unable to get registry (null)");
      }
    }

    // Clone the original registry document so that it doesn't get modified
    return (Document)channelRegistry.cloneNode(true);
  }

  /**
   * Returns the channel registry as a Document.  This document is filtered
   * according to a user's channel permissions.
   * @return the filtered channel registry as a Document
   */
  public static Document getChannelRegistry(IPerson person) throws PortalException {
    Document channelRegistry = getChannelRegistry();

    // Filter the channel registry according to permissions
    EntityIdentifier ei = person.getEntityIdentifier();
    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

    // Cycle through all the channels, looking for restricted channels
    NodeList nl = channelRegistry.getElementsByTagName("channel");
    for (int i = (nl.getLength()-1); i >=0; i--) {
      Element channel = (Element)nl.item(i);
      String channelPublishId = channel.getAttribute("chanID");
      channelPublishId = channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId;

      // Take out channels which user doesn't have access to
      if (!ap.canSubscribe(Integer.parseInt(channelPublishId)))
        channel.getParentNode().removeChild(channel);
    }

    return channelRegistry;
  }

  /**
   * Returns the channel registry as a Document.  This document is filtered
   * according to a user's channel permissions.
   * @return the filtered channel registry as a Document
   */
  public static Document getManageableChannelRegistry(IPerson person) throws PortalException {
    Document channelRegistry = getChannelRegistry();

    // Filter the channel registry according to permissions
    EntityIdentifier ei = person.getEntityIdentifier();
    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

    // Cycle through all the channels, looking for restricted channels
    NodeList nl = channelRegistry.getElementsByTagName("channel");
    for (int i = (nl.getLength()-1); i >=0; i--) {
      Element channel = (Element)nl.item(i);
      String channelPublishId = channel.getAttribute("chanID");
      channelPublishId = channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId;

      // Take out channels which user doesn't have access to
      if (!ap.canManage(Integer.parseInt(channelPublishId)))
        channel.getParentNode().removeChild(channel);
    }

    return channelRegistry;
  }

  /**
   * Returns an XML document which describes the channel registry.
   * See uPortal's <code>channelRegistry.dtd</code>
   * @return doc the channel registry document
   * @throws java.lang.Exception
   */
  public static Document getChannelRegistryXML() throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element registry = doc.createElement("registry");
    doc.appendChild(registry);

    IEntityGroup channelCategoriesGroup = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
    processGroupsRecursively(channelCategoriesGroup, doc, registry);

    return doc;
  }

  private static void processGroupsRecursively(IEntityGroup group,
    Document owner, Element parentGroup) throws Exception {
    Date now = new Date();
    Iterator iter = group.getMembers();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup()) {
        IEntityGroup memberGroup = (IEntityGroup)member;
        String key = memberGroup.getKey();
        String name = memberGroup.getName();
        String description = memberGroup.getDescription();

        // Create category element and append it to its parent
        Element categoryE = owner.createElement("category");
        categoryE.setAttribute("ID", "cat" + key);
        categoryE.setIdAttribute("ID", true);
        categoryE.setAttribute("name", name);
        categoryE.setAttribute("description", description);
        parentGroup.appendChild(categoryE);
        processGroupsRecursively(memberGroup, owner, categoryE);
      } else {
        IEntity channelDefMember = (IEntity)member;
        int channelPublishId = CommonUtils.parseInt(channelDefMember.getKey());
        if ( channelPublishId > 0 ) {
         IChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
         if (channelDef != null) {
          // Make sure channel is approved
          Date approvalDate = channelDef.getApprovalDate();
          if (approvalDate != null && approvalDate.before(now)) {
            Element channelDefE = channelDef.getDocument(owner, "chan" + channelPublishId);
            channelDefE = (Element)owner.importNode(channelDefE, true);
            
            if (channelDefE.getAttribute("ID") != null)
                channelDefE.setIdAttribute("ID", true);
            
            parentGroup.appendChild(channelDefE);
          }
         }
        }
      }
    }
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param channelPublishId the channel publish id
   * @return the channel element matching specified channel publish id
   * @throws PortalException
   */
  public static Element getChannel (String channelPublishId) throws PortalException {
    Document channelRegistry = getChannelRegistry();
    Element channelE = null;
    try {
        String expression = "(//channel[@ID = '" + channelPublishId + "'])[1]";
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        channelE = (Element) xpath.evaluate(expression, channelRegistry,
                XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new GeneralRenderingException("Not able to find channel " + channelPublishId + " within channel registry.", e);
    }
    return channelE;
  }

  /**
   * Create XML representing this channel definition.
   * I don't think this method really belongs in the
   * ChannelRegistryManager since this XML fragment is
   * related more to a channel instance, but we'll hold
   * it here for now and find a better place for it later :)
   * @param subscribeId the channel subscibe ID, formerly called instance ID
   * @param channelDef a channel definition
   * @return the XML representing this channel definition
   */
  public static Element getChannelXML(String subscribeId, IChannelDefinition channelDef) {
    Document doc = DocumentFactory.getNewDocument();
    Element channelE = doc.createElement("channel");
    channelE.setAttribute("ID", subscribeId);
    channelE.setAttribute("chanID", String.valueOf(channelDef.getId()));
    channelE.setAttribute("timeout", String.valueOf(channelDef.getTimeout()));
    // I18n
    if (localeAware) {
        String locale=channelDef.getLocale();
        channelE.setAttribute("name", channelDef.getName(locale));
        channelE.setAttribute("title", channelDef.getTitle(locale));
        channelE.setAttribute("locale", locale);
        if (log.isDebugEnabled())
            log.debug("ChannelRegistryManager::getChannelXML: locale=" + locale);
    }  else {
        channelE.setAttribute("name", channelDef.getName());
        channelE.setAttribute("title", channelDef.getTitle());
    }
    channelE.setAttribute("fname", channelDef.getFName());
    channelE.setAttribute("class", channelDef.getJavaClass());
    channelE.setAttribute("typeID", String.valueOf(channelDef.getTypeId()));
    channelE.setAttribute("editable", channelDef.isEditable() ? "true" : "false");
    channelE.setAttribute("hasHelp", channelDef.hasHelp() ? "true" : "false");
    channelE.setAttribute("hasAbout", channelDef.hasAbout() ? "true" : "false");
    channelE.setAttribute("secure", channelDef.isSecure() ? "true" : "false");
    channelE.setAttribute("isPortlet", channelDef.isPortlet() ? "true" : "false");
    
    // Add any parameters
    Set<IChannelParameter> parameters = channelDef.getParameters();
    for (IChannelParameter cp : parameters) {
      Element parameterE = doc.createElement("parameter");
      parameterE.setAttribute("name", cp.getName());
      parameterE.setAttribute("value", cp.getValue());
      if (cp.getOverride()) {
        parameterE.setAttribute("override", "yes");
      }
      channelE.appendChild(parameterE);
    }

    return channelE;
  }

  /**
   * Create XML representing the channel types.
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
   * @return channelTypesXML, the XML representing the channel types
   * @throws java.lang.Exception
   */
  public static Document getChannelTypesXML() throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element channelTypesE = doc.createElement("channelTypes");

    List<IChannelType> channelTypes = crs.getChannelTypes();
    for (IChannelType channelType : channelTypes) {
      int channelTypeId = channelType.getId();
      String javaClass = channelType.getJavaClass();
      String name = channelType.getName();
      String descr = channelType.getDescription();
      String cpdUri = channelType.getCpdUri();

      // <channelType>
      Element channelTypeE = doc.createElement("channelType");
      channelTypeE.setAttribute("ID", String.valueOf(channelTypeId));

      // <class>
      Element classE = doc.createElement("class");
      classE.appendChild(doc.createTextNode(javaClass));
      channelTypeE.appendChild(classE);

      // <name>
      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(name));
      channelTypeE.appendChild(nameE);

      // <description>
      Element descriptionE = doc.createElement("description");
      descriptionE.appendChild(doc.createTextNode(descr));
      channelTypeE.appendChild(descriptionE);

      // <cpd-uri>
      Element cpdUriE = doc.createElement("cpd-uri");
      cpdUriE.appendChild(doc.createTextNode(cpdUri));
      channelTypeE.appendChild(cpdUriE);

      channelTypesE.appendChild(channelTypeE);
    }
    doc.appendChild(channelTypesE);

    return doc;
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param channelPublishId the channel publish ID
   * @return the channel element matching chanID
   * @throws org.jasig.portal.PortalException
   */
  public static NodeList getCategories(String channelPublishId) throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    NodeList categories = null;
    try {
        String expression = "//category[channel/@ID = '" + channelPublishId + "']";
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        categories = (NodeList) xpath.evaluate(expression, channelRegistry, XPathConstants.NODESET);
        
    } catch (XPathExpressionException e) {
        throw new GeneralRenderingException("Not able to find channel " + channelPublishId + " within channel registry.", e);
    }
    return categories;
  }

  /**
   * Removes a channel from the channel registry.
   * @param channelID the channel ID
   * @param person the person removing the channel
   * @throws java.lang.Exception
   */
  public static void removeChannel (String channelID, IPerson person) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);
    // Remove the channel
    String sChannelPublishId = channelID.startsWith("chan") ? channelID.substring(4) : channelID;
    int channelPublishId = Integer.parseInt(sChannelPublishId);
    IChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
    crs.disapproveChannelDefinition(channelDef);

    // Record that a channel has been deleted
    EventPublisherLocator.getApplicationEventPublisher().publishEvent(new RemovedChannelDefinitionPortalEvent(channelDef, person, channelDef));
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
        channelTypes = getChannelTypesXML();
      } catch (Exception e) {
        throw new PortalException(e);
      }

      if (channelTypes != null)
      {
        channelTypesCache.put(CHANNEL_TYPES_CACHE_KEY, channelTypes);
        if (log.isInfoEnabled())
            log.info( "Caching channel types.");
      } else {
    	  throw new PortalException("unable to get channelTypes (null)");
      }
    }

    // Clone the original channel types document so that it doesn't get modified
    return (Document)channelTypes.cloneNode(true);
  }

  /**
   * Returns a CPD (channel publishing document) as a Document
   * @param chanTypeID the channel type ID, "-1" if channel type is "custom"
   * @return the CPD document matching the chanTypeID, <code>null</code> if "custom" channel
   * @throws org.jasig.portal.PortalException
   */
  public static Document getCPD(String chanTypeID) throws PortalException {
    //  There are no CPD docs for custom channels (chanTypeID = -1)
    if (chanTypeID == null || chanTypeID.equals("-1"))
      return null;

    Document cpd = (Document)cpdCache.get(CPD_CACHE_KEY + chanTypeID);
    if (cpd == null) {
      // CPD doc has expired, so get it and cache it
      Element channelTypes = getChannelTypes().getDocumentElement();

      // Look for channel type element matching the channel type ID
      Element chanType = null;

      for (Node n = channelTypes.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("channelType")) {
          chanType = (Element)n;
          if (chanTypeID.equals(chanType.getAttribute("ID")))
            break;
        }
      }

      // Find the cpd-uri within this element
      String cpdUri = null;
      for (Node n = chanType.getLastChild(); n != null; n = n.getPreviousSibling()) {
        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("cpd-uri")) {
          // Found the <cpd-uri> element, now get its value
          for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
            if (m instanceof Text)
              cpdUri = m.getNodeValue();
          }
          break;
        }
      }

      if (cpdUri != null) {
        try {
          cpd = ResourceLoader.getResourceAsDocument(ChannelRegistryManager.class, cpdUri, true);
        } catch (java.io.IOException ioe) {
          throw new ResourceMissingException(cpdUri, "Channel publishing document", ioe);
        } catch (org.xml.sax.SAXException se) {
          throw new PortalException("Unable to parse CPD file: " + cpdUri, se);
        } catch (ParserConfigurationException pce) {
          throw new PortalException("Unable to parse CPD file: " + cpdUri, pce);
        }
      }

      if (cpd != null) {
        cpdCache.put(CPD_CACHE_KEY + chanTypeID, cpd);
        if (log.isInfoEnabled())
            log.info( "Caching CPD for channel type " + chanTypeID);
      }else {
    	  throw new PortalException("unable to get Channel Publishing Document (null)");
      }
    }

    // Clone the original CPD document so that it doesn't get modified
    return (Document)cpd.cloneNode(true);
  }
  
  public static void expireCache() {
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);	  
  }
}






