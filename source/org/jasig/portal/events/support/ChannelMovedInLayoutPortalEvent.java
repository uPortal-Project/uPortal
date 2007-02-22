package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.security.IPerson;

public final class ChannelMovedInLayoutPortalEvent extends
		ChannelLayoutPortalEvent {
    
    private static final String EVENT_SUFFIX = " was moved in layout";

	public ChannelMovedInLayoutPortalEvent(final Object source,
			final IPerson person, final UserProfile profile,
			final IUserLayoutChannelDescription description) {
		super(source, person, profile, description);
	}

	public String toString() {
		return getEvent() + " " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
    
    public String getEvent() {
        return super.getEvent() + EVENT_SUFFIX;
    }
}
