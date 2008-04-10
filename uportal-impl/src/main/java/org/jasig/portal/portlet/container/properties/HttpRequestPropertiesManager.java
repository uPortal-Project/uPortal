/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides some extra information from the {@link HttpServletRequest} to the portlet as properties.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HttpRequestPropertiesManager extends BaseRequestPropertiesManager {
    private IPortalRequestUtils portalRequestUtils;
    
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest portletRequest, IPortletWindow portletWindow) {
        final HttpServletRequest httpServletRequest = this.portalRequestUtils.getOriginalPortletAdaptorRequest(portletRequest);
        
        final Map<String, String[]> properties = new HashMap<String, String[]>();

        properties.put("REMOTE_ADDR", new String[] { httpServletRequest.getRemoteAddr() });
        properties.put("REQUEST_METHOD", new String[] { httpServletRequest.getMethod() });

        return properties;
    }
}
