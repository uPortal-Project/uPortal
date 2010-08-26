/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream.events;

import java.io.Serializable;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface CharacterEvent extends Serializable {
    public CharacterEventTypes getEventType();
}
