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

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletTitlePlaceholderEventImpl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletTitlePlaceholderEventSource implements CharacterEventSource {
    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(javax.xml.stream.XMLEventReader, javax.xml.stream.events.StartElement)
     */
    @Override
    public List<CharacterEvent> getCharacterEvents(XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(java.util.regex.MatchResult)
     */
    @Override
    public List<CharacterEvent> getCharacterEvents(MatchResult matchResult) {
        final String idString = matchResult.group(1);
        final int id = Integer.parseInt(idString);

        return Arrays.asList((CharacterEvent)new PortletTitlePlaceholderEventImpl(id));
        
    }

}
