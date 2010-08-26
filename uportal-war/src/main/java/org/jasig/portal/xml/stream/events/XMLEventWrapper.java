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

package org.jasig.portal.xml.stream.events;

import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Wraps an {@link XMLEvent} delegating all method calls to it
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XMLEventWrapper implements XMLEvent {
    private final XMLEvent event;
    
    public XMLEventWrapper(XMLEvent event) {
        this.event = event;
    }

    public Characters asCharacters() {
        return this.event.asCharacters();
    }

    public EndElement asEndElement() {
        return this.event.asEndElement();
    }

    public StartElement asStartElement() {
        return this.event.asStartElement();
    }

    public int getEventType() {
        return this.event.getEventType();
    }

    public Location getLocation() {
        return this.event.getLocation();
    }

    public QName getSchemaType() {
        return this.event.getSchemaType();
    }

    public boolean isAttribute() {
        return this.event.isAttribute();
    }

    public boolean isCharacters() {
        return this.event.isCharacters();
    }

    public boolean isEndDocument() {
        return this.event.isEndDocument();
    }

    public boolean isEndElement() {
        return this.event.isEndElement();
    }

    public boolean isEntityReference() {
        return this.event.isEntityReference();
    }

    public boolean isNamespace() {
        return this.event.isNamespace();
    }

    public boolean isProcessingInstruction() {
        return this.event.isProcessingInstruction();
    }

    public boolean isStartDocument() {
        return this.event.isStartDocument();
    }

    public boolean isStartElement() {
        return this.event.isStartElement();
    }

    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        this.event.writeAsEncodedUnicode(writer);
    }

    @Override
    public boolean equals(Object obj) {
        return this.event.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.event.hashCode();
    }

    @Override
    public String toString() {
        return this.event.toString();
    }
}
