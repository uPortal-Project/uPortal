/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.portal.events.handlers;

import org.jasig.portal.events.EventHandler;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.support.ChannelAddedToLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelInstanciatedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelMovedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelPortalEvent;
import org.jasig.portal.events.support.ChannelRemovedFromLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelRenderedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelTargetedInLayoutPortalEvent;
import org.jasig.portal.events.support.ChannelUpdatedInLayoutPortalEvent;
import org.jasig.portal.events.support.LayoutPortalEvent;
import org.jasig.portal.events.support.ModifiedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.PublishedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.RemovedChannelDefinitionPortalEvent;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.events.support.UserAddedFolderToLayoutPortalEvent;
import org.jasig.portal.events.support.UserLoggedInPortalEvent;
import org.jasig.portal.events.support.UserMovedFolderInLayoutPortalEvent;
import org.jasig.portal.events.support.UserRemovedFolderFromLayoutPortalEvent;
import org.jasig.portal.events.support.UserSessionCreatedPortalEvent;
import org.jasig.portal.events.support.UserSessionDestroyedPortalEvent;
import org.jasig.portal.events.support.UserUpdatedFolderInLayoutPortalEvent;
import org.jasig.portal.services.stats.IStatsRecorder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Adapter class for legacy IStatsRecorder such that deployers don't immediately
 * need to write Event Handlers. They can re-use their existing IStatsRecorder.
 * 
 * @author Scott Battaglia
 * @version $Revision$ Date$
 * @since 2.6
 * 
 */
public final class StatsRecorderEventHandlerAdapter implements EventHandler,
		InitializingBean {

	/** Instance of <code>IStatsRecorder</code> to delegate to. */
	private IStatsRecorder recorder;

	public void handleEvent(final PortalEvent event) {
		final Class<? extends PortalEvent> eventClass = event.getClass();
		if (eventClass.equals(ChannelAddedToLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelAddedToLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass
				.equals(ChannelInstanciatedInLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelInstantiated(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass.equals(ChannelMovedInLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelMovedInLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass.equals(ChannelRemovedFromLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelRemovedFromLayout(portalEvent
					.getPerson(), portalEvent.getProfile(), portalEvent
					.getChannelDescription());
		} else if (eventClass.equals(ChannelRenderedInLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelRendered(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass.equals(ChannelTargetedInLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelTargeted(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass.equals(ChannelUpdatedInLayoutPortalEvent.class)) {
			final ChannelLayoutPortalEvent portalEvent = (ChannelLayoutPortalEvent) event;
			this.recorder.recordChannelUpdatedInLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent
							.getChannelDescription());
		} else if (eventClass
				.equals(ModifiedChannelDefinitionPortalEvent.class)) {
			final ChannelPortalEvent portalEvent = (ChannelPortalEvent) event;
			this.recorder.recordChannelDefinitionModified(portalEvent
					.getPerson(), portalEvent.getChannelDefinition());
		} else if (eventClass
				.equals(PublishedChannelDefinitionPortalEvent.class)) {
			final ChannelPortalEvent portalEvent = (ChannelPortalEvent) event;
			this.recorder.recordChannelDefinitionPublished(portalEvent
					.getPerson(), portalEvent.getChannelDefinition());
		} else if (eventClass.equals(RemovedChannelDefinitionPortalEvent.class)) {
			final ChannelPortalEvent portalEvent = (ChannelPortalEvent) event;
			this.recorder.recordChannelDefinitionRemoved(portalEvent
					.getPerson(), portalEvent.getChannelDefinition());
		} else if (eventClass.equals(UserLoggedOutPortalEvent.class)) {
			this.recorder.recordLogout(event.getPerson());
		} else if (eventClass.equals(UserAddedFolderToLayoutPortalEvent.class)) {
			final LayoutPortalEvent portalEvent = (LayoutPortalEvent) event;
			this.recorder.recordFolderAddedToLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent.getFolder());
		} else if (eventClass.equals(UserLoggedInPortalEvent.class)) {
			this.recorder.recordLogin(event.getPerson());
		} else if (eventClass.equals(UserMovedFolderInLayoutPortalEvent.class)) {
			final LayoutPortalEvent portalEvent = (LayoutPortalEvent) event;
			this.recorder.recordFolderMovedInLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent.getFolder());
		} else if (eventClass
				.equals(UserRemovedFolderFromLayoutPortalEvent.class)) {
			final LayoutPortalEvent portalEvent = (LayoutPortalEvent) event;
			this.recorder.recordFolderRemovedFromLayout(
					portalEvent.getPerson(), portalEvent.getProfile(),
					portalEvent.getFolder());
		} else if (eventClass.equals(UserSessionCreatedPortalEvent.class)) {
			this.recorder.recordSessionCreated(event.getPerson());
		} else if (eventClass.equals(UserSessionDestroyedPortalEvent.class)) {
			this.recorder.recordSessionDestroyed(event.getPerson());
		} else if (eventClass
				.equals(UserUpdatedFolderInLayoutPortalEvent.class)) {
			final LayoutPortalEvent portalEvent = (LayoutPortalEvent) event;
			this.recorder.recordFolderUpdatedInLayout(portalEvent.getPerson(),
					portalEvent.getProfile(), portalEvent.getFolder());
		}
	}

	public boolean supports(final PortalEvent event) {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.recorder, "recorder cannot be null.");
	}

	public void setRecorder(final IStatsRecorder recorder) {
		this.recorder = recorder;
	}
}
