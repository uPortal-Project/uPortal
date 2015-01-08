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
package org.jasig.portal.portlet.dao.jpa;

import org.jasig.portal.portlet.om.IPortletType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for PortletDefinitionImpl.
 */
public class PortletDefinitionImplTest {

    @Mock IPortletType mockType;

    @Before
    public void setUp() {
        initMocks(this);
    }

    /**
     * Test that when the relevant parameter is present with a valid value,
     * parses alternative maximized URL.
     */
    @Test
    public void testAlternativeMaximizedLinkParsing() {

        final PortletDefinitionImpl portletDefinition =
            new PortletDefinitionImpl(mockType, "fname", "name", "title", "applicationId",
                "portletName", false /* isFramework */);

        portletDefinition.addParameter("alternativeMaximizedLink", "http://www.google.com");

        assertEquals("http://www.google.com", portletDefinition.getAlternativeMaximizedLink());

        portletDefinition.removeParameter("alternativeMaximizedLink");

        assertNull(portletDefinition.getAlternativeMaximizedLink());

        portletDefinition.addParameter("alternativeMaximizedLink", "  \t  ");

        assertNull("Whitespace value should have been interpreted as no parameter value.",
            portletDefinition.getAlternativeMaximizedLink());
    }
}
