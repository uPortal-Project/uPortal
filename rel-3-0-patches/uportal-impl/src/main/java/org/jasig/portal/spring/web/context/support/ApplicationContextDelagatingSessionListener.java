/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
