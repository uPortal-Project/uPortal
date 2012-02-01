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

package org.jasig.portal.rendering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.CharacterEventSource;
import org.jasig.portal.character.stream.PortletContentPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletHeaderPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletHelpPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletTitlePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterDataEvent;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletHeaderPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletHelpPlaceholderEvent;
import org.jasig.portal.character.stream.events.PortletTitlePlaceholderEvent;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.XmlUtilities;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StAXSerializingComponentTest {

    @Test
    public void testSerializing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final XmlUtilities xmlUtilities = mock(XmlUtilities.class);
        final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
        
        when(xmlUtilities.getHtmlOutputFactory()).thenReturn(XMLOutputFactory.newFactory());
        
        final StAXSerializingComponent staxSerializingComponent = new StAXSerializingComponent();
        
        //Setup a simple pass-through parent
        staxSerializingComponent.setWrappedComponent(new SimpleStAXSource());
        staxSerializingComponent.setXmlUtilities(xmlUtilities);

        final IPortletWindow portletWindow = mock(IPortletWindow.class);
        when(portletWindowRegistry.getPortletWindow(Matchers.eq(request), Matchers.any(StartElement.class))).thenReturn(new Tuple<IPortletWindow, StartElement>(portletWindow, null));
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(Matchers.eq(request), Matchers.anyString())).thenReturn(portletWindow);
        
        final PortletContentPlaceholderEventSource contentPlaceholderEventSource = new PortletContentPlaceholderEventSource();
        contentPlaceholderEventSource.setPortletWindowRegistry(portletWindowRegistry);

        final PortletHeaderPlaceholderEventSource headerPlaceholderEventSource = new PortletHeaderPlaceholderEventSource();
        headerPlaceholderEventSource.setPortletWindowRegistry(portletWindowRegistry);

        final PortletTitlePlaceholderEventSource portletTitlePlaceholderEventSource = new PortletTitlePlaceholderEventSource();
        portletTitlePlaceholderEventSource.setPortletWindowRegistry(portletWindowRegistry);
        
        final PortletHelpPlaceholderEventSource portletHelpPlaceholderEventSource = new PortletHelpPlaceholderEventSource();
        portletHelpPlaceholderEventSource.setPortletWindowRegistry(portletWindowRegistry);
        
        
        
        final Map<String, CharacterEventSource> chunkingElements = new LinkedHashMap<String, CharacterEventSource>();
        chunkingElements.put("portlet", contentPlaceholderEventSource);
        chunkingElements.put("portlet-header", headerPlaceholderEventSource);
        staxSerializingComponent.setChunkingElements(chunkingElements);
        
        final Map<String, CharacterEventSource> chunkingPatterns = new LinkedHashMap<String, CharacterEventSource>();
        chunkingPatterns.put("\\{up-portlet-title\\(([^\\)]+)\\)\\}", portletTitlePlaceholderEventSource);
        chunkingPatterns.put("\\{up-portlet-help\\(([^\\)]+)\\)\\}", portletHelpPlaceholderEventSource);
        staxSerializingComponent.setChunkingPatterns(chunkingPatterns);
        
        final PipelineEventReader<CharacterEventReader, CharacterEvent> eventReader = staxSerializingComponent.getEventReader(request, response);
        
        //Expected events structure, leaving the data out to make it at least a little simpler
        final List<Class<? extends CharacterEvent>> whenedEvents = new ArrayList<Class<? extends CharacterEvent>>();
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHeaderPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletTitlePlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletTitlePlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletHelpPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletTitlePlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);
        whenedEvents.add(PortletContentPlaceholderEvent.class);
        whenedEvents.add(CharacterDataEvent.class);

        
        final Iterator<CharacterEvent> eventItr = eventReader.iterator();
        final Iterator<Class<? extends CharacterEvent>> whenedEventTypeItr = whenedEvents.iterator();
        
        int eventCount = 0;
        while (whenedEventTypeItr.hasNext()) {
            eventCount++;
            assertTrue("The number of events returned by the eventReader less than the whened event count of: " + whenedEvents.size() + " was: " + eventCount, eventItr.hasNext());
            
            final Class<? extends CharacterEvent> whenedEventType = whenedEventTypeItr.next();
            final CharacterEvent event = eventItr.next();
            assertNotNull("Event number " + eventCount + " is null", event);
            
            final Class<? extends CharacterEvent> eventType = event.getClass();
            assertTrue("Event " + eventType.getName() + " at index " + eventCount + " is not assignable to whened event type: " + whenedEventType.getName(), whenedEventType.isAssignableFrom(eventType));
        }
        
        assertFalse("The number of events returned by the eventReader is more than the whened event count of: " + whenedEvents.size(), eventItr.hasNext());
    }
    
    private static final class SimpleStAXSource implements StAXPipelineComponent {
        @Override
        public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
            return new CacheKey("SimpleStAXSource", 1);
        }

        @Override
        public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            final XMLEventReader xmlEventReader;
            try {
                xmlEventReader = inputFactory.createXMLEventReader(this.getClass().getResourceAsStream("chunkingTest.xml"));
            }
            catch (XMLStreamException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(xmlEventReader);
        }
    }
}
