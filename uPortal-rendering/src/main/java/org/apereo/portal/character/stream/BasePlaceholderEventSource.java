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
package org.apereo.portal.character.stream;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.MatchResult;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 * Base implementation of CharacterEventSource that deals with reading the element data off the
 * stream.
 *
 */
public abstract class BasePlaceholderEventSource implements CharacterEventSource {

    @Override
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            XMLEventReader eventReader,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException {
        this.generateCharacterEvents(servletRequest, event, eventBuffer);
        this.readToEndElement(eventReader, event);
    }

    protected void generateCharacterEvents(
            HttpServletRequest servletRequest,
            StartElement event,
            Collection<CharacterEvent> eventBuffer) {
        throw new UnsupportedOperationException("Super class must implement this method");
    }

    @Override
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer) {
        throw new UnsupportedOperationException("Super class must implement this method");
    }

    /**
     * Read {@link XMLEvent}s off the {@link XMLEventReader} until the corresponding {@link
     * EndElement} is found.
     */
    protected final void readToEndElement(XMLEventReader eventReader, StartElement event)
            throws XMLStreamException {
        final Deque<QName> elementStack = new LinkedList<QName>();
        elementStack.push(event.getName());

        while (!elementStack.isEmpty()) {
            final XMLEvent nextEvent = eventReader.nextEvent();
            if (nextEvent.isStartElement()) {
                final StartElement startElement = nextEvent.asStartElement();
                elementStack.push(startElement.getName());
            } else if (nextEvent.isEndElement()) {
                final QName lastStart = elementStack.pop();
                final EndElement endElement = nextEvent.asEndElement();
                if (!lastStart.equals(endElement.getName())) {
                    throw new XMLStreamException(
                            "Invalid XML Structure, expected EndElement "
                                    + lastStart
                                    + " but found EndElment "
                                    + endElement.getName());
                }
            }
        }
    }
}
