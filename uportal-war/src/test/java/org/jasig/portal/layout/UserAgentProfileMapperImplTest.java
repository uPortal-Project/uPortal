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
package org.jasig.portal.layout;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserAgentProfileMapperImplTest {

    UserAgentProfileMapper mapper = new UserAgentProfileMapper();
    @Mock IPerson person;
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        mapper.setDefaultProfileName("profile");
        mapper.setUserAgentHeader("agent");
    }

    @Test
    public void testDefault() {
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("profile", fname);
    }
    
    @Test
    public void testMobileProfile() {
        when(request.getHeader("agent")).thenReturn("iPhone", "Android", "Blackberry");
                
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("mobileDefault", fname);
       
    }
    
}
