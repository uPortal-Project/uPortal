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
package org.jasig.portal.redirect;

import java.util.HashMap;
import java.util.Map;

/**
 * AbstractRedirectionUrl provides a base for IRedirectUrls.  This class adds
 * support for additional static URL parameters, as well as for dynamic parameters.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public abstract class AbstractRedirectionUrl implements IRedirectionUrl {

    private Map<String, String[]> additionalParameters = new HashMap<String, String[]>();
    private Map<String, String> dynamicParameters = new HashMap<String, String>();

    /**
     * Get a map of additional parameters to be added to the url.  This
     * map specifies hard-coded parameters that will be used in every url.
     * 
     * The map keys represent the parameter name, while the entry values should
     * be an array of values for the associated parameter name.
     * 
     * @return
     */
    public Map<String, String[]> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * Set a map of additional parameters to be added to the url.  This
     * map specifies hard-coded parameters that will be used in every url.
     * 
     * The map keys represent the parameter name, while the entry values should
     * be an array of values for the associated parameter name.
     * 
     * @param
     */
    public void setAdditionalParameters(
            Map<String, String[]> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    /**
     * Get a map of dynamic parameters to be copied from the incoming URL
     * to the target url, mapping the parameter name in the 
     * incoming URL to the parameter name in the target URL.
     * 
     * @return
     */
    public Map<String, String> getDynamicParameters() {
        return dynamicParameters;
    }

    /**
     * Set a map of dynamic parameters to be copied from the incoming URL
     * to the target url, mapping the parameter name in the 
     * incoming URL to the parameter name in the target URL.
     * 
     * @param
     */
    public void setDynamicParameters(Map<String, String> dynamicParameters) {
        this.dynamicParameters = dynamicParameters;
    }


}
