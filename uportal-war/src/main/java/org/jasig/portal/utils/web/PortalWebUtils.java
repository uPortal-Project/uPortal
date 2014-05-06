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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.portlet.PortletRequest;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.portlet.context.PortletRequestAttributes;
import org.springframework.web.util.WebUtils;


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
    
    
    /**
     * Get a {@link ConcurrentMap} for the specified name from the {@link ServletRequest} attributes. If it doesn't
     * exist create it and store it in the attributes. This is done in a thread-safe matter that ensures only one
     * Map per name & request will be created
     * 
     * @See {@link #getRequestAttributeMutex(ServletRequest)}
     */
    public static <K, V> ConcurrentMap<K, V> getMapRequestAttribute(ServletRequest servletRequest, String name) {
        return getMapRequestAttribute(servletRequest, name, true);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> ConcurrentMap<K, V> getMapRequestAttribute(ServletRequest servletRequest, String name, boolean create) {
        final Object mutex = getRequestAttributeMutex(servletRequest);
        synchronized (mutex) {
            ConcurrentMap<K, V> map = (ConcurrentMap<K, V>)servletRequest.getAttribute(name);
            if (map == null) {
                if (!create) {
                    return null;
                }
                
                map = new ConcurrentHashMap<K, V>();
                servletRequest.setAttribute(name, map);
            }
            return map;
        }
    }
    
    /**
     * Get a {@link ConcurrentMap} for the specified name from the {@link HttpSession} attributes. If it doesn't
     * exist create it and store it in the attributes. This is done in a thread-safe matter that ensures only one
     * Map per name & session will be created
     * 
     * @See {@link WebUtils#getSessionMutex(HttpSession)}
     */
    public static <K, V> ConcurrentMap<K, V> getMapSessionAttribute(HttpSession session, String name) {
        return getMapSessionAttribute(session, name, true);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> ConcurrentMap<K, V> getMapSessionAttribute(HttpSession session, String name, boolean create) {
        final Object mutex = WebUtils.getSessionMutex(session);
        synchronized (mutex) {
            ConcurrentMap<K, V> map = (ConcurrentMap<K, V>)session.getAttribute(name);
            if (map == null) {
                if (!create) {
                    return null;
                }
                
                map = new ConcurrentHashMap<K, V>();
                session.setAttribute(name, map);
            }
            return map;
        }
    }

    /**
     * Get the request context path from the current request.
     * Copes with both HttpServletRequest and PortletRequest and so usable when handling
     * Spring-processed Servlet or Portlet requests.
     * Requires that Spring have bound the request, as in the case of dispatcher servlet or portlet or when the binding
     * filter or listener is active.  This should be the case for all requests in the uPortal framework and framework
     * portlets.
     * @return request.getContextPath() for the relevant servlet or portlet request
     * @throws IllegalStateException if the request is not Spring-bound or is neither Servlet nor Portlet flavored
     */
    public static String currentRequestContextPath() {

        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (null == requestAttributes) {
            throw new IllegalStateException("Request attributes are not bound.  " +
                    "Not operating in context of a Spring-processed Request?");
        }

        if (requestAttributes instanceof ServletRequestAttributes) {

            final ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            final HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getContextPath();

        } else if (requestAttributes instanceof PortletRequestAttributes) {

            final PortletRequestAttributes portletRequestAttributes = (PortletRequestAttributes) requestAttributes;
            final PortletRequest request = portletRequestAttributes.getRequest();
            return request.getContextPath();

        } else {
            throw new IllegalStateException("Request attributes are an unrecognized implementation.");
        }

    }
}
