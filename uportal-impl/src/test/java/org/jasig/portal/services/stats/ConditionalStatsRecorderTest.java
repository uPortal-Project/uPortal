/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.layout.node.UserLayoutChannelDescription;
import org.jasig.portal.layout.node.UserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

import junit.framework.TestCase;

/**
 * This testcase tests each IStatsRecorder method as implemented in ConditionalStatsRecorder
 * verifying that when configured not to propogate the "event", it doesn't, and when configured
 * to propogate the "event", it does.
 * @since uPortal 2.5.2
 */
public class ConditionalStatsRecorderTest extends TestCase {

	private ConditionalStatsRecorder conditionalRecorder;
	private MockStatsRecorder mockRecorder;
	private StatsRecorderFlagsImpl statsRecorderFlags;
	
	private IPerson dummyPerson = new PersonImpl();
	private UserProfile dummyProfile = new UserProfile();
	
	private IUserLayoutChannelDescription dummyChannelDescription = new UserLayoutChannelDescription();
	
	private IUserLayoutFolderDescription dummyFolderDescription = new UserLayoutFolderDescription();
	
	// a channel definition with a bogus ID.
	private ChannelDefinition dummyChannelDefinition = new ChannelDefinition(1);
	
	
	protected void setUp() {
		this.conditionalRecorder = new ConditionalStatsRecorder();
		MockStatsRecorder mockTarget = new MockStatsRecorder();
		conditionalRecorder.setTargetStatsRecorder(mockTarget);
		this.mockRecorder = mockTarget;
		this.statsRecorderFlags = new StatsRecorderFlagsImpl();
		conditionalRecorder.setFlags(this.statsRecorderFlags);
	}
	
	public void testRecordLogin() {
		assertEquals(0, mockRecorder.getLogins());
		
		this.conditionalRecorder.recordLogin(dummyPerson);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getLogins());
		
		this.statsRecorderFlags.setRecordLogin(true);
		
