/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.pluto.container.PortletURLProvider;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortletPortalUrl;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImplTest extends TestCase {

    /**
     * Run {@link PortletUrlSyntaxProviderImpl#toPortletUrl(IPortalPortletUrl)} throw
     * the control scenario - PortletMode.VIEW, PortletURLProvider.TYPE.RENDER, WindowState.NORMAL, empty parameter map.
     * 
     * @throws Exception
     */
    @Test
    public void testToPortletUrlControl() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortletUrl.isAction()).andReturn(false);
        expect(mockPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
        replay(mockPortletUrl, portletWindowId);
        
        PortletUrl result = PortletUrlSyntaxProviderImpl.toPortletUrl(portletWindowId, mockPortletUrl);
        Assert.assertEquals(new HashMap<String, List<String>>(), result.getParameters());
        Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
        Assert.assertEquals(PortletURLProvider.TYPE.RENDER, result.getRequestType());
        Assert.assertEquals(WindowState.NORMAL, result.getWindowState());
        Assert.assertNull(result.getSecure());
        verify(mockPortletUrl, portletWindowId);
    }
    
    /**
     * Same test as {@link #testToPortletUrlControl()}, only set WindowState.MAXIMIZED.
     * 
     * @throws Exception
     */
    @Test
    public void testToPortletUrlMaximized() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortletUrl.isAction()).andReturn(false);
        expect(mockPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
        replay(mockPortletUrl, portletWindowId);
        
        PortletUrl result = PortletUrlSyntaxProviderImpl.toPortletUrl(portletWindowId, mockPortletUrl);
        Assert.assertEquals(new HashMap<String, List<String>>(), result.getParameters());
        Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
        Assert.assertEquals(PortletURLProvider.TYPE.RENDER, result.getRequestType());
        Assert.assertEquals(WindowState.MAXIMIZED, result.getWindowState());
        Assert.assertNull(result.getSecure());
        verify(mockPortletUrl, portletWindowId);
    }
    
    /**
     * Same test as {@link #testToPortletUrlControl()}, only set PortletMode.EDIT.
     * 
     * @throws Exception
     */
    @Test
    public void testToPortletUrlEdit() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        expect(mockPortletUrl.getPortletParameters()).andReturn(new HashMap<String, List<String>>());
        expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.EDIT);
        expect(mockPortletUrl.isAction()).andReturn(false);
        expect(mockPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
        replay(mockPortletUrl, portletWindowId);
        
        PortletUrl result = PortletUrlSyntaxProviderImpl.toPortletUrl(portletWindowId, mockPortletUrl);
        Assert.assertEquals(new HashMap<String, List<String>>(), result.getParameters());
        Assert.assertEquals(PortletMode.EDIT, result.getPortletMode());
        Assert.assertEquals(PortletURLProvider.TYPE.RENDER, result.getRequestType());
        Assert.assertEquals(WindowState.NORMAL, result.getWindowState());
        Assert.assertNull(result.getSecure());
        verify(mockPortletUrl, portletWindowId);
    }
    
    /**
     * Tests a IPortalPortletUrl that is an action on a maximized portlet with parameters.
     * 
     * @throws Exception
     */
    @Test
    public void testToPortletUrlActionWithParameters() throws Exception {
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        Map<String, List<String>> portletParameters = new HashMap<String, List<String>>();
        List<String> list1 = new ArrayList<String>();
        list1.add("value1a");
        list1.add("value2a");
        List<String> list2 = new ArrayList<String>();
        list2.add("value1b");
        list2.add("value2b");
        list2.add("value3b");
        List<String> list3 = new ArrayList<String>();
        portletParameters.put("list1", list1);
        portletParameters.put("list2", list2);
        portletParameters.put("list3", list3);
        expect(mockPortletUrl.getPortletParameters()).andReturn(portletParameters);
        expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
        expect(mockPortletUrl.isAction()).andReturn(true);
        expect(mockPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
        
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        replay(mockPortletUrl, portletWindowId);
        
        PortletUrl result = PortletUrlSyntaxProviderImpl.toPortletUrl(portletWindowId, mockPortletUrl);
        Map<String, List<String>> resultParameters = result.getParameters();
        Assert.assertEquals(Arrays.asList( "value1a", "value2a" ), resultParameters.get("list1"));
        Assert.assertEquals(Arrays.asList( "value1b", "value2b", "value3b" ), resultParameters.get("list2"));
        Assert.assertEquals(Arrays.asList( ), resultParameters.get("list3"));
        Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
        Assert.assertEquals(PortletURLProvider.TYPE.ACTION, result.getRequestType());
        Assert.assertEquals(WindowState.MAXIMIZED, result.getWindowState());
        Assert.assertNull(result.getSecure());
        verify(mockPortletUrl, portletWindowId);
    }
    
    /**
     * Verify {@link PortletUrlSyntaxProviderImpl#mergeWithPortletUrl(IPortalPortletUrl, PortletUrl)}
     * throws expected exception for null arguments.
     * @throws Exception
     */
    @Test
    public void testMergeWithPortletUrlNullArguments() throws Exception {
        IPortletPortalUrl mockPortletUrl = createMock(IPortletPortalUrl.class);
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        replay(mockPortletUrl, portletWindowId);
        
        PortletUrl emptyPortletUrl = new PortletUrl(portletWindowId);
        
        try {
            PortletUrlSyntaxProviderImpl.mergeWithPortletUrl(null, emptyPortletUrl);
            Assert.fail("expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            //success
        }
        
        try {
            PortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortletUrl, null);
            Assert.fail("expected IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
            //success
        }
        
        verify(mockPortletUrl, portletWindowId);
    }
    
    /**
     * Control case is {@link PortletUrl} with empty parameters map,
     * PortletMode.VIEW, PortletURLProvider.TYPE.RENDER, WindowState.NORMAL.
     * 
     * @throws Exception
     */
    @Test
    public void testMergeWithPortletUrlControl() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        mockPortalPortletUrl.setPortletMode(PortletMode.VIEW);
        expectLastCall();
        mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
        expectLastCall();
        replay(mockPortalPortletUrl, portletWindowId);
        
        PortletUrl portletUrl = new PortletUrl(portletWindowId);
        portletUrl.setParameters(new HashMap<String, List<String>>());
        portletUrl.setPortletMode(PortletMode.VIEW);
        portletUrl.setRequestType(PortletURLProvider.TYPE.RENDER);
        portletUrl.setWindowState(WindowState.NORMAL);
        
        PortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
        
        verify(mockPortalPortletUrl, portletWindowId);
    }
    
    /**
     * Same as {@link #testMergeWithPortletUrlControl()}, only using PortletURLProvider.TYPE.ACTION.
     * 
     * @throws Exception
     */
    @Test
    public void testMergeWithPortletUrlAction() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        mockPortalPortletUrl.setAction(true);
        expectLastCall();
        mockPortalPortletUrl.setPortletMode(PortletMode.VIEW);
        expectLastCall();
        mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
        expectLastCall();
        replay(mockPortalPortletUrl, portletWindowId);
        
        PortletUrl portletUrl = new PortletUrl(portletWindowId);
        portletUrl.setParameters(new HashMap<String, List<String>>());
        portletUrl.setPortletMode(PortletMode.VIEW);
        portletUrl.setRequestType(PortletURLProvider.TYPE.ACTION);
        portletUrl.setWindowState(WindowState.NORMAL);
        
        PortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
        
        verify(mockPortalPortletUrl, portletWindowId);
    }
    
    /**
     * Same as {@link #testMergeWithPortletUrlAction()}, only adds 
     * request parameters.
     * 
     * @throws Exception
     */
    @Test
    public void testMergeWithPortletUrlActionWithParams() throws Exception {
        IPortletWindowId portletWindowId = createMock(IPortletWindowId.class);
        
        IPortletPortalUrl mockPortalPortletUrl = createMock(IPortletPortalUrl.class);
        mockPortalPortletUrl.setAction(true);
        expectLastCall();
        mockPortalPortletUrl.setPortletMode(PortletMode.EDIT);
        expectLastCall();
        mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
        expectLastCall();
        mockPortalPortletUrl.setPortalParameter("key1", Arrays.asList( "value1", "value2" ));
        mockPortalPortletUrl.setPortalParameter("key2", Arrays.asList( "value1", "value2", "value3" ));
        replay(mockPortalPortletUrl, portletWindowId);

        PortletUrl portletUrl = new PortletUrl(portletWindowId);
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("key1", Arrays.asList( "value1", "value2" ));
        params.put("key2", Arrays.asList( "value1", "value2", "value3" ));
        portletUrl.setParameters(params);
        portletUrl.setPortletMode(PortletMode.EDIT);
        portletUrl.setRequestType(PortletURLProvider.TYPE.ACTION);
        portletUrl.setWindowState(WindowState.NORMAL);
        
        PortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
        
        verify(mockPortalPortletUrl, portletWindowId);
    }
}
