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

import org.jasig.portal.concurrency.IEntityLock;
import org.jasig.portal.concurrency.LockingException;

/**
 * Interface for finding and maintaining <code>IEntityLocks</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IEntityLockStore
{
/**
 * Adds this IEntityLock to the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
public void add(IEntityLock lock) throws LockingException;
/**
 * Deletes this IEntityLock from the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
public void delete(IEntityLock lock) throws LockingException;
/**
 * Delete all IEntityLocks from the store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 */
public void deleteAll() throws LockingException;
/**
 * Deletes the expired IEntityLocks from the underlying store.
 * @param expiration java.util.Date
 */
public void deleteExpired(Date expiration) throws LockingException;
/**
 * Returns an IEntityLock[] based on the params, any or all of which may be null.
 * A null param means any value, so <code>find(myType,myKey,null,null,null)</code>
 * will return all <code>IEntityLocks</code> for myType and myKey.
 *
 * @return org.jasig.portal.groups.IEntityLock[]
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param expiration Date
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] find(Class entityType, String entityKey, Integer lockType,
  Date expiration, String lockOwner)
throws LockingException;
/**
 * Returns an IEntityLock[] containing unexpired locks, based on the params,
 * any or all of which may be null EXCEPT FOR <code>expiration</code>.  A null
 * param means any value, so <code> find(expir,myType,myKey,null,null)</code>
 * will return all <code>IEntityLocks</code> for myType and myKey unexpired
 * as of <code>expiration</code>.
 *
 * @param expiration Date
 * @param entityType Class
 * @param entityKey String
 * @param lockType Integer - so we can accept a null value.
 * @param lockOwner String
 * @exception LockingException - wraps an Exception specific to the store.
 */
public IEntityLock[] findUnexpired(
    Date expiration,
    Class entityType,
    String entityKey,
    Integer lockType,
    String lockOwner)
throws LockingException;
/**
 * Updates the lock's <code>expiration</code> in the underlying store.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 * @param expiration java.util.Date
 * @param lockType Integer
 */
public void update(IEntityLock lock, Date newExpiration)
throws LockingException;
/**
 * Updates the lock's <code>expiration</code> and <code>lockType</code> in the
 * underlying store.  Param <code>lockType</code> may be null.
 * @param group org.jasig.portal.concurrency.locking.IEntityLock
 * @param expiration java.util.Date
 * @param lockType Integer
 */
public void update(IEntityLock lock, Date newExpiration, Integer newLockType)
throws LockingException;
}
