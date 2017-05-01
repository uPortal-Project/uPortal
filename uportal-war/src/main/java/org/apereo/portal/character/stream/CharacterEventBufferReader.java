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

import java.util.List;
import java.util.ListIterator;
import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 * Creates a {@link CharacterEventReader} that wraps a {@link List} of {@link CharacterEvent}s
 *
 */
public class CharacterEventBufferReader implements CharacterEventReader {
    private final ListIterator<CharacterEvent> eventBuffer;

    public CharacterEventBufferReader(ListIterator<CharacterEvent> eventBuffer) {
        this.eventBuffer = eventBuffer;
    }

    @Override
    public void close() {
        //NO-OP
    }

    @Override
    public CharacterEvent peek() {
        final CharacterEvent event = this.eventBuffer.next();
        //Step back by one in the list
        this.eventBuffer.previous();

        return event;
    }

    @Override
    public boolean hasNext() {
        return this.eventBuffer.hasNext();
    }

    @Override
    public CharacterEvent next() {
        return this.eventBuffer.next();
    }

    @Override
    public void remove() {
        this.eventBuffer.remove();
    }
}
