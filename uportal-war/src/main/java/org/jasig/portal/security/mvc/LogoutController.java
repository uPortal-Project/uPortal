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

package org.jasig.portal.security.mvc;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.events.IPortalEventFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.utils.ResourceLoader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple servlet to handle user logout. When a user
 * logs out, their session gets invalidated and they
 * are returned to the guest page.
 * @author Ken Weiner, kweiner@unicon.net
 * @author Don Fracapane, df7@columbia.edu
 * @version $Revision$
 */
@Controller
@RequestMapping("/Logout")
public class LogoutController implements InitializingBean {

    private static final Log log = LogFactory.getLog(LogoutController.class);

    private Map<String, String> redirectMap;
    private IPortalEventFactory portalEventFactory;
    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortalEventFactory(IPortalEventFactory portalEventFactory) {
        this.portalEventFactory = portalEventFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, String> rdHash = new HashMap<String, String>(1);

        try {
            // We retrieve the redirect strings for each context
            // from the security properties file.
            String key;
            final Properties props = ResourceLoader.getResourceAsProperties(LogoutController.class,
                    "/properties/security.properties");
            final Enumeration propNames = props.propertyNames();
            while (propNames.hasMoreElements()) {
                final String propName = (String) propNames.nextElement();
                final String propValue = props.getProperty(propName);
                if (propName.startsWith("logoutRedirect.")) {
                    key = propName.substring(15);
                    key = key.startsWith("root.") ? key.substring(5) : key;
                    if (log.isDebugEnabled()) {
                        log.debug("Redirect key=" + key + ", value=" + propValue);
                    }

                    rdHash.put(key, propValue);
                }
            }
        }
        catch (final PortalException pe) {
            log.error("Failed to load logout redirect URLs", pe);
        }
        catch (final IOException ioe) {
            log.error("Failed to load logout redirect URLs", ioe);
        }

        this.redirectMap = rdHash;
    }

    /**
     * Process the incoming request and response.
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String redirect = this.getRedirectionUrl(request);
        final HttpSession session = request.getSession(false);

        if (session != null) {
            // Record that an authenticated user is requesting to log out
            try {
                final IPerson person = personManager.getPerson(request);
                if (person != null && person.getSecurityContext().isAuthenticated()) {
                    this.portalEventFactory.publishLogoutEvent(request, this, person);
                }
            }
            catch (final Exception e) {
                log.error("Exception recording logout " + "associated with request " + request, e);
            }

            final String originalUid = (String) session.getAttribute(LoginController.SWAP_ORIGINAL_UID);
            //Logging out from a swapped user, just redirect to the Login servlet
            if (originalUid != null) {
                redirect = request.getContextPath() + "/Login";
            }
            else {
                // Clear out the existing session for the user
                try {
                    session.invalidate();
                }
                catch (final IllegalStateException ise) {
                    // IllegalStateException indicates session was already invalidated.
                    // This is fine.  LogoutController is looking to guarantee the logged out session is invalid;
                    // it need not insist that it be the one to perform the invalidating.
                    if (log.isTraceEnabled()) {
                        log.trace("LogoutController encountered IllegalStateException invalidating a presumably already-invalidated session.",
                                ise);
                    }
                }
            }
        }

        // Send the user back to the guest page
        final String encodedRedirectURL = response.encodeRedirectURL(redirect);
        response.sendRedirect(encodedRedirectURL);
    }

    /**
     * The redirect is determined based upon the context that passed authentication
     * The LogoutController looks at each authenticated context and determines if a
     * redirect exists for that context in the redirectMap variable (loaded from
     * security.properties file). The redirect is returned for the first authenticated
     * context that has an associated redirect string. If such a context is not found,
     * we use the default DEFAULT_REDIRECT that was originally setup.
     *
     * NOTE:
     * This will work or not work based upon the logic in the root context. At this time,
     * all known security contexts extend the ChainingSecurityContext class. If a context
     * has the variable stopWhenAuthenticated set to false, the user may be logged into
     * multiple security contexts. If this is the case, the logout process currently
     * implemented does not accommodate multiple logouts. As a reference implemention,
     * the current implementation assumes only one security context has been authenticated.
     * Modifications to perform multiple logouts should be considered when a concrete
     * need arises and can be handled by this class or through a change in the
     * ISecurityConext API where a context knows how to perform it's own logout.
     *
     * @param request
     * @return String representing the redirection URL
     */
    private String getRedirectionUrl(HttpServletRequest request) {
        String redirect = null;
        final String defaultRedirect = request.getContextPath() + "/";
        IPerson person = null;
        if (this.redirectMap == null) {
            return defaultRedirect;
        }
        try {
            // Get the person object associated with the request
            person = this.personManager.getPerson(request);
            if (person != null) {
                // Retrieve the security context for the user
                final ISecurityContext securityContext = person.getSecurityContext();
                if (securityContext.isAuthenticated()) {
                    if (log.isDebugEnabled()) {
                        log.debug("LogoutController::getRedirectionUrl()"
                                + " Looking for redirect string for the root context");
                    }
                    redirect = this.redirectMap.get("root");
                    if (redirect != null && !redirect.equals("")) {
                        return redirect;
                    }
                }
                final Enumeration subCtxNames = securityContext.getSubContextNames();
                while (subCtxNames.hasMoreElements()) {
                    final String subCtxName = (String) subCtxNames.nextElement();
                    if (log.isDebugEnabled()) {
                        log.debug("LogoutController::getRedirectionUrl() " + " subCtxName = " + subCtxName);
                    }
                    // strip off "root." part of name
                    final ISecurityContext sc = securityContext.getSubContext(subCtxName);
                    if (log.isDebugEnabled()) {
                        log.debug("LogoutController::getRedirectionUrl()" + " subCtxName isAuth = " + sc.isAuthenticated());
                    }
                    if (sc.isAuthenticated()) {
                        if (log.isDebugEnabled()) {
                            log.debug("LogoutController::getRedirectionUrl()"
                                    + " Looking for redirect string for subCtxName = " + subCtxName);
                        }
                        redirect = this.redirectMap.get(subCtxName);
                        if (redirect != null && !redirect.equals("")) {
                            if (log.isDebugEnabled()) {
                                log.debug("LogoutController::getRedirectionUrl()" + " subCtxName redirect = " + redirect);
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (final Exception e) {
            // Log the exception
            log.error("LogoutController::getRedirectionUrl() Error:", e);
        }
        if (redirect == null) {
            redirect = defaultRedirect;
        }
        if (log.isDebugEnabled()) {
            log.debug("LogoutController::getRedirectionUrl()" + " redirectionURL = " + redirect);
        }
        return redirect;
    }
}
