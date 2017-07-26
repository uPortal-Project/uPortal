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
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.events.IPortalAuthEventFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.IdentitySwapperManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple servlet to handle user logout. When a user logs out, their session gets invalidated and
 * they are returned to the guest page.
 */
@Controller
@RequestMapping("/Logout")
public class LogoutController {

    /* package-private */ static final String LOGOUT_REDIRECT_PREFIX = "logoutRedirect.";
    /* package-private */ static final String LOGOUT_REDIRECT_ROOT = "root";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IPortalAuthEventFactory portalEventFactory;
    private IPersonManager personManager;
    private IdentitySwapperManager identitySwapperManager;

    private Environment environment;

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
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Process the incoming request and response.
     *
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     */
    @RequestMapping
    public void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String redirect = this.selectRedirectionUrl(request);
        final HttpSession session = request.getSession(false);

        if (session != null) {
            // Record that an authenticated user is requesting to logger out
            try {
                final IPerson person = personManager.getPerson(request);
                if (person != null && person.getSecurityContext().isAuthenticated()) {
                    this.portalEventFactory.publishLogoutEvent(request, this, person);
                }
            } catch (final Exception e) {
                logger.error("Exception recording logout " + "associated with request " + request, e);
            }

            final String originalUid = this.identitySwapperManager.getOriginalUsername(session);
            //Logging out from a swapped user, just redirect to the Login servlet
            if (originalUid != null) {
                redirect = request.getContextPath() + "/Login";
            } else {
                // Clear out the existing session for the user
                try {
                    session.invalidate();
                } catch (final IllegalStateException ise) {
                    // IllegalStateException indicates session was already invalidated.
                    // This is fine.  LogoutController is looking to guarantee the logged out session is invalid;
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
            logger.trace("Redirecting to " + redirect + " to send the user back to the guest page.");
        }

        final String encodedRedirectURL = response.encodeRedirectURL(redirect);
        response.sendRedirect(encodedRedirectURL);

    }

    /**
     * The redirect location is chosen based upon the user's authentication status (if applicable).
     * The highest priority is a <code>logoutRedirect</code> setting defined for the specific
     * {@link ISecurityContext} through which the user is authenticated.  If there isn't a specific
     * setting -- and commonly there isn't -- the LogoutController chooses the
     * <code>logoutRedirect</code> setting defined for <code>root</code>.
     *
     * <p>NOTE: All known security contexts extend the ChainingSecurityContext class. If a
     * context has the variable stopWhenAuthenticated set to false, the user may be logged into
     * multiple security contexts. If this is the case, the first one found "wins."  (The logout
     * process currently implemented does not accommodate multiple logouts.
     *
     * @return String representing the redirection URL
     */
    private String selectRedirectionUrl(HttpServletRequest request) {

        String rslt = null; // default

        // Get the person object associated with the request
        final IPerson person = this.personManager.getPerson(request);
        final String username = person != null
                ? person.getUserName()
                : "[unavailable]";

        if (person != null) {

            // Analyze the user's authentication status
            final ISecurityContext securityContext = person.getSecurityContext();
            if (securityContext.isAuthenticated()) {

                // Prefer a 'logoutRedirect' specific to the context
                final Enumeration subCtxNames = securityContext.getSubContextNames();
                while (subCtxNames.hasMoreElements()) {
                    final String subCtxName = (String) subCtxNames.nextElement();
                    logger.debug("Considering authenticated subCtxName='{}' for user='{}'", subCtxName, username);
                    final ISecurityContext sc = securityContext.getSubContext(subCtxName);
                    if (sc.isAuthenticated()) {
                        rslt = getRedirectionUrlForSubContext(subCtxName);
                        if (StringUtils.isNotBlank(rslt)) {
                            break;
                        }
                    }
                }

                // But fall back to 'root'
                if (rslt == null) {
                    final String rootRedirectProperty = LOGOUT_REDIRECT_PREFIX + LOGOUT_REDIRECT_ROOT;
                    if (environment.containsProperty(rootRedirectProperty)) {
                        // Apparently a logoutRedirect defined at the root level takes priority...
                        rslt = environment.getProperty(rootRedirectProperty);
                    }
                }

            }
        }

        // Use a sensible default if we don't have one...
        rslt = rslt != null ? rslt : request.getContextPath() + "/";

        logger.debug("Calculated redirectionURL='{}' for user='{}'", rslt, username);

        return rslt;

    }

    /* package-private */ String getRedirectionUrlForSubContext(String subCtxName) {
        String rslt = null; // default
        final String subCtxRedirectProperty = LOGOUT_REDIRECT_PREFIX + subCtxName;
        if (environment.containsProperty(subCtxRedirectProperty)) {
            rslt = environment.getProperty(subCtxRedirectProperty);
        }
        return rslt;
    }

}
