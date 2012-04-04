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

package org.jasig.portal.portlet.rendering;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.portlet.Event;
import javax.xml.namespace.QName;

import net.sf.ehcache.Ehcache;

import org.apache.pluto.container.om.portlet.EventDefinition;
import org.apache.pluto.container.om.portlet.EventDefinitionReference;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class PortletEventCoordinatationServiceTest {
    @InjectMocks private PortletEventCoordinatationService portletEventCoordinatationService = new PortletEventCoordinatationService();
    @Mock private Ehcache supportedEventCache;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    
    
    @Test
    public void testSupportedEventResolution() throws Exception {
        final QName searchRequestName = new QName("https://source.jasig.org/schemas/uportal/search", "SearchRequest");
        final QName searchResultsName = new QName("https://source.jasig.org/schemas/uportal/search", "SearchResults");
        
        //org.jasig.portal.search.SearchQuery
        
        final Event event = mock(Event.class);
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId(1);
        final PortletApplicationDefinition portletApplicationDefinition = mock(PortletApplicationDefinition.class);
        final PortletDefinition portletDefinition = mock(PortletDefinition.class);
        final EventDefinitionReference searchRequestEventDefinitionReference = mock(EventDefinitionReference.class);
        final EventDefinitionReference searchResultsEventDefinitionReference = mock(EventDefinitionReference.class);
        final EventDefinition searchRequestEventDefinition = mock(EventDefinition.class);
        final EventDefinition searchResultsEventDefinition = mock(EventDefinition.class);
        
        when(event.getQName()).thenReturn(searchRequestName);
        
        when(searchRequestEventDefinitionReference.getQualifiedName(anyString())).thenReturn(searchRequestName);
        when(searchRequestEventDefinitionReference.getQName()).thenReturn(searchRequestName);
        
        when(searchResultsEventDefinitionReference.getQualifiedName(anyString())).thenReturn(searchResultsName);
        when(searchResultsEventDefinitionReference.getQName()).thenReturn(searchResultsName);
        
        when(searchRequestEventDefinition.getQName()).thenReturn(searchRequestName);
        when(searchRequestEventDefinition.getQualifiedName(anyString())).thenReturn(searchRequestName);
        
        when(searchResultsEventDefinition.getQName()).thenReturn(searchResultsName);
        when(searchResultsEventDefinition.getQualifiedName(anyString())).thenReturn(searchResultsName);


        when(this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinitionId)).thenReturn(portletApplicationDefinition);
        when(this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId)).thenReturn(portletDefinition);
        
        final List<? extends EventDefinition> eventDefinitions = Arrays.asList(searchRequestEventDefinition, searchResultsEventDefinition);
        when(portletApplicationDefinition.getEventDefinitions()).thenReturn((List) eventDefinitions);
        
        final List<? extends EventDefinitionReference> supportedProcessingEvents = Collections.singletonList(searchRequestEventDefinitionReference);
        when(portletDefinition.getSupportedProcessingEvents()).thenReturn((List) supportedProcessingEvents);
        
        final boolean supportsEvent = portletEventCoordinatationService.supportsEvent(event, portletDefinitionId);
        assertTrue(supportsEvent);
    }
}
