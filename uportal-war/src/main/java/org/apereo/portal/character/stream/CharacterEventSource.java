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

import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import org.apereo.portal.character.stream.events.CharacterEvent;

/**
 * Generates {@link CharacterEvent} instances from matched {@link StartElement} events of {@link
 * Pattern}s
 *
 */
public interface CharacterEventSource {
    /**
     * The passed {@link StartElement} was mapped to this {@link CharacterEventSource}. Return a
     * {@link List} of {@link CharacterEvent}s based on the {@link StartElement}. The matching
     * {@link EndElement} should be read off of the {@link XMLEventReader} before returning.
     */
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            XMLEventReader eventReader,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException;

    /** The passed {@link Matcher} matches a character block. The block will be split */
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer);
}
