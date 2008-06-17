/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.spring.web.context.support;

import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationEvent;

/**
 * Event about a HttpSession.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class HttpSessionEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    /**
     * @param source The HttpSession the event is about.
     */
    public HttpSessionEvent(HttpSession source) {
        super(source);
    }

    public final HttpSession getSession() {
        return (HttpSession) super.getSource();
    }

}