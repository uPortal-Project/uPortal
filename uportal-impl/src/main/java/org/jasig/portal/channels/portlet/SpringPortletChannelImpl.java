/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class SpringPortletChannelImpl implements ISpringPortletChannel {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortletWindowRegistry portletWindowRegistry;
    private OptionalContainerServices optionalContainerServices;
    private PortletContainer portletContainer;
    
    
    
    
    //***** ISpringPortletChannel methods *****//

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#initSession(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures)
     */
    public void initSession(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures) {
        // TODO Auto-generated method stub
//        this.portletContainer.doLoad(portletWindow, servletRequest, servletResponse);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#action(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public void action(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#generateKey(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public ChannelCacheKey generateKey(ChannelStaticData channelStaticData,
            PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#getTitle(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData)
     */
    public String getTitle(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, ChannelRuntimeData channelRuntimeData) {
        final HttpServletRequest httpServletRequest = portalControlStructures.getHttpServletRequest();
        final String title = (String)httpServletRequest.getAttribute(IPortletAdaptor.ATTRIBUTE_PORTLET_TITLE);
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Retrieved title '" + title + "' from request for channel: pubId=" + channelStaticData.getChannelPublishId() + ", subId=" + channelStaticData.getChannelSubscribeId());
        }
        
        return title;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#isCacheValid(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData, java.lang.Object)
     */
    public boolean isCacheValid(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures,
            ChannelRuntimeData channelRuntimeData, Object validity) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#portalEvent(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.PortalEvent)
     */
    public void portalEvent(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures, PortalEvent portalEvent) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.channels.portlet.ISpringPortletChannel#render(org.jasig.portal.ChannelStaticData, org.jasig.portal.PortalControlStructures, org.jasig.portal.ChannelRuntimeData, java.io.PrintWriter)
     */
    public void render(ChannelStaticData channelStaticData, PortalControlStructures portalControlStructures,
            ChannelRuntimeData channelRuntimeData, PrintWriter printWriter) {
        // TODO Auto-generated method stub

    }
}
