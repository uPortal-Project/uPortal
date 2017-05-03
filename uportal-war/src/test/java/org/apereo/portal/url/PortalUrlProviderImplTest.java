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
package org.apereo.portal.url;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.apereo.portal.mock.portlet.om.MockPortletWindowId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 */
public class PortalUrlProviderImplTest {
    private MockHttpServletRequest request;

    @InjectMocks private PortalUrlProviderImpl portalUrlProvider = new PortalUrlProviderImpl();
    @Mock private IUrlNodeSyntaxHelper urlProviderLayoutHelper;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IUserInstance userInstance;
    @Mock private IUserPreferencesManager preferencesManager;
    @Mock private IUserLayoutManager userLayoutManager;
    @Mock private IUserLayoutChannelDescription node;
    @Mock private IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry;
    @Mock private IUrlNodeSyntaxHelper urlNodeSyntaxHelper;
    @Mock private IUrlSyntaxProvider urlSyntaxProvider;
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private IPortletWindow portletWindow;
    @Mock private IPortletEntity portletEntity;

    private final String folderNodeId = "n1";
    private final String portletDefinitionIdStr = "pd1";
    private final String portletDefinitionFname = "fname1";
    private final MockPortletWindowId portletWindowId = new MockPortletWindowId("w1");

    @Before
    public void setup() {
        request = new MockHttpServletRequest();

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetDefaultUrlNoTargets() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request))
                .thenReturn(urlNodeSyntaxHelper);

        //Run the test
        final IPortalUrlBuilder urlBuilder = portalUrlProvider.getDefaultUrl(request);

        assertNotNull(urlBuilder);

        assertNull(urlBuilder.getTargetFolderId());
        assertNull(urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test
    public void testGetDefaultUrl() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.FOLDER);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request))
                .thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);

        //Run the test
        final IPortalUrlBuilder urlBuilder = portalUrlProvider.getDefaultUrl(request);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(null, urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDefaultUrlMissingLayoutNode() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(null);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request))
                .thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);

        //Run the test
        portalUrlProvider.getDefaultUrl(request);
    }

    @Test
    public void testGetPortalUrlBuilderByLayoutNode() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.FOLDER);

        //Run the test
        final IPortalUrlBuilder urlBuilder =
                portalUrlProvider.getPortalUrlBuilderByLayoutNode(
                        request, folderNodeId, UrlType.RENDER);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(null, urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPortalUrlBuilderByMissingPortletLayoutNode() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);

        //Run the test
        final IPortalUrlBuilder urlBuilder =
                portalUrlProvider.getPortalUrlBuilderByLayoutNode(
                        request, folderNodeId, UrlType.RENDER);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(
                new MockPortletWindowId(portletDefinitionIdStr),
                urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test
    public void testGetPortalUrlBuilderByPortletLayoutNode() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                        request, folderNodeId))
                .thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);

        //Run the test
        final IPortalUrlBuilder urlBuilder =
                portalUrlProvider.getPortalUrlBuilderByLayoutNode(
                        request, folderNodeId, UrlType.RENDER);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(portletWindowId, urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test
    public void testGetPortalUrlBuilderByPortletWindowId() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletEntity.getLayoutNodeId()).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(portletWindowRegistry.getOrCreateDefaultPortletWindowByLayoutNodeId(
                        request, folderNodeId))
                .thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId))
                .thenReturn(portletWindow);
        when(node.getId()).thenReturn(folderNodeId);

        //Run the test
        final IPortalUrlBuilder urlBuilder =
                portalUrlProvider.getPortalUrlBuilderByPortletWindow(
                        request, portletWindowId, UrlType.RENDER);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(portletWindowId, urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }

    @Test
    public void testGetPortalUrlBuilderByPortletFname() {
        //Setup mock objects
        when(urlProviderLayoutHelper.getDefaultLayoutNodeId(request)).thenReturn(folderNodeId);
        when(portletWindow.getPortletEntity()).thenReturn(portletEntity);
        when(portletEntity.getLayoutNodeId()).thenReturn(folderNodeId);
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(this.portletWindowRegistry.getOrCreateDefaultPortletWindowByFname(
                        request, portletDefinitionFname))
                .thenReturn(portletWindow);
        when(portletWindow.getPortletWindowId()).thenReturn(portletWindowId);
        when(userLayoutManager.getNode(folderNodeId)).thenReturn(node);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId))
                .thenReturn(portletWindow);
        when(node.getId()).thenReturn(folderNodeId);

        //Run the test
        final IPortalUrlBuilder urlBuilder =
                portalUrlProvider.getPortalUrlBuilderByPortletFName(
                        request, portletDefinitionFname, UrlType.RENDER);

        assertNotNull(urlBuilder);

        assertEquals(folderNodeId, urlBuilder.getTargetFolderId());
        assertEquals(portletWindowId, urlBuilder.getTargetPortletWindowId());
        assertEquals(UrlType.RENDER, urlBuilder.getUrlType());
    }
}
