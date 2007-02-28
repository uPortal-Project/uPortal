package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

public final class ModifiedChannelDefinitionPortalEvent extends
		ChannelPortalEvent {

	public ModifiedChannelDefinitionPortalEvent(final Object source,
			final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition);
	}

	public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was modified by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
