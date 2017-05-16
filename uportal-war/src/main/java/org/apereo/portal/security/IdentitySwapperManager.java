/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;

/**
 * Manages workflow around use of the identity swapper features.
 *
 */
public interface IdentitySwapperManager {
    /**
     * Check if the currentUser can impersonate the targetUsername, returns true if they can, false
     * if not.
     */
    boolean canImpersonateUser(IPerson currentUser, String targetUsername);

    /**
     * Check if the currentUser can impersonate the targetUsername, returns true if they can, false
     * if not.
     */
    boolean canImpersonateUser(String currentUserName, String targetUsername);

    /**
     * Setup the request so that a subsequent redirect to the login servlet will result in
     * impersonation
     *
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void impersonateUser(PortletRequest portletRequest, IPerson currentUser, String targetUsername);

    /**
     * Setup the request so that a subsequent redirect to the login servlet will result in
     * impersonation. This will login with the default profile.
     *
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void impersonateUser(
            PortletRequest portletRequest, String currentUserName, String targetUsername);

    /**
     * Setup the request so that a subsequent redirect to the login servlet will result in an
     * impersonation with a selected profile
     *
     * @param portletRequest The portlet request
     * @param currentUserName The current username of the administrator
     * @param targetUsername The target user name of the person being impersonated
     * @param profile The profile of which you want to login under
     */
    void impersonateUser(
            PortletRequest portletRequest,
            String currentUserName,
            String targetUsername,
            String profile);

    /**
     * During impersonation of targetUsername sets the original user to currentUserName for later
     * retrieval by {@link #getOriginalUsername(HttpSession)}. If the original authentication will
     * also be needed for later retrieval, use {@link #setOriginalUser(HttpSession, String, String,
     * Authentication)} instead.
     *
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void setOriginalUser(HttpSession session, String currentUserName, String targetUsername);

    /**
     * During impersonation of targetUsername sets the original user to currentUserName for later retrieval by
     * {@link #getOriginalUsername(HttpSession)} and the set the original authentication for later retrieval by
     * {@link #getOriginalAuthentication(HttpSession).
     *
     * @throws RuntimeAuthorizationException if the current user cannot impersonate the target user
     */
    void setOriginalUser(
            HttpSession session,
            String currentUserName,
            String targetUsername,
            Authentication originalAuth);

    /**
     * @return The original user if the current user is an impersonation, null if no impersonation
     *     is happening
     */
    String getOriginalUsername(HttpSession session);

    /** @return the authentication for the original user */
    Authentication getOriginalAuthentication(HttpSession session);

    /** @return The target of impersonation, null if there is no impersonation target */
    String getTargetUsername(HttpSession session);

    /**
     * @return The requested profile as part of an impersonation, null if there is no profile (will
     *     use default)
     */
    String getTargetProfile(HttpSession session);

    /**
     * @param request needed to provide a session for the user
     * @return a true/false the user is actually another user impersonating as this user.
     */
    boolean isImpersonating(HttpServletRequest request);
}
