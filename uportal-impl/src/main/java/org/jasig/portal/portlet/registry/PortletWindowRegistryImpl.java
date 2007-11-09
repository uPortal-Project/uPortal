/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.pluto.PortletWindow;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.om.PortletWindowIdImpl;

/**
 * Provides the default implementation of the window registry
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowRegistryImpl implements IPortletWindowRegistry {
    private static final String PORTLET_WINDOW_MAP_ATTRIBUTE = PortletWindowRegistryImpl.class.getName() + ".PORTLET_WINDOW_MAP";
    

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#convertPortletWindow(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow portletWindow) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        
        if (portletWindow instanceof IPortletWindow) {
            return (IPortletWindow)portletWindow;
        }
        
        throw new UnsupportedOperationException("Cannot convert '" + portletWindow.getClass() + "' to '" + IPortletWindow.class + "' at this time");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindow(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindowId)
     */
    public IPortletWindow getPortletWindow(HttpServletRequest request, IPortletWindowId portletWindowId) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        final Map<IPortletWindowId, IPortletWindow> portletWindowMap = this.getPortletWindowMap(request);
        if (portletWindowMap == null) {
            return null;
        }
        
        return portletWindowMap.get(portletWindowId);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#getPortletWindowId(java.lang.String)
     */
    public IPortletWindowId getPortletWindowId(String portletWindowId) {
        return new PortletWindowIdImpl(portletWindowId);
    }
    
    /**
     * Get the Map of IPortletWindows for the request.
     * 
     * @param request the current request
     * @return The Map of IPortletWindows managed by this class for the request, null if that Map does not yet exist.
     */
    protected Map<IPortletWindowId, IPortletWindow> getPortletWindowMap(HttpServletRequest request) {
        final HttpSession session = this.getSession(request);
        
        return (Map<IPortletWindowId, IPortletWindow>)session.getAttribute(PORTLET_WINDOW_MAP_ATTRIBUTE);
    }

    /**
     * Gets the session for the request.
     * 
     * @param request The current request
     * @return The session for the current request, will not return null.
     */
    protected HttpSession getSession(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("A HttpSession must already exist for the PortletWindowRegistryImpl to function");
        }
        return session;
    }
}
