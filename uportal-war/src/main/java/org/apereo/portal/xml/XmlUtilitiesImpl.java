/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.xml;

import com.ctc.wstx.api.EmptyElementHandler;
import com.ctc.wstx.api.WstxOutputProperties;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apereo.portal.utils.DocumentFactory;
import org.apereo.portal.utils.cache.resource.CachedResource;
import org.apereo.portal.utils.cache.resource.CachingResourceLoader;
import org.apereo.portal.utils.cache.resource.TemplatesBuilder;
import org.apereo.portal.xml.stream.IndentingXMLEventWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of core XML related utilities
 *
 */
@Service
public class XmlUtilitiesImpl implements XmlUtilities {
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private final XMLOutputFactory xmlOutputFactory;
    private final XMLOutputFactory htmlOutputFactory;
    private final XMLInputFactory xmlInputFactory;
    private TemplatesBuilder templatesBuilder;

    private CachingResourceLoader cachingResourceLoader;

    public XmlUtilitiesImpl() {
        this.xmlOutputFactory = XMLOutputFactory.newFactory();

        this.xmlInputFactory = XMLInputFactory.newInstance();

        this.htmlOutputFactory = XMLOutputFactory.newFactory();
        this.htmlOutputFactory.setProperty(
                WstxOutputProperties.P_OUTPUT_EMPTY_ELEMENT_HANDLER,
                EmptyElementHandler.HtmlEmptyElementHandler.getInstance());
    }

    @Autowired
    public void setCachingResourceLoader(CachingResourceLoader cachingResourceLoader) {
        this.cachingResourceLoader = cachingResourceLoader;
    }

    @Autowired
    public void setTemplatesBuilder(TemplatesBuilder templatesBuilder) {
        this.templatesBuilder = templatesBuilder;
    }

    @Override
    public Templates getTemplates(Resource stylesheet)
            throws TransformerConfigurationException, IOException {
        final CachedResource<Templates> templates = this.getStylesheetCachedResource(stylesheet);
        return templates.getCachedResource();
    }

    @Override
    public Transformer getTransformer(Resource stylesheet)
            throws TransformerConfigurationException, IOException {
        final Templates templates = this.getTemplates(stylesheet);
        return templates.newTransformer();
    }

    @Override
    public Transformer getIdentityTransformer()
            throws TransformerConfigurationException, IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        return transformerFactory.newTransformer();
    }

    @Override
    public Serializable getStylesheetCacheKey(Resource stylesheet)
            throws TransformerConfigurationException, IOException {
        final CachedResource<Templates> templates = this.getStylesheetCachedResource(stylesheet);
        return templates.getCacheKey();
    }

    @Override
    public XMLOutputFactory getXmlOutputFactory() {
        return this.xmlOutputFactory;
    }

    @Override
    public XMLOutputFactory getHtmlOutputFactory() {
        return this.htmlOutputFactory;
    }

    @Override
    public XMLInputFactory getXmlInputFactory() {
        return this.xmlInputFactory;
    }

    @Override
    public String serializeXMLEvents(List<XMLEvent> xmlEvents) {
        return this.serializeXMLEvents(xmlEvents, false);
    }

    @Override
    public String serializeXMLEvents(List<XMLEvent> xmlEvents, boolean isHtml) {
        final XMLOutputFactory outputFactory;
        if (isHtml) {
            outputFactory = this.getHtmlOutputFactory();
        } else {
            outputFactory = this.getXmlOutputFactory();
        }

        final StringWriter writer = new StringWriter();
        final XMLEventWriter xmlEventWriter;
        try {
            xmlEventWriter =
                    new IndentingXMLEventWriter(outputFactory.createXMLEventWriter(writer));
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLEventWriter", e);
        }

        try {
            for (final XMLEvent bufferedEvent : xmlEvents) {
                xmlEventWriter.add(bufferedEvent);
            }
            xmlEventWriter.flush();
            xmlEventWriter.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to write XMLEvents to XMLEventWriter", e);
        }

        return writer.toString();
    }

    @Override
    public Node convertToDom(XMLEventReader xmlEventReader) throws XMLStreamException {

        //Convert the XmlEventReader into a DOM
        final XMLOutputFactory xmlOutputFactory = this.getXmlOutputFactory();
        final DOMResult sourceDom = new DOMResult(DocumentFactory.getThreadDocument());
        final XMLEventWriter sourceWriter = xmlOutputFactory.createXMLEventWriter(sourceDom);
        sourceWriter.add(xmlEventReader);
        sourceWriter.flush();
        sourceWriter.close();

        return sourceDom.getNode();
    }

    /*
     * Credit for this impl from: http://snippets.dzone.com/posts/show/3754
     */
    @Override
    public String getUniqueXPath(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }

        final StringBuilder path = new StringBuilder();

        for (;
                node != null && node.getNodeType() == Node.ELEMENT_NODE;
                node = node.getParentNode()) {
            final int elementIndex = getElementIndex(node);
            final String nodeName = node.getNodeName();
            if (elementIndex > 1) {
                path.insert(0, "]").insert(0, elementIndex).insert(0, "[");
            }
            path.insert(0, nodeName).insert(0, "/");
        }

        return path.toString();
    }

    /** Gets the index of this element relative to other siblings with the same node name */
    private int getElementIndex(Node node) {
        final String nodeName = node.getNodeName();

        int count = 1;
        for (Node previousSibling = node.getPreviousSibling();
                previousSibling != null;
                previousSibling = previousSibling.getPreviousSibling()) {
            if (previousSibling.getNodeType() == Node.ELEMENT_NODE
                    && previousSibling.getNodeName().equals(nodeName)) {
                count++;
            }
        }

        return count;
    }

    private CachedResource<Templates> getStylesheetCachedResource(Resource stylesheet)
            throws IOException {
        return this.cachingResourceLoader.getResource(stylesheet, this.templatesBuilder);
    }

    public static String getElementText(Element e) {
        final StringBuilder val = new StringBuilder();
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
                val.append(n.getNodeValue());
            }
        }
        return val.toString();
    }

    public static String toString(Node node) {
        final Transformer identityTransformer;
        try {
            identityTransformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(
                    "Failed to create identity transformer to serialize Node to String", e);
        }
        identityTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        final StringWriter outputWriter = new StringWriter();
        final StreamResult outputTarget = new StreamResult(outputWriter);
        final DOMSource xmlSource = new DOMSource(node);
        try {
            identityTransformer.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            throw new RuntimeException("Failed to convert Node to String using Transformer", e);
        }

        return outputWriter.toString();
    }

    public static String toString(XMLEvent event) {
        final StringWriter writer = new StringWriter();
        try {
            event.writeAsEncodedUnicode(writer);
        } catch (XMLStreamException e) {
            writer.write(event.toString());
        }

        return writer.toString();
    }
}
