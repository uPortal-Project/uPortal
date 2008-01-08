/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Manager that has a single backing Map of properties.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockRequestPropertiesManager implements IRequestPropertiesManager {
    private Map<String, String[]> properties = new HashMap<String, String[]>();
    
    /**
     * @return the properties
     */
    public Map<String, String[]> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String[]> properties) {
        this.properties = properties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.IRequestPropertiesManager#addResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        String[] values = this.properties.get(property);
        values = (String[])ArrayUtils.add(values, value);
        this.properties.put(property, values);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.IRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        return this.properties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.properties.IRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        this.properties.put(property, new String[] { value });
    }
}
