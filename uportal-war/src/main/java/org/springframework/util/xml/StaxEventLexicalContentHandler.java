/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.springframework.util.xml;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EntityReference;

import org.springframework.util.Assert;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaxEventLexicalContentHandler extends StaxEventHandler implements LexicalHandler {

    public static final String EMPTY_SYSTEM_IDENTIFIER = "EMPTY";
    public static final String EMTPY_SYSTEM_ID_HTML_DOCTYPE_TO_REPLACE = "<!DOCTYPE html SYSTEM \"" + EMPTY_SYSTEM_IDENTIFIER + "\">";
    public static final String EMPTY_SYSTEM_ID_HTML_DOCTYPE_TO_USE = "<!DOCTYPE html>";

    private final XMLEventFactory eventFactory;
    private final XMLEventWriter eventWriter;

    public StaxEventLexicalContentHandler(XMLEventWriter writer, XMLEventFactory factory) {
        super(writer, factory);
        Assert.notNull(writer, "'writer' must not be null");
        this.eventFactory = factory;
        this.eventWriter = writer;
    }

    public StaxEventLexicalContentHandler(XMLEventWriter writer) {
        this(writer, XMLEventFactory.newInstance());
    }

    @Override
    protected void dtdInternal(String dtd) throws XMLStreamException {
        // System identifier must be specified to print DOCTYPE.

        // This method is only called if the system identifier is specified.
        // Since the HTML5 DOCTYPE declaration does not include a system 
        // identifier, this code allows the static string EMPTY to serve as
        // a temporary system id for doctypes which should not have one set.

        // If public identifier is specified print 'PUBLIC
        // <public> <system>', or if a non-'EMPTY' system identifier is 
        // specified, print 'SYSTEM <system>'.
        String dtdToUse;
        if (EMTPY_SYSTEM_ID_HTML_DOCTYPE_TO_REPLACE.equals(dtd)) {
            dtdToUse = EMPTY_SYSTEM_ID_HTML_DOCTYPE_TO_USE;
        } else {
            dtdToUse = dtd;
        }
        this.eventWriter.add(this.eventFactory.createDTD(dtdToUse));
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    @Override
    public void startEntity(String name) throws SAXException {
        final EntityReference event = eventFactory.createEntityReference(name, null);
        try {
        	this.eventWriter.add(event);
        }
        catch (XMLStreamException ex) {
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

}
