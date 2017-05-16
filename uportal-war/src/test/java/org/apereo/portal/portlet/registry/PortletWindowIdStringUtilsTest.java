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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test class for {@link PortletWindowIdStringUtilsTest}
 *
 */
public class PortletWindowIdStringUtilsTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void
            convertToDelegateLayoutNodeIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasNoWindowInstanceId() {
        // given
        final String portletWindowIdString = "88_n149_52";
        // when
        final String result =
                PortletWindowIdStringUtils.convertToDelegateLayoutNodeId(portletWindowIdString);
        // then
        assertEquals("dlg-88-n149-52", result);
    }

    @Test
    public void
            convertToDelegateLayoutNodeIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasWindowInstanceId() {
        // given
        final String portletWindowIdString = "88_n149_52.tw";
        // when
        final String result =
                PortletWindowIdStringUtils.convertToDelegateLayoutNodeId(portletWindowIdString);
        // then
        assertEquals("dlg-88-n149-52.tw", result);
    }

    @Test
    public void formatMethodShouldReturnCorrectResultWhenTheWindowInstanceIdIsNotNull() {
        // given
        final String portletEntityIdString = "88_n149_52";
        final String portletWindowInstanceIdString = "tw";
        // when
        final String result =
                PortletWindowIdStringUtils.format(
                        portletEntityIdString, portletWindowInstanceIdString);
        // then
        assertEquals(portletEntityIdString + "." + portletWindowInstanceIdString, result);
    }

    @Test
    public void formatMethodShouldReturnCorrectResultWhenTheWindowInstanceIdIsNull() {
        // given
        final String portletEntityIdString = "88_n149_52";
        final String portletWindowInstanceIdString = null;
        // when
        final String result =
                PortletWindowIdStringUtils.format(
                        portletEntityIdString, portletWindowInstanceIdString);
        // then
        assertEquals(portletEntityIdString, result);
    }

    @Test
    public void
            hasCorrectNumberOfPartsMethodShouldReturnTrueWhenPortletWindowIdHasNoWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_u110_18";
        // when
        final boolean result =
                PortletWindowIdStringUtils.hasCorrectNumberOfParts(portletWindowIdString);
        // then
        assertTrue(result);
    }

    @Test
    public void
            hasCorrectNumberOfPartsMethodShouldReturnTrueWhenPortletWindowIdHasWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_u110_18.tw";
        // when
        final boolean result =
                PortletWindowIdStringUtils.hasCorrectNumberOfParts(portletWindowIdString);
        // then
        assertTrue(result);
    }

    @Test
    public void hasCorrectNumberOfPartsMethodShouldReturnFalseWhenPortletWindowIdHasTooManyParts() {
        // given
        final String portletWindowIdString = "90_u110_18.tw.blah";
        // when
        final boolean result =
                PortletWindowIdStringUtils.hasCorrectNumberOfParts(portletWindowIdString);
        // then
        assertFalse(result);
    }

    @Test
    public void
            parsePortletEntityIdMethodShouldReturnResultEqualToInputWhenPortletWindowIdHasNoWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_n155_18";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletEntityId(portletWindowIdString);
        // then
        assertEquals(portletWindowIdString, result);
    }

    @Test
    public void
            parsePortletEntityIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasWindowInstanceId() {
        // given
        final String portletEntityIdString = "90_n155_18";
        final String portletWindowIdString = portletEntityIdString + ".tw";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletEntityId(portletWindowIdString);
        // then
        assertEquals(portletEntityIdString, result);
    }

    @Test
    public void
            parsePortletEntityIdMethodShouldReturnResultEqualToInputWhenPortletWindowIdHasNoWindowInstanceIdAndHasDelegateWithWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_dlg-5-ctf1-18.tw_18";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletEntityId(portletWindowIdString);
        // then
        assertEquals(portletWindowIdString, result);
    }

    @Test
    public void
            parsePortletEntityIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasWindowInstanceIdAndHasDelegateWithWindowInstanceId() {
        // given
        final String portletEntityIdString = "90_dlg-5-ctf1-18.tw_18";
        final String portletWindowIdString = portletEntityIdString + ".tw";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletEntityId(portletWindowIdString);
        // then
        assertEquals(portletEntityIdString, result);
    }

    @Test
    public void
            parsePortletWindowInstanceIdMethodShouldReturnNullWhenWhenPortletWindowIdHasNoWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_n155_18";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletWindowInstanceId(portletWindowIdString);
        // then
        assertNull(result);
    }

    @Test
    public void
            parsePortletWindowInstanceIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasWindowInstanceId() {
        // given
        final String portletEntityIdString = "90_n155_18";
        final String portletWindowInstanceId = "tw";
        final String portletWindowIdString = portletEntityIdString + "." + portletWindowInstanceId;
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletWindowInstanceId(portletWindowIdString);
        // then
        assertEquals(portletWindowInstanceId, result);
    }

    @Test
    public void
            parsePortletWindowInstanceIdMethodShouldReturnNullWhenPortletWindowIdHasNoWindowInstanceIdAndHasDelegateWithWindowInstanceId() {
        // given
        final String portletWindowIdString = "90_dlg-5-ctf1-18.tw_18";
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletWindowInstanceId(portletWindowIdString);
        // then
        assertNull(result);
    }

    @Test
    public void
            parsePortletWindowInstanceIdMethodShouldReturnCorrectResultWhenPortletWindowIdHasWindowInstanceIdAndHasDelegateWithWindowInstanceId() {
        // given
        final String portletEntityIdString = "90_dlg-5-ctf1-18.1_18";
        final String portletWindowInstanceId = "tw";
        final String portletWindowIdString = portletEntityIdString + "." + portletWindowInstanceId;
        // when
        final String result =
                PortletWindowIdStringUtils.parsePortletWindowInstanceId(portletWindowIdString);
        // then
        assertEquals(portletWindowInstanceId, result);
    }
}
