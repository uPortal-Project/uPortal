/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded character channel that is also cacheable.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @author <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedCharacterChannelAdapter
 */
public class MultithreadedCacheableCharacterChannelAdapter extends MultithreadedCharacterChannelAdapter implements ICacheable {
    public MultithreadedCacheableCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid) {
        super(channel, uid);
    }
    public ChannelCacheKey generateKey() {
        return ((IMultithreadedCacheable)channel).generateKey(this.uid);
    }
    public boolean isCacheValid(Object validity) {
        return ((IMultithreadedCacheable)channel).isCacheValid(validity, this.uid);
    }
}
