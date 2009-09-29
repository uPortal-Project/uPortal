/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;

public interface IChannelPublishingService {

	public static final String FRAMEWORK_OWNER = "UP_FRAMEWORK";
	public static final String SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
	public static final String GRANT_PERMISSION_TYPE = "GRANT";

	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition, IPerson publisher, ChannelCategory[] categories, IGroupMember[] groupMembers);
	
	public void removeChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition approveChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition publishChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition expireChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
}
