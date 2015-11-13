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
package org.jasig.portal.events.aggr.session;

import com.google.common.collect.ImmutableSet;
import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.TestEventFactory;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.naming.CompositeName;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaAggrEventsTestContext.xml")
public class JpaEventSessionDaoTest extends BaseAggrEventsJpaDaoTest {
    @Autowired
    private ICompositeGroupService compositeGroupService;
    @Autowired
    private EventSessionDao eventSessionDao;


    @Test
    public void testEventSessionDao() throws Exception {
        final IEntityGroup everyoneGroup = mock(IEntityGroup.class);
        when(everyoneGroup.getServiceName()).thenReturn(new CompositeName("local"));
        when(everyoneGroup.getName()).thenReturn("Everyone");
        when(compositeGroupService.findGroup("local.0")).thenReturn(everyoneGroup);

        final IEntityGroup adminGroup = mock(IEntityGroup.class);
        when(adminGroup.getServiceName()).thenReturn(new CompositeName("local"));
        when(adminGroup.getName()).thenReturn("Admins");
        when(compositeGroupService.findGroup("local.1")).thenReturn(adminGroup);

        final IPerson person = mock(IPerson.class);
//
//        Example event session "1234567890_abcdefg";
        final int EVENT_SESSION_LENGTH = 17;
        final int EVENT_SESSION_HYPHEN_LENGTH = 10;

        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                DateTime timeStamp = null;
                for (int i = 0; i <= 100; i++) {
                    String sessionId = UUID.randomUUID().toString().replaceAll("[\\s\\-()]", "").substring(0, EVENT_SESSION_LENGTH - 1);
                    sessionId = sessionId.substring(0, 10) + "_" + sessionId.substring(EVENT_SESSION_HYPHEN_LENGTH, sessionId.length());
                    LoginEvent loginEvent = TestEventFactory.newLoginEvent(this, "testServer", sessionId, person,
                            ImmutableSet.<String>of("local.0", "local.1"),
                            Collections.<String, List<String>>emptyMap());
                    final EventSession eventSession = eventSessionDao.getEventSession(loginEvent);
                    assertNotNull(eventSession);
                    assertEquals(sessionId, eventSession.getEventSessionId());
                    final Set<AggregatedGroupMapping> groupMappings = eventSession.getGroupMappings();
                    assertEquals(2, groupMappings.size());
                    timeStamp = loginEvent.getTimestampAsDate();
                }
                eventSessionDao.purgeEventSessionsBefore(timeStamp.plusYears(1));
            }
        });

    }
}
