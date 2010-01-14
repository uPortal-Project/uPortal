/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
