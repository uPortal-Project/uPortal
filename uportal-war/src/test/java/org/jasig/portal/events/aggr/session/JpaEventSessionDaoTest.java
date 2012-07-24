/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.events.aggr.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.naming.CompositeName;

import org.jasig.portal.concurrency.CallableWithoutResult;
import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.TestEventFactory;
import org.jasig.portal.events.aggr.groups.AggregatedGroupMapping;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.test.BaseAggrEventsJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableSet;

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

        final String eventSessionId1 = "1234567890_abcdefg";
        final String eventSessionId2 = "0000000000_aaaaaaa";
        
        final LoginEvent loginEvent1 = TestEventFactory.newLoginEvent(this, "testServer", eventSessionId1, person, 
                ImmutableSet.<String>of("local.0", "local.1"), 
                Collections.<String, List<String>>emptyMap());
        
        final LoginEvent loginEvent2 = TestEventFactory.newLoginEvent(this, "testServer", eventSessionId2, person, 
                ImmutableSet.<String>of("local.0", "local.1"), 
                Collections.<String, List<String>>emptyMap());
        

        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final EventSession eventSession1 = eventSessionDao.getEventSession(loginEvent1);
                assertNotNull(eventSession1);
                assertEquals(eventSessionId1, eventSession1.getEventSessionId());
                final Set<AggregatedGroupMapping> groupMappings1 = eventSession1.getGroupMappings();
                assertEquals(2, groupMappings1.size());
                
                final EventSession eventSession2 = eventSessionDao.getEventSession(loginEvent2);
                assertNotNull(eventSession2);
                assertEquals(eventSessionId2, eventSession2.getEventSessionId());
                final Set<AggregatedGroupMapping> groupMappings2 = eventSession2.getGroupMappings();
                assertEquals(2, groupMappings2.size());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                final EventSession eventSession = eventSessionDao.getEventSession(loginEvent1);
                assertNotNull(eventSession);
                
                assertEquals(eventSessionId1, eventSession.getEventSessionId());
                
                final Set<AggregatedGroupMapping> groupMappings = eventSession.getGroupMappings();
                assertEquals(2, groupMappings.size());
            }
        });
        
        this.execute(new CallableWithoutResult() {
            @Override
            protected void callWithoutResult() {
                eventSessionDao.purgeEventSessionsBefore(loginEvent1.getTimestampAsDate().plusYears(1));
            }
        });
    }
}
