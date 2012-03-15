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
public abstract class StylesheetUserPreferencesTransformerConfigurationSource extends TransformerConfigurationSourceAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected IStylesheetUserPreferencesService stylesheetUserPreferencesService;

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
        
        final Map<String, String> stylesheetParameters = stylesheetUserPreferences.getStylesheetParameters();
        
        return new LinkedHashMap<String, Object>(stylesheetParameters);
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
