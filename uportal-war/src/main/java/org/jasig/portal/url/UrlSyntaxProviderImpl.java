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

package org.jasig.portal.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.jasig.portal.xml.xpath.XPathOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * {@link IPortalUrlProvider} and {@link IUrlSyntaxProvider} implementation
 * that uses a consistent human readable URL format.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Component("portalUrlProvider")
public class UrlSyntaxProviderImpl implements IUrlSyntaxProvider {
    static final String SEPARATOR = "_";
    static final String PORTAL_PARAM_PREFIX                  = "u" + SEPARATOR;

    static final String PORTLET_CONTROL_PREFIX               = "pC";
    static final String PORTLET_PARAM_PREFIX                 = "pP" + SEPARATOR;
    static final String PORTLET_PUBLIC_RENDER_PARAM_PREFIX   = "pG" + SEPARATOR;
    static final String PARAM_TARGET_PORTLET                 = PORTLET_CONTROL_PREFIX + "t";
    static final String PARAM_ADDITIONAL_PORTLET             = PORTLET_CONTROL_PREFIX + "a";
    static final String PARAM_DELEGATE_PARENT                = PORTLET_CONTROL_PREFIX + "d";
    static final String PARAM_RESOURCE_ID                    = PORTLET_CONTROL_PREFIX + "r";
    static final String PARAM_CACHEABILITY                   = PORTLET_CONTROL_PREFIX + "c";
    static final String PARAM_WINDOW_STATE                   = PORTLET_CONTROL_PREFIX + "s";
    static final String PARAM_PORTLET_MODE                   = PORTLET_CONTROL_PREFIX + "m";
    static final String PARAM_COPY_PARAMETERS                = PORTLET_CONTROL_PREFIX + "p";
    
    static final Set<String> LEGACY_URL_PATHS = ImmutableSet.of(
            "/render.userLayoutRootNode.uP",
            "/tag.idempotent.render.userLayoutRootNode.uP");
    static final String LEGACY_PARAM_PORTLET_FNAME = "uP_fname";
    static final String LEGACY_PARAM_PORTLET_REQUEST_TYPE = "pltc_type";
    static final String LEGACY_PARAM_PORTLET_STATE = "pltc_state";
    static final String LEGACY_PARAM_PORTLET_MODE = "pltc_mode";
    static final String LEGACY_PARAM_PORTLET_PARAM_PREFX = "pltp_";
    static final String LEGACY_PARAM_LAYOUT_ROOT = "root";
    static final String LEGACY_PARAM_LAYOUT_ROOT_VALUE = "uP_root";
    static final String LEGACY_PARAM_LAYOUT_STRUCT_PARAM = "uP_sparam";
    static final String LEGACY_PARAM_LAYOUT_TAB_ID = "activeTab";
    
    static final String SLASH = "/";
    static final String PORTLET_PATH_PREFIX = "p";
    static final String FOLDER_PATH_PREFIX = "f";
    static final String REQUEST_TYPE_SUFFIX = ".uP";
    
    private static final Pattern SLASH_PATTERN = Pattern.compile(SLASH);
    private static final String PORTAL_CANONICAL_URL = UrlSyntaxProviderImpl.class.getName() + ".PORTAL_CANONICAL_URL";
    private static final String PORTAL_REQUEST_INFO_ATTR = UrlSyntaxProviderImpl.class.getName() + ".PORTAL_REQUEST_INFO"; 
    private static final String PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR = UrlSyntaxProviderImpl.class.getName() + ".PORTAL_REQUEST_PARSING_IN_PROGRESS";
    
