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
package org.jasig.portal.utils.web;

/**
 * @author Chris Waymire <cwaymire@unicon.net>
 */
import org.jasig.portal.rest.RemoteCookieCheckController;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RemoteCookieCheckFilter implements Filter {
    public static final String COOKIE_NAME = "JSESSIONID";
    public static final String REFERER_ATTRIBUTE = "COOKIE_CHECK_REFERER";

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

            if (!cookieFound) {
                ((HttpServletRequest) request).getSession(true).setAttribute(REFERER_ATTRIBUTE,((HttpServletRequest) request).getRequestURI());
                String url = ((HttpServletRequest) request).getContextPath() + "/api" + RemoteCookieCheckController.COOKIE_CHECK_REQUEST_MAPPING;
                ((HttpServletResponse) response).sendRedirect(url);
                return;
            }
        }

        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
    }
}