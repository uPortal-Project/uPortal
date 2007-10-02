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
    //Track the current request and response in a thread local as well
    //as the member variables. This ensures that the case of multiple
    //request from a single user rendering at the same time is possible.
    //The class member variables are still used as a fallback if needed.
    protected static final ThreadLocal reqLocal = new ThreadLocal();
    protected static final ThreadLocal resLocal = new ThreadLocal();
         
    protected IUserPreferencesManager ulm;
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected ChannelManager cm;

    public PortalControlStructures(HttpServletRequest req,HttpServletResponse res, ChannelManager cm, IUserPreferencesManager ulm) {
        this.setHttpServletRequest(req);
        this.setHttpServletResponse(res);
        this.setChannelManager(cm);
        this.setUserPreferencesManager(ulm);
    }
    
    public PortalControlStructures() {
        this(null, null, null, null);
    }
        

    public IUserPreferencesManager getUserPreferencesManager() { return ulm; }
    public HttpServletRequest getHttpServletRequest() {
        final HttpServletRequest localReq = (HttpServletRequest)reqLocal.get();
        if (localReq != null) {
            return localReq;
        }
        return req;
    }

    public HttpServletResponse getHttpServletResponse() {
        final HttpServletResponse localRes = (HttpServletResponse)resLocal.get();
        if (localRes != null) {
            return localRes;
        }
        return res;
    }
    public ChannelManager getChannelManager() {return cm; }

    /**
     * Convience method for getting just the HttpSession
     * @return the session
     */
    public HttpSession getHttpSession() {
      HttpSession session = null;
      if (this.getHttpServletRequest() != null)
         session = this.getHttpServletRequest().getSession(false);
      return session;
    }

    public void setUserPreferencesManager(IUserPreferencesManager lm) { ulm=lm; }
    public void setHttpServletRequest(HttpServletRequest r) {
        req = r;
        reqLocal.set(req);
    }

    public void setHttpServletResponse(HttpServletResponse r) {
        res = r;
        resLocal.set(res);
    }
    public void setChannelManager(ChannelManager m) { cm=m; }
}
