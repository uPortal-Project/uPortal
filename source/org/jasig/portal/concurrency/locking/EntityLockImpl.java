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
import org.jasig.portal.concurrency.IEntityLockService;
import org.jasig.portal.concurrency.LockingException;
/**
  * An implementation of IEntityLock.  A lock is granted to a
  * <code>lockOwner</code> for an <code>entityType</code> and
  * <code>entityKey</code>.  It guarantees some degree of exclusive
  * access to the entity, depending on <code>lockType</code> and
  * <code>expirationTime</code>.
  *
  * @author Dan Ellentuck
  * @version $Revision$
  * @see org.jasig.portal.concurrency.IEntityLock
*/
public class EntityLockImpl implements IEntityLock {
    private Class entityType;
    private String entityKey;
    private String lockOwner;
    private Date expirationTime;
    private int lockType;
    private IEntityLockService service;
/**
 *
 */
protected EntityLockImpl (
    Class newEntityType,
    String newEntityKey,
    int newLockType,
    Date newExpirationTime,
    String newLockOwner  )
{
    super();
    setEntityType(newEntityType);
    setEntityKey(newEntityKey);
    setLockType(newLockType);
    setExpirationTime(newExpirationTime);
    setLockOwner(newLockOwner);
}
/**
 *
 */
protected EntityLockImpl (
    Class newEntityType,
    String newEntityKey,
    int newLockType,
    Date newExpirationTime,
    String newLockOwner,
    IEntityLockService newService)
{
    super();
    setEntityType(newEntityType);
    setEntityKey(newEntityKey);
    setLockType(newLockType);
    setExpirationTime(newExpirationTime);
    setLockOwner(newLockOwner);
    setService(newService);
}
/**
 * Delegate to the service.
 * @param newType int
 * @exception org.jasig.portal.concurrency.LockingException - if the conversion fails.
 */
public void convert(int newType) throws LockingException
{
    getService().convert(this, newType);
}
/**
 * Delegate to the service.
 * @param newType int
 * @param duration int
 * @exception org.jasig.portal.concurrency.LockingException - if the conversion fails.
 */
public void convert(int newType, int duration) throws LockingException
{
    getService().convert(this, newType);
}
/**
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj)
{
    if ( obj == null )
        return false;
    if ( obj == this )
        return true;
    if ( ! ( obj instanceof EntityLockImpl))
        return false;

    IEntityLock candidate = (IEntityLock) obj;
    return this.getEntityType().equals(candidate.getEntityType()) &&
           this.getEntityKey().equals(candidate.getEntityKey()) &&
           this.getLockOwner().equals(candidate.getLockOwner()) &&
           this.getLockType() == candidate.getLockType() &&
           this.getExpirationTime().equals(candidate.getExpirationTime());
}
/**
 */
protected void expire()
{
    setExpirationTime(new Date(0));
}
/**
 * @throws Throwable
 */
protected void finalize() throws Throwable
{
    super.finalize();
}
/**
 * @return java.lang.String
 */
public String getEntityKey() {
    return entityKey;
}
/**
 * @see org.jasig.portal.groups.EntityTypes for known types.
 * @return java.lang.Class
 */
public Class getEntityType() {
    return entityType;
}
/**
 * @return java.util.Date
 */
public Date getExpirationTime() {
    return expirationTime;
}
/**
 * Could be the portal user or the framework or ...?
 * @return java.lang.String
 */
public String getLockOwner() {
    return lockOwner;
}
/**
 * See IEntityLockingService for a description of lock types.
 * @return int
 */
public int getLockType() {
    return lockType;
}
/**
 * @return org.jasig.portal.concurrency.locking.IEntityLockService
 */
protected IEntityLockService getService() {
    return service;
}
/**
 * This method is supported primarily for hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode()
{
    return getEntityType().hashCode() + getEntityKey().hashCode() + getLockOwner().hashCode();
}
/**
 * Answer if the lock is unexpired.
 * @return boolean
 */
public boolean isLocked() {
    return getExpirationTime().after(new Date());
}
/**
 * Delegate to the service.
 * @return boolean
 */
public boolean isValid() throws LockingException
{
    return getService().isValid(this);
}
/**
 * Delegate to the service.
 */
public void release() throws LockingException
{
    getService().release(this);
}
/**
 * Delegate to the service.
 */
public void renew() throws LockingException
{
    getService().renew(this);
}
/**
 * Delegate to the service.
 */
public void renew(int duration) throws LockingException
{
    getService().renew(this, duration);
}
/**
 * @param newEntityKey
 */
private void setEntityKey(String newEntityKey) {
    entityKey = newEntityKey;
}
/**
 * @param newEntityType
 */
private void setEntityType(Class newEntityType) {
    entityType = newEntityType;
}
/**
 * @param newExpirationTime
 */
void setExpirationTime(Date newExpirationTime) {
    expirationTime = newExpirationTime;
}
/**
 * @param newLockOwner
 */
private void setLockOwner(String newLockOwner) {
    lockOwner = newLockOwner;
}
/**
 * @param newLockType
 */
void setLockType(int newLockType) {
    lockType = newLockType;
}
/**
 * @param newService org.jasig.portal.concurrency.locking.IEntityLockService
 */
private void setService(IEntityLockService newService) {
    service = newService;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString()
{
     return "EntityLockImpl for " + getEntityType().getName() + "(" + getEntityKey() + ")" +
       " type " + getLockType() + " owner " + getLockOwner() + " " + getExpirationTime();
}
}
