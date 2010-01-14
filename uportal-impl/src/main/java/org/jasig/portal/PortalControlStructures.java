/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This object is passed to special channels.
 * 
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class PortalControlStructures {
    protected final IUserPreferencesManager ulm;
    protected final HttpServletRequest req;
    protected final HttpServletResponse res;
    protected final HttpSession session;
    protected final ChannelManager cm;

    public PortalControlStructures(HttpServletRequest req, HttpServletResponse res, ChannelManager cm, IUserPreferencesManager ulm) {
        this.req = req;
        this.res = res;
        this.session = null;
        this.cm = cm;
        this.ulm = ulm;
    }
    
    public PortalControlStructures(HttpServletRequest req, HttpServletResponse res) {
        this(req, res, null, null);
    }

    public PortalControlStructures(HttpSession session, ChannelManager cm, IUserPreferencesManager ulm) {
        this.req = null;
        this.res = null;
        this.session = session;
        this.cm = cm;
        this.ulm = ulm;
    }

    public IUserPreferencesManager getUserPreferencesManager() {
        return ulm;
    }

    public HttpServletRequest getHttpServletRequest() {
        return req;
    }

    public HttpServletResponse getHttpServletResponse() {
        return res;
    }

    public ChannelManager getChannelManager() {
        return cm;
    }

    /**
     * Convience method for getting just the HttpSession
     * @return the session
     */
    public HttpSession getHttpSession() {
        if (this.session != null) {
            return this.session;
        }

        if (this.req != null) {
            return this.req.getSession(false);
        }

        return null;
    }
}
