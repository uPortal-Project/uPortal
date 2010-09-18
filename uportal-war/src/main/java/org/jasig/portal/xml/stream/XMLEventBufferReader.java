/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.xml.stream;

import java.util.ListIterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.XMLEvent;

/**
 * Wraps a {@link ListIterator} of {@link XMLEvent}s with an {@link XMLEventReader}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLEventBufferReader implements XMLEventReader {
    private final ListIterator<XMLEvent> eventBuffer;
    private XMLEvent previousEvent;
    
    public XMLEventBufferReader(ListIterator<XMLEvent> eventBuffer) {
        this.eventBuffer = eventBuffer;
    }

    @Override
    public void close() {
        //NO-OP
    }

    @Override
    public XMLEvent peek() {
        final XMLEvent event = this.eventBuffer.next();
        //Step back by one in the list
        this.eventBuffer.previous();
        
        return event;
    }

    @Override
    public boolean hasNext() {
        return this.eventBuffer.hasNext();
    }

    @Override
    public XMLEvent next() {
        this.previousEvent = this.eventBuffer.next();
        return this.previousEvent;
    }
    
    @Override
    public void remove() {
        this.eventBuffer.remove();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        XMLEvent event = this.previousEvent;
        if (event == null || !event.isStartElement()) {
            throw new XMLStreamException("Must be on START_ELEMENT to read next text", event.getLocation());
        }
        
        final StringBuilder text = new StringBuilder();
        while (!event.isEndDocument()) {
            switch (event.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA: {
                    final Characters characters = event.asCharacters();
                    text.append(characters.getData());
                    break;
                }
                case XMLStreamConstants.ENTITY_REFERENCE: {
                    final EntityReference entityReference = (EntityReference)event;
                    final EntityDeclaration declaration = entityReference.getDeclaration();
                    text.append(declaration.getReplacementText());
                    break;
                }
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.PROCESSING_INSTRUCTION: {
                    //Ignore
                    break;
                }
                default: {
                    throw new XMLStreamException("Unexpected event type '" + XMLStreamConstantsUtils.getEventName(event.getEventType()) + "' encountered. Found event: " + event, event.getLocation());
                }
            }
            
            event = this.nextEvent();
        }
        
        return text.toString();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        // TODO no idea how to cache these :(
        return null;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return this.next();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        XMLEvent event = this.nextEvent();
        while ((event.isCharacters() && event.asCharacters().isWhiteSpace())
                || event.isProcessingInstruction()
                || event.getEventType() == XMLStreamConstants.COMMENT) {
            
            event = this.nextEvent();
        }

        if (!event.isStartElement()  && event.isEndElement()) {
            throw new XMLStreamException("Unexpected event type '" + XMLStreamConstantsUtils.getEventName(event.getEventType()) + "' encountered. Found event: " + event, event.getLocation());
        }

        return event;
    }
}