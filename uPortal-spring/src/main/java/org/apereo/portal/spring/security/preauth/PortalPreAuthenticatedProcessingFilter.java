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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.PortalException;
import org.apereo.portal.layout.profile.ProfileSelectionEvent;
import org.apereo.portal.portlets.swapper.IdentitySwapperPrincipal;
import org.apereo.portal.portlets.swapper.IdentitySwapperSecurityContext;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.security.mvc.LoginController;
import org.apereo.portal.services.Authentication;
import org.apereo.portal.spring.security.PortalPersonUserDetails;
import org.apereo.portal.utils.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * PortalPreAuthenticatedProcessingFilter enables Spring Security pre-authentication in uPortal by
 * returning the current IPerson object as the user details.
 *
 * <p>At login, fires ProfileSelectionEvent representing any runtime request for a profile selection
 * (as in, profile request parameter or target profile indicated by the swapper manager).
 *
 */
public class PortalPreAuthenticatedProcessingFilter
        extends AbstractPreAuthenticatedProcessingFilter {

    protected final Log swapperLog = LogFactory.getLog("org.jasig.portal.portlets.swapper");

    private String loginPath = "/Login";
    private String logoutPath = "/Logout";

    protected HashMap<String, String> credentialTokens;
    protected HashMap<String, String> principalTokens;
    protected Authentication authenticationService = null;

    private IPersonManager personManager;
    private IdentitySwapperManager identitySwapperManager;
    private ApplicationEventPublisher eventPublisher;

    private boolean clearSecurityContextPriorToPortalAuthentication = true; //default

    @Autowired
    public void setIdentitySwapperManager(IdentitySwapperManager identitySwapperManager) {
        this.identitySwapperManager = identitySwapperManager;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setAuthenticationService(Authentication authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setClearSecurityContextPriorToPortalAuthentication(boolean b) {
        this.clearSecurityContextPriorToPortalAuthentication = b;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.credentialTokens = new HashMap<String, String>(1);
        this.principalTokens = new HashMap<String, String>(1);
        this.retrieveCredentialAndPrincipalTokensFromPropertiesFile();
    }

    /**
     * Set the path to the portal's local login servlet.
     *
     * @param loginPath
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    /**
     * Set the path to the portal's local logout servlet.
     *
     * @param logoutPath
     */
    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Set up some DEBUG logging for performance troubleshooting
        final long timestamp = System.currentTimeMillis();
        UUID uuid =
                null; // Tagging with a UUID (instead of username) because username changes in the /Login process
        if (logger.isDebugEnabled()) {
            uuid = UUID.randomUUID();
            final HttpServletRequest httpr = (HttpServletRequest) request;
            logger.debug(
                    "STARTING ["
                            + uuid.toString()
                            + "] for URI="
                            + httpr.getRequestURI()
                            + " #milestone");
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String currentPath = httpServletRequest.getServletPath();

        /**
         * Override the base class's main filter method to bypass this filter if we're currently at
         * the login servlet. Since that servlet sets up the user session and authentication, we
         * need it to run before this filter is useful.
         */
        if (loginPath.equals(currentPath)) {
            final org.springframework.security.core.Authentication originalAuthentication =
                    SecurityContextHolder.getContext().getAuthentication();
            if (this.clearSecurityContextPriorToPortalAuthentication) {
                SecurityContextHolder.clearContext();
            }
            this.logForLoginPath(currentPath);
            this.doPortalAuthentication((HttpServletRequest) request, originalAuthentication);
            chain.doFilter(request, response);
        } else if (logoutPath.equals(currentPath)) {
            SecurityContextHolder.clearContext();
            this.logForLogoutPath(currentPath);
            chain.doFilter(request, response);
        }
        // otherwise, call the base class logic
        else {
            this.logForNonLoginOrLogoutPath(currentPath);
            super.doFilter(request, response, chain);
        }

        if (logger.isDebugEnabled()) {
            final HttpServletRequest httpr = (HttpServletRequest) request;
            logger.debug(
                    "FINISHED ["
                            + uuid.toString()
                            + "] for URI="
                            + httpr.getRequestURI()
                            + " in "
                            + Long.toString(System.currentTimeMillis() - timestamp)
                            + "ms #milestone");
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // if there's no session, the user hasn't yet visited the login servlet and we should just give up
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        // otherwise, use the person's current SecurityContext as the credentials
        final IPerson person = personManager.getPerson(request);
        return person.getSecurityContext();
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // if there's no session, the user hasn't yet visited the login servlet and we should just give up
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        // otherwise, use the current IPerson as the UserDetails
        final IPerson person = personManager.getPerson(request);
        final UserDetails details = new PortalPersonUserDetails(person);
        return details;
    }

    private void doPortalAuthentication(
            final HttpServletRequest request,
            final org.springframework.security.core.Authentication originalAuthentication) {

        IdentitySwapHelper identitySwapHelper = null;
        final String requestedSessionId = request.getRequestedSessionId();
        if (request.isRequestedSessionIdValid()) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "doPortalAuthentication for valid requested session id "
                                + requestedSessionId);
            }
            identitySwapHelper =
                    getIdentitySwapDataAndInvalidateSession(request, originalAuthentication);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "Requested session id "
                                + requestedSessionId
                                + " was not valid "
                                + "so no attempt to apply swapping rules.");
            }
        }

        HttpSession s = request.getSession(true);
        IPerson person = null;
        try {
            final HashMap<String, String> principals;
            final HashMap<String, String> credentials;
            person = personManager.getPerson(request);

            if (identitySwapHelper != null && identitySwapHelper.isSwapOrUnswapRequest()) {
                this.handleIdentitySwap(person, s, identitySwapHelper);
                principals = new HashMap<String, String>();
                credentials = new HashMap<String, String>();
            }
            //Norm authN path
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

        this.publishProfileSelectionEvent(person, request, identitySwapHelper);
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
            return this.originalUsername == null && this.targetUsername != null;
        }

        public boolean isUnswapRequest() {
            return this.originalUsername != null;
        }

        public boolean isSwapOrUnswapRequest() {
            return this.isSwapRequest() || this.isUnswapRequest();
        }

        public org.springframework.security.core.Authentication getOriginalAuthenticationForSwap() {
            return this.originalAuthenticationForSwap;
        }

        public org.springframework.security.core.Authentication
                getOriginalAuthenticationForUnswap() {
            return this.originalAuthenticationForUnswap;
        }

        public String getSwapFromUid() {
            if (this.isSwapRequest()) {
                return this.personName;
            }
            if (this.isUnswapRequest()) {
                return this.targetUsername;
            }
            return null;
        }

        public String getSwapToUid() {
            if (this.isSwapRequest()) {
                return this.targetUsername;
            }
            if (this.isUnswapRequest()) {
                return this.originalUsername;
            }
            return null;
        }

        public String getTargetProfile() {
            return this.targetProfile;
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Invalidating the impersonated session in un-swapping.");
                }
                s.invalidate();
            }
        } catch (IllegalStateException ise) {
            // ISE indicates session was already invalidated.
            // This is fine.  This servlet trying to guarantee that the session has been invalidated;
            // it doesn't have to insist that it is the one that invalidated it.
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "LoginServlet attempted to invalidate an already invalid session.", ise);
            }
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
            this.identitySwapperManager.setOriginalUser(
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

        //Setup the custom security context
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
            this.publishProfileSelectionEvent(event);
        } else if (identitySwapHelper != null && identitySwapHelper.isSwapRequest()) {
            final ProfileSelectionEvent event =
                    new ProfileSelectionEvent(
                            this, identitySwapHelper.getTargetProfile(), person, request);
            this.publishProfileSelectionEvent(event);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "No requested or swapper profile requested so no profile selection event.");
            }
        }
    }

    private void publishProfileSelectionEvent(final ProfileSelectionEvent event) {
        try {
            this.eventPublisher.publishEvent(event);
        } catch (final Exception exceptionFiringProfileSelection) {
            // failing to swap as the desired profile selection is bad,
            // but preventing login entirely is worse.  Log the exception and continue.
            logger.error(
                    "Exception on firing profile selection event " + event,
                    exceptionFiringProfileSelection);
        }
    }

    private void logForLoginPath(final String currentPath) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Path ["
                            + currentPath
                            + "] is loginPath, so cleared security context"
                            + " so we can re-establish it once the new session is established.");
        }
    }

    private void logForLogoutPath(final String currentPath) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Path ["
                            + currentPath
                            + "] is logoutPath, so cleared security context"
                            + " so can re-establish it once the new session is established.");
        }
    }

    private void logForNonLoginOrLogoutPath(final String currentPath) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Path ["
                            + currentPath
                            + "] is neither a login nor a logout path,"
                            + " so no uPortal-custom filtering.");
        }
    }

    private void retrieveCredentialAndPrincipalTokensFromPropertiesFile() {
        try {
            String key;
            // We retrieve the tokens representing the credential and principal
            // parameters from the security properties file.
            Properties props =
                    ResourceLoader.getResourceAsProperties(
                            getClass(), "/properties/security.properties");
            Enumeration<?> propNames = props.propertyNames();
            while (propNames.hasMoreElements()) {
                String propName = (String) propNames.nextElement();
                String propValue = props.getProperty(propName);
                if (propName.startsWith("credentialToken.")) {
                    key = propName.substring(16);
                    this.credentialTokens.put(key, propValue);
                }
                if (propName.startsWith("principalToken.")) {
                    key = propName.substring(15);
                    this.principalTokens.put(key, propValue);
                }
            }
        } catch (PortalException pe) {
            logger.error("LoginServlet::static ", pe);
        } catch (IOException ioe) {
            logger.error("LoginServlet::static ", ioe);
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
        HashMap<String, String> retHash = new HashMap<String, String>(1);

        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String contextName = entry.getKey();
            String parmName = entry.getValue();
            String parmValue = null;
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

    /**
     * Convenience method for sub-classes to access the event publisher without having to override
     * this class implementation of setApplicationEventPublisher (which this class had to override
     * in its parent class because that parent class failed to expose a getter method like this!)
     *
     * @return the Spring application event publisher.
     */
    protected final ApplicationEventPublisher getApplicationEventPublisher() {
        return this.eventPublisher;
    }
}
