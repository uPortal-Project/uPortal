/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.events.EventType;
import org.jasig.portal.security.IPerson;

public final class PublishedChannelDefinitionPortalEvent extends ChannelPortalEvent {
    private static final long serialVersionUID = 1L;

    public PublishedChannelDefinitionPortalEvent(final Object source, final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition, EventType.getEventType("CHANNEL_DEFINITION_PUBLISHED"));
	}

	/* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was published by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
