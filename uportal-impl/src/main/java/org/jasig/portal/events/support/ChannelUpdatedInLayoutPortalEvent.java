package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

public final class ChannelUpdatedInLayoutPortalEvent extends
		ChannelLayoutPortalEvent {

	public ChannelUpdatedInLayoutPortalEvent(final Object source,
			final IPerson person, final UserProfile profile,
			final IUserLayoutChannelDescription description) {
		super(source, person, profile, description);
	}

	public String toString() {
		return "Channel [" + getChannelDescription().getName() + ", "
				+ getChannelDescription().getChannelPublishId() + ", "
				+ getChannelDescription().getChannelSubscribeId()
				+ "] was updated in layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
