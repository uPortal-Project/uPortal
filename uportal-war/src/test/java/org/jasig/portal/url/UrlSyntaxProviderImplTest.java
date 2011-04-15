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

/**
 * 
 */
package org.jasig.portal.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.utils.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Test harness for {@link UrlSyntaxProviderImpl}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UrlSyntaxProviderImplTest {
    @InjectMocks private UrlSyntaxProviderImpl urlSyntaxProvider = new UrlSyntaxProviderImpl(); 
    @Mock private IPortalRequestUtils portalRequestUtils;
    @Mock private IUrlNodeSyntaxHelperRegistry urlNodeSyntaxHelperRegistry;
    @Mock private IUrlNodeSyntaxHelper urlNodeSyntaxHelper;
    @Mock private IPortletEntityRegistry portletEntityRegistry;
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private IPortletEntity portletEntity;
    @Mock private IPortletWindow portletWindow;

    
    @Test
    public void testNonTargetedGeneration() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, null)).thenReturn(Collections.EMPTY_LIST);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, null, null, UrlType.RENDER);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/normal/render.uP", url);
    }
    
    @Test
    public void testSingleFolderUrlWithStateTypeGeneration() throws Exception {
        final String layoutNodeId = "n2";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, layoutNodeId)).thenReturn(Arrays.asList(layoutNodeId));
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, null, UrlType.RENDER);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/f/n2/normal/render.uP", url);
    }
  
    @Test
    public void testSingleFolderPortletFnameSubscribeIdRenderUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("pe1");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletEntityRegistry.getPortletEntity(portletEntityId)).thenReturn(portletEntity);
        when(portletEntity.getChannelSubscribeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.RENDER);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/f/n2/normal/render.uP?plCt=fname.s3", url);
    }

    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedActionUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("pe1");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletEntityRegistry.getPortletEntity(portletEntityId)).thenReturn(portletEntity);
        when(portletEntity.getChannelSubscribeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.ACTION);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        portletUrlBuilder.setWindowState(WindowState.MAXIMIZED);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/f/n2/p/fname.s3/max/action.uP", url);
    }

    @Test
    public void testSingleFolderPortletFnameSubscribeIdMinimizedRenderUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("pe1");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletEntityRegistry.getPortletEntity(portletEntityId)).thenReturn(portletEntity);
        when(portletEntity.getChannelSubscribeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        portletUrlBuilder.setWindowState(WindowState.MINIMIZED);
        portletUrlBuilder.setParameter("action", "dashboard");
        
        final String url = portalUrlBuilder.getUrlString();
                     
        assertEquals("/uPortal/f/n2/normal/render.uP?plCt=fname.s3&plCs=minimized&plP_action=dashboard", url);
    }

    @Test
    public void testSingleFolderMultiplePortletFnameSubscribeIdMinimizedRenderUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId1 = new MockPortletWindowId("pw1");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("pw2");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("pe1");

        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId1)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId1)).thenReturn(portletWindow);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId2)).thenReturn(portletWindow);
        when(portletWindow.getPortletEntityId()).thenReturn(portletEntityId);
        when(portletEntityRegistry.getPortletEntity(portletEntityId)).thenReturn(portletEntity);
        when(portletEntity.getChannelSubscribeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId1, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder1 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId1);
        portletUrlBuilder1.setWindowState(WindowState.MINIMIZED);
        portletUrlBuilder1.setParameter("action", "dashboard");
        final IPortletUrlBuilder portletUrlBuilder2 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId2);
        portletUrlBuilder2.setParameter("a", "b");
        portletUrlBuilder2.setParameter("b", "c");
        portletUrlBuilder2.setPortletMode(PortletMode.HELP);
        
        
        final String url = portalUrlBuilder.getUrlString();
                     
        assertEquals("/uPortal/f/n2/normal/render.uP?plCt=fname.s3&plCs=minimized&plP_action=dashboard&plCa=pw2&plCm_pw2=help&plP_pw2_a=b&plP_pw2_b=c", url);
    }
    
    @Test
    public void testSingleFolderMultiplePortletFnameSubscribeIdMinimizedRenderUrlParsing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        request.setRequestURI("/f/n2/normal/render.uP");
        request.setQueryString("?plCt=fname.s3&plCs=minimized&plP_action=dashboard&plCa=pw2&plCm_pw2=help&plP_pw2_a=b&plP_pw2_b=c");
        request.addParameter("plCt", "fname.s3");
        request.addParameter("plCs", "minimized");
        request.addParameter("plP_action", "dashboard");
        request.addParameter("plCa", "pw2");
        request.addParameter("plCm_pw2", "help");
        request.addParameter("plP_pw2_a", "b");
        request.addParameter("plP_pw2_b", "c");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("s3");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("pw2");
        
        when(this.portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(this.urlNodeSyntaxHelper.getLayoutNodeForFolderNames(request, Arrays.asList("n2"))).thenReturn("n2");
        when(this.urlNodeSyntaxHelper.getPortletForFolderName(request, "fname.s3")).thenReturn(portletWindowId);
        when(this.portletWindowRegistry.getPortletWindowId("pw2")).thenReturn(portletWindowId2);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        assertNotNull(portalRequestInfo);
        assertEquals("n2", portalRequestInfo.getTargetedLayoutNodeId());
        assertEquals(portletWindowId, portalRequestInfo.getTargetedPortletWindowId());
        assertEquals(UrlState.NORMAL, portalRequestInfo.getUrlState());
        assertEquals(UrlType.RENDER, portalRequestInfo.getUrlType());
        
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        assertNotNull(portletRequestInfoMap);
        assertEquals(2, portletRequestInfoMap.size());
        
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId);
        assertNotNull(portletRequestInfo);
        assertEquals(portletWindowId, portletRequestInfo.getPortletWindowId());
        assertEquals(ImmutableMap.builder().put("action", Arrays.asList("dashboard")).build(), portletRequestInfo.getPortletParameters());
        assertEquals(WindowState.MINIMIZED, portletRequestInfo.getWindowState());
        assertNull(portletRequestInfo.getPortletMode());
        
        final IPortletRequestInfo portletRequestInfo2 = portletRequestInfoMap.get(portletWindowId2);
        assertNotNull(portletRequestInfo2);
        assertEquals(portletWindowId2, portletRequestInfo2.getPortletWindowId());
        assertEquals(ImmutableMap.builder().put("a", Arrays.asList("b")).put("b", Arrays.asList("c")).build(), portletRequestInfo2.getPortletParameters());
        assertNull(portletRequestInfo2.getWindowState());
        assertEquals(PortletMode.HELP, portletRequestInfo2.getPortletMode());
    }

    @Test
    public void testParsePortletWindowIdSuffix() {
        final Set<String> ids = ImmutableSet.of("pw2");
        final MockPortletWindowId expectedPortletWindowId = new MockPortletWindowId("pw2");
        
        when(this.portletWindowRegistry.getPortletWindowId("pw2")).thenReturn(expectedPortletWindowId);
        
        IPortletWindowId portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE + UrlSyntaxProviderImpl.SEPARATOR + "pw2");
        
        assertEquals(expectedPortletWindowId, portletWindowId);
        
        portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE + UrlSyntaxProviderImpl.SEPARATOR + UrlSyntaxProviderImpl.SEPARATOR + "pw2");
        
        assertNull(portletWindowId);
        
        portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE);
        
        assertNull(portletWindowId);
    }
    
    @Test
    public void testParsePortletParameterName() {
        final Set<String> ids = ImmutableSet.of("pw2");
        final MockPortletWindowId expectedPortletWindowId = new MockPortletWindowId("pw2");
        
        when(this.portletWindowRegistry.getPortletWindowId("pw2")).thenReturn(expectedPortletWindowId);
        
        Tuple<String, IPortletWindowId> portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + "pw2" + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("foo", portletParameterInfo.first);
        assertEquals(expectedPortletWindowId, portletParameterInfo.second);
        

        
        portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("_foo", portletParameterInfo.first);
        assertNull(portletParameterInfo.second);
        

        
        portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + "pw1" + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("pw1_foo", portletParameterInfo.first);
        assertNull(portletParameterInfo.second);
    }
}
