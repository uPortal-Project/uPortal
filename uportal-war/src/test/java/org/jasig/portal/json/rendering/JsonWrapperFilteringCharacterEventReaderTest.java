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

package org.jasig.portal.json.rendering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.jasig.portal.character.stream.CharacterEventBufferReader;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.CharacterEventTypes;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JsonWrapperFilteringCharacterEventReaderTest {
    @Test
    public void testFilteringEvents() {
        final CharacterEventReader baseEventReader = new CharacterEventBufferReader(Arrays.asList(
                CharacterDataEventImpl.create("<layout>"), 
                JsonLayoutPlaceholderEventImpl.INSTANCE,
                CharacterDataEventImpl.create("{'yay':json}"),
                JsonLayoutPlaceholderEventImpl.INSTANCE,
                CharacterDataEventImpl.create("</layout>")).listIterator());
        
        final JsonWrapperFilteringCharacterEventReader jsonWrapperFilteringCharacterEventReader = new JsonWrapperFilteringCharacterEventReader(baseEventReader);
        
        CharacterEvent event;
        
        assertTrue(jsonWrapperFilteringCharacterEventReader.hasNext());
        event = jsonWrapperFilteringCharacterEventReader.peek();
        assertNotNull(event);
        assertEquals(CharacterEventTypes.CHARACTER, event.getEventType());
        
        assertTrue(jsonWrapperFilteringCharacterEventReader.hasNext());
        event = jsonWrapperFilteringCharacterEventReader.peek();
        assertNotNull(event);
        assertEquals(CharacterEventTypes.CHARACTER, event.getEventType());
        
        assertTrue(jsonWrapperFilteringCharacterEventReader.hasNext());
        event = jsonWrapperFilteringCharacterEventReader.next();
        assertNotNull(event);
        assertEquals(CharacterEventTypes.CHARACTER, event.getEventType());
        
        assertFalse(jsonWrapperFilteringCharacterEventReader.hasNext());
        try {
            event = jsonWrapperFilteringCharacterEventReader.peek();
            fail();
        }
        catch (NoSuchElementException e) {
        }
        
        try {
            event = jsonWrapperFilteringCharacterEventReader.next();
            fail();
        }
        catch (NoSuchElementException e) {
        }
    }
}
