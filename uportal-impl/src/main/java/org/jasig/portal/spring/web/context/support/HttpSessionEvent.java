/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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