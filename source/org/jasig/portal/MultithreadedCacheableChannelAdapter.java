/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded channel that is also cacheable.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 */

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
