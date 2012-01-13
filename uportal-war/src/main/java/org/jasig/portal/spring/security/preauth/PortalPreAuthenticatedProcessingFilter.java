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
package org.jasig.portal.spring.security.preauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.spring.security.PortalPersonUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * PortalPreAuthenticatedProcessingFilter enables Spring Security 
 * pre-authentication in uPortal by returning the current IPerson object as
 * the user details.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class PortalPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    private String loginPath = "/Login";
    
    /**
     * Set the path to the portal's local login servlet.
     * 
     * @param loginPath
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }
    
    private String logoutPath = "/Logout";
    
    /**
     * Set the path to the portal's local logout servlet.
     * 
     * @param logoutPath
     */
    public void setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String currentPath = httpServletRequest.getServletPath();
        
        /**
         * Override the base class's main filter method to bypass this filter if
         * we're currently at the login servlet.  Since that servlet sets up the 
         * user session and authentication, we need it to run before this filter
         * is useful.
         */
        if (loginPath.equals(currentPath)) {
            // clear out the current security context so we can re-establish
            // it once the new session is established
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
        }
        
        else if (logoutPath.equals(currentPath)) {
            // clear out the current security context so we can re-establish
            // it once the new session is established
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
        }
        
        // otherwise, call the base class logic
        else {
            super.doFilter(request, response, chain);
        }
        
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // if there's no session, the user hasn't yet visited the login 
        // servlet and we should just give up
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        // otherwise, use the person's current SecurityContext as the 
        // credentials
        final IPerson person = personManager.getPerson(request);
        return person.getSecurityContext();
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // if there's no session, the user hasn't yet visited the login 
        // servlet and we should just give up
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        // otherwise, use the current IPerson as the UserDetails
        final IPerson person = personManager.getPerson(request);       
        final UserDetails details = new PortalPersonUserDetails(person);
        return details;
    }

}
