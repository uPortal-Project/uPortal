/**
 * 
 */
package org.jasig.portal.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpression;

import org.easymock.EasyMock;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Test harness for {@link PortalUrlProviderImpl}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class PortalUrlProviderImplTest {
    
    /**
     * {@link PortalUrlProviderImpl#getPortalRequestInfo(HttpServletRequest)} will cache the
     * {@link IPortalRequestInfo} as a request attribute - verify this behavior.
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoAlreadyAnAttribute() throws Exception {
        PortalRequestInfoImpl requestInfo = new PortalRequestInfoImpl();
        requestInfo.setAction(false);
        requestInfo.setTargetedChannelSubscribeId("1");
        requestInfo.setTargetedLayoutNodeId("home");
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        replay(mockPortletWindowId);
        requestInfo.setTargetedPortletWindowId(mockPortletWindowId);
        requestInfo.setUrlState(UrlState.NORMAL);
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setAttribute(PortalUrlProviderImpl.class.getName() + ".PORTAL_REQUEST_INFO", requestInfo);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        IPortalRequestInfo retrieved = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(requestInfo, retrieved);
        
        verify(mockPortletWindowId);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/normal/weather.31/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoControl() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/weather.31/render.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry, mockChannelRegistryStore);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/normal/weather.31/
     </pre>
     * The missing "render.uP" is still valid - assert requestInfo.isAction still returns false.
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoDefaultIsRender() throws Exception {
         MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         mockRequest.setContextPath("/uPortal/");
         mockRequest.setRequestURI("/uPortal/home/normal/weather.31/");
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
         replay(mockPortletWindowRegistry);
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
         Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
         Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
         Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
         Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
         Assert.assertFalse(requestInfo.isAction());
         
         verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * * Verify expected results for:
     <pre>
     /uPortal/home/normal/weather/
     </pre>
     * Key points:
     <ul>
     <li>The missing "render.uP" is still valid - assert requestInfo.isAction still returns false.</li>
     <li>No subscribe id</li>
     </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoNoSubscribeId() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/weather/");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/normal/
     </pre>
     * Key points:
     <ul>
     <li>The missing "render.uP" is still valid - assert requestInfo.isAction still returns false.</li>
     <li>No subscribe id</li>
     <li>No targeted channel</li>
     </ul>
     * 
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoNoTargetedChannel() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        replay(mockPortletWindowRegistry);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals(null, requestInfo.getTargetedPortletWindowId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/
     </pre>
     * Key points:
     <ul>
     <li>The missing "render.uP" is still valid - assert requestInfo.isAction still returns false.</li>
     <li>No subscribe id</li>
     <li>No targeted channel</li>
     <li>No window state</li>
     </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoOnlyHomeFolder() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        replay(mockPortletWindowRegistry);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals(null, requestInfo.getTargetedPortletWindowId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for example 4:
     <pre>
     /uPortal/max/weather/render.uP
     </pre>
     * No folder here - just a maximized window state and a channel with no subscriber id.
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestMaxWeather() throws Exception {
         MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         mockRequest.setContextPath("/uPortal/");
         mockRequest.setRequestURI("/uPortal/max/weather/render.uP");
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
         replay(mockPortletWindowRegistry);
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
         Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
         Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
         Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
         Assert.assertEquals(null, requestInfo.getTargetedLayoutNodeId());
         Assert.assertFalse(requestInfo.isAction());
         
         verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for a modified example 4:
     <pre>
     /uPortal/max/weather/
     </pre>
     * No folder here - just a maximized window state and a channel with no subscriber id.
     * "render.uP" is not here either - assert not an action.
     * 
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestMaxWeatherMissingRender() throws Exception {
         MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         mockRequest.setContextPath("/uPortal/");
         mockRequest.setRequestURI("/uPortal/max/weather/");
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
         replay(mockPortletWindowRegistry);
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
         Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
         Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
         Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
         Assert.assertEquals(null, requestInfo.getTargetedLayoutNodeId());
         Assert.assertFalse(requestInfo.isAction());
         
         verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for example 5:
     <pre>
     /uPortal/max/bookmarks.ctf1/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestMaxBookmarksTransientSubscribeId() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/max/bookmarks.ctf1/render.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("bookmarks");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("bookmarks")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("bookmarks")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
        Assert.assertEquals("ctf1", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("bookmarks", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals(null, requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/normal/weather.31/action.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoControlAction() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/weather.31/action.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertTrue(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/max/weather.31/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoControlMaximized() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/max/weather.31/render.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
        Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/exclusive/weather.31/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoControlExclusive() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/exclusive/weather.31/render.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.EXCLUSIVE, requestInfo.getUrlState());
        Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/subtab1/subtab2/normal/weather.31/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoSubtabs() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/subtab1/subtab2/normal/weather.31/render.uP");
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindowId("weather")).andReturn(mockPortletWindowId);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals("31", requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals("weather", requestInfo.getTargetedPortletWindowId().getStringId());
        // needs to match "deepest" folder
        Assert.assertEquals("subtab2", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
        
        verify(mockPortletWindowId, mockPortletWindowRegistry);
    }
    
    /**
     * Verify expected results for:
     <pre>
     /uPortal/home/normal/render.uP
     </pre>
     * @throws Exception
     */
    @Test
    public void testGetPortalRequestInfoNoChannel() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/render.uP");
        
        //IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        //expect(mockPortletWindowId.getStringId()).andReturn(null);
        //replay(mockPortletWindowId);
        //ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        //expect(mockPortletWindowRegistry.getPortletWindowId(null)).andReturn(mockPortletWindowId);
        //replay(mockPortletWindowRegistry);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        //provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals(null, requestInfo.getTargetedChannelSubscribeId());
        Assert.assertEquals(null, requestInfo.getTargetedPortletWindowId());
        Assert.assertEquals("home", requestInfo.getTargetedLayoutNodeId());
        Assert.assertFalse(requestInfo.isAction());
    }
    
    /**
     * Pass null input into {@link PortalUrlProviderImpl#generatePortletUrl(javax.servlet.http.HttpServletRequest, IPortalPortletUrl, org.jasig.portal.portlet.om.IPortletWindowId)},
     * verify expected {@link IllegalArgumentException} thrown.
     * 
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlNullArguments() throws Exception {
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        
        // arguments are HttpServletRequest, IPortalPortletUrl, IPortletWindowId
        try {
            provider.generatePortletUrl(null, null, (IPortletWindowId)null);
            Assert.fail("expected IllegalArgumentException for null HttpServletRequest");
        } catch (IllegalArgumentException e) {
            // success
        }
        
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        try {
            provider.generatePortletUrl(mockRequest, null, (IPortletWindowId)null);
            Assert.fail("expected IllegalArgumentException for null IPortalPortletUrl");
        } catch (IllegalArgumentException e) {
            // success
        }
        
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        replay(mockPortletUrl);
        
        try {
            provider.generatePortletUrl(mockRequest, mockPortletUrl, (IPortletWindowId)null);
            Assert.fail("expected IllegalArgumentException for null IPortletWindowId");
        } catch (IllegalArgumentException e) {
            // success
        }
        verify(mockPortletUrl);    
    }
    
    /**
     * From http://www.ja-sig.org/wiki/display/UPC/Consistent+Portal+URLs
     * Tests "Example Url" #3:
     <pre>
     Renders a maximized portlet with fname weather and subscribe id 31 that exists on the home folder
    
    /uPortal/home/max/weather.31/render.uP?pltc_target=target
     </pre>
     *
     * context path: /uPortal/
     * channel fname: weather
     * channel subscribe id: 31
     * portlet window state: max
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlHomeMaxWeather() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
        expect(mockPortalPortletUrl.isAction()).andReturn(false);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("31");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.MAXIMIZED);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/uPortal/home/max/weather.31/render.uP?pltc_target=target", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * From http://www.ja-sig.org/wiki/display/UPC/Consistent+Portal+URLs
     * Tests "Example Url" #3:
     <pre>
     Renders a maximized portlet with fname weather and subscribe id 31 that exists on the home folder
    
    /uPortal/home/max/weather.31/render.uP?pltc_target=target
     </pre>
     *
     * context path: /uPortal/
     * channel fname: weather
     * channel subscribe id: 31
     * portlet window state: max
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlHomeMaxWeatherAlternateContextPath() throws Exception {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/p/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
        expect(mockPortalPortletUrl.isAction()).andReturn(false);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("31");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.MAXIMIZED);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/p/home/max/weather.31/render.uP?pltc_target=target", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * /uPortal/home/normal/weather.31/render.uP?pltc_target=target
     * 
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlHomeNormalWeather() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
        expect(mockPortalPortletUrl.isAction()).andReturn(false);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("31");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.NORMAL);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/uPortal/home/normal/weather.31/render.uP?pltc_target=target", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * /uPortal/home/normal/weather.31/render.uP?pltc_target=target&pltc_mode=help
     * 
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlHomeNormalWeatherHelp() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
        expect(mockPortalPortletUrl.isAction()).andReturn(false);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.HELP);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("31");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.NORMAL);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/uPortal/home/normal/weather.31/render.uP?pltc_target=target&pltc_mode=help", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * From http://www.ja-sig.org/wiki/display/UPC/Consistent+Portal+URLs
     * Tests "Example Url" #5:
     <pre>
     Renders a maximized portlet with fname weather and subscribe id 31 that exists on the home folder
    
    /uPortal/max/weather.ctf31/render.uP?pltc_target=target
     </pre>
     *
     * context path: /uPortal/
     * channel fname: weather
     * channel subscribe id: ctf31
     * portlet window state: max
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlTransientWeather() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
        expect(mockPortalPortletUrl.isAction()).andReturn(false);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("ctf31");
        details.setFolderName(null);
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.MAXIMIZED);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/uPortal/max/weather.ctf31/render.uP?pltc_target=target", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * From http://www.ja-sig.org/wiki/display/UPC/Consistent+Portal+URLs
     * Tests "Example Url" #7:
     <pre>
    Action URL for the weather portlet on a normal view of the home tab that is passing two parameters, action and zip. Since this is an action it would redirect to a normal URL rendering the home tab.

    /uPortal/normal/home/weather.31/action.uP?pltc_target=target&pltp_pp_action=addCity&pltp_pp_zip=53706
     </pre>
     *
     * context path: /uPortal/
     * channel fname: weather
     * channel subscribe id: 31
     * portlet window state: normal
     * @throws Exception
     */
    @Test
    public void testGeneratePortletUrlWeatherAction() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortalPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
        expect(mockPortalPortletUrl.isAction()).andReturn(true);
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        Map<String, List<String>> portletParameters = new HashMap<String, List<String>>();
        List<String> ppActionValues = new ArrayList<String>();
        ppActionValues.add("addCity");
        portletParameters.put("pp_action", ppActionValues);
        List<String> ppZipValues = new ArrayList<String>();
        ppZipValues.add("53706");
        portletParameters.put("pp_zip", ppZipValues);
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(portletParameters);
        replay(mockPortalPortletUrl);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("target");
        replay(mockPortletWindowId);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("weather");
        details.setChannelId("31");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        details.setPortletMode(PortletMode.VIEW);
        details.setPortletWindowId(mockPortletWindowId);
        details.setWindowState(WindowState.NORMAL);
        PortalUrlProviderImpl provider = generateMockProviderForPortletUrl(details);
        
        String result = provider.generatePortletUrl(mockRequest, mockPortalPortletUrl, mockPortletWindowId);
        Assert.assertEquals("/uPortal/home/normal/weather.31/action.uP?pltc_target=target&pltp_pp_action=addCity&pltp_pp_zip=53706", result);
        
        verify(mockPortalPortletUrl, mockPortletWindowId);
    }
    
    /**
     * Control test for {@link PortalUrlProviderImpl#generateChannelUrl(HttpServletRequest, IChannelPortalUrl)}.
     * Normal layoutstate, home folder, channelName, 32 subscribeId.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlControl() throws Exception {
    	 MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         mockRequest.setContextPath("/uPortal/");
         mockRequest.setRequestURI("/uPortal/home/normal/channelName.32/render.uP");
         
         IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
         expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("32");
         expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
         expect(mockPortalChannelUrl.isWorker()).andReturn(false);
         expect(mockPortalChannelUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
         replay(mockPortalChannelUrl);
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(false);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         ProviderSetupDetails details = new ProviderSetupDetails();
         details.setChannelFName("channelName");
         details.setChannelId("32");
         details.setFolderName("home");
         details.setHttpServletRequest(mockRequest);
         PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
         
         Assert.assertEquals("/uPortal/home/normal/channelName.32/render.uP", result);
         verify(mockPortalChannelUrl);
    }
    
    /**
     * Same test as {@link #testGenerateChannelUrlControl()}, only worker.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlWorker() throws Exception {
    	 MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         mockRequest.setContextPath("/uPortal/");
         mockRequest.setRequestURI("/uPortal/home/normal/channelName.32/worker.uP");
         
         IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
         expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("32");
         expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
         expect(mockPortalChannelUrl.isWorker()).andReturn(true);
         expect(mockPortalChannelUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
         replay(mockPortalChannelUrl);
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(false);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         ProviderSetupDetails details = new ProviderSetupDetails();
         details.setChannelFName("channelName");
         details.setChannelId("32");
         details.setFolderName("home");
         details.setHttpServletRequest(mockRequest);
         PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
         
         Assert.assertEquals("/uPortal/home/normal/channelName.32/worker.uP", result);
         verify(mockPortalChannelUrl);
    }
    
    /**
     * Same url as {@link #testGenerateChannelUrlControl()}, set
     * state to maximized.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlMaximized() throws Exception {
    	MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/max/channelName.32/render.uP");
        
        IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
        expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("32");
        expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
        expect(mockPortalChannelUrl.isWorker()).andReturn(false);
        expect(mockPortalChannelUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalChannelUrl);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(false);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("channelName");
        details.setChannelId("32");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
        
        Assert.assertEquals("/uPortal/home/max/channelName.32/render.uP", result);
        verify(mockPortalChannelUrl);
    }
    
    /**
     * Same url as {@link #testGenerateChannelUrlControl()}, add one portal parameter.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlParameter() throws Exception {
    	MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/channelName.32/render.uP");
        
        IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
        expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("32");
        expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
        expect(mockPortalChannelUrl.isWorker()).andReturn(false);
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        List<String> list1 = Arrays.asList(new String[] {"value1"});
        map.put("list1", list1);
        expect(mockPortalChannelUrl.getPortalParameters()).andReturn(map);
        replay(mockPortalChannelUrl);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(false);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("channelName");
        details.setChannelId("32");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
        
        Assert.assertEquals("/uPortal/home/normal/channelName.32/render.uP?uP_list1=value1", result);
        verify(mockPortalChannelUrl);
    }
    
    /**
     * Same url as {@link #testGenerateChannelUrlControl()}, add multiple portal parameters.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlParameters() throws Exception {
    	MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/home/normal/channelName.32/render.uP");
        
        IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
        expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("32");
        expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
        expect(mockPortalChannelUrl.isWorker()).andReturn(false);
        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        List<String> list1 = Arrays.asList(new String[] {"value1", "value2" });
        map.put("list1", list1);
        List<String> list2 = Arrays.asList(new String[] {});
        map.put("list2", list2);
        expect(mockPortalChannelUrl.getPortalParameters()).andReturn(map);
        replay(mockPortalChannelUrl);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(false);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("channelName");
        details.setChannelId("32");
        details.setFolderName("home");
        details.setHttpServletRequest(mockRequest);
        PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
        
        Assert.assertEquals("/uPortal/home/normal/channelName.32/render.uP?uP_list1=value1&uP_list1=value2&uP_list2=", result);
        verify(mockPortalChannelUrl);
    }
    
    /**
     * Test a maximied transient channel url.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateChannelUrlTransient() throws Exception {
    	MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/max/channelName.ctf32/render.uP");
        
        IChannelPortalUrl mockPortalChannelUrl = createMock(IChannelPortalUrl.class);
        expect(mockPortalChannelUrl.getChannelSubscribeId()).andReturn("ctf32");
        expect(mockPortalChannelUrl.getFName()).andReturn("channelName");
        expect(mockPortalChannelUrl.isWorker()).andReturn(false);
        expect(mockPortalChannelUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockPortalChannelUrl);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(false);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("channelName")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setChannelFName("channelName");
        details.setChannelId("ctf32");
        details.setFolderName(null);
        details.setHttpServletRequest(mockRequest);
        PortalUrlProviderImpl provider = generateMockProviderForChannelUrl(details);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        String result = provider.generateChannelUrl(mockRequest, mockPortalChannelUrl);
        
        Assert.assertEquals("/uPortal/max/channelName.ctf32/render.uP", result);
        verify(mockPortalChannelUrl);
    }
    
    /**
     * Not a test case.
     * Internal method to mock up a {@link PortalUrlProviderImpl} for testing 
     * {@link PortalUrlProviderImpl#generatePortletUrl(HttpServletRequest, IPortalPortletUrl, IPortletWindowId)}.
     * 
     * @param request
     * @param portletWindowId
     * @return
     */
    protected PortalUrlProviderImpl generateMockProviderForPortletUrl(ProviderSetupDetails details) {
        // BEGIN transient mock objects
        String expressionText = "/layout/folder/folder[descendant::channel[@ID='" + details.getChannelId() + "']]/@ID";
        
        IUserLayout mockUserLayout = createMock(IUserLayout.class);
        // we have to tell EasyMock to expect ANY instance of XPathExpression as XPathExpression equals is based on instance equality
        expect(mockUserLayout.findNodeId(EasyMock.isA(XPathExpression.class))).andReturn(expressionText);
        replay(mockUserLayout);
        
        // BEGIN only expect IUserLayoutNodeDescription calls if folderName is defined
        IUserLayoutNodeDescription mockUserLayoutNodeDescription = createMock(IUserLayoutNodeDescription.class);
        if(null != details.getFolderName()) {
            expect(mockUserLayoutNodeDescription.getType()).andReturn(IUserLayoutNodeDescription.FOLDER);
            expect(mockUserLayoutNodeDescription.getId()).andReturn(details.getFolderName());
        }
        replay(mockUserLayoutNodeDescription);
        // END only expect IUserLayoutNodeDescription calls if folderName is defined
        
        IUserLayoutManager mockUserLayoutManager = createMock(IUserLayoutManager.class);
        expect(mockUserLayoutManager.getUserLayout()).andReturn(mockUserLayout);
        expect(mockUserLayoutManager.getNode(expressionText)).andReturn(mockUserLayoutNodeDescription);
        replay(mockUserLayoutManager);
        IUserPreferencesManager mockUserPreferencesManager = createMock(IUserPreferencesManager.class);
        expect(mockUserPreferencesManager.getUserLayoutManager()).andReturn(mockUserLayoutManager).times(2);
        replay(mockUserPreferencesManager);
        IUserInstance mockUser = createMock(IUserInstance.class);
        expect(mockUser.getPreferencesManager()).andReturn(mockUserPreferencesManager).times(2);
        replay(mockUser);
        IPortletEntityId mockPortletEntityId = createMock(IPortletEntityId.class);
        expect(mockPortletEntityId.getStringId()).andReturn(details.getChannelId());
        replay(mockPortletEntityId);
        IPortletWindow mockPortletWindow = createMock(IPortletWindow.class);
        expect(mockPortletWindow.getPortletEntityId()).andReturn(mockPortletEntityId);
        expect(mockPortletWindow.getWindowState()).andReturn(details.getWindowState());
        expect(mockPortletWindow.getPortletMode()).andReturn(details.getPortletMode());
        replay(mockPortletWindow);
        
        IPortletDefinitionId mockPortletDefinitionId = createMock(IPortletDefinitionId.class);
        replay(mockPortletDefinitionId);
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.getFName()).andReturn(details.getChannelFName());
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IPortletDefinition mockPortletDefinition = createMock(IPortletDefinition.class);
        expect(mockPortletDefinition.getChannelDefinition()).andReturn(mockChannelDefinition);
        replay(mockPortletDefinition);
        IPortletEntity mockPortletEntity = createMock(IPortletEntity.class);
        expect(mockPortletEntity.getChannelSubscribeId()).andReturn(details.getChannelId()).times(2);
        expect(mockPortletEntity.getPortletDefinitionId()).andReturn(mockPortletDefinitionId);
        replay(mockPortletEntity);
        // END transient mock objects

        // BEGIN mock dependencies for PortalUrlProviderImpl
        IUserInstanceManager mockUserInstanceManager= createMock(IUserInstanceManager.class);
        expect(mockUserInstanceManager.getUserInstance(details.getHttpServletRequest())).andReturn(mockUser).times(2);
        replay(mockUserInstanceManager);
    
        IPortletDefinitionRegistry mockPortletDefinitionRegistry = createMock(IPortletDefinitionRegistry.class);
        expect(mockPortletDefinitionRegistry.getPortletDefinition(mockPortletDefinitionId)).andReturn(mockPortletDefinition);
        replay(mockPortletDefinitionRegistry);
    
        IPortletEntityRegistry mockPortletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(mockPortletEntityRegistry.getPortletEntity(mockPortletEntityId)).andReturn(mockPortletEntity);
        replay(mockPortletEntityRegistry);
        
        ITransientPortletWindowRegistry mockPortletWindowRegistry = createMock(ITransientPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getPortletWindow(details.getHttpServletRequest(), details.getPortletWindowId())).andReturn(mockPortletWindow);
        replay(mockPortletWindowRegistry);
        
        IPortalRequestUtils mockPortalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(mockPortalRequestUtils.getOriginalPortalRequest(details.getHttpServletRequest())).andReturn(details.getHttpServletRequest());
        replay(mockPortalRequestUtils);
        
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition(details.getChannelFName())).andReturn(mockChannelDefinition);
        // END mock dependencies for PortalUrlProviderImpl
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceManager);
        provider.setPortletDefinitionRegistry(mockPortletDefinitionRegistry);
        provider.setPortletEntityRegistry(mockPortletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setPortalRequestUtils(mockPortalRequestUtils);
        return provider;
    }
    
    /**
     * Not a test case.
     * Internal method to mock up a {@link PortalUrlProviderImpl} for testing
     * {@link PortalUrlProviderImpl#generateChannelUrl(HttpServletRequest, IChannelPortalUrl)}.
     * 
     * @param request
     * @param portletWindowId
     * @return
     */
    protected PortalUrlProviderImpl generateMockProviderForChannelUrl(ProviderSetupDetails details) {
        // BEGIN transient mock objects
    	String expressionText = "/layout/folder/folder[descendant::channel[@ID='" + details.getChannelId() + "']]/@ID";
        
        IUserLayout mockUserLayout = createMock(IUserLayout.class);
        // we have to tell EasyMock to expect ANY instance of XPathExpression as XPathExpression equals is based on instance equality
        expect(mockUserLayout.findNodeId(EasyMock.isA(XPathExpression.class))).andReturn(expressionText);
        replay(mockUserLayout);
        
        // BEGIN only expect IUserLayoutNodeDescription calls if folderName is defined
        IUserLayoutNodeDescription mockUserLayoutNodeDescription = createMock(IUserLayoutNodeDescription.class);
        if(null != details.getFolderName()) {
            expect(mockUserLayoutNodeDescription.getType()).andReturn(IUserLayoutNodeDescription.FOLDER);
            expect(mockUserLayoutNodeDescription.getId()).andReturn(details.getFolderName());
        }
        replay(mockUserLayoutNodeDescription);
        // END only expect IUserLayoutNodeDescription calls if folderName is defined
        
        IUserLayoutManager mockUserLayoutManager = createMock(IUserLayoutManager.class);
        if(null != details.getChannelFName()) {
        	expect(mockUserLayoutManager.getSubscribeId(details.getChannelFName())).andReturn(details.getChannelId());
        } else {
        	expect(mockUserLayoutManager.getNode(details.getChannelId())).andReturn(mockUserLayoutNodeDescription);
        }
        expect(mockUserLayoutManager.getUserLayout()).andReturn(mockUserLayout);
        expect(mockUserLayoutManager.getNode(expressionText)).andReturn(mockUserLayoutNodeDescription);
        replay(mockUserLayoutManager);
        IUserPreferencesManager mockUserPreferencesManager = createMock(IUserPreferencesManager.class);
        expect(mockUserPreferencesManager.getUserLayoutManager()).andReturn(mockUserLayoutManager).times(2);
        replay(mockUserPreferencesManager);
        IUserInstance mockUser = createMock(IUserInstance.class);
        expect(mockUser.getPreferencesManager()).andReturn(mockUserPreferencesManager).times(2);
        replay(mockUser);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.getFName()).andReturn(details.getChannelFName());
        replay(mockChannelDefinition);
        IPortletEntity mockPortletEntity = createMock(IPortletEntity.class);
        expect(mockPortletEntity.getChannelSubscribeId()).andReturn(details.getChannelId()).times(2);
        replay(mockPortletEntity);
        // END transient mock objects

        // BEGIN mock dependencies for PortalUrlProviderImpl
        IUserInstanceManager mockUserInstanceManager= createMock(IUserInstanceManager.class);
        expect(mockUserInstanceManager.getUserInstance(details.getHttpServletRequest())).andReturn(mockUser).times(2);
        replay(mockUserInstanceManager);
        // END mock dependencies for PortalUrlProviderImpl
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setUserInstanceManager(mockUserInstanceManager);
        return provider;
    }
    
    /**
     * Inner bean to hold all of the various settings needed
     * for setting up a mock {@link PortalUrlProviderImpl}.
     * 
     * @author Nicholas Blair, nblair@doit.wisc.edu
     *
     */
    protected static class ProviderSetupDetails {
        private HttpServletRequest httpServletRequest;
        private IPortletWindowId portletWindowId;
        private String folderName;
        private String channelId;
        private String channelFName;
        private WindowState windowState;
        private PortletMode portletMode;
        /**
         * @return the httpServletRequest
         */
        public HttpServletRequest getHttpServletRequest() {
            return httpServletRequest;
        }
        /**
         * @param httpServletRequest the httpServletRequest to set
         */
        public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
            this.httpServletRequest = httpServletRequest;
        }
        /**
         * @return the portletWindowId
         */
        public IPortletWindowId getPortletWindowId() {
            return portletWindowId;
        }
        /**
         * @param portletWindowId the portletWindowId to set
         */
        public void setPortletWindowId(IPortletWindowId portletWindowId) {
            this.portletWindowId = portletWindowId;
        }
        /**
         * @return the folderName
         */
        public String getFolderName() {
            return folderName;
        }
        /**
         * @param folderName the folderName to set
         */
        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }
        /**
         * @return the channelId
         */
        public String getChannelId() {
            return channelId;
        }
        /**
         * @param channelId the channelId to set
         */
        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }
        /**
         * @return the channelFName
         */
        public String getChannelFName() {
            return channelFName;
        }
        /**
         * @param channelFName the channelFName to set
         */
        public void setChannelFName(String channelFName) {
            this.channelFName = channelFName;
        }
        /**
         * @return the windowState
         */
        public WindowState getWindowState() {
            return windowState;
        }
        /**
         * @param windowState the windowState to set
         */
        public void setWindowState(WindowState windowState) {
            this.windowState = windowState;
        }
        /**
         * @return the portletMode
         */
        public PortletMode getPortletMode() {
            return portletMode;
        }
        /**
         * @param portletMode the portletMode to set
         */
        public void setPortletMode(PortletMode portletMode) {
            this.portletMode = portletMode;
        }
    }
    
}
