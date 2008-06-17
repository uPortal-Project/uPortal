/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
