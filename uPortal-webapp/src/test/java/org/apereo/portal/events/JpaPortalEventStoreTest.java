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
package org.apereo.portal.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.xml.namespace.QName;
import org.apereo.portal.concurrency.CallableWithoutResult;
import org.apereo.portal.concurrency.FunctionWithoutResult;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.apereo.portal.mock.portlet.om.MockPortletWindowId;
import org.apereo.portal.security.SystemPerson;
import org.apereo.portal.test.BaseRawEventsJpaDaoTest;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaRawEventsTestApplicationContext.xml")
public class JpaPortalEventStoreTest extends BaseRawEventsJpaDaoTest {
    @Autowired private IPortalEventDao portalEventDao;

    @Ignore // Breaks on move to Gradle
    @Test
    public void testStoreSingleEvents() throws Exception {
        final DateTime startDate = DateTime.now().minusDays(1);
        final DateTime endDate = DateTime.now().plusDays(1);

        final List<PortalEvent> originalEvents = generateEvents();

        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        for (final PortalEvent event : originalEvents) {
                            portalEventDao.storePortalEvent(event);
                        }
                    }
                });

        verifyGetEvents(originalEvents, startDate, endDate);
        verifyAggregateEvents(originalEvents, startDate, endDate);
        verifyAggregateEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
        deleteEvents(originalEvents, startDate, endDate);
        verifyGetEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
        verifyAggregateEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
    }

    @Ignore // Breaks on move to Gradle
    @Test
    public void testStoreBatchEvents() throws Exception {
        final DateTime startDate = DateTime.now().minusDays(1);
        final DateTime endDate = DateTime.now().plusDays(1);

        final List<PortalEvent> originalEvents = generateEvents();

        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        final DateTime oldestPortalEventTimestamp =
                                portalEventDao.getOldestPortalEventTimestamp();
                        assertNull(oldestPortalEventTimestamp);

                        final DateTime newestPortalEventTimestamp =
                                portalEventDao.getNewestPortalEventTimestamp();
                        assertNull(newestPortalEventTimestamp);
                    }
                });

        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        portalEventDao.storePortalEvents(originalEvents);
                    }
                });

        Collections.sort(
                originalEvents,
                new Comparator<PortalEvent>() {
                    @Override
                    public int compare(PortalEvent o1, PortalEvent o2) {
                        return o1.getTimestampAsDate().compareTo(o2.getTimestampAsDate());
                    }
                });

        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        final DateTime oldestPortalEventTimestamp =
                                portalEventDao.getOldestPortalEventTimestamp();
                        final DateTime newestPortalEventTimestamp =
                                portalEventDao.getNewestPortalEventTimestamp();

                        assertNotNull(oldestPortalEventTimestamp);
                        assertNotNull(newestPortalEventTimestamp);

                        assertEquals(
                                originalEvents.get(0).getTimestampAsDate().getMillis(),
                                oldestPortalEventTimestamp.getMillis());
                        assertEquals(
                                originalEvents
                                        .get(originalEvents.size() - 1)
                                        .getTimestampAsDate()
                                        .getMillis(),
                                newestPortalEventTimestamp.getMillis());
                    }
                });

        verifyGetEvents(originalEvents, startDate, endDate);
        verifyAggregateEvents(originalEvents, startDate, endDate);
        verifyAggregateEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
        deleteEvents(originalEvents, startDate, endDate);
        verifyGetEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
        verifyAggregateEvents(Collections.<PortalEvent>emptyList(), startDate, endDate);
    }

    protected void verifyGetEvents(
            final List<PortalEvent> originalEvents,
            final DateTime startDate,
            final DateTime endDate) {
        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        // Get all events
                        final List<PortalEvent> portalEvents = new LinkedList<PortalEvent>();
                        portalEventDao.getPortalEvents(
                                startDate,
                                endDate,
                                new FunctionWithoutResult<PortalEvent>() {
                                    @Override
                                    protected void applyWithoutResult(PortalEvent input) {
                                        portalEvents.add(input);
                                    }
                                });

                        assertEquals(originalEvents.size(), portalEvents.size());

                        final Iterator<PortalEvent> originalEventItr = originalEvents.iterator();
                        final Iterator<PortalEvent> eventItr = portalEvents.iterator();

                        while (originalEventItr.hasNext()) {
                            assertEquals(
                                    originalEventItr.next().getClass(), eventItr.next().getClass());
                        }
                    }
                });
    }

    protected void verifyAggregateEvents(
            final List<PortalEvent> originalEvents,
            final DateTime startDate,
            final DateTime endDate) {
        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {
                        // Get all events
                        final List<PortalEvent> portalEvents = new LinkedList<PortalEvent>();

                        final AtomicReference<DateTime> nextStart =
                                new AtomicReference<DateTime>(startDate);

                        // aggregate all events, 5 at a time.
                        final int loadSize = 10;
                        int startSize;
                        do {
                            startSize = portalEvents.size();
                            portalEventDao.aggregatePortalEvents(
                                    nextStart.get(),
                                    endDate,
                                    loadSize,
                                    new Function<PortalEvent, Boolean>() {
                                        @Override
                                        public Boolean apply(PortalEvent input) {
                                            portalEvents.add(input);
                                            nextStart.set(input.getTimestampAsDate());
                                            return Boolean.TRUE;
                                        }
                                    });
                        } while (loadSize + startSize == portalEvents.size());

                        assertEquals(originalEvents.size(), portalEvents.size());

                        final Iterator<PortalEvent> originalEventItr = originalEvents.iterator();
                        final Iterator<PortalEvent> eventItr = portalEvents.iterator();

                        while (originalEventItr.hasNext()) {
                            assertEquals(
                                    originalEventItr.next().getClass(), eventItr.next().getClass());
                        }
                    }
                });
    }

    protected void deleteEvents(
            final List<PortalEvent> originalEvents,
            final DateTime startDate,
            final DateTime endDate) {
        execute(
                new CallableWithoutResult() {
                    @Override
                    protected void callWithoutResult() {

                        // Delete the events
                        portalEventDao.deletePortalEventsBefore(endDate);
                    }
                });
    }

    private static final long EVENT_DELAY = 100;

    protected List<PortalEvent> generateEvents() throws Exception {
        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final PortalEvent.PortalEventBuilder eventBuilder =
                new PortalEvent.PortalEventBuilder(
                        this, "example.com", sessionId, SystemPerson.INSTANCE, null);

        final Set<String> groups = ImmutableSet.of("Student", "Employee");
        final Map<String, List<String>> attributes =
                ImmutableMap.of(
                        "username",
                        (List<String>) ImmutableList.of("system"),
                        "roles",
                        (List<String>) ImmutableList.of("student", "employee"));

        final List<PortalEvent> events = new LinkedList<PortalEvent>();

        events.add(new LoginEvent(eventBuilder, groups, attributes));

        Thread.sleep(EVENT_DELAY);
        events.add(
                new FolderAddedToLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "n32"));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new FolderMovedInLayoutPortalEvent(
                        eventBuilder, SystemPerson.INSTANCE, 1, "n12", "n32"));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new FolderDeletedFromLayoutPortalEvent(
                        eventBuilder, SystemPerson.INSTANCE, 1, "n24", "n32", "My Tab"));

        Thread.sleep(EVENT_DELAY);
        events.add(
                new PortletAddedToLayoutPortalEvent(
                        eventBuilder, SystemPerson.INSTANCE, 1, "n32", "portletA"));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new PortletMovedInLayoutPortalEvent(
                        eventBuilder, SystemPerson.INSTANCE, 1, "n32", "n24", "portletA"));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new PortletDeletedFromLayoutPortalEvent(
                        eventBuilder, SystemPerson.INSTANCE, 1, "n24", "portletA"));

        final PortletExecutionEvent.PortletExecutionEventBuilder portletExecutionEventBuilder =
                new PortletExecutionEvent.PortletExecutionEventBuilder(
                        eventBuilder,
                        new MockPortletWindowId("pw1"),
                        "fname",
                        12345,
                        Collections.EMPTY_MAP,
                        WindowState.NORMAL,
                        PortletMode.VIEW);

        Thread.sleep(EVENT_DELAY);
        events.add(new PortletActionExecutionEvent(portletExecutionEventBuilder));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new PortletEventExecutionEvent(
                        portletExecutionEventBuilder,
                        new QName("http://www.jasig.org/foo", "event", "e")));
        Thread.sleep(EVENT_DELAY);
        events.add(new PortletRenderExecutionEvent(portletExecutionEventBuilder, true, false));
        Thread.sleep(EVENT_DELAY);
        events.add(
                new PortletResourceExecutionEvent(
                        portletExecutionEventBuilder, "someImage.jpg", false, false));

        Thread.sleep(EVENT_DELAY);
        events.add(new LogoutEvent(eventBuilder));

        return events;
    }
}
