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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jasig.portal.utils.cache.resource.CachedResource;
import org.jasig.portal.utils.cache.resource.CachingResourceLoader;
import org.jasig.portal.utils.cache.resource.TemplatesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of core XML related utilities
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XmlUtilitiesImpl implements XmlUtilities {
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private TemplatesBuilder templatesBuilder;
    
    private CachingResourceLoader cachingResourceLoader;

    @Autowired
    public void setCachingResourceLoader(CachingResourceLoader cachingResourceLoader) {
        this.cachingResourceLoader = cachingResourceLoader;
    }

    @Autowired
    public void setTemplatesBuilder(TemplatesBuilder templatesBuilder) {
        this.templatesBuilder = templatesBuilder;
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
        return templates.getCacheKey();
    }

    private CachedResource<Templates> getStylesheetCachedResource(Resource stylesheet) throws IOException {
        return this.cachingResourceLoader.getResource(stylesheet, this.templatesBuilder);
    }
    
    public static String getElementText(Element e) {
        final StringBuilder text = new StringBuilder();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.TEXT_NODE || 
                n.getNodeType() == Node.CDATA_SECTION_NODE) {
                text.append(n.getNodeValue());
            }
            else {
                break;
            }
        }
        return text.toString();
    }
    
    public static String toString(Node node) {
        final Transformer identityTransformer;
        try {
            identityTransformer = transformerFactory.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to create identity transformer to serialize Node to String", e);
        }
        identityTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
        final StringWriter outputWriter = new StringWriter();
        final StreamResult outputTarget = new StreamResult(outputWriter);
        final DOMSource xmlSource = new DOMSource(node);
        try {
            identityTransformer.transform(xmlSource, outputTarget);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to convert Node to String using Transformer", e);
        }
        
        return outputWriter.toString();
    }

    public static String toString(XMLEvent event) {
        final StringWriter writer = new StringWriter();
        try {
            event.writeAsEncodedUnicode(writer);
        }
        catch (XMLStreamException e) {
            writer.write(event.toString());
        }

        return writer.toString();
    }
}
