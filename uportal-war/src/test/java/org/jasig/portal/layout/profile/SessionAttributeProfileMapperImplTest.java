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
package org.jasig.portal.layout.profile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;

public class SessionAttributeProfileMapperImplTest {
    
    SessionAttributeProfileMapperImpl mapper = new SessionAttributeProfileMapperImpl();
    @Mock IPerson person;
    @Mock HttpServletRequest request;
    MockHttpSession session;
    
    @Before
    public void setUp() {

        session = new MockHttpSession();

        MockitoAnnotations.initMocks(this);
        when(request.getSession(false)).thenReturn(session);

        mapper.setAttributeName("key");
        
        Map<String,String> mappings = new HashMap<String,String>();
        mappings.put("key1", "fname1");
        mappings.put("key2", "fname2");
        mapper.setMappings(mappings);

        // intentionally does not  mapper.setDefaultProfileName("profile");
        // so that can test the no-default-set case
    }

    /**
     * Test that when aware of a desired profile selection,
     * and that desire maps to a configured profile fname,
     * then performs that mapping.
     *
     * This is the happy path, normal case.
     */
    @Test
    public void testMapsToProfileIndicatedByRequestedKey() {

        // first the mapper handles a profile selection request, at user /Login

        final ProfileSelectionEvent selectionEvent = new ProfileSelectionEvent(this, "key2", person, request);

        mapper.onApplicationEvent(selectionEvent);

        // then the mapper is subsequently consulted in the context of that session

        // key2 --> fname2 in the mapping config injected in setUp().
        assertEquals("fname2", mapper.getProfileFname(person, request));

    }

    /**
     * Test that when not aware of a desired profile selection,
     * returns the configured default profile name.
     */
    @Test
    public void testDefault() {
        mapper.setDefaultProfileName("defaultProfile");

        final String fname = mapper.getProfileFname(person, request);
        assertEquals("defaultProfile", fname);
    }

    /**
     * Test that when aware of a desired profile selection,
     * but that desire does not map to a configured profile fname,
     * then returns unconfigured default.
     */
    @Test
    public void testMapsToNullWhenRequestedKeyNotMappedAndNoDefaultConfigured() {

        final ProfileSelectionEvent selectionEvent = new ProfileSelectionEvent(this, "bogusKey", person, request);

        mapper.onApplicationEvent(selectionEvent);

        assertEquals(null, mapper.getProfileFname(person, request));

    }

    /**
     * Test that when aware of a desired profile selection,
     * but that desire does not map to a configured profile fname,
     * then returns configured default.
     */
    @Test
    public void testMapsToConfiguredDefaultWhenRequestedProfileKeyNotMapped() {

        mapper.setDefaultProfileName("default_profile_fname");

        final ProfileSelectionEvent selectionEvent = new ProfileSelectionEvent(this, "bogusKey", person, request);

        mapper.onApplicationEvent(selectionEvent);

        assertEquals("default_profile_fname", mapper.getProfileFname(person, request));
    }

}
