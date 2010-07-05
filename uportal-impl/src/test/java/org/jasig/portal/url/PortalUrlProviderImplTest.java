/**
 * 
 */
package org.jasig.portal.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.WindowState;
import javax.xml.xpath.XPathExpression;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.mock.portlet.om.MockPortletEntityId;
import org.jasig.portal.mock.portlet.om.MockPortletWindowId;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test harness for {@link PortalUrlProviderImpl}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class PortalUrlProviderImplTest {
    private PortalUrlProviderImpl portalUrlProvider;
    private IUserInstanceManager userInstanceManager;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletWindowRegistry portletWindowRegistry;
    private IUserInstance userInstance;
    private IUserPreferencesManager userPreferencesManager;
    private IUserLayoutManager userLayoutManager;
    private IUserLayout userLayout;
    private IUserLayoutNodeDescription userLayoutNodeDescription;
    private IPerson person;
    private IPortalRequestUtils portalRequestUtils;
    private IChannelDefinition channelDefinition;
    private IPortletDefinition portletDefinition;
    private IPortletEntity portletEntity;
    private IPortletWindow portletWindow;
    
    private Object[] mockObjects;
    
    @Before
    public void setupPortletUrlProvider() {
        this.portalUrlProvider = new PortalUrlProviderImpl();

        this.userInstanceManager = createMock(IUserInstanceManager.class);
        this.portalUrlProvider.setUserInstanceManager(this.userInstanceManager);
        
        this.portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        this.portalUrlProvider.setPortletEntityRegistry(portletEntityRegistry);
        
        this.portletWindowRegistry = createMock(IPortletWindowRegistry.class);
        this.portalUrlProvider.setPortletWindowRegistry(this.portletWindowRegistry);
        
        this.portletDefinitionRegistry = createMock(IPortletDefinitionRegistry.class);
        this.portalUrlProvider.setPortletDefinitionRegistry(this.portletDefinitionRegistry);
        
        this.portalRequestUtils = createMock(IPortalRequestUtils.class);
        this.portalUrlProvider.setPortalRequestUtils(this.portalRequestUtils);
        
        this.userInstance = createMock(IUserInstance.class);
        this.userPreferencesManager = createMock(IUserPreferencesManager.class);
        this.userLayoutManager = createMock(IUserLayoutManager.class);
        this.userLayout = createMock(IUserLayout.class);
        this.userLayoutNodeDescription = createMock(IUserLayoutNodeDescription.class);
        this.person = createMock(IPerson.class);
        this.channelDefinition = createMock(IChannelDefinition.class);
        this.portletDefinition = createMock(IPortletDefinition.class);
        this.portletEntity = createMock(IPortletEntity.class);
        this.portletWindow = createMock(IPortletWindow.class);
        
        this.mockObjects = new Object[] {
                this.userInstanceManager, this.portletEntityRegistry, this.portletWindowRegistry, this.userInstance, 
                this.userPreferencesManager, this.userLayoutManager, this.person, this.portletEntity, this.portletWindow,
                this.userLayout, this.portalRequestUtils, this.portletDefinitionRegistry, this.channelDefinition,
                this.portletDefinition, this.userLayoutNodeDescription
        };
    }
    
    @After
    public void tearDownPortletUrlProvider() {
        this.portalUrlProvider = null;
        this.userInstanceManager = null;
        this.portletDefinitionRegistry = null;
        this.portletEntityRegistry = null;
        this.portletWindowRegistry = null;
        this.userInstance = null;
        this.userPreferencesManager = null;
        this.userLayoutManager = null;
        this.userLayout = null;
        this.userLayoutNodeDescription = null;
        this.person = null;
        this.portalRequestUtils = null;
        this.channelDefinition = null;
        this.portletDefinition = null;
        this.portletEntity = null;
        this.portletWindow = null;
        
        this.mockObjects = null;
    }
    
    private void replayAll(Object... objects) {
        replay(this.mockObjects);
        
        if (objects != null) {
            replay(objects);
        }
    }
    
    private void verifyAll(Object... objects) {
        verify(this.mockObjects);
        
        if (objects != null) {
            verify(objects);
        }
    }
    
    private void testFolderUrlHelper(String uri, IPortalRequestInfo expected) throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI(uri);
        
        this.replayAll();
        
        final IPortalRequestInfo requestInfo = this.portalUrlProvider.getPortalRequestInfo(mockRequest);
        
        this.verifyAll();

        assertEquals(expected.getTargetedLayoutNodeId(), requestInfo.getTargetedLayoutNodeId());
        assertEquals(expected.getLayoutParameters(), requestInfo.getLayoutParameters());
        assertEquals(expected.getPortalParameters(), requestInfo.getPortalParameters());
        assertEquals(expected.getUrlState(), requestInfo.getUrlState());
        assertEquals(expected.getUrlType(), requestInfo.getUrlType());
    }
    
    @Test
    public void testNoPathNoSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        
        this.testFolderUrlHelper("/uPortal", expected);
    }
    
    @Test
    public void testNoPathWithSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        
        this.testFolderUrlHelper("/uPortal/", expected);
    }
    
    @Test
    public void testNoPathWithState() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        
        this.testFolderUrlHelper("/uPortal/max", expected);
    }
    
    @Test
    public void testNoPathWithType() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        
        this.testFolderUrlHelper("/uPortal/action.uP", expected);
    }
    
    @Test
    public void testSingleFolderUrlNoSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        
        this.testFolderUrlHelper("/uPortal/f/folderName", expected);
    }
    
    @Test
    public void testSingleFolderUrlWithSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        
        this.testFolderUrlHelper("/uPortal/f/folderName/", expected);
    }
    
    @Test
    public void testSingleFolderUrlWithState() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "max";
        
        this.testFolderUrlHelper("/uPortal/f/folderName/max", expected);
    }
    
    @Test
    public void testSingleFolderUrlWithType() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        
        this.testFolderUrlHelper("/uPortal/f/folderName/action.uP", expected);
    }
    
    @Test
    public void testSingleFolderUrlWithStateType() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "n2";
        expected.urlState = UrlState.NORMAL;
        expected.urlType = UrlType.RENDER;
        
        this.testFolderUrlHelper("/uPortal/f/n2/normal/render.uP", expected);
    }
    
    @Test
    public void testSingleFolderUrlWithStateTypeGeneration() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal");
        
        final String nodeId = "n2";
        
        expect(this.userInstanceManager.getUserInstance(mockRequest)).andReturn(this.userInstance).times(2);
        expect(this.userInstance.getPreferencesManager()).andReturn(this.userPreferencesManager).times(2);
        expect(this.userPreferencesManager.getUserLayoutManager()).andReturn(this.userLayoutManager).times(2);
        expect(this.userLayoutManager.getNode(nodeId)).andReturn(this.userLayoutNodeDescription).times(2);
        expect(this.userLayoutNodeDescription.getId()).andReturn(nodeId).times(2);
        
        this.replayAll();
        
        final ILayoutPortalUrl folderUrl = this.portalUrlProvider.getFolderUrlByNodeId(mockRequest, nodeId);
        
        final String urlString = folderUrl.getUrlString();
        
        this.verifyAll();
        
        assertEquals("/uPortal/f/n2/normal/render.uP", urlString);
    }
    
    @Test
    public void testMultipleFolderUrlNoSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName2";
        
        this.testFolderUrlHelper("/uPortal/f/folderName/folderName2", expected);
    }
    
    @Test
    public void testMultipleFolderUrlWithSlash() throws Exception {
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName2";
        
        this.testFolderUrlHelper("/uPortal/f/folderName/folderName2/", expected);
    }
    
    private void testFolderPortletFnameUrlHelper(String uri, String subscribeId, IPortalRequestInfo expected) throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI(uri);
        
        final MockPortletEntityId portletEntityId = new MockPortletEntityId(subscribeId);
        final MockPortletWindowId portletWindowId = new MockPortletWindowId(portletEntityId.getStringId());

        expect(this.userInstanceManager.getUserInstance(mockRequest)).andReturn(this.userInstance);
        expect(this.userInstance.getPreferencesManager()).andReturn(this.userPreferencesManager);
        expect(this.userPreferencesManager.getUserLayoutManager()).andReturn(this.userLayoutManager);
        expect(this.userLayoutManager.getSubscribeId("portletName")).andReturn(subscribeId);
        expect(this.userInstance.getPerson()).andReturn(this.person);
        expect(this.person.getID()).andReturn(1337);
        expect(this.portletEntityRegistry.getPortletEntity(subscribeId, 1337)).andReturn(this.portletEntity);
        expect(this.portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        expect(this.portletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(this.portletWindow);
        expect(this.portletWindow.getPortletWindowId()).andReturn(portletWindowId);
        
        this.replayAll();
        
        final IPortalRequestInfo requestInfo = this.portalUrlProvider.getPortalRequestInfo(mockRequest);
        
        this.verifyAll();

        assertEquals(expected.getTargetedLayoutNodeId(), requestInfo.getTargetedLayoutNodeId());
        assertEquals(expected.getLayoutParameters(), requestInfo.getLayoutParameters());
        assertEquals(expected.getPortalParameters(), requestInfo.getPortalParameters());
        assertEquals(expected.getUrlState(), requestInfo.getUrlState());
        assertEquals(expected.getUrlType(), requestInfo.getUrlType());
        
        final IPortletRequestInfo expectedPortletRequestInfo = expected.getPortletRequestInfo();
        final IPortletRequestInfo portletRequestInfo = requestInfo.getPortletRequestInfo();
        assertRequestInfoEquals(expectedPortletRequestInfo, portletRequestInfo);
    }
    
    @Test
    public void testSingleFolderPortletFnameUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        
        testFolderPortletFnameUrlHelper("/uPortal/f/folderName/p/portletName", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        
        testFolderPortletFnameUrlHelper("/uPortal/f/folderName/p/portletName/", "n42", expected);
    }
    
    private void testFolderPortletFnameSubscribeIdUrlHelper(String uri, String subscribeId, IPortalRequestInfo expected) throws Exception {
        this.testFolderPortletFnameSubscribeIdUrlHelper(uri, null, subscribeId, expected);
    }
    private void testFolderPortletFnameSubscribeIdUrlHelper(String uri, Map<String, String[]> params, String subscribeId, IPortalRequestInfo expected) throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI(uri);
        mockRequest.setParameters(params == null ? Collections.EMPTY_MAP : params);
        
        final MockPortletEntityId portletEntityId = new MockPortletEntityId(subscribeId);
        final MockPortletWindowId portletWindowId = new MockPortletWindowId(portletEntityId.getStringId());

        expect(this.userInstanceManager.getUserInstance(mockRequest)).andReturn(this.userInstance);
        expect(this.userInstance.getPerson()).andReturn(this.person);
        expect(this.person.getID()).andReturn(1337);
        expect(this.portletEntityRegistry.getPortletEntity(expected.getPortletRequestInfo().getTargetWindowId().getStringId(), 1337)).andReturn(this.portletEntity);
        expect(this.portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        expect(this.portletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(this.portletWindow);
        expect(this.portletWindow.getPortletWindowId()).andReturn(portletWindowId);
        
        this.replayAll();
        
        final IPortalRequestInfo requestInfo = this.portalUrlProvider.getPortalRequestInfo(mockRequest);
        
        this.verifyAll();

        assertEquals(expected.getTargetedLayoutNodeId(), requestInfo.getTargetedLayoutNodeId());
        assertEquals(expected.getLayoutParameters(), requestInfo.getLayoutParameters());
        assertEquals(expected.getPortalParameters(), requestInfo.getPortalParameters());
        assertEquals(expected.getUrlState(), requestInfo.getUrlState());
        assertEquals(expected.getUrlType(), requestInfo.getUrlType());
        
        final IPortletRequestInfo expectedPortletRequestInfo = expected.getPortletRequestInfo();
        final IPortletRequestInfo portletRequestInfo = requestInfo.getPortletRequestInfo();
        assertRequestInfoEquals(expectedPortletRequestInfo, portletRequestInfo);
    }

    private MockPortletWindowId setPortletUrlGenerationByPortletId(final MockHttpServletRequest mockRequest, String nodeId, String subscribeId) {
        final MockPortletWindowId portletWindowId = new MockPortletWindowId("pw1");
        final MockPortletEntityId portletEntityId = new MockPortletEntityId("pe1");
        final MockPortletDefinitionId portletDefinitionId = new MockPortletDefinitionId("pd1");
        
        expect(this.userInstanceManager.getUserInstance(mockRequest)).andReturn(this.userInstance).times(2);
        expect(this.userInstance.getPreferencesManager()).andReturn(this.userPreferencesManager).times(2);
        expect(this.userPreferencesManager.getUserLayoutManager()).andReturn(this.userLayoutManager).times(2);
        expect(this.userLayoutManager.getUserLayout()).andReturn(this.userLayout);
        expect(this.portletWindowRegistry.getPortletWindow(mockRequest, portletWindowId)).andReturn(this.portletWindow).times(2);
        expect(this.portletWindow.getPortletWindowId()).andReturn(portletWindowId);
        expect(this.portletWindow.getPortletEntityId()).andReturn(portletEntityId);
        expect(this.portletEntityRegistry.getPortletEntity(portletEntityId)).andReturn(this.portletEntity);
        expect(this.portletEntity.getChannelSubscribeId()).andReturn(subscribeId);
        expect(this.userLayout.findNodeId(isA(XPathExpression.class))).andReturn(nodeId);
        expect(this.userLayoutManager.getNode(nodeId)).andReturn(this.userLayoutNodeDescription);
        expect(this.portalRequestUtils.getOriginalPortalRequest(mockRequest)).andReturn(mockRequest);
        expect(this.userLayoutNodeDescription.getId()).andReturn(nodeId);
        expect(this.portletWindow.getWindowState()).andReturn(null);
        expect(this.portletEntity.getPortletDefinitionId()).andReturn(portletDefinitionId);
        expect(this.portletDefinitionRegistry.getPortletDefinition(portletDefinitionId)).andReturn(this.portletDefinition);
        expect(this.portletDefinition.getChannelDefinition()).andReturn(this.channelDefinition);
        expect(this.channelDefinition.getFName()).andReturn("fname");
        
        return portletWindowId;
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        
        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42/", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42/max", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42/max/", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdInvalidStateUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42/invalid/", "n42", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("s3");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "n2";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.NORMAL;
        expected.urlType = UrlType.RENDER;
        
        final Map<String, String[]> params = new ParameterMap();
        params.put("pltC_t", new String[] { "fname.s3" });

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/n2/normal/render.uP", params, "s3", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdUrlGeneration() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal");
        
        final String nodeId = "n2";
        final String subscribeId = "s3";
        final MockPortletWindowId portletWindowId = this.setPortletUrlGenerationByPortletId(mockRequest, nodeId, subscribeId);
        
        this.replayAll();
        
        final IPortletPortalUrl portletUrl = this.portalUrlProvider.getPortletUrl(TYPE.RENDER, mockRequest, portletWindowId);
        
        final String urlString = portletUrl.getUrlString();
        
        this.verifyAll();
        
        assertEquals("/uPortal/f/n2/normal/render.uP?pltC_t=fname.s3", urlString);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedActionUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("s3");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "n2";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;
        expected.urlType = UrlType.ACTION;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/n2/p/fname.s3/max/action.uP", "s3", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedActionUrlGeneration() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal");
        
        final String nodeId = "n2";
        final String subscribeId = "s3";
        final MockPortletWindowId portletWindowId = this.setPortletUrlGenerationByPortletId(mockRequest, nodeId, subscribeId);
        
        this.replayAll();
        
        final IPortletPortalUrl portletUrl = this.portalUrlProvider.getPortletUrl(TYPE.ACTION, mockRequest, portletWindowId);
        
        portletUrl.setWindowState(WindowState.MAXIMIZED);
        
        final String urlString = portletUrl.getUrlString();
        
        this.verifyAll();
        
        assertEquals("/uPortal/f/n2/p/fname.s3/max/action.uP", urlString);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMinimizedRenderUrl() throws Exception {
        final Map<String, List<String>> portletParams = new LinkedHashMap<String, List<String>>();
        portletParams.put("action", Arrays.asList("dashboard"));
        
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("s3");
        expectedPortletRequestInfo.windowState = WindowState.MINIMIZED;
        expectedPortletRequestInfo.setPortletParameters(portletParams);
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "n2";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.NORMAL;
        expected.urlType = UrlType.RENDER;
        
        final Map<String, String[]> params = new ParameterMap();
        params.put("pltC_t", new String[] { "fname.s3" });
        params.put("pltC_s", new String[] { "minimized" });
        params.put("pltP_action", new String[] { "dashboard" });

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/n2/normal/render.uP", params, "s3", expected);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMinimizedRenderUrlGeneration() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal");
        
        final String nodeId = "n2";
        final String subscribeId = "s3";
        final MockPortletWindowId portletWindowId = this.setPortletUrlGenerationByPortletId(mockRequest, nodeId, subscribeId);
        
        this.replayAll();
        
        final IPortletPortalUrl portletUrl = this.portalUrlProvider.getPortletUrl(TYPE.RENDER, mockRequest, portletWindowId);
        
        portletUrl.setWindowState(WindowState.MINIMIZED);
        portletUrl.setPortletParameter("action", "dashboard");
        
        final String urlString = portletUrl.getUrlString();
        
        this.verifyAll();
        
        assertEquals("/uPortal/f/n2/normal/render.uP?pltC_t=fname.s3&pltC_s=minimized&pltP_action=dashboard", urlString);
    }
    
    @Test
    public void testSingleFolderPortletFnameSubscribeIdMaximizedInvalidTypeUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/folderName/p/portletName.n42/max/invalid.uP", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        
        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdMaximizedUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/max", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdMaximizedUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/max/", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdInvalidStateUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/invalid/", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdMaximizedActionUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;
        expected.urlType = UrlType.ACTION;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/max/action.uP", "n42", expected);
    }
    
    @Test
    public void testMultipleFolderPortletFnameSubscribeIdMaximizedInvalidTypeUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.targetedLayoutNodeId = "folderName";
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/f/baseFolder/folderName/p/portletName.n42/max/invalid.uP", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;
        
        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdMaximizedUrlNoSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/max", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdMaximizedUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/max/", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdInvalidStateUrlWithSlash() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/invalid/", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdMaximizedActionUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;
        expected.urlType = UrlType.ACTION;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/max/action.uP", "n42", expected);
    }
    
    @Test
    public void testNoFolderPortletFnameSubscribeIdMaximizedInvalidTypeUrl() throws Exception {
        MockPortletRequestInfo expectedPortletRequestInfo = new MockPortletRequestInfo();
        expectedPortletRequestInfo.targetWindowId = new MockPortletWindowId("n42");
        
        MockPortalRequestInfo expected = new MockPortalRequestInfo();
        expected.portletRequestInfo = expectedPortletRequestInfo;
        expected.urlState = UrlState.MAX;

        this.testFolderPortletFnameSubscribeIdUrlHelper("/uPortal/p/portletName.n42/max/invalid.uP", "n42", expected);
    }
    
    private void assertRequestInfoEquals(final IPortletRequestInfo expectedPortletRequestInfo, final IPortletRequestInfo portletRequestInfo) {
        if (expectedPortletRequestInfo != null) {
            assertNotNull(portletRequestInfo);
            assertEquals(expectedPortletRequestInfo.getTargetWindowId(), portletRequestInfo.getTargetWindowId());
            assertEquals(expectedPortletRequestInfo.getPortletMode(), portletRequestInfo.getPortletMode());
            assertEquals(expectedPortletRequestInfo.getWindowState(), portletRequestInfo.getWindowState());
            assertEquals(expectedPortletRequestInfo.getPortletParameters(), portletRequestInfo.getPortletParameters());
            assertEquals(expectedPortletRequestInfo.getPortletParameters(), portletRequestInfo.getPortletParameters());
            
            final IPortletRequestInfo expectedDelegatePortletRequestInfo = expectedPortletRequestInfo.getDelegatePortletRequestInfo();
            final IPortletRequestInfo delegatePortletRequestInfo = portletRequestInfo.getDelegatePortletRequestInfo();
            if (expectedDelegatePortletRequestInfo != null) {
                this.assertRequestInfoEquals(expectedDelegatePortletRequestInfo, delegatePortletRequestInfo);
            }
            else {
                assertNull(delegatePortletRequestInfo);
            }
        }
        else {
            assertNull(portletRequestInfo);
        }
    }
}
