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

package org.jasig.portal.groups;

import org.jasig.portal.concurrency.IEntityLock;

    /**
 * Extends <code>EntityGroupImpl</code> to make it lockable for writing.
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class LockableEntityGroupImpl extends EntityGroupImpl implements ILockableEntityGroup
{
    protected IEntityLock lock;
/**
 * LockableEntityGroupImpl constructor.
 * @param groupKey java.lang.String
 * @param groupType java.lang.Class
 * @exception GroupsException
 */
public LockableEntityGroupImpl(String groupKey, Class groupType) throws GroupsException {
    super(groupKey, groupType);
}

/**
 * Delegates to the factory.
 */
public void delete() throws GroupsException
{
    getLockableGroupService().deleteGroup(this);
}

/**
 * @return org.jasig.portal.concurrency.IEntityLock
 */
public IEntityLock getLock() {
    return lock;
}

/**
 * @return org.jasig.portal.groups.ILockableGroupService
 */
protected ILockableGroupService getLockableGroupService() throws GroupsException
{
    return (ILockableGroupService) super.getLocalGroupService();
}

/**
 * Ask the service to update this group (in the store), update the 
 * back-pointers of the updated members, and force the retrieval of 
 * containing groups in case the memberships of THIS group have 
 * changed during the time the group has been locked.  
 */
private void primUpdate(boolean renewLock) throws GroupsException
{
    getLockableGroupService().updateGroup(this, renewLock);
    clearPendingUpdates();
    setGroupKeysInitialized(false);
}

/**
 * Ask the service to update this group (in the store), update the 
 * back-pointers of the updated members, and force the retrieval of 
 * containing groups in case the memberships of THIS group have 
 * changed during the time the group has been locked.  
 */
private void primUpdateMembers(boolean renewLock) throws GroupsException
{
    getLockableGroupService().updateGroupMembers(this, renewLock);
    clearPendingUpdates();
    setGroupKeysInitialized(false);
}

/**
 * @param newLock org.jasig.portal.concurrency.IEntityLock
 */
public void setLock(IEntityLock newLock)
{
    lock = newLock;
}

/**
 */
public String toString()
{
    return "LockableEntityGroupImpl (" + getKey() + ") "  + getName();
}

/**
 *
 */
public void update() throws GroupsException
{
    primUpdate(false);
}

/**
 *
 */
public void updateAndRenewLock() throws GroupsException
{
    primUpdate(true);
}

/**
 *
 */
public void updateMembers() throws GroupsException
{
    primUpdateMembers(false);
}

/**
 *
 */
public void updateMembersAndRenewLock() throws GroupsException
{
    primUpdateMembers(true);
}

}
