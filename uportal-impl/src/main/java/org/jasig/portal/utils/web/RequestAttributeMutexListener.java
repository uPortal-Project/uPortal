/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils.web;

import java.io.Serializable;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * Servlet 2.3 HttpSessionListener that automatically exposes the
 * request attribute mutex when an ServletRequest gets created.
 * To be registered as a listener in <code>web.xml</code>.
 *
 * <p>The request attribute mutex is guaranteed to be the same object during
 * the entire lifetime of the request, available under the key defined
 * by the <code>REQUEST_MUTEX_ATTRIBUTE</code> constant. It serves as a
 * safe reference to synchronize on for locking on the current request attributes.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestAttributeMutexListener implements ServletRequestListener {

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
     */
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE, new Mutex());
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
     */
    public void requestDestroyed(ServletRequestEvent sre) {
        sre.getServletRequest().removeAttribute(PortalWebUtils.REQUEST_MUTEX_ATTRIBUTE);
    }

    /**
     * The mutex to be registered.
     * Doesn't need to be anything but a plain Object to synchronize on.
     */
    private static class Mutex implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
