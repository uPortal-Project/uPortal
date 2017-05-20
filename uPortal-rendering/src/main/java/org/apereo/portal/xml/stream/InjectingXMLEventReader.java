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
import java.util.LinkedList;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Base class for {@link XMLEventReader}s for classes that want to inject additional events into the
 * stream.
 *
 */
public abstract class InjectingXMLEventReader extends BaseXMLEventReader {
    private Deque<XMLEvent> additionalEvents;

    public InjectingXMLEventReader(XMLEventReader reader) {
        super(reader);
    }

    @Override
    protected final XMLEvent internalNextEvent() throws XMLStreamException {
        if (this.additionalEvents != null && !this.additionalEvents.isEmpty()) {
            return this.additionalEvents.pop();
        }

        final XMLEvent event = this.getParent().nextEvent();
        this.additionalEvents = this.getAdditionalEvents(event);
        return event;
    }

    @Override
    public final XMLEvent peek() throws XMLStreamException {
        if (this.additionalEvents != null && !this.additionalEvents.isEmpty()) {
            return this.additionalEvents.peek();
        }

        final XMLEvent event = this.getParent().peek();
        final XMLEvent peekEvent = this.getPeekEvent(event);
        if (peekEvent != null) {
            return peekEvent;
        }

        return event;
    }

    @Override
    public boolean hasNext() {
        return super.hasNext()
                || (this.additionalEvents != null && !this.additionalEvents.isEmpty());
    }

    /**
     * The Deque with the additional events WILL BE MODIFIED by the calling code.
     *
     * @param event The current event
     * @return Any additional events that should be injected before the current event. If null the
     *     current event is returned
     */
    protected Deque<XMLEvent> getAdditionalEvents(XMLEvent event) {
        final XMLEvent additionalEvent = this.getAdditionalEvent(event);
        if (additionalEvent == null) {
            return null;
        }

        final Deque<XMLEvent> additionalEvents = new LinkedList<XMLEvent>();
        additionalEvents.push(additionalEvent);
        return additionalEvents;
    }

    /**
     * Called by {@link #getAdditionalEvents(XMLEvent)} and then wrapped with a {@link Deque}. If
     * there is a need to inject more than a single event override {@link
     * #getAdditionalEvents(XMLEvent)}
     */
    protected XMLEvent getAdditionalEvent(XMLEvent event) {
        throw new UnsupportedOperationException(
                "Either 'Deque<XMLEvent> getAdditionalEvents(XMLEvent event)' or 'XMLEvent getAdditionalEvent(XMLEvent event must be implemented");
    }

    /**
     * @param event The peeked event
     * @return An event to return in place of the peeked event, if null the peeked event is
     *     returned.
     */
    protected abstract XMLEvent getPeekEvent(XMLEvent event);
}
