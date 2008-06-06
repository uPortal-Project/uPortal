/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.events.EventType;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.security.IPerson;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public abstract class ChannelPortalEvent extends PortalEvent {
	private final ChannelDefinition channelDefinition;
	
	public ChannelPortalEvent(final Object source, final IPerson person, final ChannelDefinition channelDefinition, final EventType eventType) {
		super(source, person, eventType);
		this.channelDefinition = channelDefinition;
	}

    public final ChannelDefinition getChannelDefinition() {
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
