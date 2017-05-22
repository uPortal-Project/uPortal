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
package org.springframework.util.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import org.springframework.util.Assert;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 */
public class StaxEventLexicalContentHandler extends StaxEventContentHandler
        implements LexicalHandler {
    public static final String EMPTY_SYSTEM_IDENTIFIER = "EMPTY";

    private final XMLEventFactory eventFactory;
    private final XMLEventConsumer eventConsumer;

    private StringBuilder cdata = null;

    public StaxEventLexicalContentHandler(XMLEventConsumer consumer, XMLEventFactory factory) {
        super(consumer, factory);

        Assert.notNull(consumer, "'consumer' must not be null");

        this.eventFactory = factory;
        this.eventConsumer = consumer;
    }

    public StaxEventLexicalContentHandler(XMLEventConsumer consumer) {
        this(consumer, XMLEventFactory.newInstance());
    }

    /**
     * Essentially the same logic as the parent but uses a static Location impl to avoid this$0
     * reference holding on to StaxEventLexicalContentHandler instances
     */
    public void setDocumentLocator(final Locator locator) {
        if (locator != null) {
            eventFactory.setLocation(new LocatorLocation(locator));
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // System identifier must be specified to print DOCTYPE.

        // This method is only called if the system identifier is specified.
        // Since the HTML5 DOCTYPE declaration does not include a system
        // identifier, this code allows the static string EMPTY to serve as
        // a temporary system id for doctypes which should not have one set.

        // If public identifier is specified print 'PUBLIC
        // <public> <system>', or if a non-'EMPTY' system identifier is
        // specified, print 'SYSTEM <system>'.

        final StringBuilder dtdBuilder = new StringBuilder("<!DOCTYPE ");
        dtdBuilder.append(name);
        if (publicId != null) {
            dtdBuilder.append(" PUBLIC \"").append(publicId).append("\" \"");
            dtdBuilder.append(systemId).append("\"");
        } else if (!EMPTY_SYSTEM_IDENTIFIER.equals(systemId)) {
            dtdBuilder.append(" SYSTEM \"");
            dtdBuilder.append(systemId).append("\"");
        }
        dtdBuilder.append(">");

        final DTD event = eventFactory.createDTD(dtdBuilder.toString());
        try {
            this.consumeEvent(event);
        } catch (XMLStreamException ex) {
            throw new SAXException("Could not create DTD: " + ex.getMessage(), ex);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    @Override
    public void endDTD() throws SAXException {
        return;
        //noop
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    @Override
    public void startEntity(String name) throws SAXException {
        final EntityReference event = eventFactory.createEntityReference(name, null);
        try {
            this.consumeEvent(event);
        } catch (XMLStreamException ex) {
            throw new SAXException("Could not create Entity: " + ex.getMessage(), ex);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    @Override
    public void endEntity(String name) throws SAXException {
        return;
        //noop
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    @Override
    public void startCDATA() throws SAXException {
        this.cdata = new StringBuilder();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    @Override
    public void endCDATA() throws SAXException {
        final Characters event = eventFactory.createCData(cdata.toString());
        cdata = null;
        try {
            this.consumeEvent(event);
        } catch (XMLStreamException ex) {
            throw new SAXException("Could not create CDATA: " + ex.getMessage(), ex);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.util.xml.StaxEventContentHandler#charactersInternal(char[], int, int)
     */
    @Override
    protected void charactersInternal(char[] ch, int start, int length) throws XMLStreamException {
        if (this.cdata != null) {
            cdata.append(ch, start, length);
        } else {
            super.charactersInternal(ch, start, length);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        final Comment event = eventFactory.createComment(new String(ch, start, length));
        try {
            this.consumeEvent(event);
        } catch (XMLStreamException ex) {
            throw new SAXException("Could not create Comment: " + ex.getMessage(), ex);
        }
    }

    private void consumeEvent(XMLEvent event) throws XMLStreamException {
        eventConsumer.add(event);
    }

    private static final class LocatorLocation implements Location {
        private final Locator locator;

        private LocatorLocation(Locator locator) {
            this.locator = locator;
        }

        public int getLineNumber() {
            return locator.getLineNumber();
        }

        public int getColumnNumber() {
            return locator.getColumnNumber();
        }

        public int getCharacterOffset() {
            return -1;
        }

        public String getPublicId() {
            return locator.getPublicId();
        }

        public String getSystemId() {
            return locator.getSystemId();
        }
    }
}
