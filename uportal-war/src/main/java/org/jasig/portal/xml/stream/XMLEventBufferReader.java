/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.xml.stream;

import java.util.ListIterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLEventBufferReader implements XMLEventReader {
    private final ListIterator<XMLEvent> eventBuffer;
    
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

    public boolean hasNext() {
        return this.eventBuffer.hasNext();
    }

    public XMLEvent next() {
        return this.eventBuffer.next();
    }
    
    public void remove() {
        this.eventBuffer.remove();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if (!this.eventBuffer.next().isStartElement()) {
            throw new XMLStreamException("Current event is not a START_ELEMENT event");
        }
        
        final StringBuilder text = new StringBuilder();
        
        while (true) {
            final XMLEvent event = this.eventBuffer.next();
            switch (event.getEventType()) {
                case XMLEvent.CHARACTERS: {
                    final Characters characters = event.asCharacters();
                    text.append(characters.getData());
                } break;
                case XMLEvent.END_ELEMENT: {
                    return text.toString();
                }
                default: {
                    throw new XMLStreamException("Event of type " + event.getEventType() + " was found instead of expected END_ELEMENT or CHARACTERS event. "  + event);
                }
            }
        }
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        // TODO no idea how to cache these :(
        return null;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return this.eventBuffer.next();
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        while (true) {
            final XMLEvent event = this.eventBuffer.next();
            switch (event.getEventType()) {
                case XMLEvent.START_ELEMENT:
                case XMLEvent.END_ELEMENT: {
                    return event;
                }
                case XMLEvent.SPACE: {
                    continue;
                }
                default: {
                    throw new XMLStreamException("Event of type " + + event.getEventType() + " was found instead of expected START_ELEMENT, END_ELEMENT or SPACE event. "  + event);
                }
            }
        }
    }
}