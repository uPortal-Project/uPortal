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
package org.jasig.portal.portlet.container.cache;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Nicholas Blair
 * @version $Id$
 */
public class CacheControlImplTest {

	@Test
	public void testSetEtagNoResponse() {
		CacheControlImpl cacheControl = new CacheControlImpl();
		cacheControl.setETag("123456");
	}
	
	@Test
	public void testSetEtagWithResponse() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		CacheControlImpl cacheControl = new CacheControlImpl(response);
		cacheControl.setETag("123456");
		
		Assert.assertEquals("123456", response.getHeader("ETag"));
	}
	
	@Test
	public void testSetEtagNullWithResponse() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		CacheControlImpl cacheControl = new CacheControlImpl(response);
		cacheControl.setETag(null);
		
		Assert.assertNull(response.getHeader("ETag"));
	}
	
	@Test
	public void testSetExpirationTimeSetsCacheControl() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		CacheControlImpl cacheControl = new CacheControlImpl(response);
		// 0 value for expiration time should not trigger header set
		cacheControl.setExpirationTime(0);
		Assert.assertNull(response.getHeader(CacheControlImpl.CACHE_CONTROL));
		cacheControl.setExpirationTime(300);
		Assert.assertEquals("private, max-age=300, must-revalidate", response.getHeader(CacheControlImpl.CACHE_CONTROL));
		// switching to public scope should trigger header overwrite
		cacheControl.setPublicScope(true);
		Assert.assertEquals("public, max-age=300, must-revalidate", response.getHeader(CacheControlImpl.CACHE_CONTROL));
	}
}
