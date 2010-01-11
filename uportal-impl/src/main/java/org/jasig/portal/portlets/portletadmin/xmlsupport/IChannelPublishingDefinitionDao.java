/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.portletadmin.xmlsupport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.channel.IChannelType;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IChannelPublishingDefinitionDao {
    public ChannelPublishingDefinition getChannelPublishingDefinition(int channelTypeId);

    public Map<IChannelType, ChannelPublishingDefinition> getChannelPublishingDefinitions();
}
