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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.Cookie;

import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for {@link JpaPortletCookieDaoImpl}.
 * 
 * @author Nicholas Blair
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class JpaPortletCookieDaoImplTest extends BaseJpaDaoTest {
	
    @Autowired
	private IPortletCookieDao portletCookieDao;

    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

	/**
	 * 
	 */
	@Test
	public void testPortalCookieLifeCycle() {
        final String value = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
        		final IPortalCookie portalCookie = portletCookieDao.createPortalCookie(1);
        		assertNotNull(portalCookie);
        		
        		final String cookieValue = portalCookie.getValue();
        		assertNotNull(cookieValue);
        		assertEquals(40, cookieValue.length());
        		assertEquals(0, portalCookie.getPortletCookies().size());
        		assertNotNull(portalCookie.getCreated());
        		assertNotNull(portalCookie.getExpires());
        		
        		return cookieValue;
            }
        });

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IPortalCookie portalCookie = portletCookieDao.getPortalCookie(value);
                assertNotNull(portalCookie);
                
                Cookie cookie2 = new Cookie("cookieName2", "cookieValue2");
                
                portletCookieDao.addOrUpdatePortletCookie(portalCookie, cookie2);
                
                return null;
            }
        });

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IPortalCookie portalCookie = portletCookieDao.getPortalCookie(value);
                assertNotNull(portalCookie);
                
                long expirationDelay = portalCookie.getExpires().getTime() - System.currentTimeMillis();
                if (expirationDelay > 0) {
                    Thread.sleep(expirationDelay);
                }
                
                portletCookieDao.purgeExpiredCookies();
                
                return null;
            }
        });

        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IPortalCookie portalCookie = portletCookieDao.getPortalCookie(value);
                assertNull(portalCookie);
                
                return null;
            }
        });
	}
}
