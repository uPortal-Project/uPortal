/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.List;
import java.util.Map;




/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ILayoutPortalUrl extends IBasePortalUrl {
    /**
     * @param renderInNormal If the URL will result in rendering in {@link UrlState#NORMAL}
     */
    public void setRenderInNormal(Boolean renderInNormal);
    
    /**
     * @return Returns true if the URL will render in {@link UrlState#NORMAL}
     */
    public Boolean isRenderInNormal();
    
    /**
     * @param action Set true if the request should be an action.
     */
    public void setAction(boolean action);
    
    /**
     * @return true if the URL is an action URL
     */
    public boolean isAction();
    
    /**
     * Sets a URL parameter targeted to the layout manager.
     * 
     * This method replaces all parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void setLayoutParameter(String name, String... values);
    
    /**
     * Adds a URL parameter targeted to the layout manager.
     * 
     * This method adds the provided parameters on to any existing parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void addLayoutParameter(String name, String... values);
    
    /**
     * Sets a layout manager parameter map for this URL.
     * 
     * All previously set portal parameters are cleared.
     * 
     * @param parameters Map containing parameters
     */
    public void setLayoutParameters(Map<String, List<String>> parameters);
    
    /**
     * Get the current layout manager parameters. The Map is mutable and making changes to the Map will affect the parameters on the URL.
     * 
     * @return Map containing currently set portal parameters.
     */
    public Map<String, List<String>> getLayoutParameters();
}
