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

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for AuditService.
 */
public class AuditServiceTest {

    private AuditService serviceUnderTest;

    @Mock private IUserLoginRegistry userLoginRegistry;

    @Before
    public void setUp() {

        initMocks(this);

        serviceUnderTest = new AuditService();
        serviceUnderTest.setUserLoginRegistry(userLoginRegistry);

    }

    /**
     * Test that the AuditService passes the login audit information to the injected Registry.
     */
    @Test
    public void registersLogin() {

        final Instant momentOfLogin = Instant.now();

        serviceUnderTest.recordLogin("someone", momentOfLogin);

        verify(userLoginRegistry).storeUserLogin("someone", momentOfLogin);

    }

    /**
     * Test that the AuditService throws on attempt to record a future login,
     * when configured to propagate failures.
     */
    @Test(expected=IllegalArgumentException.class)
    public void rejectsFutureLogins() {

        serviceUnderTest.setPropogateAuditFailures(true);

        final Instant oneMinuteIntoTheFuture = Instant.now().plus(60 * 1000);

        serviceUnderTest.recordLogin("someone", oneMinuteIntoTheFuture);
    }

    /**
     * Test that the AuditService does not throw on attempt to record a future login,
     * when configured not to propagate failures.
     */
    @Test
    public void rejectsFutureLoginsSilently() {

        serviceUnderTest.setPropogateAuditFailures(false);

        final Instant oneMinuteIntoTheFuture = Instant.now().plus(60 * 1000);

        serviceUnderTest.recordLogin("someone", oneMinuteIntoTheFuture);
    }

}
