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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEventImpl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletContentPlaceholderEventSource implements CharacterEventSource {
    private static final QName ID_ATTRIBUTE_NAME = new QName("id");
    
    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(javax.xml.stream.XMLEventReader, javax.xml.stream.events.StartElement)
     */
    @Override
    public List<CharacterEvent> getCharacterEvents(XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        final Attribute idAttribute = event.getAttributeByName(ID_ATTRIBUTE_NAME);
        final String idString = idAttribute.getValue();
        final int id = Integer.parseInt(idString);

        //Read the stream to remove the endElement event
        final XMLEvent nextEvent = eventReader.nextEvent();
        if (!nextEvent.isEndElement()) {
            throw new XMLStreamException(event.getName() +" element must be empty");
        }

        return Arrays.asList((CharacterEvent)new PortletContentPlaceholderEventImpl(id));
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(java.util.regex.MatchResult)
     */
    @Override
    public List<CharacterEvent> getCharacterEvents(MatchResult matchResult) {
        throw new UnsupportedOperationException();
    }

}
