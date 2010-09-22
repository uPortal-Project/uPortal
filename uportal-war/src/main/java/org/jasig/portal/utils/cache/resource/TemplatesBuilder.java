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

package org.jasig.portal.utils.cache.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Parses the provided input stream into a {@link Templates} object.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class TemplatesBuilder implements Loader<Templates>, ResourceLoaderAware {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.resource.ResourceBuilder#buildResource(org.springframework.core.io.Resource, java.io.InputStream)
     */
    @Override
    public LoadedResource<Templates> loadResource(Resource resource, InputStream stream) throws IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
        final ResourceTrackingURIResolver uriResolver = new ResourceTrackingURIResolver(this.resourceLoader);
        transformerFactory.setURIResolver(uriResolver);
        
        final URI uri = resource.getURI();
        final String systemId = uri.toString();
        
        final StreamSource source = new StreamSource(stream, systemId);
        final Templates templates;
        try {
            templates = transformerFactory.newTemplates(source);
        }
        catch (TransformerConfigurationException e) {
            throw new IOException("Failed to parse stream into Templates", e);
        }
        
        final Set<Resource> resolvedResources = uriResolver.getResolvedResources();
        
        return new LoadedResourceImpl<Templates>(templates, resolvedResources);
    }
    
    private static class ResourceTrackingURIResolver extends ResourceLoaderURIResolver {
        private final Set<Resource> resolvedResources = new LinkedHashSet<Resource>();
        
        public ResourceTrackingURIResolver(ResourceLoader resourceLoader) {
            super(resourceLoader);
        }

        @Override
        protected Resource resolveResource(String href, String base) throws TransformerException {
            final Resource resource = super.resolveResource(href, base);
            resolvedResources.add(resource);
            return resource;
        }

        public Set<Resource> getResolvedResources() {
            return this.resolvedResources;
        }
    }
}
