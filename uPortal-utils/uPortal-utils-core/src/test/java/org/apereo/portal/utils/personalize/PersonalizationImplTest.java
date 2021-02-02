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
package org.apereo.portal.utils.personalize;

import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpSession;
import junit.framework.TestCase;
import org.apereo.portal.security.IPerson;
import org.springframework.mock.web.MockHttpSession;

public class PersonalizationImplTest extends TestCase {

    public void testPersonalizationSessionCacheCorrupted() {
        final IPersonalizer p = PersonalizationTestUtils.mockPersonalizer();
        final IPerson user1 = PersonalizationTestUtils.mockPerson("user1");
        final HttpSession session = new MockHttpSession();

        //  Adding an object that is not a map as the cache
        session.setAttribute(PersonalizationConstants.USER_PERSONALIZATION_TOKENS, new Date());

        String original = "This is a test for {{apereo.username}}";
        String resp = p.personalize(user1, original, session);
        assertEquals("This is a test for user1", resp);

        // At this point, the session cache should be rebuilt
        Map<String, String> tokens =
                (Map<String, String>)
                        session.getAttribute(PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
        assertEquals("user1", tokens.get("apereo.username"));
    }

    public void testPersonalizationSessionCache() {
        final IPersonalizer p = PersonalizationTestUtils.mockPersonalizer();
        final IPerson user1 = PersonalizationTestUtils.mockPerson("user1");
        final HttpSession session = new MockHttpSession();

        final String original = "This is a test for {{apereo.username}}";
        final String resp = p.personalize(user1, original, session);
        assertEquals("This is a test for user1", resp);

        // At this point, the session cache should be set
        final Map<String, String> tokens =
                (Map<String, String>)
                        session.getAttribute(PersonalizationConstants.USER_PERSONALIZATION_TOKENS);
        assertEquals("user1", tokens.get("apereo.username"));

        // Manually change the session personalization tokens and try a round of personalization.
        //  The session token should be used, not the person attribute
        tokens.put("apereo.username", "user2");

        final String resp2 = p.personalize(user1, original, session);
        assertEquals("This is a test for user2", resp2);
    }

    public void testPersonalizationNoSessionCache() {
        final IPerson user1 = PersonalizationTestUtils.mockPerson("user1");
        final IPerson user2 = PersonalizationTestUtils.mockPerson("user2");

        final IPersonalizer p = PersonalizationTestUtils.mockPersonalizer();

        final String original = "This is a test for {{apereo.username}}";
        final String resp = p.personalize(user1, original);
        assertEquals("This is a test for user1", resp);

        final String resp2 = p.personalize(user2, original);
        assertEquals("This is a test for user2", resp2);
    }

    public void testPersonalizationNoUser() {
        IPersonalizer p = new PersonalizerImpl();
        final String original = "asdf";
        final String resp = p.personalize(null, original);
        assertEquals(original, resp);
    }
}
