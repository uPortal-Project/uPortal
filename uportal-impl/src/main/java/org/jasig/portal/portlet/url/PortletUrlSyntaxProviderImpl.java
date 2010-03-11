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

package org.jasig.portal.portlet.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.portlet.delegation.IPortletDelegationManager;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains the logic and string constants for generating and parsing portlet URL parameters.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImpl implements IPortletUrlSyntaxProvider {
    private static final String SEPERATOR = "_";
    private static final String PORTLET_CONTROL_PREFIX = "pltc" + SEPERATOR;
    private static final String PORTLET_PARAM_PREFIX = "pltp" + SEPERATOR;

    private static final String PARAM_REQUEST_TARGET = PORTLET_CONTROL_PREFIX + "target";
    private static final String PARAM_REQUEST_TYPE_PREFIX = PORTLET_CONTROL_PREFIX + "type" + SEPERATOR;
    private static final String PARAM_WINDOW_STATE_PREFIX = PORTLET_CONTROL_PREFIX + "state" + SEPERATOR;
    private static final String PARAM_PORTLET_MODE_PREFIX = PORTLET_CONTROL_PREFIX + "mode" + SEPERATOR;
    private static final String PARAM_DELEGATE_PREFIX = PORTLET_CONTROL_PREFIX + "delegate" + SEPERATOR;
    
    private static final Pattern URL_PARAM_NAME = Pattern.compile("&([^&?=\n]*)");
   
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String defaultEncoding = "UTF-8";
    private int bufferLength = 512;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IUserInstanceManager userInstanceManager;
    private IPortletDelegationManager portletDelegationManager;
    private boolean useAnchors = true;
    private Set<WindowState> transientWindowStates = new HashSet<WindowState>(Arrays.asList(IPortletRenderer.EXCLUSIVE, IPortletRenderer.DETACHED));
    private Set<WindowState> anchoringWindowStates = new HashSet<WindowState>(Arrays.asList(WindowState.MINIMIZED, WindowState.NORMAL));
    
    
    /**
     * @return the useAnchors
     */
    public boolean isUseAnchors() {
        return this.useAnchors;
    }
    /**
     * If anchors should be added to generated URLs
     */
    public void setUseAnchors(boolean useAnchors) {
        this.useAnchors = useAnchors;
    }
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired(required=true)
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /**
     * @return the defaultEncoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }
    /**
     * @param defaultEncoding the defaultEncoding to set
     */
    public void setDefaultEncoding(String defaultEncoding) {
        Validate.notNull(defaultEncoding, "defaultEncoding can not be null");
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * @return the bufferLength
     */
    public int getBufferLength() {
        return bufferLength;
    }
    /**
     * @param bufferLength the bufferLength to set
     */
    public void setBufferLength(int bufferLength) {
        if (bufferLength < 1) {
            throw new IllegalArgumentException("bufferLength must be at least 1");
        }

        this.bufferLength = bufferLength;
    }

    /**
     * @return the portletWindowRegistry
     */
    public IPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired(required=true)
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired(required=true)
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public IPortletEntityRegistry getPortletEntityRegistry() {
        return portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Autowired(required=true)
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    public IUserInstanceManager getUserInstanceManager() {
        return userInstanceManager;
    }
    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    @Autowired(required=true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    public Set<WindowState> getTransientWindowStates() {
        return this.transientWindowStates;
    }
    /**
     * {@link WindowState}s that have transient {@link IPortletWindow}s. These states must be the ONLY
     * content rendering links on the page. Defaults to EXCLUSIVE and DETACHED.
     */
    public void setTransientWindowStates(Set<WindowState> transientWindowStates) {
        if (transientWindowStates == null) {
            this.transientWindowStates = Collections.emptySet();
        }
        else {
            this.transientWindowStates = new LinkedHashSet<WindowState>(transientWindowStates);
        }
    }

    public Set<WindowState> getAnchoringWindowStates() {
        return this.anchoringWindowStates;
    }
    /**
     * {@link WindowState}s where anchors should be added to the ends of the generated URLS, only if
     * {@link #setUseAnchors(boolean)} is true. Defaults to MINIMIZED and NORMAL
     */
    public void setAnchoringWindowStates(Set<WindowState> anchoringWindowStates) {
        if (anchoringWindowStates == null) {
            this.anchoringWindowStates = Collections.emptySet();
        }
        else {
            this.anchoringWindowStates = new LinkedHashSet<WindowState>(anchoringWindowStates);
        }
    }
    
    public IPortletDelegationManager getPortletDelegationManager() {
        return this.portletDelegationManager;
    }
    @Autowired(required=true)
    public void setPortletDelegationManager(IPortletDelegationManager portletDelegationManager) {
        this.portletDelegationManager = portletDelegationManager;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#parsePortletParameters(javax.servlet.http.HttpServletRequest)
     */
    public PortletUrl parsePortletUrl(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        final IPortletWindowId targetedPortletWindowId = resolveTargetWindowId(request);
        if (targetedPortletWindowId == null) {
            return null;
        }
        
        final PortletUrl portletUrl = new PortletUrl(targetedPortletWindowId);
        
        this.parsePortletParameters(request, portletUrl);
    
        return portletUrl;
    }
    
    @SuppressWarnings("unchecked")
    protected void parsePortletParameters(HttpServletRequest request, PortletUrl portletUrl) {
        final IPortletWindowId portletWindowId = portletUrl.getTargetWindowId();
        final String portletWindowIdStr = portletWindowId.toString();
        
        final String requestTypeStr = request.getParameter(PARAM_REQUEST_TYPE_PREFIX + portletWindowIdStr);
        if (requestTypeStr != null) {
            final RequestType requestType = RequestType.valueOf(requestTypeStr);
            portletUrl.setRequestType(requestType);
        }
        else {
            //Default to RENDER request if no request type was specified
            portletUrl.setRequestType(RequestType.RENDER);
        }
        
        final String windowStateStr = request.getParameter(PARAM_WINDOW_STATE_PREFIX + portletWindowIdStr);
        if (windowStateStr != null) {
            final WindowState windowState = new WindowState(windowStateStr);
            portletUrl.setWindowState(windowState);
        }
        
        final String portletModeStr = request.getParameter(PARAM_PORTLET_MODE_PREFIX + portletWindowIdStr);
        if (portletModeStr != null) {
            final PortletMode portletMode = new PortletMode(portletModeStr);
            portletUrl.setPortletMode(portletMode);
        }
        
        final Map<String, String[]> requestParameters = request.getParameterMap();
        final Set<String> urlParameterNames = this.getUrlParameterNames(request);
        
        final Map<String, List<String>> portletParameters = new LinkedHashMap<String, List<String>>(requestParameters.size());
        final String fqParameterName = PORTLET_PARAM_PREFIX + portletWindowIdStr + SEPERATOR;
        for (final Map.Entry<String, String[]> parameterEntry : requestParameters.entrySet()) {
            final String parameterName = parameterEntry.getKey();
            
            //If the parameter starts with the param prefix add it to the Map
            if (parameterName.startsWith(fqParameterName)) {
                final String portletParameterName = parameterName.substring(fqParameterName.length());
                final String[] portletParameterValues = parameterEntry.getValue();

                if (portletParameterValues == null) {
                    portletParameters.put(portletParameterName, null);
                }
                else {
                    portletParameters.put(portletParameterName, Arrays.asList(portletParameterValues));
                }
            }
            //If it did not appear on the URL it must be a submit parameter so add it to the Map
            else if (urlParameterNames != null && !urlParameterNames.contains(parameterName)) {
                final String[] portletParameterValues = parameterEntry.getValue();

                if (portletParameterValues == null) {
                    portletParameters.put(parameterName, null);
                }
                else {
                    portletParameters.put(parameterName, Arrays.asList(portletParameterValues));
                }
            }
        }
        portletUrl.setParameters(portletParameters);
        
        portletUrl.setSecure(request.isSecure());

        //If delegating recurse
        final String delegateWindowIdStr = request.getParameter(PARAM_DELEGATE_PREFIX + portletWindowIdStr);
        if (delegateWindowIdStr != null) {
            final IPortletWindowId delegateWindowId = this.portletWindowRegistry.getPortletWindowId(delegateWindowIdStr);
            
            //Verify delegation change
            final IPortletWindow delegateWindow = this.portletWindowRegistry.getPortletWindow(request, delegateWindowId);
            final IPortletWindowId delegationParentId = delegateWindow.getDelegationParent();
            if (delegationParentId == null) {
                throw new IllegalArgumentException("Delegate window '" + delegateWindowId + "' has no parent. Parent specified in the URL is '" + portletWindowId + "'");
            }
            else if (!portletWindowId.equals(delegationParentId)) {
                throw new IllegalArgumentException("Parent '" + delegationParentId + "' of delegate window '" + delegateWindowId + "' is not the parent specified in the URL: '" + portletWindowId + "'");
            }
            
            final PortletUrl delegatePortletUrl = new PortletUrl(delegateWindowId);
            portletUrl.setDelegatePortletUrl(delegatePortletUrl);
            
            this.parsePortletParameters(request, delegatePortletUrl);
        }
    }
    
    protected IPortletWindowId resolveTargetWindowId(HttpServletRequest request) {
        final String targetedPortletWindowIdStr = request.getParameter(PARAM_REQUEST_TARGET);
        if (targetedPortletWindowIdStr != null) {
            return this.portletWindowRegistry.getPortletWindowId(targetedPortletWindowIdStr);
        }
        
        //Fail over to looking for a fname
        final String targetedFname = request.getParameter("uP_fname");
        if (targetedFname == null) {
            return null;
        }
        
        //Found an FName lookup the appropriate portlet window id
        
        //Get the user's layout manager
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        
        //Determine the subscribe ID
        final String channelSubscribeId = userLayoutManager.getSubscribeId(targetedFname);
        if (channelSubscribeId == null) {
            this.logger.info("No channel subscribe ID found for fname '" + targetedFname + "'. skipping portlet parameter processing");
            return null;
        }
        
        //Find the channel and portlet definitions
        final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(channelSubscribeId);
        final String channelPublishId = channelNode.getChannelPublishId();
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(Integer.parseInt(channelPublishId));
        if (!portletDefinition.getChannelDefinition().isPortlet()) {
            this.logger.info("No portlet defintion found for channel definition '" + channelPublishId + "' with fname '" + targetedFname + "'. skipping portlet parameter processing");
            return null;
        }
        
        //Determine the appropriate portlet window ID
        final IPerson person = userInstance.getPerson();
        final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinition.getPortletDefinitionId(), channelSubscribeId, person.getID());
        final IPortletWindow defaultPortletWindow = this.portletWindowRegistry.createDefaultPortletWindow(request, portletEntity.getPortletEntityId());

        return this.portletWindowRegistry.createTransientPortletWindowId(request, defaultPortletWindow.getPortletWindowId());
    }
    


    /**
     * Parses the request URL to return a Set of the parameter names that appeared on the URL string.
     * 
     * @param request The request to look at.
     * @return The Set of parameter names from the URL.
     */
    protected Set<String> getUrlParameterNames(HttpServletRequest request) {
        // Only posts can have parameters not in the URL, ignore non-post requests.
        final String method = request.getMethod();
        if (!"POST".equals(method)) {
            return null;
        }
        
        final Set<String> urlParameterNames = new HashSet<String>();
        
        final String queryString = request.getQueryString();
        final Matcher paramNameMatcher = URL_PARAM_NAME.matcher("&" + queryString);

        final String encoding = this.getEncoding(request);
        
        while (paramNameMatcher.find()) {
            final String paramName = paramNameMatcher.group(1);
            String decParamName;
            try {
                decParamName = URLDecoder.decode(paramName, encoding);
            }
            catch (UnsupportedEncodingException uee) {
                decParamName = paramName;
            }
            
            urlParameterNames.add(decParamName);
        }
        
        return urlParameterNames;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(portletUrl, "portletUrl can not be null");
        
        //Convert the callback request to the portal request
        request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
        
        //If this portlet is a delegate and this is an action request it must be for the redirect URL
        //Store the PortletUrl as a request attribute so the dispatcher can get to it
        //Return a marker redirect URL string so the delegate dispatcher knows to ignore the redirect
        //TODO refactor
        /*
        if (Constants.METHOD_ACTION.equals(request.getAttribute(Constants.METHOD_ID))) {
            if (portletWindow.getDelegationParent() != null) {
                this.portletDelegationManager.setDelegatePortletActionRedirectUrl(request, portletUrl);
                return IPortletDelegationManager.DELEGATE_ACTION_REDIRECT_TOKEN;
            }

            final PortletUrl delegatePortletUrl = this.portletDelegationManager.getDelegatePortletActionRedirectUrl(request);
            portletUrl.setDelegatePortletUrl(delegatePortletUrl);
        }
        */
        
        //Build the base of the URL with the context path
        final StringBuilder url = new StringBuilder(this.bufferLength);
        final String contextPath = request.getContextPath();
        url.append(contextPath).append("/");
        
        this.generatePortletUrl(request, portletWindow, null, portletUrl, url);
 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated portlet URL '" + url + "' for IPortletWindow='" + portletWindow + "' and PortletUrl='" + portletUrl + "'. StringBuilder started with length " + this.bufferLength + " and ended with length " + url.capacity() + ".");
        }
        
        return url.toString();
    }
    
    protected void generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, IPortletWindowId delegationChildId, PortletUrl portletUrl, StringBuilder url) {
        //Get the encoding to use for the URL
        final String encoding = this.getEncoding(request);
        
        //Look to see if the window is being delegated to, if so recurse to the parent for URL generation first
        final IPortletWindowId delegationParentId = portletWindow.getDelegationParent();
        if (delegationParentId != null) {
            final IPortletWindow delegateParent = this.portletWindowRegistry.getPortletWindow(request, delegationParentId);
            
            PortletUrl parentUrl = this.portletDelegationManager.getParentPortletUrl(request, delegationParentId);
            if (parentUrl == null) {
                parentUrl = new PortletUrl(delegationParentId);
                parentUrl.setWindowState(delegateParent.getWindowState());
                parentUrl.setPortletMode(delegateParent.getPortletMode());
                parentUrl.setParameters(delegateParent.getRequestParameters());
            }
            
            //Parent URLs MUST be in the same type as the child
            parentUrl.setRequestType(portletUrl.getRequestType());
            
            this.generatePortletUrl(request, delegateParent, portletWindow.getPortletWindowId(), parentUrl, url);
        }
        
        // TODO Need to decide how to deal with 'secure' URL requests

        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final String channelSubscribeId = parentPortletEntity.getChannelSubscribeId();

        WindowState windowState = portletUrl.getWindowState();
        PortletMode portletMode = portletUrl.getPortletMode();
        
        final WindowState previousWindowState = portletWindow.getWindowState();
        final PortletMode previousPortletMode = portletWindow.getPortletMode();
        
        //Only do this stuff for the top level window
        if (delegationParentId == null) {
            //Get the channel runtime data from the request attributes, it should have been set there by the portlet adapter
            final ChannelRuntimeData channelRuntimeData = (ChannelRuntimeData)request.getAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA);
            if (channelRuntimeData == null) {
                throw new IllegalStateException("No ChannelRuntimeData was found as a request attribute for key '" + IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA + "' on request '" + request + "'");
            }
            
            // Determine the base path for the URL
            // If the next state is EXCLUSIVE or there is no state change and the current state is EXCLUSIVE use the worker URL base
            if (IPortletRenderer.EXCLUSIVE.equals(windowState) || (windowState == null && IPortletRenderer.EXCLUSIVE.equals(previousWindowState))) {
                final String urlBase = channelRuntimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
                url.append(urlBase);
            }
            //In detached, need to make sure the URL is right
            else if (IPortletRenderer.DETACHED.equals(windowState) || (windowState == null && IPortletRenderer.DETACHED.equals(previousWindowState))) {
                final UPFileSpec upFileSpec = new UPFileSpec(channelRuntimeData.getUPFile());
                upFileSpec.setMethodNodeId(channelSubscribeId);
                upFileSpec.setTargetNodeId(channelSubscribeId);
                final String urlBase = upFileSpec.getUPFile();
                url.append(urlBase);
            }
            //Switching back from detached to a normal state
            else if (IPortletRenderer.DETACHED.equals(previousWindowState) && windowState != null && !previousWindowState.equals(windowState)) {
                final UPFileSpec upFileSpec = new UPFileSpec(channelRuntimeData.getUPFile());
                upFileSpec.setMethodNodeId(UPFileSpec.USER_LAYOUT_ROOT_NODE);
                final String urlBase = upFileSpec.getUPFile();
                url.append(urlBase);
            }
            //No special handling, just use the base action URL
            else {
                final String urlBase = channelRuntimeData.getBaseActionURL();
                url.append(urlBase);
            }
            
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Using root url base '" + url + "'");
            }
        }
        
        //Set the request target, creating a transient window ID if needed
        boolean forceWindowState = false;
        final String portletWindowIdString;
        //If rendering as a delegate just reuse the id (it will always be transient)
        if (delegationParentId != null) {
            portletWindowIdString = portletWindowId.toString();
        }
        //If switching from a non-transient state to a transient state generate a new transient window id
        else if (this.transientWindowStates.contains(windowState) && !this.transientWindowStates.contains(previousWindowState)) {
            final IPortletWindowId transientPortletWindowId = this.portletWindowRegistry.createTransientPortletWindowId(request, portletWindowId);
            portletWindowIdString = transientPortletWindowId.toString();
        }
        //If the window is transient, it is in a transient state and it is switching from a non-transient state
        else if (this.portletWindowRegistry.isTransient(request, portletWindowId) && 
                !this.transientWindowStates.contains(windowState) &&
                (windowState != null || !this.transientWindowStates.contains(previousWindowState))) {
            //Get non-transient version of id
            final IPortletEntityId portletEntityId = portletWindow.getPortletEntityId();
            final IPortletWindowId defaultPortletWindowId = this.portletWindowRegistry.getDefaultPortletWindowId(portletEntityId);
            portletWindowIdString = defaultPortletWindowId.getStringId();
            
            if (windowState == null) {
                final IPortletWindow defaultPortletWindow = this.portletWindowRegistry.getPortletWindow(request, defaultPortletWindowId);
                if (!previousWindowState.equals(defaultPortletWindow.getWindowState())) {
                    forceWindowState = true;
                    windowState = previousWindowState;
                }
                if (!previousPortletMode.equals(defaultPortletWindow.getPortletMode())) {
                    portletMode = previousPortletMode;
                }
            }
        }
        else {
            portletWindowIdString = portletWindowId.getStringId();
        }
        
        //Only one target per url
        if (delegationParentId == null) {
            this.encodeAndAppend(url.append("?"), encoding, PARAM_REQUEST_TARGET, portletWindowIdString);
        }
        
        //Only if actually delegating rendering
        if (delegationChildId != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_DELEGATE_PREFIX + portletWindowIdString, delegationChildId.toString());
        }
        
        //Set the request type
        final RequestType requestType = portletUrl.getRequestType();
        final String requestTypeString = requestType != null ? requestType.toString() : RequestType.RENDER.toString();
        this.encodeAndAppend(url.append("&"), encoding, PARAM_REQUEST_TYPE_PREFIX + portletWindowIdString, requestTypeString);
        
        // If set add the window state
        if (windowState != null && (forceWindowState || !previousWindowState.equals(windowState))) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE_PREFIX + portletWindowIdString, windowState.toString());
            
            //uPortal specific parameters are only needed the top most parent portlet window
            if (delegationParentId == null) {
                //Add the parameters needed by the portal structure & theme to render the correct window state 
                if (WindowState.MAXIMIZED.equals(windowState)) {
                    this.encodeAndAppend(url.append("&"), encoding, "uP_root", channelSubscribeId);
                }
                else if (WindowState.NORMAL.equals(windowState)) {
                    this.encodeAndAppend(url.append("&"), encoding, "uP_root", IUserLayout.ROOT_NODE_NAME);
                    this.encodeAndAppend(url.append("&"), encoding, "uP_tcattr", "minimized");
                    this.encodeAndAppend(url.append("&"), encoding, "minimized_channelId", channelSubscribeId);
                    this.encodeAndAppend(url.append("&"), encoding, "minimized_" + channelSubscribeId + "_value", "false");
                }
                else if (WindowState.MINIMIZED.equals(windowState)) {
                    this.encodeAndAppend(url.append("&"), encoding, "uP_root", IUserLayout.ROOT_NODE_NAME);
                    this.encodeAndAppend(url.append("&"), encoding, "uP_tcattr", "minimized");
                    this.encodeAndAppend(url.append("&"), encoding, "minimized_channelId", channelSubscribeId);
                    this.encodeAndAppend(url.append("&"), encoding, "minimized_" + channelSubscribeId + "_value", "true");
                }
                else if (IPortletRenderer.DETACHED.equals(windowState)) {
                    this.encodeAndAppend(url.append("&"), encoding, "uP_detach_target", channelSubscribeId);
                }
            }
        }
        //Or for any transient state always add the window state
        else if (this.transientWindowStates.contains(windowState) || this.transientWindowStates.contains(previousWindowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE_PREFIX + portletWindowIdString, previousWindowState.toString());
        }
        
        //If set add the portlet mode
        if (portletMode != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE_PREFIX + portletWindowIdString, portletMode.toString());
        }
        //Or for any transient state always add the portlet mode
        else if (this.transientWindowStates.contains(windowState) || this.transientWindowStates.contains(previousWindowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE_PREFIX + portletWindowIdString, portletWindow.getPortletMode().toString());
        }
        
        //Add the parameters to the URL
        final Map<String, List<String>> parameters = portletUrl.getParameters();
        if (parameters != null) {
            for (final Map.Entry<String, List<String>> parameterEntry : parameters.entrySet()) {
                final String name = parameterEntry.getKey();
                final List<String> values = parameterEntry.getValue();

                this.encodeAndAppend(url.append("&"), encoding, PORTLET_PARAM_PREFIX + portletWindowIdString + SEPERATOR + name, values);
            }
        }
       
        //Add the anchor if anchoring is enabled
        if (this.useAnchors && delegationParentId == null && !RequestType.ACTION.equals(requestType) && ((windowState != null && this.anchoringWindowStates.contains(windowState)) || (windowState == null && this.anchoringWindowStates.contains(previousWindowState)))) {
            url.append("#").append(channelSubscribeId);
        }
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
     * @param values The values for the parameter, a & will be appeneded between each name/value pair added when multiple values are passed.
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
            boolean first = true;
            for (final Iterator<String> valuesItr = values.iterator(); valuesItr.hasNext(); ) {
                String value = valuesItr.next();
                
                if (value == null) {
                    value = "";
                }
                
                try {
                    value = URLEncoder.encode(value, encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encode portlet URL parameter value '" + value + "' for encoding '" + encoding + "'");
                }
                
                if (!first) {
                    url.append("&");
                }
                
                url.append(name).append("=").append(value);
                first = false;
            }
        }
    }
}
