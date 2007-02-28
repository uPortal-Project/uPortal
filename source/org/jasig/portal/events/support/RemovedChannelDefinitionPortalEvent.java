package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

public final class RemovedChannelDefinitionPortalEvent extends
		ChannelPortalEvent {

	public RemovedChannelDefinitionPortalEvent(final Object source,
			final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition);
	}

	public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was removed by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
