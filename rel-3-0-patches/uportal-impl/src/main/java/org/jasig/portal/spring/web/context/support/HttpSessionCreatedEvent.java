/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.web.context.support;

import javax.servlet.http.HttpSession;


/**
 * This is a spring application context compatible version of {@link javax.servlet.http.HttpSessionEvent} that is
 * sent to the application context when {@link javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)}
 * is called.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class HttpSessionCreatedEvent extends HttpSessionEvent {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpSessionEvent#HttpSessionEvent(HttpSession)
     */
    public HttpSessionCreatedEvent(HttpSession source) {
        super(source);
    }
}
