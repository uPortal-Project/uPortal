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
 * Defines an <code>IEntityGroup</code> that can be locked for update.   
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface ILockableEntityGroup extends IEntityGroup {
/**
 * @return org.jasig.portal.concurrency.IEntityLock
 */
public IEntityLock getLock();
/**
 * @param lock org.jasig.portal.concurrency.IEntityLock
 */
public void setLock(IEntityLock lock);

/**
 * Updates the group and its members and renews the lock.
 */
public void updateAndRenewLock() throws GroupsException;

/**
 * Updates the members and renews the lock.
 */
public void updateMembersAndRenewLock() throws GroupsException;
}
