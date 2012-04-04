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
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exposes the transformer parameter from {@link StylesheetUserPreferences#getParameterValues()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class StylesheetUserPreferencesTransformerConfigurationSource extends TransformerConfigurationSourceAdapter implements BeanNameAware {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private String beanName;

    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerConfigurationKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKeyBuilder cacheKeyBuilder = CacheKey.builder(getName());
        
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);

        final Map<String, String> stylesheetParameters = this.stylesheetUserPreferencesService.populateStylesheetParameters(request, stylesheetPreferencesScope, new LinkedHashMap<String, String>());
        cacheKeyBuilder.putAll(stylesheetParameters);
        
        final Properties outputProperties = this.stylesheetUserPreferencesService.populateOutputProperties(request, stylesheetPreferencesScope, new Properties());
        cacheKeyBuilder.putAll(outputProperties);
        
        return cacheKeyBuilder.build();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public final LinkedHashMap<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);
        
        final Map<String, String> stylesheetParameters = this.stylesheetUserPreferencesService.populateStylesheetParameters(request, stylesheetPreferencesScope, new LinkedHashMap<String, String>());
        
        return new LinkedHashMap<String, Object>(stylesheetParameters);
    }
    
    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        final PreferencesScope stylesheetPreferencesScope = this.getStylesheetPreferencesScope(request);
        
        return this.stylesheetUserPreferencesService.populateOutputProperties(request, stylesheetPreferencesScope, new Properties());
    }

    protected String getName() {
        return this.beanName;
    }
    
    protected abstract PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request);
    
}
