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

package org.jasig.portal.services.stats;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
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

	public void recordChannelDefinitionPublished(IPerson person, IPortletDefinition channelDef) {
		this.channelDefinitionPublishes++;
	}

	public void recordChannelDefinitionModified(IPerson person, IPortletDefinition channelDef) {
		this.channelDefinitionModifies++;
	}

	public void recordChannelDefinitionRemoved(IPerson person, IPortletDefinition channelDef) {
		this.channelDefinitionRemoves++;
	}

	public void recordChannelAddedToLayout(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelAddsToLayout++;
	}

	public void recordChannelUpdatedInLayout(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelUpdatesInLayout++;
	}

	public void recordChannelMovedInLayout(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelMovesInLayout++;
	}

	public void recordChannelRemovedFromLayout(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelRemovesFromLayout++;
	}

	public void recordFolderAddedToLayout(IPerson person, IUserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderAddsToLayout++;
	}

	public void recordFolderUpdatedInLayout(IPerson person, IUserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderUpdatesInLayout++;
	}

	public void recordFolderMovedInLayout(IPerson person, IUserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderMovesInLayout++;
	}

	public void recordFolderRemovedFromLayout(IPerson person, IUserProfile profile, IUserLayoutFolderDescription folderDesc) {
		this.folderRemovesFromLayout++;
	}

	public void recordChannelInstantiated(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelInstantiates++;
	}

	public void recordChannelRendered(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
		this.channelRenders++;	
	}

	public void recordChannelTargeted(IPerson person, IUserProfile profile, IUserLayoutChannelDescription channelDesc) {
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
