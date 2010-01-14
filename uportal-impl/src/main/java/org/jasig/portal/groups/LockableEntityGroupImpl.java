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
