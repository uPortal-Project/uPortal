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
package org.apereo.portal.spring.security.preauth;

import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.security.oauth.IdTokenFactory;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public abstract class PortalPreAuthenticatedProcessingFilterTestBase {

    @InjectMocks
    org.apereo.portal.spring.security.preauth.PortalPreAuthenticatedProcessingFilter filter;

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
    @Mock IdTokenFactory idTokenFactory;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock IdentitySwapperManager identitySwapperManager;

    String username;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.username = "testuser";
        filter.setAuthenticationService(new org.apereo.portal.services.Authentication());
        filter.setApplicationEventPublisher(eventPublisher);
        filter.setIdentitySwapperManager(identitySwapperManager);
        filter.setIdTokenFactory(idTokenFactory);
        filter.afterPropertiesSet();

        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        when(personManager.getPerson(request)).thenReturn(person);
        when(person.getName()).thenReturn(this.username);
        when(person.isGuest()).thenReturn(false);
        when(person.getSecurityContext()).thenReturn(context);

        this.additionalSetup();
    }

    public abstract void additionalSetup();
}
