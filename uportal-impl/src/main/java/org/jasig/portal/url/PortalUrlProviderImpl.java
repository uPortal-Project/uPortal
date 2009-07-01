/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;

/**
 * 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalUrlProviderImpl implements IPortalUrlProvider, IUrlGenerator {
    public static final Pattern VALID_FNAME_CHARS = Pattern.compile("[A-Za-z0-9_\\-]");
    
    public static final String SEPERATOR = "_";
    public static final String PORTAL_PARAM_PREFIX = "uP" + SEPERATOR;
    public static final String PORTLET_CONTROL_PREFIX = "pltc" + SEPERATOR;
    public static final String PORTLET_PARAM_PREFIX = "pltp" + SEPERATOR;

    public static final String PARAM_REQUEST_TARGET = PORTLET_CONTROL_PREFIX + "target";
    public static final String PARAM_WINDOW_STATE = PORTLET_CONTROL_PREFIX + "state";
    public static final String PARAM_PORTLET_MODE = PORTLET_CONTROL_PREFIX + "mode";
    
    private static final String PORTAL_REQUEST_INFO_ATTR = PortalUrlProviderImpl.class.getName() + ".PORTAL_REQUEST_INFO"; 
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String defaultEncoding = "UTF-8";
    private IUserInstanceManager userInstanceManager;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private ITransientPortletWindowRegistry portletWindowRegistry;
    

    /**
     * @param defaultEncoding the defaultEncoding to set
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    public void setPortletWindowRegistry(ITransientPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    

    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalRequestInfo(javax.servlet.http.HttpServletRequest)
     */
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request) {
        IPortalRequestInfo portalRequestInfo = (IPortalRequestInfo)request.getAttribute(PORTAL_REQUEST_INFO_ATTR);
        if (portalRequestInfo != null) {
            return portalRequestInfo;
        }
        
        final String contextPath = request.getContextPath();
        final String requestURI = request.getRequestURI();
        
        //Get the request path after the servlet context
        final String requestPath = requestURI.substring(contextPath.length());
        
        
        // Request regex: ^/(?:([^/]*)/)*(NORMAL|MAX|DETACHED|EXCLUSIVE|LEGACY)/(?:([^/]*)/)?(render|action)\.uP$
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getDefaultUrl(javax.servlet.http.HttpServletRequest)
     */
    public IBasePortalUrl getDefaultUrl(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final String rootFolderId = userLayoutManager.getRootFolderId();
        
        //TODO determine default active tab for user
        
        return new PortalLayoutUrlImpl(request, this, rootFolderId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getFolderUrlByNodeId(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IBasePortalUrl getFolderUrlByNodeId(HttpServletRequest request, String folderNodeId) {
        final String resolvedFolderId = this.verifyFolderId(request, folderNodeId);
        return new PortalLayoutUrlImpl(request, this, resolvedFolderId);

    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByNodeId(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortalPortletUrl getPortletUrl(HttpServletRequest request, IPortletWindowId portletWindowId) {
        return new PortalPortletUrlImpl(request, this, portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByFName(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IPortalPortletUrl getPortletUrlByFName(HttpServletRequest request, String portletFName) {
        //Get the user's layout manager
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Determine the subscribe ID
        final String channelSubscribeId = userLayoutManager.getSubscribeId(portletFName);
        if (channelSubscribeId == null) {
            throw new IllegalArgumentException("No channel subscribe ID found for fname '" + portletFName + "'.");
        }
        
        return this.getPortletUrlByNodeId(request, channelSubscribeId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByNodeId(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IPortalPortletUrl getPortletUrlByNodeId(HttpServletRequest request, String portletNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Find the channel and portlet definitions
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(portletNodeId);
        final String channelPublishId = channelNode.getChannelPublishId();
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(Integer.parseInt(channelPublishId));
        if (portletDefinition == null) {
            throw new IllegalArgumentException("No portlet defintion found for channel definition '" + channelPublishId + "'.");
        }
        
        //Determine the appropriate portlet window ID
        final IPerson person = userInstance.getPerson();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinition.getPortletDefinitionId(), portletNodeId, person.getID());
        final IPortletWindow defaultPortletWindow = this.portletWindowRegistry.createDefaultPortletWindow(request, portletEntity.getPortletEntityId());
        final IPortletWindowId portletWindowId = this.portletWindowRegistry.createTransientPortletWindowId(request, defaultPortletWindow.getPortletWindowId());
        
        return this.getPortletUrl(request, portletWindowId);
    }
    

    protected String verifyFolderId(HttpServletRequest request, String folderNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(folderNodeId);
        
        if (node == null) {
            throw new IllegalArgumentException("No layout node exists for id: " + folderNodeId);
        }
        if (node.getType() != IUserLayoutNodeDescription.FOLDER) {
            throw new IllegalArgumentException("Layout node is not a folder for id: " + folderNodeId);
        }
        
        final String resolvedFolderId = node.getId();
        return resolvedFolderId;
    }
    
    /**
     * Tries to determine the encoded from the request, if not available falls back to configured default.
     * 
     * @param request The current request.
     * @return The encoding to use.
     */
    protected String getEncoding(HttpServletRequest request) {
        final String encoding = request.getCharacterEncoding();
        if (encoding != null) {
            return encoding;
        }
        
        return this.defaultEncoding;
    }
    
    
    protected void encodeAndAppend(StringBuilder url, String encoding, String name, String... values) {
        this.encodeAndAppend(url, encoding, name, Arrays.asList(values));
    }
    
    /**
     * Encodes parameter name and value(s) on to the url using the specified encoding. The option to pass more than one
     * value is provided to avoid encoding the same name multiple times.  
     * 
     * @param url The URL StringBuilder to append the parameters to
     * @param encoding The encoding to use.
     * @param name The name of the parameter
     * @param values The values for the parameter, a & will be appended between each name/value pair added when multiple values are passed.
     */
    protected void encodeAndAppend(StringBuilder url, String encoding, String name, List<String> values) {
        try {
            name = URLEncoder.encode(name, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode portlet URL parameter name '" + name + "' for encoding '" + encoding + "'");
        }
        
        if (values.size() == 0) {
            url.append(name).append("=");
        }
        else {
            for (final Iterator<String> valueItr = values.iterator(); valueItr.hasNext(); ) {
                String value = valueItr.next();
                if (value == null) {
                    value = "";
                }
                
                try {
                    value = URLEncoder.encode(value, encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encode portlet URL parameter value '" + value + "' for encoding '" + encoding + "'");
                }
                
                url.append(name).append("=").append(value);
                
                if (valueItr.hasNext()) {
                    url.append("&");
                }
            }
        }
    }

    /**
     * @param request The current requet
     * @return The base URL, will be absolute (start with / or protocol://) and will end with a /
     */
    protected StringBuilder getUrlBase(HttpServletRequest request) {
        final StringBuilder url = new StringBuilder(request.getContextPath());
        
        //Make sure the URL starts with a /
        if (url.charAt(0) != '/') {
            url.insert(0, '/');
        }
        
        //Make sure the URL ends with a /
        if (url.charAt(url.length() - 1) != '/') {
            url.append('/');
        }

        return url;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlGenerator#generatePortalUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.url.IBasePortalUrl, java.lang.String)
     */
    public String generatePortalUrl(HttpServletRequest request, IBasePortalUrl basePortalUrl, String targetFolderId) {
        final StringBuilder url = this.getUrlBase(request);

        //Add targeted folder id
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        
        final XPathExpression findChannelTabExpression = this.getFindChannelTabIdExpression(targetFolderId);
        final String tabId = userLayout.findNodeId(findChannelTabExpression);
        
        //Add folder ID of parent tab if it exists
        if (tabId != null) {
            final String folderId = this.verifyFolderId(request, tabId);
            url.append("/").append(folderId);
        }
        
        
        final String folderId = this.verifyFolderId(request, targetFolderId);
        url.append("/").append(folderId);

        //TODO need to allow portal URLs to set the UrlState
        //TODO need to determine current URL state if specific UrlState is not already set?
        //Add state information
        url.append("/").append(UrlState.NORMAL);
        
        //TODO are portal URLs always render URLs?
        url.append("/render.uP");

        //Add all portal parameters
        final Map<String, List<String>> portalParameters = basePortalUrl.getPortalParameters();
        if (portalParameters.size() > 0) {
            final String encoding = this.getEncoding(request);
            
            String seperator = "?";
            for (final Map.Entry<String, List<String>> paramEntry : portalParameters.entrySet()) {
                final String name = paramEntry.getKey();
                final List<String> values = paramEntry.getValue();
                this.encodeAndAppend(url.append(seperator), encoding, PORTAL_PARAM_PREFIX + name, values);
                seperator = "&";
            }
        }
        
        return url.toString();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlGenerator#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.url.IPortalPortletUrl, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortalPortletUrl portalPortletUrl, IPortletWindowId portletWindowId) {
        final StringBuilder url = this.getUrlBase(request);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(portletWindow.getPortletEntityId());
        
        //Add folder information if available: /tabId
        final String channelSubscribeId = portletEntity.getChannelSubscribeId();
        if (!channelSubscribeId.startsWith(TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX)) {
            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
            final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
            final IUserLayout userLayout = userLayoutManager.getUserLayout();
            
            final XPathExpression findChannelTabExpression = this.getFindChannelTabIdExpression(channelSubscribeId);
            final String tabId = userLayout.findNodeId(findChannelTabExpression);
            
            //Add folder ID of parent tab if it exists
            if (tabId != null) {
                final String folderId = this.verifyFolderId(request, tabId);
                url.append("/").append(folderId);
            }
        }
        
        
        //Add state information
        final WindowState requestedWindowState = portalPortletUrl.getWindowState();
        final WindowState currentWindowState = portletWindow.getWindowState();
        final WindowState urlWindowState = requestedWindowState != null ? requestedWindowState : currentWindowState;
        url.append("/");
        if (null == urlWindowState || WindowState.NORMAL.equals(urlWindowState)) {
            url.append(UrlState.NORMAL);
        }
        else if (WindowState.MAXIMIZED.equals(urlWindowState)) {
            url.append(UrlState.MAX);
        }
        else if (IPortletAdaptor.DETACHED.equals(urlWindowState)) {
            url.append(UrlState.DETACHED);
        }
        else if (IPortletAdaptor.EXCLUSIVE.equals(urlWindowState)) {
            url.append(UrlState.EXCLUSIVE);
        }
        else {
            this.logger.warn("Unknown WindowState '" + urlWindowState + "' specified for portlet window " + portletWindow + ", defaulting to NORMAL");
            url.append(UrlState.NORMAL);
        }
        
        
        //Add channel information: /fname.chanid
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(portletEntity.getPortletDefinitionId());
        final IChannelDefinition channelDefinition = portletDefinition.getChannelDefinition();
        final String fname = channelDefinition.getFName();
        final String validFname = VALID_FNAME_CHARS.matcher(fname).replaceAll("_");
        url.append("/").append(validFname).append(".").append(portletEntity.getChannelSubscribeId());

        
        //File part 
        if (portalPortletUrl.isAction()) {
            url.append("/action.uP");
        }
        else {
            url.append("/render.uP");
        }
        final String encoding = this.getEncoding(request);
        
        
        //Query String
        
        
        //Target portlet window info
        this.encodeAndAppend(url.append("?"), encoding, PARAM_REQUEST_TARGET, portletWindowId.getStringId());
        
        
        //Portlet mode info
        final PortletMode portletMode = portalPortletUrl.getPortletMode();
        if (portletMode != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE, portletMode.toString());
        }
        
        
        //Add window state param for switching between normal and maximized
        if (requestedWindowState != null && !requestedWindowState.equals(currentWindowState) 
                && (WindowState.MINIMIZED.equals(urlWindowState) || WindowState.NORMAL.equals(urlWindowState))) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE, requestedWindowState.toString());
        }

        
        //Add all portal parameters
        final Map<String, List<String>> portalParameters = portalPortletUrl.getPortalParameters();
        for (final Map.Entry<String, List<String>> paramEntry : portalParameters.entrySet()) {
            final String name = paramEntry.getKey();
            final List<String> values = paramEntry.getValue();
            this.encodeAndAppend(url.append("&"), encoding, PORTAL_PARAM_PREFIX + name, values);
        }

        
        //Add all portlet parameters
        final Map<String, List<String>> portletParameters = portalPortletUrl.getPortletParameters();
        for (final Map.Entry<String, List<String>> paramEntry : portletParameters.entrySet()) {
            final String name = paramEntry.getKey();
            final List<String> values = paramEntry.getValue();
            this.encodeAndAppend(url.append("&"), encoding, PORTLET_PARAM_PREFIX + name, values);
        }
        
        
        return url.toString();
    }
    
    
    protected XPathExpression getFindChannelTabIdExpression(String channelNodeId) {
        final String expression = "/layout/folder/folder[descendant::channel[@ID='" + channelNodeId + "']]/@ID";
        
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        try {
            //TODO compile the expression once and cache it!
            return xPath.compile(expression);
        }
        catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Invalid XPath expression: " + expression, e);
        }
    }
    
    
    protected XPathExpression getFindTabIdExpression(String tabNodeId) {
        final String expression = "/layout/folder/folder[@ID='" + tabNodeId + "']/@ID";
        
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xPath = xPathFactory.newXPath();
        try {
            //TODO compile the expression once and cache it!
            return xPath.compile(expression);
        }
        catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Invalid XPath expression: " + expression, e);
        }
    }
}
