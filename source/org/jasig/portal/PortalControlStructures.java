/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>This object is passed to special channels</p>
 * @author Peter Kharchenko, peterk@interactivebusiness.com
 * @version $Revision$
 */
public class PortalControlStructures {
    protected IUserPreferencesManager ulm;
    protected HttpServletRequest req;
    protected HttpServletResponse res;
    protected ChannelManager cm;

    public PortalControlStructures(HttpServletRequest req,HttpServletResponse res, ChannelManager cm, IUserPreferencesManager ulm) {
        this.req=req;
        this.res=res;
        this.cm=cm;
        this.ulm=ulm;
    }
    
    public PortalControlStructures() {
        this.req=null;
        this.res=null;
        this.cm=null;
        this.ulm=null;
    }
        

    public IUserPreferencesManager getUserPreferencesManager() { return ulm; }
    public HttpServletRequest getHttpServletRequest() { return req;}
    public HttpServletResponse getHttpServletResponse() { return res; }
    public ChannelManager getChannelManager() {return cm; }

    /**
     * Convience method for getting just the HttpSession
     * @return the session
     */
    public HttpSession getHttpSession() {
      HttpSession session = null;
      if (req != null)
         session = req.getSession(false);
      return session;
    }

    public void setUserPreferencesManager(IUserPreferencesManager lm) { ulm=lm; }
    public void setHttpServletRequest(HttpServletRequest r) { req=r; }
    public void setHttpServletResponse(HttpServletResponse r) { res=r; }
    public void setChannelManager(ChannelManager m) { cm=m; }
}
