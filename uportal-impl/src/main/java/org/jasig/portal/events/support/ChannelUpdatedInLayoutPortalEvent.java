package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

public final class ChannelUpdatedInLayoutPortalEvent extends ChannelLayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    public ChannelUpdatedInLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode) {
        super(source, person, profile, description, parentNode, EventType.getEventType("LAYOUT_CHANNEL_UPDATED"));
    }

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
    public String toString() {
		return "Channel [" + getChannelDescription().getName() + ", "
				+ getChannelDescription().getChannelPublishId() + ", "
				+ getChannelDescription().getChannelSubscribeId()
				+ "] was updated in layout " + getProfile().getLayoutId()
				+ " under node [" + getParentDescription().getId() + "," + getParentDescription().getName()
                + "] by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
