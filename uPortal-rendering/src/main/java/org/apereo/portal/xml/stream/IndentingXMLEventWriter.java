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

import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.lang.StringUtils;

/** Adds indentation to an {@link XMLEventWriter} */
public class IndentingXMLEventWriter extends EventWriterDelegate {
    public static final String NEW_LINE = "\n";
    private static final ConcurrentMap<Integer, String> indentCache =
            new ConcurrentHashMap<Integer, String>();

    private enum StackState {
        WROTE_MARKUP,
        WROTE_DATA
    }

    private final XMLEventFactory xmlEventFactory;
    private int indentSize = 2;

    private final Deque<Set<StackState>> scopeState = new LinkedList<Set<StackState>>();
    private int depth = 0; // document scope

    public IndentingXMLEventWriter(XMLEventWriter out) {
        super(out);
        xmlEventFactory = XMLEventFactory.newFactory();
        scopeState.add(EnumSet.noneOf(StackState.class));
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    /** @return System.getProperty("line.separator") or {@link #NEW_LINE} if that fails. */
    public static String getLineSeparator() {
        try {
            return System.getProperty("line.separator");
        } catch (SecurityException ignored) {
        }
        return NEW_LINE;
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        switch (event.getEventType()) {
            case XMLStreamConstants.CHARACTERS:
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.SPACE:
                {
                    wrappedWriter.add(event);
                    afterData();
                    return;
                }
            case XMLStreamConstants.START_ELEMENT:
                {
                    beforeStartElement();
                    wrappedWriter.add(event);
                    afterStartElement();
                    return;
                }

            case XMLStreamConstants.END_ELEMENT:
                {
                    beforeEndElement();
                    wrappedWriter.add(event);
                    afterEndElement();
                    return;
                }
            case XMLStreamConstants.START_DOCUMENT:
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
            case XMLStreamConstants.COMMENT:
            case XMLStreamConstants.DTD:
                {
                    beforeMarkup();
                    wrappedWriter.add(event);
                    afterMarkup();
                    return;
                }
            case XMLStreamConstants.END_DOCUMENT:
                {
                    wrappedWriter.add(event);
                    afterEndDocument();
                    break;
                }
            default:
                {
                    wrappedWriter.add(event);
                    return;
                }
        }
    }

    /** Prepare to write markup, by writing a new line and indentation. */
    protected void beforeMarkup() {
        final Set<StackState> state = scopeState.getFirst();
        if (!state.contains(StackState.WROTE_DATA) && (depth > 0 || !state.isEmpty())) {
            final String indent = getIndent(this.depth, this.indentSize);
            final Characters indentEvent = xmlEventFactory.createCharacters(indent);
            try {
                wrappedWriter.add(indentEvent);
            } catch (XMLStreamException e) {
                //Ignore exceptions caused by indentation
            }
            afterMarkup(); // indentation was written
        }
    }

    /** Note that markup or indentation was written. */
    protected void afterMarkup() {
        final Set<StackState> state = scopeState.getFirst();
        state.add(StackState.WROTE_MARKUP);
    }

    /** Note that data were written. */
    protected void afterData() {
        final Set<StackState> state = scopeState.getFirst();
        state.add(StackState.WROTE_DATA);
    }

    /** Prepare to start an element, by allocating stack space. */
    protected void beforeStartElement() {
        beforeMarkup();
    }

    /** Note that an element was started. */
    protected void afterStartElement() {
        afterMarkup();
        ++depth;
        scopeState.push(EnumSet.noneOf(StackState.class));
    }

    /** Prepare to end an element, by writing a new line and indentation. */
    protected void beforeEndElement() {
        final Set<StackState> state = scopeState.getFirst();
        // but not data
        if (depth > 0
                && state.contains(StackState.WROTE_MARKUP)
                && !state.contains(StackState.WROTE_DATA)) {
            final String indent = this.getIndent(depth - 1, indentSize);
            final Characters indentEvent = xmlEventFactory.createCharacters(indent);
            try {
                wrappedWriter.add(indentEvent);
            } catch (XMLStreamException e) {
                //Ignore exceptions caused by indentation
            }
        }
    }

    /** Note that an element was ended. */
    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
            scopeState.pop();
        }
    }

    /** Note that a document was ended. */
    protected void afterEndDocument() {
        depth = 0;
        final Set<StackState> state = scopeState.getFirst();
        if (state.contains(StackState.WROTE_MARKUP)
                && !state.contains(StackState.WROTE_DATA)) { // but not data
            try {
                final String indent = getLineSeparator() + StringUtils.repeat("  ", 0);
                final Characters indentEvent = xmlEventFactory.createCharacters(indent);
                wrappedWriter.add(indentEvent);
            } catch (Exception ignored) {
            }
        }
        scopeState.clear();
        scopeState.push(EnumSet.noneOf(StackState.class)); // start fresh
    }

    /** Generate an indentation string for the specified depth and indent size */
    protected String getIndent(int depth, int size) {
        final int length = depth * size;
        String indent = indentCache.get(length);
        if (indent == null) {
            indent = getLineSeparator() + StringUtils.repeat(" ", length);
            indentCache.put(length, indent);
        }
        return indent;
    }
}
