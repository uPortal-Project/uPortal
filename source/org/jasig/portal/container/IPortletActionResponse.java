/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

/**
 * Represents the wishes of a portlet to optionally 
 * change modes and/or window states.  Also captures
 * a portlet's desire to optionally redirect the user to another
 * location (URL).  Finally, captures any parameters that should
 * be sent as render parameters.
 * <p>
 * The purpose of this interface is to serve as an abstraction to
 * Pluto's <code>InternalActionResponse</code> to limit uPortal's
 * dependency on Pluto as a portlet container.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface IPortletActionResponse {

    /**
     * The key by which an <code>IPortletActionResponse</code> implementation
     * should be stored as a request attribute.
     */
    String REQUEST_ATTRIBUTE_KEY = "org.jasig.portal.container.IProcessActionResults";

    /**
     * Returns the portlet mode that a portlet has requested in its 
     * <code>processAction</code> method.
     * @return the requested portlet mode or <code>null</code>
     */
    PortletMode getChangedPortletMode();
    
    /**
     * Returns the portlet window state that a portlet has requested in its
     * <code>processAction</code> method.
     * @return the requested window state or <code>null</code>
     */
    WindowState getChangedWindowState();

    /**
     * Returns the redirect location that a portlet has requested in its
     * <code>processAction</code> method.
     * @return the requested redirect location or <code>null</code>
     */
    String getRedirectLocation();
    
    /**
     * Returns the render parameters that a portlet has set in its
     * <code>processAction</code> method.
     * @return the requested render parameters or <code>null</code>
     */
    Map getRenderParameters();
}
