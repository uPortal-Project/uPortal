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
package org.apereo.portal.xml.stream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for XMLEventReader that implements the {@link #getElementText()} and {@link #nextTag()} APIs
 * in a way that is agnostic from the rest of the XMLEventReader implementation. Both will use the
 * subclasses {@link #internalNextEvent()} as the exclusive way to read events.
 *
 */
public abstract class BaseXMLEventReader extends EventReaderDelegate {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private XMLEvent previousEvent;

    public BaseXMLEventReader(XMLEventReader reader) {
        super(reader);
    }

    /** Subclass's version of {@link #nextEvent()}, called by {@link #next()} */
    protected abstract XMLEvent internalNextEvent() throws XMLStreamException;

    /** @return The XMLEvent returned by the last call to {@link #internalNextEvent()} */
    protected final XMLEvent getPreviousEvent() {
        return this.previousEvent;
    }

    @Override
    public final XMLEvent nextEvent() throws XMLStreamException {
        this.previousEvent = this.internalNextEvent();
        return this.previousEvent;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public final Object next() {
        try {
            return this.nextEvent();
        } catch (XMLStreamException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#getElementText()
     */
    @Override
    public final String getElementText() throws XMLStreamException {
        XMLEvent event = this.previousEvent;
        if (event == null) {
            throw new XMLStreamException(
                    "Must be on START_ELEMENT to read next text, element was null");
        }
        if (!event.isStartElement()) {
            throw new XMLStreamException(
                    "Must be on START_ELEMENT to read next text", event.getLocation());
        }

        final StringBuilder text = new StringBuilder();
        while (!event.isEndDocument()) {
            switch (event.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                    {
                        final Characters characters = event.asCharacters();
                        text.append(characters.getData());
                        break;
                    }
                case XMLStreamConstants.ENTITY_REFERENCE:
                    {
                        final EntityReference entityReference = (EntityReference) event;
                        final EntityDeclaration declaration = entityReference.getDeclaration();
                        text.append(declaration.getReplacementText());
                        break;
                    }
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    {
                        //Ignore
                        break;
                    }
                default:
                    {
                        throw new XMLStreamException(
                                "Unexpected event type '"
                                        + XMLStreamConstantsUtils.getEventName(event.getEventType())
                                        + "' encountered. Found event: "
                                        + event,
                                event.getLocation());
                    }
            }

            event = this.nextEvent();
        }

        return text.toString();
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLEventReader#nextTag()
     */
    @Override
    public final XMLEvent nextTag() throws XMLStreamException {
        XMLEvent event = this.nextEvent();
        while ((event.isCharacters() && event.asCharacters().isWhiteSpace())
                || event.isProcessingInstruction()
                || event.getEventType() == XMLStreamConstants.COMMENT) {

            event = this.nextEvent();
        }

        if (!event.isStartElement() && event.isEndElement()) {
            throw new XMLStreamException(
                    "Unexpected event type '"
                            + XMLStreamConstantsUtils.getEventName(event.getEventType())
                            + "' encountered. Found event: "
                            + event,
                    event.getLocation());
        }

        return event;
    }
}
