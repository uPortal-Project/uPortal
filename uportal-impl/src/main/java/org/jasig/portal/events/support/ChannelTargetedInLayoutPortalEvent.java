/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events.support;

import org.jasig.portal.UserProfile;
import org.jasig.portal.events.EventType;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IPerson;

public final class ChannelTargetedInLayoutPortalEvent extends ChannelLayoutPortalEvent {
    private static final long serialVersionUID = 1L;

	public ChannelTargetedInLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode) {
        super(source, person, profile, description, parentNode, EventType.getEventType("CHANNEL_TARGETED"));
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
        return "Channel " + getChannelDescriptionString()
            + " was targeted in layout " + getProfile().getLayoutId()
            + " under node " + getParentDescriptionString()
            + " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
