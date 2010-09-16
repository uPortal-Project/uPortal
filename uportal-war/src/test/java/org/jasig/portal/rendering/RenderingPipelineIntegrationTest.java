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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.web.skin.ResourcesDao;
import org.jasig.portal.xml.stream.XMLStreamConstantsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private PipelineComponent<?, ?> component;
    
    //Mocked Beans
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private ResourcesDao resourcesDao;

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
    public void setResourcesDao(ResourcesDao resourcesDao) {
        this.resourcesDao = resourcesDao;
    }



    @Test
    public void testRenderingPipeline() throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = builder.newDocument();
        final DocumentFragment headFragment = doc.createDocumentFragment();
        
        expect(this.resourcesDao
                .getResourcesFragment("/media/skins/universality/uportal3/uportal3_aggr.skin.xml", "/media/skins/universality/uportal3/"))
                .andReturn(headFragment.getChildNodes());
        
        
        replay(this.resourcesDao, this.portalUrlProvider, this.portletWindowRegistry);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        
        final PipelineEventReader<?, ?> eventReader = this.component.getEventReader(request, response);
        
        for (final Object event : eventReader) {
            System.out.println(toString(event));
        }
        
        verify(this.resourcesDao, this.portalUrlProvider, this.portletWindowRegistry);
    }
    
    private String toString(Object event) throws Exception {
        if (event instanceof XMLEvent) {
            final XMLEvent xmlEvent = (XMLEvent)event;
            
            final StringBuilder eventString = new StringBuilder("[");
            eventString.append(XMLStreamConstantsUtils.getEventName(xmlEvent.getEventType()));
            
            final StringWriter writer = new StringWriter();
            xmlEvent.writeAsEncodedUnicode(writer);
            eventString.append(" ").append(writer.toString());
            
            eventString.append("]");
            return eventString.toString();
        }
        
        return String.valueOf(event);
    }
}
