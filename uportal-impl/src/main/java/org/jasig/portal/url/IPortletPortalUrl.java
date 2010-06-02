/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.Map;

import org.apache.pluto.container.PortletURLProvider;

/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletPortalUrl extends IBasePortalUrl, PortletURLProvider {
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
     * Sets a portlet parameter map for this URL.
     * 
     * All previously set portal parameters are cleared.
     * 
     * @param parameters Map containing parameters
     */
    public void setPortletParameters(Map<String, String[]> parameters);

    /**
     * Adds the specified parameter values to the existing portlet parameter, or creates it new if it doesn't
     * already exist.
     */
    public void addPortletParameter(String name, String... values);
    
    /**
     * Get the current portlet parameters. The Map is mutable and making changes to the Map will affect the portlet parameters on the URL.
     * 
     * @return Map containing currently set portal parameters.
     */
    public Map<String, String[]> getPortletParameters();
    
    /**
     * @see #getPortletParameters()
     */
    public Map<String, String[]> getRenderParameters();
}
