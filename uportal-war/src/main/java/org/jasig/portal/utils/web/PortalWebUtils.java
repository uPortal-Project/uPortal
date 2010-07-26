/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
