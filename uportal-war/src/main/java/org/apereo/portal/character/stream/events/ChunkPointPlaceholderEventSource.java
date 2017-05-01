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
package org.apereo.portal.character.stream.events;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;
import org.apereo.portal.character.stream.BasePlaceholderEventSource;
import org.apereo.portal.rendering.StAXSerializingComponent;
import org.apereo.portal.xml.stream.ChunkingEventReader;

/**
 * Returns an empty string event. The chunk-point element and this source are used to break up the
 * character stream created by the {@link StAXSerializingComponent} and {@link ChunkingEventReader}.
 * The <chunk-point/> element should be placed before and after sections of markup that are very
 * user or page specific (username, page title, session key, etc.). This allows the string
 * de-duplication logic of the {@link CharacterDataEventImpl} to be more effective as more users
 * will have common character event strings in their rendering pipeline.
 *
 */
public class ChunkPointPlaceholderEventSource extends BasePlaceholderEventSource {
    /** Represents <chunk-point> layout element */
    public static final String CHUNK_POINT = "chunk-point";

    @Override
    protected void generateCharacterEvents(
            HttpServletRequest servletRequest,
            StartElement event,
            Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(CharacterDataEventImpl.EMPTY_CHARACTER_DATA);
    }
}
