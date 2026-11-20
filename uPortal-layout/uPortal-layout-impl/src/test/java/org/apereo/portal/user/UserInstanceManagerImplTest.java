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
package org.apereo.portal.user;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.i18n.ILocaleStore;
import org.apereo.portal.i18n.LocaleManagerFactory;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.UserLayoutManagerFactory;
import org.apereo.portal.layout.profile.IProfileMapper;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.url.IPortalRequestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class UserInstanceManagerImplTest {

    private final IPerson user = mock(IPerson.class);
    private final IPerson guest = mock(IPerson.class);
    private final IPersonManager personManager = mock(IPersonManager.class);
    private final UserInstanceManagerImpl manager = new UserInstanceManagerImpl();

    /** Set by a test to act as another request that logs out while the person is read. */
    private Runnable afterPersonRead = () -> {};

    @Before
    public void setUp() {
        final IPortalRequestUtils portalRequestUtils = mock(IPortalRequestUtils.class);
        when(portalRequestUtils.getOriginalPortalRequest(any(HttpServletRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // As BasePersonManager: the person of the request's session, or else a guest
        when(personManager.getPerson(any(HttpServletRequest.class)))
                .thenAnswer(
                        invocation -> {
                            final HttpServletRequest request = invocation.getArgument(0);
                            final HttpSession session = request.getSession(false);
                            final Object person =
                                    session == null
                                            ? null
                                            : session.getAttribute(
                                                    IPersonManager.PERSON_SESSION_KEY);
                            afterPersonRead.run();
                            return person == null ? guest : person;
                        });

        final ILocaleStore localeStore = mock(ILocaleStore.class);
        when(localeStore.getUserLocales(any(IPerson.class))).thenReturn(new Locale[0]);

        final IProfileMapper profileMapper = mock(IProfileMapper.class);
        when(profileMapper.getProfileFname(any(IPerson.class), any(HttpServletRequest.class)))
                .thenReturn("default");

        final IUserLayoutStore userLayoutStore = mock(IUserLayoutStore.class);
        when(userLayoutStore.getUserProfileByFname(any(IPerson.class), any(String.class)))
                .thenReturn(mock(IUserProfile.class));

        final UserLayoutManagerFactory userLayoutManagerFactory =
                mock(UserLayoutManagerFactory.class);
        when(userLayoutManagerFactory.getUserLayoutManager(
                        any(IPerson.class), any(IUserProfile.class)))
                .thenReturn(mock(IUserLayoutManager.class));

        manager.setPortalRequestUtils(portalRequestUtils);
        manager.setPersonManager(personManager);
        manager.setLocaleStore(localeStore);
        manager.setLocaleManagerFactory(mock(LocaleManagerFactory.class));
        manager.setProfileMapper(profileMapper);
        manager.setUserLayoutStore(userLayoutStore);
        manager.setUserLayoutManagerFactory(userLayoutManagerFactory);
    }

    @Test
    public void userInstanceIsForThePersonOfTheSession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession().setAttribute(IPersonManager.PERSON_SESSION_KEY, user);

        assertSame(user, manager.getUserInstance(request).getPerson());
    }

    /**
     * A logout on another request ends the session after this request has read its person. The new
     * session must not get the logged-out user's instance: every later request with the new session
     * would render that user's layout.
     */
    @Test
    public void logoutDuringTheRequestLeavesNoUserInstanceInTheNewSession() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final HttpSession loggedIn = request.getSession();
        loggedIn.setAttribute(IPersonManager.PERSON_SESSION_KEY, user);
        afterPersonRead =
                () -> {
                    afterPersonRead = () -> {};
                    loggedIn.invalidate();
                };

        final IUserInstance duringLogout = manager.getUserInstance(request);

        assertNotSame(loggedIn, request.getSession(false));
        assertSame(guest, duringLogout.getPerson());

        // A later request with the new session
        final MockHttpServletRequest next = new MockHttpServletRequest();
        next.setSession(request.getSession(false));
        assertSame(guest, manager.getUserInstance(next).getPerson());
    }
}
