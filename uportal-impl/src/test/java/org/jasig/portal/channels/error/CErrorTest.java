/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.error;

import java.util.List;

import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.channels.EventRecordingChannel;

import junit.framework.TestCase;

/**
 * Testcase for CError.  Tests that it passes events to channels it wraps.
 * @author andrew.petro@yale.edu
 */
public class CErrorTest extends TestCase {

	/**
	 * Test that CError propogates events to wrapped channels.
	 */
	public void testReceiveEvent() {
		EventRecordingChannel irc = new EventRecordingChannel();
		
		IChannel cError = new CError(ErrorCode.GENERAL_ERROR, new Throwable(), "bogusSusbscribeId", irc);
		
		assertTrue(irc.getEventsReceived().isEmpty());

		cError.receiveEvent(PortalEvent.SESSION_DONE_EVENT);
		
		List eventsReceived = irc.getEventsReceived();
		assertFalse(eventsReceived.isEmpty());
		PortalEvent event = (PortalEvent) irc.getEventsReceived().get(0);
		assertEquals(PortalEvent.SESSION_DONE, event.getEventNumber());
	}

}
