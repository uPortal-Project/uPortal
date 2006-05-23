/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
    /**
     * Commons Logging instance configured to log as the runtime class.
     * Available to subclasses for logging, and used in the implementation of
     * debug() so that debug messages will log as the runtime class.
     */
    protected final Log log = LogFactory.getLog(getClass());
    
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
    cleanupThread.setDaemon(true);
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
        { throw new CachingException("Problem adding entity type " + type, ex); }
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
 * Print a debug message prepended with the current time.
 * <p>This method is deprecated.  Instead use Commons Logging directly.  Backing 
 * logging implementations such as log4j have capabilities for including
 * (and formatting) the timestamps of log entries without that timestamp
 * being explicitly generated here in the logging client.</p>
 * 
 * <p>While this method internally guards against needless additional object 
 * creation, code calling this method should itself use the isDebugEnabled()
 * guard in order to avoid generating intermediary Strings as the <code>msg</code> 
 * argument to this method.</p>
 * @deprecated Use Commons Logging directly.
 */
void debug(String msg)
{
    if (log.isDebugEnabled()) {
        java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
        log.debug(ts + " : " + msg);
    }
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
