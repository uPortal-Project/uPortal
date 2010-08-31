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

import org.jasig.portal.character.stream.events.CharacterEvent;

/**
 * Base class for application event readers that simply want to filter
 * or monitor events passing through the reader.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class FilteringCharacterEventReader extends CharacterEventReaderDelegate {

    public FilteringCharacterEventReader(CharacterEventReader delegate) {
        super(delegate);
    }
    
    @Override
    public CharacterEvent next() {
        final CharacterEvent event = super.next();
        return this.filterEvent(event, false);
    }

    @Override
    public CharacterEvent peek() {
        final CharacterEvent event = super.peek();
        return this.filterEvent(event, true);
    }

    /**
     * @param event The current event
     * @param peek If the event is from a {@link #peek()} call
     * @return The event to return
     */
    protected abstract CharacterEvent filterEvent(CharacterEvent event, boolean peek);
}
