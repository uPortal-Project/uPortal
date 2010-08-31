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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEventImpl;

/**
 * Generates a {@link PortletContentPlaceholderEvent} for a {@link StartElement} event. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletContentPlaceholderEventSource extends PortletPlaceholderEventSource {
    
    @Override
    protected List<CharacterEvent> getCharacterEvents(String subscribeId, XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        //Read the stream to remove the endElement event
        final XMLEvent nextEvent = eventReader.nextEvent();
        if (!nextEvent.isEndElement()) {
            throw new XMLStreamException(event.getName() + " element must be empty");
        }

        return Arrays.asList((CharacterEvent)new PortletContentPlaceholderEventImpl(subscribeId));
    }
}
