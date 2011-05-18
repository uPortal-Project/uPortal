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
import java.util.EnumSet;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

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
    public static final String SEPARATOR = "_";
    public static final String PORTAL_PARAM_PREFIX                  = "uP" + SEPARATOR;

    public static final String PORTLET_CONTROL_PREFIX               = "plC";
    public static final String PORTLET_PARAM_PREFIX                 = "plP" + SEPARATOR;
    public static final String PORTLET_PUBLIC_RENDER_PARAM_PREFIX   = "plG" + SEPARATOR;
    public static final String PARAM_TARGET_PORTLET                 = PORTLET_CONTROL_PREFIX + "t";
    public static final String PARAM_ADDITIONAL_PORTLET             = PORTLET_CONTROL_PREFIX + "a";
    public static final String PARAM_RESOURCE_ID                    = PORTLET_CONTROL_PREFIX + "r";
    public static final String PARAM_CACHEABILITY                   = PORTLET_CONTROL_PREFIX + "c";
    public static final String PARAM_WINDOW_STATE                   = PORTLET_CONTROL_PREFIX + "s";
    public static final String PARAM_PORTLET_MODE                   = PORTLET_CONTROL_PREFIX + "m";
    
    public static final String SLASH = "/";
    public static final String PORTLET_PATH_PREFIX = "p";
    public static final String FOLDER_PATH_PREFIX = "f";
    public static final String REQUEST_TYPE_SUFFIX = ".uP";
    
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
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
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
        
        // set a flag to say this request is currently being parsed
        final Boolean inProgressAttr = (Boolean) request.getAttribute(PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR);
        if(inProgressAttr != null && inProgressAttr) {
            if(logger.isDebugEnabled()) {
                logger.warn("Portal request info parsing already in progress, returning null");
            }
            return null;
        }
        request.setAttribute(PORTAL_REQUEST_PARSING_IN_PROGRESS_ATTR, Boolean.TRUE);
        
        
        try {
            final IUrlNodeSyntaxHelper urlNodeSyntaxHelper = this.urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request);
            
            final PortalRequestInfoImpl portalRequestInfo = new PortalRequestInfoImpl();
            IPortletWindowId targetedPortletWindowId = null;
            PortletRequestInfoImpl targetedPortletRequestInfo = null;
            
            //Clone the parameter map so data can be removed from it as it is parsed to help determine what to do with non-namespaced parameters
            @SuppressWarnings("unchecked")
            final Map<String, String[]> parameterMap = new ParameterMap(request.getParameterMap());
            
            final String requestPath = this.urlPathHelper.getPathWithinApplication(request);
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
                        
                        if (PORTLET_PATH_PREFIX.equals(pathPart)) {
                            if (++pathPartIndex < requestPathParts.length) {
                                pathPart = requestPathParts[pathPartIndex];

                                targetedPortletWindowId = urlNodeSyntaxHelper.getPortletForFolderName(request, pathPart);
                            }

                            break;
                        }
                        
                        //See if a portlet was targeted by parameter  
                        final String[] targetedPortletIds = parameterMap.remove(PARAM_TARGET_PORTLET);
                        if (targetedPortletIds != null && targetedPortletIds.length > 0) {
                            final String targetedPortletString = targetedPortletIds[0];
                            targetedPortletWindowId = urlNodeSyntaxHelper.getPortletForFolderName(request, targetedPortletString);
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
                                targetedPortletWindowId = this.portletWindowRegistry.getStatelessPortletWindowId(request, targetedPortletWindowId);
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
            
            //Parse all remaining parameters from the request
            for (final Entry<String, String[]> parameterEntry : parameterMap.entrySet()) {
                final String name = parameterEntry.getKey();
                final List<String> values = Arrays.asList(parameterEntry.getValue());
                
                /* NOTE: continues are being used to allow fall-through behavior like a switch statement would provide */
                
                //Portal Parameters, just need to remove the prefix
                if (name.startsWith(PORTAL_PARAM_PREFIX)) {
                    final Map<String, List<String>> portalParameters = portalRequestInfo.getPortalParameters();
                    portalParameters.put(this.safeSubstringAfter(PORTAL_PARAM_PREFIX, name), values);
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
                            break;
                        }
                        
                        portletParameters = targetedPortletRequestInfo.getPortletParameters();
                    }
                    else {
                        final PortletRequestInfoImpl portletRequestInfoImpl = portalRequestInfo.getPortletRequestInfo(portletWindowId);
                        portletParameters = portletRequestInfoImpl.getPortletParameters();
                    }
                    
                    portletParameters.put(paramName, values);
                    continue;
                }
                
                //Portlet control parameters are either used directly or as a prefix to a windowId. Use the SuffixedPortletParameter to simplify their parsing
                boolean consumed = false;
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
                    
                    //Use the enum helper to store the parameter values on the requet info
                    suffixedPortletParameter.storeParameter(portletRequestInfo, values);
                    consumed = true;
                    break;
                }
                
                //Have to put the continue for the previous loop here
                if (consumed) {
                    continue;
                }

                //If the parameter was not ignored by a previous parser add it to whatever was targeted (portlet or portal) 
                final Map<String, List<String>> parameters;
                if (targetedPortletRequestInfo != null) {
                    parameters = targetedPortletRequestInfo.getPortletParameters();
                }
                else {
                    parameters = portalRequestInfo.getPortalParameters();
                }
                parameters.put(name, values);
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
        final int windowIdEndIdx = name.indexOf(SEPARATOR, PORTLET_PARAM_PREFIX.length());
        if (windowIdEndIdx > PORTLET_PARAM_PREFIX.length()) {
            
            //Extract the suspected window id and check the additional portlet ids Set to see if it was passed on the request
            final String portletWindowIdStr = name.substring(PORTLET_PARAM_PREFIX.length(), windowIdEndIdx);
            if (additionalPortletIds.contains(portletWindowIdStr)) {
                final String paramName;
                
                //Do sanity checks on substring operations to avoid potential exceptions
                if (windowIdEndIdx + SEPARATOR.length() < name.length()) {
                    paramName = name.substring(windowIdEndIdx + SEPARATOR.length());
                }
                else {
                    paramName = "";
                }
                final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(request, portletWindowIdStr);
                
                return new Tuple<String, IPortletWindowId>(paramName, portletWindowId);
            }
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
            return this.portletWindowRegistry.getPortletWindowId(request, portletWindowIdStr);
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
                case RENDER:
                case ACTION: {
                    //state & mode for action & render requests
                    portletUrlBuilder.setWindowState(portletRequestInfo.getWindowState());
                    portletUrlBuilder.setPortletMode(portletRequestInfo.getPortletMode());
                    break;
                }
                
                case RESOURCE: {
                    //cacheability and resourceId for resource requests
                    portletUrlBuilder.setCacheability(portletRequestInfo.getCacheability());
                    portletUrlBuilder.setResourceId(portletRequestInfo.getResourceId());
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
            urlState = this.determineUrlState(urlType, portletWindow, targetedPortletUrlBuilder);
            
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
    protected void addPortletUrlData(final HttpServletRequest request, final UrlStringBuilder url, final UrlType urlType, final IPortletUrlBuilder portletUrlBuilder, final IPortletWindowId targetedPortletWindowId, final boolean statelessUrl) {
        final IPortletWindowId portletWindowId = portletUrlBuilder.getPortletWindowId();
        final boolean targeted = portletWindowId.equals(targetedPortletWindowId);
        
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
                    //TODO deal with delegates of the targeted window, they will be stateless as well
                    final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
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
            
        final Map<String, String[]> parameters = portletUrlBuilder.getParameters();
        if (targeted && statelessUrl && parameters.size() == 0) {
            //TODO deal with delegates of the targeted window, they will be stateless as well
            final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
            final Map<String, String[]> currentParameters = portletWindow.getRenderParameters();
            url.addParametersArray(PORTLET_PARAM_PREFIX + suffixedPortletWindowId, currentParameters);
        }
        else {
            url.addParametersArray(PORTLET_PARAM_PREFIX + suffixedPortletWindowId, parameters);
        }
    }

    /**
     * Determine the {@link UrlState} to use for the targeted portlet window
     */
    protected UrlState determineUrlState(final UrlType urlType, final IPortletWindow portletWindow, final IPortletUrlBuilder targetedPortletUrlBuilder) {
        final WindowState requestedWindowState;
        if (targetedPortletUrlBuilder == null || urlType == UrlType.RESOURCE) {
            requestedWindowState = null;
        }
        else {
            requestedWindowState = targetedPortletUrlBuilder.getWindowState();
        }
        
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
