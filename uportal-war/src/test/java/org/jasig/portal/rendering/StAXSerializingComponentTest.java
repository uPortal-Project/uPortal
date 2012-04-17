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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import net.sf.ehcache.Ehcache;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.CharacterEventSource;
import org.jasig.portal.character.stream.PortletContentPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletHeaderPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletHelpPlaceholderEventSource;
import org.jasig.portal.character.stream.PortletTitlePlaceholderEventSource;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletContentPlaceholderEventImpl;
import org.jasig.portal.character.stream.events.PortletHeaderPlaceholderEventImpl;
import org.jasig.portal.character.stream.events.PortletHelpPlaceholderEventImpl;
import org.jasig.portal.character.stream.events.PortletTitlePlaceholderEventImpl;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.XmlUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.ImmutableList;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class StAXSerializingComponentTest {
    @InjectMocks private StAXSerializingComponent staxSerializingComponent = new StAXSerializingComponent();
    @Mock private XmlUtilities xmlUtilities;
    @Mock private Ehcache stringCache;

    @Test
    public void testSerializing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final IPortletWindowRegistry portletWindowRegistry = mock(IPortletWindowRegistry.class);
        
        when(xmlUtilities.getHtmlOutputFactory()).thenReturn(XMLOutputFactory.newFactory());
        
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
        final List<? extends CharacterEvent> expectedEvents = this.getExpectedEvents();

        
        final Iterator<CharacterEvent> eventItr = eventReader.iterator();
        final Iterator<? extends CharacterEvent> expectedEventsItr = expectedEvents.iterator();
        
        int eventCount = 0;
        while (expectedEventsItr.hasNext()) {
            eventCount++;
            assertTrue("The number of events returned by the eventReader less than the expected event count of: " + expectedEvents.size() + " was: " + eventCount, eventItr.hasNext());
            
            final CharacterEvent expectedEvent = expectedEventsItr.next();
            final CharacterEvent event = eventItr.next();
            assertEquals("Events at index " + eventCount + " do not match\n" + expectedEvent + "\n" + event, expectedEvent, event);
        }
        
        assertFalse("The number of events returned by the eventReader is more than the expected event count of: " + expectedEvents.size(), eventItr.hasNext());
    }
    
    private List<? extends CharacterEvent> getExpectedEvents() {
        return ImmutableList.<CharacterEvent>builder()
                .add(CharacterDataEventImpl.create("<!--\n" + 
                		"\n" + 
                		"    Licensed to Jasig under one or more contributor license\n" + 
                		"    agreements. See the NOTICE file distributed with this work\n" + 
                		"    for additional information regarding copyright ownership.\n" + 
                		"    Jasig licenses this file to you under the Apache License,\n" + 
                		"    Version 2.0 (the \"License\"); you may not use this file\n" + 
                		"    except in compliance with the License. You may obtain a\n" + 
                		"    copy of the License at:\n" + 
                		"\n" + 
                		"    http://www.apache.org/licenses/LICENSE-2.0\n" + 
                		"\n" + 
                		"    Unless required by applicable law or agreed to in writing,\n" + 
                		"    software distributed under the License is distributed on\n" + 
                		"    an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" + 
                		"    KIND, either express or implied. See the License for the\n" + 
                		"    specific language governing permissions and limitations\n" + 
                		"    under the License.\n" + 
                		"\n" + 
                		"--><layout>\n" + 
                		"    <folder>\n" + 
                		"        "))
        		.add(new PortletHeaderPlaceholderEventImpl(null))
        		.add(CharacterDataEventImpl.create("\n" + 
        				"        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "        "))
                .add(new PortletHeaderPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"    </folder>\n" + 
                		"    <folder ID=\"0\" type=\"root\">\n" + 
                		"        <folder ID=\"a\" type=\"header\">\n" + 
                		"            "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"            "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"        </folder>\n" + 
                		"        <folder ID=\"1\" fname=\"my-tab\" type=\"tab\">\n" + 
                		"            <name xml:lang=\"en\">My Tab</name>\n" + 
                		"            <folder ID=\"2\" type=\"column\">\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"            </folder>\n" + 
                		"            <folder ID=\"6\" type=\"column\">\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                        "                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"            </folder>\n" + 
                		"        </folder>\n" + 
                		"        <folder ID=\"11\" fname=\"my-classes\" type=\"tab\">\n" + 
                		"            <name xml:lang=\"en\">My Classes "))
                .add(new PortletTitlePlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("</name>\n" + 
                		"            <name xml:lang=\"de\">My Classes "))
                .add(new PortletTitlePlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("</name>\n" + 
                		"            <link href=\""))
                .add(new PortletHelpPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\">Help Link</link>\n" + 
                		"            <name xml:lang=\"ja\">My Classes "))
                .add(new PortletTitlePlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("</name>\n" + 
                		"            <folder ID=\"12\" type=\"column\">\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"            </folder>\n" + 
                		"            <folder ID=\"16\" type=\"column\">\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"                "))
                .add(new PortletContentPlaceholderEventImpl(null))
                .add(CharacterDataEventImpl.create("\n" + 
                		"            </folder>\n" + 
                		"        </folder>\n" + 
                		"    </folder>\n" + 
                		"</layout>"))
                .build();
    }
    
    private static final class SimpleStAXSource implements StAXPipelineComponent {
        @Override
        public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
            return CacheKey.build("SimpleStAXSource", 1);
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
