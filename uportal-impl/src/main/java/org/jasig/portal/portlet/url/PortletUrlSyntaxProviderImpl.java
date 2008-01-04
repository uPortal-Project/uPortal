/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
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
    private static final String PORTLET_PARAM_PREFIX = "plt" + SEPERATOR;

    private static final String PARAM_REQUEST_TYPE = PORTLET_PARAM_PREFIX + "type" + SEPERATOR;
    private static final String PARAM_WINDOW_STATE = PORTLET_PARAM_PREFIX + "state" + SEPERATOR;
    private static final String PARAM_PORTLET_MODE = PORTLET_PARAM_PREFIX + "mode" + SEPERATOR;
    
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
    public Map<IPortletWindowId, PortletUrl> parsePortletParameters(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        /*
         * TODO change this to return a Tuple<id, url>
         * don't prefix portlet parameters
         * 
         * plt_target=foo
         * plt_type=ACTION
         * plt_mode=EDIT
         * plt_state=NORMAL
         * paramName=paramVal
         */
        
        final Map<IPortletWindowId, PortletUrl> parsedUrls = new HashMap<IPortletWindowId, PortletUrl>();
        
        //Iterate through all request parameters
        final Map<String, String[]> requestParameters = request.getParameterMap();
        for (final Map.Entry<String, String[]> parameterEntry : requestParameters.entrySet()) {
            final String parameterName = parameterEntry.getKey();
            
            //Check each parameter that starts with the portlet prefix
            if (parameterName.startsWith(PORTLET_PARAM_PREFIX)) {
                final Tuple<String, String> parsedParameterName = this.parseParameterName(parameterName);
                if (parsedParameterName == null) {
                    //The parameter name wasn't valid, this should have already been logged so just skip it.
                    continue;
                }
                
                final String[] values = parameterEntry.getValue();
                
                if (parameterName.startsWith(PARAM_REQUEST_TYPE)) {
                    final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(parsedParameterName.second);
                    
                    //Log a warning if there is more than one value for the parameter
                    if (values.length > 1) {
                        this.logger.warn("Portlet meta parameter '" + parameterName + "' has " + values.length + " parameters. Only the first will be used, the others will be ignored. values=" + Arrays.toString(values));
                    }
                    
                    //Convert the value to the enum type
                    final RequestType requestType;
                    try {
                        requestType = RequestType.valueOf(values[0]);
                    }
                    catch (IllegalArgumentException iae) {
                        this.logger.warn("The value '" + values[0] + "' of portlet meta parameter '" + parameterName + "' could not be converted to a " + RequestType.class + "'. The parameter will be ignored.", iae);
                        continue;
                    }
                    
                    //Update the portlet URL for the window
                    final PortletUrl portletUrl = this.getPortletUrl(portletWindowId, parsedUrls);
                    portletUrl.setRequestType(requestType);
                }
                else if (parameterName.startsWith(PARAM_WINDOW_STATE)) {
                    final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(parsedParameterName.second);
                    
                    //Log a warning if there is more than one value for the parameter
                    if (values.length > 1) {
                        this.logger.warn("Portlet meta parameter '" + parameterName + "' has " + values.length + " parameters. Only the first will be used, the others will be ignored. values=" + Arrays.toString(values));
                    }
                    
                    //Convert the value to a WindowState
                    final WindowState windowState = new WindowState(values[0]);
                    //TODO validate the the WindowState is valid for uPortal
                    
                    //Update the portlet URL for the window
                    final PortletUrl portletUrl = this.getPortletUrl(portletWindowId, parsedUrls);
                    portletUrl.setWindowState(windowState);
                }
                else if (parameterName.startsWith(PARAM_PORTLET_MODE)) {
                    final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(parsedParameterName.second);
                    
                    //Log a warning if there is more than one value for the parameter
                    if (values.length > 1) {
                        this.logger.warn("Portlet meta parameter '" + parameterName + "' has " + values.length + " parameters. Only the first will be used, the others will be ignored. values=" + Arrays.toString(values));
                    }
                    
                    //Convert the value to a PortletMode
                    final PortletMode portletMode = new PortletMode(values[0]);
                    //TODO validate the the PortletMode is valid for uPortal
                    
                    //Update the portlet URL for the window
                    final PortletUrl portletUrl = this.getPortletUrl(portletWindowId, parsedUrls);
                    portletUrl.setPortletMode(portletMode);
                }
                //just a regular parameter
                else {
                    final IPortletWindowId portletWindowId = this.portletWindowRegistry.getPortletWindowId(parsedParameterName.first);
                    
                    final PortletUrl portletUrl = this.getPortletUrl(portletWindowId, parsedUrls);
                    
                    Map<String, String[]> portletParameters = portletUrl.getParameters();
                    if (portletParameters == null) {
                        portletParameters = new HashMap<String, String[]>();
                        portletUrl.setParameters(portletParameters);
                    }
                    
                    portletParameters.put(parsedParameterName.second, values);
                }
            }
        }
        
        //Prune PortletUrl objects that don't have a request type from the returned Map
        for (final Iterator<Entry<IPortletWindowId, PortletUrl>> parsedUrlEntryItr = parsedUrls.entrySet().iterator(); parsedUrlEntryItr.hasNext(); ) {
            final Entry<IPortletWindowId, PortletUrl> parsedUrlEntry = parsedUrlEntryItr.next();
            
            final PortletUrl portletUrl = parsedUrlEntry.getValue();
            if (portletUrl.getRequestType() == null) {
                this.logger.warn("Parameteres targeting IPortletWindowId='" + parsedUrlEntry.getKey() + "' will be ignored as no request type parameter was specified. Ignored data: " + portletUrl);
                parsedUrlEntryItr.remove();
            }
        }
        
        return parsedUrls;
    }
    
    /**
     * Gets/Creates PortletUrl to parse parameters into from the parsedUrls Map
     * 
     * @param portletWindowId ID to get ParsedUrl for
     * @param parsedUrls Map of existing ParsedUrl objects
     * @return The PortletUrl to parse parameters into for the IPortletWindowId
     */
    protected PortletUrl getPortletUrl(IPortletWindowId portletWindowId, Map<IPortletWindowId, PortletUrl> parsedUrls) {
        PortletUrl portletUrl = parsedUrls.get(portletWindowId);
        
        if (portletUrl == null) {
            portletUrl = new PortletUrl();
            parsedUrls.put(portletWindowId, portletUrl);
        }
        
        return portletUrl;
    }
    
    /**
     * Parses a parameter name into a Tuple that contains the second and third parts. The parsing
     * is done based on the {@link #SEPERATOR} string.
     * 
     * @param parameterName Name of parameter to parse
     * @return A Tuple with the second/third parse of the parameter name, null if the parameter name can not be parsed.
     */
    protected Tuple<String, String> parseParameterName(String parameterName) {
        final int firstIndex = parameterName.indexOf(SEPERATOR);
        if (firstIndex < 0) {
            this.logger.warn("Portlet parameter name '" + parameterName + "' cannot be parsed. The index of the first seperator character '" + SEPERATOR + "' returned " + firstIndex + ". The parameter will be ignored.");
            return null;
        }
        
        final int secondIndex = parameterName.indexOf(SEPERATOR, firstIndex + SEPERATOR.length());
        if (secondIndex < 0) {
            this.logger.warn("Portlet parameter name '" + parameterName + "' cannot be parsed. The index of the second seperator character '" + SEPERATOR + "' returned " + secondIndex + ". The parameter will be ignored.");
            return null;
        }
        
        final String firstPart = parameterName.substring(firstIndex + SEPERATOR.length(), secondIndex);
        final String secondPart = parameterName.substring(secondIndex + SEPERATOR.length());

        return new Tuple<String, String>(firstPart, secondPart);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(portletUrl, "portletUrl can not be null");
        
        //Get the channel runtime data from the request attributes, it should have been set there by the portlet adapter
        final ChannelRuntimeData channelRuntimeData = (ChannelRuntimeData)request.getAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA);
        if (channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData was found as a request attribute for key '" + IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA + "' on request '" + request + "'");
        }

        //Get the encoding to use for the URL
        final String encoding = this.getEncoding(request);
        
        //Get the string version of the portlet ID (local variable to avoid needless getStringId() calls)
        final String portletWindowIdString = portletWindow.getPortletWindowId().getStringId();
        
        // TODO Need to decide how to deal with 'secure' URL requests
        // Determine the base path for the URL
        // If the next state is EXCLUSIVE or there is no state change and the current state is EXCLUSIVE use the worker URL base
        final String urlBase;
        final WindowState windowState = portletUrl.getWindowState();
        if (IPortletAdaptor.EXCLUSIVE.equals(windowState) || (windowState == null && IPortletAdaptor.EXCLUSIVE.equals(portletWindow.getWindowState()))) {
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
        
        //Set the request type
        final RequestType requestType = portletUrl.getRequestType();
        final String requestTypeString = requestType != null ? requestType.toString() : RequestType.RENDER.toString();
        this.encodeAndAppend(url.append("?"), encoding, PARAM_REQUEST_TYPE + portletWindowIdString, requestTypeString);
        
        // If set add the window state
        if (windowState != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE + portletWindowIdString, windowState.toString());
        }
        
        //If set add the portlet mode
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE + portletWindowIdString, portletMode.toString());
        }
        
        //Add the parameters to the URL
        final Map<String, String[]> parameters = portletUrl.getParameters();
        if (parameters != null) {
            for (final Map.Entry<String, String[]> parameterEntry : parameters.entrySet()) {
                final String name = parameterEntry.getKey();
                final String[] values = parameterEntry.getValue();
                
                this.encodeAndAppend(url.append("&"), encoding, PORTLET_PARAM_PREFIX + portletWindowIdString + SEPERATOR + name, values);
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
