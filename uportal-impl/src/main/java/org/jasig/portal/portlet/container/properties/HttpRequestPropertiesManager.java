/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Provides some extra information from the {@link HttpServletRequest} to the portlet as properties.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HttpRequestPropertiesManager extends BaseRequestPropertiesManager {

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        final Map<String, String[]> properties = new HashMap<String, String[]>();

        properties.put("REMOTE_ADDR", new String[] { request.getRemoteAddr() });
        properties.put("REQUEST_METHOD", new String[] { request.getMethod() });

        return properties;
    }
}
