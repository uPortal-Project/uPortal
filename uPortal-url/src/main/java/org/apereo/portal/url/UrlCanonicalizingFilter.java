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
package org.apereo.portal.url;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.math.NumberUtils;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/** Sets request info headers and forces canonical portal URLs */
public class UrlCanonicalizingFilter extends OncePerRequestFilter {
    private static final String COOKIE_NAME = "UrlCanonicalizingFilter.REDIRECT_COUNT";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private IUrlSyntaxProvider urlSyntaxProvider;
    private IPersonManager personManager;
    private PortalHttpServletFactoryService servletFactoryService;
    private int maximumRedirects = 5;
    private LoginRefUrlEncoder loginRefUrlEncoder;

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setServletFactoryService(PortalHttpServletFactoryService servletFactoryService) {
        this.servletFactoryService = servletFactoryService;
    }

    @Autowired(required = false)
    public void setLoginRefUrlEncoder(LoginRefUrlEncoder loginRefUrlEncoder) {
        this.loginRefUrlEncoder = loginRefUrlEncoder;
    }
    /** Maximum number of consecutive redirects that are allowed. Defaults to 5. */
    public void setMaximumRedirects(int maximumRedirects) {
        this.maximumRedirects = maximumRedirects;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("GET".equals(request.getMethod())) {
            final String canonicalUrl = this.urlSyntaxProvider.getCanonicalUrl(request);

            final String canonicalUri;
            final int queryStringIndex = canonicalUrl.indexOf("?");
            if (queryStringIndex < 0) {
                canonicalUri = canonicalUrl;
            } else {
                canonicalUri = canonicalUrl.substring(0, queryStringIndex);
            }

            String requestURI = request.getRequestURI();
            // If cookies are disabled and Tomcat has appended the sessionId to the URL, remove the
            // jsessionid for the purposes of comparing the request URI to the canonical URI.  This
            // allows a search indexing engine such as Googlebot to access the guest view of a
            // uportal
            // page which typically renders OK (not guaranteed depending upon content).  See
            // UP-4414.
            if (requestURI.contains(";jsessionid")) {
                requestURI = requestURI.substring(0, requestURI.indexOf(";"));
            }

            final int redirectCount = this.getRedirectCount(request);
            if (!canonicalUri.equals(requestURI)) {
                if (redirectCount < this.maximumRedirects) {
                    this.setRedirectCount(request, response, redirectCount + 1);

                    /*
                     * This is the place where we should decide if...
                     *   - (1) the user is a guest
                     *   - (2) the canonicalUrl is not the requested content
                     *   - (3) there is a strategy for external login
                     *
                     * If all of these are true, we should attempt to send the
                     * user to external login with a properly-encoded deep-linking
                     * service URL attached.
                     */

                    String encodedTargetUrl = null;

                    IPerson person = personManager.getPerson(request);
                    if (
                    /* #1 */ person.isGuest()
                            && /* #2 */ urlSyntaxProvider
                                    .doesRequestPathReferToSpecificAndDifferentContentVsCanonicalPath(
                                            requestURI, canonicalUri)
                            && /* #3 */ loginRefUrlEncoder != null) {
                        encodedTargetUrl = loginRefUrlEncoder.encodeLoginAndRefUrl(request);
                    }

                    if (encodedTargetUrl == null) {
                        // For whatever reason, we haven't chosen to send the
                        // user through external login, so we use the canonicalUrl
                        encodedTargetUrl = response.encodeRedirectURL(canonicalUrl);
                    }

                    response.sendRedirect(encodedTargetUrl);
                    logger.debug(
                            "Redirecting from {} to canonicalized URL {}, redirect {}",
                            requestURI,
                            canonicalUri,
                            redirectCount);
                    return;
                }

                this.clearRedirectCount(request, response);
                logger.debug(
                        "Not redirecting from {} to canonicalized URL {} due to limit of {} redirects",
                        requestURI,
                        canonicalUri,
                        redirectCount);
            } else {
                logger.trace(
                        "Requested URI {} is the canonical URL {}, "
                                + "so no (further?) redirect is necessary (after {} redirects).",
                        requestURI,
                        canonicalUri,
                        redirectCount);
                if (redirectCount > 0) {
                    this.clearRedirectCount(request, response);
                }
            }
        }

        final PortalHttpServletFactoryService.RequestAndResponseWrapper wrapper =
                servletFactoryService.createRequestAndResponseWrapper(request, response);

        filterChain.doFilter(wrapper.getRequest(), wrapper.getResponse());
    }

    private void clearRedirectCount(HttpServletRequest request, HttpServletResponse response) {
        final Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private void setRedirectCount(
            HttpServletRequest request, HttpServletResponse response, int count) {
        final Cookie cookie = new Cookie(COOKIE_NAME, Integer.toString(count));
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(30);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private int getRedirectCount(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return 0;
        }

        for (final Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                final String value = cookie.getValue();
                return NumberUtils.toInt(value, 0);
            }
        }

        return 0;
    }
}
