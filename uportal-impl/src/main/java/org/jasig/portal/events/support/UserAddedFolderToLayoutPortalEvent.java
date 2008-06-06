package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

public final class UserAddedFolderToLayoutPortalEvent extends LayoutPortalEvent {
    private static final long serialVersionUID = 1L;

    public UserAddedFolderToLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile,
			final IUserLayoutFolderDescription folder) {
		super(source, person, profile, folder, EventType.getEventType("LAYOUT_FOLDER_ADDED"));
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		return "Folder [" + getFolder().getName() + ", " + getFolder().getId()
				+ "] was added to layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
