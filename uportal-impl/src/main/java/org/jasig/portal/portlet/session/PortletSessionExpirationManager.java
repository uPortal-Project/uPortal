/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.optional.PortletInvocationEvent;
import org.apache.pluto.spi.optional.PortletInvocationListener;
import org.jasig.portal.spring.web.context.support.HttpSessionDestroyedEvent;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.util.WebUtils;

/**
 * After each request processed by a portlet the portlets session (if one exists) is stored in a Map in the Portal's
 * session. When a portal session is invalidated the {@link PortletSession#invalidate()} method is called on all portlet
 * sessions in the Map.
 * 
 * TODO this may not play well with distributed sessions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletSessionExpirationManager implements PortletInvocationListener, ApplicationListener {
    public static final String PORTLET_SESSIONS_MAP = PortletSessionExpirationManager.class.getName() + ".PORTLET_SESSIONS";
    
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortalRequestUtils portalRequestUtils;
    
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onEnd(org.apache.pluto.spi.optional.PortletInvocationEvent)
     */
    @SuppressWarnings("unchecked")
    public void onEnd(PortletInvocationEvent event) {
        final PortletRequest portletRequest = event.getPortletRequest();
        final PortletSession portletSession = portletRequest.getPortletSession(false);
        if (portletSession == null) {
            return;
        }

        final HttpServletRequest portalRequest = this.portalRequestUtils.getOriginalPortalRequest(portletRequest);
        final HttpSession portalSession = portalRequest.getSession();
        
        if (portalSession != null) {
            NonSerializableMapHolder<String, PortletSession> portletSessions;
            synchronized (WebUtils.getSessionMutex(portalSession)) {
                portletSessions = (NonSerializableMapHolder<String, PortletSession>)portalSession.getAttribute(PORTLET_SESSIONS_MAP);
                if (portletSessions == null || !portletSessions.isValid()) {
                    portletSessions = new NonSerializableMapHolder(new ConcurrentHashMap<String, PortletSession>());
                    portalSession.setAttribute(PORTLET_SESSIONS_MAP, portletSessions);
                }
            }
            
            final PortletWindow portletWindow = event.getPortletWindow();
            final String contextPath = portletWindow.getContextPath();
            portletSessions.put(contextPath, portletSession);
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof HttpSessionDestroyedEvent) {
            final HttpSession session = ((HttpSessionDestroyedEvent)event).getSession();
            final Map<String, PortletSession> portletSessions = (Map<String, PortletSession>)session.getAttribute(PORTLET_SESSIONS_MAP);
            if (portletSessions == null) {
                return;
            }
            
            for (final Map.Entry<String, PortletSession> portletSessionEntry: portletSessions.entrySet()) {
                final String contextPath = portletSessionEntry.getKey();
                final PortletSession portletSession = portletSessionEntry.getValue();
                try {
                    portletSession.invalidate();
                }
                catch (IllegalStateException e) {
                    this.logger.info("PortletSession with id '" + portletSession.getId() + "' for context '" + contextPath + "' has already been invalidated.");
                }
                catch (Exception e) {
                    this.logger.warn("Failed to invalidate PortletSession with id '" + portletSession.getId() + "' for context '" + contextPath + "'", e);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onBegin(org.apache.pluto.spi.optional.PortletInvocationEvent)
     */
    public void onBegin(PortletInvocationEvent event) {
        // Ignore
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.PortletInvocationListener#onError(org.apache.pluto.spi.optional.PortletInvocationEvent, java.lang.Throwable)
     */
    public void onError(PortletInvocationEvent event, Throwable t) {
        // Ignore
    }
    
    /**
     * Map implementation that holds the Map reference passed into the constructor in a transient field. This allows a
     * Map of non-serializable objects to be stored in the session but skipped during session persistence.
     */
    private static final class NonSerializableMapHolder<K, V> implements Map<K, V>, Serializable {
        private static final long serialVersionUID = 1L;

        private final transient Map<K, V> delegate;

        public NonSerializableMapHolder(Map<K, V> delegate) {
            this.delegate = delegate;
        }
        
        public boolean isValid() {
            return this.delegate != null;
        }

        public void clear() {
            delegate.clear();
        }

        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        public Set<java.util.Map.Entry<K, V>> entrySet() {
            return delegate.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        public V get(Object key) {
            return delegate.get(key);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        public Set<K> keySet() {
            return delegate.keySet();
        }

        public V put(K key, V value) {
            return delegate.put(key, value);
        }

        public void putAll(Map<? extends K, ? extends V> t) {
            delegate.putAll(t);
        }

        public V remove(Object key) {
            return delegate.remove(key);
        }

        public int size() {
            return delegate.size();
        }

        public Collection<V> values() {
            return delegate.values();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
