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
 * @version $Revision$
 */
public class CachingException extends org.jasig.portal.PortalException {
/**
 *
 */
public CachingException() {
    super();
}
/**
 * @param msg java.lang.String
 */
public CachingException(String msg) {
    super(msg);
}
/**
 * Add CACHING_EXCEPTION to PortalExceptions.
 */
public int getExceptionCode() {
    return 0;
}
}
