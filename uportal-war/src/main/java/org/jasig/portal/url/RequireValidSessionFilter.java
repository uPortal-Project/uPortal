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

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.mvc.LoginController;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Redirects the user to the Login servlet if they don't already have a session.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequireValidSessionFilter extends OncePerRequestFilter {
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final HttpSession session = request.getSession(false);
        if (session != null && !session.isNew()) {
            //Session exists and is not new, don't bother filtering
            return true;
        }
        
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //Assume shouldNotFilter was called first and returned false, session is invalid and user needs login
        
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
