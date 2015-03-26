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

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    /**
     * Test that the audit service reflects the timestamp of the most recent login as reported by
     * the injected registry.
     */
    @Test
    public void reportsMostRecentLogin() {

        final ReadableInstant oneDayAgo = Instant.now().minus(Duration.standardDays(1));

        final IUserLogin buckyRecentLogin = new IUserLogin() {
            @Override public String getUserIdentifier() {
                return "bucky_badger";
            }

            @Override public ReadableInstant getInstant() {
                return oneDayAgo;
            }
        };

        when(userLoginRegistry.mostRecentLoginBy("bucky_badger")).thenReturn(buckyRecentLogin);

        assertEquals(oneDayAgo,  serviceUnderTest.timestampOfMostRecentLoginBy("bucky_badger"));

    }

    /**
     * Test that the audit service returns null when no known last login for a user.
     */
    @Test
    public void reportsNoKnownLastLogin() {

        when(userLoginRegistry.mostRecentLoginBy("bucky_badger")).thenReturn(null);

        assertNull(serviceUnderTest.timestampOfMostRecentLoginBy("bucky_badger"));

    }

}
