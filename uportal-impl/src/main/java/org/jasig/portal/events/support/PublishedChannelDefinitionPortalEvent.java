package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

public final class PublishedChannelDefinitionPortalEvent extends
		ChannelPortalEvent {

	public PublishedChannelDefinitionPortalEvent(final Object source,
			final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition);
	}

	public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was published by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
