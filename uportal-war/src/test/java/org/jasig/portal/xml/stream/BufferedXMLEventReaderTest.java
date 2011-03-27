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

package org.jasig.portal.xml.stream;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BufferedXMLEventReaderTest {
    
    @Test
    public void testBufferNoEvents() throws Exception {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        
        final InputStream xmlStream = this.getClass().getResourceAsStream("document.xml");
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(xmlStream);
        
        final BufferedXMLEventReader reader = new BufferedXMLEventReader(xmlEventReader);
        
        int eventCount = 0;
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(122, eventCount);
        
        reader.reset();
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(122, eventCount);
    }
    
    @Test
    public void testBufferAllEvents() throws Exception {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        
        final InputStream xmlStream = this.getClass().getResourceAsStream("document.xml");
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(xmlStream);
        
        final BufferedXMLEventReader reader = new BufferedXMLEventReader(xmlEventReader, -1);
        
        final XMLEvent firstEvent = reader.peek();
        
        int eventCount = 0;
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(122, eventCount);
        
        reader.reset();
        
        final XMLEvent firstEventAgain = reader.peek();
        assertEquals(firstEvent, firstEventAgain);
        
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(244, eventCount);
    }
    
    @Test
    public void testBufferSomeEvents() throws Exception {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        
        final InputStream xmlStream = this.getClass().getResourceAsStream("document.xml");
        final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(xmlStream);
        
        final BufferedXMLEventReader reader = new BufferedXMLEventReader(xmlEventReader, 10);
        
        int eventCount = 0;
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(122, eventCount);
        
        reader.reset();
        while (reader.hasNext()) {
            reader.nextEvent();
            eventCount++;
        }
        assertEquals(132, eventCount);
    }
}
