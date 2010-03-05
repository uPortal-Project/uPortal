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

package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.channels.ChannelHttpServletRequestWrapper;

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
        if (req instanceof ChannelHttpServletRequestWrapper) {
            this.req = req;
        }
        else {
            this.req = new ChannelHttpServletRequestWrapper(req);
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
