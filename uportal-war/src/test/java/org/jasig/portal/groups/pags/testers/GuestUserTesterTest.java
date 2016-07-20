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
package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Test;

public class GuestUserTesterTest {

    @Test
    public void testGuestTrue() throws Exception {
        GuestUserTester tester = new GuestUserTester("", "true");
        Assert.assertTrue(tester.test(createGuestPerson()));
        Assert.assertFalse(tester.test(createPerson()));
    }

    @Test
    public void testGuestFalse() throws Exception {
        GuestUserTester tester = new GuestUserTester("", "false");
        Assert.assertTrue(tester.test(createPerson()));
        Assert.assertFalse(tester.test(createGuestPerson()));
    }

    protected static IPerson createGuestPerson() throws Exception {
        IPerson person = new PersonImpl();
        person.setAttribute(IPerson.USERNAME, "guest");

        return person;
	}

	protected static IPerson createPerson() throws Exception {
        IPerson person = new PersonImpl();
        person.setAttribute(IPerson.USERNAME, "non_guest");

        return person;
	}

}
