package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.events.EventType;
import org.jasig.portal.security.IPerson;

public final class RemovedChannelDefinitionPortalEvent extends ChannelPortalEvent {
    private static final long serialVersionUID = 1L;

    public RemovedChannelDefinitionPortalEvent(final Object source, final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition, EventType.getEventType("CHANNEL_DEFINITION_REMOVED"));
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was removed by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
