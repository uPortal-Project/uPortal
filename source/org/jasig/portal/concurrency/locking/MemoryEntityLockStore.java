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

package org.jasig.portal.concurrency.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;
import org.jasig.portal.utils.SmartCache;
/**
 * In-memory store for <code>IEntityLocks</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class MemoryEntityLockStore implements IEntityLockStore
{
    private static IEntityLockStore singleton;

    // The lock store is a Map that contains a SmartCache for each entity type:
    private Map lockCache;
/**
 * MemoryEntityLockStore constructor comment.
 */
public MemoryEntityLockStore() {
    super();
    initializeCache();
}
/**
 * Adds this IEntityLock to the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
public void add(IEntityLock lock) throws LockingException
{
    primAdd(lock, lock.getExpirationTime());
}
/**
 * Deletes this IEntityLock from the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
public void delete(IEntityLock lock) throws LockingException
{
    Map m = getLockCache(lock.getEntityType());
    synchronized (m) {
        m.remove(getCacheKey(lock));
    }
}
public void deleteAll()
{
    initializeCache();
}
/**
 * Deletes the expired IEntityLocks from the underlying store.
 * @param expiration java.util.Date
 */
public void deleteExpired(java.util.Date expiration) throws LockingException
{
    // let SmartCache handle it.
}
/**
 * Returns an IEntityLock[] based on the params, any or all of which may be null.  A
 * null param means any value, so <code>find(myType,myKey,null,null,null)</code> will
 * return all <code>IEntityLocks</code> for myType and myKey.
 *
 * @return org.jasig.portal.concurrency.locking.IEntityLock[]
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param expiration Date
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] find
   (Class entityType,
    String entityKey,
    Integer lockType,
    java.util.Date expiration,
    String lockOwner)
throws LockingException
{
    List locks = new ArrayList();
    Map cache = null;
    Collection caches = null;
    Iterator cacheIterator = null;
    Iterator cacheKeyIterator = null;
    Iterator keyIterator = null;
    IEntityLock lock = null;

    if ( entityType == null )
    {
        caches = getLockCache().values();
    }
    else
    {
        caches = new ArrayList(1);
        caches.add(getLockCache(entityType));
    }

    cacheIterator = caches.iterator();
    while ( cacheIterator.hasNext() )
    {
        cache = (Map) cacheIterator.next();
        cacheKeyIterator = cache.keySet().iterator();
        List keys = new ArrayList();

        // Synchronize on the cache only while collecting its keys.  There is some
        // exposure here.
        synchronized (cache) {
        while ( cacheKeyIterator.hasNext() )
            { keys.add(cacheKeyIterator.next()); }
        }

        keyIterator = keys.iterator();
        while ( keyIterator.hasNext() )
        {
            lock = getLockFromCache(keyIterator.next(), cache);
            if ( ( lock != null ) &&
                 ( (entityKey == null)  || (entityKey.equals(lock.getEntityKey())) ) &&
                 ( (lockType == null)   || (lockType.intValue() == lock.getLockType()) ) &&
                 ( (lockOwner == null)  || (lockOwner.equals(lock.getLockOwner())) ) &&
                 ( (expiration == null) || (expiration.equals(lock.getExpirationTime())) )
               )
                  { locks.add(lock); }
        }
    }
    return ((IEntityLock[])locks.toArray(new IEntityLock[locks.size()]));
}
/**
 * Returns this lock if it exists in the store.
 * @return IEntityLock
 * @param key IEntityLock
 */
public IEntityLock find(IEntityLock lock) throws LockingException
{
    IEntityLock foundLock = null;
    Map m = getLockCache(lock.getEntityType());
    foundLock = getLockFromCache(getCacheKey(lock), m);

    if ( foundLock != null )
    {
        if ( lock.getLockType() != foundLock.getLockType() ||
           ! lock.getExpirationTime().equals(foundLock.getExpirationTime()) )
                { foundLock = null; }
    }

    return foundLock;
}
/**
 * Returns an IEntityLock[] containing unexpired locks, based on the params,
 * any or all of which may be null EXCEPT FOR <code>expiration</code>.  A null
 * param means any value, so <code> find(expir,myType,myKey,null,null)</code>
 * will return all <code>IEntityLocks</code> for myType and myKey unexpired
 * as of <code>expir</code>.
 *
 * @param expiration Date
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] findUnexpired
   (java.util.Date expiration,
    Class entityType,
    String entityKey,
    Integer lockType,
    String lockOwner)
throws LockingException
{
    IEntityLock[] locks = find(entityType, entityKey, lockType, null, lockOwner);
    List lockAL = new ArrayList(locks.length);
    for ( int i =0; i<locks.length; i++)
    {
        if ( locks[i].getExpirationTime().after(expiration) )
            { lockAL.add(locks[i]); }
    }
    return ((IEntityLock[])lockAL.toArray(new IEntityLock[lockAL.size()]));
}
/**
 * @param lock org.jasig.portal.concurrency.locking.IEntityLock
 */
private String getCacheKey(IEntityLock lock) {
    return lock.getEntityKey() + lock.getLockOwner();
}
/**
 * @return java.util.Map
 */
private java.util.Map getLockCache()
{
    return lockCache;
}
/**
 * @return java.util.Map
 */
private synchronized Map getLockCache(Class type)
{
    Map m  = (Map) getLockCache().get(type);
    if ( m == null )
    {
        m = new SmartCache();
        getLockCache().put(type, m);
    }
    return m;
}
private IEntityLock getLockFromCache(Object cacheKey, Map cache)
{
    synchronized (cache) {
        return (IEntityLock) cache.get(cacheKey);
    }
}
/**
 *
 */
private void initializeCache()
{
    lockCache = new HashMap(10);
}
/**
 * @param newLockCache java.util.Map
 */
private void setLockCache(java.util.Map newLockCache) {
    lockCache = newLockCache;
}
/**
 * @return org.jasig.portal.concurrency.locking.IEntityLockStore
 */
public static synchronized IEntityLockStore singleton()
{
    if ( singleton == null )
        { singleton = new MemoryEntityLockStore(); }
    return singleton;
}
/**
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 * @param expiration java.util.Date
 */
public void update(IEntityLock lock, java.util.Date newExpiration)
throws LockingException
{
    update(lock, newExpiration, null);
}
/**
 * Make sure the store has a reference to the lock, and then add the lock 
 * to refresh the SmartCache wrapper.  
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 * @param expiration java.util.Date
 * @param lockType Integer
 */
public void update(IEntityLock lock, java.util.Date newExpiration, Integer newLockType)
throws LockingException
{
    if ( find(lock) == null )
        { throw new LockingException("Problem updating " + lock + " : not found in store."); }
    primAdd(lock, newExpiration);
}

/**
 * Adds this IEntityLock to the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
private void primAdd(IEntityLock lock, Date expiration) throws LockingException
{
    long now = System.currentTimeMillis();
    long willExpire = expiration.getTime();
    long cacheIntervalSecs = (willExpire - now) / 1000;
    SmartCache sc = (SmartCache)getLockCache(lock.getEntityType());

    synchronized (sc) {
        sc.put( getCacheKey(lock), lock, (cacheIntervalSecs) );
    }
}
}
