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

import java.util.Map;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference implementation of IEntityCache.  Each cache holds entities of
 * a single type in an LRUCache, a kind of HashMap.  Synchronization for
 * get(), add() and remove() is handled by the LRUCache.  At intervals,
 * the cleanupThread kicks off a sweep of the cache to trim it down to
 * its maximum size.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.concurrency.caching.LRUCache
 */
public class ReferenceEntityCache implements IEntityCache
{
    private static final Log log = LogFactory.getLog(ReferenceEntityCache.class);
    protected Map cache;
    protected Class entityType;
    protected String simpleTypeName;
    protected static int threadID = 0;

    // The interval between cache updates; defaults to 60 seconds.
    protected int sweepIntervalMillis = 60 * 1000;

   // An alarm clock to kick off the cache refresh.
    protected Thread cleanupThread = null;
    protected class CacheSweeper implements Runnable
    {
        protected CacheSweeper() {
            super();
        }
        public void run() {
            for (;;)
            {
                try { Thread.sleep(sweepIntervalMillis); }
                catch(InterruptedException e) {}
                cleanupCache();
            }
        }
    }

/**
 * ReferenceEntityCache constructor comment.
 */
public ReferenceEntityCache(Class type, int maxSize, int maxUnusedTime, int sweepInterval)
throws CachingException
{
    super();
    initializeEntityType(type);
    entityType = type;
    sweepIntervalMillis = sweepInterval;
    setCache(new LRUCache(maxSize, maxUnusedTime));
    String threadName = "uPortal ReferenceEntityCache sweeper thread #" + ++threadID;
    cleanupThread = new Thread(new CacheSweeper(), threadName);
    cleanupThread.start();
}

/**
 * Checks that <code>entity</code> is the same type as, i.e., could be cast
 * to, the cache type.
 *
 * @param entity the entity to be added to the cache.
 */
public void add(IBasicEntity entity) throws CachingException
{
    if ( ! this.getEntityType().isAssignableFrom(entity.getEntityIdentifier().getType()) )
        { throw new CachingException("Problem adding " + entity + ": entity type is incompatible with cache.");}

    getCache().put(entity.getEntityIdentifier().getKey(), entity);
}

/**
 *
 */
private void initializeEntityType(Class type) throws CachingException
{
    try
        { EntityTypes.addIfNecessary(type, "Added by ReferenceEntityCache"); }
    catch (Exception ex)
        { throw new CachingException("Problem adding entity type " + type +
          " : " + ex.getMessage()); }
}

/**
 * Remove stale entries from the cache.
 */
public void cleanupCache()
{
    int before = size();
    debug("ENTERING ReferenceEntityCache.cleanupCache() for " + getSimpleTypeName() + " : number of entries: " + before);
    ((LRUCache)getCache()).sweepCache();
    debug("LEAVING ReferenceEntityCache.cleanupCache() for " + getSimpleTypeName() + " : removed " +
        (before - size()) + " cache entries.");
}

/**
 * Remove all entries from the cache.
 */
public void clearCache()
{
    getCache().clear();
}

/**
 */
void debug(String msg)
{
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
    log.debug(ts + " : " + msg);
}

/**
 * @param key the key of the entity.
 * @return org.jasig.portal.concurrency.IBasicEntity
 */
public IBasicEntity get(String key) {
    return (IBasicEntity) getCache().get(key);
}

/**
 * @return java.util.Map
 */
protected java.util.Map getCache() {
    return cache;
}

/**
 * @return java.lang.Class
 */
public final java.lang.Class getEntityType() {
    return entityType;
}

/**
 * @param key the key of the entity to be un-cached.
 */
public void remove(String key) throws CachingException
{
    cache.remove(key);
}

/**
 * @param newCache java.util.Map
 */
protected void setCache(java.util.Map newCache) {
    cache = newCache;
}

/**
 * @return int
 */
public int size() {
    return getCache().size();
}

/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
    return "ReferenceEntityCache for " + getSimpleTypeName();
}

private String getSimpleTypeName() 
{
    if ( simpleTypeName == null )
    {
        String name = getEntityType().getName();
        while (name.indexOf('.') >= 0) 
            { name = name.substring(name.indexOf('.')+1); }
        simpleTypeName = name;
    }
    return simpleTypeName;
}

/**
 * @param entity the entity to be updated in the cache.
 */
public void update(IBasicEntity entity) throws CachingException
{
    add(entity);
}

}
