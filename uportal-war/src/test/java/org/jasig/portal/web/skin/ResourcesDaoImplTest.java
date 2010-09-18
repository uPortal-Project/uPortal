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
		Assert.assertNull(resourcesDao.getResources("media/path/to/skin.xml"));
	}
}
