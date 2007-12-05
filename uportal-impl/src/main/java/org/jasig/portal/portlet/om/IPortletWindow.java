/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import java.io.Serializable;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.PortletWindow;

/**
 * uPortal extensions to the Pluto {@link PortletWindow} interface. A portlet window
 * represents the actual rendering/interaction layer of the portlet object model.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletWindow extends PortletWindow, Serializable {
    /**
     * Retrieve this windows unique id which will be
     *  used to communicate back to the referencing portal.
     * @return unique id.
     */
    public IPortletWindowId getPortletWindowId();
    
    /**
     * @return The ID of the parent portlet entity.
     */
    public IPortletEntityId getPortletEntityId();
    
    /**
     * @param state The current {@link WindowState} of this PortletWindow
     * @throws IllegalArgumentException If state is null
     */
    public void setWindowState(WindowState state);
    
    /**
     * @param mode The current {@link PortletMode} of this PortletWindow
     * @throws IllegalArgumentException If mode is null
     */
    public void setPortletMode(PortletMode mode);
    
    /**
     * @param requestParameters The current request parameters for the portlet
     * @throws IllegalArgumentException if parameters is null.
     */
    public void setRequestParameters(Map<String, String[]> requestParameters);
    
    /**
     * @return The current request parameters for the portlet
     */
    public Map<String, String[]> getRequestParameers();
    
    /**
     * Sets the expiration timeout for the portlet rendering cache. If null is set
     * the timeout configured in the portlet.xml should be used.
     * 
     * @param expirationCache Set the cache expiration length for the portlet in seconds.
     */
    public void setExpirationCache(Integer expirationCache);

    /**
     * @return The expiration timeout for the portlet, if null the value from portlet.xml should be used.
     */
    public Integer getExpirationCache();
}
