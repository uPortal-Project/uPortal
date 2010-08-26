/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

/**
 * {@link PipelineComponent} for a {@link XMLEventReader} and {@link XMLEvent}.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface StAXPipelineComponent extends PipelineComponent<XMLEventReader, XMLEvent> {
}
