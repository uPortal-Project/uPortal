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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.springframework.util.xml.StaxEventLexicalContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Stax utility methods
 *
 */
public final class StaxUtils {
    private StaxUtils() {}

    public static StartElement getRootElement(final XMLEventReader bufferedXmlEventReader) {
        XMLEvent rootElement;
        try {
            rootElement = bufferedXmlEventReader.nextEvent();
            while (rootElement.getEventType() != XMLEvent.START_ELEMENT
                    && bufferedXmlEventReader.hasNext()) {
                rootElement = bufferedXmlEventReader.nextEvent();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to read root element from XML", e);
        }

        if (XMLEvent.START_ELEMENT != rootElement.getEventType()) {
            throw new IllegalArgumentException(
                    "Bad XML document for import, no root element could be found");
        }
        return rootElement.asStartElement();
    }

    public static ContentHandler createLexicalContentHandler(XMLEventWriter eventWriter) {
        return new StaxEventLexicalContentHandler(eventWriter);
    }
}
