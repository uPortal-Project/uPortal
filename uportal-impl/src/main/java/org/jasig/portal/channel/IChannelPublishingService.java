package org.jasig.portal.channel;

import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;

public interface IChannelPublishingService {

	public static final String FRAMEWORK_OWNER = "UP_FRAMEWORK";
	public static final String SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
	public static final String GRANT_PERMISSION_TYPE = "GRANT";

	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition, IPerson publisher, String[] categoryIDs, IGroupMember[] groupMembers);
	
	public void removeChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition approveChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition publishChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
	public IChannelDefinition expireChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
}
