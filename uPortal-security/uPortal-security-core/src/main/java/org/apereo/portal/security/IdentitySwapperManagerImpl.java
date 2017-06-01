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
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apereo.portal.EntityIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("identitySwapperManager")
public class IdentitySwapperManagerImpl implements IdentitySwapperManager {

    private static final String SWAP_TARGET_UID =
            IdentitySwapperManagerImpl.class.getName() + ".SWAP_TARGET_UID";
    private static final String SWAP_TARGET_PROFILE =
            IdentitySwapperManagerImpl.class.getName() + ".SWAP_TARGET_PROFILE";
    private static final String SWAP_ORIGINAL_UID =
            IdentitySwapperManagerImpl.class.getName() + ".SWAP_ORIGINAL_UID";
    private static final String SWAP_ORIGINAL_AUTH =
            IdentitySwapperManagerImpl.class.getName() + ".SWAP_ORIGINAL_AUTH";

    private IAuthorizationService authorizationService;

    @Autowired
    public void setAuthorizationService(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean canImpersonateUser(IPerson currentUser, String targetUsername) {
        final EntityIdentifier ei = currentUser.getEntityIdentifier();
        final IAuthorizationPrincipal ap =
                authorizationService.newPrincipal(ei.getKey(), ei.getType());

        return canImpersonateUser(ap, targetUsername);
    }

    @Override
    public boolean canImpersonateUser(String currentUserName, String targetUsername) {
        final IAuthorizationPrincipal ap =
                authorizationService.newPrincipal(currentUserName, IPerson.class);
        return canImpersonateUser(ap, targetUsername);
    }

    protected boolean canImpersonateUser(final IAuthorizationPrincipal ap, String targetUsername) {
        return ap.hasPermission(
                IPermission.PORTAL_USERS, IPermission.IMPERSONATE_USER_ACTIVITY, targetUsername);
    }

    @Override
    public void impersonateUser(
            PortletRequest portletRequest, IPerson currentUser, String targetUsername) {
        this.impersonateUser(portletRequest, currentUser.getName(), targetUsername);
    }

    @Override
    public void impersonateUser(
            PortletRequest portletRequest, String currentUserName, String targetUsername) {
        impersonateUser(portletRequest, currentUserName, targetUsername, "default");
    }

    @Override
    public void impersonateUser(
            PortletRequest portletRequest,
            String currentUserName,
            String targetUsername,
            String profile) {
        if (!canImpersonateUser(currentUserName, targetUsername)) {
            throw new RuntimeAuthorizationException(
                    currentUserName, IPermission.IMPERSONATE_USER_ACTIVITY, targetUsername);
        }

        final PortletSession portletSession = portletRequest.getPortletSession();
        portletSession.setAttribute(
                SWAP_TARGET_UID, targetUsername, PortletSession.APPLICATION_SCOPE);
        portletSession.setAttribute(SWAP_TARGET_PROFILE, profile, PortletSession.APPLICATION_SCOPE);
    }

    @Override
    public void setOriginalUser(
            HttpSession session, String currentUserName, String targetUsername) {
        this.setOriginalUser(session, currentUserName, targetUsername, null);
    }

    @Override
    public void setOriginalUser(
            HttpSession session,
            String currentUserName,
            String targetUsername,
            Authentication originalAuth) {
        if (!canImpersonateUser(currentUserName, targetUsername)) {
            throw new RuntimeAuthorizationException(
                    currentUserName, IPermission.IMPERSONATE_USER_ACTIVITY, targetUsername);
        }
        session.setAttribute(SWAP_ORIGINAL_UID, currentUserName);
        session.setAttribute(SWAP_ORIGINAL_AUTH, originalAuth);
    }

    @Override
    public String getOriginalUsername(HttpSession session) {
        return (String) session.getAttribute(SWAP_ORIGINAL_UID);
    }

    @Override
    public Authentication getOriginalAuthentication(HttpSession session) {
        return (Authentication) session.getAttribute(SWAP_ORIGINAL_AUTH);
    }

    @Override
    public String getTargetUsername(HttpSession session) {
        return (String) session.getAttribute(SWAP_TARGET_UID);
    }

    @Override
    public String getTargetProfile(HttpSession session) {
        return (String) session.getAttribute(SWAP_TARGET_PROFILE);
    }

    @Override
    public boolean isImpersonating(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        return this.getOriginalUsername(session) != null;
    }
}
