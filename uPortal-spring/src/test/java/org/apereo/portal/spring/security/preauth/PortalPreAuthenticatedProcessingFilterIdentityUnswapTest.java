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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PortalPreAuthenticatedProcessingFilterIdentityUnswapTest
        extends PortalPreAuthenticatedProcessingFilterTestBase {

    private String originalUsername;
    private String targetUsername;
    @Mock Authentication originalAuthentication;

    @Override
    public void additionalSetup() {
        this.originalUsername = "originalUsername";
        this.targetUsername = "targetUsername";
        SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.getContext().setAuthentication(this.auth);
        given(this.identitySwapperManager.getOriginalAuthentication(this.session))
                .willReturn(this.originalAuthentication);
    }

    @Test
    public void testThatOriginalUsernameIsSetAsPersonUserName() throws Exception {
        // given
        this.requestIsForIdentityUnswapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        verify(this.person).setUserName(this.originalUsername);
    }

    @Test
    public void testThatOriginalAuthenticationIsSetInSecurityContext()
            throws IOException, ServletException {
        // given
        this.requestIsForIdentityUnswapLogin();
        this.requestedSessionIdIsValid();
        // when
        this.filter.doFilter(this.request, this.response, this.filterChain);
        // then
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(this.originalAuthentication, auth);
    }

    private void requestIsForIdentityUnswapLogin() {
        when(this.identitySwapperManager.getTargetProfile(this.session)).thenReturn(null);
        when(this.identitySwapperManager.getOriginalUsername(this.session))
                .thenReturn(this.originalUsername);
        when(this.identitySwapperManager.getTargetUsername(this.session))
                .thenReturn(this.targetUsername);
        when(this.request.getServletPath()).thenReturn("/Login");
    }

    private void requestedSessionIdIsValid() {
        when(this.request.isRequestedSessionIdValid()).thenReturn(true);
    }
}
