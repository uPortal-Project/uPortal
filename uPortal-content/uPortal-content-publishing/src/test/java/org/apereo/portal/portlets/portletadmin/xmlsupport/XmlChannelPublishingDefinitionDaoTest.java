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
package org.apereo.portal.portlets.portletadmin.xmlsupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apereo.portal.portlet.dao.IPortletTypeDao;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.registry.IPortletTypeRegistry;
import org.apereo.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.apereo.portal.portletpublishing.xml.Step;
import org.apereo.portal.xml.PortletDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XmlChannelPublishingDefinitionDaoTest {

    @InjectMocks private XmlChannelPublishingDefinitionDao xmlChannelPublishingDefinitionDao;

    @Mock private IPortletTypeRegistry portletTypeRegistry;

    @Mock private IPortletTypeDao portletTypeDao;

    @Mock private IPortletType portletType;

    @Mock private Map<Integer, PortletPublishingDefinition> cpdCache = getCpdCache();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<IPortletType> portletTypes = new ArrayList();
        xmlChannelPublishingDefinitionDao.setCpdCache(getCpdCache());
        xmlChannelPublishingDefinitionDao.afterPropertiesSet();
        when(portletType.getId()).thenReturn(1010);
        when(portletType.getName()).thenReturn("Advanced CMS");
        when(portletType.getDescription()).thenReturn("Displays configured HTML content");
        when(portletType.getCpdUri())
                .thenReturn("/org/apereo/portal/portlets/CMS/AdvancedCMSPortlet.cpd.xml");
        when(portletTypeRegistry.getPortletType("CHN123")).thenReturn(portletType);
        portletTypes.add(portletType);
        when(portletTypeRegistry.getPortletTypes()).thenReturn(portletTypes);
        assertNotNull(xmlChannelPublishingDefinitionDao);
    }

    @Test
    public void testGetChannelPublishingDefinitions() {
        Map<IPortletType, PortletPublishingDefinition> results =
                xmlChannelPublishingDefinitionDao.getChannelPublishingDefinitions();
        assertEquals(results.size(), 1);
    }

    @Test
    public void testGetChannelPublishingDefinition() {
        PortletPublishingDefinition portletPublishingDefinition =
                xmlChannelPublishingDefinitionDao.getChannelPublishingDefinition(1010);
        assertEquals(
                portletPublishingDefinition.getPortletDescriptor().getPortletName(),
                "WebProxyPortlet");
        assertEquals(
                portletPublishingDefinition.getPortletDescriptor().getWebAppName(),
                "/WebProxyPortlet");
        assertEquals(portletPublishingDefinition.getSteps().size(), 1);
        List<Step> steps = portletPublishingDefinition.getSteps();
        Step step = steps.get(0);
        assertEquals(step.getName(), "General Configuration");
        assertEquals(
                step.getDescription(),
                "Use the configuration options after reviewing the portlet publishing information to configure the portlet.");
    }

    /** @return */
    public static Map<Integer, PortletPublishingDefinition> getCpdCache() {
        Map<Integer, PortletPublishingDefinition> cpdCache = new HashMap<>();
        PortletPublishingDefinition portletPublishingDefinition = new PortletPublishingDefinition();
        PortletDescriptor portletDescriptor = new PortletDescriptor();
        portletDescriptor.setWebAppName("/WebProxyPortlet");
        portletDescriptor.setPortletName("WebProxyPortlet");
        portletDescriptor.setIsFramework(false);
        portletPublishingDefinition.setPortletDescriptor(portletDescriptor);
        List<Step> steps = portletPublishingDefinition.getSteps();
        Step step = new Step();
        step.setName("General Configuration");
        step.setDescription(
                "Use the configuration options after reviewing the portlet publishing information to configure the portlet.");
        steps.add(step);
        cpdCache.put(1010, portletPublishingDefinition);
        return cpdCache;
    }
}
