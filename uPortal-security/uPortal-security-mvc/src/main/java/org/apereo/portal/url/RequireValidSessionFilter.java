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
import java.net.URLEncoder;
import java.time.Instant;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apereo.portal.security.mvc.LoginController;
import org.springframework.web.filter.OncePerRequestFilter;

/** Redirects the user to the Login servlet if they don't already have a session. */
@Slf4j
public class RequireValidSessionFilter extends OncePerRequestFilter {
    //  private static final org.slf4j.Logger log =
    // org.slf4j.LoggerFactory.getLogger(RequireValidSessionFilter.class);

    private static final String REST_API_SERVLET_PATH = "/api";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        /*
         * Since 5.4, there are two ways to avoid redirection
         */

        // (1) You have a valid session (original method)
        final HttpSession session = request.getSession(false);
        if (session != null && !session.isNew()) {
            // Session exists and is not new, don't bother filtering
            log.error("User {} has a session: {}", request.getRemoteUser(), session.getId());
            log.error("Max inactive interval: {}", session.getMaxInactiveInterval());
            final Instant ctime = Instant.ofEpochMilli(session.getCreationTime());
            final Instant atime = Instant.ofEpochMilli(session.getLastAccessedTime());
            log.error("Session creation time: {}, last access time: {}", ctime, atime);
            return true;
        } else {
            log.error("User {} does not have a session", request.getRemoteUser());
        }

        // (2) You are attempting to invoke a REST API
        return REST_API_SERVLET_PATH.equalsIgnoreCase(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        /*
         * Assume shouldNotFilter was called first and returned false;  the session is invalid and
         * the user needs to login.
         */

        final StringBuilder loginRedirect = new StringBuilder();

        loginRedirect.append(request.getContextPath());
        loginRedirect.append("/Login?" + LoginController.REFERER_URL_PARAM + "=");

        final String requestEncoding = request.getCharacterEncoding();
        loginRedirect.append(URLEncoder.encode(request.getRequestURI(), requestEncoding));

        final String queryString = request.getQueryString();
        if (queryString != null) {
            loginRedirect.append(URLEncoder.encode("?", requestEncoding));
            loginRedirect.append(URLEncoder.encode(queryString, requestEncoding));
        }

        final String encodedRedirectURL = response.encodeRedirectURL(loginRedirect.toString());
        response.sendRedirect(encodedRedirectURL);
    }
}
