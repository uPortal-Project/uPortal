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

import java.util.Date;
/**
  * Defines a lock associated with an owner and a portal entity that
  * guarantees some degree of exclusive access to the entity, depending on
  * <code>lockType</code> and <code>expirationTime</code>.
  *
  * See IEntityLockService for the rules governing lock assignment
  * and a code example.
  *
  * @author Dan Ellentuck
  * @version $Revision$
  * @see org.jasig.portal.concurrency.IEntityLockService
*/
public interface IEntityLock {

/**
 * Attempts to change the <code>lockType</code> of this lock to
 * <code>newType</code>.  The <code>expirationTime</code> is renewed.
 *
 * @param newType int
 * @exception org.jasig.portal.concurrency.LockingException - if the conversion fails.
 */
public void convert(int newType) throws LockingException;
/**
 * Attempts to change the <code>lockType</code> of this lock to
 * <code>newType</code>.  The <code>expirationTime</code> is extended
 * <code>newDuration</code> seconds.
 *
 * @param newType int
 * @param newDuration int
 * @exception org.jasig.portal.concurrency.LockingException - if the conversion fails.
 */
public void convert(int newType, int newDuration) throws LockingException;
/**
 * @return java.lang.String
 */
public String getEntityKey();
/**
 * @see org.jasig.portal.EntityTypes for known types.
 * @return java.lang.Class
 */
public Class getEntityType();
/**
 * @return java.util.Date
 */
public Date getExpirationTime();
/**
 * Could be the portal user or the framework or ...?
 * @return java.lang.String
 */
public String getLockOwner();
/**
 * See IEntityLockingService for a description of lock types.
 * @return int
 */
public int getLockType();
/**
 * Answers if this lock is still good.
 */
public boolean isValid() throws LockingException;
/**
 * Invalidate the lock.
 */
public void release() throws LockingException;
/**
 * Extends the expiration time of this lock for a service-defined period.
 */
public void renew() throws LockingException;
/**
 * Extends the expiration time of this lock for <code>duration</code> seconds.
 */
public void renew(int duration) throws LockingException;
}
