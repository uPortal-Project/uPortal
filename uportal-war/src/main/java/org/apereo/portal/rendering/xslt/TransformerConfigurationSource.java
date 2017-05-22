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
package org.apereo.portal.rendering.xslt;

import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import org.apereo.portal.utils.cache.CacheKey;

/**
 * Used by a {@link XSLTComponent} to configure the {@link Transformer}
 *
 */
public interface TransformerConfigurationSource {
    /**
     * Passed on to {@link Transformer#setParameter(String, Object)}, ignored if null is returned
     */
    public Map<String, Object> getParameters(
            HttpServletRequest request, HttpServletResponse response);

    /**
     * Passed on to {@link Transformer#setOutputProperties(Properties)}, ignored if null is returned
     */
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response);

    /**
     * A key representing the state of the parameters and properties for the request, ignored if
     * null is returned
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
}
