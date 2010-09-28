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

package org.jasig.portal.rendering;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.jasig.portal.xml.stream.XMLStreamConstantsUtils;
import org.jasig.resource.aggr.util.ResourcesElementsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "renderingPipelineTestContext.xml")
public class RenderingPipelineIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private PipelineComponent<?, ?> component;
    
    //Mocked Beans
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private ResourcesElementsProvider resourcesElementsProvider;
    private IUserInstanceManager userInstanceManager;

    @Autowired
    public void setComponent(
            @Qualifier("renderingPipeline") PipelineComponent<?, ?> component) {
        this.component = component;
    }
    
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Test
    public void testRenderingPipeline() throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = builder.newDocument();
        final DocumentFragment headFragment = doc.createDocumentFragment();
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        
        expect(this.resourcesElementsProvider
                .getResourcesXmlFragment(isA(HttpServletRequest.class), eq("/media/skins/universality/uportal3/skin.xml")))
                .andReturn(headFragment.getChildNodes()).anyTimes();
        expect(this.resourcesElementsProvider
                .getResourcesParameter(isA(HttpServletRequest.class), eq("/media/skins/universality/uportal3/skin.xml"), eq("fss-theme")))
                .andReturn(".fl-mist").anyTimes();
        
        
        replay(this.resourcesElementsProvider, this.portalUrlProvider, this.portletWindowRegistry, this.userInstanceManager);
        
        final PipelineEventReader<?, ?> eventReader = this.component.getEventReader(request, response);
        
        for (final Object event : eventReader) {
            logger.debug(toString(event));
        }
        
        verify(this.resourcesElementsProvider, this.portalUrlProvider, this.portletWindowRegistry, this.userInstanceManager);
    }
    
    private String toString(Object event) throws Exception {
        if (event instanceof XMLEvent) {
            final XMLEvent xmlEvent = (XMLEvent)event;
            
            final StringBuilder eventBuilder = new StringBuilder("[");
            eventBuilder.append(XMLStreamConstantsUtils.getEventName(xmlEvent.getEventType()));
            
            final String eventString = XmlUtilitiesImpl.toString(xmlEvent);
            eventBuilder.append(" ").append(eventString);
            
            eventBuilder.append("]");
            return eventBuilder.toString();
        }
        
        return String.valueOf(event);
    }
}
