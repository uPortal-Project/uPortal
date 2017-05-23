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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.EventReaderDelegate;

/** Wish this was provided by the JDK like {@link EventReaderDelegate} */
public abstract class EventWriterDelegate implements XMLEventWriter {

    /** The downstream writer, to which events are delegated. */
    protected final XMLEventWriter wrappedWriter;

    protected EventWriterDelegate(XMLEventWriter out) {
        this.wrappedWriter = out;
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        wrappedWriter.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return wrappedWriter.getNamespaceContext();
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        wrappedWriter.setDefaultNamespace(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        wrappedWriter.setPrefix(prefix, uri);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return wrappedWriter.getPrefix(uri);
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        wrappedWriter.add(event);
    }

    /** Add events from the given reader, one by one. */
    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    @Override
    public void flush() throws XMLStreamException {
        wrappedWriter.flush();
    }

    @Override
    public void close() throws XMLStreamException {
        wrappedWriter.close();
    }
}
