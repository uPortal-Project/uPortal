package org.jasig.portal;
/**
 * Internal adapter for a multithreaded character channel that is also cacheable.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
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
