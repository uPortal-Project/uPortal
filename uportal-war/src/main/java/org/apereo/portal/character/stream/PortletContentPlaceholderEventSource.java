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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.character.stream.events.PortletContentPlaceholderEvent;
import org.apereo.portal.character.stream.events.PortletContentPlaceholderEventImpl;
import org.apereo.portal.portlet.om.IPortletWindowId;

/**
 * Generates a {@link PortletContentPlaceholderEvent} for a {@link StartElement} event.
 *
 */
public class PortletContentPlaceholderEventSource extends PortletPlaceholderEventSource {

    @Override
    protected void generateCharacterEvents(
            IPortletWindowId portletWindowId,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException {
        eventBuffer.add(new PortletContentPlaceholderEventImpl(portletWindowId));
    }
}
