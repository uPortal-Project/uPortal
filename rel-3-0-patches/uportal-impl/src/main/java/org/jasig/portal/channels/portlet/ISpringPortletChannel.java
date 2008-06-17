/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;

/**
 * Defines a static version of a IChannel that also implements  IPortletAdaptor, ICharacterChannel
 * and IPrivilegedChannel, ICacheable.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ISpringPortletChannel {
    /**
     * Called when a session is created for a user of the channel described by the ChannelStaticData argument. 
     * 
     * @param channelStaticData The data describing the channel the user session was created for.
     * @param portalControlStructures Information about the current request/response.
     */
    public void initSession(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures);
    
    /**
     * Called before {@link #isCacheValid(ChannelStaticData, PortalControlStructures, ChannelRuntimeData, Object)} and
     * {@link #render(ChannelStaticData, PortalControlStructures, ChannelRuntimeData, PrintWriter)} during the render
     * cycle. Responsible for generating a unique key that represents the state of the rendered channel for caching. 
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request. 
     * @return A unique key that represents the state of the portlet for the method parameters.
     */
    public ChannelCacheKey generateKey(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
    
    /**
     * Called after {@link #generateKey(ChannelStaticData, PortalControlStructures, ChannelRuntimeData)} and before
     * {@link #render(ChannelStaticData, PortalControlStructures, ChannelRuntimeData, PrintWriter)} during the render
     * cycle. The validity object {@link ChannelCacheKey#getKeyValidity()} from the previous rendering is passed in
     * to provide a method for this channel to check if it is still valid. 
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     * @param validity The validity object from the previous rendering, used to check if the cached state is still valid.
     * @return <code>true</code> if the cache is still valid, <code>false</code> if not.
     */
    public boolean isCacheValid(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, Object validity);
    
    /**
     * Called after {@link #generateKey(ChannelStaticData, PortalControlStructures, ChannelRuntimeData)} and
     * {@link #render(ChannelStaticData, PortalControlStructures, ChannelRuntimeData, PrintWriter)}. The channel should
     * render it's output to the provided PrintWriter and <b>NOT</b> to the response provided by  {@link PortalControlStructures#getHttpServletResponse()}.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     * @param printWriter The PrintWriter to write the output of the channel to.
     */
    public void render(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, PrintWriter printWriter);
    
    /**
     * Called after {@link #render(ChannelStaticData, PortalControlStructures, ChannelRuntimeData, PrintWriter)}, provides
     * the title to display for the channel for this rendering.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     * @return The title to display for the channel for this rendering, if null is returned the configured title is used.
     */
    public String getTitle(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
    
    /**
     * Handles an action request to the channel. No content is rendered and no other channel will process an action during
     * this request.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     */
    public void action(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);

    /**
     * Notification of a portal event.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response, not all events are request driven so the control structures may only be partially populated
     * @param portalEvent The portal event.
     */
    public void portalEvent(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, PortalEvent portalEvent);
    
    /**
     * Notification that the channel will be refreshed due to an error. Clean up information about
     * the last request and ensure the channel is in a renderable state
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     */
    public void prepareForRefresh(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
    
    /**
     * Notification that the channel will be reset due to an error. Clean up information about
     * the last request and all state information.
     * 
     * @param channelStaticData The static description data for the channel.
     * @param portalControlStructures Information about the current request/response.
     * @param channelRuntimeData Portal provided information for the current request.
     */
    public void prepareForReset(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
}