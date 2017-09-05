/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlets.account;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.portletpublishing.xml.Preference;
import org.apereo.portal.portlets.StringListAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserAccountHelperTest {

    private static Set<String> CURRENT_ATTRIBUTE_NAMES;

    private static List<Object> CURRENT_ATTRIBUTE_VALUES;

    private static final String TEST_ATTRIBUTE_NAME = "attr_name_0";

    private static final String TEST_USER_NAME = "Jane Doe";

    private static final String TEST_USER_LOGIN_TOKEN = "16526520982";

    private UserAccountHelper userAccountHelper;

    @Mock private ILocalAccountDao accountDao;

    @Mock private ILocalAccountPerson accountPerson;

    static {
        CURRENT_ATTRIBUTE_NAMES =
                Collections.unmodifiableSet(
                        new HashSet<>(
                                Arrays.asList(
                                        new String[] {
                                            "attr_name_0",
                                            "attr_name_1",
                                            "attr_name_2",
                                            "attr_name_3"
                                        })));

        CURRENT_ATTRIBUTE_VALUES =
                Collections.unmodifiableList(
                        new ArrayList<>(Arrays.asList(new String[] {"attr_value_0"})));
    }

    @Before
    public void setup() throws Exception {
        userAccountHelper = new UserAccountHelper();
        userAccountHelper.setAccountEditAttributes(generateAccountEditPreferences());
        accountDao = mock(ILocalAccountDao.class);
        accountPerson = mock(ILocalAccountPerson.class);
        when(accountPerson.getName()).thenReturn(TEST_USER_NAME);
        when(accountPerson.getId()).thenReturn(10000l);
        when(accountPerson.getAttributeValue("loginToken")).thenReturn(TEST_USER_LOGIN_TOKEN);
        when(accountDao.getCurrentAttributeNames()).thenReturn(CURRENT_ATTRIBUTE_NAMES);
        when(accountPerson.getAttributeValues(TEST_ATTRIBUTE_NAME))
                .thenReturn(CURRENT_ATTRIBUTE_VALUES);
        when(accountDao.getPerson(TEST_USER_NAME)).thenReturn(accountPerson);
        userAccountHelper.setLocalAccountDao(accountDao);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetNewAccountForm() {
        PersonForm personForm = userAccountHelper.getNewAccountForm();
        Map<String, StringListAttribute> personAttributeList = personForm.getAttributes();
        assertNotNull(personForm);
        assertEquals(4, personAttributeList.size());
    }

    @Test
    public void testGetForm() {
        PersonForm personForm = userAccountHelper.getForm(TEST_USER_NAME);
        assertEquals(personForm.getId(), 10000l);
        assertEquals(personForm.getUsername(), TEST_USER_NAME);
        // validate person attributes
        Map<String, StringListAttribute> personAttributes = personForm.getAttributes();
        assertNotNull(personAttributes);
        StringListAttribute attributeValues = personAttributes.get(TEST_ATTRIBUTE_NAME);
        List<String> values = attributeValues.getValue();
        assertEquals(values.get(0), CURRENT_ATTRIBUTE_VALUES.get(0));
    }

    @Test
    public void testValidateLoginToken() {
        boolean isTokenValid =
                userAccountHelper.validateLoginToken(TEST_USER_NAME, TEST_USER_LOGIN_TOKEN);
        assertTrue(isTokenValid);
    }

    /** @return */
    private List<Preference> generateAccountEditPreferences() {
        List<Preference> accountEditPreferences = new ArrayList<>();
        Preference preference = new Preference();
        preference.setName("pref_name_0");
        preference.setDescription("pref_description_0");
        accountEditPreferences.add(preference);
        return accountEditPreferences;
    }
}
