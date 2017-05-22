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
package org.apereo.portal.xml.stream.events;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

/**
 */
public class StartElementWrapper extends XMLEventWrapper implements StartElement {
    private final StartElement startElement;

    public StartElementWrapper(StartElement startElement) {
        super(startElement);
        this.startElement = startElement;
    }

    @Override
    public StartElement asStartElement() {
        return this;
    }

    @Override
    public Attribute getAttributeByName(QName name) {
        return this.startElement.getAttributeByName(name);
    }

    @Override
    public Iterator<Attribute> getAttributes() {
        return this.startElement.getAttributes();
    }

    @Override
    public QName getName() {
        return this.startElement.getName();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.startElement.getNamespaceContext();
    }

    @Override
    public Iterator<Namespace> getNamespaces() {
        return this.startElement.getNamespaces();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return this.startElement.getNamespaceURI(prefix);
    }
}
