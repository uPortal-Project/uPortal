/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.character.stream.events;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletTitlePlaceholderEventImpl implements PortletTitlePlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private final int portletWindowId;
    
    public PortletTitlePlaceholderEventImpl(int portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.PortletPlaceholderEvent#getPortletWindowId()
     */
    @Override
    public int getPortletWindowId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_CONTENT;
    }

    @Override
    public String toString() {
        return "PortletTitlePlaceholderEventImpl [portletWindowId=" + this.portletWindowId + "]";
    }
}
