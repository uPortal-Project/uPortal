/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.concurrency;

/**
 * A <code>CachingException</code> describes a problem that has arisen during
 * an attempt to add, update, remove or reference a cache entry.  If the problem
 * arises in the store, the <code>CachingException</code> should wrap an
 * <code>Exception</code> specific to the store, like a <code>java.sql.SQLException</code>.
 *
 * @author Dan Ellentuck
 * @version $Revision$ $Date$
 */
public class CachingException extends org.jasig.portal.PortalException {
    
    
    /**
     * Instantiate a CachingException with the given cause.
     * @param cause A throwable that caused the caching problem.
     */
    public CachingException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a CachingException with the given message.
     * @param msg message describing nature of caching problem.
     */
    public CachingException(String msg) {
        super(msg);
    }
    
    /**
     * Instantiate a CachingException with the given message and underlying cause.
     * @param msg message describing nature of caching problem.
     * @param cause underlying cause.
     */
    public CachingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
