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

import java.util.Date;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.IEntityLockService;
import org.jasig.portal.concurrency.LockingException;
import org.jasig.portal.services.LogService;
/**
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceEntityLockService implements IEntityLockService
{
    // Singleton instance:
    private static IEntityLockService singleton = null;

    // Store for IEntityLocks:
    private IEntityLockStore lockStore = null;

    // Locking properties, initialized with default values, are settable
    // via portal.properties:

    // Are we running in a multi-server environment?  If so, the lock store
    // will be in persistent storage.
    private boolean multiServer = false;

    // Lifetime of a lock in seconds, defaults to 5 minutes.
    private int defaultLockPeriod = 300;

    /* Fudge factor in milliseconds, extends the apparent expiration times
     * of potentially conflicting locks beyond their actual expirations.
     * We only use it when checking for locking conflicts and then only if
     * inMemory == false.  Defaults to 5000.
     */
    private int lockToleranceMillis = 5000;
/**
 * ReferenceEntityLockingService constructor comment.
 */
public ReferenceEntityLockService() throws LockingException
{
    super();
    initialize();
}
/**
 * Attempts to change the lock's <code>lockType</code> to <code>newType</code>.
 * @param lock IEntityLock
 * @param newType int
 * @exception org.jasig.portal.concurrency.LockingException
 */
public void convert(IEntityLock lock, int newType) throws LockingException
{
    convert(lock, newType, defaultLockPeriod);
}
/**
 * Attempts to change the lock's <code>lockType</code> to <code>newType</code>.
 * @param lock IEntityLock
 * @param newType int
 * @param newDuration int
 * @exception org.jasig.portal.concurrency.LockingException
 */
public void convert(IEntityLock lock, int newType, int newDuration) throws LockingException
{
    if ( lock.getLockType() == newType )
       { throw new LockingException("Could not convert " + lock + " : old and new lock TYPEs are the same."); }

    if ( ! isValidLockType(newType)  )
        { throw new LockingException("Could not convert " + lock + " : lock TYPE " + newType + " is invalid."); }

    if ( ! isValid(lock) )
        { throw new LockingException("Could not convert " + lock + " : lock is invalid."); }

    if ( newType == WRITE_LOCK && retrieveLocks(lock.getEntityType(), lock.getEntityKey(), null).length > 1 )
        { throw new LockingException("Could not convert " + lock + " : another lock already exists."); }

    if ( newType == READ_LOCK )
        { /* Can always convert to READ */ }

    Date newExpiration = getNewExpiration(newDuration);
    getLockStore().update(lock, newExpiration, new Integer(newType));
    ((EntityLockImpl)lock).setLockType(newType);
    ((EntityLockImpl)lock).setExpirationTime(newExpiration);
}

/**
 * Answer if this <code>IEntityLock</code> exists in the store.
 * @param lock
 * @return boolean
 */
public boolean existsInStore(IEntityLock lock) throws LockingException
{
    Class entityType = lock.getEntityType();
    String key = lock.getEntityKey();
    Integer lockType = new Integer(lock.getLockType());
    Date expiration = lock.getExpirationTime();
    String owner = lock.getLockOwner();
    IEntityLock[] lockArray = getLockStore().find(entityType, key, lockType, expiration, owner);

    return (lockArray.length == 1);
}

/**
 * @return int
 */
private int getDefaultLockPeriod() {
    return defaultLockPeriod;
}
/**
 * @return org.jasig.portal.concurrency.locking.IEntityLockStore
 */
private IEntityLockStore getLockStore() {
    return lockStore;
}
/**
 * @return int
 */
private int getLockToleranceMillis() {
    return lockToleranceMillis;
}
/**
 * @return java.util.Date
 */
private Date getNewExpiration(int durationSecs)
{
    return new Date(System.currentTimeMillis() + (durationSecs*1000));
}
/**
 * @exception LockingException
 */
