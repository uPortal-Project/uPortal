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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.EntityTypes;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.concurrency.IEntityCache;
import org.jasig.portal.concurrency.IEntityCachingService;
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

}
/**
 * Factory method returns a new instance of <code>IEntityCache</code>
 * for <code>type</code>.
 * @param type Class
 * @param maxSize int - the maximum size of the cache.
 * @param maxIdleSize int - the idle time in milliseconds after which a cache entry
 * may be purged.
 * @param sweepInterval int - the period of time in milliseconds between cache sweeps.
 * @return IEntityCache
 * @exception org.jasig.portal.concurrency.CachingException
 */
public IEntityCache newCache(Class type, int maxSize, int maxIdleTime, int sweepInterval )
throws CachingException
{
    return (multiServer)
        ? new ReferenceInvalidatingEntityCache( type, maxSize, maxIdleTime, sweepInterval )
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
