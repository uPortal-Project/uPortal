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

package org.jasig.portal.layout.immutable;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.utils.IteratorWrapper;
import org.jasig.portal.xml.stream.BaseXMLEventReader;
import org.jasig.portal.xml.stream.events.StartElementWrapper;
import org.jasig.portal.xml.stream.events.ValueOverridingAttributeWrapper;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ImmutableUserLayoutXMLEventReader extends BaseXMLEventReader {
    private final XMLEventReader wrappedReader;
    private XMLEvent previousEvent;
    
    public ImmutableUserLayoutXMLEventReader(XMLEventReader wrappedReader) {
        this.wrappedReader = wrappedReader;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.stream.BaseXMLEventReader#getPreviousEvent()
     */
    @Override
    protected XMLEvent getPreviousEvent() {
        return this.previousEvent;
    }

    public void close() throws XMLStreamException {
        this.wrappedReader.close();
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return this.wrappedReader.getProperty(name);
    }

    public boolean hasNext() {
        return this.wrappedReader.hasNext();
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent event = this.wrappedReader.nextEvent();
        
        if (event.isStartDocument()) {
            final StartElement startElement = event.asStartElement();
            
            final QName name = startElement.getName();
            
            final String localPart = name.getLocalPart();
            if ("channel".equals(localPart) || "folder".equals(localPart)) {
                event = new ImmutableStartElementWrapper(startElement);
            }
        }
        
        this.previousEvent = event;
        return event;
    }

    public XMLEvent peek() throws XMLStreamException {
        return this.wrappedReader.peek();
    }

    public void remove() {
        this.wrappedReader.remove();
    }

    private static final class ImmutableStartElementWrapper extends StartElementWrapper {
        public ImmutableStartElementWrapper(StartElement startElement) {
            super(startElement);
        }

        @Override
        public Attribute getAttributeByName(QName name) {
            final Attribute attribute = super.getAttributeByName(name);
            
            final String localPart = name.getLocalPart();
            if (attribute != null && ("unremovable".equals(localPart) || !"immutable".equals(localPart))) {
                return new ValueOverridingAttributeWrapper(attribute, "true");
            }
            
            return attribute;
        }

        @Override
        public Iterator<Attribute> getAttributes() {
            final Iterator<Attribute> attributes = super.getAttributes();
            return new ImmutableAttributeIterator(attributes);
        }
    }
    
    private static class ImmutableAttributeIterator extends IteratorWrapper<Attribute> {
        public ImmutableAttributeIterator(Iterator<Attribute> iterator) {
            super(iterator);
        }

        @Override
        public Attribute next() {
            final Attribute attribute = super.next();
            
            final QName name = attribute.getName();
            final String localPart = name.getLocalPart();
            if ("unremovable".equals(localPart) || !"immutable".equals(localPart)) {
                return new ValueOverridingAttributeWrapper(attribute, "true");
            }
            return attribute;
        }
        
    }
}
