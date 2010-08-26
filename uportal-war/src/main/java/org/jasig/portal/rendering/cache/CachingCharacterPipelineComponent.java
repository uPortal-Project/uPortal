/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.cache;

import java.util.ListIterator;

import org.jasig.portal.character.stream.CharacterEventBufferReader;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.rendering.CharacterPipelineComponent;

/**
 * component that can cache character pipeline events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingCharacterPipelineComponent extends CachingPipelineComponent<CharacterEventReader, CharacterEvent> implements CharacterPipelineComponent {
    
    @Override
    protected CharacterEventReader createEventReader(ListIterator<CharacterEvent> eventCache) {
        return new CharacterEventBufferReader(eventCache);
    }
}
