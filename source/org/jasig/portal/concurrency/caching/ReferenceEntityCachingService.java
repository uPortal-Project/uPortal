/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency.caching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.concurrency.IEntityCachingService;
import org.jasig.portal.properties.PropertiesManager;
/**
 * Caching service reference implementation.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceEntityCachingService implements IEntityCachingService
{
    // Singleton instance:
    private static IEntityCachingService singleton;

    // Our cache of caches:
    private Map caches = new HashMap(10);

    // The following properties can be overridden via settings in portal.properties.
    // Here they are initialized to their defaults.

    // Will there be peer caches on other servers?
    boolean multiServer = false;

    // Default maximum cache size.
    int defaultMaxCacheSize = 1000;

    // The interval between cache updates; defaults to 60 seconds.
    int defaultSweepIntervalMillis = 60 * 1000;

    // The interval after which a cache entry may be purged if it has not
    // been touched.  Defaults to 30 minutes.
    int defaultMaxIdleTimeMillis = 30 * 60 * 1000;
    
    /* Fudge factor in milliseconds, for reconciling system clocks on 
     * different hosts.  We retrieve invalidations between now and
     * the time of our last retrieval -- plus the fudge factor.  
     * Only used when inMemory == false.  Defaults to 5000.
     */
    int clockToleranceMillis = 5000;
/**
 * ReferenceEntityCachingService constructor comment.
 */
public ReferenceEntityCachingService() throws CachingException
{
    super();
    initialize();
}
/**
 * Adds the entity to the cache.
 * @param ent org.jasig.portal.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void add(IBasicEntity ent) throws CachingException
{
    getCache(ent.getEntityIdentifier().getType()).add(ent);
}
/**
 * Create a cache for a specific entity type.
 */
IEntityCache createCache(Class type) throws CachingException
{
    int max = 0, idle = 0, interval = 0;
    String service = "org.jasig.portal.concurrency.IEntityCachingService";
    String sep = ".";

    try
    {
        max = PropertiesManager.getPropertyAsInt(service + sep + type.getName() + sep + "maxCacheSize");
    }
    catch ( Exception e )
        { max = defaultMaxCacheSize; }

    try
    {
        idle = PropertiesManager.getPropertyAsInt(service + sep + type.getName() + sep + "MaxIdleTime");
        idle *= 1000;
    }
    catch ( Exception e )
        { idle = defaultMaxIdleTimeMillis; }

    try
    {
        interval = PropertiesManager.getPropertyAsInt(service + sep + type.getName() + sep + "sweepInterval");
        interval *= 1000;
    }
    catch ( Exception e )
        { interval = defaultSweepIntervalMillis; }

    return newCache(type, max, idle, interval);
}
/**
 * Create a cache for each known entity type.
 */
private void createCaches() throws CachingException
{
    for ( Iterator types = EntityTypes.singleton().getAllEntityTypes(); types.hasNext() ;)
    {
        Class type = (Class) types.next();
        IEntityCache cache = createCache(type);
        getCaches().put( type, cache );
    }
}
/**
 * Returns the cached entity identified by type and key.
 * @param type Class
 * @param key String
 * @return IBasicEntity entity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IBasicEntity get(Class type, String key) throws CachingException {
    return getCache(type).get(key);
}
/**
 * Returns the <code>IEntityCache</code> for <code>type</code>.
 * @param type Class
 * @return IEntityCache
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IEntityCache getCache(Class type) throws CachingException {
    IEntityCache c = (IEntityCache)getCaches().get(type);
    if ( c == null )
    {
        c = createCache(type);
        getCaches().put(type, c);
    }
    return c;
}
/**
 * @return java.util.Map
 */
protected java.util.Map getCaches() {
    return caches;
}
/**
 */
private void initialize() throws CachingException
{
    loadDefaultProperties();
//  createCaches();
}
/**
 * Loads default properties applied to caches if not specifically
 * overridden.
 */
private void loadDefaultProperties()
{
    try
    {
        multiServer = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.concurrency.multiServer");
    }
    catch ( Exception e ) { /* defaults to false */ }

    try
    {
        int defaultSweepIntervalSecs = PropertiesManager.getPropertyAsInt
            ("org.jasig.portal.concurrency.IEntityCachingService.defaultSweepInterval");
        defaultSweepIntervalMillis = defaultSweepIntervalSecs * 1000;
    }
    catch ( Exception ex ) { /* defaults to 60 seconds */ }

    try
    {
        defaultMaxCacheSize = PropertiesManager.getPropertyAsInt
            ("org.jasig.portal.concurrency.IEntityCachingService.defaultMaxCacheSize");
    }
    catch ( Exception ex ) { /* defaults to 1000 */ }

    try
    {
        int defaultMaxIdleTimeSecs = PropertiesManager.getPropertyAsInt
            ("org.jasig.portal.concurrency.IEntityCachingService.defaultMaxIdleTime");
        defaultMaxIdleTimeMillis = defaultMaxIdleTimeSecs * 1000;
    }
    catch ( Exception ex ) { /* defaults to 30 minutes */ }
    
    if ( multiServer ) {
        try
        {
            int clockTolerance = PropertiesManager.getPropertyAsInt
                ("org.jasig.portal.concurrency.clockTolerance");
            clockToleranceMillis = clockTolerance;
        }
        catch ( Exception ex ) { /* defaults to 5000. */ }
    }

}
/**
 * Factory method returns a new instance of <code>IEntityCache</code>
 * for <code>type</code>.
 * @param type Class
 * @param maxSize int - the maximum size of the cache.
 * @param maxIdleTime int - the idle time in milliseconds after which a cache entry may be purged.
 * @param sweepInterval int - the period of time in milliseconds between cache sweeps.
 * @return IEntityCache
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IEntityCache newCache(Class type, int maxSize, int maxIdleTime, int sweepInterval )
throws CachingException
{
    return (multiServer)
        ? new ReferenceInvalidatingEntityCache( type, maxSize, maxIdleTime, sweepInterval, clockToleranceMillis )
        : new ReferenceEntityCache( type, maxSize, maxIdleTime, sweepInterval );
}
/**
 * Removes the cached entity identified by type and key from the cache
 * and notifies peer caches.
 * @param type Class
 * @param key String
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void remove(Class type, String key) throws CachingException
{
    getCache(type).remove(key);
}
/**
 * @param newCaches java.util.Map
 */
protected void setCaches(java.util.Map newCaches) {
    caches = newCaches;
}
/**
 * @return org.jasig.portal.concurrency.IEntityCachingService
 */
public static synchronized IEntityCachingService singleton() throws CachingException
{
    if ( singleton == null )
        { singleton = new ReferenceEntityCachingService(); }
    return singleton;
}
/**
 * Updates the entity in the cache and notifies peer caches.
 * @param ent org.jasig.portal.IBasicEntity
 * @exception org.jasig.portal.concurrency.CachingException
 */
public void update(IBasicEntity ent) throws CachingException
{
    getCache(ent.getEntityIdentifier().getType()).update(ent);
}
}
