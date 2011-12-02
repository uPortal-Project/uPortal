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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PortalAuthenticationTest {

    @Mock IPerson person;
    @Mock ISecurityContext context;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
       
        when(person.getName()).thenReturn("testuser");
        when(person.isGuest()).thenReturn(false);
        when(person.getSecurityContext()).thenReturn(context);
    }
    
    @Test
    public void testUserAuthentication() throws IOException, ServletException {
        PortalAuthentication auth = new PortalAuthentication(person);

        assertEquals("testuser", auth.getName());
        assertEquals(true, auth.isAuthenticated());
        assertEquals(context, auth.getCredentials());
        assertEquals(person, auth.getPrincipal());
    }

    @Test
    public void testGuestAuthentication() throws IOException, ServletException {
        when(person.getName()).thenReturn("guest");
        when(person.isGuest()).thenReturn(true);
        PortalAuthentication auth = new PortalAuthentication(person);

        assertEquals("guest", auth.getName());
        assertEquals(false, auth.isAuthenticated());
        assertEquals(context, auth.getCredentials());
        assertEquals(person, auth.getPrincipal());
    }


}
