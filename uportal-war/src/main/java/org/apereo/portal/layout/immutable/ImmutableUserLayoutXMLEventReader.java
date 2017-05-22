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
package org.apereo.portal.layout.immutable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apereo.portal.xml.stream.FilteringXMLEventReader;
import org.apereo.portal.xml.stream.events.StartElementWrapper;

/**
 */
public class ImmutableUserLayoutXMLEventReader extends FilteringXMLEventReader {
    private static final XMLEventFactory EVENT_FACTORY = XMLEventFactory.newFactory();

    public ImmutableUserLayoutXMLEventReader(XMLEventReader wrappedReader) {
        super(wrappedReader);
    }

    @Override
    protected XMLEvent filterEvent(XMLEvent event, boolean peek) {
        if (event.isStartElement()) {
            final StartElement startElement = event.asStartElement();

            final QName name = startElement.getName();

            final String localPart = name.getLocalPart();
            if ("channel".equals(localPart) || "folder".equals(localPart)) {
                return new ImmutableStartElementWrapper(startElement, "folder".equals(localPart));
            }
        }

        return event;
    }

    private static final class ImmutableStartElementWrapper extends StartElementWrapper {
        private static final String dlmNamespaceURI = "http://www.uportal.org/layout/dlm";
        private static final String dlmPrefix = "dlm";

        private final boolean isFolder;

        public ImmutableStartElementWrapper(StartElement startElement, boolean isFolder) {
            super(startElement);
            this.isFolder = isFolder;
        }

        @Override
        public Attribute getAttributeByName(QName name) {
            final String localPart = name.getLocalPart();
            if ("unremovable".equals(localPart) || "immutable".equals(localPart)) {
                return EVENT_FACTORY.createAttribute(name, "true");
            }
            if ("deleteAllowed".equals(localPart)
                    || "editAllowed".equals(localPart)
                    || "moveAllowed".equals(localPart)
                    || "addChildAllowed".equals(localPart)) {
                return EVENT_FACTORY.createAttribute(name, "false");
            }

            return super.getAttributeByName(name);
        }

        @Override
        public Iterator<Attribute> getAttributes() {
            final Map<QName, Attribute> attributes = new LinkedHashMap<QName, Attribute>();

            for (final Iterator<Attribute> attributeItr = super.getAttributes();
                    attributeItr.hasNext();
                    ) {
                final Attribute attribute = attributeItr.next();
                attributes.put(attribute.getName(), attribute);
            }

            final QName immutableName = new QName("immutable");
            final Attribute immutableAttribute =
                    EVENT_FACTORY.createAttribute(immutableName, "true");
            attributes.put(immutableName, immutableAttribute);

            final QName unremovableName = new QName("unremovable");
            final Attribute unremovableAttribute =
                    EVENT_FACTORY.createAttribute(unremovableName, "true");
            attributes.put(unremovableName, unremovableAttribute);

            final QName dlmDeleteName = new QName(dlmNamespaceURI, "deleteAllowed", dlmPrefix);
            final Attribute dlmDeleteNameAttribute =
                    EVENT_FACTORY.createAttribute(dlmDeleteName, "false");
            attributes.put(dlmDeleteName, dlmDeleteNameAttribute);

            final QName dlmMoveName = new QName(dlmNamespaceURI, "moveAllowed", dlmPrefix);
            final Attribute dlmMoveNameAttribute =
                    EVENT_FACTORY.createAttribute(dlmMoveName, "false");
            attributes.put(dlmMoveName, dlmMoveNameAttribute);

            if (isFolder) {
                final QName dlmEditName = new QName(dlmNamespaceURI, "editAllowed", dlmPrefix);
                final Attribute dlmEditNameAttribute =
                        EVENT_FACTORY.createAttribute(dlmEditName, "false");
                attributes.put(dlmEditName, dlmEditNameAttribute);

                final QName dlmAddChildName =
                        new QName(dlmNamespaceURI, "addChildAllowed", dlmPrefix);
                final Attribute dlmAddChildNameAttribute =
                        EVENT_FACTORY.createAttribute(dlmAddChildName, "false");
                attributes.put(dlmAddChildName, dlmAddChildNameAttribute);
            }

            return Collections.unmodifiableCollection(attributes.values()).iterator();
        }
    }
}
