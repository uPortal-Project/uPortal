package org.jasig.portal.utils.cache;

import java.util.Set;

/**
 * Used to tag cache entries (keys or values)
 * 
 * @author Eric Dalquist
 */
public interface TaggedCacheEntry {
    /**
     * @return Tags for the cache entry
     */
    Set<CacheEntryTag> getTags();
}
