/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
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
