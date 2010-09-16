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
    
    public FilteringXMLEventReader(XMLEventReader reader) {
        super(reader);
    }

    @Override
    protected final XMLEvent internalNextEvent() throws XMLStreamException {
        final XMLEvent event = this.getParent().nextEvent();
        return this.filterEvent(event, false);
    }

    @Override
    public final XMLEvent peek() throws XMLStreamException {
        final XMLEvent event = super.peek();
        return this.filterEvent(event, true);
    }
    
    /**
     * @param event The current event
     * @param peek If the event is from a {@link #peek()} call
     * @return The event to return
     */
    protected abstract XMLEvent filterEvent(XMLEvent event, boolean peek);
}
