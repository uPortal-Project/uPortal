/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

public enum ChannelLifecycleState {
	
	CREATED(0),APPROVED(1),PUBLISHED(2),EXPIRED(3);
	
	private int order;
	
	private ChannelLifecycleState(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return this.order;
	}
	
	public boolean isBefore(ChannelLifecycleState state) {
		return (this.getOrder() < state.getOrder());
	}

	public boolean isAfter(ChannelLifecycleState state) {
		return (this.getOrder() > state.getOrder());
	}

}