private void initialize() throws LockingException
{
    String eMsg = null;

    try
    {
        multiServer = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.concurrency.multiServer");

        lockStore = ( multiServer )
            ? RDBMEntityLockStore.singleton()
            : MemoryEntityLockStore.singleton();
    }
    catch ( Exception e )
    {
        eMsg = "ReferenceEntityLockingService.initialize(): Failed to instantiate entity lock store. " + e;
        LogService.log(LogService.ERROR, eMsg);
        throw new LockingException(eMsg);
    }

    try
    {
        int lockDuration = PropertiesManager.getPropertyAsInt
            ("org.jasig.portal.concurrency.IEntityLockService.defaultLockDuration");
        setDefaultLockPeriod(lockDuration);
    }
    catch ( Exception ex ) { /* defaults to 5 minutes. */ }

    if ( multiServer ) {
        try
        {
            int lockTolerance = PropertiesManager.getPropertyAsInt
                ("org.jasig.portal.concurrency.clockTolerance");
            setLockToleranceMillis(lockTolerance);
        }
        catch ( Exception ex ) { /* defaults to 0. */ }
    }
}
/**
 * Answers if the entity represented by the entityType and entityKey already
 * has a lock of some type.
 * 
 * @param entityType
 * @param entityKey
 * @exception org.jasig.portal.concurrency.LockingException
 */
private boolean isLocked(Class entityType, String entityKey) throws LockingException
{
    return isLocked(entityType, entityKey, null);
}
/**
 * Answers if the entity represented by entityType and entityKey has one
 * or more locks.  Param <code>lockType</code> can be null.
 *
 * @param entityType
 * @param entityKey
 * @param lockType (optional)
 * @exception org.jasig.portal.concurrency.LockingException
 */
private boolean isLocked(Class entityType, String entityKey, Integer lockType) throws LockingException
{
    IEntityLock[] locks = retrieveLocks(entityType, entityKey, lockType);
    return locks.length > 0;
}
/**
 * @return boolean
 */
private boolean isMultiServer() {
    return multiServer;
}
/**
 * @param lock IEntityLock
 * @return boolean
 */
private boolean isUnexpired(IEntityLock lock)
{
    return lock.getExpirationTime().getTime() > System.currentTimeMillis();
}
/**
 * Answers if this <code>IEntityLock</code> represents a lock that is still
 * good.  To be valid, a lock must exist in the underlying store and be
 * unexpired.
 *
 * @param lock IEntityLock
 * @exception org.jasig.portal.concurrency.LockingException
 */
public boolean isValid(IEntityLock lock) throws LockingException
{
    return isUnexpired(lock) && existsInStore(lock);
}
/**
 *
 */
private boolean isValidLockType(int lockType)
{
    return ( (lockType == READ_LOCK) || (lockType == WRITE_LOCK) );
}

/**
 * Returns a lock for the entity, lock type and owner if no conflicting locks exist.
 * @param entityType
 * @param entityKey
 * @param lockType
 * @param owner
 * @return org.jasig.portal.groups.IEntityLock
 * @exception LockingException
 */
public IEntityLock newLock(Class entityType, String entityKey, int lockType, String owner)
throws LockingException
{
    return newLock(entityType, entityKey, lockType, owner, defaultLockPeriod);
}

/**
 * Returns a lock for the entity, lock type and owner if no conflicting locks exist.
 * @param entityType
 * @param entityKey
 * @param lockType
 * @param owner
 * @param durationSecs
 * @return org.jasig.portal.groups.IEntityLock
 * @exception LockingException
 */
