/**
 * 
 */
package org.jasig.portal.portlet.url;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.Assert;

import org.jasig.portal.url.IPortalPortletUrl;
import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Test harness for {@link AlternatePortletUrlSyntaxProviderImpl}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class AlternatePortletUrlSyntaxProviderImplTest {

	/**
	 * Run {@link AlternatePortletUrlSyntaxProviderImpl#toPortletUrl(IPortalPortletUrl)} throw
	 * the control scenario - PortletMode.VIEW, RequestType.RENDER, WindowState.NORMAL, empty parameter map.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testToPortletUrlControl() throws Exception {
		IPortalPortletUrl mockPortletUrl = createMock(IPortalPortletUrl.class);
		expect(mockPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
		expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
		expect(mockPortletUrl.isAction()).andReturn(false);
		expect(mockPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
		replay(mockPortletUrl);
		
		PortletUrl result = AlternatePortletUrlSyntaxProviderImpl.toPortletUrl(mockPortletUrl);
		Assert.assertEquals(new HashMap<String, String[]>(), result.getParameters());
		Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
		Assert.assertEquals(RequestType.RENDER, result.getRequestType());
		Assert.assertEquals(WindowState.NORMAL, result.getWindowState());
		Assert.assertNull(result.getSecure());
		verify(mockPortletUrl);
	}
	
	/**
	 * Same test as {@link #testToPortletUrlControl()}, only set WindowState.MAXIMIZED.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testToPortletUrlMaximized() throws Exception {
		IPortalPortletUrl mockPortletUrl = createMock(IPortalPortletUrl.class);
		expect(mockPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
		expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
		expect(mockPortletUrl.isAction()).andReturn(false);
		expect(mockPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
		replay(mockPortletUrl);
		
		PortletUrl result = AlternatePortletUrlSyntaxProviderImpl.toPortletUrl(mockPortletUrl);
		Assert.assertEquals(new HashMap<String, String[]>(), result.getParameters());
		Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
		Assert.assertEquals(RequestType.RENDER, result.getRequestType());
		Assert.assertEquals(WindowState.MAXIMIZED, result.getWindowState());
		Assert.assertNull(result.getSecure());
		verify(mockPortletUrl);
	}
	
	/**
	 * Same test as {@link #testToPortletUrlControl()}, only set PortletMode.EDIT.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testToPortletUrlEdit() throws Exception {
		IPortalPortletUrl mockPortletUrl = createMock(IPortalPortletUrl.class);
		expect(mockPortletUrl.getPortalParameters()).andReturn(new HashMap<String, List<String>>());
		expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.EDIT);
		expect(mockPortletUrl.isAction()).andReturn(false);
		expect(mockPortletUrl.getWindowState()).andReturn(WindowState.NORMAL);
		replay(mockPortletUrl);
		
		PortletUrl result = AlternatePortletUrlSyntaxProviderImpl.toPortletUrl(mockPortletUrl);
		Assert.assertEquals(new HashMap<String, String[]>(), result.getParameters());
		Assert.assertEquals(PortletMode.EDIT, result.getPortletMode());
		Assert.assertEquals(RequestType.RENDER, result.getRequestType());
		Assert.assertEquals(WindowState.NORMAL, result.getWindowState());
		Assert.assertNull(result.getSecure());
		verify(mockPortletUrl);
	}
	
	/**
	 * Tests a IPortalPortletUrl that is an action on a maximized portlet with parameters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testToPortletUrlActionWithParameters() throws Exception {
		IPortalPortletUrl mockPortletUrl = createMock(IPortalPortletUrl.class);
		Map<String, List<String>> portalParameters = new HashMap<String, List<String>>();
		List<String> list1 = new ArrayList<String>();
		list1.add("value1a");
		list1.add("value2a");
		List<String> list2 = new ArrayList<String>();
		list2.add("value1b");
		list2.add("value2b");
		list2.add("value3b");
		List<String> list3 = new ArrayList<String>();
		portalParameters.put("list1", list1);
		portalParameters.put("list2", list2);
		portalParameters.put("list3", list3);
		expect(mockPortletUrl.getPortalParameters()).andReturn(portalParameters);
		expect(mockPortletUrl.getPortletMode()).andReturn(PortletMode.VIEW);
		expect(mockPortletUrl.isAction()).andReturn(true);
		expect(mockPortletUrl.getWindowState()).andReturn(WindowState.MAXIMIZED);
		replay(mockPortletUrl);
		
		PortletUrl result = AlternatePortletUrlSyntaxProviderImpl.toPortletUrl(mockPortletUrl);
		Map<String, String[]> resultParameters = result.getParameters();
		Assert.assertTrue(Arrays.deepEquals(new String [] { "value1a", "value2a" }, resultParameters.get("list1")));
		Assert.assertTrue(Arrays.deepEquals(new String [] { "value1b", "value2b", "value3b" }, resultParameters.get("list2")));
		Assert.assertTrue(Arrays.deepEquals(new String [] { }, resultParameters.get("list3")));
		Assert.assertEquals(PortletMode.VIEW, result.getPortletMode());
		Assert.assertEquals(RequestType.ACTION, result.getRequestType());
		Assert.assertEquals(WindowState.MAXIMIZED, result.getWindowState());
		Assert.assertNull(result.getSecure());
		verify(mockPortletUrl);
	}
	
	/**
	 * Verify {@link AlternatePortletUrlSyntaxProviderImpl#mergeWithPortletUrl(IPortalPortletUrl, PortletUrl)}
	 * throws expected exception for null arguments.
	 * @throws Exception
	 */
	@Test
	public void testMergeWithPortletUrlNullArguments() throws Exception {
		IPortalPortletUrl mockPortalPortletUrl = createMock(IPortalPortletUrl.class);
		replay(mockPortalPortletUrl);
		PortletUrl emptyPortletUrl = new PortletUrl();
		
		try {
			AlternatePortletUrlSyntaxProviderImpl.mergeWithPortletUrl(null, emptyPortletUrl);
			Assert.fail("expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
			//success
		}
		
		try {
			AlternatePortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, null);
			Assert.fail("expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
			//success
		}
	}
	
	/**
	 * Control case is {@link PortletUrl} with empty parameters map,
	 * PortletMode.VIEW, RequestType.RENDER, WindowState.NORMAL.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMergeWithPortletUrlControl() throws Exception {
		PortletUrl portletUrl = new PortletUrl();
		portletUrl.setParameters(new HashMap<String, String[]>());
		portletUrl.setPortletMode(PortletMode.VIEW);
		portletUrl.setRequestType(RequestType.RENDER);
		portletUrl.setWindowState(WindowState.NORMAL);
		
		IPortalPortletUrl mockPortalPortletUrl = createMock(IPortalPortletUrl.class);
		mockPortalPortletUrl.setPortletMode(PortletMode.VIEW);
		expectLastCall();
		mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
		expectLastCall();
		replay(mockPortalPortletUrl);
		
		AlternatePortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
		
		verify(mockPortalPortletUrl);
	}
	
	/**
	 * Same as {@link #testMergeWithPortletUrlControl()}, only using RequestType.ACTION.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMergeWithPortletUrlAction() throws Exception {
		PortletUrl portletUrl = new PortletUrl();
		portletUrl.setParameters(new HashMap<String, String[]>());
		portletUrl.setPortletMode(PortletMode.VIEW);
		portletUrl.setRequestType(RequestType.ACTION);
		portletUrl.setWindowState(WindowState.NORMAL);
		
		IPortalPortletUrl mockPortalPortletUrl = createMock(IPortalPortletUrl.class);
		mockPortalPortletUrl.setAction(true);
		expectLastCall();
		mockPortalPortletUrl.setPortletMode(PortletMode.VIEW);
		expectLastCall();
		mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
		expectLastCall();
		replay(mockPortalPortletUrl);
		
		AlternatePortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
		
		verify(mockPortalPortletUrl);
	}
	
	/**
	 * Same as {@link #testMergeWithPortletUrlAction()}, only adds 
	 * request parameters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMergeWithPortletUrlActionWithParams() throws Exception {
		PortletUrl portletUrl = new PortletUrl();
		Map<String, String[]> params = new HashMap<String, String[]>();
		params.put("key1", new String [] { "value1", "value2" });
		params.put("key2", new String [] { "value1", "value2", "value3" });
		portletUrl.setParameters(params);
		portletUrl.setPortletMode(PortletMode.EDIT);
		portletUrl.setRequestType(RequestType.ACTION);
		portletUrl.setWindowState(WindowState.NORMAL);
		
		IPortalPortletUrl mockPortalPortletUrl = createMock(IPortalPortletUrl.class);
		mockPortalPortletUrl.setAction(true);
		expectLastCall();
		mockPortalPortletUrl.setPortletMode(PortletMode.EDIT);
		expectLastCall();
		mockPortalPortletUrl.setWindowState(WindowState.NORMAL);
		expectLastCall();
		mockPortalPortletUrl.setPortalParameter("key1", new String [] { "value1", "value2" });
		mockPortalPortletUrl.setPortalParameter("key2", new String [] { "value1", "value2", "value3" });
		replay(mockPortalPortletUrl);
		
		AlternatePortletUrlSyntaxProviderImpl.mergeWithPortletUrl(mockPortalPortletUrl, portletUrl);
		
		verify(mockPortalPortletUrl);
	}
}
