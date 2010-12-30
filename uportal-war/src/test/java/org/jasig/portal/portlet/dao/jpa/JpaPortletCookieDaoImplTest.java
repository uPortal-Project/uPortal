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
package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.time.DateUtils;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.junit.Assert;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * Tests for {@link JpaPortletCookieDaoImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class JpaPortletCookieDaoImplTest extends AbstractJpaTests {
	
	private EntityManager entityManager;
	private JpaPortletCookieDaoImpl portletCookieDao;
	private JpaPortletTypeDao jpaChannelTypeDao;
    private JpaPortletDefinitionDao jpaPortletDefinitionDao;
    private JpaPortletEntityDao jpaPortletEntityDao;
	
    public JpaPortletCookieDaoImplTest() {
        this.setDependencyCheck(false);
    }
	/*
	 * (non-Javadoc)
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }

	/**
	 * @param entityManager the entityManager to set
	 */
	@PersistenceContext(unitName="uPortalPersistence")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	/**
	 * @param portletCookieDao the portletCookieDao to set
	 */
	public void setPortletCookieDao(JpaPortletCookieDaoImpl portletCookieDao) {
		this.portletCookieDao = portletCookieDao;
	}
	/**
	 * @param jpaChannelTypeDao the jpaChannelTypeDao to set
	 */
	public void setJpaChannelTypeDao(JpaPortletTypeDao jpaChannelTypeDao) {
		this.jpaChannelTypeDao = jpaChannelTypeDao;
	}
	/**
	 * @param jpaPortletDefinitionDao the jpaPortletDefinitionDao to set
	 */
	public void setJpaPortletDefinitionDao(
			JpaPortletDefinitionDao jpaPortletDefinitionDao) {
		this.jpaPortletDefinitionDao = jpaPortletDefinitionDao;
	}
	/**
	 * @param jpaPortletEntityDao the jpaPortletEntityDao to set
	 */
	public void setJpaPortletEntityDao(JpaPortletEntityDao jpaPortletEntityDao) {
		this.jpaPortletEntityDao = jpaPortletEntityDao;
	}

	/**
	 * 
	 */
	public void testPortalCookieLifeCycle() {
		IPortalCookie newCookie = this.portletCookieDao.createPortalCookie();
		Assert.assertNotNull(newCookie);
		final String cookieValue = newCookie.getValue();
		Assert.assertEquals(0, newCookie.getPortletCookies().size());
		Assert.assertEquals(40, newCookie.getValue().length());
		Assert.assertNotNull(newCookie.getCreated());
		Assert.assertNotNull(newCookie.getExpires());
		
		this.checkPoint();
		
		IPortalCookie retrieved = this.portletCookieDao.getPortalCookie(cookieValue);
		Assert.assertNotNull(retrieved);
		Assert.assertEquals(newCookie, retrieved);
		
		Date newExpiration = DateUtils.addHours(retrieved.getExpires(), 2);
		
		this.portletCookieDao.updatePortalCookieExpiration(retrieved, newExpiration);
		
		this.checkPoint();
		
		IPortalCookie retrieved2 = this.portletCookieDao.getPortalCookie(cookieValue);
		Assert.assertEquals(newExpiration, retrieved2.getExpires());
		Assert.assertEquals(retrieved.getCreated(), retrieved2.getCreated());
		
		this.portletCookieDao.deletePortalCookie(retrieved2);
		
		this.checkPoint();
		
		IPortalCookie gone = this.portletCookieDao.getPortalCookie(cookieValue);
		Assert.assertNull(gone);
	}
	
	/**
	 * 
	 */
	public void testPortletCookieLifeCycle() {
		final IPortletType channelType = this.jpaChannelTypeDao.createPortletType("BaseType", "foobar");
        this.checkPoint();
        
        //Create a definition
        IPortletDefinition chanDef1 = this.jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", IPortletRenderer.class.getName(), "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
        this.checkPoint();
        
        IPortletEntity portEnt1 = this.jpaPortletEntityDao.createPortletEntity(chanDef1.getPortletDefinitionId(), "chanSub1", 1);
        IPortletEntityId portletEntityId = portEnt1.getPortletEntityId();
        this.checkPoint();
        
        portEnt1 = this.jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
        Assert.assertNotNull(portEnt1);
        
        IPortalCookie portalCookie = this.portletCookieDao.createPortalCookie();
        final String portalCookieValue = portalCookie.getValue();
		Assert.assertNotNull(portalCookie);
		
		this.checkPoint();
		
		Cookie cookie = new Cookie("cookieName1", "cookieValue1");
		portalCookie = this.portletCookieDao.storePortletCookie(portalCookie, portletEntityId, cookie);
		
		this.checkPoint();
		
		portalCookie = this.portletCookieDao.getPortalCookie(portalCookieValue);
		Assert.assertEquals(1, portalCookie.getPortletCookies().size());
		IPortletCookie portletCookie1 = new ArrayList<IPortletCookie>(portalCookie.getPortletCookies()).get(0);
		Assert.assertEquals("cookieName1", portletCookie1.getName());
		Assert.assertEquals("cookieValue1", portletCookie1.getValue());
		Assert.assertFalse(portletCookie1.isSecure());
		
		cookie.setSecure(true);
		this.portletCookieDao.updatePortletCookie(portalCookie, portletEntityId, cookie);
		
		this.checkPoint();
		
		portalCookie = this.portletCookieDao.getPortalCookie(portalCookieValue);
		Assert.assertEquals(1, portalCookie.getPortletCookies().size());
		portletCookie1 = new ArrayList<IPortletCookie>(portalCookie.getPortletCookies()).get(0);
		Assert.assertEquals("cookieName1", portletCookie1.getName());
		Assert.assertEquals("cookieValue1", portletCookie1.getValue());
		Assert.assertTrue(portletCookie1.isSecure());
		
		Cookie cookie2 = new Cookie("cookieName2", "cookieValue2");
		portalCookie = this.portletCookieDao.storePortletCookie(portalCookie, portletEntityId, cookie2);
		
		this.checkPoint();
		
		portalCookie = this.portletCookieDao.getPortalCookie(portalCookieValue);
		Assert.assertEquals(2, portalCookie.getPortletCookies().size());
		
		this.portletCookieDao.deletePortletCookie(portalCookie, portletEntityId, cookie);
		
		this.checkPoint();
		
		portalCookie = this.portletCookieDao.getPortalCookie(portalCookieValue);
		Assert.assertEquals(1, portalCookie.getPortletCookies().size());
		
	}
	
	
	private void checkPoint() {
        this.entityManager.flush();
        this.entityManager.clear();
    }
}
