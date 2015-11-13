/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.utils.web;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.rest.RemoteCookieCheckController;

public class RemoteCookieCheckFilter implements Filter {
    public static final String COOKIE_NAME = "JSESSIONID";
    public static final String REFERER_ATTRIBUTE = "COOKIE_CHECK_REFERER";

    // Set of User-Agent header values that will not be forced through the cookie check.
    private Set<Pattern> regexIgnoredUserAgents = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if(!"POST".equals(httpServletRequest.getMethod())) {
            boolean cookieFound = false;
            Cookie[] cookies = httpServletRequest.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase(COOKIE_NAME)) {
                        cookieFound = true;
                        break;
                    }
                }
            }

            String userAgent = ((HttpServletRequest) request).getHeader("User-Agent");
            if (!cookieFound && !userAgentInIgnoreList(userAgent)) {
                final HttpSession session = httpServletRequest.getSession(true);
                
                String requestURI = httpServletRequest.getRequestURI();
                final String queryString = httpServletRequest.getQueryString();
                if (queryString != null) {
                    requestURI += "?" + queryString;
                }
                
                session.setAttribute(REFERER_ATTRIBUTE, requestURI);
                String url = httpServletRequest.getContextPath() + "/api" + RemoteCookieCheckController.COOKIE_CHECK_REQUEST_MAPPING;
                ((HttpServletResponse) response).sendRedirect(url);
                return;
            }
        }

        chain.doFilter(request,response);
    }

    private boolean userAgentInIgnoreList(String userAgent) {
        for (Pattern ignorePattern : regexIgnoredUserAgents) {
            if (ignorePattern.matcher(userAgent).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    public void setRegexIgnoredUserAgents(Set<String> regexIgnoredUserAgents) {
        HashSet<Pattern> ignorePatterns = new HashSet<>();
        for (String regex : regexIgnoredUserAgents) {
            ignorePatterns.add(Pattern.compile(regex));
        }
        this.regexIgnoredUserAgents = ignorePatterns;
    }
}