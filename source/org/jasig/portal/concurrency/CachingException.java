/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
     *  Instantiate a bare CachingException.
     * Deprecated because it would be so much more helpful if you were to
     * instead use a contructor with a message.
     * @deprecated use a more helpful constructor
     */
    public CachingException() {
        super();
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

    /**
     * This method always returns zero.  Presumably it is here because at one time
     * PortalExceptions were to be identified by integer codes.  In any case, this
     * method does not correspond to any interface and is deprecated for future
     * removal.
     * @return 0
     * @deprecated
     */
    public int getExceptionCode() {
        return 0;
    }
}
