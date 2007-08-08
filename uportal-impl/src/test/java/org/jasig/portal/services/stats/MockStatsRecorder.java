/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * This is a Mock Object stats recorder.  It exists to support stats recorder
 * unit testing.
 * 
 * This object maintains internal counters of received "events" on the
 * IStatsRecorder API and exposes those lists for interrogation.  Test code can examine these counters to implement
 * sanity checking.
 * @since uPortal 2.5.2
 */
public class MockStatsRecorder 
	implements IStatsRecorder {

	private int logins = 0;
	
	private int logouts = 0;
	
	private int sessionCreates = 0;
	
	private int sessionDestroys = 0;
	
	private int channelDefinitionPublishes = 0;
	
	private int channelDefinitionModifies = 0;
	
	private int channelDefinitionRemoves = 0;
	
	private int channelAddsToLayout = 0;
	
	private int channelUpdatesInLayout = 0;
	
	private int channelMovesInLayout = 0;
	
	private int channelRemovesFromLayout = 0;
	
	private int folderAddsToLayout = 0;
	
	private int folderUpdatesInLayout = 0;
	
	private int folderMovesInLayout = 0;
	
	private int folderRemovesFromLayout = 0;
	
	private int channelInstantiates = 0;
	
	private int channelRenders = 0;
	
	private int channelTargets = 0;
	
	// IStatsRecorder methods
	
	public void recordLogin(IPerson person) {
		this.logins++;
	}

	public void recordLogout(IPerson person) {
		this.logouts++;
	}

	public void recordSessionCreated(IPerson person) {
		this.sessionCreates++;
	}

	public void recordSessionDestroyed(IPerson person) {
		this.sessionDestroys++;
	}

	public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
		this.channelDefinitionPublishes++;
	}

	public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
		this.channelDefinitionModifies++;
	}

	public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
		this.channelDefinitionRemoves++;
	}

	public void recordChannelAddedToLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelAddsToLayout++;
	}

	public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelUpdatesInLayout++;
	}

	public void recordChannelMovedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelMovesInLayout++;
	}

	public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelRemovesFromLayout++;
	}

	public void recordFolderAddedToLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderAddsToLayout++;
	}

	public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderUpdatesInLayout++;
	}

	public void recordFolderMovedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderMovesInLayout++;
	}

	public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderRemovesFromLayout++;
	}

	public void recordChannelInstantiated(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelInstantiates++;
	}

	public void recordChannelRendered(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelRenders++;	
	}

	public void recordChannelTargeted(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelTargets++;
	}

	
	
	
    //	 counter accessor methods
	
	
	public int getChannelAddsToLayout() {
		return channelAddsToLayout;
	}

	public int getChannelDefinitionModifies() {
		return channelDefinitionModifies;
	}

	public int getChannelDefinitionPublishes() {
		return channelDefinitionPublishes;
	}

	public int getChannelDefinitionRemoves() {
		return channelDefinitionRemoves;
	}

	public int getChannelInstantiates() {
		return channelInstantiates;
	}

	public int getChannelMovesInLayout() {
		return channelMovesInLayout;
	}

	public int getChannelRemovesFromLayout() {
		return channelRemovesFromLayout;
	}

	public int getChannelRenders() {
		return channelRenders;
	}

	public int getChannelTargets() {
		return channelTargets;
	}

	public int getChannelUpdatesInLayout() {
		return channelUpdatesInLayout;
	}

	public int getFolderAddsToLayout() {
		return folderAddsToLayout;
	}

	public int getFolderMovesInLayout() {
		return folderMovesInLayout;
	}

	public int getFolderRemovesFromLayout() {
		return folderRemovesFromLayout;
	}

	public int getFolderUpdatesInLayout() {
		return folderUpdatesInLayout;
	}

	public int getLogins() {
		return logins;
	}

	public int getLogouts() {
		return logouts;
	}

	public int getSessionCreates() {
		return sessionCreates;
	}

	public int getSessionDestroys() {
		return sessionDestroys;
	}
	
}
