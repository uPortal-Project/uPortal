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

import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
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
    @Mock private IPortletEntity portletEntity1;
    @Mock private IPortletEntity portletEntity2;
    @Mock private IPortletWindow portletWindow1;
    @Mock private IPortletWindow portletWindow2;

    @Test
    public void getCleanedContextPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        String path = urlSyntaxProvider.getCleanedContextPath(request);
        assertEquals("uPortal", path);
        
        request.setContextPath("");
        
        path = urlSyntaxProvider.getCleanedContextPath(request);
        assertEquals("", path);
    }
    
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
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow1);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.RENDER);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/f/n2/normal/render.uP?pCt=fname.s3", url);
    }
  
    @Test
    public void testSingleFolderPortletFnameSubscribeIdDetachedRenderUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow1);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletWindow1.getWindowState()).thenReturn(IPortletRenderer.DETACHED);
        when(portletWindow1.getPortletMode()).thenReturn(PortletMode.VIEW);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        portletUrlBuilder.setPortletMode(PortletMode.EDIT);
        
        final String url = portalUrlBuilder.getUrlString();
        
        assertEquals("/uPortal/f/n2/p/fname.s3/detached/render.uP?pCm=edit", url);
    }

    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedActionUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId = "s3";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow1);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId);
        
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
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId)).thenReturn(portletWindow1);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getPortletUrlBuilder(portletWindowId);
        portletUrlBuilder.setWindowState(WindowState.MINIMIZED);
        portletUrlBuilder.setParameter("action", "dashboard");
        
        final String url = portalUrlBuilder.getUrlString();
                     
        assertEquals("/uPortal/f/n2/normal/render.uP?pCt=fname.s3&pCs=minimized&pP_action=dashboard", url);
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

        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId1)).thenReturn(fname + "." + subscribeId);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId1)).thenReturn(portletWindow1);
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId2)).thenReturn(portletWindow2);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId1, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder1 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId1);
        portletUrlBuilder1.setWindowState(WindowState.MINIMIZED);
        portletUrlBuilder1.setParameter("action", "dashboard");
        final IPortletUrlBuilder portletUrlBuilder2 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId2);
        portletUrlBuilder2.setParameter("a", "b");
        portletUrlBuilder2.setParameter("b", "c");
        portletUrlBuilder2.setPortletMode(PortletMode.HELP);
        
        
        final String url = portalUrlBuilder.getUrlString();
                     
        assertEquals("/uPortal/f/n2/normal/render.uP?pCt=fname.s3&pCs=minimized&pP_action=dashboard&pCa=pw2&pCm_pw2=help&pP_pw2_a=b&pP_pw2_b=c", url);
    }
    
    @Test
    public void testSingleFolderMultiplePortletFnameSubscribeIdMinimizedRenderUrlParsing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        request.setRequestURI("/f/n2/normal/render.uP");
        request.setQueryString("?pCt=fname.s3&pCs=minimized&pP_action=dashboard&pCa=pw2&pCm_pw2=help&pP_pw2_a=b&pP_pw2_b=c");
        request.addParameter("pCt", "fname.s3");
        request.addParameter("pCs", "minimized");
        request.addParameter("pP_action", "dashboard");
        request.addParameter("pCa", "pw2");
        request.addParameter("pCm_pw2", "help");
        request.addParameter("pP_pw2_a", "b");
        request.addParameter("pP_pw2_b", "c");
        request.addParameter("postedParameter", "foobar");
        
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("s3");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("pw2");
        
        when(this.portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(this.urlNodeSyntaxHelper.getLayoutNodeForFolderNames(request, Arrays.asList("n2"))).thenReturn("n2");
        when(this.urlNodeSyntaxHelper.getPortletForFolderName(request, "n2", "fname.s3")).thenReturn(portletWindowId);
        when(this.portletWindowRegistry.getPortletWindowId(request, "pw2")).thenReturn(portletWindowId2);
        
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
        assertEquals(ImmutableMap.of("action", Arrays.asList("dashboard"), "postedParameter", Arrays.asList("foobar")), portletRequestInfo.getPortletParameters());
        assertEquals(WindowState.MINIMIZED, portletRequestInfo.getWindowState());
        assertNull(portletRequestInfo.getPortletMode());
        
        final IPortletRequestInfo portletRequestInfo2 = portletRequestInfoMap.get(portletWindowId2);
        assertNotNull(portletRequestInfo2);
        assertEquals(portletWindowId2, portletRequestInfo2.getPortletWindowId());
        assertEquals(ImmutableMap.of("a", Arrays.asList("b"), "b", Arrays.asList("c")), portletRequestInfo2.getPortletParameters());
        assertNull(portletRequestInfo2.getWindowState());
        assertEquals(PortletMode.HELP, portletRequestInfo2.getPortletMode());
    }

    @Test
    public void testSingleFolderPortletDelegationFnameSubscribeIdMinimizedRenderUrlGeneration() throws Exception {
        final String layoutNodeId = "n2";
        final String subscribeId1 = "s3";
        final String subscribeId2 = "dlg-71-44";
        final String fname = "fname";
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        
        final MockPortletWindowId portletWindowId1 = new MockPortletWindowId("pw1");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("pw2");

        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(urlNodeSyntaxHelper.getFolderNamesForLayoutNode(request, subscribeId1)).thenReturn(Arrays.asList(layoutNodeId));
        when(urlNodeSyntaxHelper.getFolderNameForPortlet(request, portletWindowId1)).thenReturn(fname + "." + subscribeId1);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId1)).thenReturn(portletWindow1);
        when(portletWindow1.getPortletEntity()).thenReturn(portletEntity1);
        when(portletEntity1.getLayoutNodeId()).thenReturn(subscribeId1);
        
        when(portletWindowRegistry.getPortletWindow(request, portletWindowId2)).thenReturn(portletWindow2);
        when(portletWindow2.getPortletEntity()).thenReturn(portletEntity2);
        when(portletWindow2.getDelegationParentId()).thenReturn(portletWindowId1);
        when(portletEntity2.getLayoutNodeId()).thenReturn(subscribeId2);
        
        final PortalUrlBuilder portalUrlBuilder = new PortalUrlBuilder(urlSyntaxProvider, request, layoutNodeId, portletWindowId1, UrlType.RENDER);
        final IPortletUrlBuilder portletUrlBuilder1 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId1);
        portletUrlBuilder1.setWindowState(WindowState.MINIMIZED);
        portletUrlBuilder1.setParameter("action", "dashboard");
        final IPortletUrlBuilder portletUrlBuilder2 = portalUrlBuilder.getPortletUrlBuilder(portletWindowId2);
        portletUrlBuilder2.setParameter("a", "b");
        portletUrlBuilder2.setParameter("b", "c");
        portletUrlBuilder2.setPortletMode(PortletMode.HELP);
        
        
        final String url = portalUrlBuilder.getUrlString();
                     
        assertEquals("/uPortal/f/n2/normal/render.uP?pCt=fname.s3&pCs=minimized&pP_action=dashboard&pCa=pw2&pCd_pw2=pw1&pCm_pw2=help&pP_pw2_a=b&pP_pw2_b=c", url);
    }
    
    @Test
    public void testSingleFolderPortletDelegationFnameSubscribeIdMinimizedRenderUrlParsing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        request.setRequestURI("/f/n2/normal/render.uP");
        request.setQueryString("?pCt=fname.s3&pCs=minimized&pP_action=dashboard&pCa=pw2&pCd_pw2=pw1&pCm_pw2=help&pP_pw2_a=b&pP_pw2_b=c");
        request.addParameter("pCt", "fname.s3");
        request.addParameter("pCs", "minimized");
        request.addParameter("pP_action", "dashboard");
        request.addParameter("pCa", "pw2");
        request.addParameter("pCd_pw2", "pw1");
        request.addParameter("pCm_pw2", "help");
        request.addParameter("pP_pw2_a", "b");
        request.addParameter("pP_pw2_b", "c");
        request.addParameter("postedParameter", "foobar");
        
        final MockPortletWindowId portletWindowId1 = new MockPortletWindowId("pw1");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("pw2");
        
        when(this.portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(this.urlNodeSyntaxHelper.getLayoutNodeForFolderNames(request, Arrays.asList("n2"))).thenReturn("n2");
        when(this.urlNodeSyntaxHelper.getPortletForFolderName(request, "n2", "fname.s3")).thenReturn(portletWindowId1);
        when(this.portletWindowRegistry.getPortletWindowId(request, "pw2")).thenReturn(portletWindowId2);
        when(this.portletWindowRegistry.getPortletWindowId(request, "pw1")).thenReturn(portletWindowId1);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        assertNotNull(portalRequestInfo);
        assertEquals("n2", portalRequestInfo.getTargetedLayoutNodeId());
        assertEquals(portletWindowId1, portalRequestInfo.getTargetedPortletWindowId());
        assertEquals(UrlState.NORMAL, portalRequestInfo.getUrlState());
        assertEquals(UrlType.RENDER, portalRequestInfo.getUrlType());
        
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        assertNotNull(portletRequestInfoMap);
        assertEquals(2, portletRequestInfoMap.size());
        
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId1);
        assertNotNull(portletRequestInfo);
        assertEquals(portletWindowId1, portletRequestInfo.getPortletWindowId());
        assertEquals(ImmutableMap.of("action", Arrays.asList("dashboard")), portletRequestInfo.getPortletParameters());
        assertEquals(WindowState.MINIMIZED, portletRequestInfo.getWindowState());
        assertNull(portletRequestInfo.getPortletMode());
        assertNull(portletRequestInfo.getDelegateParentWindowId());
        
        final IPortletRequestInfo portletRequestInfo2 = portletRequestInfoMap.get(portletWindowId2);
        assertNotNull(portletRequestInfo2);
        assertEquals(portletWindowId2, portletRequestInfo2.getPortletWindowId());
        assertEquals(ImmutableMap.of("a", Arrays.asList("b"), "b", Arrays.asList("c"), "postedParameter", Arrays.asList("foobar")), portletRequestInfo2.getPortletParameters());
        assertNull(portletRequestInfo2.getWindowState());
        assertEquals(PortletMode.HELP, portletRequestInfo2.getPortletMode());
        assertEquals(portletWindowId1, portletRequestInfo2.getDelegateParentWindowId());
    }
    
    @Test
    public void testSingleFolderPortletDelegationFnameSubscribeIdMinimizedRenderUrlParsingTwo() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        request.setRequestURI("/p/portlet-admin.ctf3/max/action.uP");
        request.setQueryString("?pCa=71_dlg-44-ctf3-8_8&pCd_71_dlg-44-ctf3-8_8=44_ctf3_8&pP_71_dlg-44-ctf3-8_8_action=updateKey&pP_execution=e4s4&pP__eventId=configModeAction");
        request.addParameter("pCa", "71_dlg-44-ctf3-8_8");
        request.addParameter("pCd_71_dlg-44-ctf3-8_8", "44_ctf3_8");
        request.addParameter("pP_71_dlg-44-ctf3-8_8_action", "updateKey");
        request.addParameter("pP_execution", "e4s4");
        request.addParameter("pP__eventId", "configModeAction");
        request.addParameter("googleApiKey", "12345");
        
        final MockPortletWindowId portletWindowId1 = new MockPortletWindowId("44_ctf3_8");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("71_dlg-44-ctf3-8_8");
        
        when(this.portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(this.urlNodeSyntaxHelper.getPortletForFolderName(request, null, "portlet-admin.ctf3")).thenReturn(portletWindowId1);
        when(this.portletWindowRegistry.getPortletWindowId(request, "71_dlg-44-ctf3-8_8")).thenReturn(portletWindowId2);
        when(this.portletWindowRegistry.getPortletWindowId(request, "44_ctf3_8")).thenReturn(portletWindowId1);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        assertNotNull(portalRequestInfo);
        assertNull(portalRequestInfo.getTargetedLayoutNodeId());
        assertEquals(portletWindowId1, portalRequestInfo.getTargetedPortletWindowId());
        assertEquals(UrlState.MAX, portalRequestInfo.getUrlState());
        assertEquals(UrlType.ACTION, portalRequestInfo.getUrlType());
        
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        assertNotNull(portletRequestInfoMap);
        assertEquals(2, portletRequestInfoMap.size());
        
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId1);
        assertNotNull(portletRequestInfo);
        assertEquals(portletWindowId1, portletRequestInfo.getPortletWindowId());
        assertEquals(ImmutableMap.of("execution", Arrays.asList("e4s4"), "_eventId", Arrays.asList("configModeAction")), portletRequestInfo.getPortletParameters());
        assertEquals(WindowState.MAXIMIZED, portletRequestInfo.getWindowState());
        assertNull(portletRequestInfo.getPortletMode());
        assertNull(portletRequestInfo.getDelegateParentWindowId());
        
        final IPortletRequestInfo portletRequestInfo2 = portletRequestInfoMap.get(portletWindowId2);
        assertNotNull(portletRequestInfo2);
        assertEquals(portletWindowId2, portletRequestInfo2.getPortletWindowId());
        assertEquals(ImmutableMap.of("action", Arrays.asList("updateKey"), "googleApiKey", Arrays.asList("12345")), portletRequestInfo2.getPortletParameters());
        assertNull(portletRequestInfo2.getWindowState());
        assertNull(portletRequestInfo2.getPortletMode());
        assertEquals(portletWindowId1, portletRequestInfo2.getDelegateParentWindowId());
    }
    

    
    @Test
    public void testSingleFolderPortletDelegationFnameSubscribeIdPostUrlParsing() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/uPortal");
        request.setRequestURI("/p/portlet-admin.ctf3/max/resource.uP");
        request.setQueryString("??pCa=30_dlg-16-ctf3-5_5&pCd_30_dlg-16-ctf3-5_5=16_ctf3_5&pCr_30_dlg-16-ctf3-5_5=preview&pP_execution=e2s3&pP__eventId=configModeAction");
        request.addParameter("pCa", "30_dlg-16-ctf3-5_5");
        request.addParameter("pCd_30_dlg-16-ctf3-5_5", "16_ctf3_5");
        request.addParameter("pCr_30_dlg-16-ctf3-5_5", "preview");
        request.addParameter("pP_execution", "e2s3");
        request.addParameter("pP__eventId", "configModeAction");
        request.addParameter("content", "<div>some content</div>"); //not on URL, was posted
        
        final MockPortletWindowId portletWindowId1 = new MockPortletWindowId("16_ctf3_5");
        final MockPortletWindowId portletWindowId2 = new MockPortletWindowId("30_dlg-16-ctf3-5_5");
        
        when(this.portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(urlNodeSyntaxHelperRegistry.getCurrentUrlNodeSyntaxHelper(request)).thenReturn(urlNodeSyntaxHelper);
        when(this.urlNodeSyntaxHelper.getPortletForFolderName(request, null, "portlet-admin.ctf3")).thenReturn(portletWindowId1);
        when(this.portletWindowRegistry.getPortletWindowId(request, "30_dlg-16-ctf3-5_5")).thenReturn(portletWindowId2);
        when(this.portletWindowRegistry.getPortletWindowId(request, "16_ctf3_5")).thenReturn(portletWindowId1);
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        
        assertNotNull(portalRequestInfo);
        assertNull(portalRequestInfo.getTargetedLayoutNodeId());
        assertEquals(portletWindowId1, portalRequestInfo.getTargetedPortletWindowId());
        assertEquals(UrlState.MAX, portalRequestInfo.getUrlState());
        assertEquals(UrlType.RESOURCE, portalRequestInfo.getUrlType());
        
        final Map<IPortletWindowId, ? extends IPortletRequestInfo> portletRequestInfoMap = portalRequestInfo.getPortletRequestInfoMap();
        assertNotNull(portletRequestInfoMap);
        assertEquals(2, portletRequestInfoMap.size());
        
        final IPortletRequestInfo portletRequestInfo = portletRequestInfoMap.get(portletWindowId1);
        assertNotNull(portletRequestInfo);
        assertEquals(portletWindowId1, portletRequestInfo.getPortletWindowId());
        assertEquals(ImmutableMap.of("execution", Arrays.asList("e2s3"), "_eventId", Arrays.asList("configModeAction")), portletRequestInfo.getPortletParameters());
        assertNull(portletRequestInfo.getDelegateParentWindowId());
        
        final IPortletRequestInfo portletRequestInfo2 = portletRequestInfoMap.get(portletWindowId2);
        assertNotNull(portletRequestInfo2);
        assertEquals(portletWindowId2, portletRequestInfo2.getPortletWindowId());
        assertEquals(ImmutableMap.of("content", Arrays.asList("<div>some content</div>")), portletRequestInfo2.getPortletParameters());
        assertEquals(portletWindowId1, portletRequestInfo2.getDelegateParentWindowId());
    }

    @Test
    public void testParsePortletWindowIdSuffix() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final Set<String> ids = ImmutableSet.of("pw2");
        final MockPortletWindowId expectedPortletWindowId = new MockPortletWindowId("pw2");
        
        when(this.portletWindowRegistry.getPortletWindowId(request, "pw2")).thenReturn(expectedPortletWindowId);
        
        IPortletWindowId portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(request, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE + UrlSyntaxProviderImpl.SEPARATOR + "pw2");
        
        assertEquals(expectedPortletWindowId, portletWindowId);
        
        portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(request, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE + UrlSyntaxProviderImpl.SEPARATOR + UrlSyntaxProviderImpl.SEPARATOR + "pw2");
        
        assertNull(portletWindowId);
        
        portletWindowId = this.urlSyntaxProvider.parsePortletWindowIdSuffix(request, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE, ids, 
                UrlSyntaxProviderImpl.PARAM_WINDOW_STATE);
        
        assertNull(portletWindowId);
    }
    
    @Test
    public void testParsePortletParameterName() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        final Set<String> ids = ImmutableSet.of("pw2");
        final MockPortletWindowId expectedPortletWindowId = new MockPortletWindowId("pw2");
        
        when(this.portletWindowRegistry.getPortletWindowId(request, "pw2")).thenReturn(expectedPortletWindowId);
        
        Tuple<String, IPortletWindowId> portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(request, UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + "pw2" + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("foo", portletParameterInfo.first);
        assertEquals(expectedPortletWindowId, portletParameterInfo.second);
        

        
        portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(request, UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("_foo", portletParameterInfo.first);
        assertNull(portletParameterInfo.second);
        

        
        portletParameterInfo = this.urlSyntaxProvider.parsePortletParameterName(request, UrlSyntaxProviderImpl.PORTLET_PARAM_PREFIX + "pw1" + UrlSyntaxProviderImpl.SEPARATOR + "foo" , ids);
        
        assertNotNull(portletParameterInfo);
        assertEquals("pw1_foo", portletParameterInfo.first);
        assertNull(portletParameterInfo.second);
    }
}
