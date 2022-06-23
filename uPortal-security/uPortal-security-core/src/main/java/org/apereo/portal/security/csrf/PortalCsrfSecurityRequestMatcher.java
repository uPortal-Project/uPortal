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
package org.apereo.portal.security.csrf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of Spring's <code>RequestMatcher</code> that tells Spring Security which
 * requests (URLs) to protect with CSRF tokens. Nearly all URLs are protected. The exceptions are
 * uPortal's REST APIs (which are secured by other means and should be available to non-portal
 * clients) and a few odds-and-ends like /Login.
 */
@Component("portalCsrfSecurityRequestMatcher")
public class PortalCsrfSecurityRequestMatcher implements RequestMatcher {

    private static final String[] IGNORED_METHODS =
            new String[] {HttpMethod.GET.toString(), HttpMethod.HEAD.toString()};

    private static final String LOGIN_PATTERN = "/Login.*";

    private static final String LOGOUT_PATTERN = "/Logout.*";

    private static final String API_PATTERN = "/api.*";

    private static final String API_DOCS_PATTERN = "/api/swagger-ui.html";

    private static final String[] IGNORED_PATTERNS =
            new String[] {LOGIN_PATTERN, LOGOUT_PATTERN, API_PATTERN, API_DOCS_PATTERN};

    private final Set<RequestMatcher> ignoredMatchers = new HashSet<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        Arrays.stream(IGNORED_PATTERNS)
                .forEach(pattern -> ignoredMatchers.add(new RegexRequestMatcher(pattern, null)));
        logger.info(
                "CSRF token protection ignoring the following methods: {}",
                Arrays.asList(IGNORED_METHODS));
        logger.info(
                "CSRF token protection ignoring the following patterns: {}",
                Arrays.asList(IGNORED_PATTERNS));
    }

    @Override
    public boolean matches(HttpServletRequest request) {

        // Check the method
        boolean rslt =
                Arrays.stream(IGNORED_METHODS)
                        .noneMatch(s -> s.equalsIgnoreCase(request.getMethod()));

        // Check URI
        if (rslt) {
            rslt = ignoredMatchers.stream().noneMatch(matcher -> matcher.matches(request));
        }

        logger.trace(
                "Spring CSRF protection for method='{}', URI='{}' is {}",
                request.getMethod(),
                request.getRequestURI(),
                rslt ? "ENABLED" : "DISABLED");

        return rslt;
    }
}
