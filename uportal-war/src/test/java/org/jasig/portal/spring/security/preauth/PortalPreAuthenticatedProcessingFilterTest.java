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
package org.jasig.portal.spring.security.preauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.layout.profile.ProfileSelectionEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.IdentitySwapperManager;
import org.jasig.portal.security.mvc.LoginController;
import org.jasig.portal.spring.security.PortalPersonUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortalPreAuthenticatedProcessingFilterTest {
    @InjectMocks PortalPreAuthenticatedProcessingFilter filter;

    @Mock FilterChain filterChain;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HttpSession session;
    @Mock IPersonManager personManager;
    @Mock IPerson person;
    @Mock ISecurityContext context;
    @Mock Authentication auth;
    @Mock SecurityContext initialContext;
    @Mock AuthenticationManager authenticationManager;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock IdentitySwapperManager identitySwapperManager;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        filter.setAuthenticationService(new org.jasig.portal.services.Authentication());
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setIdentitySwapperManager(identitySwapperManager);
        filter.afterPropertiesSet();
       
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(personManager.getPerson(request)).thenReturn(person);
        when(person.getName()).thenReturn("testuser");
        when(person.isGuest()).thenReturn(false);
        when(person.getSecurityContext()).thenReturn(context);
    }
    
    @Test
    public void testLogin() throws IOException, ServletException {
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(request.getServletPath()).thenReturn("/Login");
        filter.doFilter(request, response, filterChain);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testLogout() throws IOException, ServletException {
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(request.getServletPath()).thenReturn("/Logout");
        filter.doFilter(request, response, filterChain);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    public void testGetAuthenticatedCredentials() {
        ISecurityContext creds = (ISecurityContext) filter.getPreAuthenticatedCredentials(request);
        assertEquals(context, creds);
    }

    @Test
    public void testAuth() throws IOException, ServletException {
        PortalPersonUserDetails details = (PortalPersonUserDetails) filter.getPreAuthenticatedPrincipal(request);
        
        assertEquals("testuser", details.getUsername());
    }

    @Test
    public void testFiresProfileSelectionEvent() throws IOException, ServletException {
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(request.getServletPath()).thenReturn("/Login");
        when(request.getParameter(LoginController.REQUESTED_PROFILE_KEY)).thenReturn("someProfileKey");
        filter.doFilter(request, response, filterChain);

        final ProfileSelectionEvent expectedEvent =
                new ProfileSelectionEvent(filter, "someProfileKey", person, request);
        verify(this.eventPublisher).publishEvent(expectedEvent);
    }

    /**
     * Test that when swapping to another identity while specifying a target profile, fires event for that profile.
     */
    @Test
    public void testFiresSwappedToProfileSelectionEvent() throws IOException, ServletException {
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(request.getServletPath()).thenReturn("/Login");
        when(request.isRequestedSessionIdValid()).thenReturn(true);



        when(identitySwapperManager.getTargetProfile(session)).thenReturn("targetProfileKey");
        when(identitySwapperManager.getOriginalUsername(session)).thenReturn(null);
        when(identitySwapperManager.getTargetUsername(session)).thenReturn("targetUsername");

        filter.doFilter(request, response, filterChain);

        final ProfileSelectionEvent expectedEvent =
                new ProfileSelectionEvent(filter, "targetProfileKey", person, request);
        verify(this.eventPublisher).publishEvent(expectedEvent);
    }

}
