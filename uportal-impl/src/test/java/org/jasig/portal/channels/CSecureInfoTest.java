/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.util.List;

import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;

import junit.framework.TestCase;

/**
 * Testcase for CSecureInfo.
 * @author andrew.petro@yale.edu
 */
public class CSecureInfoTest extends TestCase {

	/**
	 * Test that CSecureInfo propogates channel events to 
	 * the wrapped channel.
	 */
	public void testReceiveEvent() {
		EventRecordingChannel irc = new EventRecordingChannel();
		
		
		IChannel cSecureInfo = new CSecureInfo("bugusSubId", irc);
		
			
		assertTrue(irc.getEventsReceived().isEmpty());

		cSecureInfo.receiveEvent(PortalEvent.SESSION_DONE_EVENT);
		
		List eventsReceived = irc.getEventsReceived();
		assertFalse(eventsReceived.isEmpty());
		PortalEvent event = (PortalEvent) irc.getEventsReceived().get(0);
		assertEquals(PortalEvent.SESSION_DONE, event.getEventNumber());
	}

}
