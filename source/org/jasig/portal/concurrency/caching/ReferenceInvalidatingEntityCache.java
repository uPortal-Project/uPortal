/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.concurrency.caching;

import java.util.Date;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.services.LogService;
/**
 * Reference implementation of <code>IEntityCache</code> that is meant
 * for a multi-server environment in which updates to cached entities may
 * occur on remote servers, making the local copy of the entity invalid.
 * <p>
 * Cache entries are wrapped in a <code>CacheEntry</code> that records
 * their creation time.  At intervals, cleanupCache() is called by the
 * cache's cleanup thread.  When this happens, the class retrieves
 * invalidation notices from its invalidation store and purges stale entries.
 * <p>
 * When we update a cache entry, we invalidate first and replace second
 * to ensure that the cache entry creation time is no earlier than the
 * invalidation timestamp.  This prevents the cache entry from being purged
 * by its own invalidation notice.
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceInvalidatingEntityCache extends ReferenceEntityCache
{
    private static RDBMCachedEntityInvalidationStore invalidationStore;
    private long lastUpdateMillis = 0;

    // Wrapper records the time a cache entry was created.
    class CacheEntry implements IBasicEntity
    {
        protected IBasicEntity ent;
        protected Date creationTime = new Date();

        protected CacheEntry(IBasicEntity entity) {
            super();
            ent = entity;
        }
        public EntityIdentifier getEntityIdentifier() {
            return ent.getEntityIdentifier();
        }
        public Class getType() {
            return getEntityIdentifier().getType();
        }
        public String getKey() {
            return getEntityIdentifier().getKey();
        }
        public IBasicEntity getEntity() {
            return ent;
        }
        public Date getCreationTime() {
            return creationTime;
        }
    }
/**
 * ReferenceInvalidatingEntityCache constructor comment.
 */
public ReferenceInvalidatingEntityCache(Class type, int maxSize, int maxUnusedTime, int sweepInterval )
throws CachingException
{
    super(type, maxSize, maxUnusedTime, sweepInterval);
}
/**
 * Wrap the incoming entity and add to the cache.
 * @param IBasicEntity entity - the entity to be added to the cache.
 */
public void add(IBasicEntity entity) throws CachingException
{
    super.add(new CacheEntry(entity));
}
/**
 * Remove stale entries from the cache.
 */
public void cleanupCache()
{
    String msg = null;
    long start = 0, end = 0;
    java.sql.Timestamp ts;

    start = System.currentTimeMillis();
    debug("ENTERING " + this + " cleanupCache() ");

    if ( ! getCache().isEmpty() )
    {
        removeInvalidEntities();
        super.cleanupCache();
    }

    end = System.currentTimeMillis();
    msg = "LEAVING " + this + " cleanupCache(); total time: " + (end - start) + "ms";
    debug(msg);
}
/**
 * May want to do something with the invalidator thread.
 * @throws Throwable
 */
protected void finalize() throws Throwable
{
    super.finalize();
}
/**
 * Unwraps and returns the cached entity.
 * @param String key - the key of the entity.
 * @return org.jasig.portal.IBasicEntity
 */
public IBasicEntity get(String key) {
    CacheEntry entry = (CacheEntry)primGet(key);
    return ( entry != null ) ? entry.getEntity() : null;
}
/**
 * @return org.jasig.portal.concurrency.caching.RDBMCachedEntityInvalidationStore
 */
private static synchronized RDBMCachedEntityInvalidationStore getInvalidationStore()
throws CachingException
{
    if ( invalidationStore == null )
        { invalidationStore = new RDBMCachedEntityInvalidationStore(); }
    return invalidationStore;
}
/**
 * @param entity org.jasig.portal.IBasicEntity
 */
public void invalidate(IBasicEntity entity) throws CachingException
{
    getInvalidationStore().add(entity);
}
/**
 * Returns the WRAPPED cached entity.
 * @param String key - the key of the entity.
 * @return org.jasig.portal.IBasicEntity
 */
private IBasicEntity primGet(String key) {
    return super.get(key);
}
/**
 * @param IEntity entity - the entity to be un-cached.
 */
private void primRemove(String key) throws CachingException
{
    super.remove(key);
}
/**
 * @param String key - the key of the entity to be un-cached.
 */
public void remove(String key) throws CachingException
{
    IBasicEntity ent = get(key);
    if ( ent != null )
    {
        invalidate(ent);
        primRemove(key);
    }
}
/**
 * Retrieves invalidations that were added to the store since the last
 * time we checked, and removes corresponding entries from the cache
 * if they were added before the invalidation timestamp.
 */
public void removeInvalidEntities()
{
    CachedEntityInvalidation[] invalidations = null;
    long nowMillis = System.currentTimeMillis();
    Date lastUpdate = new Date(lastUpdateMillis);
    int removed = 0;

    debug("ReferenceInvalidatingEntityCache.removeInvalidEntries(): " + getEntityType() +
          " checking for cache invalidations added since: " + lastUpdate);
    try
    {
        invalidations = getInvalidationStore().findAfter(lastUpdate, getEntityType(), null);

        debug("ReferenceInvalidatingEntityCache.removeInvalidEntries(): " + getEntityType() +
              " retrieved " + invalidations.length + " invalidations.");

        for ( int i=0; i<invalidations.length; i++ )
        {
            String key = invalidations[i].getKey();

            CacheEntry entry = (CacheEntry)primGet(key);

            if ( entry != null &&
                entry.getCreationTime().before(invalidations[i].getInvalidationTime()) )
            {
                primRemove( key );
                removed++;
            }
        }

        debug("ReferenceInvalidatingEntityCache.removeInvalidEntries(): " + getEntityType() +
              " removed " + removed + " cache entries.");
    }
    catch (CachingException ce)
    {
        LogService.log(LogService.ERROR,
            "ReferenceInvalidatingEntityCache.removeInvalidEntries(): " + ce.getMessage());
    }

    lastUpdateMillis = nowMillis;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
    return "ReferenceInvalidatingEntityCache for " + getEntityType().getName();
}
/**
 * First invalidate, then cache the incoming entity.
 * @param IBasicEntity entity - the entity to be updated in the cache.
 */
public void update(IBasicEntity entity) throws CachingException
{
    invalidate(entity);
    add(entity);
}
}
