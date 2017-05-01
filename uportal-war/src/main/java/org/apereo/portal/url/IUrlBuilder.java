/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.url;

import java.util.List;
import java.util.Map;

/**
 * Base class to define common methods for URL builders
 *
 */
public interface IUrlBuilder {
    /**
     * Sets a URL parameter
     *
     * <p>This method replaces all parameters with the given name.
     *
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void setParameter(String name, String... values);
    /** @see #setParameter(String, String...) */
    public void setParameter(String name, List<String> values);

    /**
     * Adds a URL parameter
     *
     * <p>This method adds the provided parameters on to any existing parameters with the given
     * name.
     *
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void addParameter(String name, String... values);

    /**
     * Sets a parameter map for this URL.
     *
     * <p>All previously set parameters are cleared.
     *
     * @param parameters Map containing parameters
     */
    public void setParameters(Map<String, List<String>> parameters);

    /**
     * Get the current parameters. The Map is mutable and making changes to the Map will affect the
     * parameters on the URL.
     *
     * @return Map containing currently set parameters.
     */
    public Map<String, String[]> getParameters();
}
