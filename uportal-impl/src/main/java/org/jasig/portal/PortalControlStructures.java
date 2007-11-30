/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
public class PortalControlStructures {
    protected IUserPreferencesManager ulm;
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected HttpSession session;
    protected ChannelManager cm;

    public PortalControlStructures(HttpServletRequest req, HttpServletResponse res, ChannelManager cm, IUserPreferencesManager ulm) {
        this.req = req;
        this.res = res;
        this.session = null;
        this.cm = cm;
        this.ulm = ulm;
    }

    public PortalControlStructures() {
        this.req = null;
        this.res = null;
        this.session = null;
        this.cm = null;
        this.ulm = null;
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

    public void setUserPreferencesManager(IUserPreferencesManager lm) {
        ulm = lm;
    }

    public void setHttpServletRequest(HttpServletRequest r) {
        req = r;
    }

    public void setHttpServletResponse(HttpServletResponse r) {
        res = r;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.session = httpSession;
    }

    public void setChannelManager(ChannelManager m) {
        cm = m;
    }
}
