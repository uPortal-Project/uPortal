/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalPortletUrl extends IBasePortalUrl {
    /**
     * Sets a URL parameter targeted to the portlet.
     * 
     * This method replaces all parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void setPortletParameter(String name, String... values);
    
    /**
     * Adds a URL parameter targeted to the portlet.
     * 
     * This method adds the provided parameters on to any existing parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void addPortletParameter(String name, String... values);
    
    /**
     * Sets a portlet parameter map for this URL.
     * 
     * All previously set portal parameters are cleared.
     * 
     * @param parameters Map containing parameters
     */
    public void setPortletParameters(Map<String, List<String>> parameters);
    
    /**
     * Get the current portlet parameters. The Map is mutable and making changes to the Map will affect the portlet parameters on the URL.
     * 
     * @return Map containing currently set portal parameters.
     */
    public Map<String, List<String>> getPortletParameters();
    
    /**
     * @param windowState The WindowState to render the portlet in
     */
    public void setWindowState(WindowState windowState);
    
    /**
     * @return The currently set WindowState for the URL, will return null if {@link #setWindowState(WindowState)} has not be called
     */
    public WindowState getWindowState();
    
    /**
     * @param portletMode The PortletMode to render the portlet in
     */
    public void setPortletMode(PortletMode portletMode);
    
    /**
     * @return The currently set PortletMode for the URL, will return null if {@link #setPortletMode(PortletMode)} has not be called
     */
    public PortletMode getPortletMode();
    
    /**
     * @param action Set true if the request should be an action.
     */
    public void setAction(boolean action);
    
    /**
     * @return true if the URL is an action URL
     */
    public boolean isAction();
}
