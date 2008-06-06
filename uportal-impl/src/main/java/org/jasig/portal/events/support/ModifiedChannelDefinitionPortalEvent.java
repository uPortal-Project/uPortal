package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.events.EventType;
import org.jasig.portal.security.IPerson;

public final class ModifiedChannelDefinitionPortalEvent extends ChannelPortalEvent {
    private static final long serialVersionUID = 1L;

	public ModifiedChannelDefinitionPortalEvent(final Object source, final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person, channelDefinition, EventType.getEventType("CHANNEL_DEFINITION_MODIFIED"));
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
		return "Channel '" + getChannelDefinition().getName()
				+ "' was modified by " + getDisplayName() + " at "
				+ getTimestampAsDate();
	}
}
