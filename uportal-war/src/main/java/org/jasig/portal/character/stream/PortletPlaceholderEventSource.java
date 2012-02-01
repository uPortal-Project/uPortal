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

import java.util.List;
import java.util.regex.MatchResult;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for create {@link CharacterEvent}s that target a portlet based on either
 * an {@link XMLEvent} or {@link MatchResult}. Takes care of parsing out the {@link IPortletWindowId}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class PortletPlaceholderEventSource extends BasePlaceholderEventSource {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private IPortletWindowRegistry portletWindowRegistry;
    private int portletIdGroup = 1;
    
    /**
     * The {@link MatchResult} group index that contains the portlet subscribe ID. Defaults to 1
     */
    public void setPortletIdGroup(int portletIdGroup) {
        this.portletIdGroup = portletIdGroup;
    }
    
    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(javax.xml.stream.XMLEventReader, javax.xml.stream.events.StartElement)
     */
    @Override
    public final List<CharacterEvent> getCharacterEvents(HttpServletRequest request, XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        final Tuple<IPortletWindow, StartElement> portletWindowAndElement = this.portletWindowRegistry.getPortletWindow(request, event);
        if (portletWindowAndElement == null) {
            this.logger.warn("Could not find IPortletWindow for StartElement " + event + ". No PortletPlaceholderEvent will be generated. " + event.getLocation());
            return null;
        }

        final IPortletWindowId portletWindowId = portletWindowAndElement.first.getPortletWindowId();
        return this.getCharacterEvents(portletWindowId, eventReader, event);
    }
    
    /**
     * Implement to generate CharacterEvents based on a {@link StartElement} match. Allows the
     * implementation to read the contents of the placeholder element from the provided {@link XMLEventReader} if
     * it needs additional info. 
     * 
     * Default impl simply calls {@link #getCharacterEvents(IPortletWindowId, MatchResult)} then {@link #readToEndElement(XMLEventReader, StartElement)}
     */
    protected List<CharacterEvent> getCharacterEvents(IPortletWindowId portletWindowId, XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        final List<CharacterEvent> characterEvents = this.getCharacterEvents(portletWindowId, event);
        this.readToEndElement(eventReader, event);
        return characterEvents;
    }
    
    /**
     * Implement to generate CharacterEvents based on a {@link StartElement} match. If not implemented throws 
     * UnsupportedOperationException
     */
    @SuppressWarnings("unused")
    protected List<CharacterEvent> getCharacterEvents(IPortletWindowId portletWindowId, StartElement event) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(java.util.regex.MatchResult)
     */
    @Override
    public final List<CharacterEvent> getCharacterEvents(HttpServletRequest request, MatchResult matchResult) {
        final String subscribeId = matchResult.group(this.portletIdGroup);
        if (subscribeId == null) {
            this.logger.warn("MatchResult returned null for group " + this.portletIdGroup + ". No PortletPlaceholderEvent will be generated. " + matchResult);
            return null;
        }
        
        final IPortletWindow portletWindow = this.portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(request, subscribeId);
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        
        return this.getCharacterEvents(portletWindowId, matchResult);
    }
    
    /**
     * Implement to generate CharacterEvents based on a regular expression match. If not implemented throws 
     * UnsupportedOperationException
     */
    protected List<CharacterEvent> getCharacterEvents(IPortletWindowId portletWindowId, MatchResult matchResult) {
        throw new UnsupportedOperationException();
    }
}
