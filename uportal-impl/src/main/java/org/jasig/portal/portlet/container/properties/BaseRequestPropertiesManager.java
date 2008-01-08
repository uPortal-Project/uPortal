/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Do nothing base class for IRequestPropertiesManager impls. {@link #getRequestProperties(HttpServletRequest, IPortletWindow)}
 * returns {@link Collections#emptyList()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseRequestPropertiesManager implements IRequestPropertiesManager {
    protected final Log logger = LogFactory.getLog(this.getClass());

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#addResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        //noop
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        //noop
    }
}
