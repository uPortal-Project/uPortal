/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.portlet.container.PortletContainerUtils;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Required;

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
    private static final String PARAM_REQUEST_TYPE = PORTLET_CONTROL_PREFIX + "type";
    private static final String PARAM_WINDOW_STATE = PORTLET_CONTROL_PREFIX + "state";
    private static final String PARAM_PORTLET_MODE = PORTLET_CONTROL_PREFIX + "mode";
    
    private static final Pattern URL_PARAM_NAME = Pattern.compile("&([^&?=\n]*)");
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String defaultEncoding = "UTF-8";
    private int bufferLength = 512;
    private IPortletWindowRegistry portletWindowRegistry;
    
    
    
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
    @Required
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry, "portletWindowRegistry can not be null");
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#parsePortletParameters(javax.servlet.http.HttpServletRequest)
     */
    public Tuple<IPortletWindowId, PortletUrl> parsePortletParameters(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        final String targetedPortletWindowIdStr = request.getParameter(PARAM_REQUEST_TARGET);
        if (targetedPortletWindowIdStr == null) {
            return null;
        }
        
        final IPortletWindowId targetedPortletWindowId = this.portletWindowRegistry.getPortletWindowId(targetedPortletWindowIdStr);
        final PortletUrl portletUrl = new PortletUrl();
        
        final String requestTypeStr = request.getParameter(PARAM_REQUEST_TYPE);
        if (requestTypeStr != null) {
            final RequestType requestType = RequestType.valueOf(requestTypeStr);
            portletUrl.setRequestType(requestType);
        }
        else {
            //Default to RENDER request if no request type was specified
            portletUrl.setRequestType(RequestType.RENDER);
        }
        
        final String windowStateStr = request.getParameter(PARAM_WINDOW_STATE);
        if (windowStateStr != null) {
            final WindowState windowState = new WindowState(windowStateStr);
            portletUrl.setWindowState(windowState);
        }
        
        final String portletModeStr = request.getParameter(PARAM_PORTLET_MODE);
        if (portletModeStr != null) {
            final PortletMode portletMode = new PortletMode(portletModeStr);
            portletUrl.setPortletMode(portletMode);
        }
        
        final Map<String, String[]> requestParameters = request.getParameterMap();
        final Set<String> urlParameterNames = this.getUrlParameterNames(request);
        
        final Map<String, String[]> portletParameters = new HashMap<String, String[]>(requestParameters.size());
        for (final Map.Entry<String, String[]> parameterEntry : requestParameters.entrySet()) {
            final String parameterName = parameterEntry.getKey();
            
            //If the parameter starts with the param prefix add it to the Map
            if (parameterName.startsWith(PORTLET_PARAM_PREFIX)) {
                final String portletParameterName = parameterName.substring(PORTLET_PARAM_PREFIX.length());
                final String[] portletParameterValues = parameterEntry.getValue();

                portletParameters.put(portletParameterName, portletParameterValues);
            }
            //If it did not appear on the URL it must be a submit parameter so add it to the Map
            else if (urlParameterNames != null && !urlParameterNames.contains(parameterName)) {
                final String[] portletParameterValues = parameterEntry.getValue();

                portletParameters.put(parameterName, portletParameterValues);
            }
        }
        portletUrl.setParameters(portletParameters);
        
        portletUrl.setSecure(request.isSecure());
        
        return new Tuple<IPortletWindowId, PortletUrl>(targetedPortletWindowId, portletUrl);
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
        request = PortletContainerUtils.getOriginalPortletAdaptorRequest(request);
        
        //Get the channel runtime data from the request attributes, it should have been set there by the portlet adapter
        final ChannelRuntimeData channelRuntimeData = (ChannelRuntimeData)request.getAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA);
        if (channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData was found as a request attribute for key '" + IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA + "' on request '" + request + "'");
        }
        
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final String channelSubscribeId = parentPortletEntity.getChannelSubscribeId();

        //Get the encoding to use for the URL
        final String encoding = this.getEncoding(request);
        
        //Get the string version of the portlet ID (local variable to avoid needless getStringId() calls)
        final String portletWindowIdString = portletWindowId.getStringId();
        
        // TODO Need to decide how to deal with 'secure' URL requests
        // Determine the base path for the URL
        // If the next state is EXCLUSIVE or there is no state change and the current state is EXCLUSIVE use the worker URL base
        final String urlBase;
        final WindowState windowState = portletUrl.getWindowState();
        final WindowState previousWindowState = portletWindow.getWindowState();
        if (IPortletAdaptor.EXCLUSIVE.equals(windowState) || (windowState == null && IPortletAdaptor.EXCLUSIVE.equals(previousWindowState))) {
            urlBase = channelRuntimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);

            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Using worker url base '" + urlBase + "'");
            }
        }
        else {
            urlBase = channelRuntimeData.getBaseActionURL();
            
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Using default url base '" + urlBase + "'");
            }
        }

        final StringBuilder url = new StringBuilder(this.bufferLength);
        final String contextPath = request.getContextPath();
        url.append(contextPath).append("/");
        url.append(urlBase);
        
        //Set the request target
        this.encodeAndAppend(url.append("?"), encoding, PARAM_REQUEST_TARGET, portletWindowIdString);
        
        //Set the request type
        final RequestType requestType = portletUrl.getRequestType();
        final String requestTypeString = requestType != null ? requestType.toString() : RequestType.RENDER.toString();
        this.encodeAndAppend(url.append("&"), encoding, PARAM_REQUEST_TYPE, requestTypeString);
        
        // If set add the window state
        if (windowState != null && !previousWindowState.equals(windowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE, windowState.toString());
            
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
        }
        
        //If set add the portlet mode
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE, portletMode.toString());
        }
        
        //Add the parameters to the URL
        final Map<String, String[]> parameters = portletUrl.getParameters();
        if (parameters != null) {
            for (final Map.Entry<String, String[]> parameterEntry : parameters.entrySet()) {
                final String name = parameterEntry.getKey();
                final String[] values = parameterEntry.getValue();

                this.encodeAndAppend(url.append("&"), encoding, PORTLET_PARAM_PREFIX + name, values);
            }
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated portlet URL '" + url + "' for IPortletWindow='" + portletWindow + "' and PortletUrl='" + portletUrl + "'. StringBuilder started with length " + this.bufferLength + " and ended with length " + url.capacity() + ".");
        }
        
        return url.toString();
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
     * Encodes parameter name and value(s) on to the url using the specified encoding. The option to pass more than one
     * value is provided to avoid encoding the same name multiple times.  
     * 
     * @param url The URL StringBuilder to append the parameters to
     * @param encoding The encoding to use.
     * @param name The name of the parameter
     * @param values The values for the parameter, a & will be appeneded between each name/value pair added when multiple values are passed.
     */
    protected void encodeAndAppend(StringBuilder url, String encoding, String name, String... values) {
        try {
            name = URLEncoder.encode(name, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode portlet URL parameter name '" + name + "' for encoding '" + encoding + "'");
        }
        
        if (values.length == 0) {
            url.append(name).append("=");
        }
        else {
            for (int index = 0; index < values.length; index++) {
                String value = values[index];
                
                if (value == null) {
                    value = "";
                }
                
                try {
                    value = URLEncoder.encode(value, encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encode portlet URL parameter value '" + value + "' for encoding '" + encoding + "'");
                }
                
                if (index > 0) {
                    url.append("&");
                }
                
                url.append(name).append("=").append(value);
            }
        }
    }
}
