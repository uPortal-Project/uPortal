/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.spi.optional.PortletRegistryService;
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
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.xml.sax.ContentHandler;

/**
 * Call paths
 * 
 * init
 *  setStaticData
 * 
 * render
 *  setPortalControlStructures
 *  setRuntimeData
 *  generateKey
 *  isCacheValid
 *  renderCharacters - final call? Cleanup?
 *  
 * action - TODO implement this
 *  setPortalControlStructures
 *  setRuntimeData
 *  doAction - final call? Cleanup?
 *  
 * event
 *  setPortalControlStructures - TODO add this call
 *  receiveEvent - final call? Cleanup?
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CPortletAdaptor implements IPortletAdaptor, ICharacterChannel, IPrivilegedChannel, ICacheable {
    private IPortletWindowRegistry portletWindowRegistry;
    private OptionalContainerServices optionalContainerServices;
    private PortletContainer portletContainer;

    private ChannelStaticData channelStaticData;
    private PortalControlStructures portalControlStructures;
    private ChannelRuntimeData channelRuntimeData;

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#getRuntimeProperties()
     */
    public ChannelRuntimeProperties getRuntimeProperties() {
        // TODO provide title from PortalCallbackService#setTitle calls
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent ev) {
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("PortalControlStructures must be set before setRuntimeData is called");
        }

        switch (ev.getEventNumber()) {
            case PortalEvent.SESSION_DONE: {
                final IPortletWindowId portletWindowId = null; //TODO how to figure out window ID(s)

                final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
                final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
                
                final HttpServletResponse httpServletResponse = this.portalControlStructures.getHttpServletResponse();
                
                //TODO setup request to denote this request is meant to invalidate the portlet's session
                
                try {
                    this.portletContainer.doAdmin(portletWindow, httpServletRequest, httpServletResponse);
                }
                catch (PortletException pe) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(pe);
                }
                catch (IOException ioe) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(ioe);
                }
                catch (PortletContainerException pce) {
                    // TODO Auto-generated catch block
                    throw new RuntimeException(pce);
                }
            }
            break;
            
            case PortalEvent.UNSUBSCRIBE: {
                //TODO clean up entity, window & associated objects
            }
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#renderXML(org.xml.sax.ContentHandler)
     */
    public void renderXML(ContentHandler out) throws PortalException {
        throw new UnsupportedOperationException("CPortletAdapter only renders character data");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.channelRuntimeData = rd;

        if (this.portalControlStructures == null) {
            throw new IllegalStateException("PortalControlStructures must be set before setRuntimeData is called");
        }

        //Attach the runtime data as an attribute on the request so it is accessible to other portlet rendering related classes  
        final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
        httpServletRequest.setAttribute(ATTRIBUTE_RUNTIME_DATA, this.channelRuntimeData);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)
     */
    public void setStaticData(ChannelStaticData sd) throws PortalException {
        this.channelStaticData = sd;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        this.portalControlStructures = pcs;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICharacterChannel#renderCharacters(java.io.PrintWriter)
     */
    public void renderCharacters(PrintWriter pw) throws PortalException {
        if (this.portalControlStructures == null) {
            throw new IllegalStateException("PortalControlStructures must be set before renderCharacters is called");
        }
        if (this.channelRuntimeData == null) {
            throw new IllegalStateException("ChannelRuntimeData must be set before setRuntimeData is called");
        }
        
        final IPortletWindowId portletWindowId = null; //TODO how to figure out window ID 

        final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);

        final HttpServletResponse httpServletResponse = this.portalControlStructures.getHttpServletResponse();
        final ContentRedirectingHttpServletResponse httpServletResponseWrapper = new ContentRedirectingHttpServletResponse(httpServletResponse, pw);

        try {
            this.portletContainer.doRender(portletWindow, httpServletRequest, httpServletResponseWrapper);
            httpServletResponseWrapper.flushBuffer();
        }
        catch (PortletException pe) {
            // TODO Auto-generated catch block
            throw new RuntimeException(pe);
        }
        catch (IOException ioe) {
            // TODO Auto-generated catch block
            throw new RuntimeException(ioe);
        }
        catch (PortletContainerException pce) {
            // TODO Auto-generated catch block
            throw new RuntimeException(pce);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey() {
        final long currentTimeMillis = System.currentTimeMillis();
        final String currentTime = Long.toString(currentTimeMillis);
        
        final ChannelCacheKey channelCacheKey = new ChannelCacheKey();
        channelCacheKey.setKeyScope(ChannelCacheKey.INSTANCE_KEY_SCOPE);
        channelCacheKey.setKey(currentTime);
        channelCacheKey.setKeyValidity(currentTime);
        
        return channelCacheKey;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity) {
        final IPortletWindowId portletWindowId = this.getTargetedPortletWindowId();
        
        final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
        final IPortletWindow portletWindow = this.portletWindowRegistry.getPortletWindow(httpServletRequest, portletWindowId);
        
        final PortletRegistryService portletRegistryService = this.optionalContainerServices.getPortletRegistryService();
        
        final String contextPath = portletWindow.getContextPath();
        final String portletName = portletWindow.getPortletName();
        final PortletDD portletDescriptor;
        try {
            portletDescriptor = portletRegistryService.getPortletDescriptor(contextPath, portletName);
        }
        catch (PortletContainerException pce) {
            // TODO Auto-generated catch block
            throw new RuntimeException(pce);
        }
        
        // TODO Auto-generated method stub
        return false;
    }
    
    protected IPortletWindowId getTargetedPortletWindowId() {
        //TODO figure out how to determine the targeted portlet window ID
        return null;
    }
}
