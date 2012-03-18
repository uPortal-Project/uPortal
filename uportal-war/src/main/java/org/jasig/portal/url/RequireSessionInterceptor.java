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

package org.jasig.portal.url;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.mvc.LoginController;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Redirects the user to the Login servlet if they don't already have a session.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequireSessionInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            if (!session.isNew()) {
                return true;
            }
        }
        
        //Session is null, redirect to Login servlet
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
        
        return false;
    }
}
