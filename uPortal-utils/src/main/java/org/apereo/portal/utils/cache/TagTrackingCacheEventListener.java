/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.MapMaker;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Tracks entries added to {@link Ehcache} instances that have keys or values which implement {@link
 * TaggedCacheEntry}. Allows for external removal of elements that match a specified tag
 *
 */
@Service("tagTrackingCacheEventListener")
public class TagTrackingCacheEventListener extends CacheEventListenerAdapter
        implements TaggedCacheEntryPurger {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // tag type -> set of caches that contain keys tagged with that type
    // I don't believe that this will leak Ehcache references as this class should have the same lifecycle as the CacheManager
    private final LoadingCache<String, Set<Ehcache>> taggedCaches =
            CacheBuilder.newBuilder()
                    .build(
                            new CacheLoader<String, Set<Ehcache>>() {
                                @Override
                                public Set<Ehcache> load(String key) throws Exception {
                                    return Collections.newSetFromMap(
                                            new ConcurrentHashMap<Ehcache, Boolean>());
                                }
                            });

    // Cache Name -> Key Tag -> Set of Keys
    private final LoadingCache<String, LoadingCache<CacheEntryTag, Set<Object>>> taggedCacheKeys =
            CacheBuilder.newBuilder()
                    .build(
                            new CacheLoader<String, LoadingCache<CacheEntryTag, Set<Object>>>() {
                                @Override
                                public LoadingCache<CacheEntryTag, Set<Object>> load(String key)
                                        throws Exception {
                                    // Key Tag -> Set of Tagged Cache Keys
                                    return CacheBuilder.newBuilder()
                                            .build(
                                                    new CacheLoader<CacheEntryTag, Set<Object>>() {
                                                        @Override
                                                        public Set<Object> load(CacheEntryTag key)
                                                                throws Exception {
                                                            // Set of Tagged Cache Keys
                                                            return Collections.newSetFromMap(
                                                                    new MapMaker()
                                                                            .weakKeys()
                                                                            .<Object, Boolean>
                                                                                    makeMap());
                                                        }
                                                    });
                                }
                            });

    /** Remove all cache entries with keys that have the specified tag */
    @Override
    public int purgeCacheEntries(CacheEntryTag tag) {
        final String tagType = tag.getTagType();
        final Set<Ehcache> caches = taggedCaches.getIfPresent(tagType);

        //Tag exists in cache(s)
        if (caches == null || caches.isEmpty()) {
            return 0;
        }

        int purgeCount = 0;

        //Iterate over each cache to remove the tagged entries
        for (final Ehcache cache : caches) {
            final String cacheName = cache.getName();

            //See if there are any tagged cache keys for the cache
            final LoadingCache<CacheEntryTag, Set<Object>> cacheKeys =
                    taggedCacheKeys.getIfPresent(cacheName);
            if (cacheKeys != null) {

                //Remove all cache keys from the cache
                final Set<Object> taggedKeys = cacheKeys.asMap().remove(tag);
                if (taggedKeys != null) {
                    final int keyCount = taggedKeys.size();
                    purgeCount += keyCount;
                    logger.debug("Removing {} keys from {} for tag {}", keyCount, cacheName, tag);

                    cache.removeAll(taggedKeys);
                }
            }
        }

        return purgeCount;
    }

    /** Get the tags associated with the element */
    protected Set<CacheEntryTag> getTags(Element element) {
        final Object key = element.getObjectKey();
        if (key instanceof TaggedCacheEntry) {
            return ((TaggedCacheEntry) key).getTags();
        }

        final Object value = element.getObjectValue();
        if (value instanceof TaggedCacheEntry) {
            return ((TaggedCacheEntry) value).getTags();
        }

        return null;
    }

    /** If the element has a TaggedCacheKey record the tag associations */
    protected void putElement(Ehcache cache, Element element) {
        final Set<CacheEntryTag> tags = this.getTags(element);

        //Check if the key is tagged
        if (tags != null && !tags.isEmpty()) {
            final String cacheName = cache.getName();
            final Object key = element.getObjectKey();
            final LoadingCache<CacheEntryTag, Set<Object>> cacheKeys =
                    taggedCacheKeys.getUnchecked(cacheName);

            logger.debug("Tracking {} tags in cache {} for key {}", tags.size(), cacheName, key);

            //Add all the tags to the tracking map
            for (final CacheEntryTag tag : tags) {
                //Record that this tag type is stored in this cache
                final String tagType = tag.getTagType();
                final Set<Ehcache> caches = taggedCaches.getUnchecked(tagType);
                caches.add(cache);

                //Record the tag->key association
                final Set<Object> taggedKeys = cacheKeys.getUnchecked(tag);
                taggedKeys.add(key);
            }
        }
    }

    /** If the element has a TaggedCacheKey remove the tag associations */
    protected void removeElement(Ehcache cache, Element element) {
        final Set<CacheEntryTag> tags = this.getTags(element);

        //Check if the key is tagged
        if (tags != null && !tags.isEmpty()) {
            final String cacheName = cache.getName();
            final LoadingCache<CacheEntryTag, Set<Object>> cacheKeys =
                    taggedCacheKeys.getIfPresent(cacheName);

            //If there are tracked tagged keys remove matching tags
            if (cacheKeys != null) {
                final Object key = element.getObjectKey();

                logger.debug(
                        "Tracking removing key cache {} with tag {} : {}", cacheName, tags, key);

                for (final CacheEntryTag tag : tags) {
                    final Set<Object> taggedKeys = cacheKeys.getIfPresent(tag);

                    //Remove the tagged key
                    if (taggedKeys != null) {
                        taggedKeys.remove(key);
                    }
                }
            }
        }
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        putElement(cache, element);
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        removeElement(cache, element);
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        removeElement(cache, element);
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        removeElement(cache, element);
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        final String cacheName = cache.getName();
        final LoadingCache<CacheEntryTag, Set<Object>> cacheKeys =
                taggedCacheKeys.getIfPresent(cacheName);
        if (cacheKeys != null) {
            logger.debug("Tracking remove all tagged keys for cache {}", new Object[] {cacheName});
            cacheKeys.invalidateAll();
        }
    }
}
