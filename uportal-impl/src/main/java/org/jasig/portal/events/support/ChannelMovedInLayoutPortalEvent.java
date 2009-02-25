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

public final class ChannelMovedInLayoutPortalEvent extends ChannelLayoutPortalEvent {
    private static final long serialVersionUID = 1L;
    
    private final IUserLayoutNodeDescription newParentNode;

	public ChannelMovedInLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile, 
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription oldParentNode, 
            final IUserLayoutNodeDescription newParentNode) {
        super(source, person, profile, description, oldParentNode, EventType.getEventType("LAYOUT_CHANNEL_MOVED"));
        
        this.newParentNode = newParentNode;
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		return "Channel " + getChannelDescriptionString()
                + " was moved in layout " + getProfile().getLayoutId()
				+ " under node [" + this.newParentNode.getId() + "," + this.newParentNode.getName()
                + "] from node " + getParentDescriptionString()
                + " by " + getDisplayName() + " at " + getTimestampAsDate();
	}
    
    public final String getDestinationFolderId() {
        return this.newParentNode.getId();
    }
    public void setDestinationFolderId(String id) {
        //ignore, method required for hibernate
    }
}
