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

package org.jasig.portal.spring.web.context.support;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.SessionScope;
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
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#resolveContextualObject(java.lang.String)
     */
    @Override
	public Object resolveContextualObject(String arg0) {
		//TODO implement me!
		return null;
	}
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory<?> objectFactory) {
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
        final HttpServletRequest portalRequest = this.portalRequestUtils.getCurrentPortalRequest();
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
