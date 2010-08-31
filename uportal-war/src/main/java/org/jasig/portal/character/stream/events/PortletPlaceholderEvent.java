/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream.events;


/**
 * A generic base event type for any event that targets a portlet window.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface PortletPlaceholderEvent extends CharacterEvent {
    /**
     * @return The layout subscribe ID of the targeted portlet
     */
    public String getPortletSubscribeId();
}
