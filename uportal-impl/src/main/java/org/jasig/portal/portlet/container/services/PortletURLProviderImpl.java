/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.spi.PortletURLProvider;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.portlet.CPortletAdaptor;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * TODO seperate PortletURL type class (represents the URL state) and the encoder/parser
 * TODO review URL code from sandbox to try and mimic APIs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {
    private static final PortletMode EXCLUSIVE = new PortletMode("EXCLUSIVE");
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final IPortletWindow portletWindow;
    private final HttpServletRequest httpServletRequest;
    
    private final Map<String, String[]> parameters = new HashMap<String, String[]>();
    private PortletMode portletMode;
    private WindowState windowState;
    private boolean isAction = false;
    
    public PortletURLProviderImpl(IPortletWindow portletWindow, HttpServletRequest httpServletRequest) {
        this.portletWindow = portletWindow;
        this.httpServletRequest = httpServletRequest;
        
        this.portletMode = this.portletWindow.getPortletMode();
        this.windowState = this.portletWindow.getWindowState();
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.parameters.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#isSecureSupported()
     */
    public boolean isSecureSupported() {
        return false; //TODO determine how to tie back in to the uPortal secure URL APIs, if they exist.
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setSecure()
     */
    public void setSecure() throws PortletSecurityException {
        throw new PortletSecurityException("Secure URLs are not supported at this time");
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setAction(boolean)
     */
    public void setAction(boolean action) {
        this.isAction = action;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setParameters(java.util.Map)
     * @param parmeters is Map<String, String[]>
     */
    @SuppressWarnings("unchecked")
    public void setParameters(Map parameters) {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        if (!this.portletWindow.getPortletMode().equals(mode)) {
            this.portletMode = mode;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        if (!this.portletWindow.getWindowState().equals(state)) {
            this.windowState = state;
        }
    }

    private static final String UP_PARAM_PREFIX = "uP_";

    // The portlet control parameter names
    private static final String ACTION =             UP_PARAM_PREFIX + "portlet_action";
    private static final String UP_ROOT =            UP_PARAM_PREFIX + "root";
    private static final String UP_TCATTR =          UP_PARAM_PREFIX + "tcattr";
    private static final String UP_HELP_TARGET =     UP_PARAM_PREFIX + "help_target";
    private static final String UP_EDIT_TARGET =     UP_PARAM_PREFIX + "edit_target";
    private static final String UP_VIEW_TARGET =     UP_PARAM_PREFIX + "view_target";
    private static final String MIN_CHAN_ID =        "minimized_channelId";

    private static final String MINIMIZED = "minimized";
    private static final String ROOT =      IUserLayout.ROOT_NODE_NAME;
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        /*
         * uPortal URLs are automaticly targeted at the channel by chanid
         * since portlet windows are a step beyond chanids ... this is going to be interesting
         * 
         * does the PortletWindow get a different ID than the channel?
         * deployment - contextName.portletName - from file system
         * definition - chanid - from UP_CHANEL tabel
         * entity - subscribe id - from layout
         * window - generated? is it possible to have a window without an entity?
         * 
         * /portal/
         * tag.8c85c816775c39d9.render.userLayoutRootNode.target.u19l1n756.uP
         * ?uP_portlet_action=true
         * &action=toggleFolder
         * &folderIndex=27707.27708.27815.27835
         * #u19l1n756
         * 
         * /portal/
         * tag.82ee3319ca18d6b4.render.userLayoutRootNode.target.u19l1n756.uP
         * ?uP_portlet_action=false
         * &uP_view_target=u19l1n756
         * &action=viewBookmarks
         * #u19l1n756
         * 
         * options
         * 1. state & mode are derived from channel events & layout state directly by the portlet adapter
         * 2. the layout state is derived from the portlet's window object
         * 
         * with #1
         * -portlet URL parsing would have to take place early on in the pipeline
         * --create new channel interface / url flag for this?!?
         * -portlet URL parser would know how to communicate portlet state changes to layout manager
         */
        
        final ChannelRuntimeData channelRuntimeData = (ChannelRuntimeData)this.httpServletRequest.getAttribute(CPortletAdaptor.RUNTIME_DATA);
        final IPortletWindowId portletWindowId = this.portletWindow.getPortletWindowId();
        final String portletWindowIdStr = portletWindowId.toString();
        
        final StringBuilder url = new StringBuilder(1024);
        
        // Start with the absolute servlet context
        url.append(this.httpServletRequest.getContextPath()).append("/");

        // Add the uPortal base path
        // If the next state is EXCLUSIVE or there is no state change and the current state is EXCLUSIVE use the worker URL base
        final String urlBase;
        if (EXCLUSIVE.equals(this.windowState) || (this.windowState == null && EXCLUSIVE.equals(this.portletWindow.getWindowState()))) {
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
        url.append(urlBase);
        
        // There are always parameters, add the seperator
        url.append("?");
        
        // Add the action flag
        this.encodeAndAppend(url, ACTION, Boolean.toString(this.isAction));
        
        // If set provide the new window state information to the layout manager
        if (this.windowState != null) {
            //Switching to MINIMIZED, Goal is not focused and minimized
            if (WindowState.MINIMIZED.equals(this.windowState)) {
                this.logger.trace("Adding minimize URL parameters");
                
                this.encodeAndAppend(url.append("&"), UP_TCATTR, MINIMIZED);
                this.encodeAndAppend(url.append("&"), MIN_CHAN_ID, portletWindowIdStr);
                this.encodeAndAppend(url.append("&"), MINIMIZED + "_" + portletWindowIdStr + "_value", Boolean.TRUE.toString());
            }
            //Switching to NORMAL, Goal is not focused and not minimized
            else if (WindowState.NORMAL.equals(this.windowState)) {
                this.logger.trace("Adding normal URL parameters");
                
                this.encodeAndAppend(url.append("&"), UP_ROOT, ROOT);
            }
            //Switching to MAXIMIZED, Goal is focused and not minimized
            else if (WindowState.MAXIMIZED.equals(this.windowState)) {
                this.logger.trace("Adding maximized URL parameters");
                
                this.encodeAndAppend(url.append("&"), UP_ROOT, portletWindowIdStr);
            }

            //If our last state was minimized un-minimize it
            if (WindowState.MINIMIZED.equals(this.portletWindow.getWindowState()) && !EXCLUSIVE.equals(this.windowState)) {
                this.logger.trace("Adding minimized to normal switch URL parameters");
                
                this.encodeAndAppend(url.append("&"), UP_TCATTR, MINIMIZED);
                this.encodeAndAppend(url.append("&"), MIN_CHAN_ID, portletWindowIdStr);
                this.encodeAndAppend(url.append("&"), MINIMIZED + "_" + portletWindowIdStr + "_value", Boolean.FALSE.toString());
            }
        }
        
        
        return url.toString();
    }
    
    protected void encodeAndAppend(StringBuilder url, String name, String value) {
        String encoding = this.httpServletRequest.getCharacterEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        
        try {
            name = URLEncoder.encode(name, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode portlet URL parameter name '" + name + "' for encoding '" + encoding + "'");
        }
        
        try {
            value = URLEncoder.encode(value, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode portlet URL parameter name '" + value + "' for encoding '" + encoding + "'");
        }
        
        url.append(name).append("=").append(value);
    }
}
