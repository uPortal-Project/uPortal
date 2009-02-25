/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.concurrency;

/**
 * Factory interface for creating an <code>IEntityLockService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IEntityLockServiceFactory {
/**
 * Return an IEntityLockService instance.
 * @return org.jasig.portal.concurrency.IEntityLockService
 * @exception org.jasig.portal.concurrency.LockingException
 */
public IEntityLockService newLockService() throws LockingException;
}
