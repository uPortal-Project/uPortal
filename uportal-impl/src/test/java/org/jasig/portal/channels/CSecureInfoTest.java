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
