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

import java.util.Collection;
import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.BasePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterEvent;


public class JsonLayoutPlaceholderEventSource extends BasePlaceholderEventSource {
    
    @Override
    protected void generateCharacterEvents(HttpServletRequest servletRequest, StartElement event,
            Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(JsonLayoutPlaceholderEventImpl.INSTANCE);
    }

    @Override
    public void generateCharacterEvents(HttpServletRequest servletRequest, MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer) {
        eventBuffer.add(JsonLayoutPlaceholderEventImpl.INSTANCE);
    }
}
