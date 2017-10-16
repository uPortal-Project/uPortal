/*
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
package org.apereo.portal.portlet.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PortletEntityIdStringUtilsTest {

    @Test
    public void isDelegateLayoutNode() {
        boolean result = PortletEntityIdStringUtils.isDelegateLayoutNode("dlg-5-ctf1-18_18");
        assertTrue(result);
        result = PortletEntityIdStringUtils.isDelegateLayoutNode("88_n149_52");
        assertFalse(result);
    }

    @Test
    public void hasCorrectNumberOfPartsTest() {
        boolean result = PortletEntityIdStringUtils.hasCorrectNumberOfParts("88_n149_52");
        assertTrue(result);
    }

    @Test
    public void parsePortletDefinitionId() {
        String portletDefinitionId =
                PortletEntityIdStringUtils.parsePortletDefinitionId("88_n149_52");
        assertEquals(portletDefinitionId, "88");
    }

    @Test
    public void parseLayoutNodeId() {
        String layoutNodeId = PortletEntityIdStringUtils.parseLayoutNodeId("88_n149_52");
        assertEquals(layoutNodeId, "n149");
    }

    @Test
    public void parseUserIdAsString() {
        String layoutNodeId = PortletEntityIdStringUtils.parseUserIdAsString("88_n149_52");
        assertEquals(layoutNodeId, "52");
    }
}
