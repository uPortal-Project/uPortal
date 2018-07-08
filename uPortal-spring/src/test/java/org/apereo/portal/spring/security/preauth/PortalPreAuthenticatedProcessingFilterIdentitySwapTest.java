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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apereo.portal.layout.profile.ProfileSelectionEvent;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortalPreAuthenticatedProcessingFilterIdentitySwapTest
        extends PortalPreAuthenticatedProcessingFilterTestBase {

    private String targetProfileKey;
    private String targetUsername;

    @Override
    public void additionalSetup() {
        this.targetProfileKey = "targetProfileKey";
        this.targetUsername = "targetUsername";
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(this.auth);
    }

    @Test
    public void testThatOriginalUserIsSetWhenSecurityContextIsStillAvailable() throws Exception {
        // given
        this.filter.setClearSecurityContextPriorToPortalAuthentication(false);
        this.requestIsForIdentitySwapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        verify(this.identitySwapperManager)
                .setOriginalUser(this.session, this.username, this.targetUsername, this.auth);
    }

    @Test
    public void testThatOriginalUserIsSetWhenSecurityContextIsNoLongerAvailable() throws Exception {
        // given
        this.filter.setClearSecurityContextPriorToPortalAuthentication(true);
        this.requestIsForIdentitySwapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        verify(this.identitySwapperManager)
                .setOriginalUser(this.session, this.username, this.targetUsername, this.auth);
    }

    @Test
    public void testThatTargetUsernameIsSetAsPersonName() throws Exception {
        // given
        this.requestIsForIdentitySwapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        verify(this.person).setUserName(this.targetUsername);
    }

    /**
     * Test that when swapping to another identity while specifying a target profile, fires event
     * for that profile.
     */
    @Test
    public void testThatProfileSelectedEventIsSent() throws IOException, ServletException {
        // given
        this.requestIsForIdentitySwapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        final ProfileSelectionEvent expectedEvent =
                new ProfileSelectionEvent(
                        this.filter, this.targetProfileKey, this.person, this.request);
        verify(this.eventPublisher).publishEvent(expectedEvent);
    }

    private void requestIsForIdentitySwapLogin() {
        when(this.identitySwapperManager.getTargetProfile(this.session))
                .thenReturn(this.targetProfileKey);
        when(this.identitySwapperManager.getOriginalUsername(this.session)).thenReturn(null);
        when(this.identitySwapperManager.getTargetUsername(this.session))
                .thenReturn(this.targetUsername);
        when(this.request.getServletPath()).thenReturn("/Login");
    }

    private void requestedSessionIdIsValid() {
        when(this.request.isRequestedSessionIdValid()).thenReturn(true);
    }
}
