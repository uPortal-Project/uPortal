/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    public static final String PORTAL_PARAM_PREFIX = "uP" + SEPARATOR;
    public static final String PORTLET_CONTROL_PREFIX = "pltc" + SEPARATOR;
    public static final String PORTLET_PARAM_PREFIX = "pltp" + SEPARATOR;

    public static final String PARAM_REQUEST_TARGET = PORTLET_CONTROL_PREFIX + "target";
    public static final String PARAM_WINDOW_STATE = PORTLET_CONTROL_PREFIX + "state";
    public static final String PARAM_PORTLET_MODE = PORTLET_CONTROL_PREFIX + "mode";
    
    public static final String SLASH = "/";
    public static final String ACTION_SUFFIX = "action.uP";
    public static final String RENDER_SUFFIX = "render.uP";
    public static final String WORKER_SUFFIX = "worker.uP";
    
    public static final String NO_STATE_REGEX = ".*(normal|max|detached|exclusive|legacy).*";
    private static final Pattern NO_STATE_PATTERN = Pattern.compile(NO_STATE_REGEX);
    public static final String PORTAL_REQUEST_REGEX = "^(?:([^/]*)/)*(normal|max|detached|exclusive|legacy)/(?:([^/]*)/)?(render\\.uP|action\\.uP|worker\\.uP|)$";
    private static final Pattern PORTAL_REQUEST_PATTERN = Pattern.compile(PORTAL_REQUEST_REGEX);
    
    private static final String PORTAL_REQUEST_INFO_ATTR = PortalUrlProviderImpl.class.getName() + ".PORTAL_REQUEST_INFO"; 
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
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
	
	/* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalRequestInfo(javax.servlet.http.HttpServletRequest)
     */
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request) throws InvalidPortalRequestException {        
        IPortalRequestInfo portalRequestInfo = (IPortalRequestInfo)request.getAttribute(PORTAL_REQUEST_INFO_ATTR);
        if (portalRequestInfo != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("short-circuit: found portalRequestInfo within request attributes");
            }
            return portalRequestInfo;
        }
        
        //TODO don't create a new object each time
        final String requestPath = new UrlPathHelper().getPathWithinApplication(request);
        // first pass looks for absence of state
        Matcher firstPass = NO_STATE_PATTERN.matcher(requestPath);
        if(!firstPass.matches()) {
        	// requestPath doesn't contain state
        	// assume NORMAL state, not an action
        	// and that requestPath solely contains folder information (no targeted channel)
        	PortalRequestInfoImpl requestInfo = new PortalRequestInfoImpl();
        	requestInfo.setUrlState(UrlState.NORMAL);
        	requestInfo.setAction(false);
        	
        	String [] folderElements = requestPath.split("\\/");
            if(folderElements.length > 0) {
                requestInfo.setTargetedLayoutNodeId(folderElements[folderElements.length-1]);
            }
            
            if(logger.isDebugEnabled()) {
                logger.debug("finished building requestInfo: " + requestInfo);
            }
            return requestInfo;
        }
        // if we get here, the requestPath contains one of the states
        Matcher m = PORTAL_REQUEST_PATTERN.matcher(requestPath);
        if(m.matches()) {
            PortalRequestInfoImpl requestInfo = new PortalRequestInfoImpl();
            String folderInformation = m.group(1);
            String stateInformation = m.group(2);
            String channelInformation = m.group(3);
            String renderInformation = m.group(4);
            
            // upper case group(2) to get UrlState enum
            requestInfo.setUrlState(UrlState.valueOfIngoreCase(stateInformation));
            // true if group(4) matches ACTION, false otherwise
            requestInfo.setAction(ACTION_SUFFIX.equals(renderInformation));
            
            // group(3) can be null - if so ignore setting targetedPortletWindowId and targetedChannelSubscribeId
            if(null != channelInformation) {
            	String channelName = channelInformation;
            	String channelSubscribeId = null;
            	// portletWindowId cannot contain a "."
                // if group(3) contains a ".", set subscribe Id to value after "."
            	if(channelInformation.contains(".")) {
            		String [] channelInformationElements = channelInformation.split("\\.");
            		channelName = channelInformationElements[0];
            		channelSubscribeId = channelInformationElements[1];
            	}
            	else {
            	    //lookup 
            	    final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            	    final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
            	    final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
            	    channelSubscribeId = userLayoutManager.getSubscribeId(channelName);
            	}
            	
            	IChannelDefinition channelDefinition = this.channelRegistryStore.getChannelDefinition(channelName);
            	boolean isPortlet = channelDefinition.isPortlet();
                
            	requestInfo.setTargetedChannelSubscribeId(channelSubscribeId);
            	
            	if(isPortlet) {
        	        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            	    final IPerson person = userInstance.getPerson();
            	    
            	    final IPortletEntity portletEntity = this.portletEntityRegistry.getPortletEntity(channelSubscribeId, person.getID());
            	    final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindow(request, portletEntity.getPortletEntityId());
            	    requestInfo.setTargetedPortletWindowId(portletWindow.getPortletWindowId());
            	}
            }
            
            // group(1) may contain slashes to separate sub folders
            // set targetedLayoutNodeId to LAST folder
            if(!StringUtils.isBlank(folderInformation)) {
                String [] folderElements = folderInformation.split("\\/");
                if(folderElements.length > 0) {
                    requestInfo.setTargetedLayoutNodeId(folderElements[folderElements.length-1]);
                }
            }
            
            request.setAttribute(PORTAL_REQUEST_INFO_ATTR, requestInfo);
            
            if(logger.isDebugEnabled()) {
                logger.debug("finished building requestInfo: " + requestInfo);
            }
            return requestInfo;
        }
         
        throw new InvalidPortalRequestException("could not extract portal request from " + requestPath);
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
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByNodeId(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletPortalUrl getPortletUrl(HttpServletRequest request, IPortletWindowId portletWindowId) {
        return new PortletPortalUrlImpl(request, this, portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortletUrlByFName(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public IPortletPortalUrl getPortletUrlByFName(HttpServletRequest request, String portletFName) {
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
    public IPortletPortalUrl getPortletUrlByNodeId(HttpServletRequest request, String portletNodeId) {
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
        
        //Support using portlet URLs for channels, if not a portlet create a url using the subscribe ID
        if (!channelDefinition.isPortlet()) {
            return new PortletPortalUrlImpl(request, this, portletNodeId);
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
        
        return this.getPortletUrl(request, portletWindowId);
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
            url.addPath(ACTION_SUFFIX);
        }
        else {
            url.addPath(RENDER_SUFFIX);
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
        url.addPath(validFname + "." + channelSubscribeId);
        
        //File part 
        if (portalPortletUrl.isAction()) {
            url.addPath(ACTION_SUFFIX);
        }
        else {
            url.addPath(RENDER_SUFFIX);
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
        final Map<String, List<String>> portletParameters = portalPortletUrl.getPortletParameters();
        url.addParameters(PORTLET_PARAM_PREFIX, portletParameters);
        
        if(logger.isDebugEnabled()) {
            logger.debug("finished portlet url: " + url.toString());
        }
        return url.toString();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IUrlGenerator#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.url.IPortalPortletUrl, java.lang.String)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletPortalUrl portalPortletUrl, String channelSubscribeId) {
        Validate.notNull(request, "HttpServletRequest was null");
        Validate.notNull(portalPortletUrl, "IPortalPortletUrl was null");
        Validate.notNull(channelSubscribeId, "IPortletWindowId was null");
       
        //Convert the callback request to the portal request
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final String encoding = this.getEncoding(request);
        final UrlBuilder url = new UrlBuilder(encoding);
        
        final String contextPath = this.getCleanedContextPath(request);
        url.setPath(contextPath);
        
        //Add folder information if available: /tabId
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

        final WindowState requestedWindowState = portalPortletUrl.getWindowState();
      
        if (WindowState.MAXIMIZED.equals(requestedWindowState)) {
            url.addPath(UrlState.MAX.toLowercaseString());
        }
        else if (IPortletRenderer.DETACHED.equals(requestedWindowState)) {
            url.addPath(UrlState.DETACHED.toLowercaseString());
        }
        else if (IPortletRenderer.EXCLUSIVE.equals(requestedWindowState)) {
            url.addPath(UrlState.EXCLUSIVE.toLowercaseString());
        }
        else if (WindowState.MINIMIZED.equals(requestedWindowState)) {
            //Support for minimizing channels
            url.addParameter(PORTAL_PARAM_PREFIX + "tcattr", "minimized");
            url.addParameter("minimized_channelId", channelSubscribeId);
            url.addParameter("minimized_" + channelSubscribeId + "_value", "true");
        }
        else {
            url.addPath(UrlState.NORMAL.toLowercaseString());
        }
        
        //TODO PRINT WindowState support, handle the same way as detached
        
        /**
         * state - max, print
         *  uP_print_target={@ID}
         * mode - help, about, edit
         *  uP_help|edit|about_target={@ID}
         */
        
        final PortletMode requestedPortletMode = portalPortletUrl.getPortletMode();
//        if (PortletMode.EDIT.equals(requestedPortletMode)) {
//            
//        }
        if (requestedPortletMode != null) {
            this.logger.warn("Request PortletMode '" + requestedPortletMode + "' is not handled for IChannel integration");
        }
        
        //TODO portlet mode to channel event mapping support
        
        //Add channel information: /fname.chanid
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Find the channel definitions
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(channelSubscribeId);
        if(channelNode == null) {
            // not in user layout
            throw new IllegalArgumentException("channel node id not in user's layout: " + channelSubscribeId);
        }
        
        final String fname = channelNode.getFunctionalName();
        final String validFname = FunctionalNameType.INVALID_CHARS_PATTERN.matcher(fname).replaceAll("_");
        url.addPath(validFname + "." + channelSubscribeId);
        
        //File part 
        if (portalPortletUrl.isAction()) {
            url.addPath(ACTION_SUFFIX);
        }
        else {
            url.addPath(RENDER_SUFFIX);
        }
        
        //Add all portal parameters
        final Map<String, List<String>> portalParameters = portalPortletUrl.getPortalParameters();
        url.addParameters(PORTAL_PARAM_PREFIX, portalParameters);
        
        //Add all portlet parameters
        final Map<String, List<String>> portletParameters = portalPortletUrl.getPortletParameters();
        url.addParameters(PORTLET_PARAM_PREFIX, portletParameters);
        
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
