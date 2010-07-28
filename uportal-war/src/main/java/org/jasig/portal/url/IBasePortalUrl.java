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
 * Common base for a portal URL, provides methods to set parameters for the portal. Portal URLs generally
 * provide support for navigational changes within the portal rendered layout.
 * 
 * The portal URL implementation 'x-www-form-urlencoded' encodes all parameter names and values. Developers should not encode them.
 * 
 * The portal may prefix the attribute names internally
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IBasePortalUrl {
    /**
     * Sets a URL parameter targeted to the portal.
     * 
     * This method replaces all parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void setPortalParameter(String name, String... values);
    public void setPortalParameter(String name, List<String> values);
    
    /**
     * Adds a URL parameter targeted to the portal.
     * 
     * This method adds the provided parameters on to any existing parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void addPortalParameter(String name, String... values);
    
    /**
     * Sets a portal parameter map for this URL.
     * 
     * All previously set portal parameters are cleared.
     * 
     * @param parameters Map containing parameters
     */
    public void setPortalParameters(Map<String, List<String>> parameters);
    
    /**
     * Get the current portal parameters. The Map is mutable and making changes to the Map will affect the parameters on the URL.
     * 
     * @return Map containing currently set portal parameters.
     */
    public Map<String, List<String>> getPortalParameters();
    
    /**
     * @return Generate a URL to be used in markup or as a redirect. The URL will be absolute, starting with a / or with a protocol such as http://
     */
    public String getUrlString();
}
