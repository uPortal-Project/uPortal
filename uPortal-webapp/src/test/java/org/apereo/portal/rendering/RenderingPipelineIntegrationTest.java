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
package org.apereo.portal.rendering;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import org.apereo.portal.mock.portlet.om.MockPortletWindowId;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.security.xslt.IXalanMessageHelper;
import org.apereo.portal.url.IPortalUrlBuilder;
import org.apereo.portal.url.IPortalUrlProvider;
import org.apereo.portal.url.IPortletUrlBuilder;
import org.apereo.portal.url.UrlType;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.xml.XmlUtilitiesImpl;
import org.apereo.portal.xml.stream.XMLStreamConstantsUtils;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
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

/** */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "renderingPipelineTestContext.xml")
public class RenderingPipelineIntegrationTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private PipelineComponent<?, ?> component;

    // Mocked Beans
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private ResourcesElementsProvider resourcesElementsProvider;
    private IUserInstanceManager userInstanceManager;
    private IXalanMessageHelper xalanMessageHelper;

    @Autowired
    public void setComponent(@Qualifier("renderingPipeline") PipelineComponent<?, ?> component) {
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
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setXalanMessageHelper(IXalanMessageHelper xalanMessageHelper) {
        this.xalanMessageHelper = xalanMessageHelper;
    }

    @Test
    public void testRenderingPipeline() throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        final Document doc = builder.newDocument();
        final DocumentFragment headFragment = doc.createDocumentFragment();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final IPortalUrlBuilder portalUrlBuilder = mock(IPortalUrlBuilder.class);
        final IPortletUrlBuilder portletUrlBuilder = mock(IPortletUrlBuilder.class);

        when(portalUrlBuilder.getUrlString()).thenReturn("URL_PLACEHOLDER");
        when(portletUrlBuilder.getPortalUrlBuilder()).thenReturn(portalUrlBuilder);
        when(portalUrlBuilder.getTargetedPortletUrlBuilder()).thenReturn(portletUrlBuilder);
        when(portalUrlBuilder.getPortletUrlBuilder(any(IPortletWindowId.class)))
                .thenReturn(portletUrlBuilder);

        when(this.resourcesElementsProvider.getResourcesXmlFragment(
                        any(HttpServletRequest.class),
                        eq("/media/skins/respondr/defaultSkin/skin.xml")))
                .thenReturn(headFragment.getChildNodes());
        when(this.portalUrlProvider.getDefaultUrl(any(HttpServletRequest.class)))
                .thenReturn(portalUrlBuilder);
        when(this.portalUrlProvider.getPortalUrlBuilderByLayoutNode(
                        any(HttpServletRequest.class), any(String.class), any(UrlType.class)))
                .thenReturn(portalUrlBuilder);
        when(this.portalUrlProvider.getPortalUrlBuilderByPortletFName(
                        any(HttpServletRequest.class), any(String.class), any(UrlType.class)))
                .thenReturn(portalUrlBuilder);

        final IPortletWindow portletWindow = mock(IPortletWindow.class);
        when(portletWindowRegistry.getPortletWindow(
                        any(HttpServletRequest.class), any(StartElement.class)))
                .thenReturn(new Tuple<IPortletWindow, StartElement>(portletWindow, null));
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                        any(HttpServletRequest.class), any(String.class)))
                .thenReturn(portletWindow);
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                        any(HttpServletRequest.class), anyString()))
                .thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(new MockPortletWindowId("1"));

        final PipelineEventReader<?, ?> eventReader =
                this.component.getEventReader(request, response);

        for (final Object event : eventReader) {
            logger.debug(toString(event));
        }

        final String mediaType = eventReader.getOutputProperty(OutputKeys.MEDIA_TYPE);
        assertEquals("text/html", mediaType);
    }

    private String toString(Object event) throws Exception {
        if (event instanceof XMLEvent) {
            final XMLEvent xmlEvent = (XMLEvent) event;

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
