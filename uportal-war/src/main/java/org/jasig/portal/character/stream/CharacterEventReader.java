/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream;

import java.util.Iterator;

import org.jasig.portal.character.stream.events.CharacterEvent;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface CharacterEventReader extends Iterator<CharacterEvent> {
    /**
     * Check the next XMLEvent without reading it from the stream. Returns null if the stream is
     * at EOF or has no more XMLEvents. A call to peek() will be equal to the next return of next(). 
     */
    public CharacterEvent peek();
    
    /**
     * Frees any resources associated with this Reader. This method does not close the underlying
     * input source. 
     */
    public void close();
}
