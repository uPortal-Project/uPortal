/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import junit.framework.TestCase;

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
import org.jasig.portal.utils.cache.CacheKey;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StAXSerializingComponentTest extends TestCase {

    public void testSerializing() throws Exception {
        final StAXSerializingComponent staxSerializingComponent = new StAXSerializingComponent();
        
        //Setup a simple pass-through parent
        staxSerializingComponent.setParentComponent(new SimpleStAXSource());
        
        final Map<QName, CharacterEventSource> chunkingElements = new LinkedHashMap<QName, CharacterEventSource>();
        chunkingElements.put(new QName("portlet"), new PortletContentPlaceholderEventSource());
        chunkingElements.put(new QName("portlet-header"), new PortletHeaderPlaceholderEventSource());
        staxSerializingComponent.setChunkingElements(chunkingElements);
        
        final Map<String, CharacterEventSource> chunkingPatterns = new LinkedHashMap<String, CharacterEventSource>();
        chunkingPatterns.put("\\{up-portlet-title\\(([^\\)]+)\\)\\}", new PortletTitlePlaceholderEventSource());
        chunkingPatterns.put("\\{up-portlet-help\\(([^\\)]+)\\)\\}", new PortletHelpPlaceholderEventSource());
        staxSerializingComponent.setChunkingPatterns(chunkingPatterns);
        
        final PipelineEventReader<CharacterEventReader, CharacterEvent> eventReader = staxSerializingComponent.getEventReader(null, null);
        
        //Expected events structure, leaving the data out to make it at least a little simpler
        final List<Class<? extends CharacterEvent>> expectedEvents = new ArrayList<Class<? extends CharacterEvent>>();
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHeaderPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletTitlePlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletTitlePlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletHelpPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletTitlePlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);
        expectedEvents.add(PortletContentPlaceholderEvent.class);
        expectedEvents.add(CharacterDataEvent.class);

        
        final Iterator<CharacterEvent> eventItr = eventReader.iterator();
        final Iterator<Class<? extends CharacterEvent>> expectedEventTypeItr = expectedEvents.iterator();
        
        int eventCount = 0;
        while (expectedEventTypeItr.hasNext()) {
            eventCount++;
            assertTrue("The number of events returned by the eventReader less than the expected event count of: " + expectedEvents.size() + " was: " + eventCount, eventItr.hasNext());
            
            final Class<? extends CharacterEvent> expectedEventType = expectedEventTypeItr.next();
            final CharacterEvent event = eventItr.next();
            assertNotNull("Event number " + eventCount + " is null", event);
            
            final Class<? extends CharacterEvent> eventType = event.getClass();
            assertTrue("Event " + eventType.getName() + " at index " + eventCount + " is not assignable to expected event type: " + expectedEventType.getName(), expectedEventType.isAssignableFrom(eventType));
        }
        
        assertFalse("The number of events returned by the eventReader is more than the expected event count of: " + expectedEvents.size(), eventItr.hasNext());
    }
    
    private static final class SimpleStAXSource implements StAXPipelineComponent {
        @Override
        public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
            return new CacheKey(1);
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
