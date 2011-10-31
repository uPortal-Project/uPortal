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

package org.jasig.portal.user;

import java.util.Map;

import org.jasig.portal.events.LogoutEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

/**
 * On configuration this bean will load all beans of type {@link CachingPersonAttributeDaoImpl} and store references to
 * them. When a {@link UserLoggedOutPortalEvent} or {@link UserSessionDestroyedPortalEvent} are recieved this code will
 * iterate over all caching DAOs and call the {@link CachingPersonAttributeDaoImpl#removeUserAttributes(String)} method
 * to clear out the caches.
 * 
 * @author Eric Dalquist
 * @version $Revision: 1.1 $
 */
public class UserAttributesCacheCleaner implements ApplicationListener<LogoutEvent>, ApplicationContextAware,
        InitializingBean {
    private ApplicationContext applicationContext;
    private Map<String, CachingPersonAttributeDaoImpl> cachingAttributeDaos;

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.cachingAttributeDaos = this.applicationContext.getBeansOfType(CachingPersonAttributeDaoImpl.class);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(LogoutEvent event) {
        final IPerson person = event.getPerson();
        final String userName = person.getUserName();

        for (final CachingPersonAttributeDaoImpl cachingAttributeDao : this.cachingAttributeDaos.values()) {
            cachingAttributeDao.removeUserAttributes(userName);
        }
    }
}
