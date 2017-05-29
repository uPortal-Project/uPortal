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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

/**
 * Defines commonly used XML and XSLT utilities
 *
 */
public interface XmlUtilities {
    /** Get {@link Templates} for the specified stylesheet resource */
    public Templates getTemplates(Resource stylesheet)
            throws TransformerConfigurationException, IOException;

    /** Get a {@link Transformer} for the specified stylesheet resource */
    public Transformer getTransformer(Resource stylesheet)
            throws TransformerConfigurationException, IOException;

    /** @return The identity transformer */
    public Transformer getIdentityTransformer()
            throws TransformerConfigurationException, IOException;

    /**
     * Gets an appropriate cache key for the specified stylesheet resource. The key should be valid
     * for both {@link Transformer}s and {@link Templates} based on this stylesheet.
     */
    public Serializable getStylesheetCacheKey(Resource stylesheet)
            throws TransformerConfigurationException, IOException;

    /**
     * The standard shared XMLOutputFactory to be used by uPortal code. Clients should not set any
     * properties on this XMLOutputFactory.
     */
    public XMLOutputFactory getXmlOutputFactory();

    /**
     * The standard shared XMLOutputFactory to be used by uPortal code writing StAX to HTML. Clients
     * should not set any properties on this XMLOutputFactory
     */
    public XMLOutputFactory getHtmlOutputFactory();

    /**
     * The standard shared XMLInputFactory to be used by uPortal code. Clients should not set any
     * properties on this XMLInputFactory, if they need to they should use {@link
     * XMLInputFactory#newFactory()} directly
     */
    public XMLInputFactory getXmlInputFactory();

    /** Serializes the List of XMLEvents into a XML String */
    public String serializeXMLEvents(List<XMLEvent> xmlEvents);

    /** Serializes the List of XMLEvents into a XML String using HTML safe formatting */
    public String serializeXMLEvents(List<XMLEvent> xmlEvents, boolean isHtml);

    /** Get the unique XPath for the specified Node */
    public String getUniqueXPath(Node node);

    /** Convert the data from an {@link XMLEventReader} into a DOM node */
    public Node convertToDom(XMLEventReader xmlEventReader) throws XMLStreamException;
}
