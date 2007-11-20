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
    public void initSession(ChannelStaticData channelStaticData);
    
    public ChannelCacheKey generateKey(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
    
    public boolean isCacheValid(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, Object validity);
    
    public void render(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData, PrintWriter printWriter);
    
    public String getTitle(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);
    
    public void action(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData);

    public void portalEvent(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, PortalEvent portalEvent);
    
    public void destroySession(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures);
}