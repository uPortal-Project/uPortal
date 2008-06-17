/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.support;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 *
 */
public abstract class ChannelPortalEvent extends PortalEvent {

	private final ChannelDefinition channelDefinition;
	
	public ChannelPortalEvent(final Object source, final IPerson person, final ChannelDefinition channelDefinition) {
		super(source, person);
		this.channelDefinition = channelDefinition;
	}
	
	public final ChannelDefinition getChannelDefinition() {
		return this.channelDefinition;
	}
}
