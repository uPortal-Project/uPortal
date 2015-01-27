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

package org.jasig.portal.events.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.security.SystemPerson;
import org.jasig.portal.security.audit.IAuditService;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AuditingLoginEventHandlerTest {

    private AuditingLoginEventHandler handlerUnderTest;

    @Mock private IAuditService auditService;


    @Before
    public void setUp() {
        initMocks(this);

        handlerUnderTest = new AuditingLoginEventHandler();
        handlerUnderTest.setAuditService(auditService);
    }

    /**
     * Test that, upon hearing a LoginEvent, presents the login to the AuditService.
     */
    @Test
    public void reportsLoginToAuditService() {

        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final Set<String> groups = ImmutableSet.of("Student", "Employee");
        final Map<String, List<String>> attributes = ImmutableMap
            .of("username", (List<String>) ImmutableList.of("system"), "roles",
                (List<String>) ImmutableList.of("student", "employee"));

        final LoginEvent loginEvent = new LoginEvent(groups, attributes,
            "example.com", sessionId, SystemPerson.INSTANCE,  /* request */ null,
            /* source */ this);


        handlerUnderTest.onApplicationEvent(loginEvent);

        verify(auditService).recordLogin("system", new Instant(loginEvent.getTimestamp()));
    }

}
