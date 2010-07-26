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

package org.jasig.portal.channel;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IPerson;

/**
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public interface IChannelPublishingService {

	public static final String FRAMEWORK_OWNER = "UP_PORTLET_SUBSCRIBE";
	public static final String SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
	public static final String GRANT_PERMISSION_TYPE = "GRANT";

	public IChannelDefinition saveChannelDefinition(IChannelDefinition definition, IPerson publisher, ChannelCategory[] categories, IGroupMember[] groupMembers);

	public void removeChannelDefinition(IChannelDefinition channelDefinition, IPerson person);
	
}
