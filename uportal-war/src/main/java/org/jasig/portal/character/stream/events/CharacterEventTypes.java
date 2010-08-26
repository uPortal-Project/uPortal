/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream.events;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public enum CharacterEventTypes {
    /**
     * @see CharacterDataEvent
     */
    CHARACTER,
    /**
     * @see PortletContentPlaceholderEvent
     */
    PORTLET_CONTENT,
    /**
     * @see PortletTitlePlaceholderEvent
     */
    PORTLET_TITLE,
    /**
     * @see PortletHelpPlaceholderEvent
     */
    PORTLET_HELP,
    /**
     * @see PortletHeaderPlaceholderEvent
     */
    PORTLET_HEADER;
}
