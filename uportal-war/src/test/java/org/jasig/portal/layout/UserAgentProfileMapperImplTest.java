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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.UserAgentProfileMapper.Mapping;
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
        
        List<Mapping> mappings = new ArrayList<Mapping>();
        Mapping mapping1 = new Mapping();
        mapping1.setPattern(".*iPad.*");
        mapping1.setProfileName("tablet");
        Mapping mapping2 = new Mapping();
        mapping2.setPattern(".*iOS.*");
        mapping2.setProfileName("mobile");
        mappings.add(mapping1);
        mappings.add(mapping2);
        mapper.setMappings(mappings);
    }

    @Test
    public void testDefault() {
        final String fname = mapper.getProfileFname(person, request);
        assertEquals("profile", fname);
    }
    
    @Test
    public void testMatchProfile() {
        when(request.getHeader("agent")).thenReturn("iPad iOS", "iPhone iOS");
        
        final String fname1 = mapper.getProfileFname(person, request);
        assertEquals("tablet", fname1);
        
        final String fname2 = mapper.getProfileFname(person, request);
        assertEquals("mobile", fname2);
    }
    
}
