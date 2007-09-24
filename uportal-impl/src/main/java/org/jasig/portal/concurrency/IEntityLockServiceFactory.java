/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
