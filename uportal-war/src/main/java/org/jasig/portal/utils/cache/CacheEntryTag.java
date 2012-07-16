package org.jasig.portal.utils.cache;

/**
 * Tag for a cache entry
 * 
 * @author Eric Dalquist
 */
public interface CacheEntryTag {
    /**
     * @return The type of tag, should be a generally consistent type across the application. ex: username
     */
    String getTagType();
}
