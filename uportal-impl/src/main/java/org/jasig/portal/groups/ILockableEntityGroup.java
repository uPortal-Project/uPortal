/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
