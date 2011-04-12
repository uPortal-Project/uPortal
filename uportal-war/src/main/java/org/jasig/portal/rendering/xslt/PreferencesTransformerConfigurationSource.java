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
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Exposes the transformer parameter from {@link StylesheetUserPreferences#getParameterValues()}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class PreferencesTransformerConfigurationSource extends TransformerConfigurationSourceAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    private IStylesheetDescriptorDao stylesheetDescriptorDao;

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }

    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerConfigurationKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final LinkedHashMap<String, Object> transformerParameters = this.getParameters(request, response);
        final Properties outputProperties = this.getOutputProperties(request, response);
        return new CacheKey(this.getName(), transformerParameters, outputProperties);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public final LinkedHashMap<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request);
        
        final long stylesheetDescriptorId = stylesheetUserPreferences.getStylesheetDescriptorId();
        final IStylesheetDescriptor stylesheetDescriptor = this.stylesheetDescriptorDao.getStylesheetDescriptor(stylesheetDescriptorId);
        
        //Add default values
        final LinkedHashMap<String, Object> compositeStylesheetParameters = new LinkedHashMap<String, Object>();
        for (final IStylesheetParameterDescriptor stylesheetParameterDescriptor : stylesheetDescriptor.getStylesheetParameterDescriptors()) {
            final String defaultValue = stylesheetParameterDescriptor.getDefaultValue();
            if (defaultValue != null) {
                final String name = stylesheetParameterDescriptor.getName();
                compositeStylesheetParameters.put(name, defaultValue);
            }
        }
        
        //Add user overrides
        final Map<String, String> stylesheetParameters = stylesheetUserPreferences.getStylesheetParameters();
        compositeStylesheetParameters.putAll(stylesheetParameters);
        
        this.logger.debug("Setting transformer parameters: {}", compositeStylesheetParameters);
        
        return compositeStylesheetParameters;
    }
    
    @Override
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        final IStylesheetUserPreferences stylesheetUserPreferences = this.getStylesheetUserPreferences(request);
        final Properties outputProperties = stylesheetUserPreferences.getOutputProperties();
        
        this.logger.debug("Setting output parameters: {}", outputProperties);
        
        return outputProperties;
    }

    protected abstract String getName();
    
    protected abstract IStylesheetUserPreferences getStylesheetUserPreferences(HttpServletRequest request);
}
