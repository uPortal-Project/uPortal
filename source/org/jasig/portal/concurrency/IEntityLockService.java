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

package org.jasig.portal.concurrency;

import org.jasig.portal.EntityIdentifier;

/**
  * Defines an api for acquiring lock objects, <code>IEntityLocks</code>, that
  * can be used to control concurrent access to portal entities.  A lock is
  * associated with a particular entity and has an <code>owner</code>, a
  * <code>lockType</code> and a service-controlled <code>expirationTime</code>.
  * Currently supported lock types are READ_LOCK and WRITE_LOCK.
  * <p>
  * If I want to lock an entity for update, I ask the service for a write lock:
  * <p>
  * <code>int lockType = IEntityLockService.WRITE_LOCK;<br>
  *       EntityIdentifier eid = myEntity.getEntityIdentifier();<br>
  *       IEntityLock lock = svc.newLock(eid, lockType, lockOwner);</code>
  * <p>
  * If there is no conflicting lock on the entity, the service responds with
  * the requested lock.  If I acquire the lock, I know that no other client will
  * get be able to get a conflicting lock.  From then on, I communicate with the
  * service via the lock:
  * <p>
  * <code>
  *   lock.convert(int newType);<br>
  *   lock.isValid();<br>
  *   lock.release();<br>
  *   lock.renew();<br>
  * </code>
  * <p>
  * A READ lock guarantees repeatable reads; other clients can get READ locks
  * but not WRITE locks.  A WRITE lock guarantees exclusive access; no other
  * clients can get either READ or WRITE locks on the entity.
  * <p>
  * NB: since the locking service is not part of a transactional or object
  * persistence framework, it has no way to enforce its own use.
  *
  * @author Dan Ellentuck
  * @version $Revision$
*/
public interface IEntityLockService {

    // The different types of locks:
    public static int READ_LOCK = 0;
    public static int WRITE_LOCK = 1;

/**
 * Attempts to change the lock's <code>lockType</code> to <code>newType</code>.
 * @param lock IEntityLock
 * @param newType int
 * @exception LockingException
 */
public void convert(IEntityLock lock, int newType) throws LockingException;
/**
 * Attempts to change the lock's <code>lockType</code> to <code>newType</code>.
 * @param lock IEntityLock
 * @param newType int
 * @param newDuration int
 * @exception org.jasig.portal.concurrency.LockingException
 */
public void convert(IEntityLock lock, int newType, int newDuration) throws LockingException;
/**
 * Answer if this <code>IEntityLock</code> exists in the store.
 * @return boolean
 * @param lock
 */
public boolean existsInStore(IEntityLock lock) throws LockingException;
/**
 * Answers if this <code>IEntityLock</code> represents a lock that is still
 * good.  To be valid, a lock must exist in the underlying store and be
 * unexpired and unreleased.
 *
 * @param lock IEntityLock
 * @exception org.jasig.portal.concurrency.LockingException
 */
public boolean isValid(IEntityLock lock) throws LockingException;
/**
 * Returns a lock for the entity, lock type and owner.
 * @return org.jasig.portal.concurrency.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param lockType int
 * @param owner String
 * @exception org.jasig.portal.concurrency.LockingException
 */
public IEntityLock newLock(Class entityType, String entityKey, int lockType, String owner)
throws LockingException;
/**
 * Returns a lock for the entity, lock type and owner.
 * @return org.jasig.portal.concurrency.IEntityLock
 * @param entityType Class
 * @param entityKey String
 * @param lockType int
 * @param owner String
 * @param durationSecs int
 * @exception org.jasig.portal.concurrency.LockingException
 */
public IEntityLock newLock(Class entityType, String entityKey, int lockType,
  String owner, int durationSecs)
throws LockingException;
/**
 * Returns a lock for the entity, lock type and owner.
 * @return org.jasig.portal.concurrency.IEntityLock
 * @param entityID EntityIdentifier
 * @param lockType int
 * @param owner String
 * @exception org.jasig.portal.concurrency.LockingException
 */
public IEntityLock newLock(EntityIdentifier entityID, int lockType, String owner)
throws LockingException;
/**
 * Returns a lock for the entity, lock type and owner.
 * @return org.jasig.portal.concurrency.IEntityLock
 * @param entityID EntityIdentifier
 * @param lockType int
 * @param owner String
 * @param durationSecs int
 * @exception org.jasig.portal.concurrency.LockingException
 */
public IEntityLock newLock(EntityIdentifier entityID, int lockType, String owner, int durationSecs)
throws LockingException;

/**
 * Releases the <code>IEntityLock</code>.
 * @param lock IEntityLock
 * @exception org.jasig.portal.concurrency.LockingException
 */
public void release(IEntityLock lock) throws LockingException;

/**
 * Extends the expiration time of the lock by the default increment.
 * @param lock IEntityLock
 * @exception org.jasig.portal.concurrency.LockingException
 */
public void renew(IEntityLock lock) throws LockingException;

/**
 * Extends the expiration time of the lock by <code>duration</code> seconds.
 * @param lock IEntityLock
 * @param duration
 * @exception LockingException
 */
public void renew(IEntityLock lock, int duration) throws LockingException;
}
