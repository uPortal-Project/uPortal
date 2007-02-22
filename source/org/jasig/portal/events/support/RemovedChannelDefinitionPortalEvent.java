package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.security.IPerson;

public final class RemovedChannelDefinitionPortalEvent extends
		ChannelPortalEvent {
    
    private static final String EVENT_SUFFIX = " was removed";

	public RemovedChannelDefinitionPortalEvent(final Object source,
			final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition);
	}

	public String toString() {
		return getEvent() + " by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
    
    public String getEvent() {
        return super.getEvent() + EVENT_SUFFIX;
    }
}
