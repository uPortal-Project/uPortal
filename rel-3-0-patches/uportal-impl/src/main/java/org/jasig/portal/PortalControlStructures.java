/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.url.AttributeScopingHttpServletRequestWrapper;

/**
 * This object is passed to special channels.
 * 
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class PortalControlStructures {
    protected final IUserPreferencesManager ulm;
    protected final HttpServletRequest req;
    protected final HttpServletResponse res;
    protected final HttpSession session;
    protected final ChannelManager cm;

    public PortalControlStructures(HttpServletRequest req, HttpServletResponse res, ChannelManager cm, IUserPreferencesManager ulm) {
        if (req instanceof AttributeScopingHttpServletRequestWrapper) {
            this.req = req;
        }
        else {
            this.req = new AttributeScopingHttpServletRequestWrapper(req);
        }

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
