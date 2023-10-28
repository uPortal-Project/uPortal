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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.mock.portlet.om.MockPortletWindowId;
import org.apereo.portal.rendering.AnalyticsIncorporationComponent;
import org.apereo.portal.security.SystemPerson;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnalyticsIncorporationComponentEventSerializationTest {
    @InjectMocks private TestableAnalyticsIncorporationComponent analyticsIncorporationComponent;
    //    private ObjectMapper objectMapper;

    @Before
    public void setup() throws Exception {
        analyticsIncorporationComponent.afterPropertiesSet();

        //        final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
        //        omfb.afterPropertiesSet();
        //        objectMapper = omfb.getObject();
        //        analyticsIncorporationComponent.setMapper(objectMapper);
    }

    @Test
    public void testEventFiltering() throws Exception {
        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final PortalEvent.PortalEventBuilder eventBuilder =
                new PortalEvent.PortalEventBuilder(
                        this, "example.com", sessionId, SystemPerson.INSTANCE, null);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletExecutionEventBuilder =
                new PortletExecutionEvent.PortletExecutionEventBuilder(
                        eventBuilder,
                        new MockPortletWindowId("pw1"),
                        "fname1",
                        123450000,
                        Collections.<String, List<String>>emptyMap(),
                        WindowState.NORMAL,
                        PortletMode.VIEW);

        final Set<PortalEvent> portalEvents =
                ImmutableSet.<PortalEvent>of(
                        new PortletRenderExecutionEvent(
                                portletExecutionEventBuilder, false, false));

        final String result =
                analyticsIncorporationComponent.serializePortletRenderExecutionEvents(portalEvents);

        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(objectMapper.readTree("{\"pw1\":{\"fname\":\"fname1\",\"executionTimeNano\":123450000}}"), objectMapper.readTree(result));
    }

    private static class TestableAnalyticsIncorporationComponent
            extends AnalyticsIncorporationComponent {
        @Override
        protected String serializePortletRenderExecutionEvents(Set<PortalEvent> portalEvents) {
            // TODO Auto-generated method stub
            return super.serializePortletRenderExecutionEvents(portalEvents);
        }

        @Override
        protected String serializePageData(HttpServletRequest request, long startTime) {
            // TODO Auto-generated method stub
            return super.serializePageData(request, startTime);
        }
    }

    // Tests to demonstrate: https://github.com/FasterXML/jackson-databind/issues/245

    @JsonFilter(PortletRenderExecutionEventFilterMixIn.FILTER_NAME)
    private interface PortletRenderExecutionEventFilterMixIn {
        static final String FILTER_NAME = "PortletRenderExecutionEventFilter";
    }

    private PortalEvent createEvent() {
        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final PortalEvent.PortalEventBuilder eventBuilder =
                new PortalEvent.PortalEventBuilder(
                        this, "example.com", sessionId, SystemPerson.INSTANCE, null);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletExecutionEventBuilder =
                new PortletExecutionEvent.PortletExecutionEventBuilder(
                        eventBuilder,
                        new MockPortletWindowId("pw1"),
                        "fname1",
                        123450000,
                        Collections.<String, List<String>>emptyMap(),
                        WindowState.NORMAL,
                        PortletMode.VIEW);
        return new PortletRenderExecutionEvent(portletExecutionEventBuilder, false, false);
    }

    @Test
    public void testMixinNoCopy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        mapper.addMixInAnnotations(Object.class, PortletRenderExecutionEventFilterMixIn.class);
        final FilterProvider filterProvider =
                new SimpleFilterProvider()
                        .addFilter(
                                PortletRenderExecutionEventFilterMixIn.FILTER_NAME,
                                SimpleBeanPropertyFilter.filterOutAllExcept(
                                        "fname", "executionTimeNano", "parameters"));
        final ObjectWriter portletEventWriter = mapper.writer(filterProvider);

        final String result = portletEventWriter.writeValueAsString(createEvent());

        ObjectMapper objectMapper = new ObjectMapper();
        assertEquals(
            objectMapper.readTree("{\"@c\":\".PortletRenderExecutionEvent\",\"fname\":\"fname1\",\"executionTimeNano\":123450000,\"parameters\":{}}"),
            objectMapper.readTree(result));
    }

    /**
     * Fails as actual output is:
     * {"@c":".PortletRenderExecutionEvent","timestamp":1371671516798,"serverId":"example.com","eventSessionId":"1234567890123_system_AAAAAAAAAAA","userName":"system","fname":"fname1","executionTimeNano":123450000,"parameters":{},"targeted":false,"usedPortalCache":false}
     */
    @Ignore
    @Test
    public void testMixinWithCopy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        // Clone from "shared" ObjectMapper
        mapper = mapper.copy();

        mapper.addMixInAnnotations(Object.class, PortletRenderExecutionEventFilterMixIn.class);
        final FilterProvider filterProvider =
                new SimpleFilterProvider()
                        .addFilter(
                                PortletRenderExecutionEventFilterMixIn.FILTER_NAME,
                                SimpleBeanPropertyFilter.filterOutAllExcept(
                                        "fname", "executionTimeNano", "parameters"));
        final ObjectWriter portletEventWriter = mapper.writer(filterProvider);

        final String result = portletEventWriter.writeValueAsString(createEvent());

        assertEquals(
                "{\"@c\":\".PortletRenderExecutionEvent\",\"fname\":\"fname1\",\"executionTimeNano\":123450000,\"parameters\":{}}",
                result);
    }
}
