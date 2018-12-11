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
package org.apereo.portal.spring.security.preauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.layout.profile.ProfileSelectionEvent;
import org.apereo.portal.portlets.swapper.IdentitySwapperPrincipal;
import org.apereo.portal.portlets.swapper.IdentitySwapperSecurityContext;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContextFactory;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.security.mvc.LoginController;
import org.apereo.portal.security.oauth.IdTokenFactory;
import org.apereo.portal.services.Authentication;
import org.apereo.portal.services.PersonService;
import org.apereo.portal.spring.security.PortalPersonUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * PortalPreAuthenticatedProcessingFilter enables Spring Security pre-authentication in uPortal by
 * returning the current IPerson object as the user details.
 *
 * <p>At login, fires ProfileSelectionEvent representing any runtime request for a profile selection
 * (as in, profile request parameter or target profile indicated by the swapper manager).
 */
public class PortalPreAuthenticatedProcessingFilter
        extends AbstractPreAuthenticatedProcessingFilter {

    private static final String SWAPPER_LOG_NAME = "org.jasig.portal.portlets.swapper";

    /** Log for identity swapper activity. */
    private final Logger swapperLog = LoggerFactory.getLogger(SWAPPER_LOG_NAME);

    /**
     * "Regular" log. This variable covers a commons-logging log of the same name in the superclass.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String loginPath = "/Login";
    private String logoutPath = "/Logout";

    private HashMap<String, String> credentialTokens;
    private HashMap<String, String> principalTokens;
    private Authentication authenticationService = null;

    private IPersonManager personManager;
    private PersonService personService;
    private IdentitySwapperManager identitySwapperManager;
    private ApplicationEventPublisher eventPublisher;

    private boolean clearSecurityContextPriorToPortalAuthentication = true; // default

    // Empty set is the default for automated tests
    private Set<ISecurityContextFactory> securityContextFactories = Collections.emptySet();

    private IdTokenFactory idTokenFactory;

    @Autowired
    public void setIdentitySwapperManager(IdentitySwapperManager identitySwapperManager) {
        this.identitySwapperManager = identitySwapperManager;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Autowired
    public void setAuthenticationService(Authentication authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Autowired
    public void setIdTokenFactory(IdTokenFactory idTokenFactory) {
        this.idTokenFactory = idTokenFactory;
    }

    /**
     * This setting controls whether Spring's <code>SecurityContextHolder</code> will be reset
     * (emptied) whenever a login request is processed. The default is <code>true</code>, and this
     * option should only be set to <code>false</code> with extreme care. Problems occur When this
     * setting is <code>false</code> in portals that permits unauthenticated users because
     * authenticated users tend to retain their unauthenticated identity (e.g. 'guest') in Spring
     * Security.
     */
    public void setClearSecurityContextPriorToPortalAuthentication(boolean b) {
        this.clearSecurityContextPriorToPortalAuthentication = b;
    }

    @Autowired
    public void setSecurityContextFactories(Set<ISecurityContextFactory> securityContextFactories) {
        this.securityContextFactories = securityContextFactories;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        credentialTokens = new HashMap<>(1);
        principalTokens = new HashMap<>(1);
        retrieveCredentialAndPrincipalTokens();
    }

    /**
     * Set the path to the portal's local login servlet.
     *
     * @param loginPath The path to the portal's local login servlet
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    /**
     * Set the path to the portal's local logout servlet.
     *
     * @param logoutPath The path to the portal's local logout servlet
     */
    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        // Set up some DEBUG logging for performance troubleshooting
        final long timestamp = System.currentTimeMillis();
        // Tagging with a UUID (instead of username) because username changes in the /Login process
        UUID uuid = null;
        if (logger.isDebugEnabled()) {
            uuid = UUID.randomUUID();
            logger.debug(
                    "STARTING [{}] for URI='{}' #milestone",
                    uuid,
                    httpServletRequest.getRequestURI());
        }

        final String currentPath = httpServletRequest.getServletPath();

        /*
         * Override the base class's main filter method to bypass this filter if we're currently at
         * the login servlet. Since that servlet sets up the user session and authentication, we
         * need it to run before this filter is useful.
         */
        if (loginPath.equals(currentPath)) {
            final org.springframework.security.core.Authentication originalAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (clearSecurityContextPriorToPortalAuthentication) {
                SecurityContextHolder.clearContext();
            }
            logForLoginPath(currentPath);
            doPortalAuthentication((HttpServletRequest) request, originalAuthentication);
            chain.doFilter(request, response);
        } else if (logoutPath.equals(currentPath)) {
            SecurityContextHolder.clearContext();
            logForLogoutPath(currentPath);
            chain.doFilter(request, response);
        }
        // otherwise, call the base class logic
        else {
            logForNonLoginOrLogoutPath(currentPath);
            super.doFilter(request, response, chain);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "FINISHED [{}] for URI='{}' in {}ms #milestone",
                    uuid,
                    httpServletRequest.getRequestURI(),
                    Long.toString(System.currentTimeMillis() - timestamp));
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {

        /*
         * First consult the Authorization header
         */
        final String bearerToken = idTokenFactory.getBearerToken(request);
        if (StringUtils.isNotBlank(bearerToken)) {
            return bearerToken;
        }

        // if there's no session, the user hasn't yet visited the login servlet and we should just
        // give up
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        // otherwise, use the person's current SecurityContext as the credentials
        final IPerson person = personManager.getPerson(request);
        logger.debug("getPreAuthenticatedCredentials -- person=[{}]", person);
        return person.getSecurityContext();
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {

        /*
         * First consult the Authorization header
         */
        final Jws<Claims> userinfo = idTokenFactory.getUserInfo(request);
        if (userinfo != null) {
            final String username = userinfo.getBody().getSubject();
            logger.debug(
                    "Processing authentication for username='{}' based on OIDC Id token in the {} header",
                    username,
                    HttpHeaders.AUTHORIZATION);
            final IPerson person = personService.getPerson(username);
            return new PortalPersonUserDetails(person);
        }

        /*
         * Next check the session
         */
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final IPerson person = personManager.getPerson(request);
            logger.debug("getPreAuthenticatedPrincipal -- person=[{}]", person);
            return new PortalPersonUserDetails(person);
        }

        // Neither mechanism produced a principal
        return null;
    }

    private void doPortalAuthentication(
            final HttpServletRequest request,
            final org.springframework.security.core.Authentication originalAuthentication) {

        IdentitySwapHelper identitySwapHelper = null;
        final String requestedSessionId = request.getRequestedSessionId();
        if (request.isRequestedSessionIdValid()) {
            logger.debug(
                    "doPortalAuthentication for valid requested session id='{}'",
                    requestedSessionId);
            identitySwapHelper =
                    getIdentitySwapDataAndInvalidateSession(request, originalAuthentication);
        } else {
            logger.trace(
                    "Requested session id='{}' was not valid, so no attempt to apply "
                            + "swapping rules.",
                    requestedSessionId);
        }

        HttpSession s = request.getSession(true);
        IPerson person = null;
        try {
            final HashMap<String, String> principals;
            final HashMap<String, String> credentials;
            person = personManager.getPerson(request);

            if (identitySwapHelper != null && identitySwapHelper.isSwapOrUnswapRequest()) {
                handleIdentitySwap(person, s, identitySwapHelper);
                principals = new HashMap<>();
                credentials = new HashMap<>();
            }
            // Norm authN path
            else {
                // WE grab all of the principals and credentials from the request and load
                // them into their respective HashMaps.
                principals = getPropertyFromRequest(principalTokens, request);
                credentials = getPropertyFromRequest(credentialTokens, request);
            }

            // Attempt to authenticate using the incoming request
            authenticationService.authenticate(request, principals, credentials, person);
        } catch (Exception e) {
            // Log the exception
            logger.error("Exception authenticating the request", e);
            // Reset everything
            request.getSession(false).invalidate();
            // Add the authentication failure
            request.getSession(true).setAttribute(LoginController.AUTH_ERROR_KEY, Boolean.TRUE);
        }

        publishProfileSelectionEvent(person, request, identitySwapHelper);
    }

    /**
     * Helper inner class for encapsulating logic for determining whether or not the request is for
     * an identity "swap" or "unswap", and determining the "swap from" and "swap to" values.
     */
    class IdentitySwapHelper {
        private String originalUsername;
        private String personName;
        private String targetProfile;
        private String targetUsername;
        private org.springframework.security.core.Authentication originalAuthenticationForSwap;
        private org.springframework.security.core.Authentication originalAuthenticationForUnswap;

        IdentitySwapHelper(final HttpSession s, final String personName) {
            // must pull out session data during creation because session may later be invalidated
            this.originalAuthenticationForUnswap =
                    identitySwapperManager.getOriginalAuthentication(s);
            this.originalUsername = identitySwapperManager.getOriginalUsername(s);
            this.personName = personName;
            this.targetUsername = identitySwapperManager.getTargetUsername(s);
            this.targetProfile = identitySwapperManager.getTargetProfile(s);
        }

        public boolean isSwapRequest() {
            return originalUsername == null && targetUsername != null;
        }

        public boolean isUnswapRequest() {
            return originalUsername != null;
        }

        public boolean isSwapOrUnswapRequest() {
            return isSwapRequest() || isUnswapRequest();
        }

        public org.springframework.security.core.Authentication getOriginalAuthenticationForSwap() {
            return originalAuthenticationForSwap;
        }

        public org.springframework.security.core.Authentication
                getOriginalAuthenticationForUnswap() {
            return originalAuthenticationForUnswap;
        }

        public String getSwapFromUid() {
            if (isSwapRequest()) {
                return personName;
            }
            if (isUnswapRequest()) {
                return targetUsername;
            }
            return null;
        }

        public String getSwapToUid() {
            if (isSwapRequest()) {
                return targetUsername;
            }
            if (isUnswapRequest()) {
                return originalUsername;
            }
            return null;
        }

        public String getTargetProfile() {
            return targetProfile;
        }

        public void setOriginalAuthenticationForSwap(
                final org.springframework.security.core.Authentication auth) {
            this.originalAuthenticationForSwap = auth;
        }
    }

    private IdentitySwapHelper getIdentitySwapDataAndInvalidateSession(
            final HttpServletRequest request,
            final org.springframework.security.core.Authentication originalAuth) {
        IdentitySwapHelper identitySwapHelper = null;
        try {
            HttpSession s = request.getSession(false);
            if (s != null) {
                final IPerson person = personManager.getPerson(request);
                identitySwapHelper = new IdentitySwapHelper(s, person.getName());
                if (identitySwapHelper.isSwapRequest()) {
                    identitySwapHelper.setOriginalAuthenticationForSwap(originalAuth);
                }
                logger.debug("Invalidating the impersonated session in un-swapping.");
                s.invalidate();
            }
        } catch (IllegalStateException ise) {
            // ISE indicates session was already invalidated.
            // This is fine.  This servlet trying to guarantee that the session has been
            // invalidated;
            // it doesn't have to insist that it is the one that invalidated it.
            logger.trace("LoginServlet attempted to invalidate an already invalid session.", ise);
        }
        return identitySwapHelper;
    }

    private void handleIdentitySwap(
            final IPerson person,
            final HttpSession session,
            final IdentitySwapHelper identitySwapHelper) {
        String msgFormat;
        if (identitySwapHelper.isSwapRequest()) {
            msgFormat = "Swapping identity for '%s' to '%s'";
            identitySwapperManager.setOriginalUser(
                    session,
                    identitySwapHelper.getSwapFromUid(),
                    identitySwapHelper.getSwapToUid(),
                    identitySwapHelper.getOriginalAuthenticationForSwap());
        } else {
            msgFormat = "Reverting swapped identity from '%s' to '%s'";
            if (identitySwapHelper.getOriginalAuthenticationForUnswap() != null) {
                SecurityContextHolder.getContext()
                        .setAuthentication(identitySwapHelper.getOriginalAuthenticationForUnswap());
            }
        }
        person.setUserName(identitySwapHelper.getSwapToUid());
        final String msg =
                String.format(
                        msgFormat,
                        identitySwapHelper.getSwapFromUid(),
                        identitySwapHelper.getSwapToUid());
        swapperLog.warn(msg);

        // Setup the custom security context
        final IdentitySwapperPrincipal identitySwapperPrincipal =
                new IdentitySwapperPrincipal(person);
        final IdentitySwapperSecurityContext identitySwapperSecurityContext =
                new IdentitySwapperSecurityContext(identitySwapperPrincipal);
        person.setSecurityContext(identitySwapperSecurityContext);
    }

    private void publishProfileSelectionEvent(
            final IPerson person,
            final HttpServletRequest request,
            final IdentitySwapHelper identitySwapHelper) {
        final String requestedProfile = request.getParameter(LoginController.REQUESTED_PROFILE_KEY);
        if (requestedProfile != null) {
            final ProfileSelectionEvent event =
                    new ProfileSelectionEvent(this, requestedProfile, person, request);
            publishProfileSelectionEvent(event);
        } else if (identitySwapHelper != null && identitySwapHelper.isSwapRequest()) {
            final ProfileSelectionEvent event =
                    new ProfileSelectionEvent(
                            this, identitySwapHelper.getTargetProfile(), person, request);
            publishProfileSelectionEvent(event);
        } else {
            logger.trace(
                    "No requested or swapper profile requested so no profile selection event.");
        }
    }

    private void publishProfileSelectionEvent(final ProfileSelectionEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (final Exception e) {
            // failing to swap as the desired profile selection is bad,
            // but preventing login entirely is worse.  Log the exception and continue.
            logger.error("Exception on firing profile selection event='{}'", event, e);
        }
    }

    private void logForLoginPath(final String currentPath) {
        logger.debug(
                "Path [{}] is loginPath, so cleared security context so we can re-establish "
                        + "it once the new session is established.",
                currentPath);
    }

    private void logForLogoutPath(final String currentPath) {
        logger.debug(
                "Path [{}] is logoutPath, so cleared security context so can re-establish "
                        + "it once the new session is established.",
                currentPath);
    }

    private void logForNonLoginOrLogoutPath(final String currentPath) {
        logger.trace(
                "Path [{}] is neither a login nor a logout path, so no uPortal-custom "
                        + "filtering.",
                currentPath);
    }

    private void retrieveCredentialAndPrincipalTokens() {
        for (ISecurityContextFactory fac : securityContextFactories) {
            final String principalToken = fac.getPrincipalToken();
            if (StringUtils.isNotBlank(principalToken)) {
                principalTokens.put(fac.getName(), principalToken);
            }
            final String credentialToken = fac.getCredentialToken();
            if (StringUtils.isNotBlank(credentialToken)) {
                credentialTokens.put(fac.getName(), credentialToken);
            }
        }
    }

    /**
     * Get the values represented by each token from the request and load them into a HashMap that
     * is returned.
     *
     * @param tokens
     * @param request
     * @return HashMap of properties
     */
    private HashMap<String, String> getPropertyFromRequest(
            HashMap<String, String> tokens, HttpServletRequest request) {
        // Iterate through all of the other property keys looking for the first property
        // named like propname that has a value in the request
        HashMap<String, String> retHash = new HashMap<>();

        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String contextName = entry.getKey();
            String parmName = entry.getValue();
            String parmValue;
            if (request.getAttribute(parmName) != null) {
                // Upstream components (like servlet filters) may supply information
                // for the authentication process using request attributes.
                try {
                    parmValue = (String) request.getAttribute(parmName);
                } catch (ClassCastException cce) {
                    String msg = "The request attribute '" + parmName + "' must be a String.";
                    throw new RuntimeException(msg, cce);
                }
            } else {
                // If a configured parameter isn't provided by a request attribute,
                // check request parameters (i.e. querystring, form fields).
                parmValue = request.getParameter(parmName);
            }
            // null value causes exception in context.authentication
            // alternately we could just not set parm if value is null
            if ("password".equals(parmName)) {
                // make sure we don't trim passwords, since they might have
                // leading or trailing spaces
                parmValue = (parmValue == null ? "" : parmValue);
            } else {
                parmValue = (parmValue == null ? "" : parmValue).trim();
            }

            // The relationship between the way the properties are stored and the way
            // the subcontexts are named has to be closely looked at to make this work.
            // The keys are either "root" or the subcontext name that follows "root.". As
            // as example, the contexts ["root", "root.simple", "root.cas"] are represented
            // as ["root", "simple", "cas"].
            String key = (contextName.startsWith("root.") ? contextName.substring(5) : contextName);
            retHash.put(key, parmValue);
        }
        return (retHash);
    }

    @Override
    public void setApplicationEventPublisher(
            ApplicationEventPublisher anApplicationEventPublisher) {
        super.setApplicationEventPublisher(anApplicationEventPublisher);
        this.eventPublisher = anApplicationEventPublisher;
    }
}
