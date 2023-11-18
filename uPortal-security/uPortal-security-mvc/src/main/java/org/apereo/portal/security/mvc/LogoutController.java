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
package org.apereo.portal.security.mvc;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.events.IPortalAuthEventFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.url.UrlAuthCustomizerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple servlet to handle user logout. When a user logs out, their session gets invalidated and
 * they are returned to the guest page.
 */
@Controller
@RequestMapping("/Logout")
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${logout.redirect:}")
    private String logoutRedirect;

    private IPortalAuthEventFactory portalEventFactory;
    private IPersonManager personManager;
    private IdentitySwapperManager identitySwapperManager;
    private UrlAuthCustomizerRegistry urlCustomizer;

    @Autowired
    public void setIdentitySwapperManager(IdentitySwapperManager identitySwapperManager) {
        this.identitySwapperManager = identitySwapperManager;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortalEventFactory(IPortalAuthEventFactory portalEventFactory) {
        this.portalEventFactory = portalEventFactory;
    }

    @Autowired
    public void setUrlCustomizer(UrlAuthCustomizerRegistry urlCustomizer) {
        this.urlCustomizer = urlCustomizer;
    }

    /**
     * Process the incoming request and response.
     *
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     */
    @RequestMapping
    public void doLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String redirect = this.selectRedirectionUrl(request);
        final HttpSession session = request.getSession(false);

        if (session != null) {
            // Record that an authenticated user is requesting to log out
            try {
                final IPerson person = personManager.getPerson(request);
                if (person != null && person.getSecurityContext().isAuthenticated()) {
                    this.portalEventFactory.publishLogoutEvent(request, this, person);
                }
            } catch (final Exception e) {
                logger.error(
                        "Exception recording logout " + "associated with request " + request, e);
            }

            final String originalUid = this.identitySwapperManager.getOriginalUsername(session);
            // Logging out from a swapped user, just redirect to the Login servlet
            if (originalUid != null) {
                redirect = request.getContextPath() + "/Login";
            } else {
                // Clear out the existing session for the user
                try {
                    session.invalidate();
                } catch (final IllegalStateException ise) {
                    // IllegalStateException indicates session was already invalidated.
                    // This is fine.  LogoutController is looking to guarantee the logged out
                    // session is invalid;
                    // it need not insist that it be the one to perform the invalidating.
                    if (logger.isTraceEnabled()) {
                        logger.trace(
                                "LogoutController encountered IllegalStateException invalidating a presumably already-invalidated session.",
                                ise);
                    }
                }
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Redirecting to " + redirect + " to send the user back to the guest page.");
        }

        final String encodedRedirectURL = response.encodeRedirectURL(redirect);
        response.sendRedirect(encodedRedirectURL);
    }

    /**
     * The redirect location is specified as <code>logoutRedirect.root</code> in the Spring
     * environment.
     *
     * @return The redirection URL
     */
    private String selectRedirectionUrl(HttpServletRequest request) {

        final String defaultRedirect = request.getContextPath() + "/";

        String result = null; // default

        // Get the person object associated with the request
        final IPerson person = this.personManager.getPerson(request);
        final String username = person != null ? person.getUserName() : "[unavailable]";

        if (person != null) {
            // Analyze the user's authentication status
            final ISecurityContext securityContext = person.getSecurityContext();
            if (securityContext.isAuthenticated() && StringUtils.isNotBlank(logoutRedirect)) {
                result = logoutRedirect;
            }
        }

        // Otherwise use a sensible default...
        result =
                result != null
                        ? urlCustomizer.customizeUrl(request, result)
                        : urlCustomizer.customizeUrl(request, defaultRedirect);

        logger.debug("Calculated redirectionURL='{}' for user='{}'", result, username);

        return result;
    }
}
