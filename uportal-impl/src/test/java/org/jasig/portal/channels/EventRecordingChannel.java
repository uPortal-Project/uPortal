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

package org.jasig.portal.channels;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/**
 * Mock channel implementation which exists to record
 * the events it receives.  Useful for testcases.
 * @author andrew.petro@yale.edu
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class EventRecordingChannel implements IChannel {

	private List eventsReceived = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)
	 */
	public void setStaticData(ChannelStaticData sd) throws PortalException {
		// does nothing - mock implementation
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
	 */
	public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
		// does nothing - mock implementation
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
	 */
	public void receiveEvent(PortalEvent ev) {
		// record the event -- my reason for being!
		this.eventsReceived.add(ev);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IChannel#getRuntimeProperties()
	 */
	public ChannelRuntimeProperties getRuntimeProperties() {
		// mock implementation
		return new ChannelRuntimeProperties();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.IChannel#renderXML(org.xml.sax.ContentHandler)
	 */
	public void renderXML(ContentHandler out) throws PortalException {
		// do nothing - mock implementation
	}

	/**
	 * Get a List of events received by this channel in the order they were
	 * received.
	 * @return Returns the eventsReceived.
	 */
	public List getEventsReceived() {
		return eventsReceived;
	}
	
}
