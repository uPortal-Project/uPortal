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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for create {@link CharacterEvent}s that target a portlet based on either
 * an {@link XMLEvent} or {@link MatchResult}. Takes care of parsing out the {@link IPortletWindowId}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class PortletPlaceholderEventSource implements CharacterEventSource {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private int portletIdGroup = 1;
    
    /**
     * The {@link MatchResult} group index that contains the portlet subscribe ID. Defaults to 1
     */
    public void setPortletIdGroup(int portletIdGroup) {
        this.portletIdGroup = portletIdGroup;
    }


    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(javax.xml.stream.XMLEventReader, javax.xml.stream.events.StartElement)
     */
    @Override
    public final List<CharacterEvent> getCharacterEvents(XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        final Attribute idAttribute = event.getAttributeByName(IUserLayoutManager.ID_ATTR_NAME);
        if (idAttribute == null) {
            this.logger.warn("StartElement " + event.getName() + " does not have an " + IUserLayoutManager.ID_ATTR_NAME + " Attribute. No PortletPlaceholderEvent will be generated. " + event.getLocation());
            return null;
        }
        
        final String subscribeId = idAttribute.getValue();
        
        return this.getCharacterEvents(subscribeId, eventReader, event);
    }
    
    /**
     * Implement to generate CharacterEvents based on a {@link StartElement} match. If not implemented throws 
     * UnsupportedOperationException
     */
    @SuppressWarnings("unused")
    protected List<CharacterEvent> getCharacterEvents(String subscribeId, XMLEventReader eventReader, StartElement event) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.CharacterEventSource#getCharacterEvents(java.util.regex.MatchResult)
     */
    @Override
    public final List<CharacterEvent> getCharacterEvents(MatchResult matchResult) {
        final String subscribeId = matchResult.group(this.portletIdGroup);
        if (subscribeId == null) {
            this.logger.warn("MatchResult returned null for group " + this.portletIdGroup + ". No PortletPlaceholderEvent will be generated. " + matchResult);
            return null;
        }
        
        return this.getCharacterEvents(subscribeId, matchResult);
    }
    
    /**
     * Implement to generate CharacterEvents based on a regular expression match. If not implemented throws 
     * UnsupportedOperationException
     */
    protected List<CharacterEvent> getCharacterEvents(String subscribeId, MatchResult matchResult) {
        throw new UnsupportedOperationException();
    }
}
