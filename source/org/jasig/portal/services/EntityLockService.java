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

package org.jasig.portal.services;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.IEntityLockService;
import org.jasig.portal.concurrency.IEntityLockServiceFactory;
import org.jasig.portal.concurrency.LockingException;

/**
  * This is a bootstrap class and facade for the IEntityLockService implementation.
  * It presents a simple api for acquiring lock objects, <code>IEntityLocks</code>,
  * that can be used to control concurrent access to portal entities in a
  * multi-server environment.  (See org.jasig.portal.concurrency.IEntityLockService
  * for a fuller description.)
  * <p>
  * Currently supported lock types are IEntityLockService.READ_LOCK and
  * IEntityLockService.WRITE_LOCK.
  * <p>
  * If I want to lock an entity for update, I ask the service for a write lock:
  * <p>
  * <code>
  *       Class type = anEntity.getClass(); // maybe hard-coded(?)<br>
  *       String key = anEntity.getKey();<br>
  *       EntityIdentifier ei = new EntityIdentifier(key, type);<br>
  *       String owner = getThePortalUserId();<br>
  *       IEntityLock lock = EntityLockService.instance().newWriteLock(ei, owner);<br>
  * </code>
  * <p>
  * Or maybe:
  * <p>
  * <code>
  *       IEntityLock lock = EntityLockService.instance().newWriteLock(ei, owner, duration);<br>
  * </code>
  * <p>
  * If there are no conflicting locks on the entity, the service returns the
  * requested lock.  If I acquire the lock, I know that no other client will be
  * able to get a conflicting lock, and from then on, I communicate with the
  * service via the lock:
  * <p>
  * <code>
  *   lock.convert(int newType); // See IEntityLockService for types.<br>
  *   lock.isValid();<br>
  *   lock.release();<br>
  *   lock.renew();<br>
  * </code>
  * <p>
  * A READ lock guarantees shared access; other clients can get READ locks
  * but not WRITE locks.  A WRITE lock guarantees exclusive access; no other
  * clients can get either READ or WRITE locks on the entity.
  *
  * @author  Dan Ellentuck
  * @version $Revision$
  */

public class EntityLockService
{
    // Singleton instance of the bootstrap class:
    private static EntityLockService instance = null;
    // The lock service:
    private IEntityLockService lockService = null;
    /** Creates new EntityLockService */
    private EntityLockService() throws LockingException
    {
        super();
        initialize();
    }
/**
 * @exception LockingException
 */
private void initialize() throws LockingException
{
    String eMsg = null;
    String factoryName =
        PropertiesManager.getProperty("org.jasig.portal.concurrency.IEntityLockServiceFactory");

    if ( factoryName == null )
    {
        eMsg = "EntityLockService.initialize(): No entry for org.jasig.portal.concurrency.IEntityLockServiceFactory in portal.properties.";
        LogService.log(LogService.ERROR, eMsg);
        throw new LockingException(eMsg);
    }

    try
    {
        IEntityLockServiceFactory lockServiceFactory =
            (IEntityLockServiceFactory)Class.forName(factoryName).newInstance();
        lockService = lockServiceFactory.newLockService();
    }
    catch (Exception e)
    {
        eMsg = "EntityLockService.initialize(): Problem creating entity lock service... " + e.getMessage();
        LogService.log(LogService.ERROR, eMsg);
        throw new LockingException(eMsg);
    }
}
    public static synchronized EntityLockService instance() throws LockingException {
        if ( instance==null ) {
            instance = new EntityLockService();
        }
        return instance;
    }
/**
 * Returns a read lock for the entity type, entity key and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param owner String
 * @exception LockingException
 */
public IEntityLock newReadLock(Class entityType, String entityKey, String owner)
throws LockingException
{
    return lockService.newLock(entityType, entityKey, IEntityLockService.READ_LOCK, owner);
}
/**
 * Returns a read lock for the entity type, entity key and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param owner String
 * @param duration int (in seconds)
 * @exception LockingException
 */
public IEntityLock newReadLock(Class entityType, String entityKey, String owner, int duration)
throws LockingException
{
    return lockService.newLock(entityType, entityKey, IEntityLockService.READ_LOCK, owner, duration);
}
/**
 * Returns a read lock for the <code>IBasicEntity</code> and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityID EntityIdentifier
 * @param owner String
 * @exception LockingException
 */
public IEntityLock newReadLock(EntityIdentifier entityID, String owner)
throws LockingException
{
    return lockService.newLock(entityID.getType(), entityID.getKey(), IEntityLockService.READ_LOCK, owner);
}
/**
 * Returns a read lock for the <code>IBasicEntity</code>, owner and duration.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityID EntityIdentifier
 * @param owner String
 * @param durationSecs int
 * @exception LockingException
 */
public IEntityLock newReadLock(EntityIdentifier entityID, String owner, int durationSecs)
throws LockingException
{
    return lockService.newLock(entityID.getType(), entityID.getKey(), IEntityLockService.READ_LOCK, owner, durationSecs);
}
/**
 * Returns a write lock for the entity type, entity key and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param owner String
 * @exception LockingException
 */
public IEntityLock newWriteLock(Class entityType, String entityKey, String owner)
throws LockingException
{
    return lockService.newLock(entityType, entityKey, IEntityLockService.WRITE_LOCK, owner);
}
/**
 * Returns a write lock for the entity type, entity key and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param owner String
 * @param durationSecs int
 * @exception LockingException
 */
public IEntityLock newWriteLock(Class entityType, String entityKey, String owner, int durationSecs)
throws LockingException
{
    return lockService.newLock(entityType, entityKey, IEntityLockService.WRITE_LOCK, owner, durationSecs);
}
/**
 * Returns a write lock for the <code>IBasicEntity</code> and owner.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityID EntityIdentifier
 * @param owner String
 * @exception LockingException
 */
public IEntityLock newWriteLock(EntityIdentifier entityID, String owner)
throws LockingException
{
    return lockService.newLock(entityID.getType(), entityID.getKey(), IEntityLockService.WRITE_LOCK, owner);
}
/**
 * Returns a write lock for the <code>IBasicEntity</code>, owner and duration.
 * @return org.jasig.portal.concurrency.locking.IEntityLock
 * @param entityID EntityIdentifier
 * @param owner String
 * @param durationSecs int
 * @exception LockingException
 */
public IEntityLock newWriteLock(EntityIdentifier entityID, String owner, int durationSecs)
throws LockingException
{
    return lockService.newLock(entityID.getType(), entityID.getKey(), IEntityLockService.WRITE_LOCK, owner, durationSecs);
}
}
