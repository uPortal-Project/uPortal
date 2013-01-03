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
package org.jasig.portal.security;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Manages workflow around use of the identity swapper features.
 * 
 * @author Eric Dalquist
 */
public interface IdentitySwapperManager {
    /**
     * Check if the currentUser can impersonate the targetUsername, returns true if they can, false if not.
     */
    boolean canImpersonateUser(IPerson currentUser, String targetUsername);
    
    /**
     * Check if the currentUser can impersonate the targetUsername, returns true if they can, false if not.
     */
    boolean canImpersonateUser(String currentUserName, String targetUsername);
    
    /**
     * Setup the request so that a subsequent redirect to the login servlet will result in impersonation
     * 
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void impersonateUser(PortletRequest portletRequest, IPerson currentUser, String targetUsername);
    
    /**
     * Setup the request so that a subsequent redirect to the login servlet will result in impersonation
     * 
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void impersonateUser(PortletRequest portletRequest, String currentUserName, String targetUsername);
    
    /**
     * During impersonation of targetUsername sets the original user to currentUserName for later
     * retrieval by {@link #getOriginalUsername(HttpSession)}
     * 
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void setOriginalUser(HttpSession session, String currentUserName, String targetUsername);
    
    /**
     * @return The original user if the current user is an impersonation, null if no impersonation is happening
     */
    String getOriginalUsername(HttpSession session);
    
    /**
     * @return The target of impersonation, null if there is no impersonation target
     */
    String getTargetUsername(HttpSession session);

    /**
     * 
     * @param request needed to provide a session for the user
     * @return a true/false the user is actually another user impersonating as this user.
     */
    boolean isImpersonating(HttpServletRequest request);
}
