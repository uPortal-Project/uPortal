/**
 * 
 */
package org.jasig.portal.web.skin;

import java.io.InputStream;

import javax.servlet.ServletContext;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for {@link ResourcesDaoImpl}.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class ResourcesDaoImplTest {

	/**
	 * Verify expected results with "org/jasig/portal/web/skin/resources1.xml"
	 * 
	 */
	@Test
	public void testControl() throws Exception {
		InputStream testResourcesStream = new ClassPathResource("org/jasig/portal/web/skin/resources1.xml").getInputStream();
		ServletContext mockContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(mockContext.getResourceAsStream(EasyMock.isA(String.class))).andReturn(testResourcesStream);
		EasyMock.replay(mockContext);
		
		ResourcesDaoImpl resourcesDao = new ResourcesDaoImpl();
		resourcesDao.setServletContext(mockContext);
		Resources result = resourcesDao.getResources("media/path/to/skin.xml");
		Assert.assertNotNull(result);
		Assert.assertEquals(7, result.getCss().size());
		Assert.assertEquals(7, result.getJs().size());
		EasyMock.verify(mockContext);
	}
	
	/**
	 * Verify expected {@link IllegalArgumentException}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSkinNotFound() throws Exception {
		ServletContext mockContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(mockContext.getResourceAsStream(EasyMock.isA(String.class))).andReturn(null);
		EasyMock.replay(mockContext);
		
		ResourcesDaoImpl resourcesDao = new ResourcesDaoImpl();
		resourcesDao.setServletContext(mockContext);
		try {
			resourcesDao.getResources("media/path/to/skin.xml");
			Assert.fail("expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
			// success
		}
	}
}
