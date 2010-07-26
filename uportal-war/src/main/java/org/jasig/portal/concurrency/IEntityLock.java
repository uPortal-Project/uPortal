/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
