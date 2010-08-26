/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream.events;

import org.springframework.util.Assert;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CharacterDataEventImpl implements CharacterDataEvent {
    private static final long serialVersionUID = 1L;
    
    private final String data;
    
    public CharacterDataEventImpl(String data) {
        Assert.notNull(data);
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterDataEvent#getData()
     */
    @Override
    public String getData() {
        return this.data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.CHARACTER;
    }

    @Override
    public String toString() {
        return "CharacterDataEventImpl [data=" + this.data + "]\n";
    }
}
