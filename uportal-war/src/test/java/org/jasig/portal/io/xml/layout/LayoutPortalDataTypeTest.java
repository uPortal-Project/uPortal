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

package org.jasig.portal.io.xml.layout;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import org.jasig.portal.io.xml.PortalDataKey;
import org.junit.Test;

/**
 * @author Drew Wills
 */
public class LayoutPortalDataTypeTest {
    
    private static final XMLInputFactory FAC = XMLInputFactory.newFactory();

    private static final String FRAGMENT_LAYOUT_NODE = "<layout xmlns:dlm=\"http://www.uportal.org/layout/dlm\" script=\"classpath://org/jasig/portal/io/import-layout_v3-2.crn\" username=\"guest-lo\"/>";
    private static final String REGULAR_LAYOUT_NODE = "<layout xmlns:dlm=\"http://www.uportal.org/layout/dlm\" script=\"classpath://org/jasig/portal/io/import-layout_v3-2.crn\" username=\"jdoe\"/>";
    
    private static final PortalDataKey FRAGMENT_LAYOUT_DATA_KEY = FragmentLayoutPortalDataType.IMPORT_32_DATA_KEY;
    private static final PortalDataKey REGULAR_LAYOUT_DATA_KEY = LayoutPortalDataType.IMPORT_32_DATA_KEY;
    
    @Test
    public void testPostProcessSinglePortalDataKey_detectFragmentLayout() throws Exception {

        LayoutPortalDataType lpdt = new LayoutPortalDataType();
        
        XMLEventReader fragmentLayoutReader = FAC.createXMLEventReader(new StringReader(FRAGMENT_LAYOUT_NODE));
        String[] fragmentSystemIds = new String[] {
                "foo.fragment-layout",
                "foo.fragment-layout.xml"
        };
        for (String sysId : fragmentSystemIds) {
            Set<PortalDataKey> keys = lpdt.postProcessPortalDataKey(sysId, REGULAR_LAYOUT_DATA_KEY, fragmentLayoutReader);
            assertEquals("postProcessPortalDataKey() returned the wrong number of results.  Expected 1, was " + keys.size(), keys.size(), 1);
            assertTrue("postProcessPortalDataKey() failed to detect a fragment layout for systemId=" + sysId, keys.contains(FRAGMENT_LAYOUT_DATA_KEY));
        }

        XMLEventReader regularLayoutReader = FAC.createXMLEventReader(new StringReader(REGULAR_LAYOUT_NODE));
        String[] regularSystemIds = new String[] {
                "foo.layout",
                "foo.layout.xml"
        };
        for (String sysId : regularSystemIds) {
            Set<PortalDataKey> keys = lpdt.postProcessPortalDataKey(sysId, REGULAR_LAYOUT_DATA_KEY, regularLayoutReader);
            assertEquals("postProcessPortalDataKey() returned the wrong number of results.  Expected 1, was " + keys.size(), keys.size(), 1);
            assertTrue("postProcessPortalDataKey() failed to detect a fragment layout for systemId=" + sysId, keys.contains(REGULAR_LAYOUT_DATA_KEY));
        }

    }
}
