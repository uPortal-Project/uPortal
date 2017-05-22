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
import java.util.regex.MatchResult;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.apereo.portal.character.stream.events.CharacterEvent;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for create {@link CharacterEvent}s that target a portlet based on either an {@link
 * XMLEvent} or {@link MatchResult}. Takes care of parsing out the {@link IPortletWindowId}
 *
 */
public abstract class PortletPlaceholderEventSource extends BasePlaceholderEventSource {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IPortletWindowRegistry portletWindowRegistry;
    private int portletIdGroup = 1;

    /** The {@link MatchResult} group index that contains the portlet subscribe ID. Defaults to 1 */
    public void setPortletIdGroup(int portletIdGroup) {
        this.portletIdGroup = portletIdGroup;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            XMLEventReader eventReader,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException {
        final Tuple<IPortletWindow, StartElement> portletWindowAndElement =
                this.portletWindowRegistry.getPortletWindow(servletRequest, event);
        if (portletWindowAndElement == null) {
            this.logger.warn(
                    "Could not find IPortletWindow for StartElement "
                            + event
                            + ". No PortletPlaceholderEvent will be generated. "
                            + event.getLocation());
            return;
        }

        final IPortletWindowId portletWindowId = portletWindowAndElement.first.getPortletWindowId();
        this.generateCharacterEvents(portletWindowId, eventReader, event, eventBuffer);
    }

    /**
     * Implement to generate CharacterEvents based on a {@link StartElement} match. Allows the
     * implementation to read the contents of the placeholder element from the provided {@link
     * XMLEventReader} if it needs additional info.
     *
     * <p>Default impl simply calls {@link #getCharacterEvents(IPortletWindowId, MatchResult)} then
     * {@link #readToEndElement(XMLEventReader, StartElement)}
     */
    protected void generateCharacterEvents(
            IPortletWindowId portletWindowId,
            XMLEventReader eventReader,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException {
        this.generateCharacterEvents(portletWindowId, event, eventBuffer);
        this.readToEndElement(eventReader, event);
    }

    /**
     * Implement to generate CharacterEvents based on a {@link StartElement} match. If not
     * implemented throws UnsupportedOperationException
     */
    protected void generateCharacterEvents(
            IPortletWindowId portletWindowId,
            StartElement event,
            Collection<CharacterEvent> eventBuffer)
            throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void generateCharacterEvents(
            HttpServletRequest servletRequest,
            MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer) {
        final String subscribeId = matchResult.group(this.portletIdGroup);
        if (subscribeId == null) {
            this.logger.warn(
                    "MatchResult returned null for group "
                            + this.portletIdGroup
                            + ". No PortletPlaceholderEvent will be generated. "
                            + matchResult);
            return;
        }

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                        servletRequest, subscribeId);
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        this.generateCharacterEvents(portletWindowId, matchResult, eventBuffer);
    }

    /**
     * Implement to generate CharacterEvents based on a regular expression match. If not implemented
     * throws UnsupportedOperationException
     */
    protected void generateCharacterEvents(
            IPortletWindowId portletWindowId,
            MatchResult matchResult,
            Collection<CharacterEvent> eventBuffer) {
        throw new UnsupportedOperationException();
    }
}
