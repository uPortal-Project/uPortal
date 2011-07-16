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

package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Sets a custom session timeout for unauthenticated users.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class GuestSessionExpirationInterceptor extends HandlerInterceptorAdapter {
    private IPersonManager personManager;
    private int unauthenticatedUserSessionTimeout = 0;
    
    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    /**
     * The {@link HttpSession#setMaxInactiveInterval(int)} value to set for guest users. Defaults to 0.
     * If <= 0 no override is done.
     */
    public void setUnauthenticatedUserSessionTimeout(int unauthenticatedUserSessionTimeout) {
        this.unauthenticatedUserSessionTimeout = unauthenticatedUserSessionTimeout;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (unauthenticatedUserSessionTimeout <= 0) {
            return true;
        }
        
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        
        // Update the session timeout for an unauthenticated user.
        final IPerson person = personManager.getPerson(request);
        if (person != null && !person.getSecurityContext().isAuthenticated()) {
            session.setMaxInactiveInterval(unauthenticatedUserSessionTimeout);
        }
        
        return true;
    }
}
