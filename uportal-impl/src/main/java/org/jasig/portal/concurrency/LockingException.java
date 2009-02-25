/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
     * Instantiate a LockingException with the given cause.
     * @param cause Throwable that caused the locking problem
     */
    public LockingException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a LockingException with the given message.
     * @param msg message describing nature of locking problem
     */
    public LockingException(String msg) {
        super(msg);
    }

    /**
     * Instantiate a LockingException with the given message
     * and underlying cause.
     * @param msg message describing nature of locking problem
     * @param cause underlying cause
     */
    public LockingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
