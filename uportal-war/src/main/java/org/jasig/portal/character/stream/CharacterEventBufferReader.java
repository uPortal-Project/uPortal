/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream;

import java.util.List;
import java.util.ListIterator;

import org.jasig.portal.character.stream.events.CharacterEvent;

/**
 * Creates a {@link CharacterEventReader} that wraps a {@link List} of {@link CharacterEvent}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CharacterEventBufferReader implements CharacterEventReader {
    private final ListIterator<CharacterEvent> eventBuffer;
    
    public CharacterEventBufferReader(ListIterator<CharacterEvent> eventBuffer) {
        this.eventBuffer = eventBuffer;
    }

    @Override
    public void close() {
        //NO-OP
    }

    @Override
    public CharacterEvent peek() {
        final CharacterEvent event = this.eventBuffer.next();
        //Step back by one in the list
        this.eventBuffer.previous();
        
        return event;
    }

    @Override
    public boolean hasNext() {
        return this.eventBuffer.hasNext();
    }

    @Override
    public CharacterEvent next() {
        return this.eventBuffer.next();
    }

    @Override
    public void remove() {
        this.eventBuffer.remove();
    }
}