		this.conditionalRecorder.recordLogin(dummyPerson);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getLogins());
	}
	
	public void testRecordLogout() {
		assertEquals(0, mockRecorder.getLogouts());
		
		this.conditionalRecorder.recordLogout(dummyPerson);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getLogouts());
		
		this.statsRecorderFlags.setRecordLogout(true);
		
		this.conditionalRecorder.recordLogout(dummyPerson);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getLogouts());
	}
	
	public void testRecordSessionCreated() {
		assertEquals(0, mockRecorder.getSessionCreates());
		
		this.conditionalRecorder.recordSessionCreated(dummyPerson);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getSessionCreates());
		
		this.statsRecorderFlags.setRecordSessionCreated(true);
		
		this.conditionalRecorder.recordSessionCreated(dummyPerson);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getSessionCreates());
	}
	
	public void testRecordSessionDestroyed() {
		assertEquals(0, mockRecorder.getSessionDestroys());
		
		this.conditionalRecorder.recordSessionDestroyed(dummyPerson);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getSessionDestroys());
		
		this.statsRecorderFlags.setRecordSessionDestroyed(true);
		
		this.conditionalRecorder.recordSessionDestroyed(dummyPerson);
		
		// the conditional recorder should have passed along the event since it was configured to do so
		assertEquals(1, mockRecorder.getSessionDestroys());
		
	}
	
	public void testRecordChannelDefinitionPublished() {
		assertEquals(0, mockRecorder.getChannelDefinitionPublishes());
		
		this.conditionalRecorder.recordChannelDefinitionPublished(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelDefinitionPublishes());
		
		this.statsRecorderFlags.setRecordChannelDefinitionPublished(true);
		
		this.conditionalRecorder.recordChannelDefinitionPublished(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should have passed along the event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelDefinitionPublishes());
	}
	
	public void testRecordChannelDefinitionModified() {
		assertEquals(0, mockRecorder.getChannelDefinitionPublishes());
		
		this.conditionalRecorder.recordChannelDefinitionModified(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelDefinitionModifies());
		
		this.statsRecorderFlags.setRecordChannelDefinitionModified(true);
		
		this.conditionalRecorder.recordChannelDefinitionModified(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should have passed along the event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelDefinitionModifies());
	}

	public void testRecordChannelDefinitionRemoved() {
		assertEquals(0, mockRecorder.getChannelDefinitionRemoves());
		
		this.conditionalRecorder.recordChannelDefinitionRemoved(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelDefinitionRemoves());
		
		this.statsRecorderFlags.setRecordChannelDefinitionRemoved(true);
		
		this.conditionalRecorder.recordChannelDefinitionRemoved(dummyPerson, dummyChannelDefinition);
		
		// the conditional recorder should have passed along the event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelDefinitionRemoves());
	}
	
	public void testRecordChannelAddedToLayout() {
		assertEquals(0, mockRecorder.getChannelAddsToLayout());
		
		this.conditionalRecorder.recordChannelAddedToLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelAddsToLayout());
		
		this.statsRecorderFlags.setRecordChannelAddedToLayout(true);
		
		this.conditionalRecorder.recordChannelAddedToLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelAddsToLayout());
		
	}
	
	public void testRecordChannelUpdatedInLayout() {
		assertEquals(0, mockRecorder.getChannelUpdatesInLayout());
		
		this.conditionalRecorder.recordChannelUpdatedInLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelUpdatesInLayout());
		
		this.statsRecorderFlags.setRecordChannelUpdatedInLayout(true);
		
		this.conditionalRecorder.recordChannelUpdatedInLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelUpdatesInLayout());
		
	}
	
	public void testRecordChannelMovedInLayout() {
		assertEquals(0, mockRecorder.getChannelMovesInLayout());
		
		this.conditionalRecorder.recordChannelMovedInLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelMovesInLayout());
		
		this.statsRecorderFlags.setRecordChannelMovedInLayout(true);
		
		this.conditionalRecorder.recordChannelMovedInLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelMovesInLayout());
		
	}
	
	public void testRecordChannelRemovedFromLayout() {
		assertEquals(0, mockRecorder.getChannelRemovesFromLayout());
		
		this.conditionalRecorder.recordChannelRemovedFromLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelRemovesFromLayout());
		
		this.statsRecorderFlags.setRecordChannelRemovedFromLayout(true);
		
		this.conditionalRecorder.recordChannelRemovedFromLayout(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelRemovesFromLayout());
		
	}
	
	public void testRecordFolderAddedToLayout() {
		assertEquals(0, mockRecorder.getFolderAddsToLayout());
		
		this.conditionalRecorder.recordFolderAddedToLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getFolderAddsToLayout());
		
		this.statsRecorderFlags.setRecordFolderAddedToLayout(true);
		
		this.conditionalRecorder.recordFolderAddedToLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getFolderAddsToLayout());
		
	}
	
	public void testRecordFolderUpdatedInLayout() {
		assertEquals(0, mockRecorder.getFolderUpdatesInLayout());
		
		this.conditionalRecorder.recordFolderUpdatedInLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getFolderUpdatesInLayout());
		
		this.statsRecorderFlags.setRecordFolderUpdatedInLayout(true);
		
		this.conditionalRecorder.recordFolderUpdatedInLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getFolderUpdatesInLayout());
		
	}
	
	public void testRecordFolderMovedInLayout() {
		assertEquals(0, mockRecorder.getFolderMovesInLayout());
		
		this.conditionalRecorder.recordFolderMovedInLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getFolderMovesInLayout());
		
		this.statsRecorderFlags.setRecordFolderMovedInLayout(true);
		
		this.conditionalRecorder.recordFolderMovedInLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getFolderMovesInLayout());
		
	}
	
	public void testRecordFolderRemovedFromLayout() {
		assertEquals(0, mockRecorder.getFolderRemovesFromLayout());
		
		this.conditionalRecorder.recordFolderRemovedFromLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getFolderRemovesFromLayout());
		
		this.statsRecorderFlags.setRecordFolderRemovedFromLayout(true);
		
		this.conditionalRecorder.recordFolderRemovedFromLayout(dummyPerson, dummyProfile, dummyFolderDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getFolderRemovesFromLayout());
		
	}
	
	public void testRecordChannelInstantiated() {
		assertEquals(0, mockRecorder.getChannelInstantiates());
		
		this.conditionalRecorder.recordChannelInstantiated(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelInstantiates());
		
		this.statsRecorderFlags.setRecordChannelInstantiated(true);
		
		this.conditionalRecorder.recordChannelInstantiated(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelInstantiates());
		
	}
	
	public void testRecordChannelRendered() {
		assertEquals(0, mockRecorder.getChannelRenders());
		
		this.conditionalRecorder.recordChannelRendered(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelRenders());
		
		this.statsRecorderFlags.setRecordChannelRendered(true);
		
		this.conditionalRecorder.recordChannelRendered(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelRenders());
		
	}
	
	public void testRecordChannelTargetted() {
		
		assertEquals(0, mockRecorder.getChannelTargets());
		
		this.conditionalRecorder.recordChannelTargeted(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should not have passed along event since it was not configured to do so
		assertEquals(0, mockRecorder.getChannelTargets());
		
		this.statsRecorderFlags.setRecordChannelTargeted(true);
		
		this.conditionalRecorder.recordChannelTargeted(dummyPerson, dummyProfile, dummyChannelDescription);
		
		// the conditional recorder should have passed along event since it was configured to do so
		assertEquals(1, mockRecorder.getChannelTargets());
	}
	
}
