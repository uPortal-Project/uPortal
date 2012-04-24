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

package org.jasig.portal.rendering.xslt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;

/**
 * Merges the results of multiple {@link TransformerConfigurationSource}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MergingTransformerConfigurationSource implements TransformerConfigurationSource {
    private List<TransformerConfigurationSource> sources;
    
    public void setSources(List<TransformerConfigurationSource> sources) {
        this.sources = sources;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerConfigurationKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKeyBuilder cacheKeyBuilder = CacheKey.builder(this.getClass().getName());
        
        for (final TransformerConfigurationSource source : this.sources) {
            final CacheKey key = source.getCacheKey(request, response);
            cacheKeyBuilder.add(key);
        }
        
        return cacheKeyBuilder.build();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerOutputProperties(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        final Properties mergedProperties = new Properties();
        
        for (final TransformerConfigurationSource source : this.sources) {
            final Properties properties = source.getOutputProperties(request, response);
            if (properties != null) {
                mergedProperties.putAll(properties);
            }
        }
        
        return mergedProperties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final Map<String, Object> mergedParameters = new LinkedHashMap<String, Object>();
        
        for (final TransformerConfigurationSource source : this.sources) {
            final Map<String, Object> parameters = source.getParameters(request, response);
            if (parameters != null) {
                mergedParameters.putAll(parameters);
            }
        }
        
        return mergedParameters;
    }
}
