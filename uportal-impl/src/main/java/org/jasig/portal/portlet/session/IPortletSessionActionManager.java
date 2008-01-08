/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.portlet.session;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletContainerException;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Provides access to a Portlet's session through distinct actions.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletSessionActionManager {

    /**
     * Like calling {@link #clear(IPortletWindow, HttpServletRequest, HttpServletResponse, int)} with {@link PortletSession#PORTLET_SCOPE}.
     */
    public void clear(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws PortletException, IOException, PortletContainerException;
    
    /**
     * Removes all {@link PortletSession} attributes for the specified {@link IPortletWindow} and scope.
     * 
     * @param portletWindow The window who's PortletSession will be used.
     * @param httpServletRequest The current request
     * @param httpServletResponse The current response
     * @param scope The scope for the action. {@link PortletSession#APPLICATION_SCOPE} or {@link PortletSession#PORTLET_SCOPE}
     */
    public void clear(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int scope) throws PortletException, IOException, PortletContainerException;
    
    /**
     * Like calling {@link #setAttribute(IPortletWindow, HttpServletRequest, HttpServletResponse, String, Object, int)} with {@link PortletSession#PORTLET_SCOPE}.
     */
    public void setAttribute(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name, Object value) throws PortletException, IOException, PortletContainerException;
    
    /**
     * @param portletWindow The window who's PortletSession will be used.
     * @param httpServletRequest The current request
     * @param httpServletResponse The current response
     * @param name The name of the session attribute
     * @param value The value of the session attribute
     * @param scope The scope for the action. {@link PortletSession#APPLICATION_SCOPE} or {@link PortletSession#PORTLET_SCOPE}
     */
    public void setAttribute(IPortletWindow portletWindow, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String name, Object value, int scope) throws PortletException, IOException, PortletContainerException;
}

