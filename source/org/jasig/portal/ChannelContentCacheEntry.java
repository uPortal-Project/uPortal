/* Copyright 2008 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.serialize.CachingSerializer;

public class ChannelContentCacheEntry extends BaseChannelCacheEntry {
    
    public ChannelContentCacheEntry(String channelId) {
        super(channelId);
    }

    public void replayCache(CachingSerializer serializer, ChannelManager cm)
        throws PortalException {
        cm.outputChannel(getChannelId(), serializer);
    }

    public CacheType getCacheType() {
        return CacheType.CHANNEL_CONTENT;
    }
}
