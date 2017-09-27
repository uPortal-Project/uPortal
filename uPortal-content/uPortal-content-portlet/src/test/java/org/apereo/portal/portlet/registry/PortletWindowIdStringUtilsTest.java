/**
 * Licensed to Jasig under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Jasig
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.portlet.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PortletWindowIdStringUtilsTest {

    @Test
    public void parsePortletEntityIdTest() {
        String portletEntityId = PortletWindowIdStringUtils.parsePortletEntityId("71_u54_12.2");
        assertNotNull(portletEntityId);
        assertEquals(portletEntityId, "71_u54_12");
    }

    @Test
    public void parsePortletWindowInstanceIdTest() {
        String portletWindowInstanceId =
                PortletWindowIdStringUtils.parsePortletWindowInstanceId("88_n149_52.tw");
        assertNotNull(portletWindowInstanceId);
        assertEquals(portletWindowInstanceId, "tw");
    }

    @Test
    public void hasCorrectNumberOfPartsTest() {
        Boolean result = PortletWindowIdStringUtils.hasCorrectNumberOfParts("88_n149_52.tw");
        assertTrue(result);
    }
}
