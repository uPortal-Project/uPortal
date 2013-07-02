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
package org.jasig.portal.character.stream;

import java.util.Deque;
import java.util.LinkedList;

import org.jasig.portal.character.stream.events.CharacterEvent;

public abstract class InjectingCharacterEventReader extends CharacterEventReaderDelegate {
    private Deque<CharacterEvent> additionalEvents;

    public InjectingCharacterEventReader(CharacterEventReader delegate) {
        super(delegate);
    }

    @Override
    public CharacterEvent next() {
        if (this.additionalEvents != null && !this.additionalEvents.isEmpty()) {
            return this.additionalEvents.pop();
        }
        
        final CharacterEvent event = this.getParent().next();
        this.additionalEvents = this.getAdditionalEvents(event);
        if (this.additionalEvents != null) {
            //Stick the current event at the bottom of the deque so it isn't forgotten
            this.additionalEvents.offer(event);
            return this.additionalEvents.pop();
        }
        
        return event;
    }

    @Override
    public final CharacterEvent peek() {
        if (this.additionalEvents != null && !this.additionalEvents.isEmpty()) {
            return this.additionalEvents.peek();
        }
        
        final CharacterEvent event = this.getParent().peek();
        final CharacterEvent peekEvent = this.getPeekEvent(event);
        if (peekEvent != null) {
            return peekEvent;
        }
        
        return event;
    }
    
    @Override
    public boolean hasNext() {
        return super.hasNext() || (this.additionalEvents != null && !this.additionalEvents.isEmpty());
    }

    /**
     * The Deque with the additional events WILL BE MODIFIED by the calling code.
     * 
     * @param event The current event
     * @return Any additional events that should be injected before the current event. If null the current event is returned
     */
    protected Deque<CharacterEvent> getAdditionalEvents(CharacterEvent event) {
        final CharacterEvent additionalEvent = this.getAdditionalEvent(event);
        if (additionalEvent == null) {
            return null;
        }
        
        final Deque<CharacterEvent> additionalEvents = new LinkedList<CharacterEvent>();
        additionalEvents.push(additionalEvent);
        return additionalEvents;
    }
    
    /**
     * Called by {@link #getAdditionalEvents(CharacterEvent)} and then wrapped with a {@link Deque}. If there is a need to
     * inject more than a single event override {@link #getAdditionalEvents(CharacterEvent)}
     */
    protected CharacterEvent getAdditionalEvent(CharacterEvent event) {
        throw new UnsupportedOperationException("Either 'Deque<CharacterEvent> getAdditionalEvents(CharacterEvent event)' or 'CharacterEvent getAdditionalEvent(CharacterEvent event must be implemented");
    }
    
    /**
     * @param event The peeked event
     * @return An event to return in place of the peeked event, if null the peeked event is returned.
     */
    protected abstract CharacterEvent getPeekEvent(CharacterEvent event);
}
