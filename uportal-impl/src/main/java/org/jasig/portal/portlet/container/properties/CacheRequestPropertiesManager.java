/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.Map;

import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Deals with seting and retrieving the {@link RenderResponse#EXPIRATION_CACHE} property.
 * 
 * TODO this needs to be implemented to hook into the adapter's caching APIs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheRequestPropertiesManager extends BaseRequestPropertiesManager {

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.BaseRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    @Override
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        this.logger.warn("EXPIRATION_CACHE REQUEST PROPERTY NOT IMPLEMENTED: Providing a '" + RenderResponse.EXPIRATION_CACHE + "' value of 0", new Throwable());
        return Collections.singletonMap(RenderResponse.EXPIRATION_CACHE, new String[] { "0" });
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.BaseRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    @Override
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        if (RenderResponse.EXPIRATION_CACHE.equals(property)) {
            final UnsupportedOperationException uoe = new UnsupportedOperationException("This functionality is not yet implemented in uPortal");
            this.logger.warn("EXPIRATION_CACHE REQUEST PROPERTY NOT IMPLEMENTED: Ignoring '" + RenderResponse.EXPIRATION_CACHE + "' request", uoe);
            throw uoe;
        }
    }

}
