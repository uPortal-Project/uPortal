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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Caches events written to a List
 *
 */
public class XMLEventBufferWriter implements XMLEventWriter {
    private final Map<String, String> prefixes = new LinkedHashMap<String, String>();
    private final List<XMLEvent> eventBuffer = new LinkedList<XMLEvent>();
    private NamespaceContext namespaceContext;
    private String defaultNamespace;

    /** @return The buffer of events written to the writer so far */
    public List<XMLEvent> getEventBuffer() {
        return this.eventBuffer;
    }

    /** @return Prefixes configured via {@link #setPrefix(String, String)} */
    public Map<String, String> getPrefixes() {
        return this.prefixes;
    }

    /** @return The default namespace specified by {@link #setDefaultNamespace(String)} */
    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    @Override
    public void flush() throws XMLStreamException {}

    @Override
    public void close() throws XMLStreamException {}

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        this.eventBuffer.add(event);
    }

    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            this.eventBuffer.add(reader.nextEvent());
        }
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return prefixes.get(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        this.prefixes.put(uri, prefix);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        this.defaultNamespace = uri;
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.namespaceContext = context;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }
}
