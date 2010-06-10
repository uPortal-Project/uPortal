/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.LinkedList;
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

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.dao.usertype.FunctionalNameType;
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
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@link IPortalUrlProvider} and {@link IUrlGenerator} implementation
 * that uses a consistent human readable URL format.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component
public class PortalUrlProviderImpl implements IPortalUrlProvider, IUrlGenerator {
    
    
    public static final String SEPARATOR = "_";
    public static final String PORTAL_PARAM_PREFIX                  = "uP" + SEPARATOR;
    public static final String LAYOUT_PARAM_PREFIX                  = "uPl" + SEPARATOR;
    public static final String PORTLET_CONTROL_PREFIX               = "Pc" + SEPARATOR;
    public static final String PORTLET_PARAM_PREFIX                 = "Pp" + SEPARATOR;
    public static final String PORTLET_PUBLIC_RENDER_PARAM_PREFIX   = "Ppr" + SEPARATOR;

    public static final String PARAM_WINDOW_STATE   = PORTLET_CONTROL_PREFIX + "s";
    public static final String PARAM_PORTLET_MODE   = PORTLET_CONTROL_PREFIX + "m";
    
    public static final String SLASH = "/";
    public static final String PORTLET_PATH_ELEMENT_SEPERATOR = ".";
    public static final String PORTLET_PATH_PREFIX = "p";
    public static final String FOLDER_PATH_PREFIX = "f";
    public static final String REQUEST_TYPE_SUFFIX = ".uP";
    
    private static final Pattern SLASH_PATTERN = Pattern.compile(SLASH);
    private static final Pattern PORTLET_PATH_ELEMENT_SEPERATOR_PATTERN = Pattern.compile(PORTLET_PATH_ELEMENT_SEPERATOR);
    private static final String PORTAL_REQUEST_INFO_ATTR = PortalUrlProviderImpl.class.getName() + ".PORTAL_REQUEST_INFO"; 
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private String defaultEncoding = "UTF-8";
    private IUserInstanceManager userInstanceManager;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private IChannelRegistryStore channelRegistryStore;
    private IPortalRequestUtils portalRequestUtils;

