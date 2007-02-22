package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

public final class UserRemovedFolderFromLayoutPortalEvent extends
		LayoutPortalEvent {
    
    private static final String EVENT_SUFFIX = " was removed from layout";

	public UserRemovedFolderFromLayoutPortalEvent(final Object source,
			final IPerson person, final UserProfile profile,
			final IUserLayoutFolderDescription folder) {
		super(source, person, profile, folder);
	}

	public String toString() {
		return getEvent() + " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
    
    public String getEvent() {
        return super.getEvent() + EVENT_SUFFIX + ' ' + getProfile().getLayoutId();
    }
}
