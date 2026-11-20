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
package org.apereo.portal.layout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.UserProfile;
import org.apereo.portal.layout.dao.IStylesheetDescriptorDao;
import org.apereo.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.apereo.portal.security.IPerson;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserLayoutHelperImplTest {

    /**
     * A reset of all profiles updates the profile rows one at a time. It first locks them all in
     * the statement a layout save locks them with, so the two take the rows in the same order and
     * can't deadlock.
     */
    @Test
    public void testResetOfAllProfilesLocksTheProfileRowsFirst() {
        final IUserIdentityStore userIdentityStore = mock(IUserIdentityStore.class);
        when(userIdentityStore.getPortalUID(any(IPerson.class), anyBoolean())).thenReturn(20);
        final IUserLayoutStore userLayoutStore = mock(IUserLayoutStore.class);
        final Hashtable<Integer, UserProfile> profiles = new Hashtable<>();
        profiles.put(1, mock(UserProfile.class));
        profiles.put(2, mock(UserProfile.class));
        when(userLayoutStore.getUserProfileList(any(IPerson.class))).thenReturn(profiles);
        final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        final IPersonAttributes student = mock(IPersonAttributes.class);
        when(student.getAttributes())
                .thenReturn(Collections.singletonMap("username", List.<Object>of("student")));

        final UserLayoutHelperImpl helper = new UserLayoutHelperImpl();
        helper.setUserIdentityStore(userIdentityStore);
        helper.setUserLayoutStore(userLayoutStore);
        helper.setStylesheetDescriptorDao(mock(IStylesheetDescriptorDao.class));
        helper.setStylesheetUserPreferencesDao(mock(IStylesheetUserPreferencesDao.class));
        helper.setJdbcTemplate(jdbcTemplate);

        helper.resetUserLayoutAllProfiles(student);

        final InOrder order = inOrder(jdbcTemplate, userLayoutStore);
        order.verify(jdbcTemplate)
                .update("UPDATE UP_USER_PROFILE SET LAYOUT_ID=LAYOUT_ID WHERE USER_ID=?", 20);
        order.verify(userLayoutStore, times(2))
                .updateUserProfile(any(IPerson.class), any(IUserProfile.class));
    }
}
