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




/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ILayoutPortalUrl extends IBasePortalUrl {
    
    /**
     * @return the layout folder id this URL targets
     */
    public String getTargetFolderId();
    
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