    /**
     * @param defaultEncoding the defaultEncoding to set
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Autowired
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
    }

    /**
	 * @param portalRequestUtils the portalRequestUtils to set
	 */
    @Autowired
	public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
		this.portalRequestUtils = portalRequestUtils;
	}
    
    private enum ParseStep {
        FOLDER,
        PORTLET,
        STATE,
        TYPE,
        COMPLETE;
    }
	
	/* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalRequestInfo(javax.servlet.http.HttpServletRequest)
     */
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request) throws InvalidPortalRequestException {        
        final IPortalRequestInfo portalRequestInfo = (IPortalRequestInfo)request.getAttribute(PORTAL_REQUEST_INFO_ATTR);
        if (portalRequestInfo != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("short-circuit: found portalRequestInfo within request attributes");
            }
            return portalRequestInfo;
        }
        
        final PortalRequestInfoImpl requestInfoBuilder = new PortalRequestInfoImpl();
        PortletRequestInfoImpl portletRequestInfoBuilder = null;
        
        final String requestPath = this.urlPathHelper.getPathWithinApplication(request);
        final String[] requestPathParts = SLASH_PATTERN.split(requestPath);
        
        ParseStep parseStep = ParseStep.FOLDER;
        
        
        for (int pathPartIndex = 0; pathPartIndex < requestPathParts.length; pathPartIndex++) {
            String pathPart = requestPathParts[pathPartIndex];
            
            switch (parseStep) {
                case FOLDER: {
                    parseStep = ParseStep.PORTLET;
                    
                    if (FOLDER_PATH_PREFIX.equals(pathPart)) {
                        final List<String> folders = new LinkedList<String>();
                        for (;pathPartIndex < requestPathParts.length; pathPartIndex++) {
                            pathPart = requestPathParts[pathPartIndex];
                            if (PORTLET_PATH_PREFIX.equals(pathPart)) {
                                pathPartIndex--;
                                break;
                            }

                            folders.add(pathPart);
                        }
                        
                        //TODO resolve folder names to layout nodes
                        if (folders.size() > 0) {
                            requestInfoBuilder.setTargetedLayoutNodeId(folders.get(folders.size() - 1));
                        }
                        break;
                    }
                }
                case PORTLET: {
                    parseStep = ParseStep.STATE;
                    
                    if (PORTLET_PATH_PREFIX.equals(pathPart)) {
                        if (++pathPartIndex < requestPathParts.length) {
                            pathPart = requestPathParts[pathPartIndex];
                            final String[] portletParts = PORTLET_PATH_ELEMENT_SEPERATOR_PATTERN.split(pathPart);
                            final String fname = portletParts[0];
                            final String subscribeId;
                            if  (portletParts.length > 1) {
                                subscribeId = portletParts[1];
                            }
                            else {
                                final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
                                final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
                                final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
                                subscribeId = userLayoutManager.getSubscribeId(fname);
                            }
                            
                            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
                            final IPerson person = userInstance.getPerson();
                            
                            final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(subscribeId, person.getID());
                            final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
                            
                            portletRequestInfoBuilder = new PortletRequestInfoImpl(portletWindow.getPortletWindowId());
                        }
                        
                        break;
                    }
                }
                case STATE: {
                    parseStep = ParseStep.TYPE;
                    
                    final UrlState urlState = UrlState.valueOfIngoreCase(pathPart, null);
                    if (urlState != null) {
                        requestInfoBuilder.setUrlState(urlState);
                        break;
                    }
                }
                case TYPE: {
                    parseStep = ParseStep.COMPLETE;
                    
                    if (pathPartIndex == requestPathParts.length - 1 && pathPart.endsWith(REQUEST_TYPE_SUFFIX) && pathPart.length() > REQUEST_TYPE_SUFFIX.length()) {
                        final String urlTypePart = pathPart.substring(0, pathPart.length() - REQUEST_TYPE_SUFFIX.length());
                        
                        final UrlType urlType = UrlType.valueOfIngoreCase(urlTypePart, null);
                        if (urlType != null) {
                            requestInfoBuilder.setUrlType(urlType);
                            break;
                        }
                    }
                }
            }
        }
        
        /*
    public static final String PORTAL_PARAM_PREFIX                  = "uP" + SEPARATOR;
    public static final String LAYOUT_PARAM_PREFIX                  = "uPl" + SEPARATOR;
    public static final String PORTLET_CONTROL_PREFIX               = "Pc" + SEPARATOR;
    public static final String PORTLET_PARAM_PREFIX                 = "Pp" + SEPARATOR;
    public static final String PORTLET_PUBLIC_RENDER_PARAM_PREFIX   = "Ppr" + SEPARATOR;

    public static final String PARAM_WINDOW_STATE   = PORTLET_CONTROL_PREFIX + "s";
    public static final String PARAM_PORTLET_MODE   = PORTLET_CONTROL_PREFIX + "m";
         */
        
        
        
        request.setAttribute(PORTAL_REQUEST_INFO_ATTR, requestInfoBuilder);
        
        if(logger.isDebugEnabled()) {
            logger.debug("finished building requestInfo: " + requestInfoBuilder);
        }
        
        return requestInfoBuilder;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getDefaultUrl(javax.servlet.http.HttpServletRequest)
     */
    public IBasePortalUrl getDefaultUrl(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        final IUserLayout userLayout = userLayoutManager.getUserLayout();
        
        final XPathExpression defaultTabIdExpression = this.getUserDefaultTabIdExpression();
        final String defaultTabId = userLayout.findNodeId(defaultTabIdExpression);
          
        // call out to getFolderUrlByNodeId, pass in default nodeId for user
        return getFolderUrlByNodeId(request, defaultTabId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getFolderUrlByNodeId(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public ILayoutPortalUrl getFolderUrlByNodeId(HttpServletRequest request, String folderNodeId) {
        final String resolvedFolderId = this.verifyLayoutNodeId(request, folderNodeId);
        return new LayoutPortalUrlImpl(request, this, resolvedFolderId);

    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletResourceUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    @Override
    public IPortletResourcePortalUrl getPortletResourceUrl(HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new ResourceUrlProviderImpl(portletWindow, request);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByNodeId(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletPortalUrl getPortletUrl(TYPE type, HttpServletRequest request, IPortletWindowId portletWindowId) {
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        return new PortletPortalUrlImpl(type, portletWindow, request, this);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByFName(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IPortletPortalUrl getPortletUrlByFName(TYPE type, HttpServletRequest request, String portletFName) {
        //Get the user's layout manager
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Determine the subscribe ID
        final String channelSubscribeId = userLayoutManager.getSubscribeId(portletFName);
        if (channelSubscribeId == null) {
            throw new IllegalArgumentException("No channel subscribe ID found for fname '" + portletFName + "'.");
        }
        
        return this.getPortletUrlByNodeId(type, request, channelSubscribeId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByNodeId(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IPortletPortalUrl getPortletUrlByNodeId(TYPE type, HttpServletRequest request, String portletNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Find the channel and portlet definitions
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(portletNodeId);
        final String channelPublishId = channelNode.getChannelPublishId();
        final IChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(Integer.parseInt(channelPublishId));
        if(null == channelDefinition) {
            throw new IllegalArgumentException("No channel definition found for publish id: " + channelPublishId);
        }
        
        final IPortletDefinition portletDefinition = channelDefinition.getPortletDefinition();
        if (portletDefinition == null) {
            throw new IllegalArgumentException("No portlet defintion found for channel definition '" + channelPublishId + "'.");
        }
        
        //Determine the appropriate portlet window ID
        final IPerson person = userInstance.getPerson();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinition.getPortletDefinitionId(), portletNodeId, person.getID());
        final IPortletWindow defaultPortletWindow = this.portletWindowRegistry.createDefaultPortletWindow(request, portletEntity.getPortletEntityId());
        final IPortletWindowId portletWindowId = this.portletWindowRegistry.createTransientPortletWindowId(request, defaultPortletWindow.getPortletWindowId());
        
        return this.getPortletUrl(type, request, portletWindowId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlGenerator#generateLayoutUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.url.ILayoutPortalUrl, java.lang.String)
     */
    public String generateLayoutUrl(HttpServletRequest request, ILayoutPortalUrl layoutPortalUrl, String targetNodeId) {
        final String encoding = this.getEncoding(request);
        final UrlBuilder url = new UrlBuilder(encoding);
        
        final String contextPath = this.getCleanedContextPath(request);
        url.setPath(contextPath);

        final String folderId = this.verifyLayoutNodeId(request, targetNodeId);
        url.addPath(folderId);

        final Boolean renderInNormal = layoutPortalUrl.isRenderInNormal();
        if (renderInNormal != null && renderInNormal) {
            url.addPath(UrlState.NORMAL.toLowercaseString());
        }
        else {
            final IPortalRequestInfo requestInfo = getPortalRequestInfo(request);
            final UrlState urlState = requestInfo.getUrlState();
            url.addPath(urlState.toLowercaseString());
        }
        
        final boolean action = layoutPortalUrl.isAction();
        if (action) {
            url.addPath(UrlType.ACTION.toLowercaseString() + REQUEST_TYPE_SUFFIX);
        }
        else {
            url.addPath(UrlType.RENDER.toLowercaseString() + REQUEST_TYPE_SUFFIX);
        }

        //Add all portal parameters
        final Map<String, List<String>> portalParameters = layoutPortalUrl.getPortalParameters();
        url.addParameters(PORTAL_PARAM_PREFIX, portalParameters);
        
        //Add all layout parameters
        final Map<String, List<String>> layoutParameters = layoutPortalUrl.getLayoutParameters();
        url.addParameters(PORTAL_PARAM_PREFIX, layoutParameters);
        
        return url.toString();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlGenerator#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.url.IPortalPortletUrl, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletPortalUrl portalPortletUrl, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "HttpServletRequest was null");
        Validate.notNull(portalPortletUrl, "IPortalPortletUrl was null");
        Validate.notNull(portletWindowId, "IPortletWindowId was null");
       
        //Convert the callback request to the portal request
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final String encoding = this.getEncoding(request);
        final UrlBuilder url = new UrlBuilder(encoding);
        
        final String contextPath = this.getCleanedContextPath(request);
        url.setPath(contextPath);
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(portletWindow.getPortletEntityId());
        
        //Add folder information if available: /tabId
        final String channelSubscribeId = portletEntity.getChannelSubscribeId();
        // if not a transient node, we need to lookup user layout information
        if (!channelSubscribeId.startsWith(TransientUserLayoutManagerWrapper.SUBSCRIBE_PREFIX)) {
            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
            final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
            final IUserLayout userLayout = userLayoutManager.getUserLayout();
            
            final XPathExpression findChannelTabExpression = this.getFindChannelTabIdExpression(channelSubscribeId);
            final String tabId = userLayout.findNodeId(findChannelTabExpression);
            
            //Add folder ID of parent tab if it exists
            if (tabId != null) {
                final String folderId = this.verifyLayoutNodeId(request, tabId);
                url.addPath(folderId);
            }
        }
        
        //Add state information
        final WindowState requestedWindowState = portalPortletUrl.getWindowState();
        final WindowState currentWindowState = portletWindow.getWindowState();
        final WindowState urlWindowState = requestedWindowState != null ? requestedWindowState : currentWindowState;
      
        final String windowStateString;
        if (WindowState.MAXIMIZED.equals(urlWindowState)) {
            windowStateString = UrlState.MAX.toLowercaseString();
        }
        else if (IPortletRenderer.DETACHED.equals(urlWindowState)) {
            windowStateString = UrlState.DETACHED.toLowercaseString();
        }
        else if (IPortletRenderer.EXCLUSIVE.equals(urlWindowState)) {
            windowStateString = UrlState.EXCLUSIVE.toLowercaseString();
        }
        else {
            if(!WindowState.NORMAL.equals(urlWindowState)) {
                this.logger.warn("Unknown WindowState '" + urlWindowState + "' specified for portlet window " + portletWindow + ", defaulting to NORMAL");
            }
            
            windowStateString = UrlState.NORMAL.toLowercaseString();
        }
        url.addPath(windowStateString);
        
        //Add channel information: /fname.chanid
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(portletEntity.getPortletDefinitionId());
        final IChannelDefinition channelDefinition = portletDefinition.getChannelDefinition();
        final String fname = channelDefinition.getFName();
        final String validFname = FunctionalNameType.INVALID_CHARS_PATTERN.matcher(fname).replaceAll("_");
        url.addPath(validFname + PORTLET_PATH_ELEMENT_SEPERATOR + channelSubscribeId);
        
        //File part 
        switch (portalPortletUrl.getType()) {
            case ACTION: {
                url.addPath(UrlType.ACTION.toLowercaseString() + REQUEST_TYPE_SUFFIX);
            }
            break;
            
            case RENDER: {
                url.addPath(UrlType.RENDER.toLowercaseString() + REQUEST_TYPE_SUFFIX);
            }
            break;
            
            default: {
                throw new IllegalArgumentException("Unsupported URL type: " + portalPortletUrl.getType()); 
            }
        }
        
        //Query String  
        //Target portlet window info
        url.addParameter(PARAM_REQUEST_TARGET, portletWindowId.getStringId());
          
        //Portlet mode info
        final PortletMode portletMode = portalPortletUrl.getPortletMode();
        if (portletMode != null && !portletMode.equals(portletWindow.getPortletMode())) {
            url.addParameter(PARAM_PORTLET_MODE, portletMode.toString());
        } 
        
        //Add window state param for switching between normal and maximized
        if (requestedWindowState != null && !requestedWindowState.equals(currentWindowState) 
                && (WindowState.MINIMIZED.equals(urlWindowState) || WindowState.NORMAL.equals(urlWindowState))) {
            url.addParameter(PARAM_WINDOW_STATE, requestedWindowState.toString());
        }
        
        //Add all portal parameters
        final Map<String, List<String>> portalParameters = portalPortletUrl.getPortalParameters();
        url.addParameters(PORTAL_PARAM_PREFIX, portalParameters);

        //Add all portlet parameters
        final Map<String, String[]> portletParameters = portalPortletUrl.getPortletParameters();
        url.addParametersArray(PORTLET_PARAM_PREFIX, portletParameters);
        
        if(logger.isDebugEnabled()) {
            logger.debug("finished portlet url: " + url.toString());
        }
        return url.toString();
    }
    
	protected String verifyLayoutNodeId(HttpServletRequest request, String folderNodeId) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final IUserLayoutNodeDescription node = userLayoutManager.getNode(folderNodeId);
        
        if (node == null) {
            throw new IllegalArgumentException("No layout node exists for id: " + folderNodeId);
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

    /**
     * @param request The current requet
     * @return The base URL, will be absolute (start with / or protocol://) and will end with a /
     */
    protected String getCleanedContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        
        //Make sure the URL starts with a /
        if (contextPath.charAt(0) == '/') {
            contextPath = contextPath.substring(1);
        }
        
        //Make sure the URL ends with a /
        if (contextPath.charAt(contextPath.length() - 1) == '/') {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        return contextPath;
    }
    
    /**
     * Returns an {@link XPathExpression} that represents the specified channel NodeId.
     * 
     * @param channelNodeId
     * @return
     */
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
    
    /**
     * Returns an {@link XPathExpression} that represents the specified tab NodeId.
     * @param tabNodeId
     * @return
     */
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

    /**
     * Returns an {@link XPathExpression} that represent's the user's default tab.
     * 
     * @return
     */
    protected XPathExpression getUserDefaultTabIdExpression() {
    	final String expression = "/layout/folder/folder[type='regular' and hidden='false'][0]/@ID";
    	
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
