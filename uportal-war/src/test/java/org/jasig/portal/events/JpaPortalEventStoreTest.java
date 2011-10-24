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

package org.jasig.portal.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.jasig.portal.events.handlers.db.IPortalEventDao;
import org.jasig.portal.portlet.dao.jpa.BaseJpaDaoTest;
import org.jasig.portal.security.SystemPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaStatsTestApplicationContext.xml")
public class JpaPortalEventStoreTest extends BaseJpaDaoTest {
    @Autowired
    private IPortalEventDao portalEventDao;
    
    @Test
    public void testStoreAllEvents() throws Exception {
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final String sessionId = "1234567890123_system_AAAAAAAAAAA";
                final PortalEvent.PortalEventBuilder eventBuilder = new PortalEvent.PortalEventBuilder(this, "example.com", sessionId, SystemPerson.INSTANCE);
                
                final Set<String> groups = ImmutableSet.of("Student", "Employee");
                final Map<String, List<String>> attributes = ImmutableMap.of("username", (List<String>)ImmutableList.of("system"), "roles", (List<String>)ImmutableList.of("student", "employee"));
                portalEventDao.storePortalEvent(new LoginEvent(eventBuilder, groups, attributes));

                Thread.sleep(1);
                portalEventDao.storePortalEvent(new FolderAddedToLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n32"));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new FolderMovedInLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n12", "n32"));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new FolderDeletedFromLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n24", "n32", "My Tab"));
                
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletAddedToLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n32", "portletA"));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletMovedInLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n32", "n24", "portletA"));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletDeletedFromLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n24", "portletA"));
                
                
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletActionExecutionEvent(eventBuilder, "portletA", 5, null));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletEventExecutionEvent(eventBuilder, "portletA", 7, new QName("http://www.jasig.org/foo", "event", "e")));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletRenderExecutionEvent(eventBuilder, "portletA", 13, true, false));
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new PortletResourceExecutionEvent(eventBuilder, "portletA", 17, "someImage.jpg", false));
                
                Thread.sleep(1);
                portalEventDao.storePortalEvent(new LogoutEvent(eventBuilder));
                
                return null;
            }
        });
        
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Get all events
                final List<PortalEvent> portalEvents = new LinkedList<PortalEvent>();
                portalEventDao.getPortalEvents(new Date(0), new Date(Long.MAX_VALUE), new Function<PortalEvent, Object>() {
                    @Override
                    public Object apply(PortalEvent input) {
                        portalEvents.add(input);
                        return null;
                    }
                });
                
                assertEquals(12, portalEvents.size());
                
                final Iterator<PortalEvent> eventItr = portalEvents.iterator();
                
                assertTrue(eventItr.next() instanceof LoginEvent);
                
                assertTrue(eventItr.next() instanceof FolderAddedToLayoutPortalEvent);
                assertTrue(eventItr.next() instanceof FolderMovedInLayoutPortalEvent);
                assertTrue(eventItr.next() instanceof FolderDeletedFromLayoutPortalEvent);

                assertTrue(eventItr.next() instanceof PortletAddedToLayoutPortalEvent);
                assertTrue(eventItr.next() instanceof PortletMovedInLayoutPortalEvent);
                assertTrue(eventItr.next() instanceof PortletDeletedFromLayoutPortalEvent);
                
                assertTrue(eventItr.next() instanceof PortletActionExecutionEvent);
                assertTrue(eventItr.next() instanceof PortletEventExecutionEvent);
                assertTrue(eventItr.next() instanceof PortletRenderExecutionEvent);
                assertTrue(eventItr.next() instanceof PortletResourceExecutionEvent);
                
                assertTrue(eventItr.next() instanceof LogoutEvent);
                
                //Delete the events
                portalEventDao.deletePortalEvents(new Date(0), new Date(Long.MAX_VALUE));
                
                return null;
            }
        });
        
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final List<PortalEvent> portalEvents = new LinkedList<PortalEvent>();
                portalEventDao.getPortalEvents(new Date(0), new Date(Long.MAX_VALUE), new Function<PortalEvent, Object>() {
                    @Override
                    public Object apply(PortalEvent input) {
                        portalEvents.add(input);
                        return null;
                    }
                });
                
                assertEquals(0, portalEvents.size());
                
                return null;
            }
        });
    }
}
