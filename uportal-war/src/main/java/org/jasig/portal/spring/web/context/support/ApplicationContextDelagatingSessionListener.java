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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * HttpSessionListener that publishes {@link HttpSessionCreatedEvent} and {@link HttpSessionDestroyedEvent} events
 * to the {@link WebApplicationContext} retrieved using {@link WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ApplicationContextDelagatingSessionListener implements HttpSessionListener {
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent se) {
        final HttpSession session = se.getSession();
        final HttpSessionCreatedEvent httpSessionCreatedEvent = new HttpSessionCreatedEvent(session);
        
        final WebApplicationContext webApplicationContext = this.getWebApplicationContext(session);
        webApplicationContext.publishEvent(httpSessionCreatedEvent);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent se) {
        final HttpSession session = se.getSession();
        final HttpSessionDestroyedEvent httpSessionDestroyedEvent = new HttpSessionDestroyedEvent(session);
        
        final WebApplicationContext webApplicationContext = this.getWebApplicationContext(session);
        webApplicationContext.publishEvent(httpSessionDestroyedEvent);
    }

    /**
     * Retrieves the WebApplicationContextUtils for the HttpSession.
     */
    protected WebApplicationContext getWebApplicationContext(final HttpSession session) {
        final ServletContext servletContext = session.getServletContext();
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }
}
