/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.cache;

import java.util.ListIterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.rendering.StAXPipelineComponent;
import org.jasig.portal.xml.stream.XMLEventBufferReader;

/**
 * component that can cache StAX pipeline events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CachingStAXPipelineComponent extends CachingPipelineComponent<XMLEventReader, XMLEvent> implements StAXPipelineComponent {

    @Override
    protected XMLEventReader createEventReader(ListIterator<XMLEvent> eventCache) {
        return new XMLEventBufferReader(eventCache);
    }
}
