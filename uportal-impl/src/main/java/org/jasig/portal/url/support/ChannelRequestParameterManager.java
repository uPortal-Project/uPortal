/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException;

/**
 * Manages access to channel request parameters using a request attribute.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ChannelRequestParameterManager implements IChannelRequestParameterManager {
    protected static final String CHANNEL_PARAMETER_MAP_ATTRIBUTE = ChannelRequestParameterManager.class.getName() + ".CHANNEL_PARAMETER_MAP";
    protected static final Map<String, Map<String, Object[]>> NO_PARAMETERS = Collections.emptyMap();

    /* (non-Javadoc)
     * @see org.jasig.portal.url.support.IChannelRequestParameterManager#getChannelParameters(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public Map<String, Object[]> getChannelParameters(HttpServletRequest request, String channelId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(channelId, "channelId can not be null");
        
        final Map<String, Map<String, Object[]>> channelParamMaps = this.getAndCheckChannelParameterMaps(request);

        if (channelParamMaps == null) {
            return null;
        }

        return channelParamMaps.get(channelId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.support.IChannelRequestParameterManager#getTargetedChannelIds(javax.servlet.http.HttpServletRequest)
     */
    public Set<String> getTargetedChannelIds(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        final Map<String, Map<String, Object[]>> channelParamMaps = this.getAndCheckChannelParameterMaps(request);

        if (channelParamMaps == null) {
            return null;
        }

        return channelParamMaps.keySet();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.support.IChannelRequestParameterManager#setChannelParameters(javax.servlet.http.HttpServletRequest, java.lang.String, java.util.Map)
     */
    public void setChannelParameters(HttpServletRequest request, String channelId, Map<String, Object[]> parameters) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(channelId, "channelId can not be null");
        Validate.notNull(parameters, "parameters can not be null");
        
        Map<String, Map<String, Object[]>> channelParamMaps = this.getChannelParametersMap(request);

        if (channelParamMaps == NO_PARAMETERS) {
            throw new IllegalStateException("Cannot set channel parameters after setNoChannelParameters(HttpServletRequest) has been called.");
        }
        else if (channelParamMaps == null) {
            channelParamMaps = new HashMap<String, Map<String,Object[]>>();
        }

        channelParamMaps.put(channelId, parameters);

        request.setAttribute(CHANNEL_PARAMETER_MAP_ATTRIBUTE, channelParamMaps);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.support.IChannelRequestParameterManager#setNoChannelParameters(javax.servlet.http.HttpServletRequest)
     */
    public void setNoChannelParameters(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        final Map<String, Map<String, Object[]>> channelParamMaps = this.getChannelParametersMap(request);

        if (channelParamMaps != null) {
            throw new IllegalStateException("Cannot set no channel parameters after setChannelParameters(HttpServletRequest, String, Map) has been called.");
        }

        request.setAttribute(CHANNEL_PARAMETER_MAP_ATTRIBUTE, NO_PARAMETERS);
    }

    /**
     * Gets the Map of channel parameter Maps from the request, throws a {@link RequestParameterProcessingIncompleteException} if
     * no attribute exists in the request and returns null if the NO_PARAMETERS map has been set.
     * 
     * @param request Current request.
     * @return Map of channel ids to parameter Maps, null if {@link #NO_PARAMETERS} object is set.
     * @throws RequestParameterProcessingIncompleteException if no channel parameter processing has happened for the request yet.
     */
    protected Map<String, Map<String, Object[]>> getAndCheckChannelParameterMaps(HttpServletRequest request) {
        final Map<String, Map<String, Object[]>> channelParamMaps = this.getChannelParametersMap(request);
        
        if (channelParamMaps == null) {
            throw new RequestParameterProcessingIncompleteException("No channel parameter processing has been completed on this request");
        }
        //Do a reference equality check against no parameters Map
        else if (channelParamMaps == NO_PARAMETERS) {
            return null;
        }
        
        return channelParamMaps;
    }

    /**
     * Get the Map of channel parameter Maps from the request, hiding the generics casting warning.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, Object[]>> getChannelParametersMap(HttpServletRequest request) {
        return (Map<String, Map<String, Object[]>>)request.getAttribute(CHANNEL_PARAMETER_MAP_ATTRIBUTE);
    }
}
