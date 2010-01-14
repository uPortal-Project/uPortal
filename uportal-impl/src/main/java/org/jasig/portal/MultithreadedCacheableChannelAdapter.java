/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
 * Internal adapter for a multithreaded channel that is also cacheable.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class MultithreadedCacheableChannelAdapter extends MultithreadedChannelAdapter implements ICacheable {
    public MultithreadedCacheableChannelAdapter(IMultithreadedChannel channel, String uid) {
	super(channel,uid);
    }
    public ChannelCacheKey generateKey() {
	return ((IMultithreadedCacheable)channel).generateKey(this.uid);
    }
    public boolean isCacheValid(Object validity) {
	return ((IMultithreadedCacheable)channel).isCacheValid(validity,this.uid);
    }
}
