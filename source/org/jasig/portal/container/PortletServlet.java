/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container;

import java.io.IOException;

import javax.portlet.PortletSession;
import javax.portlet.PortletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Portlet Wraper servlet
 * Allow the portal container to monitor PortletSession object by adding
 * the portletSession as a request attribute
 * @author Stéphane Bond
 */
public class PortletServlet extends org.apache.pluto.core.PortletServlet {

    private static final long serialVersionUID = 1L;

    public final static String SESSION_MONITOR_ATTRIBUTE = "org.jasig.portal.container.PORTLET_SESSION_MONITOR";

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.service(request, response);
        PortletRequest portletRequest = (PortletRequest)request.getAttribute(org.apache.pluto.Constants.PORTLET_REQUEST);
        if (portletRequest != null) {
            PortletSession portletSession = portletRequest.getPortletSession(false);
            if (portletSession != null) {
                request.setAttribute(SESSION_MONITOR_ATTRIBUTE, portletSession);
            }
        }
    }

}
