/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
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
		return "Folder " + getFolderString()
                + " was added to layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
