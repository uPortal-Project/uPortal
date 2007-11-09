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
 * uPortal extensions to the Pluto {@link PortletWindow} interface.
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
    
}
