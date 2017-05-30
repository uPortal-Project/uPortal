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
package org.apereo.portal.character.stream;

import java.util.NoSuchElementException;
import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 * Base class for application event readers that simply want to filter or monitor events passing
 * through the reader.
 *
 */
public abstract class FilteringCharacterEventReader extends CharacterEventReaderDelegate {
    private CharacterEvent peekedEvent = null;

    public FilteringCharacterEventReader(CharacterEventReader delegate) {
        super(delegate);
    }

    @Override
    public boolean hasNext() {
        try {
            return peekedEvent != null || (super.hasNext() && this.peek() != null);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public CharacterEvent next() {
        return internalNext(false);
    }

    @Override
    public CharacterEvent peek() {
        if (peekedEvent != null) {
            return peekedEvent;
        }

        peekedEvent = internalNext(true);
        return peekedEvent;
    }

    protected final CharacterEvent internalNext(boolean peek) {
        CharacterEvent event = null;

        if (peekedEvent != null) {
            event = peekedEvent;
            peekedEvent = null;
            return event;
        }

        do {
            event = super.next();
            event = this.filterEvent(event, peek);
        } while (event == null);

        return event;
    }

    /**
     * @param event The current event
     * @param peek If the event is from a {@link #peek()} call
     * @return The event to return, if null is returned the event is dropped from the stream and the
     *     next event will be used.
     */
    protected abstract CharacterEvent filterEvent(CharacterEvent event, boolean peek);
}
