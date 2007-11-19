/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.channels.support.TitledChannelRuntimeProperties;
import org.springframework.web.context.WebApplicationContext;
import org.xml.sax.ContentHandler;

/**
 * An attempt at a channel that can wrap a singleton spring-conifigured {@link ISpringPortletChannel} instance and
 * delegate execution to. This class simply must maintain the instance data and pass it on to the ISpringPortletChannel
 * for each method.
 * 
 * This class also attempts to eagerly clean up session & request scoped resources.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CSpringPortletAdaptor implements IPortletAdaptor, ICharacterChannel, IPrivilegedChannel, ICacheable {
    private ChannelStaticData channelStaticData;
    private ISpringPortletChannel springPortletChannel;
    
    private ChannelRuntimeData channelRuntimeData;
    private PortalControlStructures portalControlStructures;
    

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)
     */
    public void setStaticData(ChannelStaticData sd) throws PortalException {
        this.channelStaticData = sd;
        
        final String beanName = this.channelStaticData.getParameter("springBeanName");
        if (beanName == null) {
            throw new IllegalStateException("Channel Parameter 'springBeanName' is not set and is requried");
        }
        
        final WebApplicationContext applicationContext = null; //TODO this.channelStaticData.getWebApplicationContext();
        this.springPortletChannel = (ISpringPortletChannel)applicationContext.getBean(beanName, ISpringPortletChannel.class);
        
        
        this.springPortletChannel.init(this.channelStaticData);
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        this.portalControlStructures = pcs;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("setPortalControlStructures must be set before setRuntimeData is called");
        }
        
        this.channelRuntimeData = rd;

        //Attach the runtime data as an attribute on the request so it is accessible to other portlet rendering related classes  
        final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
        httpServletRequest.setAttribute(ATTRIBUTE_RUNTIME_DATA, this.channelRuntimeData);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey() {
        if (this.channelStaticData == null) {
            throw new IllegalStateException("No ChannelStaticData is associated with this IChannel, either the channel has not yet been initialized or should be destroyed.");
        }
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("No PortalControlStructures is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        if (this.channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        
        return this.springPortletChannel.generateKey(this.channelStaticData, this.portalControlStructures, this.channelRuntimeData);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity) {
        if (this.channelStaticData == null) {
            throw new IllegalStateException("No ChannelStaticData is associated with this IChannel, either the channel has not yet been initialized or should be destroyed.");
        }
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("No PortalControlStructures is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        if (this.channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        
        return this.springPortletChannel.isCacheValid(this.channelStaticData, this.portalControlStructures, this.channelRuntimeData, validity);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICharacterChannel#renderCharacters(java.io.PrintWriter)
     */
    public void renderCharacters(PrintWriter pw) throws PortalException {
        if (this.channelStaticData == null) {
            throw new IllegalStateException("No ChannelStaticData is associated with this IChannel, either the channel has not yet been initialized or should be destroyed.");
        }
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("No PortalControlStructures is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        if (this.channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData is associated with this IChannel, either no valid request has started or the request is complete.");
        }
        
        this.springPortletChannel.render(this.channelStaticData, this.portalControlStructures, this.channelRuntimeData, pw);
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent ev) {
        try {
            if (this.channelStaticData == null) {
                throw new IllegalStateException("No ChannelStaticData is associated with this IChannel, either the channel has not yet been initialized or should be destroyed.");
            }
            if (this.portalControlStructures == null) {
                throw new IllegalStateException("No PortalControlStructures is associated with this IChannel, either no valid request has started or the request is complete.");
            }
            
            this.springPortletChannel.portalEvent(this.channelStaticData, this.portalControlStructures, ev);
        }
        finally {
            this.channelRuntimeData = null;
            this.portalControlStructures = null;
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#getRuntimeProperties()
     */
    public ChannelRuntimeProperties getRuntimeProperties() {
        try {
            if (this.channelStaticData == null) {
                throw new IllegalStateException("No ChannelStaticData is associated with this IChannel, either the channel has not yet been initialized or should be destroyed.");
            }
            if (this.portalControlStructures == null) {
                throw new IllegalStateException("No PortalControlStructures is associated with this IChannel, either no valid request has started or the request is complete.");
            }
            if (this.channelRuntimeData == null) {
                throw new IllegalStateException("No ChannelRuntimeData is associated with this IChannel, either no valid request has started or the request is complete.");
            }
            
            final String title = this.springPortletChannel.getTitle(this.channelStaticData, this.portalControlStructures, this.channelRuntimeData);
            return new TitledChannelRuntimeProperties(title);
        }
        finally {
            this.channelRuntimeData = null;
            this.portalControlStructures = null;
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#renderXML(org.xml.sax.ContentHandler)
     */
    public void renderXML(ContentHandler out) throws PortalException {
        throw new UnsupportedOperationException("renderXML is not valid to call for ICharacterChannel");
    }
}
