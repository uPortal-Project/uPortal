/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.ModifiedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedChannelDefinitionPortalEvent;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletPreference;
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
 */
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
         ChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
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
  public static Element getChannelXML(String subscribeId, ChannelDefinition channelDef) {
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
    ChannelParameter[] parameters = channelDef.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      ChannelParameter cp = parameters[i];
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
   * Update a channel definition with data from a channel XML
   * element.  I don't think this method really belongs in the
   * ChannelRegistryManager since this XML fragment contains
   * a channel subscribe ID, but we'll hold it here for now
   * and find a better place for it later :)
   * Note that this method does not set the ID, publisher ID,
   * approver ID, pubish date, or approval date.
   * @param channelE an XML element representing a channel definition
   * @param channelDef the channel definition to update
   */
  public static void setChannelXML(Element channelE, ChannelDefinition channelDef) {
    channelDef.setFName(channelE.getAttribute("fname"));
    channelDef.setName(channelE.getAttribute("name"));
    channelDef.setDescription(channelE.getAttribute("description"));
    channelDef.setTitle(channelE.getAttribute("title"));
    channelDef.setJavaClass(channelE.getAttribute("class"));

    String timeout = channelE.getAttribute("timeout");
    if (timeout != null && timeout.trim().length() != 0) {
      channelDef.setTimeout(Integer.parseInt(timeout));
    }

    String secure = channelE.getAttribute("secure");
    channelDef.setIsSecure(secure != null && secure.equals("true") ? true : false);
    
    channelDef.setTypeId(Integer.parseInt(channelE.getAttribute("typeID")));
    String chanEditable = channelE.getAttribute("editable");
    String chanHasHelp = channelE.getAttribute("hasHelp");
    String chanHasAbout = channelE.getAttribute("hasAbout");
    channelDef.setEditable(chanEditable != null && chanEditable.equals("true") ? true : false);
    channelDef.setHasHelp(chanHasHelp != null && chanHasHelp.equals("true") ? true : false);
    channelDef.setHasAbout(chanHasAbout != null && chanHasAbout.equals("true") ? true : false);
    // I18n
    if (localeAware) {
        channelDef.setLocale(channelE.getAttribute("locale"));
    }

    // Now set the channel parameters
    channelDef.clearParameters();
    NodeList channelChildren = channelE.getChildNodes();
    if (channelChildren != null) {
      for (int i = 0; i < channelChildren.getLength(); i++) {
        if (channelChildren.item(i).getNodeName().equals("parameter")) {
          Element parameterE = (Element)channelChildren.item(i);
          NamedNodeMap parameterAtts = parameterE.getAttributes();
          String paramName = null;
          String paramValue = null;
          String paramOverride = "NULL";

          for (int j = 0; j < parameterAtts.getLength(); j++) {
            Node parameterAtt = parameterAtts.item(j);
            String parameterAttName = parameterAtt.getNodeName();
            String parameterAttValue = parameterAtt.getNodeValue();

            if (parameterAttName.equals("name")) {
              paramName = parameterAttValue;
            } else if (parameterAttName.equals("value")) {
              paramValue = parameterAttValue;
            } else if (parameterAttName.equals("override") && parameterAttValue.equals("yes")) {
              paramOverride = "Y";
            }
          }

          if (paramName == null && paramValue == null) {
            throw new RuntimeException("Invalid parameter node");
          }

          channelDef.addParameter(paramName, paramValue, paramOverride);
        }
      }
    }
    
    final List<IPortletPreference> portletPreferences = new LinkedList<IPortletPreference>();
    final NodeList definitionPrefsNodes = channelE.getElementsByTagName("definitionPreferences");
    if (definitionPrefsNodes.getLength() > 2) {
        throw new IllegalArgumentException("There should be only one 'definitionPreferences' element in a ChannelDefinition element");
    }
    else if (definitionPrefsNodes.getLength() == 1) {
        final Node definitionPrefsNode = definitionPrefsNodes.item(0);
        final NodeList prefNodes = definitionPrefsNode.getChildNodes();
        for (int prefIndex = 0; prefIndex < prefNodes.getLength(); prefIndex++) {
            final Node prefNode = prefNodes.item(prefIndex);
            final NamedNodeMap attributes = prefNode.getAttributes();
            
            final Node nameNode = attributes.getNamedItem("name");
            if (nameNode == null) {
                throw new IllegalArgumentException("ChannelDefinition preference nodes must have a 'name' attribute");
            }
            final String name = nameNode.getNodeValue();
            
            final Node readOnlyNode = attributes.getNamedItem("read-only");
            final boolean readOnly = readOnlyNode != null && Boolean.valueOf(readOnlyNode.getNodeValue());
            
            final NodeList valuesNodes = prefNode.getChildNodes();
            if (valuesNodes.getLength() == 0) {
                final IPortletPreference portletPreference = new PortletPreferenceImpl(name, readOnly);
                portletPreferences.add(portletPreference);
            }
            else {
                final NodeList valueNodes = valuesNodes.item(0).getChildNodes();
                final List<String> values = new LinkedList<String>();
                
                for (int valueIndex = 0; valueIndex < valueNodes.getLength(); valueIndex++) {
                    final Node valueNode = valueNodes.item(valueIndex);
                    final String value = valueNode.getTextContent();
                    values.add(value);
                }

                final IPortletPreference portletPreference = new PortletPreferenceImpl(name, readOnly, values.toArray(new String[values.size()]));
                portletPreferences.add(portletPreference);
            }
        }
    }
    
    channelDef.replacePortletPreference(portletPreferences);
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

    ChannelType[] channelTypes = crs.getChannelTypes();
    for (int i = 0; i < channelTypes.length; i++) {
      int channelTypeId = channelTypes[i].getId();
      String javaClass = channelTypes[i].getJavaClass();
      String name = channelTypes[i].getName();
      String descr = channelTypes[i].getDescription();
      String cpdUri = channelTypes[i].getCpdUri();

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
   * Publishes a channel.
   * @param channel the channel XML fragment
   * @param categoryIDs a list of categories that the channel belongs to
   * @param groupMembers a list of groups and/or people that are permitted to subscribe to and view the channel
   * @param publisher the user ID of the channel publisher
   * @throws java.lang.Exception
   */
  public static void publishChannel (Element channel, String[] categoryIDs, IGroupMember[] groupMembers, IPerson publisher) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);

    ChannelDefinition channelDef = null;

    // Use current channel ID if modifying previously published channel, otherwise get a new ID
    boolean newChannel = true;
    int ID = 0;
    String channelPublishId = channel.getAttribute("ID");
    if (channelPublishId != null && channelPublishId.trim().length() > 0) {
      newChannel = false;
      ID = Integer.parseInt(channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId);
      channelDef = crs.getChannelDefinition(ID);
      if (log.isDebugEnabled())
          log.debug("Attempting to modify channel " + ID + "...");
    }
    else {
      channelDef = crs.newChannelDefinition();
      ID = channelDef.getId();
      if (log.isDebugEnabled())
          log.debug("Attempting to publish new channel " + ID + "...");
    }

    // Add channel
    setChannelXML(channel, channelDef);
    Date now = new Date();
    channelDef.setPublisherId(publisher.getID());
    channelDef.setPublishDate(now);
    channelDef.setApproverId(publisher.getID());
    channelDef.setApprovalDate(now);
    crs.saveChannelDefinition(channelDef);

    // Delete existing category memberships for this channel   
    String chanKey = String.valueOf(channelDef.getId());
    IEntity channelDefEntity = GroupService.getEntity(chanKey, ChannelDefinition.class);
    Iterator iter = channelDefEntity.getAllContainingGroups();
    while (iter.hasNext()) {
        IEntityGroup group = (IEntityGroup) iter.next();
        group.removeMember(channelDefEntity);
        group.update();
    }

    // For each category ID, add channel to category
    for (int i = 0; i < categoryIDs.length; i++) {
      categoryIDs[i] = categoryIDs[i].startsWith("cat") ? categoryIDs[i].substring(3) : categoryIDs[i];
      String iCatID = categoryIDs[i];
      ChannelCategory category = crs.getChannelCategory(iCatID);
      crs.addChannelToCategory(channelDef, category);
    }

    // Set groups
    AuthorizationService authService = AuthorizationService.instance();
    String target = IPermission.CHANNEL_PREFIX + ID;
    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
    IPermission[] permissions = new IPermission[groupMembers.length];
    for (int i = 0; i < groupMembers.length; i++) {
      IAuthorizationPrincipal authPrincipal = authService.newPrincipal(groupMembers[i]);
      permissions[i] = upm.newPermission(authPrincipal);
      permissions[i].setType(GRANT_PERMISSION_TYPE);
      permissions[i].setActivity(SUBSCRIBER_ACTIVITY);
      permissions[i].setTarget(target);
    }

    // If modifying the channel, remove the existing permissions before adding the new ones
    if (!newChannel) {
      IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
      upm.removePermissions(oldPermissions);
    }
    upm.addPermissions(permissions);

    if (log.isInfoEnabled())
        log.info( "Channel " + ID + " has been " + 
                (newChannel ? "published" : "modified") + ".");

    // Record that a channel has been published or modified
    if (newChannel) {
    	EventPublisherLocator.getApplicationEventPublisher().publishEvent(new PublishedChannelDefinitionPortalEvent(channelDef, publisher, channelDef));
    } else {
    	EventPublisherLocator.getApplicationEventPublisher().publishEvent(new ModifiedChannelDefinitionPortalEvent(channelDef, publisher, channelDef));
    }
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
    ChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
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
      }
    }

    // Clone the original CPD document so that it doesn't get modified
    return (Document)cpd.cloneNode(true);
  }
}






