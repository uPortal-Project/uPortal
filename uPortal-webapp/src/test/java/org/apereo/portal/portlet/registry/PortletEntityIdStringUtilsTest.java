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
package org.apereo.portal.portlet.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test class for {@link PortletEntityIdStringUtilsTest}
 *
 */
public class PortletEntityIdStringUtilsTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void convertToDelegateLayoutNodeIdMethodShouldReturnCorrectResult() {
        // given
        final String portletEntityIdString = "88_n149_52";
        // when
        final String result =
                PortletEntityIdStringUtils.convertToDelegateLayoutNodeId(portletEntityIdString);
        // then
        assertEquals("dlg-88-n149-52", result);
    }

    @Test
    public void formatMethodShouldReturnCorrectResult() {
        // when
        final String result = PortletEntityIdStringUtils.format("88", "n149", "52");
        // then
        assertEquals("88_n149_52", result);
    }

    @Test
    public void hasCorrectNumberOfPartsMethodShouldReturnTrueWhenPortletEntityIdHasThreeParts() {
        // given
        final String portletEntityIdString = "90_u110_18";
        // when
        final boolean result =
                PortletEntityIdStringUtils.hasCorrectNumberOfParts(portletEntityIdString);
        // then
        assertTrue(result);
    }

    @Test
    public void hasCorrectNumberOfPartsMethodShouldReturnFalseWhenPortletEntityIdHasTooFewParts() {
        // given
        final String portletEntityIdString = "90_u110";
        // when
        final boolean result =
                PortletEntityIdStringUtils.hasCorrectNumberOfParts(portletEntityIdString);
        // then
        assertFalse(result);
    }

    @Test
    public void hasCorrectNumberOfPartsMethodShouldReturnFalseWhenPortletEntityIdHasTooManyParts() {
        // given
        final String portletEntityIdString = "90_u110_18_blah";
        // when
        final boolean result =
                PortletEntityIdStringUtils.hasCorrectNumberOfParts(portletEntityIdString);
        // then
        assertFalse(result);
    }

    @Test
    public void isDelegateLayoutNodeMethodShouldReturnFalse() {
        // given
        final String layoutNodeIdString = "88_n149_52";
        // when
        final boolean result = PortletEntityIdStringUtils.isDelegateLayoutNode(layoutNodeIdString);
        // then
        assertFalse(result);
    }

    @Test
    public void isDelegateLayoutNodeMethodShouldReturnTrue() {
        // given
        final String layoutNodeIdString = "dlg-88-n149-52";
        // when
        final boolean result = PortletEntityIdStringUtils.isDelegateLayoutNode(layoutNodeIdString);
        // then
        assertTrue(result);
    }

    @Test
    public void parsePortletDefinitionIdMethodShouldReturnFirstPart() {
        // given
        final String portletEntityIdString = "90_n155_18";
        // when
        final String result =
                PortletEntityIdStringUtils.parsePortletDefinitionId(portletEntityIdString);
        // then
        assertEquals("90", result);
    }

    @Test
    public void parseLayoutNodeIdMethodShouldReturnSecondPart() {
        // given
        final String portletEntityIdString = "90_n155_18";
        // when
        final String result = PortletEntityIdStringUtils.parseLayoutNodeId(portletEntityIdString);
        // then
        assertEquals("n155", result);
    }

    @Test
    public void
            parseLayoutNodeIdMethodShouldReturnSecondPartWhenPortletEntityIdHasDelegateLayoutNode() {
        // given
        final String portletEntityIdString = "90_dlg-5-ctf1-18_18";
        // when
        final String result = PortletEntityIdStringUtils.parseLayoutNodeId(portletEntityIdString);
        // then
        assertEquals("dlg-5-ctf1-18", result);
    }

    @Test
    public void
            parseLayoutNodeIdMethodShouldReturnSecondPartWhenPortletEntityIdHasDelegateLayoutNodeWithWindowInstanceId() {
        // given
        final String portletEntityIdString = "90_dlg-5-ctf1-18.tw_18";
        // when
        final String result = PortletEntityIdStringUtils.parseLayoutNodeId(portletEntityIdString);
        // then
        assertEquals("dlg-5-ctf1-18.tw", result);
    }

    @Test
    public void parseUserIdAsStringMethodShouldReturnThirdPart() {
        // given
        final String portletEntityIdString = "90_n155_18";
        // when
        final String result = PortletEntityIdStringUtils.parseUserIdAsString(portletEntityIdString);
        // then
        assertEquals("18", result);
    }
}
