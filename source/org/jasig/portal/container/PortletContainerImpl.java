/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.core.InternalActionResponse;
import org.apache.pluto.om.window.PortletWindow;
import org.jasig.portal.container.om.window.PortletWindowImpl;

/**
 * Same as Pluto's PortletContainerImpl, but this one ignores redirects.
 * In uPortal, we can't let channels and portlets request redirects because the
 * state of the response object is not guaranteed.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletContainerImpl extends org.apache.pluto.PortletContainerImpl {
    
    protected void redirect(String location, 
                            PortletWindow portletWindow, 
                            HttpServletRequest servletRequest, 
                            HttpServletResponse servletResponse, 
                            InternalActionResponse _actionResponse) 
    throws IOException {

        // If a redirect location is available, request a redirect.
        if (location != null) {
            servletResponse.sendRedirect(location);
            return;
        }

        // Make the results of the portlet's processAction call available as a 
        // request attribute so that they are available during the rendering process.
        IPortletActionResponse par = new PortletActionResponseImpl(_actionResponse);
        servletRequest.setAttribute(PortletActionResponseImpl.REQUEST_ATTRIBUTE_KEY, par);
        
        // Rather than redirect, we shall capture the portlet action response
        // and make it available to the portlet adapter via the portlet window.
        ((PortletWindowImpl)portletWindow).setPortletActionResponse(par);       
    }

}
