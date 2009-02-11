/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.web.context.support;

import java.io.Serializable;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.portlet.context.PortletRequestAttributes;
import org.springframework.web.util.WebUtils;

/**
 * Wraps {@link SessionScope} to provide functionality when no session is available by using
 * a singleton instance.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalSessionScope implements Scope {
    public static final String DESTRUCTION_CALLBACK_NAME_PREFIX = PortalSessionScope.class.getName() + ".DESTRUCTION_CALLBACK.";

    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private IPortalRequestUtils portalRequestUtils;
    
    public IPortalRequestUtils getPortalRequestUtils() {
        return this.portalRequestUtils;
    }
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory objectFactory) {
        final HttpSession session = this.getPortalSesion(true);
        
        final Object sessionMutex = WebUtils.getSessionMutex(session);
        synchronized (sessionMutex) {
            Object scopedObject = session.getAttribute(name);
            if (scopedObject == null) {
                scopedObject = objectFactory.getObject();
                session.setAttribute(name, scopedObject);
            }
            
            return scopedObject;
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    public String getConversationId() {
        final HttpSession session = this.getPortalSesion(false);
        
        if (session == null) {
            return null;
        }
        
        return session.getId();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String, java.lang.Runnable)
     */
    public void registerDestructionCallback(String name, Runnable callback) {
        final HttpSession session = this.getPortalSesion(true);
        final DestructionCallbackBindingListener callbackListener = new DestructionCallbackBindingListener(callback);
        session.setAttribute(DESTRUCTION_CALLBACK_NAME_PREFIX + name, callbackListener);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    public Object remove(String name) {
        final HttpSession session = this.getPortalSesion(false);
        if (session == null) {
            return null;
        }
        
        final Object sessionMutex = WebUtils.getSessionMutex(session);
        synchronized (sessionMutex) {
            final Object attribute = session.getAttribute(name);
            if (attribute != null) {
                session.removeAttribute(name);
            }
            
            return attribute;
        }
    }

    protected HttpSession getPortalSesion(boolean create) {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        
        HttpServletRequest portalRequest;
        if (requestAttributes instanceof ServletRequestAttributes) {
            final HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            try {
                portalRequest = this.portalRequestUtils.getOriginalPortalRequest(request);
            }
            catch (IllegalArgumentException iae) {
                portalRequest = request;
            }
        }
        else if (requestAttributes instanceof PortletRequestAttributes) {
            final PortletRequest request = ((PortletRequestAttributes)requestAttributes).getRequest();
            portalRequest = this.portalRequestUtils.getOriginalPortalRequest(request);
        }
        else {
            throw new IllegalStateException("portalSession scope only works with ServletRequestAttributes or PortletRequestAttributes in the RequestContextHolder");
        }

        return portalRequest.getSession(create);
    }


    /**
     * Adapter that implements the Servlet 2.3 HttpSessionBindingListener
     * interface, wrapping a session destruction callback.
     */
    private static class DestructionCallbackBindingListener implements HttpSessionBindingListener, Serializable {
        private static final long serialVersionUID = 1L;

        private transient final Runnable destructionCallback;

        public DestructionCallbackBindingListener(Runnable destructionCallback) {
            this.destructionCallback = destructionCallback;
        }

        public void valueBound(HttpSessionBindingEvent event) {
        }

        public void valueUnbound(HttpSessionBindingEvent event) {
            if (this.destructionCallback != null) {
                this.destructionCallback.run();
            }
        }
    }
}
