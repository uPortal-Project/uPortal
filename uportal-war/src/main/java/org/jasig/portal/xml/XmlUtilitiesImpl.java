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
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;

import org.jasig.portal.utils.cache.resource.CachedResource;
import org.jasig.portal.utils.cache.resource.CachingResourceLoader;
import org.jasig.portal.utils.cache.resource.ResourceLoaderOptions;
import org.jasig.portal.utils.cache.resource.ResourceLoaderOptionsBuilder;
import org.jasig.portal.utils.cache.resource.TemplatesBuilder;
import org.jasig.portal.xml.stream.XMLStreamConstantsUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Implementation of core XML related utilities
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XmlUtilitiesImpl implements XmlUtilities, ResourceLoaderAware, InitializingBean {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private final ResourceLoaderOptions templatesLoaderOptions = new ResourceLoaderOptionsBuilder().digestAlgorithm("SHA1").digestInput(true);

    private URIResolver uriResolver;
    private TemplatesBuilder templatesBuilder;
    
    private CachingResourceLoader cachingResourceLoader;

    @Autowired
    public void setCachingResourceLoader(CachingResourceLoader cachingResourceLoader) {
        this.cachingResourceLoader = cachingResourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        final ResourceLoaderURIResolver uriResolver = new ResourceLoaderURIResolver();
        uriResolver.setResourceLoader(resourceLoader);
        
        this.uriResolver = uriResolver;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.templatesBuilder = new TemplatesBuilder(this.uriResolver);
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
        return this.cachingResourceLoader.getResource(stylesheet, this.templatesBuilder, this.templatesLoaderOptions);
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getXmlEventFactory()
     */
    @Override
    public XMLEventFactory getXmlEventFactory() {
        return this.xmlEventFactory;
    }

    @Override
    public String xmlEventToString(XMLEvent event) {
        final StringWriter writer = new StringWriter();
        try {
            event.writeAsEncodedUnicode(writer);
        }
        catch (XMLStreamException e) {
            writer.write(event.toString());
        }

        return writer.toString();
    }

    @Override
    public String streamStateToString(XMLStreamReader streamReader) {
        final int eventType = streamReader.getEventType();
        
        final StringBuilder state = new StringBuilder();
        
        state.append("[");
        state.append(XMLStreamConstantsUtils.getEventName(eventType));
        state.append(" ");
        
        switch (eventType) {
            case XMLStreamConstants.START_ELEMENT: {
                state.append("<");
                state.append(streamReader.getName());
                
                for (int index = 0; index < streamReader.getAttributeCount(); index++) {
                    final QName attributeName = streamReader.getAttributeName(index);
                    final String value = streamReader.getAttributeValue(index);
                    state.append(" ");
                    state.append(attributeName);
                    state.append("=");
                    state.append(value);
                }
                
                state.append(">");
                break;
            }
            case XMLStreamConstants.END_ELEMENT: {
                state.append("<");
                state.append(streamReader.getName());
                state.append(">");
                break;
            }
            case XMLStreamConstants.PROCESSING_INSTRUCTION: {
                break;
            }
            case XMLStreamConstants.CHARACTERS: {
                state.append(streamReader.getText());
                break;
            }
            case XMLStreamConstants.COMMENT: {
                break;
            }
            case XMLStreamConstants.ENTITY_REFERENCE: {
                break;
            }
            case XMLStreamConstants.ATTRIBUTE: {
                break;
            }
            case XMLStreamConstants.DTD: {
                break;
            }
            case XMLStreamConstants.CDATA: {
                break;
            }
        }

        state.append("]");
        
        return state.toString();
    }
}
