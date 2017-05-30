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
package org.apereo.portal.json.rendering;

import org.apereo.portal.character.stream.CharacterEventReader;
import org.apereo.portal.character.stream.FilteringCharacterEventReader;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.character.stream.events.CharacterEventTypes;

/** Removes content not wrapped by {@link CharacterEventTypes#JSON_LAYOUT} marker events */
public class JsonWrapperFilteringCharacterEventReader extends FilteringCharacterEventReader {
    private boolean inLayout = false;

    public JsonWrapperFilteringCharacterEventReader(CharacterEventReader delegate) {
        super(delegate);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.character.stream.FilteringCharacterEventReader#filterEvent(org.apereo.portal.character.stream.events.CharacterEvent, boolean)
     */
    @Override
    protected CharacterEvent filterEvent(CharacterEvent event, boolean peek) {
        if (event.getEventType() == CharacterEventTypes.JSON_LAYOUT) {
            inLayout = !inLayout;
            return null;
        }

        if (inLayout) {
            return event;
        }

        return null;
    }
}