public IEntityLock newLock(Class entityType, String entityKey, int lockType, String owner, int durationSecs)
throws LockingException
{
    Date expires = getNewExpiration(durationSecs);
    IEntityLock lock = new EntityLockImpl(entityType, entityKey, lockType, expires, owner, this);

    if ( lockType == WRITE_LOCK && isLocked(entityType, entityKey ) )
        { throw new LockingException("Could not create lock: entity already locked."); }

    if ( lockType == READ_LOCK )
    {
        IEntityLock[] locks = retrieveLocks(entityType, entityKey, null);
        for ( int i = 0; i<locks.length; i++ )
        {
            if ( locks[i].getLockType() == WRITE_LOCK )
                { throw new LockingException("Could not create lock: entity already write locked."); }
            if ( locks[i].getLockOwner().equals(owner) )
                { throw new LockingException("Could not create lock: owner " + owner + " already holds lock on this entity."); }
        }
    }

    getLockStore().add(lock);

    return lock;
}
/**
 * Returns a lock for the entity, lock type and owner if no conflicting locks exist.
 * @return org.jasig.portal.groups.IEntityLock
 * @param entityID org.jasig.portal.EntityIdentifier
 * @param lockType int
 * @param owner String
 * @exception LockingException
 */
public IEntityLock newLock(EntityIdentifier entityID, int lockType, String owner)
throws LockingException
{
    return newLock(entityID.getType(), entityID.getKey(), lockType, owner, defaultLockPeriod);
}
/**
 * Returns a lock for the entity, lock type and owner if no conflicting locks exist.
 * @return org.jasig.portal.groups.IEntityLock
 * @param entityID org.jasig.portal.EntityIdentifier
 * @param lockType int
 * @param owner String
 * @param durationSecs int
 * @exception LockingException
 */
public IEntityLock newLock(EntityIdentifier entityID, int lockType, String owner, int durationSecs)
throws LockingException
{
    return newLock(entityID.getType(), entityID.getKey(), lockType, owner, durationSecs);
}
/**
 * Releases the <code>IEntityLock</code>.
 * @param lock IEntityLock
 * @exception LockingException
 */
public void release(IEntityLock lock) throws LockingException
{
    getLockStore().delete(lock);
    ((EntityLockImpl)lock).setExpirationTime(new Date(0));
}
/**
 * Extends the expiration time of the lock by some service-defined increment.
 * @param lock IEntityLock
 * @exception LockingException
 */
public void renew(IEntityLock lock) throws LockingException
{
    renew(lock, defaultLockPeriod);
}
/**
 * Extends the expiration time of the lock by some service-defined increment.
 * @param lock IEntityLock
 * @exception LockingException
 */
public void renew(IEntityLock lock, int duration) throws LockingException
{
    if ( isValid(lock) )
    {
        Date newExpiration = getNewExpiration(duration);
        getLockStore().update(lock, newExpiration);
        ((EntityLockImpl)lock).setExpirationTime(newExpiration);
    }
    else
        { throw new LockingException("Could not renew " + lock + " : lock is invalid."); }
}
/**
 * Returns an IEntityLock[] containing unexpired locks for the entityType, entityKey
 * and lockType.  Param <code>lockType</code> can be null.
 *
 * @param entityType
 * @param entityKey
 * @param lockType (optional)
 * @exception LockingException
 */
private IEntityLock[] retrieveLocks(Class entityType, String entityKey, Integer lockType) throws LockingException
{
    Date expiration = ( multiServer )
        ? new Date(System.currentTimeMillis() - getLockToleranceMillis())
        : new Date();

    return getLockStore().findUnexpired(expiration, entityType, entityKey, lockType, null);
}
/**
 * @param newDefaultLockPeriod int
 */
private void setDefaultLockPeriod(int newDefaultLockPeriod) {
    defaultLockPeriod = newDefaultLockPeriod;
}
/**
 * @param newLockToleranceMillis int
 */
private void setLockToleranceMillis(int newLockToleranceMillis) {
    lockToleranceMillis = newLockToleranceMillis;
}
/**
 * @param newMultiServer boolean
 */
private void setMultiServer(boolean newMultiServer) {
    multiServer = newMultiServer;
}
/**
 * @return org.jasig.portal.concurrency.locking.ReferenceEntityLockService
 */
public static synchronized IEntityLockService singleton() throws LockingException
{
    if ( singleton == null )
        { singleton = new ReferenceEntityLockService(); }
    return singleton;
}
}
