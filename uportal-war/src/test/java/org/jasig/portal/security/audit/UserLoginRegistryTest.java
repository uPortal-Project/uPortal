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

package org.jasig.portal.security.audit;

import org.jasig.portal.security.audit.dao.IUserLoginDao;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserLoginRegistryTest {

    private UserLoginRegistry registryUnderTest;

    @Mock private IUserLoginDao userLoginDao;

    @Before
    public void setUp() {

        initMocks(this);

        registryUnderTest = new UserLoginRegistry();
        registryUnderTest.setUserLoginDao(userLoginDao);
    }

    /**
     * Test that registering a login yields a create call on the injected DAO.
     */
    @Test
    public void storesRegisteredLoginViaDao() {

        final Instant now = Instant.now();

        registryUnderTest.storeUserLogin("username", now);

        verify(userLoginDao).createUserLogin("username", now);
    }

}
