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

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for ProfileSelectionEvent.
 */
public class ProfileSelectionEventTest {

    IPerson person;
    IPerson differentPerson;
    IPerson samePerson;

    @Mock HttpServletRequest request;
    @Mock HttpServletRequest anotherRequest;

    @Before
    public void setUp() {
        initMocks(this);

        person = new PersonImpl();
        person.setID(1);

        differentPerson = new PersonImpl();
        differentPerson.setID(2);

        samePerson = new PersonImpl();
        samePerson.setID(1);
    }

    @Test
    public void testSameEventIsEqual() {
        ProfileSelectionEvent event = new ProfileSelectionEvent(this, "key", person, request);

        assertEquals(event, event);
    }

    @Test
    public void testSemanticallySameEventIsEqual() {
        final ProfileSelectionEvent event = new ProfileSelectionEvent(this, "key", person, request);
        final ProfileSelectionEvent eventWithSameStuff = new ProfileSelectionEvent(this, "key", person, request);

        assertEquals(event, eventWithSameStuff);

        final ProfileSelectionEvent eventWithDifferentRequest =
                new ProfileSelectionEvent(this, "key", person, anotherRequest);
        // since the request isn't part of the semantic equality of profile selection events,
        assertEquals(event, eventWithDifferentRequest);

        final ProfileSelectionEvent eventWithEqualButNotIdenticalPerson =
                new ProfileSelectionEvent(this, "key", samePerson, request);
        assertEquals(event, eventWithEqualButNotIdenticalPerson);
    }

    @Test
    public void testSemanticallyDifferentEventIsNotEqual() {
        final ProfileSelectionEvent event = new ProfileSelectionEvent(this, "key", person, request);

        final ProfileSelectionEvent eventWithDifferentKey =
                new ProfileSelectionEvent(this, "anotherKey", person, request);
        assertNotEquals(event, eventWithDifferentKey);

        final ProfileSelectionEvent eventWithDifferentSender =
                new ProfileSelectionEvent(event, "key", person, request);
        assertNotEquals(event, eventWithDifferentSender);

        final ProfileSelectionEvent eventWithDifferentPerson =
                new ProfileSelectionEvent(event, "key", differentPerson, request);
        assertNotEquals(event, eventWithDifferentPerson);
    }



    @Test(expected = NullPointerException.class)
    public void testNullPointerOnConstructWithNullProfileKey() {

        new ProfileSelectionEvent(this, null, person, request);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPointerOnConstructWithNullPerson() {
        new ProfileSelectionEvent(this, "profileKey", null, request);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPointerOnConstructWithNullRequest() {

        new ProfileSelectionEvent(this, "profileKey", person, null);
        fail();
    }
}
