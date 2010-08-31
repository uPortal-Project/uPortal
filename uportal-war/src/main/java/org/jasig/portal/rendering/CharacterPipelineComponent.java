/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.rendering;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterEvent;

/**
 * {@link PipelineComponent} for a {@link CharacterEventReader}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface CharacterPipelineComponent extends PipelineComponent<CharacterEventReader, CharacterEvent> {
}
