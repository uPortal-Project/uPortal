/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.web;

import javax.servlet.ServletRequest;

import org.springframework.util.Assert;


/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public final class PortalWebUtils {
    private PortalWebUtils() { }

    /** Key for the mutex request session attribute */
    public static final String REQUEST_MUTEX_ATTRIBUTE = PortalWebUtils.class.getName() + ".MUTEX";
    
    /**
     * Return the best available mutex for the given request attributes:
     * that is, an object to synchronize on for the given request attributes.
     * <p>Returns the request attributes mutex attribute if available; usually,
     * this means that the RequestAttributeMutexListener needs to be defined
     * in <code>web.xml</code>. Falls back to the ServletRequest itself
     * if no mutex attribute found.
     * <p>The request attributes mutex is guaranteed to be the same object during
     * the entire lifetime of the request, available under the key defined
     * by the <code>REQUEST_MUTEX_ATTRIBUTE</code> constant. It serves as a
     * safe reference to synchronize on for locking on the current request attributes.
     * 
     * @param servletRequest the ServletRequest to find a mutex for
     * @return the mutex object (never <code>null</code>)
     * @see #REQUEST_MUTEX_ATTRIBUTE
     * @see RequestAttributeMutexListener
     */
    public static Object getRequestAttributeMutex(ServletRequest servletRequest) {
        Assert.notNull(servletRequest, "ServletRequest must not be null");
        Object mutex = servletRequest.getAttribute(REQUEST_MUTEX_ATTRIBUTE);
        if (mutex == null) {
            mutex = servletRequest;
        }
        return mutex;
    }
}
