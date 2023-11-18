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
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to work with the local login form. The form presented by the login portlet is
 * typically used to generate the post to this servlet. Actual login processing occurs in
 * PortalPreAuthenticatedProcessingFilter.
 */
@Controller
@RequestMapping("/Login")
public class LoginController {
    public static final String REFERER_URL_PARAM = "refUrl";

    public static final String AUTH_ATTEMPTED_KEY = "up_authenticationAttempted";
    public static final String AUTH_ERROR_KEY = "up_authenticationError";
    public static final String ATTEMPTED_USERNAME_KEY = "up_attemptedUserName";
    public static final String REQUESTED_PROFILE_KEY = "profile";

    protected final Log log = LogFactory.getLog(getClass());
    protected final Log swapperLog = LogFactory.getLog("org.apereo.portal.portlets.swapper");

    private IPortalUrlProvider portalUrlProvider;
    private IPersonManager personManager;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    @Autowired(required = false)
    private ILoginRedirect loginRedirect;

    /**
     * Process the incoming HttpServletRequest. Note that this processing occurs after
     * PortalPreAuthenticatedProcessingFilter has run and performed pre-processing.
     *
     * @param request
     * @param response
     * @exception ServletException
     * @exception IOException
     */
    @RequestMapping
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // create the redirect URL, adding fname and args parameters if necessary
        String redirectTarget = null;

        // check for custom redirect strategies
        if (loginRedirect != null) {
            redirectTarget = loginRedirect.redirectTarget(request);
        }

        if (redirectTarget == null) {
            final String refUrl = request.getParameter(REFERER_URL_PARAM);
            final URL redirectLocation = parseLocalRefUrl(request, refUrl);
            if (redirectLocation != null) {
                redirectTarget = redirectLocation.toString();
            }

            if (redirectTarget == null) {
                /* Grab the target functional name, if any, off the login request.
                 * Also any arguments for the target. We will pass them  along after authentication.
                 */
                String targetFname = request.getParameter("uP_fname");

                if (targetFname == null) {
                    final IPortalUrlBuilder defaultUrl =
                            this.portalUrlProvider.getDefaultUrl(request);
                    redirectTarget = defaultUrl.getUrlString();
                } else {
                    try {
                        final IPortalUrlBuilder urlBuilder =
                                this.portalUrlProvider.getPortalUrlBuilderByPortletFName(
                                        request, targetFname, UrlType.RENDER);

                        Enumeration<String> e = request.getParameterNames();
                        while (e.hasMoreElements()) {
                            String paramName = e.nextElement();
                            if (!paramName.equals("uP_fname")) {
                                urlBuilder.addParameter(
                                        paramName, request.getParameterValues(paramName));
                            }
                        }

                        redirectTarget = urlBuilder.getUrlString();
                    } catch (IllegalArgumentException e) {
                        final IPortalUrlBuilder defaultUrl =
                                this.portalUrlProvider.getDefaultUrl(request);
                        redirectTarget = defaultUrl.getUrlString();
                    }
                }
            }

            IPerson person = null;

            final Object authError =
                    request.getSession(false).getAttribute(LoginController.AUTH_ERROR_KEY);
            if (authError == null || !((Boolean) authError)) {
                person = this.personManager.getPerson(request);
            }

            if (person == null || !person.getSecurityContext().isAuthenticated()) {
                if (request.getMethod().equals("POST"))
                    request.getSession(false).setAttribute(AUTH_ATTEMPTED_KEY, "true");
                // Preserve the attempted username so it can be redisplayed to the user
                String attemptedUserName = request.getParameter("userName");
                if (attemptedUserName != null)
                    request.getSession(false)
                            .setAttribute(ATTEMPTED_USERNAME_KEY, request.getParameter("userName"));
            }
        }

        final String encodedRedirectURL = response.encodeRedirectURL(redirectTarget);

        if (log.isDebugEnabled()) {
            log.debug("Redirecting to " + redirectTarget);
        }

        response.sendRedirect(encodedRedirectURL);
    }

    /**
     * Analyzes the <code>refUrl</code> parameter, if any, and attempts to produce a local (same
     * protocol, host, and port) URL based on it.
     *
     * @param request The current {@link HttpServletRequest}
     * @param refUrl The <code>refUrl</code> parameter passed on the querry string
     * @return A valid local {@link URL} or null
     */
    /* package-private */ URL parseLocalRefUrl(
            final HttpServletRequest request, final String refUrl) {
        URL result = null; // default
        if (StringUtils.isNotBlank(refUrl)) {
            try {
                final URL context = new URL(request.getRequestURL().toString());
                final URL location = new URL(context, refUrl);

                if (location.getProtocol().equals(context.getProtocol())
                        && location.getHost().equals(context.getHost())
                        && location.getPort() == context.getPort()) {
                    result = location;
                } else {
                    log.warn("The specified refUrl is not local:  " + refUrl);
                }
            } catch (Exception e) {
                log.warn("Unable to analyze specified refUrl:  " + refUrl);
                log.debug(e);
            }
        }
        return result;
    }
}
