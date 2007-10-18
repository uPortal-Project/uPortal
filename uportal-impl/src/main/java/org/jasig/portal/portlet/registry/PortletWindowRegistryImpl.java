/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.registry;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletWindow;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Provides the default implementation of the window registry
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletWindowRegistryImpl implements IPortletWindowRegistry {

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.registry.IPortletWindowRegistry#convertPortletWindow(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public IPortletWindow convertPortletWindow(HttpServletRequest request, PortletWindow portletWindow) {
        if (portletWindow instanceof IPortletWindow) {
            return (IPortletWindow)portletWindow;
        }
        
        throw new UnsupportedOperationException("Cannot convert '" + portletWindow.getClass() + "' to '" + IPortletWindow.class + "' at this time");
    }
}
