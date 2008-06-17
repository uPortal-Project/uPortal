/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Encapsulates logic related to handling properties that can be retrieved from and
 * set on {@link javax.portlet.PortletRequest} and {@link javax.portlet.PortletResponse}
 * objects.
 * 
 * get
 * -add select servlet request properties to map
 * 
 * add/set
 * -react to setting cache timeout
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IRequestPropertiesManager {

    /**
     * Called when a portlet sets a property via {@link javax.portlet.PortletResponse#setProperty(String, String)}. This
     * method should follow the same rules as the PortletResponse method and overwrite any existing property of the same
     * name.
     * 
     * @param portletRequest The request the setProperty call was made during
     * @param portletWindow The PortletWindow representing the portlet calling setProperty
     * @param property The name of the property to set, will not be null.
     * @param value The value of the property to set, may be null.
     */
    public void setResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value);
    
    /**
     * Called when a portlet sets a property via {@link javax.portlet.PortletResponse#addProperty(String, String)}. This
     * method should follow the same rules as the PortletResponse method and add the value to any existing property of
     * the same name.
     * 
     * @param portletRequest The request the addProperty call was made during
     * @param portletWindow The PortletWindow representing the portlet calling addProperty
     * @param property The name of the property to add, will not be null.
     * @param value The value of the property to add, may be null.
     */
    public void addResponseProperty(HttpServletRequest portletRequest, IPortletWindow portletWindow, String property, String value);

    /**
     * Called when a portlet gets the request properties via {@link javax.portlet.PortletRequest#getProperties(String)},
     * {@link javax.portlet.PortletRequest#getProperty(String)}, or {@link javax.portlet.PortletRequest#getPropertyNames()}.
     * 
     * @param portletRequest The request the call was made during
     * @param portletWindow The PortletWindow representing the portlet requesting properties.
     * @return A Map of properties to present to the portlet, must not be null.
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest portletRequest, IPortletWindow portletWindow);
}
