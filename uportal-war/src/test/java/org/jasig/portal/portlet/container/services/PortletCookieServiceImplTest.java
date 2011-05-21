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

package org.jasig.portal.portlet.container.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.time.DateUtils;
import org.easymock.EasyMock;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link PortletCookieServiceImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
public class PortletCookieServiceImplTest {

	/**
	 * Control test invocation of {@link PortletCookieServiceImpl#updatePortalCookie(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
	 */
	@Test
	public void testUpdatePortletCookieControl() {
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		EasyMock.expect(portletCookieDao.createPortalCookie(PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		EasyMock.expect(portletCookieDao.updatePortalCookieExpiration(portalCookie, PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		EasyMock.replay(portletCookieDao);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		cookieService.updatePortalCookie(request, response);
		
		Cookie [] cookies = response.getCookies();
		Assert.assertNotNull(cookies);
		Assert.assertEquals(1, cookies.length);
		EasyMock.verify(portletCookieDao);
	}
	
	/**
	 * Test {@link PortletCookieServiceImpl#getOrCreatePortalCookie(javax.servlet.http.HttpServletRequest)}.
	 * that results in creating a new PortalCookie.
	 */
	@Test
	public void testGetOrCreatePortalCookieCreate() {
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		EasyMock.expect(portletCookieDao.createPortalCookie(PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		EasyMock.replay(portletCookieDao);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		cookieService.getOrCreatePortalCookie(request);
		
		EasyMock.verify(portletCookieDao);
	}
	
	/**
	 * Test {@link PortletCookieServiceImpl#getOrCreatePortalCookie(javax.servlet.http.HttpServletRequest)}.
	 * that results in returning an existing portalcookie from the request cookies
	 */
	@Test
	public void testGetOrCreatePortalCookieGetExistingFromRequestCookies() {
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		
		EasyMock.expect(portletCookieDao.getPortalCookie("ABCDEF")).andReturn(portalCookie);
		EasyMock.replay(portletCookieDao);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		Cookie [] cookies = new Cookie[1];
		Cookie cookie = new Cookie(IPortletCookieService.DEFAULT_PORTAL_COOKIE_NAME, "ABCDEF");
		cookies[0] = cookie;
		request.setCookies(cookies);
		
		IPortalCookie result = cookieService.getOrCreatePortalCookie(request);
		Assert.assertEquals(portalCookie, result);
		EasyMock.verify(portletCookieDao);
	}
	
	/**
	 * Test {@link PortletCookieServiceImpl#getOrCreatePortalCookie(javax.servlet.http.HttpServletRequest)}.
	 * that results in returning an existing portalcookie from the id stored in the session.
	 */
	@Test
	public void testGetOrCreatePortalCookieGetExistingFromSession() {
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		
		EasyMock.expect(portletCookieDao.getPortalCookie("ABCDEF")).andReturn(portalCookie);
		EasyMock.replay(portletCookieDao);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession().setAttribute(PortletCookieServiceImpl.SESSION_ATTRIBUTE__PORTAL_COOKIE_ID, "ABCDEF");
		
		IPortalCookie result = cookieService.getOrCreatePortalCookie(request);
		Assert.assertEquals(portalCookie, result);
		EasyMock.verify(portletCookieDao);
	}
	
	/**
	 * Control test for adding a portlet cookie: no existing portalCookie, portlet cookie requires persistence.
	 */
	@Test
	public void testAddCookieControl() { 
		Cookie portletCookie = new Cookie("somePortletCookieName", "somePortletCookieValue");
		// max age will trigger persistence
		portletCookie.setMaxAge(360);
		
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		
		
		EasyMock.expect(portletCookieDao.createPortalCookie(PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		EasyMock.expect(portletCookieDao.addOrUpdatePortletCookie(portalCookie, portletCookie)).andReturn(portalCookie);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		IPortletWindowId mockWindowId = EasyMock.createMock(IPortletWindowId.class);
		EasyMock.replay(portletCookieDao, mockWindowId);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		cookieService.addCookie(request, mockWindowId, portletCookie);
		
		EasyMock.verify(portletCookieDao, mockWindowId);
	}
	
	/**
	 * Control test for removing a portlet cookie.
	 * Logic is nearly identical to create, as both {@link IPortletCookieDao#addOrUpdatePortletCookie(IPortalCookie, Cookie)} is used
	 * in both scenarios.
	 */
	@Test
	public void testAddCookieRemove() { 
		Cookie portletCookie = new Cookie("somePortletCookieName", "somePortletCookieValue");
		// max age will trigger persistence removal
		portletCookie.setMaxAge(0);
		
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		
		EasyMock.expect(portletCookieDao.createPortalCookie(PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		EasyMock.expect(portletCookieDao.addOrUpdatePortletCookie(portalCookie, portletCookie)).andReturn(portalCookie);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		IPortletWindowId mockWindowId = EasyMock.createMock(IPortletWindowId.class);
		EasyMock.replay(portletCookieDao, mockWindowId);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		cookieService.addCookie(request, mockWindowId, portletCookie);
		
		EasyMock.verify(portletCookieDao, mockWindowId);
	}
	
	/**
	 * Control test for adding a portlet cookie with maxAge == -1, which results in a session-only cookie.
	 */
	@Test
	public void testAddCookieSessionOnly() { 
		Cookie portletCookie = new Cookie("somePortletCookieName", "somePortletCookieValue");
		// max age will trigger persistence
		portletCookie.setMaxAge(-1);
		
		IPortletCookieDao portletCookieDao = EasyMock.createMock(IPortletCookieDao.class);
		
		MockPortalCookie portalCookie = new MockPortalCookie();
		portalCookie.setValue("ABCDEF");
		
		EasyMock.expect(portletCookieDao.createPortalCookie(PortletCookieServiceImpl.DEFAULT_MAX_AGE)).andReturn(portalCookie);
		
		PortletCookieServiceImpl cookieService = new PortletCookieServiceImpl();
		cookieService.setPortletCookieDao(portletCookieDao);
		
		IPortletWindowId mockWindowId = EasyMock.createMock(IPortletWindowId.class);
		EasyMock.replay(portletCookieDao, mockWindowId);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		cookieService.addCookie(request, mockWindowId, portletCookie);
		
		Map<String, SessionOnlyPortletCookieImpl> sessionOnlyMap = cookieService.getSessionOnlyPortletCookieMap(request);	
		SessionOnlyPortletCookieImpl sessionOnlyCookie = sessionOnlyMap.get("somePortletCookieName");
		Assert.assertNotNull(sessionOnlyCookie);
		Assert.assertEquals(-1, sessionOnlyCookie.getMaxAge());
		Assert.assertEquals("somePortletCookieValue", sessionOnlyCookie.getValue());
		
		EasyMock.verify(portletCookieDao, mockWindowId);
	}
	
	/**
	 * Mock {@link IPortalCookie} used in these tests.
	 * 
	 * @author Nicholas Blair
	 * @version $Id$
	 */
	class MockPortalCookie implements IPortalCookie {

		private Date created = new Date();
		private Date expires = DateUtils.addHours(new Date(), 1);
		private Set<IPortletCookie> portletCookies = new HashSet<IPortletCookie>();
		private String value;
		
		@Override
		public Date getCreated() {
			return this.created;
		}
		@Override
		public Date getExpires() {
			return this.expires;
		}
		@Override
		public Set<IPortletCookie> getPortletCookies() {
			return this.portletCookies;
		}
		@Override
		public String getValue() {
			return this.value;
		}
		@Override
		public void setExpires(Date expires) {
			this.expires = expires;
		}
		public void setCreated(Date created) {
			this.created = created;
		}

		public void setPortletCookies(Set<IPortletCookie> portletCookies) {
			this.portletCookies = portletCookies;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
}
