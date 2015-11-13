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
package org.jasig.portal.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.xml.namespace.QName;

import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.jasig.portal.security.SystemPerson;
import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.jasig.portal.tenants.ITenant;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.url.UrlType;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JacksonPortalEventTest {
    private ObjectMapper mapper;
    
    @Before
    public void setup() throws Exception {
        final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
        omfb.afterPropertiesSet();
        mapper = omfb.getObject();
    }
    
    @Test
    public void testPortalEventSerialization() throws Exception {
        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final PortalEvent.PortalEventBuilder eventBuilder = new PortalEvent.PortalEventBuilder(this, "example.com", sessionId, SystemPerson.INSTANCE, null);
        
        final Set<String> groups = ImmutableSet.of("Student", "Employee");
        final Map<String, List<String>> attributes = ImmutableMap.of("username", (List<String>)ImmutableList.of("system"), "roles", (List<String>)ImmutableList.of("student", "employee"));
        
        final LoginEvent loginEvent = new LoginEvent(eventBuilder, groups, attributes);
        
        final String json = assertEventJsonEquals("{\"@c\":\".LoginEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"groups\":[\"Student\",\"Employee\"],\"attributes\":{\"username\":[\"system\"],\"roles\":[\"student\",\"employee\"]}}", loginEvent);
        
        final PortalEvent event = mapper.readValue(new StringReader(json), PortalEvent.class);
        
        assertEventJsonEquals(json, event);
    }
    
    @Test
    public void verifyOutputFormat() throws Exception {
        final String sessionId = "1234567890123_system_AAAAAAAAAAA";
        final PortalEvent.PortalEventBuilder eventBuilder = new PortalEvent.PortalEventBuilder(this, "example.com", sessionId, SystemPerson.INSTANCE, null);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletExecutionEventBuilder = new PortletExecutionEvent.PortletExecutionEventBuilder(eventBuilder, new MockPortletWindowId("pw1"), "fname", 12345, Collections.EMPTY_MAP, WindowState.NORMAL, PortletMode.VIEW);

        PortalEvent event;
        
        //TODO: Will uncomment once we add in attribute swapper event processing.
        /*event = new AttributeSwapEvent(eventBuilder, Collections.EMPTY_MAP);
        assertEventJsonEquals("{\"@c\":\".AttributeSwapEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"swappedAttributes\":{}}", event);
        
        event = new AttributeSwapResetEvent(eventBuilder);
        assertEventJsonEquals("{\"@c\":\".AttributeSwapResetEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\"}", event);
        
        event = new IdentitySwapStartEvent(eventBuilder, "originalUserName", "originalEventSessionId");
        assertEventJsonEquals("{\"@c\":\".IdentitySwapStartEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"originalUserName\":\"originalUserName\",\"originalEventSessionId\":\"originalEventSessionId\"}", event);
        
        event = new IdentitySwapStopEvent(eventBuilder, "targetUserName");
        assertEventJsonEquals("{\"@c\":\".IdentitySwapStopEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"targetUserName\":\"targetUserName\"}", event);
        */
        event = new FolderAddedToLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "newFolderId");
        assertEventJsonEquals("{\"@c\":\".FolderAddedToLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"newFolderId\":\"newFolderId\"}", event);
        
        event = new FolderDeletedFromLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "oldParentFolderId", "deletedFolderId", "deletedFolderName");
        assertEventJsonEquals("{\"@c\":\".FolderDeletedFromLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"oldParentFolderId\":\"oldParentFolderId\",\"deletedFolderId\":\"deletedFolderId\",\"deletedFolderName\":\"deletedFolderName\"}", event);
        
        event = new FolderMovedInLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "oldParentFolderId", "movedFolderId");
        assertEventJsonEquals("{\"@c\":\".FolderMovedInLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"oldParentFolderId\":\"oldParentFolderId\",\"movedFolderId\":\"movedFolderId\"}", event);
        
        event = new PortletAddedToLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "parentFolderId", "fname");
        assertEventJsonEquals("{\"@c\":\".PortletAddedToLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"fname\":\"fname\",\"parentFolderId\":\"parentFolderId\"}", event);
        
        event = new PortletDeletedFromLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "oldParentFolderId", "fname");
        assertEventJsonEquals("{\"@c\":\".PortletDeletedFromLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"fname\":\"fname\",\"oldParentFolderId\":\"oldParentFolderId\"}", event);
        
        event = new PortletMovedInLayoutPortalEvent(eventBuilder, SystemPerson.INSTANCE, 1, "oldParentFolderId", "newParentFolderId", "fname");
        assertEventJsonEquals("{\"@c\":\".PortletMovedInLayoutPortalEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"layoutId\":1,\"layoutOwner\":\"system\",\"fname\":\"fname\",\"oldParentFolderId\":\"oldParentFolderId\",\"newParentFolderId\":\"newParentFolderId\"}", event);
        
        event = new LoginEvent(eventBuilder, Collections.EMPTY_SET, Collections.EMPTY_MAP);
        assertEventJsonEquals("{\"@c\":\".LoginEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"groups\":[],\"attributes\":{}}", event);
        
        event = new LogoutEvent(eventBuilder);
        assertEventJsonEquals("{\"@c\":\".LogoutEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\"}", event);
        
        event = new PortalRenderEvent(eventBuilder, "requestPathInfo", 12345, UrlState.NORMAL, UrlType.RENDER, Collections.EMPTY_MAP, "targetedLayoutNodeId");
        assertEventJsonEquals("{\"@c\":\".PortalRenderEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"requestPathInfo\":\"requestPathInfo\",\"urlState\":\"NORMAL\",\"urlType\":\"RENDER\",\"parameters\":{},\"targetedLayoutNodeId\":\"targetedLayoutNodeId\",\"executionTimeNano\":12345}", event);
        
        event = new PortletActionExecutionEvent(portletExecutionEventBuilder);
        assertEventJsonEquals("{\"@c\":\".PortletActionExecutionEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\",\"windowState\":\"normal\",\"portletMode\":\"view\",\"executionTimeNano\":12345,\"parameters\":{}}", event);
        
        event = new PortletEventExecutionEvent(portletExecutionEventBuilder, new QName("http://example.com/uri", "EventName"));
        assertEventJsonEquals("{\"@c\":\".PortletEventExecutionEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\",\"windowState\":\"normal\",\"portletMode\":\"view\",\"executionTimeNano\":12345,\"parameters\":{},\"eventName\":\"{http://example.com/uri}EventName\"}", event);
        
        event = new PortletRenderExecutionEvent(portletExecutionEventBuilder, true, false);
        assertEventJsonEquals("{\"@c\":\".PortletRenderExecutionEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\",\"windowState\":\"normal\",\"portletMode\":\"view\",\"executionTimeNano\":12345,\"parameters\":{},\"targeted\":true,\"usedPortalCache\":false}", event);
        
        event = new PortletRenderHeaderExecutionEvent(portletExecutionEventBuilder, true, false);
        assertEventJsonEquals("{\"@c\":\".PortletRenderHeaderExecutionEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\",\"windowState\":\"normal\",\"portletMode\":\"view\",\"executionTimeNano\":12345,\"parameters\":{},\"targeted\":true,\"cached\":false}", event);

        event = new PortletResourceExecutionEvent(portletExecutionEventBuilder, "resourceId", false, false);
        assertEventJsonEquals("{\"@c\":\".PortletResourceExecutionEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\",\"windowState\":\"normal\",\"portletMode\":\"view\",\"executionTimeNano\":12345,\"parameters\":{},\"resourceId\":\"resourceId\",\"usedBrowserCache\":false,\"usedPortalCache\":false}", event);

        final IPortletExecutionWorker hungWorker = mock(IPortletExecutionWorker.class);
        when(hungWorker.getPortletFname()).thenReturn("fname");
        
        event = new PortletHungCompleteEvent(eventBuilder, hungWorker);
        assertEventJsonEquals("{\"@c\":\".PortletHungCompleteEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\"}", event);

        event = new PortletHungEvent(eventBuilder, hungWorker);
        assertEventJsonEquals("{\"@c\":\".PortletHungEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\",\"fname\":\"fname\"}", event);

        final ITenant tenant = new ITenant() {
            private static final long serialVersionUID = 1L;
            @Override
            public int compareTo(ITenant o) { return 0; }
            @Override
            public long getId() { return 1L; }
            @Override
            public String getName() { return "Mordor"; }
            @Override
            public void setName(String name) {}
            @Override
            public String getFname() { return "mordor"; }
            @Override
            public void setFname(String fname) {}
            @Override
            public String getAttribute(String name) { return null; }
            @Override
            public void setAttribute(String name, String value) {}
            @Override
            public Map<String, String> getAttributesMap() { return Collections.emptyMap(); }
        };

        event = new TenantCreatedTenantEvent(eventBuilder, tenant);
        assertEventJsonEquals("{\"@c\":\".TenantCreatedTenantEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\"}", event);

        event = new TenantUpdatedTenantEvent(eventBuilder, tenant);
        assertEventJsonEquals("{\"@c\":\".TenantUpdatedTenantEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\"}", event);

        event = new TenantRemovedTenantEvent(eventBuilder, tenant);
        assertEventJsonEquals("{\"@c\":\".TenantRemovedTenantEvent\",\"timestamp\":1371745598080,\"serverId\":\"example.com\",\"eventSessionId\":\"1234567890123_system_AAAAAAAAAAA\",\"userName\":\"system\"}", event);

    }
    
    private static final Pattern TIMESTAMP_SPLIT = Pattern.compile("(?<=\"timestamp\":)\\d+");
    private static final String TEST_NOW = "1371745598080";
    
    protected String assertEventJsonEquals(String expected, PortalEvent event) throws Exception {
        String actual = mapper.writeValueAsString(event);
        
        actual = TIMESTAMP_SPLIT.matcher(actual).replaceAll(TEST_NOW);
        expected = TIMESTAMP_SPLIT.matcher(expected).replaceAll(TEST_NOW);
        
        JSONAssert.assertEquals(expected, actual, false);
        
        return actual;
    }
}
