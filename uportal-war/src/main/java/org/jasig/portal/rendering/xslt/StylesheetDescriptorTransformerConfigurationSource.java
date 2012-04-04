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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IStylesheetUserPreferencesService.PreferencesScope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.utils.cache.CacheKey.CacheKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exposes transformer parameters from {@link IStylesheetDescriptor#getStylesheetParameterDescriptors()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class StylesheetDescriptorTransformerConfigurationSource extends TransformerConfigurationSourceAdapter implements BeanNameAware {
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

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKeyBuilder cacheKeyBuilder = CacheKey.builder(this.getName());
        
        final PreferencesScope preferencesScope = this.getStylesheetPreferencesScope(request);
        
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetUserPreferencesService.getStylesheetDescriptor(request, preferencesScope);
        
        //Build key from stylesheet descriptor parameters
        for (final IStylesheetParameterDescriptor stylesheetParameterDescriptor : stylesheetDescriptor.getStylesheetParameterDescriptors()) {
            final String defaultValue = stylesheetParameterDescriptor.getDefaultValue();
            if (defaultValue != null) {
                final String name = stylesheetParameterDescriptor.getName();
                cacheKeyBuilder.put(name, defaultValue);
            }
        }
        
        return cacheKeyBuilder.build();
    }
    
    @Override
    public final Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final PreferencesScope preferencesScope = this.getStylesheetPreferencesScope(request);
        
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetUserPreferencesService.getStylesheetDescriptor(request, preferencesScope);
        
        //Build map of stylesheet descriptor parameters
        final LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        for (final IStylesheetParameterDescriptor stylesheetParameterDescriptor : stylesheetDescriptor.getStylesheetParameterDescriptors()) {
            final String defaultValue = stylesheetParameterDescriptor.getDefaultValue();
            if (defaultValue != null) {
                final String name = stylesheetParameterDescriptor.getName();
                parameters.put(name, defaultValue);
            }
        }
        
        return parameters;
    }

    protected String getName() {
        return this.beanName;
    }
    
    protected abstract PreferencesScope getStylesheetPreferencesScope(HttpServletRequest request);
    
}
