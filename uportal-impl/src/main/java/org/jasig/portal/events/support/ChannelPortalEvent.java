/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.events.support;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.security.IPerson;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public abstract class ChannelPortalEvent extends PortalEvent {
	private final IChannelDefinition channelDefinition;
	
	public ChannelPortalEvent(final Object source, final IPerson person, final IChannelDefinition channelDefinition, final EventType eventType) {
		super(source, person, eventType);
		this.channelDefinition = channelDefinition;
	}

    public final IChannelDefinition getChannelDefinition() {
		return this.channelDefinition;
	}
    
    public final String getChannelDefinitionId() {
        return Integer.toString(this.channelDefinition.getId());
    }
    public void setChannelDefinitionId(String id) {
        //ignore, method required for hibernate
    }

    /* (non-Javadoc)
     * @see java.util.EventObject#toString()
     */
    @Override
    public String toString() {
        return this.getClass().getName() + " for Channel '" + getChannelDefinition().getName()
                + "' by " + getDisplayName() + " at " + getTimestampAsDate();
    }
}
