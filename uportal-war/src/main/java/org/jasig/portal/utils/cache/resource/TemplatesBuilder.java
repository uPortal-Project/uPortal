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

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.Resource;

/**
 * Parses the provided input stream into a {@link Templates} object.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TemplatesBuilder implements ResourceBuilder<Templates> {
    private final URIResolver uriResolver;
    
    public TemplatesBuilder() {
        this(null);
    }

    /**
     * @param uriResolver Optional {@link URIResolver} the {@link TransformerFactory} should use
     */
    public TemplatesBuilder(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.utils.cache.resource.ResourceBuilder#buildResource(org.springframework.core.io.Resource, java.io.InputStream)
     */
    @Override
    public Templates buildResource(Resource resource, InputStream stream) throws IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
        if (this.uriResolver != null) {
            transformerFactory.setURIResolver(this.uriResolver);
        }
        
        final URI resourceUri = resource.getURI();
        final String path = resourceUri.getPath();

        final String systemId = path.substring(0, path.lastIndexOf('/') + 1);
        
        final StreamSource source = new StreamSource(stream, systemId);
        try {
            return transformerFactory.newTemplates(source);
        }
        catch (TransformerConfigurationException e) {
            throw new IOException("Failed to parse stream into Templates", e);
        }
    }

}
