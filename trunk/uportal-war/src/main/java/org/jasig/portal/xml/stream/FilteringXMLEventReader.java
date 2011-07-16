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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Base class for {@link XMLEventReader}s that just want to modify events without adding or
 * removing events from the reader stream.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class FilteringXMLEventReader extends BaseXMLEventReader {
    private XMLEvent peekedEvent = null;
    
    public FilteringXMLEventReader(XMLEventReader reader) {
        super(reader);
    }

    @Override
    protected final XMLEvent internalNextEvent() throws XMLStreamException {
        return this.internalNext(false);
    }
    
    @Override
    public boolean hasNext() {
        try {
            return peekedEvent != null || (super.hasNext() && this.peek() != null);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public final XMLEvent peek() throws XMLStreamException {
        if (peekedEvent != null) {
            return peekedEvent;
        }
        
        peekedEvent = internalNext(true);
        return peekedEvent;
    }

    protected final XMLEvent internalNext(boolean peek) throws XMLStreamException {
        XMLEvent event = null;
        
        if (peekedEvent != null) {
            event = peekedEvent;
            peekedEvent = null;
            return event;
        }
        
        do {
            event = super.getParent().nextEvent();
            event = this.filterEvent(event, peek);
        } while (event == null);
        
        return event;
    }
    
    /**
     * @param event The current event
     * @param peek If the event is from a {@link #peek()} call
     * @return The event to return, if null is returned the event is dropped from the stream and the next event will be used.
     */
    protected abstract XMLEvent filterEvent(XMLEvent event, boolean peek);
}
