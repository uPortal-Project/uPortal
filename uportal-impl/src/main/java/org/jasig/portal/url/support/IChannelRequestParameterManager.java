/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.support;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Manages request parameters for channels for the duration of a HttpServletRequest.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IChannelRequestParameterManager {
    /**
     * Mark this request as not having any channel request parameters associated with it.
     * 
     * @param request The current request.
     * @throws IllegalArgumentException If request is null
     */
    public void setNoChannelParameters(HttpServletRequest request);
    
    /**
     * Get all channelIds that have had parameters associated with them for this request.
     * 
     * @param request The current request.
     * @return A Set of channel ids that have had parameters associated with this request.
     * @throws IllegalArgumentException If request is null
     */
    public Set<String> getTargetedChannelIds(HttpServletRequest request);
    
    /**
     * Associates the parameter Map with the specified channel ID for the request.
     * 
     * @param request The current request.
     * @param channelId The ID of the channel to store parameters for.
     * @param parameters The parameter map for the channel.
     * @throws IllegalArgumentException If request, channelId, or parameters are null
     */
    public void setChannelParameters(HttpServletRequest request, String channelId, Map<String, Object[]> parameters);
    
    
    /**
     * Gets the parameter Map for the specified channel Id for the request.
     * 
     * @param request The current request.
     * @param channelId The ID of the channel to get parameters for.
     * @return The parameter map for the channel id, null if the request did not target the channel.
     * @throws IllegalArgumentException If request, or channelId
     * @throws org.jasig.portal.url.processing.RequestParameterProcessingIncompleteException If this request doesn't have the nessesary information associated with it yet to return a parameter map.
     */
    public Map<String, Object[]> getChannelParameters(HttpServletRequest request, String channelId);
}