    /**
     * Enum used in getPortalRequestInfo to keep track of the parser state when reading the URL string
     */
    private enum ParseStep {
        FOLDER,
        PORTLET,
        STATE,
        TYPE,
        COMPLETE;
    }
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * WindowStates that are communicated as part of the path
     */
    private static final Set<WindowState> PATH_WINDOW_STATES = new LinkedHashSet<WindowState>(Arrays.asList(WindowState.MAXIMIZED, IPortletRenderer.DETACHED, IPortletRenderer.EXCLUSIVE));
    
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    private Set<UrlState> statelessUrlStates = EnumSet.of(UrlState.DETACHED, UrlState.EXCLUSIVE);
    private String defaultEncoding = "UTF-8";
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry;
    private IPortalUrlProvider portalUrlProvider;
    private IUserInstanceManager userInstanceManager;
    private XPathOperations xpathOperations;

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setXpathOperations(XPathOperations xpathOperations) {
        this.xpathOperations = xpathOperations;
    }

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    /**
     * @param defaultEncoding the defaultEncoding to set
     */
    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }
    
    @Autowired
    public void setUrlNodeSyntaxHelperRegistry(IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry) {
        this.urlNodeSyntaxHelperRegistry = urlNodeSyntaxHelperRegistry;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalUrlProvider#getPortalRequestInfo(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public IPortalRequestInfo getPortalRequestInfo(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        final IPortalRequestInfo cachedPortalRequestInfo = (IPortalRequestInfo)request.getAttribute(PORTAL_REQUEST_INFO_ATTR);
        if (cachedPortalRequestInfo != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("short-circuit: found portalRequestInfo within request attributes");
            }
            return cachedPortalRequestInfo;
        }
        
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            // set a flag to say this request is currently being parsed
            final Boolean inProgressAttr = (Boolean) request.getAttribute(PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR);
            if(inProgressAttr != null && inProgressAttr) {
                if(logger.isDebugEnabled()) {
                    logger.warn("Portal request info parsing already in progress, returning null");
                }
                return null;
            }
            request.setAttribute(PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR, Boolean.TRUE);
        }
        
        
        try {
            //Clone the parameter map so data can be removed from it as it is parsed to help determine what to do with non-namespaced parameters
            @SuppressWarnings("unchecked")
            final Map<String, String[]> parameterMap = new ParameterMap(request.getParameterMap());
            
            final String requestPath = this.urlPathHelper.getPathWithinApplication(request);
            if (LEGACY_URL_PATHS.contains(requestPath)) {
                return parseLegacyPortalUrl(request, parameterMap);
            }
            
            final IUrlNodeSyntaxHelper urlNodeSyntaxHelper = this.urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request);
            
            final PortalRequestInfoImpl portalRequestInfo = new PortalRequestInfoImpl();
            IPortletWindowId targetedPortletWindowId = null;
            PortletRequestInfoImpl targetedPortletRequestInfo = null;
            
            final String[] requestPathParts = SLASH_PATTERN.split(requestPath);
            
            UrlState requestedUrlState = null;
            ParseStep parseStep = ParseStep.FOLDER;
            for (int pathPartIndex = 0; pathPartIndex < requestPathParts.length; pathPartIndex++) {
                String pathPart = requestPathParts[pathPartIndex];
                if (StringUtils.isEmpty(pathPart)) {
                    continue;
                }
                
                switch (parseStep) {
                    case FOLDER: {
                        parseStep = ParseStep.PORTLET;
                        
                        if (FOLDER_PATH_PREFIX.equals(pathPart)) {
                            //Skip adding the prefix to the folders deque
                            pathPartIndex++;
                            
                            final LinkedList<String> folders = new LinkedList<String>();
                            for (;pathPartIndex < requestPathParts.length; pathPartIndex++) {
                                pathPart = requestPathParts[pathPartIndex];
                                
                                //Found the portlet part of the path, step back one and finish folder parsing
                                if (PORTLET_PATH_PREFIX.equals(pathPart)) {
                                    pathPartIndex--;
                                    break;
                                }
                                //Found the end of the path, step back one, check for state and finish folder parsing
                                else if (pathPart.endsWith(REQUEST_TYPE_SUFFIX)) {
                                    pathPartIndex--;
                                    pathPart = requestPathParts[pathPartIndex];
                                    
                                    //If a state was added to the folder list remove it and step back one so other code can handle it
                                    if (UrlState.valueOfIngoreCase(pathPart, null) != null) {
                                        folders.removeLast();
                                        pathPartIndex--;
                                    }
                                    break;
                                }
    
                                folders.add(pathPart);
                            }
                            
                            if (folders.size() > 0) {
                                final String targetedLayoutNodeId = urlNodeSyntaxHelper.getLayoutNodeForFolderNames(request, folders);
                                portalRequestInfo.setTargetedLayoutNodeId(targetedLayoutNodeId);
                            }
                            break;
                        }
                    }
                    case PORTLET: {
                        parseStep = ParseStep.STATE;
                        
                        final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
                        
                        if (PORTLET_PATH_PREFIX.equals(pathPart)) {
                            if (++pathPartIndex < requestPathParts.length) {
                                pathPart = requestPathParts[pathPartIndex];

                                targetedPortletWindowId = urlNodeSyntaxHelper.getPortletForFolderName(request, targetedLayoutNodeId, pathPart);
                            }

                            break;
                        }
                        
                        //See if a portlet was targeted by parameter  
                        final String[] targetedPortletIds = parameterMap.remove(PARAM_TARGET_PORTLET);
                        if (targetedPortletIds != null && targetedPortletIds.length > 0) {
                            final String targetedPortletString = targetedPortletIds[0];
                            targetedPortletWindowId = urlNodeSyntaxHelper.getPortletForFolderName(request, targetedLayoutNodeId, targetedPortletString);
                        }
                        
                    }
                    case STATE: {
                        parseStep = ParseStep.TYPE;
                        
                        //States other than the default only make sense if a portlet is being targeted
                        if (targetedPortletWindowId == null) {
                            break;
                        }
                        
                        requestedUrlState = UrlState.valueOfIngoreCase(pathPart, null);
    
                        //Set the URL state
                        if (requestedUrlState != null) {
                            portalRequestInfo.setUrlState(requestedUrlState);
                            
                            //If the request is stateless
                            if (statelessUrlStates.contains(requestedUrlState)) {
                                final IPortletWindow statelessPortletWindow = this.portletWindowRegistry.getOrCreateStatelessPortletWindow(request, targetedPortletWindowId);
                                targetedPortletWindowId = statelessPortletWindow.getPortletWindowId();
                            }
                            
                            //Create the portlet request info
                            targetedPortletRequestInfo = portalRequestInfo.getPortletRequestInfo(targetedPortletWindowId);
                            portalRequestInfo.setTargetedPortletWindowId(targetedPortletWindowId);
                            
                            //Set window state based on URL State first then look for the window state parameter
                            switch (requestedUrlState) {
                                case MAX: {
                                    targetedPortletRequestInfo.setWindowState(WindowState.MAXIMIZED);
                                }
                                break;
                
                                case DETACHED: {
                                    targetedPortletRequestInfo.setWindowState(IPortletRenderer.DETACHED);
                                }
                                break;
                
                                case EXCLUSIVE: {
                                    targetedPortletRequestInfo.setWindowState(IPortletRenderer.EXCLUSIVE);
                                }
                                break;
                            }
                            
                            break;
                        }
                    }
                    case TYPE: {
                        parseStep = ParseStep.COMPLETE;
                        
                        if (pathPartIndex == requestPathParts.length - 1 && pathPart.endsWith(REQUEST_TYPE_SUFFIX) && pathPart.length() > REQUEST_TYPE_SUFFIX.length()) {
                            final String urlTypePart = pathPart.substring(0, pathPart.length() - REQUEST_TYPE_SUFFIX.length());
                            
                            final UrlType urlType = UrlType.valueOfIngoreCase(urlTypePart, null);
                            if (urlType != null) {
                                portalRequestInfo.setUrlType(urlType);
                                break;
                            }
                        }
                    }
                }
            }

            //If a targeted portlet window ID is found but no targeted portlet request info has been retrieved yet, set it up
            if (targetedPortletWindowId != null && targetedPortletRequestInfo == null) {
                targetedPortletRequestInfo = portalRequestInfo.getPortletRequestInfo(targetedPortletWindowId);
                portalRequestInfo.setTargetedPortletWindowId(targetedPortletWindowId);
            }
            
            //Get the set of portlet window ids that also have parameters on the url
            final String[] additionalPortletIdArray = parameterMap.remove(PARAM_ADDITIONAL_PORTLET);
            final Set<String> additionalPortletIds = Sets.newHashSet(additionalPortletIdArray != null ? additionalPortletIdArray : new String[0]);
            
            //Used if there is delegation to capture form-submit and other non-prefixed parameters
            //Map of parent id to delegate id
            final Map<IPortletWindowId, IPortletWindowId> delegateIdMappings = new LinkedHashMap<IPortletWindowId, IPortletWindowId>(0);
            
            //Parse all remaining parameters from the request
            final Set<Entry<String, String[]>> parameterEntrySet = parameterMap.entrySet();
            for (final Iterator<Entry<String, String[]>> parameterEntryItr = parameterEntrySet.iterator(); parameterEntryItr.hasNext(); ) {
                final Entry<String, String[]> parameterEntry = parameterEntryItr.next();
                
                final String name = parameterEntry.getKey();
                final List<String> values = Arrays.asList(parameterEntry.getValue());
                
                /* NOTE: continues are being used to allow fall-through behavior like a switch statement would provide */
                
                //Portal Parameters, just need to remove the prefix
                if (name.startsWith(PORTAL_PARAM_PREFIX)) {
                    final Map<String, List<String>> portalParameters = portalRequestInfo.getPortalParameters();
                    portalParameters.put(this.safeSubstringAfter(PORTAL_PARAM_PREFIX, name), values);
                    parameterEntryItr.remove();
                    continue;
                }
                
                //Generic portlet parameters, have to remove the prefix and see if there was a portlet windowId between the prefix and parameter name
                if (name.startsWith(PORTLET_PARAM_PREFIX)) {
                    final Tuple<String, IPortletWindowId> portletParameterParts = this.parsePortletParameterName(request, name, additionalPortletIds);
                    final IPortletWindowId portletWindowId = portletParameterParts.second;
                    final String paramName = portletParameterParts.first;

                    //Get the portlet parameter map to add the parameter to
                    final Map<String, List<String>> portletParameters;
                    if (portletWindowId == null) {
                        if (targetedPortletRequestInfo == null) {
                            this.logger.warn("Parameter " + name + " is for the targeted portlet but no portlet is targeted by the request. The parameter will be ignored. Value: " + values);
                            parameterEntryItr.remove();
                            break;
                        }
                        
                        portletParameters = targetedPortletRequestInfo.getPortletParameters();
                    }
                    else {
                        final PortletRequestInfoImpl portletRequestInfoImpl = portalRequestInfo.getPortletRequestInfo(portletWindowId);
                        portletParameters = portletRequestInfoImpl.getPortletParameters();
                    }
                    
                    portletParameters.put(paramName, values);
                    parameterEntryItr.remove();
                    continue;
                }
                
                //Portlet control parameters are either used directly or as a prefix to a windowId. Use the SuffixedPortletParameter to simplify their parsing
                for (final SuffixedPortletParameter suffixedPortletParameter : SuffixedPortletParameter.values()) {
                    final String parameterPrefix = suffixedPortletParameter.getParameterPrefix();
                    //Skip to the next parameter prefix if the current doesn't match
                    if (!name.startsWith(parameterPrefix)) {
                        continue;
                    }
                    
                    //All of these parameters require at least one value
                    if (values.isEmpty()) {
                        this.logger.warn("Ignoring parameter " + name + " as it must have a value. Value: " + values);
                        break;
                    }
                    
                    //Verify the parameter is being used on the correct type of URL
                    final Set<UrlType> validUrlTypes = suffixedPortletParameter.getValidUrlTypes();
                    if (!validUrlTypes.contains(portalRequestInfo.getUrlType())) {
                        this.logger.warn("Ignoring parameter " + name + " as it is only valid for " + validUrlTypes + " requests and this is a " + portalRequestInfo.getUrlType() + " request. Value: " + values);
                        break;
                    }
                    
                    //Determine the portlet window and request info the parameter targets
                    final IPortletWindowId portletWindowId = this.parsePortletWindowIdSuffix(request, parameterPrefix, additionalPortletIds, name);
                    final PortletRequestInfoImpl portletRequestInfo = getTargetedPortletRequestInfo(portalRequestInfo, targetedPortletRequestInfo, portletWindowId);
                    if (portletRequestInfo == null) {
                        this.logger.warn("Parameter " + name + " is for the targeted portlet but no portlet is targeted by the request. The parameter will be ignored. Value: " + values);
                        break;
                    }
                    
                    parameterEntryItr.remove();
                    
                    //Use the enum helper to store the parameter values on the requet info
                    switch (suffixedPortletParameter) {
                        case RESOURCE_ID: {
                            portletRequestInfo.setResourceId(values.get(0));
                            break;
                        }
                        case CACHEABILITY: {
                            portletRequestInfo.setCacheability(values.get(0));
                            break;
                        }
                        case DELEGATE_PARENT: {
                            try {
                                final IPortletWindowId delegateParentWindowId = this.portletWindowRegistry.getPortletWindowId(request, values.get(0));
                                portletRequestInfo.setDelegateParentWindowId(delegateParentWindowId);
                                final IPortletWindowId delegateWindowId = portletRequestInfo.getPortletWindowId();
                                delegateIdMappings.put(delegateParentWindowId, delegateWindowId);
                            }
                            catch (IllegalArgumentException e) {
                                this.logger.warn("Failed to parse delegate portlet window ID '" + values.get(0) + "', the delegation window parameter will be ignored", e);
                            }
                            
                            break;
                        }
                        case WINDOW_STATE: {
                            portletRequestInfo.setWindowState(PortletUtils.getWindowState(values.get(0)));
                            break;
                        }
                        case PORTLET_MODE: {
                            portletRequestInfo.setPortletMode(PortletUtils.getPortletMode(values.get(0)));
                            break;
                        }
                        case COPY_PARAMETERS: {
                            final Map<String, List<String>> portletParameters = portletRequestInfo.getPortletParameters();
                            
                            final IPortletWindowId portletRequestInfoWindowId = portletRequestInfo.getPortletWindowId();
                            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletRequestInfoWindowId);
                            final Map<String, String[]> renderParameters = portletWindow.getRenderParameters();
                            
                            ParameterMap.putAllList(portletParameters, renderParameters);
                            
                            break;
                        }
                        default: {
                            //Uhoh, a new SuffixedPortletParameter was added without updating this switch block, don't fail but log a warning
                            this.logger.warn("Programming Error: An unknown SuffixedPortletParameter " + name + " was seen in the UrlSyntaxProvider, it will be ignored. ALL possible SuffixedPortletParameter should be handled. " + suffixedPortletParameter);
                        }
                    }
                    
                    break;
                }
            }

            //Any non-namespaced parameters still need processing?
            if (!parameterMap.isEmpty()) {
                //If the parameter was not ignored by a previous parser add it to whatever was targeted (portlet or portal)
                final Map<String, List<String>> parameters;
                if (!delegateIdMappings.isEmpty()) {
                    //Resolve the last portlet window in the chain of delegation
                    PortletRequestInfoImpl delegatePortletRequestInfo = null;
                    for (final IPortletWindowId delegatePortletWindowId : delegateIdMappings.values()) {
                        if (!delegateIdMappings.containsKey(delegatePortletWindowId)) {
                            delegatePortletRequestInfo = portalRequestInfo.getPortletRequestInfo(delegatePortletWindowId);
                            break;
                        }
                    }
                    
                    if (delegatePortletRequestInfo != null) {
                        parameters = delegatePortletRequestInfo.getPortletParameters();
                    }
                    else {
                        this.logger.warn("No root delegate portlet could be resolved, non-namespaced parameters will be sent to the targeted portlet. THIS SHOULD NEVER HAPPEN. Delegate parent/child mapping: " + delegateIdMappings);
                        
                        if (targetedPortletRequestInfo != null) {
                            parameters = targetedPortletRequestInfo.getPortletParameters();
                        }
                        else {
                            parameters = portalRequestInfo.getPortalParameters();
                        }
                    }
                }
                else if (targetedPortletRequestInfo != null) {
                    parameters = targetedPortletRequestInfo.getPortletParameters();
                }
                else {
                    parameters = portalRequestInfo.getPortalParameters();
                }
                
                ParameterMap.putAllList(parameters, parameterMap);
            }
            
            //If a portlet is targeted but no layout node is targeted must be maximized
            if (targetedPortletRequestInfo != null && portalRequestInfo.getTargetedLayoutNodeId() == null && (requestedUrlState == null || requestedUrlState == UrlState.NORMAL)) {
                portalRequestInfo.setUrlState(UrlState.MAX);
                targetedPortletRequestInfo.setWindowState(WindowState.MAXIMIZED);
            }
            
            //Make the request info object read-only, once parsed the request info should be static
            portalRequestInfo.makeReadOnly();
            
            request.setAttribute(PORTAL_REQUEST_INFO_ATTR, portalRequestInfo);
            
            if(logger.isDebugEnabled()) {
                logger.debug("finished building requestInfo: " + portalRequestInfo);
            }
            
            return portalRequestInfo;
        }
        finally {
            request.removeAttribute(PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR);
        }
    }
    
    protected IPortalRequestInfo parseLegacyPortalUrl(HttpServletRequest request, Map<String, String[]> parameterMap) {
        final PortalRequestInfoImpl portalRequestInfo = new PortalRequestInfoImpl();
        
        final String[] fname = parameterMap.remove(LEGACY_PARAM_PORTLET_FNAME);
        if (fname != null && fname.length > 0) {
            final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(request, fname[0]);
            if (portletWindow != null) {
                logger.debug("Legacy fname parameter {} resolved to {}", fname[0], portletWindow);
                
                final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                portalRequestInfo.setTargetedPortletWindowId(portletWindowId);

                final PortletRequestInfoImpl portletRequestInfo = portalRequestInfo.getPortletRequestInfo(portletWindowId);
                
                //Check the portlet request type
                final String[] type = parameterMap.remove(LEGACY_PARAM_PORTLET_REQUEST_TYPE);
                if (type != null && type.length > 0 && "ACTION".equals(type[0])) {
                    portalRequestInfo.setUrlType(UrlType.ACTION);
                }
                
                //Set the window state
                final String[] state = parameterMap.remove(LEGACY_PARAM_PORTLET_STATE);
                if (state != null && state.length > 0) {
                    final WindowState windowState = PortletUtils.getWindowState(state[0]);
                    portletRequestInfo.setWindowState(windowState);
                }
                
                //Set the portlet mode
                final String[] mode = parameterMap.remove(LEGACY_PARAM_PORTLET_MODE);
                if (mode != null && mode.length > 0) {
                    final PortletMode portletMode = PortletUtils.getPortletMode(mode[0]);
                    portletRequestInfo.setPortletMode(portletMode);
                }
                
                //Set the parameters
                final Map<String, List<String>> portletParameters = portletRequestInfo.getPortletParameters();
                for (final Map.Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
                    final String prefixedName = parameterEntry.getKey();
                    
                    //If the parameter starts with the portlet param prefix
                    if (prefixedName.startsWith(LEGACY_PARAM_PORTLET_PARAM_PREFX)) {
                        final String name = prefixedName.substring(LEGACY_PARAM_PORTLET_PARAM_PREFX.length()); 
                        
                        portletParameters.put(name, Arrays.asList(parameterEntry.getValue()));
                    }
                }
                
                //Set the url state based on the window state
                final UrlState urlState = this.determineUrlState(portletWindow, portletRequestInfo.getWindowState());
                portalRequestInfo.setUrlState(urlState);
            }
            else {
                logger.debug("Could not find portlet for legacy fname fname parameter {}", fname[0]);
            }
        }
        
        //Check root=uP_root
        final String[] root = parameterMap.remove(LEGACY_PARAM_LAYOUT_ROOT);
        if (root != null && root.length > 0) {
            if (LEGACY_PARAM_LAYOUT_ROOT_VALUE.equals(root[0])) {
                
                //Check uP_sparam=activeTab
                final String[] structParam = parameterMap.remove(LEGACY_PARAM_LAYOUT_STRUCT_PARAM);
                if (structParam != null && structParam.length > 0) {
                    if (LEGACY_PARAM_LAYOUT_TAB_ID.equals(structParam[0])) {
                        
                        //Get the active tab id
                        final String[] activeTabId = parameterMap.remove(LEGACY_PARAM_LAYOUT_TAB_ID);
                        if (activeTabId != null && activeTabId.length > 0) {
                            //Get the user's layout and do xpath for tab at index=activeTabId[0]
                            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
                            final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
                            final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
                            final IUserLayout userLayout = userLayoutManager.getUserLayout();
                            
                            final String nodeId = this.xpathOperations.doWithExpression(
                                    "/layout/folder/folder[@type='regular' and @hidden='false'][position() = $activeTabId]/@ID", 
                                    Collections.singletonMap("activeTabId", activeTabId[0]), 
                                    new Function<XPathExpression, String>() {
                                        @Override
                                        public String apply(XPathExpression xPathExpression) {
                                            return userLayout.findNodeId(xPathExpression);
                                        }
                                    });

                            //Found nodeId for activeTabId
                            if (nodeId != null) {
                                logger.debug("Found layout node {} for legacy activeTabId parameter {}", nodeId, activeTabId[0]);
                                portalRequestInfo.setTargetedLayoutNodeId(nodeId);
                            }
                            else {
                                logger.debug("No layoout node found for legacy activeTabId parameter {}", activeTabId[0]);
                            }
                        }
                    }
                }
            }
        }
        
        
        return portalRequestInfo;
    }

    /**
     * If the targetedPortletWindowId is not null {@link #getPortletRequestInfo(IPortalRequestInfo, Map, IPortletWindowId)} is called and that
     * value is returned. If targetedPortletWindowId is null targetedPortletRequestInfo is returned.
     */
    protected PortletRequestInfoImpl getTargetedPortletRequestInfo(
            final PortalRequestInfoImpl portalRequestInfo,
            final PortletRequestInfoImpl targetedPortletRequestInfo, 
            final IPortletWindowId targetedPortletWindowId) {
        
        if (targetedPortletWindowId == null) {
            return targetedPortletRequestInfo;
        }

        return portalRequestInfo.getPortletRequestInfo(targetedPortletWindowId);
    }
    
    /**
     * Parse the parameter name and the optional portlet window id from a fully qualified query parameter.
     */
    protected Tuple<String, IPortletWindowId> parsePortletParameterName(HttpServletRequest request, String name, Set<String> additionalPortletIds) {
        //Look for a 2nd separator which might indicate a portlet window id
        for (final String additionalPortletId : additionalPortletIds) {
            final int windowIdIdx = name.indexOf(additionalPortletId);
            if (windowIdIdx == -1) {
                continue;
            }
            
            final String paramName = name.substring(PORTLET_PARAM_PREFIX.length() + additionalPortletId.length() + SEPARATOR.length());
            final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(request, additionalPortletId);
            return new Tuple<String, IPortletWindowId>(paramName, portletWindowId);
        }
        
        final String paramName = this.safeSubstringAfter(PORTLET_PARAM_PREFIX, name);
        return new Tuple<String, IPortletWindowId>(paramName, null);
    }

    /**
     * Determines if the parameter name contains a {@link IPortletWindowId} after the prefix. The id must also be contained in the Set
     * of additionalPortletIds. If no id is found in the parameter name null is returned.
     */
    protected IPortletWindowId parsePortletWindowIdSuffix(HttpServletRequest request, final String prefix, final Set<String> additionalPortletIds, final String name) {
        //See if the parameter name has an additional separator
        final int windowIdStartIdx = name.indexOf(SEPARATOR, prefix.length());
        if (windowIdStartIdx < (prefix.length() + SEPARATOR.length()) - 1) {
            return null;
        }
        
        //Extract the windowId string and see if it was listed as an additional windowId
        final String portletWindowIdStr = name.substring(windowIdStartIdx + SEPARATOR.length());
        if (additionalPortletIds.contains(portletWindowIdStr)) {
            try {
                return this.portletWindowRegistry.getPortletWindowId(request, portletWindowIdStr);
            }
            catch (IllegalArgumentException e) {
                this.logger.warn("Failed to parse portlet window id: " + portletWindowIdStr + " null will be returned", e);
            }
        }

        return null;
    }
    
    protected String safeSubstringAfter(String prefix, String fullName) {
        if (prefix.length() >= fullName.length()) {
            return "";
        }
        
        return fullName.substring(prefix.length());
    }

    @Override
    public String getCanonicalUrl(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        final String cachedCanonicalUrl = (String)request.getAttribute(PORTAL_CANONICAL_URL);
        if (cachedCanonicalUrl != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("short-circuit: found canonicalUrl within request attributes");
            }
            return cachedCanonicalUrl;
        }
        
        final IPortalRequestInfo portalRequestInfo = this.getPortalRequestInfo(request);
        final UrlType urlType = portalRequestInfo.getUrlType();
        
        final IPortletWindowId targetedPortletWindowId = portalRequestInfo.getTargetedPortletWindowId();
        final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
        
        //Create a portal url builder with the appropriate target
        final IPortalUrlBuilder portalUrlBuilder;
        if (targetedPortletWindowId != null) {
            portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletWindow(request, targetedPortletWindowId, urlType);
        }
        else if (targetedLayoutNodeId != null) {
            portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByLayoutNode(request, targetedLayoutNodeId, urlType);
        }
        else {
            portalUrlBuilder = this.portalUrlProvider.getDefaultUrl(request);
        }
        
        //Copy over portal parameters
        final Map<String, List<String>> portalParameters = portalRequestInfo.getPortalParameters();
        portalUrlBuilder.setParameters(portalParameters);
        
        //Copy data for each portlet
        for (final IPortletRequestInfo portletRequestInfo : portalRequestInfo.getPortletRequestInfoMap().values()) {
            final IPortletWindowId portletWindowId = portletRequestInfo.getPortletWindowId();
            final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
            
            //Parameters
            final Map<String, List<String>> portletParameters = portletRequestInfo.getPortletParameters();
            portletUrlBuilder.setParameters(portletParameters);
            
            switch (urlType) {
                case RESOURCE: {
                    //cacheability and resourceId for resource requests
                    portletUrlBuilder.setCacheability(portletRequestInfo.getCacheability());
                    portletUrlBuilder.setResourceId(portletRequestInfo.getResourceId());
                }
                
                case RENDER:
                case ACTION: {
                    //state & mode for all requests
                    portletUrlBuilder.setWindowState(portletRequestInfo.getWindowState());
                    portletUrlBuilder.setPortletMode(portletRequestInfo.getPortletMode());
                    break;
                }
            }
        }
        
        return portalUrlBuilder.getUrlString();
    }
    
    @Override
    public String generateUrl(HttpServletRequest request, IPortalActionUrlBuilder portalActionUrlBuilder) {
        final String redirectLocation = portalActionUrlBuilder.getRedirectLocation();
        //If no redirect location just generate the portal url
        if (redirectLocation == null) {
            return this.generateUrl(request, (IPortalUrlBuilder)portalActionUrlBuilder);
        }
        
        final String renderUrlParamName = portalActionUrlBuilder.getRenderUrlParamName();
        //If no render param name just return the redirect url
        if (renderUrlParamName == null) {
            return redirectLocation;
        }
        
        //Need to stick the generated portal url onto the redirect url
        
        final StringBuilder redirectLocationBuilder = new StringBuilder(redirectLocation);
        
        
        final int queryParamStartIndex = redirectLocationBuilder.indexOf("?");
        //Already has parameters, add the new one correctly
        if (queryParamStartIndex > -1) {
            redirectLocationBuilder.append('&');
        }
        //No parameters, add parm seperator
        else {
            redirectLocationBuilder.append('?');
        }
        
        //Generate the portal url
        final String portalRenderUrl = this.generateUrl(request, (IPortalUrlBuilder)portalActionUrlBuilder);

        //Encode the render param name and the render url
        final String encoding = this.getEncoding(request);
        final String encodedRenderUrlParamName;
        final String encodedPortalRenderUrl;
        try {
            encodedRenderUrlParamName = URLEncoder.encode(renderUrlParamName, encoding);
            encodedPortalRenderUrl = URLEncoder.encode(portalRenderUrl, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Encoding '" + encoding + "' is not supported.", e);
        }
        
        return redirectLocationBuilder.append(encodedRenderUrlParamName).append("=").append(encodedPortalRenderUrl).toString();
    }

    @Override
    public String generateUrl(HttpServletRequest request, IPortalUrlBuilder portalUrlBuilder) {
        Validate.notNull(request, "HttpServletRequest was null");
        Validate.notNull(portalUrlBuilder, "IPortalPortletUrl was null");
       
        //Convert the callback request to the portal request
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final IUrlNodeSyntaxHelper urlNodeSyntaxHelper = this.urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request);
        
        //Get the encoding and create a new URL string builder
        final String encoding = this.getEncoding(request);
        final UrlStringBuilder url = new UrlStringBuilder(encoding);
        
        //Add the portal's context path
        final String contextPath = this.getCleanedContextPath(request);
        if (contextPath.length() > 0) {
            url.setPath(contextPath);
        }
        
        final Map<IPortletWindowId, IPortletUrlBuilder> portletUrlBuilders = portalUrlBuilder.getPortletUrlBuilders();
        
        //Build folder path based on targeted portlet or targeted folder
        final IPortletWindowId targetedPortletWindowId = portalUrlBuilder.getTargetPortletWindowId();
        final UrlType urlType = portalUrlBuilder.getUrlType();
        final UrlState urlState;
        if (targetedPortletWindowId != null) {
            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, targetedPortletWindowId);
            final IPortletEntity portletEntity = portletWindow.getPortletEntity();
            
            //Add folder information if available: /f/tabId
            final String channelSubscribeId = portletEntity.getLayoutNodeId();
            final List<String> folderNames = urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, channelSubscribeId);
            if (!folderNames.isEmpty()) {
                url.addPath(FOLDER_PATH_PREFIX);
                for (final String folderName : folderNames) {
                    url.addPath(folderName);
                }
            }
            
            final IPortletUrlBuilder targetedPortletUrlBuilder = portletUrlBuilders.get(targetedPortletWindowId);
            
            //Resource requests will never have a requested window state
            urlState = this.determineUrlState(portletWindow, targetedPortletUrlBuilder);
            
            final String targetedPortletString = urlNodeSyntaxHelper.getFolderNameForPortlet(request, targetedPortletWindowId);
            
            //If a non-normal render url or an action/resource url stick the portlet info in the path 
            if ((urlType == UrlType.RENDER && urlState != UrlState.NORMAL) || urlType == UrlType.ACTION || urlType == UrlType.RESOURCE) {
                url.addPath(PORTLET_PATH_PREFIX);
                url.addPath(targetedPortletString);
            }
            //For normal render requests (generally multiple portlets on a page) add the targeted portlet as a parameter
            else {
                url.addParameter(PARAM_TARGET_PORTLET, targetedPortletString);
            }
        }
        else {
            final String targetFolderId = portalUrlBuilder.getTargetFolderId();
            final List<String> folderNames = urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, targetFolderId);
            if (folderNames != null && !folderNames.isEmpty()) {
                url.addPath(FOLDER_PATH_PREFIX);
                for (final String folderName : folderNames) {
                    url.addPath(folderName);
                }
            }
            
            urlState = UrlState.NORMAL;
        }
        
        //Add the state of the URL
        url.addPath(urlState.toLowercaseString());

        //File part specifying the type of URL
        url.addPath(urlType.toLowercaseString() + REQUEST_TYPE_SUFFIX);
        
        //Add all portal parameters
        final Map<String, String[]> portalParameters = portalUrlBuilder.getParameters();
        url.addParametersArray(PORTAL_PARAM_PREFIX, portalParameters);

        //Is this URL stateless
        final boolean statelessUrl = statelessUrlStates.contains(urlState);
        
        //Add parameters for every portlet URL
        for (final IPortletUrlBuilder portletUrlBuilder : portletUrlBuilders.values()) {
            this.addPortletUrlData(request, url, urlType, portletUrlBuilder, targetedPortletWindowId, statelessUrl);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Generated '" + url + "' from '" + portalUrlBuilder);
        }
        
        return url.toString();
    }

    /**
     * Add the provided portlet url builder data to the url string builder
     */
    protected void addPortletUrlData(
            final HttpServletRequest request, final UrlStringBuilder url, final UrlType urlType, 
            final IPortletUrlBuilder portletUrlBuilder, final IPortletWindowId targetedPortletWindowId, 
            final boolean statelessUrl) {
        
        final IPortletWindowId portletWindowId = portletUrlBuilder.getPortletWindowId();
        final boolean targeted = portletWindowId.equals(targetedPortletWindowId);
        
        IPortletWindow portletWindow = null;
        
        //The targeted portlet doesn't need namespaced parameters
        final String prefixedPortletWindowId;
        final String suffixedPortletWindowId;
        if (targeted) {
            prefixedPortletWindowId = "";
            suffixedPortletWindowId = "";
        }
        else {
            final String portletWindowIdStr = portletWindowId.toString();
            prefixedPortletWindowId = SEPARATOR + portletWindowIdStr;
            suffixedPortletWindowId = portletWindowIdStr + SEPARATOR;
            url.addParameter(PARAM_ADDITIONAL_PORTLET, portletWindowIdStr);

            //targeted portlets can never be delegates (it is always the top most parent that is targeted)
            portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
            final IPortletWindowId delegationParentId = portletWindow.getDelegationParentId();
            if (delegationParentId != null) {
                url.addParameter(PARAM_DELEGATE_PARENT + prefixedPortletWindowId, delegationParentId.getStringId());
            }
        }

        switch (urlType) {
            case RESOURCE: {
                final String cacheability = portletUrlBuilder.getCacheability();
                if(cacheability != null) {
                    url.addParameter(PARAM_CACHEABILITY + prefixedPortletWindowId, cacheability);
                }
                
                final String resourceId = portletUrlBuilder.getResourceId();
                if(resourceId != null) {
                    url.addParameter(PARAM_RESOURCE_ID + prefixedPortletWindowId, resourceId);
                }
                
                break;
            }
            default: {
                //Add requested portlet mode
                final PortletMode portletMode = portletUrlBuilder.getPortletMode();
                if (portletMode != null) {
                    url.addParameter(PARAM_PORTLET_MODE + prefixedPortletWindowId, portletMode.toString());
                }
                else if (targeted && statelessUrl) {
                    portletWindow = portletWindow != null ? portletWindow : this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
                    final PortletMode currentPortletMode = portletWindow.getPortletMode();
                    url.addParameter(PARAM_PORTLET_MODE + prefixedPortletWindowId, currentPortletMode.toString());
                }
                
                //Add requested window state if it isn't included on the path
                final WindowState windowState = portletUrlBuilder.getWindowState();
                if (windowState != null && (!targeted || !PATH_WINDOW_STATES.contains(windowState))) {
                    url.addParameter(PARAM_WINDOW_STATE + prefixedPortletWindowId, windowState.toString());
                }
                
                break;
            }
        }
        
        if (portletUrlBuilder.getCopyCurrentRenderParameters()) {
            url.addParameter(PARAM_COPY_PARAMETERS + suffixedPortletWindowId);
        }
            
        final Map<String, String[]> parameters = portletUrlBuilder.getParameters();
        if (!parameters.isEmpty()) {
            url.addParametersArray(PORTLET_PARAM_PREFIX + suffixedPortletWindowId, parameters);
        }
    }

    /**
     * Determine the {@link UrlState} to use for the targeted portlet window
     */
    protected UrlState determineUrlState(final IPortletWindow portletWindow, final IPortletUrlBuilder targetedPortletUrlBuilder) {
        final WindowState requestedWindowState;
        if (targetedPortletUrlBuilder == null) {
            requestedWindowState = null;
        }
        else {
            requestedWindowState = targetedPortletUrlBuilder.getWindowState();
        }
        
        return determineUrlState(portletWindow, requestedWindowState);
    }

    /**
     * Determine the {@link UrlState} to use for the targeted portlet window
     */
    protected UrlState determineUrlState(final IPortletWindow portletWindow, final WindowState requestedWindowState) {
        //Determine the UrlState based on the WindowState of the targeted portlet 
        final WindowState currentWindowState = portletWindow.getWindowState();
        final WindowState urlWindowState = requestedWindowState != null ? requestedWindowState : currentWindowState;
        if (WindowState.MAXIMIZED.equals(urlWindowState)) {
            return UrlState.MAX;
        }
        
        if (IPortletRenderer.DETACHED.equals(urlWindowState)) {
            return UrlState.DETACHED;
        }
        
        if (IPortletRenderer.EXCLUSIVE.equals(urlWindowState)) {
            return UrlState.EXCLUSIVE;
        }

        if (!WindowState.NORMAL.equals(urlWindowState) && !WindowState.MINIMIZED.equals(urlWindowState)) {
            this.logger.warn("Unknown WindowState '" + urlWindowState + "' specified for portlet window " + portletWindow + ", defaulting to UrlState.NORMAL");
        }
        
        return UrlState.NORMAL;
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

    protected String getCleanedContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        
        if (contextPath.length() == 0) {
            return "";
        }
        
        //Make sure the context path doesn't start with a /
        if (contextPath.charAt(0) == '/') {
            contextPath = contextPath.substring(1);
        }
        
        //Make sure the URL ends with a /
        if (contextPath.charAt(contextPath.length() - 1) == '/') {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        return contextPath;
    }
}
