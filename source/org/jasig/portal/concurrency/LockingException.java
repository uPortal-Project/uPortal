/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.concurrency;

/**
 * A <code>LockingException</code> describes a problem that has arisen during
 * an attempt to create or alter an <code>IEntityLock</code>.  If the problem
 * occurs in the lock store, the <code>LockingException</code> should wrap an
 * <code>Exception</code> specific to the store, like a <code>java.sql.SQLException</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class LockingException extends org.jasig.portal.PortalException {
/**
 *
 */
public LockingException() {
	super();
}
/**
 * @param msg java.lang.String
 */
public LockingException(String msg) {
	super(msg);
}
/**
 * Add LOCKING_EXCEPTION to PortalExceptions.
 */
public int getExceptionCode() {
	return 0;
}
}
