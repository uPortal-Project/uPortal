package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

public final class UserRemovedFolderFromLayoutPortalEvent extends LayoutPortalEvent {
    private static final long serialVersionUID = 1L;

	public UserRemovedFolderFromLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile,
            final IUserLayoutFolderDescription folder) {
        super(source, person, profile, folder, EventType.getEventType("LAYOUT_FOLDER_REMOVED"));
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
		return "Folder [" + getFolder().getName() + ", " + getFolder().getId()
				+ "] was removed from layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
