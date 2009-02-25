/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
