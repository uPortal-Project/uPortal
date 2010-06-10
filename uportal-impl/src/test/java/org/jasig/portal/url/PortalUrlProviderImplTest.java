/**
 * 
 */
package org.jasig.portal.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpression;

import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.easymock.EasyMock;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
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

    /*
f/folderName
f/folderName/
f/folderName/p/portletName
f/folderName/p/portletName/
f/folderName/p/portletName.subscribeId
f/folderName/p/portletName.subscribeId/
f/folderName/p/portletName.subscribeId/state
f/folderName/p/portletName.subscribeId/state/
f/folderName/p/portletName.subscribeId/state/render.uP
f/folderName/folder2
f/folderName/folder2/
f/folderName/folder2/p/portletName
f/folderName/folder2/p/portletName/
f/folderName/folder2/p/portletName.subscribeId
f/folderName/folder2/p/portletName.subscribeId/
f/folderName/folder2/p/portletName.subscribeId/state
f/folderName/folder2/p/portletName.subscribeId/state/
f/folderName/folder2/p/portletName.subscribeId/state/render.uP
p/portletName
p/portletName/
p/portletName.subscribeId
p/portletName.subscribeId/
p/portletName.subscribeId/state
p/portletName.subscribeId/state/
p/portletName.subscribeId/state/render.uP
     */
    
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
         
         IPerson person = createMock(IPerson.class);
         expect(person.getID()).andReturn(1);
         replay(person);
         
         IUserInstance mockUserInstance = createMock(IUserInstance.class);
         expect(mockUserInstance.getPerson()).andReturn(person);
         replay(mockUserInstance);
         
         IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
         expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
         replay(mockUserInstanceInstanceManager);
         
         IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
         replay(portletEntityId);
         
         IPortletEntity portletEntity = createMock(IPortletEntity.class);
         expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
         replay(portletEntity);
         
         IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
         expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
         replay(portletEntityRegistry);
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         
         IPortletWindow portletWindow = createMock(IPortletWindow.class);
         expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
         replay(portletWindow);
         
         IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
         replay(mockPortletWindowRegistry);         
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletEntityRegistry(portletEntityRegistry);
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserLayoutManager userLayoutManager = createMock(IUserLayoutManager.class);
        expect(userLayoutManager.getSubscribeId("weather")).andReturn("ctf1");
        replay(userLayoutManager);
        
        IUserPreferencesManager userPreferencesManager = createMock(IUserPreferencesManager.class);
        expect(userPreferencesManager.getUserLayoutManager()).andReturn(userLayoutManager);
        replay(userPreferencesManager);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPreferencesManager()).andReturn(userPreferencesManager);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("ctf1", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);         
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
        IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
        Assert.assertEquals(UrlState.NORMAL, requestInfo.getUrlState());
        Assert.assertEquals("ctf1", requestInfo.getTargetedChannelSubscribeId());
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
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
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
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
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
         
         IPerson person = createMock(IPerson.class);
         expect(person.getID()).andReturn(1);
         replay(person);
         
         IUserLayoutManager userLayoutManager = createMock(IUserLayoutManager.class);
         expect(userLayoutManager.getSubscribeId("weather")).andReturn("ctf1");
         replay(userLayoutManager);
         
         IUserPreferencesManager userPreferencesManager = createMock(IUserPreferencesManager.class);
         expect(userPreferencesManager.getUserLayoutManager()).andReturn(userLayoutManager);
         replay(userPreferencesManager);
         
         IUserInstance mockUserInstance = createMock(IUserInstance.class);
         expect(mockUserInstance.getPreferencesManager()).andReturn(userPreferencesManager);
         expect(mockUserInstance.getPerson()).andReturn(person);
         replay(mockUserInstance);
         
         IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
         expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
         replay(mockUserInstanceInstanceManager);
         
         IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
         replay(portletEntityId);
         
         IPortletEntity portletEntity = createMock(IPortletEntity.class);
         expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
         replay(portletEntity);
         
         IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
         expect(portletEntityRegistry.getPortletEntity("ctf1", 1)).andReturn(portletEntity);
         replay(portletEntityRegistry);
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         
         IPortletWindow portletWindow = createMock(IPortletWindow.class);
         expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
         replay(portletWindow);
         
         IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
         replay(mockPortletWindowRegistry);   
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletEntityRegistry(portletEntityRegistry);
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         provider.setUserInstanceManager(mockUserInstanceInstanceManager);
         IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
         Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
         Assert.assertEquals("ctf1", requestInfo.getTargetedChannelSubscribeId());
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
         
         IPerson person = createMock(IPerson.class);
         expect(person.getID()).andReturn(1);
         replay(person);
         
         IUserLayoutManager userLayoutManager = createMock(IUserLayoutManager.class);
         expect(userLayoutManager.getSubscribeId("weather")).andReturn("ctf1");
         replay(userLayoutManager);
         
         IUserPreferencesManager userPreferencesManager = createMock(IUserPreferencesManager.class);
         expect(userPreferencesManager.getUserLayoutManager()).andReturn(userLayoutManager);
         replay(userPreferencesManager);
         
         IUserInstance mockUserInstance = createMock(IUserInstance.class);
         expect(mockUserInstance.getPreferencesManager()).andReturn(userPreferencesManager);
         expect(mockUserInstance.getPerson()).andReturn(person);
         replay(mockUserInstance);
         
         IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
         expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
         replay(mockUserInstanceInstanceManager);
         
         IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
         replay(portletEntityId);
         
         IPortletEntity portletEntity = createMock(IPortletEntity.class);
         expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
         replay(portletEntity);
         
         IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
         expect(portletEntityRegistry.getPortletEntity("ctf1", 1)).andReturn(portletEntity);
         replay(portletEntityRegistry);
         
         IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
         expect(mockPortletWindowId.getStringId()).andReturn("weather");
         replay(mockPortletWindowId);
         
         IPortletWindow portletWindow = createMock(IPortletWindow.class);
         expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
         replay(portletWindow);
         
         IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
         expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
         replay(mockPortletWindowRegistry);   
         
         IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
         expect(mockChannelDefinition.isPortlet()).andReturn(true);
         replay(mockChannelDefinition);
         IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
         expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
         replay(mockChannelRegistryStore);
         
         PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
         provider.setPortletEntityRegistry(portletEntityRegistry);
         provider.setPortletWindowRegistry(mockPortletWindowRegistry);
         provider.setChannelRegistryStore(mockChannelRegistryStore);
         provider.setUserInstanceManager(mockUserInstanceInstanceManager);
         IPortalRequestInfo requestInfo = provider.getPortalRequestInfo(mockRequest);
         Assert.assertEquals(UrlState.MAX, requestInfo.getUrlState());
         Assert.assertEquals("ctf1", requestInfo.getTargetedChannelSubscribeId());
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("ctf1", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("bookmarks");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);   
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("bookmarks")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);   
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);   
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);   
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IPerson person = createMock(IPerson.class);
        expect(person.getID()).andReturn(1);
        replay(person);
        
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPerson()).andReturn(person);
        replay(mockUserInstance);
        
        IUserInstanceManager mockUserInstanceInstanceManager = createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(mockRequest)).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        
        IPortletEntityId portletEntityId = createMock(IPortletEntityId.class);
        replay(portletEntityId);
        
        IPortletEntity portletEntity = createMock(IPortletEntity.class);
        expect(portletEntity.getPortletEntityId()).andReturn(portletEntityId);
        replay(portletEntity);
        
        IPortletEntityRegistry portletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(portletEntityRegistry.getPortletEntity("31", 1)).andReturn(portletEntity);
        replay(portletEntityRegistry);
        
        IPortletWindowId mockPortletWindowId = createMock(IPortletWindowId.class);
        expect(mockPortletWindowId.getStringId()).andReturn("weather");
        replay(mockPortletWindowId);
        
        IPortletWindow portletWindow = createMock(IPortletWindow.class);
        expect(portletWindow.getPortletWindowId()).andReturn(mockPortletWindowId);
        replay(portletWindow);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
        expect(mockPortletWindowRegistry.getOrCreateDefaultPortletWindow(mockRequest, portletEntityId)).andReturn(portletWindow);
        replay(mockPortletWindowRegistry);   
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.isPortlet()).andReturn(true);
        replay(mockChannelDefinition);
        IChannelRegistryStore mockChannelRegistryStore = createMock(IChannelRegistryStore.class);
        expect(mockChannelRegistryStore.getChannelDefinition("weather")).andReturn(mockChannelDefinition);
        replay(mockChannelRegistryStore);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setPortletEntityRegistry(portletEntityRegistry);
        provider.setPortletWindowRegistry(mockPortletWindowRegistry);
        provider.setChannelRegistryStore(mockChannelRegistryStore);
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        //IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.RENDER);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, String[]>());
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.RENDER);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, String[]>());
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.RENDER);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, String[]>());
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.HELP);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.RENDER);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, String[]>());
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.RENDER);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortalPortletUrl.getPortletParameters()).andReturn(new HashMap<String, String[]>());
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
        expect(mockPortalPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortalPortletUrl.getType()).andReturn(TYPE.ACTION);
        expect(mockPortalPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        Map<String, String[]> portletParameters = new ParameterMap();
        portletParameters.put("pp_action", new String[] { "addCity" });
        portletParameters.put("pp_zip", new String[] { "53706" });
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
     * Test layout URL targeting a folder.
     * 
     * @throws Exception
     */
    @Test
    public void testGenerateLayoutUrl() throws Exception {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContextPath("/uPortal/");
        mockRequest.setRequestURI("/uPortal/foobar/max/render.uP");
        
        ILayoutPortalUrl mockLayoutUrl = createMock(ILayoutPortalUrl.class);
        expect(mockLayoutUrl.isRenderInNormal()).andReturn(null);
        expect(mockLayoutUrl.isAction()).andReturn(true);
        expect(mockLayoutUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockLayoutUrl.getLayoutParameters()).andReturn(new HashMap<String, List<String>>());
        replay(mockLayoutUrl);
        
        ProviderSetupDetails details = new ProviderSetupDetails();
        details.setHttpServletRequest(mockRequest);
        details.setFolderName("somefolder");
        details.setFolderId("n32");
        PortalUrlProviderImpl provider = generateMockProviderForPortalUrl(details);
        String result = provider.generateLayoutUrl(mockRequest, mockLayoutUrl, "n32");
        
        Assert.assertEquals("/uPortal/somefolder/max/action.uP", result);
        verify(mockLayoutUrl);
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
        
        IUserLayout mockUserInstanceLayout = createMock(IUserLayout.class);
        // we have to tell EasyMock to expect ANY instance of XPathExpression as XPathExpression equals is based on instance equality
        expect(mockUserInstanceLayout.findNodeId(EasyMock.isA(XPathExpression.class))).andReturn(expressionText);
        replay(mockUserInstanceLayout);
        
        // BEGIN only expect IUserLayoutNodeDescription calls if folderName is defined
        IUserLayoutNodeDescription mockUserInstanceLayoutNodeDescription = createMock(IUserLayoutNodeDescription.class);
        if(null != details.getFolderName()) {
            expect(mockUserInstanceLayoutNodeDescription.getType()).andReturn(IUserLayoutNodeDescription.FOLDER);
            expect(mockUserInstanceLayoutNodeDescription.getId()).andReturn(details.getFolderName());
        }
        replay(mockUserInstanceLayoutNodeDescription);
        // END only expect IUserLayoutNodeDescription calls if folderName is defined
        
        IUserLayoutManager mockUserInstanceLayoutManager = createMock(IUserLayoutManager.class);
        expect(mockUserInstanceLayoutManager.getUserLayout()).andReturn(mockUserInstanceLayout);
        expect(mockUserInstanceLayoutManager.getNode(expressionText)).andReturn(mockUserInstanceLayoutNodeDescription);
        replay(mockUserInstanceLayoutManager);
        IUserPreferencesManager mockUserInstancePreferencesManager = createMock(IUserPreferencesManager.class);
        expect(mockUserInstancePreferencesManager.getUserLayoutManager()).andReturn(mockUserInstanceLayoutManager).times(2);
        replay(mockUserInstancePreferencesManager);
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPreferencesManager()).andReturn(mockUserInstancePreferencesManager).times(2);
        replay(mockUserInstance);
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
        IUserInstanceManager mockUserInstanceInstanceManager= createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(details.getHttpServletRequest())).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
    
        IPortletDefinitionRegistry mockPortletDefinitionRegistry = createMock(IPortletDefinitionRegistry.class);
        expect(mockPortletDefinitionRegistry.getPortletDefinition(mockPortletDefinitionId)).andReturn(mockPortletDefinition);
        replay(mockPortletDefinitionRegistry);
    
        IPortletEntityRegistry mockPortletEntityRegistry = createMock(IPortletEntityRegistry.class);
        expect(mockPortletEntityRegistry.getPortletEntity(mockPortletEntityId)).andReturn(mockPortletEntity);
        replay(mockPortletEntityRegistry);
        
        IPortletWindowRegistry mockPortletWindowRegistry = createMock(IPortletWindowRegistry.class);
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
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        
        IUserLayout mockUserInstanceLayout = createMock(IUserLayout.class);
        // we have to tell EasyMock to expect ANY instance of XPathExpression as XPathExpression equals is based on instance equality
        expect(mockUserInstanceLayout.findNodeId(EasyMock.isA(XPathExpression.class))).andReturn(expressionText);
        replay(mockUserInstanceLayout);
        
        // BEGIN only expect IUserLayoutNodeDescription calls if folderName is defined
        IUserLayoutChannelDescription mockUserInstanceLayoutChannelDescription = createMock(IUserLayoutChannelDescription.class);
        if(null != details.getFolderName()) {
            expect(mockUserInstanceLayoutChannelDescription.getType()).andReturn(IUserLayoutNodeDescription.FOLDER);
            expect(mockUserInstanceLayoutChannelDescription.getId()).andReturn(details.getFolderName());
        }
        if(null != details.getChannelFName()) {
            expect(mockUserInstanceLayoutChannelDescription.getFunctionalName()).andReturn(details.getChannelFName());
        }
        replay(mockUserInstanceLayoutChannelDescription);
        // END only expect IUserLayoutNodeDescription calls if folderName is defined
        
        IUserLayoutManager mockUserInstanceLayoutManager = createMock(IUserLayoutManager.class);
        if(null != details.getChannelFName()) {
        	expect(mockUserInstanceLayoutManager.getSubscribeId(details.getChannelFName())).andReturn(details.getChannelId());
        } 
        if (null != details.getChannelId())  {
        	expect(mockUserInstanceLayoutManager.getNode(details.getChannelId())).andReturn(mockUserInstanceLayoutChannelDescription);
        }
        expect(mockUserInstanceLayoutManager.getUserLayout()).andReturn(mockUserInstanceLayout);
        expect(mockUserInstanceLayoutManager.getNode(expressionText)).andReturn(mockUserInstanceLayoutChannelDescription);
        replay(mockUserInstanceLayoutManager);
        IUserPreferencesManager mockUserInstancePreferencesManager = createMock(IUserPreferencesManager.class);
        expect(mockUserInstancePreferencesManager.getUserLayoutManager()).andReturn(mockUserInstanceLayoutManager).times(3);
        replay(mockUserInstancePreferencesManager);
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPreferencesManager()).andReturn(mockUserInstancePreferencesManager).times(3);
        replay(mockUserInstance);
        
        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
        expect(mockChannelDefinition.getFName()).andReturn(details.getChannelFName());
        replay(mockChannelDefinition);
        IPortletEntity mockPortletEntity = createMock(IPortletEntity.class);
        expect(mockPortletEntity.getChannelSubscribeId()).andReturn(details.getChannelId()).times(2);
        replay(mockPortletEntity);
        // END transient mock objects

        // BEGIN mock dependencies for PortalUrlProviderImpl
        IUserInstanceManager mockUserInstanceInstanceManager= createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(details.getHttpServletRequest())).andReturn(mockUserInstance).times(3);
        replay(mockUserInstanceInstanceManager);
        // END mock dependencies for PortalUrlProviderImpl
        
        IPortalRequestUtils mockPortalRequestUtils = createMock(IPortalRequestUtils.class);
        expect(mockPortalRequestUtils.getOriginalPortalRequest(details.getHttpServletRequest())).andReturn(details.getHttpServletRequest());
        replay(mockPortalRequestUtils);
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
    protected PortalUrlProviderImpl generateMockProviderForPortalUrl(ProviderSetupDetails details) {
//        // BEGIN transient mock objects
//        String expressionText = "/layout/folder/folder[descendant::channel[@ID='" + details.getChannelId() + "']]/@ID";
//        
//        IUserLayout mockUserInstanceLayout = createMock(IUserLayout.class);
//        // we have to tell EasyMock to expect ANY instance of XPathExpression as XPathExpression equals is based on instance equality
//        expect(mockUserInstanceLayout.findNodeId(EasyMock.isA(XPathExpression.class))).andReturn(expressionText);
//        replay(mockUserInstanceLayout);
//        
//        // BEGIN only expect IUserLayoutNodeDescription calls if folderName is defined
        IUserLayoutNodeDescription mockUserInstanceLayoutNodeDescription = createMock(IUserLayoutNodeDescription.class);
        if(null != details.getFolderName()) {
            expect(mockUserInstanceLayoutNodeDescription.getType()).andReturn(IUserLayoutNodeDescription.FOLDER);
            expect(mockUserInstanceLayoutNodeDescription.getId()).andReturn(details.getFolderName());
        }
        replay(mockUserInstanceLayoutNodeDescription);
        // END only expect IUserLayoutNodeDescription calls if folderName is defined
//        
        IUserLayoutManager mockUserInstanceLayoutManager = createMock(IUserLayoutManager.class);
        expect(mockUserInstanceLayoutManager.getNode(details.getFolderId())).andReturn(mockUserInstanceLayoutNodeDescription);
//        if(null != details.getChannelFName()) {
//            expect(mockUserInstanceLayoutManager.getSubscribeId(details.getChannelFName())).andReturn(details.getChannelId());
//        } else {
//            expect(mockUserInstanceLayoutManager.getNode(details.getChannelId())).andReturn(mockUserInstanceLayoutNodeDescription);
//        }
//        expect(mockUserInstanceLayoutManager.getUserLayout()).andReturn(mockUserInstanceLayout);
//        expect(mockUserInstanceLayoutManager.getNode(expressionText)).andReturn(mockUserInstanceLayoutNodeDescription);
        replay(mockUserInstanceLayoutManager);
        IUserPreferencesManager mockUserInstancePreferencesManager = createMock(IUserPreferencesManager.class);
        expect(mockUserInstancePreferencesManager.getUserLayoutManager()).andReturn(mockUserInstanceLayoutManager).times(2);
        replay(mockUserInstancePreferencesManager);
        IUserInstance mockUserInstance = createMock(IUserInstance.class);
        expect(mockUserInstance.getPreferencesManager()).andReturn(mockUserInstancePreferencesManager).times(2);
        replay(mockUserInstance);
//        
//        IChannelDefinition mockChannelDefinition = createMock(IChannelDefinition.class);
//        expect(mockChannelDefinition.getFName()).andReturn(details.getChannelFName());
//        replay(mockChannelDefinition);
//        IPortletEntity mockPortletEntity = createMock(IPortletEntity.class);
//        expect(mockPortletEntity.getChannelSubscribeId()).andReturn(details.getChannelId()).times(2);
//        replay(mockPortletEntity);
//        // END transient mock objects

        // BEGIN mock dependencies for PortalUrlProviderImpl
        IUserInstanceManager mockUserInstanceInstanceManager= createMock(IUserInstanceManager.class);
        expect(mockUserInstanceInstanceManager.getUserInstance(details.getHttpServletRequest())).andReturn(mockUserInstance).times(2);
        replay(mockUserInstanceInstanceManager);
        // END mock dependencies for PortalUrlProviderImpl
        
        PortalUrlProviderImpl provider = new PortalUrlProviderImpl();
        provider.setUserInstanceManager(mockUserInstanceInstanceManager);
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
        private String folderId;
        
        /**
         * @return the folderId
         */
        public String getFolderId() {
            return this.folderId;
        }
        /**
         * @param folderId the folderId to set
         */
        public void setFolderId(String folderId) {
            this.folderId = folderId;
        }
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
