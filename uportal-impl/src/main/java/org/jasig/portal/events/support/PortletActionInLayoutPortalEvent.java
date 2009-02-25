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

public final class PortletActionInLayoutPortalEvent extends TimedChannelLayoutPortalEvent {
    private static final long serialVersionUID = 1L;
    
	public PortletActionInLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode,
            final long renderTime) {
        super(source, person, profile, description, parentNode, renderTime, EventType.getEventType("PORTLET_ACTION"));
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
	public String toString() {
		return "Portlet " + getChannelDescriptionString()
                + " was targeted by an action in layout " + getProfile().getLayoutId()
				+ " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
}
