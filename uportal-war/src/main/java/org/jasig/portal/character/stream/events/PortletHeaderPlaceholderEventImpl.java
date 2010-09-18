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
public class PortletHeaderPlaceholderEventImpl implements PortletHeaderPlaceholderEvent {
    private static final long serialVersionUID = 1L;

    private final String portletWindowId;
    
    public PortletHeaderPlaceholderEventImpl(String portletWindowId) {
        this.portletWindowId = portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.PortletPlaceholderEvent#getPortletWindowId()
     */
    @Override
    public String getPortletSubscribeId() {
        return this.portletWindowId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.character.stream.events.CharacterEvent#getEventType()
     */
    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.PORTLET_HEADER;
    }

    @Override
    public String toString() {
        return "PortletHeaderPlaceholderEventImpl [portletWindowId=" + this.portletWindowId + "]";
    }
}
