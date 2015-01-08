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
package org.jasig.portal.rendering.predicates;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.IUserProfile;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for ProfileFNamePredicateTest.
 *
 * Implementation note: this test currently suffers from the TestMirrorsImplementation testing anti-pattern,
 * in that it relies upon mocking up exactly the lookup path in the implementation.  It would be better to use more
 * real objects and less mock objects by mocking up only the bits necessary to make real objects interpret the mocks
 * as meaning the indicated active profile fname.
 * @since uPortal 4.2
 */
public class ProfileFNamePredicateTest {

    @Mock private HttpServletRequest request;

    @Mock private IUserInstanceManager userInstanceManager;

    @Mock private IUserInstance userInstance;

    @Mock private IPerson person;

    @Mock private IUserPreferencesManager userPreferencesManager;

    @Mock private IUserProfile userProfile;

    private ProfileFNamePredicate predicate;

    @Before
    public void beforeTests() {

        initMocks(this);

        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);

        when(userInstance.getPerson()).thenReturn(person);

        when(userInstance.getPreferencesManager()).thenReturn(userPreferencesManager);
        when(userPreferencesManager.getUserProfile()).thenReturn(userProfile);

        when(userProfile.getProfileFname()).thenReturn("exampleUserProfileFname");

        when(person.getUserName()).thenReturn("exampleUserName");

        predicate = new ProfileFNamePredicate();
        predicate.setUserInstanceManager(userInstanceManager);

    }


    /**
     * When the profile associated with the request has the expected fname,
     * the predicate returns true.
     */
    @Test
    public void whenProfileNameMatchesReturnsTrue() {

        // configure to look for the profile fname that will be found
        predicate.setProfileFNameToMatch("exampleUserProfileFname");

        assertTrue( predicate.apply(request) );

    }

    /**
     * When the profile associated with the request does not have the configured fname,
     * the predicate returns false.
     */
    @Test
    public void whenProfileNameDoesNotMatchReturnsFalse() {

        // configure to look for a profile fname that will not be found.
        predicate.setProfileFNameToMatch("willNotFindThis");

        assertFalse( predicate.apply(request) );

    }

    @Test
    public void hasFriendlyToString() {

        predicate.setProfileFNameToMatch("someProfile");

        assertEquals("Predicate: true where profile fname is someProfile.", predicate.toString());

    }


}
