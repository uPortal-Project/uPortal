package org.jasig.portal.utils.cache;

public interface TaggedCacheEntryPurger {

    /**
     * Remove all cache entries with keys that have the specified tag
     */
    public abstract void purgeCacheEntries(CacheEntryTag tag);

}