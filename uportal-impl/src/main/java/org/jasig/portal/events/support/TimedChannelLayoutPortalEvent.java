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

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class TimedChannelLayoutPortalEvent extends ChannelLayoutPortalEvent {
    private final long renderTime;

    public TimedChannelLayoutPortalEvent(final Object source, final IPerson person, final UserProfile profile,
            final IUserLayoutChannelDescription description, final IUserLayoutNodeDescription parentNode, long renderTime,
            final EventType eventType) {
        super(source, person, profile, description, parentNode, eventType);
        
        this.renderTime = renderTime;
    }

    public long getRenderTime() {
        return renderTime;
    }
    public void setRenderTime(long renderTime) {
        //ignore, method required for hibernate
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() +  " for Channel " + getChannelDescriptionString()
                + " in layout " + getProfile().getLayoutId()
                + " under node " + getParentDescriptionString()
                + " by " + getDisplayName() + " at " + getTimestampAsDate() + " in " + this.getRenderTime() + "ms";
    }

}