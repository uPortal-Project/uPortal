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
package org.apereo.portal.layout.profile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IdentitySwapperManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StickyProfileMapperImplTest {

    StickyProfileMapperImpl stickyMapper;

    @Mock IPerson person;

    @Mock IPerson guestPerson;

    @Mock HttpServletRequest request;

    @Mock IProfileSelectionRegistry registry;

    @Mock IdentitySwapperManager identitySwapperManager;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        when(registry.profileSelectionForUser("bobby")).thenReturn("profileFNameFromRegistry");

        when(identitySwapperManager.isImpersonating(request)).thenReturn(false);

        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("validKey1", "profileFName1");
        mappings.put("validKey2", "profileFName2");

        stickyMapper = new StickyProfileMapperImpl();
        stickyMapper.setProfileSelectionRegistry(registry);
        stickyMapper.setIdentitySwapperManager(identitySwapperManager);
        stickyMapper.setMappings(mappings);
        stickyMapper.setProfileKeyForNoSelection("default");

        when(person.getUserName()).thenReturn("bobby");

        when(guestPerson.isGuest()).thenReturn(true);
    }

    /**
     * Test that when the underlying registry has a stored selection for a user, reflects that
     * selection.
     */
    @Test
    public void testReflectsStoredSelection() {

        final String mappedFName = stickyMapper.getProfileFname(person, request);

        assertEquals("profileFNameFromRegistry", mappedFName);
    }

    /**
     * Test that when the underlying registry has no stored selection for a user, maps to null
     * (indicating no opinion about what profile ought to be mapped.)
     */
    @Test
    public void testMapsToNullWhenNoStoredSelection() {

        // over-ride the set-up specified behavior
        when(registry.profileSelectionForUser("bobby")).thenReturn(null);

        assertNull(stickyMapper.getProfileFname(person, request));
    }

    /**
     * Test that when the underlying registry fails, translates this failure into a null mapping
     * (indicating no available opinion about what profile ought to be mapped.)
     */
    @Test
    public void testMapsToNullWhenUnderlyingRegistryThrows() {

        // over-ride the set-up specified behavior
        when(registry.profileSelectionForUser("bobby")).thenThrow(RuntimeException.class);

        assertNull(stickyMapper.getProfileFname(person, request));
    }

    /** Test that stores selection to registry. */
    @Test
    public void testStoresValidSelectionToRegistry() {

        ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "validKey1", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verify(registry).registerUserProfileSelection("bobby", "profileFName1");

        // this verifyNoMoreInteractions() is questionable
        // it makes the test over-specified, but it would catch some weird bugs wherein the profile
        // mapper
        // might have done weird unexpected things to the registry.
        verifyNoMoreInteractions(registry);
    }

    /** Test that does not store selections by guest user to registry. */
    @Test
    public void testIgnoresGuestUserProfileSelections() {

        final ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "validKey1", guestPerson, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verifyNoMoreInteractions(registry);
    }

    /**
     * Test that when the underlying registry throws in the course of handling profile selection
     * event, failure does not propagate out of the event handling method.
     */
    @Test
    public void testFailsEventHandlingGracefully() {

        doThrow(RuntimeException.class)
                .when(registry)
                .registerUserProfileSelection("bobby", "profileFName1");

        final ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "validKey1", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verify(registry).registerUserProfileSelection("bobby", "profileFName1");
    }

    /** Test that when the identity is swapped, ignores profile selection requests. */
    @Test
    public void testIgnoresSelectionWhenIdentitySwapped() {

        // override the configuration in setUp()
        when(identitySwapperManager.isImpersonating(request)).thenReturn(true);

        ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "validKey1", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verifyNoMoreInteractions(registry);
    }

    /**
     * Test that ignores requests for profile using a key that does not map to any known profile.
     */
    @Test
    public void testIgnoresInvalidProfileKey() {

        ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "bogusKey", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verifyNoMoreInteractions(registry);
    }

    /**
     * Test that when the user selects the profile key that the mapper is configured to consider
     * meaning no preference, clears the selection in the registry.
     */
    @Test
    public void testSelectingDefaultTranslatesToClearingSelection() {

        ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "default", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        verify(registry).registerUserProfileSelection("bobby", null);
    }

    /**
     * Test that when the presenting profile key both is a key in the key->fname map and is the
     * configured key-that-ought-to-mean-no-preference, apathy wins.
     */
    @Test
    public void testSelectingDefaultOverridesConfiguredMapping() {

        stickyMapper.setProfileKeyForNoSelection("validKey2");

        ProfileSelectionEvent selectionEvent =
                new ProfileSelectionEvent(this, "validKey2", person, request);

        stickyMapper.onApplicationEvent(selectionEvent);

        // verify that stored this as null, not as profileFName2
        verify(registry).registerUserProfileSelection("bobby", null);
    }

    /**
     * Test that when asked about the profile mapping for a null IPerson, throws
     * NullPointerException (which is the Validate.notNull() behavior).
     */
    @Test(expected = NullPointerException.class)
    public void testThrowsNullPointerExceptionOnNullPerson() {

        stickyMapper.getProfileFname(null, request);
    }

    /**
     * Test that when asked about the profile mapping for a null HttpServletRequest, throws
     * NullPointerException (which is the Validate.notNull() behavior).
     */
    @Test(expected = NullPointerException.class)
    public void testThrowsNullPointerExceptionOnNullServletRequestOnGetProfileFname() {

        stickyMapper.getProfileFname(person, null);
    }

    /**
     * Test that when asked to handle profile selection by a broken IPerson with a null userName,
     * throws NullPointerException (which is the Validate.notNull() behavior).
     */
    @Test(expected = NullPointerException.class)
    public void testThrowsNullPointerExceptionOnNullUsernamedIPerson() {

        // overrides the mock behavior specified in setUp().
        when(person.getUserName()).thenReturn(null);

        stickyMapper.getProfileFname(person, request);
    }
}
