package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

public final class UserMovedFolderInLayoutPortalEvent extends LayoutPortalEvent {

	public UserMovedFolderInLayoutPortalEvent(final Object source,
			final IPerson person, final UserProfile profile,
			final IUserLayoutFolderDescription folder) {
		super(source, person, profile, folder);
	}

	public String toString() {
		return "Folder [" + getFolder().getName() + ", " + getFolder().getId()
				+ "] was moved in layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + "  at " + getTimestampAsDate();
	}
}
