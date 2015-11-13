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
package org.jasig.portal.events.tincan.converters;

import java.util.ArrayList;
import java.util.List;

import org.jasig.portal.events.MockPortletExecutionEvent;
import org.jasig.portal.events.PortletActionExecutionEvent;
import org.jasig.portal.events.PortletExecutionEvent;
import org.jasig.portal.events.tincan.converters.PortletExecutionEventConverter.FNameFilterType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


/**
 * @author Josh Helmer, jhelmer@unicon.net
 */
public class PortletExecutionEventConverterTest {
    @Test
    public void testSupportsClassFilter() throws Exception {
        assertTrue(setupSupportsTest("some-portlet", true, false, null));
    }


    @Test
    public void testSupportsClassFilterMiss() throws Exception {
        assertFalse(setupSupportsTest("some-portlet", false, false, null));
    }


    @Test
    public void testSupportsNullFNameWhitelist() {
        assertFalse(setupSupportsTest(null, true, false, FNameFilterType.Whitelist));
    }


    @Test
    public void testSupportsNullFNameBlacklist() {
        assertTrue(setupSupportsTest(null, true, false, FNameFilterType.Blacklist));
    }


    @Test
    public void testSupportsFNameFilterWhitelist() throws Exception {
        assertTrue(setupSupportsTest("some-portlet", true, true, FNameFilterType.Whitelist));
    }


    @Test
    public void testSupportsFNameFilterWhitelistMiss() throws Exception {
        assertFalse(setupSupportsTest("some-portlet", true, false, FNameFilterType.Whitelist));
    }


    @Test
    public void testSupportsFNameFilterBlacklist() {
        assertFalse(setupSupportsTest("some-portlet", true, true, FNameFilterType.Blacklist));
    }


    @Test
    public void testSupportsFNameFilterBlacklistMiss() {
        assertTrue(setupSupportsTest("some-portlet", true, false, FNameFilterType.Blacklist));
    }


    private boolean setupSupportsTest(String fname, boolean classMatch, boolean fnameMatch, FNameFilterType filterType) {
        PortletExecutionEvent event = spy(new MockPortletExecutionEvent());
        when(event.getFname()).thenReturn(fname);

        PortletExecutionEventConverter converter = new PortletExecutionEventConverter();
        List<Class<? extends PortletExecutionEvent>> classes = new ArrayList<Class<? extends PortletExecutionEvent>>();
        classes.add(PortletActionExecutionEvent.class);
        if (classMatch) {
            classes.add(MockPortletExecutionEvent.class);
        }

        converter.setSupportedEventTypes(classes);

        if (filterType != null) {
            List<String> fnames = new ArrayList<String>(2);
            fnames.add("_mock-fname");
            if (fnameMatch && fname != null) {
                fnames.add(fname);
            }

            converter.setFilterFNames(fnames);
            converter.setFnameFilterType(filterType);
        }

        return converter.supports(event);
    }
}
