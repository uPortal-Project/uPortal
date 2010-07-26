/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
