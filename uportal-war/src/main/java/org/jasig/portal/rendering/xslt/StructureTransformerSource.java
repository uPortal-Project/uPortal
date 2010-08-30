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

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.XmlUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StructureTransformerSource implements TransformerSource, ResourceLoaderAware {
    private IUserInstanceManager userInstanceManager;
    private XmlUtilities xmlUtilities;
    private ResourceLoader resourceLoader;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }



    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final Resource stylesheetResource = this.getStylesheetResource(request);
        final Serializable stylesheetCacheKey;
        try {
            stylesheetCacheKey = this.xmlUtilities.getStylesheetCacheKey(stylesheetResource);
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to get Transformer for stylesheet: " + stylesheetResource, e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load stylesheet: " + stylesheetResource, e);
        }
        
        return new CacheKey(stylesheetResource.getDescription(), stylesheetCacheKey);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerSource#getTransformer(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Transformer getTransformer(HttpServletRequest request, HttpServletResponse response) {
        final Resource stylesheetResource = this.getStylesheetResource(request);
        try {
            return this.xmlUtilities.getTransformer(stylesheetResource);
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to get Transformer for stylesheet: " + stylesheetResource, e);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load stylesheet: " + stylesheetResource, e);
        }
    }

    private Resource getStylesheetResource(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final StructureStylesheetDescription structureStylesheetDescription;
        try {
            structureStylesheetDescription = preferencesManager.getStructureStylesheetDescription();
        }
        catch (Exception e) {
            //TODO fix getStructureStylesheetDescription so it doesn't throw Exception
            throw new RuntimeException("Failed getting StructureStylesheetDescription from IUserPreferencesManager", e);
        }
        
        final String stylesheetURI = structureStylesheetDescription.getStylesheetURI();
        return this.resourceLoader.getResource(stylesheetURI);
    }

}
