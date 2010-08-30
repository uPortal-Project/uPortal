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

package org.jasig.portal.xml;

import java.io.IOException;
import java.io.Serializable;

import javax.xml.stream.XMLEventFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.jasig.portal.utils.cache.resource.CachedResource;
import org.jasig.portal.utils.cache.resource.CachingResourceLoader;
import org.jasig.portal.utils.cache.resource.ResourceLoaderOptions;
import org.jasig.portal.utils.cache.resource.ResourceLoaderOptionsBuilder;
import org.jasig.portal.utils.cache.resource.TemplatesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Implementation of core XML related utilities
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XmlUtilitiesImpl implements XmlUtilities {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private final ResourceLoaderOptions templatesLoaderOptions = new ResourceLoaderOptionsBuilder().digestAlgorithm("SHA1").digestInput(true);
    
    private CachingResourceLoader cachingResourceLoader;

    @Autowired
    public void setCachingResourceLoader(CachingResourceLoader cachingResourceLoader) {
        this.cachingResourceLoader = cachingResourceLoader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getTemplates(org.springframework.core.io.Resource)
     */
    @Override
    public Templates getTemplates(Resource stylesheet) throws TransformerConfigurationException, IOException {
        final CachedResource<Templates> templates = this.getStylesheetCachedResource(stylesheet);
        return templates.getCachedResource();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getTransformer(org.springframework.core.io.Resource)
     */
    @Override
    public Transformer getTransformer(Resource stylesheet) throws TransformerConfigurationException, IOException {
        final Templates templates = this.getTemplates(stylesheet);
        return templates.newTransformer();
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getStylesheetCacheKey(org.springframework.core.io.Resource)
     */
    @Override
    public Serializable getStylesheetCacheKey(Resource stylesheet) throws TransformerConfigurationException, IOException {
        final CachedResource<Templates> templates = this.getStylesheetCachedResource(stylesheet);
        return templates.getLastLoadDigest();
    }

    private CachedResource<Templates> getStylesheetCachedResource(Resource stylesheet) throws IOException {
        return this.cachingResourceLoader.getResource(stylesheet, TemplatesBuilder.INSTANCE, this.templatesLoaderOptions);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getXmlEventFactory()
     */
    @Override
    public XMLEventFactory getXmlEventFactory() {
        return this.xmlEventFactory;
    }
}
